package com.example.victorh.proyectosig;

import android.widget.EditText;

public class Funciones_auxiliares {
    /**
     * Devuelve True si edtxt esta vacia
     * Devuelve False si edtxt no esta vacia
     * @param edtxt
     * @return
     */
    public static boolean vacia(EditText edtxt){
        return edtxt.getText().toString().trim().length()==0;
    }
}
