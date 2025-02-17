package com.proyecto.transportesbahiacadiz.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.proyecto.transportesbahiacadiz.interfaces.FareSystemAPI;
import com.proyecto.transportesbahiacadiz.R;
import com.proyecto.transportesbahiacadiz.model.Centre;
import com.proyecto.transportesbahiacadiz.model.Gap;
import com.proyecto.transportesbahiacadiz.model.GapList;
import com.proyecto.transportesbahiacadiz.util.ConnectionClass;
import com.proyecto.transportesbahiacadiz.viewmodel.LiveDataCentre;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import serializable.Nucleo;

import static com.proyecto.transportesbahiacadiz.util.Settings.saltos;

public class GapFragment extends Fragment {
    private View view;
    private Gap[] gaps;
    private Spinner spinnerOrigin;
    private Spinner spinnerDestiny;
    private Nucleo[] centres;
    private String[] centresNames;
    private ArrayAdapter<String> adapterDestinyCentre;
    private ArrayAdapter<String> adapterOriginCentre;
    private LiveDataCentre liveDataCentre;
    private String zonaOrigen;
    private String zonaDestino;
    private ConnectionClass connectionClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gap, container, false);
        connectionClass = new ConnectionClass(getContext());
        spinnerDestiny = view.findViewById(R.id.spinner_gap_destiny_centre);
        spinnerOrigin = view.findViewById(R.id.spinner_gap_origin_centre);
        cogeDatosAPI();
        return view;
    }

    private void cogeDatosAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.ctan.es/v1/Consorcios/2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FareSystemAPI fareSystemAPI = retrofit.create(FareSystemAPI.class);
        Call<GapList> gapListCall = fareSystemAPI.getGapList();
        gapListCall.enqueue(new Callback<GapList>() {
            @Override
            public void onResponse(Call<GapList> call, Response<GapList> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Code: " + response.code());
                    return;
                }
                GapList gapList = response.body();
                gaps = new Gap[gapList.getGapList().size()];
                for (int i = 0; i < gapList.getGapList().size(); i++) {
                    gaps[i] = gapList.getGapList().get(i);
                }
                cogeDatosBbdd();
            }

            @Override
            public void onFailure(Call<GapList> call, Throwable t) {

            }
        });
    }

    private void cogeDatosBbdd(){
        new getNucleosTask().execute();
    }

    private void setSpinnerOrigin(){
        adapterOriginCentre = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, centresNames);
        adapterOriginCentre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(adapterOriginCentre);
        spinnerOrigin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                liveDataCentre = new ViewModelProvider((ViewModelStoreOwner) getContext()).get(LiveDataCentre.class);
                String centre = (String) parent.getSelectedItem();
                for(int i = 0; i < centres.length; i++){
                    if(centre == centres[i].getNombreNucleo()){
                        zonaOrigen = centres[i].getIdZona();
                    }
                }
                liveDataCentre.getCentreList().observe((LifecycleOwner) getContext(), new Observer<List<Centre>>() {
                    @Override
                    public void onChanged(List<Centre> centres) {
                        for (Centre centre : centres) {
                            centres.add(centre);
                            liveDataCentre.addCentre(centre);
                        }
                        adapterOriginCentre.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpinnerDestiny(){
        adapterDestinyCentre = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, centresNames);
        adapterDestinyCentre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestiny.setAdapter(adapterDestinyCentre);
        spinnerDestiny.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                liveDataCentre = new ViewModelProvider((ViewModelStoreOwner) getContext()).get(LiveDataCentre.class);
                String centre = (String) parent.getSelectedItem();
                for(int i = 0; i < centres.length; i++){
                    if(centre == centres[i].getNombreNucleo()){
                        zonaDestino = centres[i].getIdZona();
                        obtieneSaltos();
                    }
                }
                liveDataCentre.getCentreList().observe((LifecycleOwner) getContext(), new Observer<List<Centre>>() {
                    @Override
                    public void onChanged(List<Centre> centres) {
                        for (Centre centre : centres) {
                            centres.add(centre);
                            liveDataCentre.addCentre(centre);
                        }
                        adapterOriginCentre.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void obtieneSaltos(){
        for(int i = 0; i < gaps.length; i++){
            if(zonaDestino.equalsIgnoreCase(gaps[i].getZonaOrigen()) && zonaOrigen.equalsIgnoreCase(gaps[i].getZonaDestino())){
                TextView textViewSaltos = view.findViewById(R.id.text_view_gap);
                textViewSaltos.setText(gaps[i].getSaltos() + "");
                saltos = gaps[i].getSaltos();
            }
        }
    }

    class getNucleosTask extends AsyncTask<Void, Void, Void>{
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("nucleos");
                outputStream.flush();
                outputStream.reset();

                int size = inputStream.readInt();

                centres = new Nucleo[size];
                centresNames = new String[size];
                for(int i = 0; i < size; i++){
                    Nucleo nucleo = (Nucleo) inputStream.readObject();
                    centres[i] = nucleo;
                    centresNames[i] = nucleo.getNombreNucleo();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setSpinnerOrigin();
            setSpinnerDestiny();
        }
    }
}
