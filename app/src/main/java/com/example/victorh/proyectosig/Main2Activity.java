package com.example.victorh.proyectosig;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity implements Serializable{

    EditText edittxt_suben,edittxt_bajan;

    private String fecha = Funciones_auxiliares.getFecha();
    private String hora = Funciones_auxiliares.getHora();
    private float velocidad = MainActivity.velocidad;
    private double longitud = MainActivity.longitud;
    private double latitud = MainActivity.latitud;
    private final String TAG="main2";
    private Actividad actividadActual;
    private final int TIME_PERIOD=10000;
    private Timer timer;
    private TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        inicializarVariables();
        disableAllButton();
        Log.i(TAG,velocidad + "km/h");
        Log.i(TAG,"latitud: " + latitud);
        Log.i(TAG,"longitud:" + longitud);

        TaskGetActividad taskGetActividad= new TaskGetActividad();
        taskGetActividad.execute();
    }
    private void disableAllButton() {
        //deshabilitar todos los botones
    }
    /**
     * Inicializa las variables que se van a utilizar
     */
    void inicializarVariables(){
        this.edittxt_bajan=findViewById(R.id.edittxt_bajan);
        this.edittxt_suben=findViewById(R.id.edittxt_suben);
    }
    /**
     * funcion del boton pare (btn_pare)
     *
     * @param view
     */
    public void pare(View view) {

    }
    /**
     * funcion del boton Grabar (btn_grabar)
     *
     * @param view
     */
    public void grabar(View view) {
        if(!campos_vacios()){

            String cantidaSubida=edittxt_suben.getText().toString();
            String cantidadBajada=edittxt_bajan.getText().toString();
            disableButtonGrabar();
            TaskGrabar grabar= new TaskGrabar();
            grabar.execute(cantidaSubida,cantidadBajada);

        }
        else{
            Toast.makeText(this,"Hay algunos campos vacios",Toast.LENGTH_SHORT).show();
        }
    }
    private void disableButtonGrabar() {
    }
    private class TaskGrabar extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG,"->"+params[0]);
            Log.i(TAG,"->"+params[1]);
            enviarParadaMicro(params[0],params[1]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            enabledButtonGrabar();
            super.onPostExecute(aVoid);
        }
    }
    private void enabledButtonGrabar() {

    }
    /**
     * Devuelve true si existe al menos un campo vacio
     * Devuelve false si no ningun campo vacio
     * @return
     */
    public boolean campos_vacios(){
        return Funciones_auxiliares.vacia(this.edittxt_bajan)||Funciones_auxiliares.vacia(this.edittxt_suben);
    }
    /**
     * Funcion del boton fin Captura (btn_fin_captura)
     *
     * @param view
     */
    public void fin_captura(View view) {
        //AQUI VA TU CODIGO
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Estas seguro de finalizar la captura?");
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finalizaHilo();
                disableAllButton();
                TaskSendActividadFinalizada finalizarActividad= new TaskSendActividadFinalizada();
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
    private void finalizaHilo() {
        timer.cancel();
        timer.purge();
        task.cancel();

    }
    @Override
    public void onBackPressed(){
    }
    private void enabledAllButton() {
        Log.i(TAG,"actividad recibida...habilitar los botones");
    }
    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void lanzarHiloCadaXtiempo() {
        Log.i(TAG,"lanzarHiloCadaXtiempo");
        final Handler handler = new Handler();
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        TaskAddPuntoMicroXTIempo taskSend10Seg= new TaskAddPuntoMicroXTIempo();
                        taskSend10Seg.execute();
                    }
                });
            }
        };

        timer.schedule(task, 0, TIME_PERIOD);  //ejecutar en intervalo de 3 segundos.

    }
    private class TaskAddPuntoMicroXTIempo extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviar_Add_PuntosPasaMicro10Seg();
            return null;
        }
    }
    private class TaskSendActividadFinalizada extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviarActividad();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            gotoMain();
            super.onPostExecute(aVoid);
        }
    }
    private class TaskGetActividad extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            send_RTACT_obtenerActividad();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (actividadActual==null)
            {
                Log.i(TAG,"no existe ultima actividad. gotoMain....");
                gotoMain();
            }
            else
            {
                enabledAllButton();
                lanzarHiloCadaXtiempo();
            }
            super.onPostExecute(aVoid);
        }
    }
    private void send_RTACT_obtenerActividad() {
        String SOAP_ACTION="http://activebs.net/RTACT_obtenerActividad";
        String METHOD_NAME="RTACT_obtenerActividad";
        String NAME_SPACE="http://activebs.net/";
        String URL="http://wslectura.coosiv.com/wsRT.asmx";

        SoapObject request = new SoapObject(NAME_SPACE, METHOD_NAME);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE transporte = new HttpTransportSE(URL);
        try
        {
            transporte.call(SOAP_ACTION, envelope);

            SoapObject resSoap =(SoapObject)envelope.getResponse();

            SoapObject body=(SoapObject) resSoap.getProperty(1);
            SoapObject contenido=(SoapObject) body.getProperty(0);


            int countActividades=contenido.getPropertyCount();
            if (countActividades>0) {
                SoapObject tupla = (SoapObject) contenido.getProperty(countActividades - 1);
                actividadActual = new Actividad(
                        tupla.getProperty(0).toString(),
                        tupla.getProperty(1).toString(),
                        tupla.getProperty(2).toString(),
                        tupla.getProperty(3).toString(),

                        tupla.getProperty(4).toString(),
                        tupla.getProperty(5).toString(),
                        tupla.getProperty(6).toString(),
                        tupla.getProperty(7).toString(),

                        tupla.getProperty(8).toString(),
                        tupla.getProperty(9).toString(),
                        tupla.getProperty(10).toString(),
                        tupla.getProperty(11).toString()
                );
            }else
            {
                actividadActual=null;
            }

        }
        catch (Exception e)
        {
            Log.i(TAG,"ERROR al obtenerActividad: "+e.getMessage());
            actividadActual=null;
        }
    }
    private void enviar_Add_PuntosPasaMicro10Seg() {
        Log.i(TAG,"enviar_RTPRT_PuntosPasaMicro10Seg");
        String SOAP_ACTION="http://activebs.net/RTPRT_PuntosPasaMicro10Seg";
        String METHOD_NAME="RTPRT_PuntosPasaMicro10Seg";
        String NAME_SPACE="http://activebs.net/";
        String URL="http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject= new SoapObject(NAME_SPACE,METHOD_NAME);
            soapObject.addProperty("liNprt",1);//
            soapObject.addProperty("liNact",actividadActual.getIdActividad());
            soapObject.addProperty("lsHora",Funciones_auxiliares.getHora());
            soapObject.addProperty("lfLogi",this.longitud+"");
            soapObject.addProperty("lfLati",this.latitud+"");
            soapObject.addProperty("lfVelo",this.velocidad+"");
            SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport=new HttpTransportSE(URL);
            transport.call(SOAP_ACTION,envelope);
            SoapPrimitive resultRequest=(SoapPrimitive) envelope.getResponse();

            Log.i(TAG,"enviar_RTPRT_PuntosPasaMicro10Seg -->"+resultRequest.toString());

        }
        catch (Exception e)
        {
            Log.i(TAG,"ERROR RTPRT_PuntosPasaMicro10Seg"+e.getMessage().toString());
        }
    }
    private void enviarParadaMicro(String subida, String bajada) {
        String SOAP_ACTION="http://activebs.net/RTPPR_AdicionarPuntosPasaMicro";
        String METHOD_NAME="RTPPR_AdicionarPuntosPasaMicro";
        String NAME_SPACE="http://activebs.net/";
        String URL="http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject= new SoapObject(NAME_SPACE,METHOD_NAME);
            soapObject.addProperty("liNact",actividadActual.getIdActividad());
            soapObject.addProperty("lsHora",Funciones_auxiliares.getHora());
            soapObject.addProperty("lfLogi",this.longitud+"");
            soapObject.addProperty("lfLati",this.latitud+"");
            soapObject.addProperty("liSube",subida);
            soapObject.addProperty("liBaja",bajada);

            SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport=new HttpTransportSE(URL);
            transport.call(SOAP_ACTION,envelope);
            SoapPrimitive resultRequest=(SoapPrimitive) envelope.getResponse();
            Log.i(TAG,"enviarParadaMicro "+resultRequest.toString());

        }
        catch (Exception e)
        {
            Log.i(TAG,"ERROR enviarParadaMicro"+e.getMessage().toString());
        }
    }
    private void enviarActividad() {
        String SOAP_ACTION="http://activebs.net/RTACT_AdicionarActividad";
        String METHOD_NAME="RTACT_AdicionarActividad";
        String NAME_SPACE="http://activebs.net/";
        String URL="http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject= new SoapObject(NAME_SPACE,METHOD_NAME);
            soapObject.addProperty("liTipo",VarConst.ACTIVIDAD_FINALIZADA);
            soapObject.addProperty("lsFech",actividadActual.getFecha());
            soapObject.addProperty("lsHora",Funciones_auxiliares.getHora());
            soapObject.addProperty("lsLine",actividadActual.getLinea());
            soapObject.addProperty("lsPlac",actividadActual.getPlaca());
            soapObject.addProperty("lsUsua",actividadActual.getUsuario());
            soapObject.addProperty("lfLogi",this.longitud+"");
            soapObject.addProperty("lfLati",this.latitud+"");
            soapObject.addProperty("liReco",actividadActual.getRecorrido());
            SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport=new HttpTransportSE(URL);
            transport.call(SOAP_ACTION,envelope);
            SoapPrimitive resultRequest=(SoapPrimitive) envelope.getResponse();
            Log.i(TAG,"enviarActividad .."+resultRequest.toString());

        }
        catch (Exception e)
        {
            Log.i(TAG,"ERROR enviarActividad"+e.getMessage().toString());
        }
    }


}
