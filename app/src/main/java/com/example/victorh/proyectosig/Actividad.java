package com.example.victorh.proyectosig;

import java.io.Serializable;

public class Actividad implements Serializable {
    private String tipo;//iniciada|finalizada

    private String linea;

    private String hora;

    private String placa;

    private String rtactride;

    private String recorrido;

    private     String rtactstat;

    private String idActividad;

    private String usuario;

    private String fecha;

    private String longitud;

    private String latitud;

    public Actividad(String idActividad,String tipo, String fecha, String hora, String linea,
                       String placa,String usuario,String longitud, String latitud,
                       String recorrido,String rtactstat, String rtactride)
    {
        this.tipo = tipo;
        this.linea = linea;
        this.hora = hora;
        this.placa = placa;
        this.rtactride = rtactride;
        this.recorrido = recorrido;
        this.rtactstat = rtactstat;
        this.idActividad = idActividad;
        this.usuario = usuario;
        this.fecha = fecha;
        this.longitud = longitud;
        this.latitud = latitud;
    }

    @Override
    public String toString() {
        return "("+idActividad+", "+tipo+", "+fecha+", "+hora+", "+linea+", "+placa+", "+usuario+","+longitud+","+latitud+","+recorrido+", "+rtactstat+", "+rtactride+") ";
    }

    public String getTipo() {
        return tipo;
    }

    public String getLinea() {
        return linea;
    }

    public String getHora() {
        return hora;
    }

    public String getPlaca() {
        return placa;
    }

    public String getRtactride() {
        return rtactride;
    }

    public String getRecorrido() {
        return recorrido;
    }

    public String getRtactstat() {
        return rtactstat;
    }

    public String getIdActividad() {
        return idActividad;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getFecha() {
        return fecha;
    }

    public String getLongitud() {
        return longitud;
    }

    public String getLatitud() {
        return latitud;
    }
}
