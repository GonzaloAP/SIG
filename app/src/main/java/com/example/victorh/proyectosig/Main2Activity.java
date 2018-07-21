package com.example.victorh.proyectosig;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity implements Serializable {

    private EditText edittxt_suben, edittxt_bajan;
    private String fecha;
    private String hora;
    public static float velocidad;
    public static double latitud, longitud;
    private final String TAG = "main2";
    private Actividad actividadActual;
    private final int TIME_PERIOD = 10000;
    private Timer timer;
    private TimerTask task;
    private Button btn_pare, btn_grabar, btn_finCaptura;

    private static  double latitudParada;
    private static  double longitudParada;
    private String horaParada;

    private boolean isPressPare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        inicializarVariables();
        isPressPare=false;
        changeStateEnableAllButton(false);
        inciarEscuchadorLocalizacion();
        TaskGetActividad taskGetActividad = new TaskGetActividad();
        taskGetActividad.execute();
    }

    private void finalizaHilo() {
        timer.cancel();
        timer.purge();
        task.cancel();
    }

    private void changeStateEnableAllButton(boolean b) {
        this.btn_pare.setEnabled(b);
        this.btn_grabar.setEnabled(b);
        this.btn_finCaptura.setEnabled(b);
    }

    void inicializarVariables() {
        btn_finCaptura = (Button) findViewById(R.id.btn_fin_captura);
        btn_grabar = (Button) findViewById(R.id.btn_grabar);
        btn_pare = (Button) findViewById(R.id.btn_pare);
        this.edittxt_bajan = (EditText) findViewById(R.id.edittxt_bajan);
        this.edittxt_suben = (EditText) findViewById(R.id.edittxt_suben);
    }

    public void pare(View view) {
        isPressPare=true;
        actualizarLocalizacionHoraParada();
        changeStateEnableButtonGrabar(true);
    }

    private void actualizarLocalizacionHoraParada() {
        longitudParada=longitud;
        latitudParada=latitud;
        horaParada= Funciones_auxiliares.getHora();
    }

    public void grabar(View view) {
        if (!campos_vacios()) {
            if (!isPressPare){actualizarLocalizacionHoraParada();}

            String cantidaSubida = edittxt_suben.getText().toString();
            String cantidadBajada = edittxt_bajan.getText().toString();
            changeStateEnableButtonGrabar(false);
            TaskGrabar grabar = new TaskGrabar();
            grabar.execute(cantidaSubida, cantidadBajada);

        } else {
            Toast.makeText(this, "Hay algunos campos vacios", Toast.LENGTH_SHORT).show();
        }
    }

    public void detener(View view) {
        finalizaHilo();
    }

    private class TaskGrabar extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            enviarParadaMicro(params[0], params[1]);

            return null;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            isPressPare=false;
            changeStateEnableButtonGrabar(true);
            mostrarToastMensaje("Error al guardar.");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isPressPare=false;
            changeStateEnableButtonGrabar(true);
            mostrarToastMensaje("Parada Guardada.");
        }
    }

    private void changeStateEnableButtonGrabar(boolean b) {
        this.btn_grabar.setEnabled(b);
    }

    public boolean campos_vacios() {
        return Funciones_auxiliares.vacia(this.edittxt_bajan) || Funciones_auxiliares.vacia(this.edittxt_suben);
    }

    public void fin_captura(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Estas seguro de finalizar la captura?");
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                actualizar_Hora();
                finalizaHilo();
                changeStateEnableAllButton(false);
                TaskSendActividadFinalizada finalizarActividad = new TaskSendActividadFinalizada();
                finalizarActividad.execute();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        //super.finish();
    }

    private void gotoMain() {
        finish();
    }

    private void lanzarHiloCadaXtiempo() {
        final Handler handler = new Handler();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        actualizar_Hora();
                        TaskAddPuntoMicroXTIempo taskSend10Seg = new TaskAddPuntoMicroXTIempo();
                        taskSend10Seg.execute();
                    }
                });
            }
        };

        timer.schedule(task, 0, TIME_PERIOD);

    }

    private class TaskAddPuntoMicroXTIempo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviar_Add_PuntosPasaMicro10Seg();
            return null;
        }

    }

    private class TaskSendActividadFinalizada extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviarActividad();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mostrarToastMensaje("Recorrido Guardado. Ok");
            gotoMain();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            mostrarToastMensaje("Error al guardar.");
            gotoMain();
        }
    }
    private void inciarEscuchadorLocalizacion() {
        Toast mensajeGPS = Toast.makeText(getApplicationContext(), "Por Favor Active su GPS", Toast.LENGTH_SHORT);
        mensajeGPS.setGravity(Gravity.CENTER, 0, 0);
        mensajeGPS.show();

        iniciarVariable();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            ubicacion();
        }
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
    public class Localizacion implements LocationListener {
        Main2Activity mainActivity;

        public Main2Activity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(Main2Activity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            if (loc == null) {
                velocidad = 0;
            } else {
                velocidad = getVelocidad(loc);
                latitud = loc.getLatitude();
                longitud = loc.getLongitude();
                String Text = "Mi ubicacion actual es: " + "\n Lat = " + loc.getLatitude() + "\n Long = " + loc.getLongitude();
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
            mensajeGPS.setGravity(Gravity.CENTER, 0, 0);
            mensajeGPS.show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast mensajeGPS = Toast.makeText(getApplicationContext(), "GPS Activado", Toast.LENGTH_SHORT);
            mensajeGPS.setGravity(Gravity.CENTER, 0, 0);
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
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);

                    System.out.println("Mi direccion es: \n" + DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public float getVelocidad(Location location) {
        velocidad = 0;
        if (location == null) {
            System.out.println(velocidad + " km/h ");
        } else {
            velocidad = location.getSpeed();
            velocidad = (float) (velocidad * (3.6));
            System.out.println(velocidad + " km/h ");
        }
        return velocidad;
    }
    private void iniciarVariable() {
        velocidad = 0;
        latitud = 0;
        longitud = 0;
    }
    private void printlog(String s) {
        Log.i(TAG,s);
    }
    private void enviar_RTACT_obtenerActividad() {
        String SOAP_ACTION = "http://activebs.net/RTACT_obtenerActividad";
        String METHOD_NAME = "RTACT_obtenerActividad";
        String NAME_SPACE = "http://activebs.net/";
        String URL = "http://wslectura.coosiv.com/wsRT.asmx";

        SoapObject request = new SoapObject(NAME_SPACE, METHOD_NAME);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE transporte = new HttpTransportSE(URL);
        try {
            transporte.call(SOAP_ACTION, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();

            SoapObject body = (SoapObject) resSoap.getProperty(1);
            SoapObject contenido = (SoapObject) body.getProperty(0);


            int countActividades = contenido.getPropertyCount();
            if (countActividades > 0) {
                SoapObject tupla = (SoapObject) contenido.getProperty(countActividades - 1);
                actividadActual = new Actividad(tupla.getProperty(0).toString(), tupla.getProperty(1).toString(), tupla.getProperty(2).toString(), tupla.getProperty(3).toString(),

                        tupla.getProperty(4).toString(), tupla.getProperty(5).toString(), tupla.getProperty(6).toString(), tupla.getProperty(7).toString(),

                        tupla.getProperty(8).toString(), tupla.getProperty(9).toString(), tupla.getProperty(10).toString(), tupla.getProperty(11).toString());
            } else {
                actividadActual = null;
            }

        } catch (Exception e) {
            Log.i(TAG, "ERROR al obtenerActividad: " + e.getMessage());
            actividadActual = null;
        }
    }
    private void enviar_Add_PuntosPasaMicro10Seg() {
        String SOAP_ACTION = "http://activebs.net/RTPRT_PuntosPasaMicro10Seg";
        String METHOD_NAME = "RTPRT_PuntosPasaMicro10Seg";
        String NAME_SPACE = "http://activebs.net/";
        String URL = "http://wslectura.coosiv.com/wsRT.asmx";
        try {
            SoapObject soapObject = new SoapObject(NAME_SPACE, METHOD_NAME);
            soapObject.addProperty("liNprt", 1);//
            soapObject.addProperty("liNact", actividadActual.getIdActividad());
            soapObject.addProperty("lsHora", hora);
            soapObject.addProperty("lfLogi", this.longitud + "");
            soapObject.addProperty("lfLati", this.latitud + "");
            soapObject.addProperty("lfVelo", this.velocidad + "");
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(SOAP_ACTION, envelope);
            SoapPrimitive resultRequest = (SoapPrimitive) envelope.getResponse();
            printlog("\tenvio H: "+ soapObject.getPropertyAsString("lsHora")+" "+resultRequest.toString());


        } catch (Exception e) {
            printlog("\tenvio H: "+e.getMessage().toString());
        }
    }
    private void enviarParadaMicro(String subida, String bajada) {
        String SOAP_ACTION = "http://activebs.net/RTPPR_AdicionarPuntosPasaMicro";
        String METHOD_NAME = "RTPPR_AdicionarPuntosPasaMicro";
        String NAME_SPACE = "http://activebs.net/";
        String URL = "http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject = new SoapObject(NAME_SPACE, METHOD_NAME);
            soapObject.addProperty("liNact", actividadActual.getIdActividad());
            soapObject.addProperty("lsHora", horaParada);
            soapObject.addProperty("lfLogi", longitudParada+ "");
            soapObject.addProperty("lfLati", latitudParada + "");
            soapObject.addProperty("liSube", subida);
            soapObject.addProperty("liBaja", bajada);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(SOAP_ACTION, envelope);
            SoapPrimitive resultRequest = (SoapPrimitive) envelope.getResponse();
            printlog("enviarParadaMicro " + resultRequest.toString() + " H:"+horaParada);

        } catch (Exception e) {
            Log.i(TAG, "ERROR enviarParadaMicro" + e.getMessage().toString());
        }
    }

    private void enviarActividad() {
        String SOAP_ACTION = "http://activebs.net/RTACT_AdicionarActividad";
        String METHOD_NAME = "RTACT_AdicionarActividad";
        String NAME_SPACE = "http://activebs.net/";
        String URL = "http://wslectura.coosiv.com/wsRT.asmx";
        try {
            SoapObject soapObject = new SoapObject(NAME_SPACE, METHOD_NAME);
            soapObject.addProperty("liTipo", VarConst.ACTIVIDAD_FINALIZADA);
            soapObject.addProperty("lsFech", actividadActual.getFecha());
            soapObject.addProperty("lsHora", hora);
            soapObject.addProperty("lsLine", actividadActual.getLinea());
            soapObject.addProperty("lsPlac", actividadActual.getPlaca());
            soapObject.addProperty("lsUsua", actividadActual.getUsuario());
            soapObject.addProperty("lfLogi", this.longitud + "");
            soapObject.addProperty("lfLati", this.latitud + "");
            soapObject.addProperty("liReco", actividadActual.getRecorrido());
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(SOAP_ACTION, envelope);
            SoapPrimitive resultRequest = (SoapPrimitive) envelope.getResponse();
            Log.i(TAG, "enviarActividad .." + resultRequest.toString());

        } catch (Exception e) {
            Log.i(TAG, "ERROR enviarActividad" + e.getMessage().toString());
        }
    }

    private void mostrarToastMensaje(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    private class TaskGetActividad extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviar_RTACT_obtenerActividad();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (actividadActual == null) {
                Log.i(TAG, "no existe ultima actividad. gotoMain....");
                mostrarToastMensaje("Error..");
                gotoMain();
            } else {

                printlog("----> "+actividadActual.getUsuario()+" H:"+actividadActual.getHora()+" F:"+actividadActual.getFecha());
                changeStateEnableAllButton(true);
                changeStateEnableButtonGrabar(false);
                lanzarHiloCadaXtiempo();
            }
        }



    }
    private void actualizar_Hora() {
        hora = Funciones_auxiliares.getHora();
        fecha = Funciones_auxiliares.getFecha();
    }
}
