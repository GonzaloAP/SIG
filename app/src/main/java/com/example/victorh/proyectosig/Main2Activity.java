package com.example.victorh.proyectosig;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    EditText edittxt_suben,edittxt_bajan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        inicializarVariables();
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
        //AQUI VA TU CODIGO
    }

    /**
     * funcion del boton Grabar (btn_grabar)
     *
     * @param view
     */
    public void grabar(View view) {
        //AQUI VA TU CODIGO
        if(!campos_vacios()){

        }
        else{
            Toast.makeText(this,"Hay algunos campos vacios",Toast.LENGTH_SHORT).show();
        }
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
                finish();
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

    /**
     * Esto deshabilita la funcion de retroceder del boton del dispositivo
     */

    @Override
    public void onBackPressed(){

    }
}
