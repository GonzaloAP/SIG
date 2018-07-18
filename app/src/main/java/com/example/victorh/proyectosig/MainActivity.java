package com.example.victorh.proyectosig;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private EditText edittxtlinea, edittxtplaca, edittxtusuario;
    private String linea,placa,usuario;
    private String liTipo,lsFech,lsHora,lfLogi,lfLati,liReco;
    private SoapPrimitive resultRequest;
    private String mensaje;
    private final String TAG="main_act";
    private String salidaRetorno;
    private RadioButton rbsalida,rbretorno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarVariable();

        /*Para obtener la velocidad*/
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        /*Traer las Actualizciones del GPS*/
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        /*Inicializar en null*/
        onLocationChanged(null);

        /*Para obtener la velocidad*/

        Button btn_iniciar_captura=(Button) findViewById(R.id.btn_iniciar_captura);
        btn_iniciar_captura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!campos_vacios()) {
                    obtenerDatosCampos();
                    obtenerOtrosDatos();
                    SegundoPlano segundoPlano = new SegundoPlano();
                    segundoPlano.execute();

                }
                else
                {

                    Toast.makeText(getApplicationContext(),"Algunos campos estan vacios",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void obtenerDatosCampos() {
        linea=edittxtlinea.getText().toString();
        placa=edittxtplaca.getText().toString();
        usuario=edittxtusuario.getText().toString();

        if (rbsalida.isChecked()) salidaRetorno= VarConst.RECORRIDO_SALIDA;
        if (rbretorno.isChecked()) salidaRetorno= VarConst.RECORRIDO_RETORNO;

    }

    private void obtenerOtrosDatos() {
        liTipo=VarConst.ACTIVIDAD_INICIADA;
        lsFech="2018-07-18";
        lsHora="10:47:00";
        lfLogi="-17.810194";
        lfLati="-63.182730";
        liReco=salidaRetorno;
    }

    /**
     * Inicia todas las variables que se van a utilizar del layout
     */
    public void iniciarVariable(){
        edittxtusuario=(EditText) findViewById(R.id.edittxt_usuario);
        edittxtplaca=(EditText) findViewById(R.id.edittxt_placa);
        edittxtlinea=(EditText) findViewById(R.id.edittxt_linea);

        rbsalida=(RadioButton)findViewById(R.id.rb_salida);
        rbretorno=(RadioButton)findViewById(R.id.rb_retorno);


    }

    /**
     * Funcion de btn_iniciarCaptura
     * @param
     */
    public void iniciarCaptura( ){
        Intent intent=new Intent(this,Main2Activity.class);
        startActivity(intent);
        finish();
    }
    private class SegundoPlano extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            enviarActividad();
            Log.i(TAG,"Datos enviados: "+linea+"  "+placa+" "+usuario);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG,"ResultRequest: "+resultRequest.toString());
            iniciarCaptura();
            super.onPostExecute(aVoid);
        }
    }

    private void enviarActividad() {
        String SOAP_ACTION="http://activebs.net/RTACT_AdicionarActividad";
        String METHOD_NAME="RTACT_AdicionarActividad";
        String NAME_SPACE="http://activebs.net/";
        String URL="http://wslectura.coosiv.com/wsRT.asmx";

        try {
            SoapObject soapObject= new SoapObject(NAME_SPACE,METHOD_NAME);
            soapObject.addProperty("liTipo",liTipo);
            soapObject.addProperty("lsFech",lsFech);
            soapObject.addProperty("lsHora",lsHora);
            soapObject.addProperty("lsLine",linea);
            soapObject.addProperty("lsPlac",placa);
            soapObject.addProperty("lsUsua",usuario);
            soapObject.addProperty("lfLogi",lfLogi);
            soapObject.addProperty("lfLati",lfLati);
            soapObject.addProperty("liReco",liReco);


            SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE transport=new HttpTransportSE(URL);
            transport.call(SOAP_ACTION,envelope);
            resultRequest=(SoapPrimitive) envelope.getResponse();
            mensaje="ok";
            Log.i(TAG,"mensaje ok..");

        }
        catch (Exception e)
        {
            Log.i(TAG,"ERROR "+e.getMessage().toString());
        }
    }
    /**
     * Devuelve true si al menos un campo esta vacio.
     * Devuelve false si todos los campos no estan vacios.
     * @return
     */
    public boolean campos_vacios(){
        return Funciones_auxiliares.vacia(this.edittxtusuario)
                ||Funciones_auxiliares.vacia(this.edittxtlinea)
                ||Funciones_auxiliares.vacia(this.edittxtplaca) ;
    }

    @Override
    public void onBackPressed(){

    }

    @Override
    public void onLocationChanged(Location location) {
        float v = this.getVelocidad(location);
        /*Solo para fines de prueba, comenten el edittxtplaca*/
        //edittxtplaca.setText(v + " km/h ");
    }

    public float getVelocidad(Location location){
        float v = 0;
        if(location == null){
            System.out.println(v + " km/h ");
        }else{
            v = location.getSpeed();
            v= (float) (v * (3.6));
            System.out.println(v + " km/h ");
        }
        return v;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
