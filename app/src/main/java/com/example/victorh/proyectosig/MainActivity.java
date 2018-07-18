package com.example.victorh.proyectosig;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private EditText edittxtlinea, edittxtplaca, edittxtusuario;

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
    }

    /**
     * Inicia todas las variables que se van a utilizar del layout
     */
    public void iniciarVariable(){
        edittxtusuario=findViewById(R.id.edittxt_usuario);
        edittxtplaca=findViewById(R.id.edittxt_placa);
        edittxtlinea=findViewById(R.id.edittxt_linea);
    }

    /**
     * Funcion de btn_iniciarCaptura
     * @param view
     */
    public void iniciarCaptura(View view){
        /*Agregar Codigo*/
        if(!campos_vacios()){
            /*Agregar Codigo*/

            //Pasa a la vista Main2Activity
            Intent intent=new Intent(this,Main2Activity.class);
            //intent.putExtra("var_name",variable);  //para pasar parametros a la otra vista
            startActivity(intent);
        }else {
            /*Agregar Codigo*/
            Toast.makeText(this,"Algunos campos estan vacios",Toast.LENGTH_SHORT).show();
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
        edittxtplaca.setText(v + " km/h ");
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
