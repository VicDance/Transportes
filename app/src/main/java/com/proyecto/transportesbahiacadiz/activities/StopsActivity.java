package com.proyecto.transportesbahiacadiz.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.proyecto.transportesbahiacadiz.interfaces.FareSystemAPI;
import com.proyecto.transportesbahiacadiz.R;
import com.proyecto.transportesbahiacadiz.dialogs.NumberPickerDialog;
import com.proyecto.transportesbahiacadiz.model.Horario;
import com.proyecto.transportesbahiacadiz.model.HorarioList;
import com.proyecto.transportesbahiacadiz.model.Segment;
import com.proyecto.transportesbahiacadiz.model.SegmentList;
import com.proyecto.transportesbahiacadiz.model.Stop;
import com.proyecto.transportesbahiacadiz.util.ConnectionClass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import serializable.Parada;

import static android.view.Gravity.CENTER_VERTICAL;
import static com.proyecto.transportesbahiacadiz.activities.MainActivity.login;

public class StopsActivity extends AppCompatActivity {
    private int nucleoOrigen;
    private int nucleoDestino;
    private int ciudadOrigen;
    private int ciudadDestino;
    private double bs;
    private String destino;
    private String origen;
    private Horario[] listaHorarios;
    private Segment[] listaSegments;
    private String[] tableHeader;
    private List<Parada> stopList = new ArrayList<Parada>();
    private TableLayout tableLayout;
    private int length;
    private String nombreLinea = "";
    private int idLinea = 0;
    private String horaSalida;
    private String horaLlegada;
    private List<TextView> textViews;
    private int lineasSize;
    private ConnectionClass connectionClass;

    private Button btnPay;
    private SwipeRefreshLayout swipeRefreshLayout;

