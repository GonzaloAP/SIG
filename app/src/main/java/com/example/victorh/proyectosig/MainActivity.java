package com.example.victorh.proyectosig;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private EditText edittxtlinea, edittxtplaca, edittxtusuario;
    private String linea, placa, usuario;
    private String liTipo, lsFech, lsHora, lfLogi, lfLati, liReco;
    private SoapPrimitive resultRequest;
    private String mensaje;
    private final String TAG = "main_act";

    private String fecha = Funciones_auxiliares.getFecha();
    private String hora = Funciones_auxiliares.getHora();
    public static float velocidad;
    public static double latitud, longitud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast mensajeGPS = Toast.makeText(getApplicationContext(), "Por Favor Active su GPS", Toast.LENGTH_SHORT);
        mensajeGPS.setGravity(Gravity.CENTER,0,0);
        mensajeGPS.show();

        iniciarVariable();

        Button btn_iniciar_captura = (Button) findViewById(R.id.btn_iniciar_captura);
        btn_iniciar_captura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!campos_vacios()) {
                    obtenerDatosCampos();
                    obtenerOtrosDatos();
                    SegundoPlano segundoPlano = new SegundoPlano();
                    segundoPlano.execute();

                } else {

                    Toast.makeText(getApplicationContext(), "Algunos campos estan vacios", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            ubicacion();
        }


    }


    private void obtenerDatosCampos() {
        linea = edittxtlinea.getText().toString();
        placa = edittxtplaca.getText().toString();
        usuario = edittxtusuario.getText().toString();
    }

    private void obtenerOtrosDatos() {
        liTipo = "1";
        lsFech = "2018-07-18";
        lsHora = "10:47:00";
        lfLogi = "-17.810194";
        lfLati = "-63.182730";
        liReco = "1";
    }

    /**
     * Inicia todas las variables que se van a utilizar del layout
     */
    public void iniciarVariable() {
        edittxtusuario = (EditText) findViewById(R.id.edittxt_usuario);
        edittxtplaca = (EditText) findViewById(R.id.edittxt_placa);
        edittxtlinea = (EditText) findViewById(R.id.edittxt_linea);
        velocidad = 0;
        latitud = 0;
        longitud=0;
    }

    /**
     * Funcion de btn_iniciarCaptura
     * @param
     */
    public void iniciarCaptura() {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
        finish();
    }


    private class SegundoPlano extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviarActividad();
            Log.i(TAG, "Datos enviados: " + linea + "  " + placa + " " + usuario);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "ResultRequest: " + resultRequest.toString());
            iniciarCaptura();
            super.onPostExecute(aVoid);
        }
    }

    private void enviarActividad() {
        String SOAP_ACTION = "http://activebs.net/RTACT_AdicionarActividad";
        String METHOD_NAME = "RTACT_AdicionarActividad";
        String NAME_SPACE = "http://activebs.net/";
        String URL = "http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject = new SoapObject(NAME_SPACE, METHOD_NAME);
            soapObject.addProperty("liTipo", liTipo);
            soapObject.addProperty("lsFech", lsFech);
            soapObject.addProperty("lsHora", lsHora);
            soapObject.addProperty("lsLine", linea);
            soapObject.addProperty("lsPlac", placa);
            soapObject.addProperty("lsUsua", usuario);
            soapObject.addProperty("lfLogi", lfLogi);
            soapObject.addProperty("lfLati", lfLati);
            soapObject.addProperty("liReco", liReco);


            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(SOAP_ACTION, envelope);
            resultRequest = (SoapPrimitive) envelope.getResponse();
            mensaje = "ok";
            Log.i(TAG, "mensaje ok..");

        } catch (Exception e) {
            Log.i(TAG, "ERROR " + e.getMessage().toString());
        }
    }

    /**
     * Devuelve true si al menos un campo esta vacio.
     * Devuelve false si todos los campos no estan vacios.
     * @return
     */
    public boolean campos_vacios() {
        return Funciones_auxiliares.vacia(this.edittxtusuario)
                || Funciones_auxiliares.vacia(this.edittxtlinea)
                || Funciones_auxiliares.vacia(this.edittxtplaca);

    }

    private void ubicacion() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);


    }

    public float getVelocidad(Location location){
        velocidad = 0;
        if(location == null){
            System.out.println(velocidad + " km/h ");
        }else{
            velocidad = location.getSpeed();
            velocidad= (float) (velocidad * (3.6));
            System.out.println(velocidad + " km/h ");
        }
        return velocidad;
    }


    @Override
    public void onBackPressed(){

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ubicacion();
                return;
            }
        }
    }
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);

                    System.out.println("Mi direccion es: \n"
                            + DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;
        public MainActivity getMainActivity() {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            if(loc == null){
                velocidad = 0;
            }else{
                velocidad = getVelocidad(loc);
                latitud = loc.getLatitude();
                longitud = loc.getLongitude();
                String Text = "Mi ubicacion actual es: " + "\n Lat = "
                        + loc.getLatitude() + "\n Long = " + loc.getLongitude();
                System.out.println(Text);
                System.out.println(velocidad + "km/h");
                System.out.println("latitud: " + latitud);
                System.out.println("longitud:" + longitud);
                this.mainActivity.setLocation(loc);
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Toast mensajeGPS = Toast.makeText(getApplicationContext(), "GPS Desactivado", Toast.LENGTH_SHORT);
            mensajeGPS.setGravity(Gravity.CENTER,0,0);
            mensajeGPS.show();
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast mensajeGPS = Toast.makeText(getApplicationContext(), "GPS Activado", Toast.LENGTH_SHORT);
            mensajeGPS.setGravity(Gravity.CENTER,0,0);
            mensajeGPS.show();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

}
