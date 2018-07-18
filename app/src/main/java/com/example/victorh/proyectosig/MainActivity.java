package com.example.victorh.proyectosig;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText edittxtlinea,edittxtplaca,edittxtusuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarVariable();
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

}