    public StopsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);
        connectionClass = new ConnectionClass(this);
        swipeRefreshLayout = findViewById(R.id.stops_refresh);
        textViews = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            nucleoOrigen = 0;
            nucleoDestino = 0;
            ciudadOrigen = 0;
            ciudadDestino = 0;
        } else {
            ciudadOrigen = extras.getInt("ciudadOrigen");
            ciudadDestino = extras.getInt("ciudadDestino");
            nucleoOrigen = extras.getInt("nucleoOrigen");
            nucleoDestino = extras.getInt("nucleoDestino");
            origen = extras.getString("nombreNucleoOrigen");
            destino = extras.getString("nombreNucleoDestino");
            bs = extras.getDouble("precio");
        }
        tableLayout = findViewById(R.id.tlGridTable);
        btnPay = findViewById(R.id.pay);
        if (!login) {
            btnPay.setVisibility(View.INVISIBLE);
        }
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idLinea == 0) {
                    Toast.makeText(StopsActivity.this, getString(R.string.select_line), Toast.LENGTH_SHORT).show();
                } else {
                    showDialog(bs, horaSalida, ciudadDestino, idLinea, horaLlegada);
                }
            }
        });

        new getParadasViajeTask().execute();

        listarBloques(nucleoDestino, nucleoOrigen);
        listarHorarios(nucleoDestino, nucleoOrigen);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    tableLayout.removeAllViews();
                    listarBloques(nucleoDestino, nucleoOrigen);
                    listarHorarios(nucleoDestino, nucleoOrigen);
                    Thread.sleep(1000);
                    swipeRefreshLayout.setRefreshing(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showDialog(double bs, String horaSalida, int idCiudadDestino, int idLinea, String horaLlegada) {
        final NumberPickerDialog dialog = new NumberPickerDialog(bs, horaSalida, idCiudadDestino, idLinea, horaLlegada);
        dialog.show(getSupportFragmentManager(), "NumberPicker");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void creaCabecera() {
        TableRow cabecera = new TableRow(this);
        length = tableHeader.length;
        for (int i = 0; i < tableHeader.length; i++) {
            TableRow.LayoutParams lp = new TableRow.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 20;
            cabecera.setLayoutParams(lp);
            final TextView textView = new TextView(this);
            textView.setText("    " + tableHeader[i] + "    ");
            textView.setTextSize(20f);
            textView.setTextAppearance(R.style.Widget_MaterialComponents_TabLayout);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setBackgroundColor(Color.rgb(95, 173, 250));
            //for(int x = 0; x < tableHeader.length; x++){
            if (tableHeader[tableHeader.length - 1].equalsIgnoreCase("observaciones")) {
                //for(int x = 1; x < tableHeader.length -1; x++){
                if (i > 0 && i < tableHeader.length - 2) {
                    final int finalI = i;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //System.out.println("click");
                            new getDireccionParadaTask(finalI).execute();
                        }
                    });
                }
            }else if(tableHeader[tableHeader.length - 1].equalsIgnoreCase("frecuencia")){
                if (i > 0 && i < tableHeader.length - 1) {
                    final int finalI = i;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //System.out.println("click");
                            new getDireccionParadaTask(finalI).execute();
                        }
                    });
                }
            }
            cabecera.addView(textView);
        }
        tableLayout.addView(cabecera);

        TableRow separador_cabecera = new TableRow(this);
        separador_cabecera.setLayoutParams(new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        FrameLayout linea_cabecera = new FrameLayout(this);
        TableRow.LayoutParams linea_cabecera_params =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2);
        linea_cabecera_params.span = 10;
        linea_cabecera.setBackgroundColor(Color.rgb(37, 121, 204));
        separador_cabecera.addView(linea_cabecera, linea_cabecera_params);
        tableLayout.addView(separador_cabecera);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void creaTabla() {
        TableRow tableRow = null;
        TableRow separador_cabecera = null;
        String[] lineas = nombreLinea.split("/");
        int cont = 0;
        for (int i = 0; i < listaHorarios.length; i++) {
            tableRow = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(100, 100);
            lp.bottomMargin = 20;
            tableRow.setLayoutParams(lp);
            for (int x = 0; x < length; x++) {
                for (int z = 0; z < lineas.length; z++) {
                    if (listaHorarios[i].getNameLinea().equalsIgnoreCase(lineas[z])) {
                        final TextView textView = new TextView(this);
                        textView.setHeight(100);
                        System.out.println(lineas[z]);
                        if (x == 0) {
                            textView.setText(listaHorarios[i].getNameLinea());
                        } else if (tableHeader[tableHeader.length - 1].equalsIgnoreCase("observaciones")) {
                            textView.setText(listaHorarios[i].getObservaciones());
                            if (x > 0 && x < (tableHeader.length - 2)) {
                                textView.setText(listaHorarios[i].getHoras().get(cont));
                                cont++;
                                if (cont == tableHeader.length - 3) {
                                    cont = 0;
                                }
                            }
                            if (x == tableHeader.length - 2) {
                                textView.setText(listaHorarios[i].getDias());
                            }
                        } else if (tableHeader[tableHeader.length - 1].equalsIgnoreCase("frecuencia")) {
                            textView.setText(listaHorarios[i].getDias());
                            if (x > 0 && x < (tableHeader.length - 1)) {
                                textView.setText(listaHorarios[i].getHoras().get(cont));
                                cont++;
                                if (cont == tableHeader.length - 2) {
                                    cont = 0;
                                }
                            }
                        }
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        textView.setGravity(CENTER_VERTICAL);
                        textView.setBackgroundColor(Color.WHITE);
                        if (textView.getText().toString().trim().contains("M")) {
                            textViews.add(textView);
                        }
                        tableRow.addView(textView);

                        separador_cabecera = new TableRow(this);
                        separador_cabecera.setLayoutParams(new TableLayout.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        FrameLayout linea_cabecera = new FrameLayout(this);
                        TableRow.LayoutParams linea_cabecera_params =
                                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2);
                        linea_cabecera_params.span = 15;
                        linea_cabecera.setBackgroundColor(Color.rgb(37, 121, 204));
                        separador_cabecera.addView(linea_cabecera, linea_cabecera_params);
                        tableLayout.addView(separador_cabecera);
                    }
                }
            }
            tableLayout.addView(tableRow);
        }
        pintaLineas();
    }

    private void pintaLineas() {
        for (int i = 0; i < textViews.size(); i++) {
            final int finalI = i;
            final String[] linea = new String[1];
            textViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = textViews.get(finalI);
                    if (textView.getBackground() instanceof ColorDrawable) {
                        ColorDrawable cd = (ColorDrawable) textView.getBackground();
                        int colorCode = cd.getColor();
                        if (colorCode == Color.WHITE) {
                            for (int j = 0; j < textViews.size(); j++) {
                                textViews.get(j).setBackgroundColor(Color.WHITE);
                            }
                            linea[0] = textView.getText().toString();
                            textView.setBackgroundColor(Color.rgb(37, 121, 204));
                            idLinea = 1;
                        } else if (colorCode == Color.rgb(37, 121, 204)) {
                            textView.setBackgroundColor(Color.WHITE);
                            idLinea = 0;
                        }
                    }
                    if(idLinea != 0) {
                        new getIdLineaTask(linea).execute();
                    }

                    horaSalida = listaHorarios[finalI].getHoras().get(0);
                    if (horaSalida.contains("-")) {
                        for (int j = 0; j < listaHorarios[finalI].getHoras().size(); j++) {
                            horaSalida = listaHorarios[finalI].getHoras().get(j);
                            if (horaSalida.contains("-")) {
                                horaSalida = listaHorarios[finalI].getHoras().get(j + 1);
                            } else {
                                break;
                            }
                        }
                    }
                    horaLlegada = listaHorarios[finalI].getHoras().get(listaHorarios[finalI].getHoras().size() - 1);
                    if (horaLlegada.contains("-")) {
                        for (int j = listaHorarios[finalI].getHoras().size() - 2; j >= 0; j--) {
                            horaLlegada = listaHorarios[finalI].getHoras().get(j);
                            if (horaLlegada.contains("-")) {
                                horaLlegada = listaHorarios[finalI].getHoras().get(j - 1);
                            } else {
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    public void listarHorarios(int idNucleoDestino, int idNucleoOrigen) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.ctan.es/v1/Consorcios/2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FareSystemAPI fareSystemAPI = retrofit.create(FareSystemAPI.class);
        Call<HorarioList> horarioCall = fareSystemAPI.getHorarios(idNucleoDestino, idNucleoOrigen);
        horarioCall.enqueue(new Callback<HorarioList>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(Call<HorarioList> call, Response<HorarioList> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Code: " + response.code());
                    return;
                }
                HorarioList horarioList = response.body();
                listaHorarios = new Horario[horarioList.getHorarioList().size()];
                for (int i = 0; i < horarioList.getHorarioList().size(); i++) {
                    listaHorarios[i] = horarioList.getHorarioList().get(i);
                }
                if (lineasSize == 0) {
                    List<String> lineas = new ArrayList<>();
                    for (int i = 0; i < listaHorarios.length; i++) {
                        lineas.add(listaHorarios[i].getNameLinea());
                    }
                    if(lineas.size() == 2 && lineas.get(0) == lineas.get(1)){
                        lineas.remove(1);
                    }else if(lineas.size() == 3 && (lineas.get(0) == lineas.get(1) || lineas.get(0) == lineas.get(2) || lineas.get(1) == lineas.get(2))){
                        lineas.remove(2);
                        lineas.remove(1);
                    }else {
                        System.out.println(lineas.size());
                        for (int i = 0; i < lineas.size() ; i++) {
                            for (int j = lineas.size() - 1; j >= 0; j--) {
                                System.out.println("i: " + i + " j: " + j);
                                if(lineas.size() == i || lineas.size() == j){
                                    break;
                                }
                                if (lineas.get(i).equalsIgnoreCase(lineas.get(j))) {
                                    lineas.remove(lineas.get(j));
                                }
                            }
                        }
                    }
                    nombreLinea = "";
                    for (int i = 0; i < lineas.size() ; i++) {
                        nombreLinea += lineas.get(i) + "/";
                        System.out.println(nombreLinea);
                    }
                }
                creaTabla();
            }

            @Override
            public void onFailure(Call<HorarioList> call, Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }
        });
    }

    private void listarBloques(int idNucleoDestino, int idNucleoOrigen) {
        System.out.println("Destino " + idNucleoDestino);
        System.out.println("Origen " + idNucleoOrigen);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.ctan.es/v1/Consorcios/2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FareSystemAPI fareSystemAPI = retrofit.create(FareSystemAPI.class);
        Call<SegmentList> segmentListCall = fareSystemAPI.getBloques(idNucleoDestino, idNucleoOrigen);
        segmentListCall.enqueue(new Callback<SegmentList>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(Call<SegmentList> call, Response<SegmentList> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Code: " + response.code());
                    System.out.println(response.message());
                    return;
                }
                TextView textViewNoTrip = findViewById(R.id.text_view_no_trip);
                textViewNoTrip.setVisibility(View.INVISIBLE);
                SegmentList segmentList = response.body();
                listaSegments = new Segment[segmentList.getSegmentList().size()];
                tableHeader = new String[segmentList.getSegmentList().size()];
                for (int i = 0; i < segmentList.getSegmentList().size(); i++) {
                    listaSegments[i] = segmentList.getSegmentList().get(i);
                }
                for (int i = 0; i < listaSegments.length; i++) {
                    tableHeader[i] = listaSegments[i].getNombre();
                }
                creaCabecera();
            }

            @Override
            public void onFailure(Call<SegmentList> call, Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }
        });
    }

    class getParadasViajeTask extends AsyncTask<Void, Void, Void> {
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("paradas_viaje");
                outputStream.flush();
                outputStream.reset();

                outputStream.writeUTF(ciudadOrigen + "/" + nucleoOrigen + "/" + ciudadDestino + "/" + nucleoDestino);
                outputStream.flush();
                outputStream.reset();

                lineasSize = inputStream.readInt();
                for (int i = 0; i < lineasSize; i++) {
                    String linea = inputStream.readUTF();
                    nombreLinea += linea + "/";
                    int paradasSize = inputStream.readInt();
                    for (int j = 0; j < paradasSize; j++) {
                        Parada parada = (Parada) inputStream.readObject();
                        stopList.add(parada);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class getIdLineaTask extends AsyncTask<Void, Void, Void> {
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        private String[] linea;

        public getIdLineaTask(String[] linea) {
            this.linea = linea;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("id_linea");
                outputStream.flush();
                outputStream.reset();

                outputStream.writeUTF(linea[0]);
                outputStream.flush();
                outputStream.reset();

                idLinea = inputStream.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class getDireccionParadaTask extends AsyncTask<Void, Void, Void> {
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        private int posicion;

        public getDireccionParadaTask(int posicion){
            this.posicion = posicion;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                outputStream.writeUTF("direccion_parada");
                outputStream.flush();
                outputStream.reset();

                outputStream.writeUTF(tableHeader[posicion].trim());
                outputStream.flush();
                outputStream.reset();

                String direccion = inputStream.readUTF();

                Intent intent = new Intent(StopsActivity.this, MapStopsActivity.class);
                intent.putExtra("direccion", direccion);
                startActivity(intent);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
