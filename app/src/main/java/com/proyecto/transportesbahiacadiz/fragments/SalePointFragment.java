package com.proyecto.transportesbahiacadiz.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.proyecto.transportesbahiacadiz.R;
import com.proyecto.transportesbahiacadiz.model.Centre;
import com.proyecto.transportesbahiacadiz.util.ConnectionClass;
import com.proyecto.transportesbahiacadiz.viewmodel.LiveDataCentre;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import serializable.Nucleo;

public class SalePointFragment extends Fragment implements OnMapReadyCallback {
    private View view;
    private GoogleMap map;
    private Spinner spinner;
    private ArrayAdapter<String> adapterCentre;
    private Nucleo[] nucleos;
    private String[] nombreNucleos;
    private int idNucleo;
    private List<String> latitudes;
    private List<String> longitudes;
    private String latitud;
    private String longitud;
    private LiveDataCentre liveDataCentre;
    private Button button;
    private Boolean locationPermissionsGranted = false;
    private ConnectionClass connectionClass;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float ZOOM = 12f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sale_point, container, false);
        connectionClass = new ConnectionClass(getContext());
        button = view.findViewById(R.id.btn_buscar);
        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                mapFragment.getMapAsync(SalePointFragment.this);
            }
        });

        spinner = view.findViewById(R.id.spinner_sale_point);
        new getPuntosVentaTask().execute();
        return view;
    }

    private void setSpinner() {
        adapterCentre = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, nombreNucleos);
        adapterCentre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterCentre);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                liveDataCentre = new ViewModelProvider((ViewModelStoreOwner) getContext()).get(LiveDataCentre.class);
                String nucleo = (String) parent.getSelectedItem();
                latitudes = new ArrayList<String>();
                longitudes = new ArrayList<String>();
                for (int i = 0; i < nucleos.length; i++) {
                    if (nucleo.equals(nucleos[i].getNombreNucleo())) {
                        idNucleo = nucleos[i].getIdNucleo();
                        new getMapaTask().execute();
                    }
                }
                liveDataCentre.getCentreList().observe((LifecycleOwner) getContext(), new Observer<List<Centre>>() {
                    @Override
                    public void onChanged(List<Centre> centres) {
                        for (Centre centre : centres) {
                            centres.add(centre);
                            liveDataCentre.addCentre(centre);
                        }
                        adapterCentre.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        if (locationPermissionsGranted) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

        }
        LatLng punto = null;
        if (idNucleo != 0) {
            for (int i = 0, j = 0; i < latitudes.size() && j < longitudes.size(); i++, j++) {
                latitud = latitudes.get(i);
                longitud = longitudes.get(j);
                punto = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
                map.addMarker(new MarkerOptions().position(punto));
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, ZOOM));
            latitudes.clear();
            longitudes.clear();
        }
    }

    private void checkPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    class getPuntosVentaTask extends AsyncTask<Void, Void, Void>{
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("puntos_venta");
                outputStream.flush();
                outputStream.reset();

                int size = inputStream.readInt();
                nucleos = new Nucleo[size];
                nombreNucleos = new String[size];
                for (int i = 0; i < size; i++) {
                    Nucleo nucleo = (Nucleo) inputStream.readObject();
                    nucleos[i] = nucleo;
                    nombreNucleos[i] = nucleo.getNombreNucleo();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setSpinner();
        }
    }

    class getMapaTask extends AsyncTask<Void, Void, Void>{
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("puntos_venta_mapa");
                outputStream.flush();
                outputStream.reset();

                outputStream.writeInt(idNucleo);
                outputStream.flush();
                outputStream.reset();

                int puntosSize = inputStream.readInt();
                String direcciones;
                for (int x = 0; x < puntosSize; x++) {
                    direcciones = inputStream.readUTF();
                    String[] newDireccion = direcciones.split("/");
                    latitudes.add(newDireccion[0]);
                    longitudes.add(newDireccion[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
