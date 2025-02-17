package com.proyecto.transportesbahiacadiz.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.proyecto.transportesbahiacadiz.R;
import com.proyecto.transportesbahiacadiz.model.CodigoQR;
import com.proyecto.transportesbahiacadiz.util.ConnectionClass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static com.proyecto.transportesbahiacadiz.activities.RegisterActivity.usuario;

public class AddCardActivity extends AppCompatActivity {
    private TextView textViewDailyCard;
    private TextView textViewRetiredCard;
    private TextView textViewStudentCard;
    private ConnectionClass connectionClass;
    private String estado;

    public static CodigoQR codigoQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        connectionClass = new ConnectionClass(this);

        textViewDailyCard = findViewById(R.id.text_view_everyday_card);
        textViewRetiredCard = findViewById(R.id.text_view_retired_card);
        textViewStudentCard = findViewById(R.id.text_view_student_card);
        textViewDailyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertCard(getString(R.string.create_card), getString(R.string.create_card_daily));
            }
        });
        textViewRetiredCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertCard(getString(R.string.create_card), getString(R.string.create_card_retired));
            }
        });
        textViewStudentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertCard(getString(R.string.create_card), getString(R.string.create_card_student));
            }
        });
    }

    private void showAlertCard(String title, String body){
        new AlertDialog.Builder(AddCardActivity.this)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new addCardTask("tarjeta_estu").execute();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddCardActivity.this, "No se creó la tarjeta", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    class addCardTask extends AsyncTask<Void, Void, Void> {
        Socket cliente;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        private String tarjeta;

        public addCardTask(String tarjeta){
            this.tarjeta = tarjeta;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                cliente = new Socket(connectionClass.getConnection().get(0).getAddress(), connectionClass.getConnection().get(0).getPort());
                outputStream = new ObjectOutputStream(cliente.getOutputStream());
                inputStream = new ObjectInputStream(cliente.getInputStream());

                try {
                    outputStream.writeUTF(tarjeta);
                    outputStream.flush();
                    outputStream.reset();

                    final Random rnd = new Random();
                    long numTarjeta = rnd.nextLong();
                    outputStream.writeUTF(numTarjeta + "/" + usuario.getId());
                    outputStream.flush();
                    outputStream.reset();

                    estado = inputStream.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (estado.equalsIgnoreCase("correcto")) {
                codigoQR = new CodigoQR();
                new AlertDialog.Builder(AddCardActivity.this)
                        .setTitle(R.string.correct)
                        .setMessage(getString(R.string.create_card_successful))
                        .show();
            } else {
                new AlertDialog.Builder(AddCardActivity.this)
                        .setTitle(R.string.incorrect)
                        .setMessage(getString(R.string.create_card_unsuccessful))
                        .show();
            }
        }
    }
}
