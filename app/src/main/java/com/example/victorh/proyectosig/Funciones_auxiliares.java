package com.example.victorh.proyectosig;


import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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

    public  static String getFecha() {
        Date anotherCurDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDateString = formatter.format(anotherCurDate);
        return formattedDateString;
    }

    public  static String getHora() {
        Date anotherCurDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String formattedDateString = formatter.format(anotherCurDate);
        return formattedDateString;
    }


}
