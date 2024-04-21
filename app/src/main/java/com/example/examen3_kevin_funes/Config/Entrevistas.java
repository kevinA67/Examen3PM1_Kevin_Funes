package com.example.examen3_kevin_funes.Config;

public class Entrevistas {

    private String id;
    private String periodista;
    private String descripcion;
    private String fecha;
    private String imagen;
    private String audio;

    public Entrevistas() {
    }

    public Entrevistas(String id, String periodista, String descripcion, String fecha, String imagen, String audio) {
        this.id = id;
        this.periodista = periodista;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.imagen = imagen;
        this.audio = audio;
    }

    public Entrevistas(String periodista, String descripcion, String fecha, String imagen, String audio) {
        this.periodista = periodista;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.imagen = imagen;
        this.audio = audio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
