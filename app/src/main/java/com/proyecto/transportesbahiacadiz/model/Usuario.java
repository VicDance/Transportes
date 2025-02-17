package com.proyecto.transportesbahiacadiz.model;


import java.io.Serializable;
import java.sql.Blob;

public class Usuario{
    private int id;
    private String nombre;
    private String correo;
    private int tfno;
    private String fecha_nac;
    private byte[] imagen;
    private String contraseña;

    public Usuario(){

    }

    public Usuario(String nombre, String correo, int tfno, String fecha_nac){
        this.nombre = nombre;
        this.correo = correo;
        this.tfno = tfno;
        this.fecha_nac = fecha_nac;
    }

    public Usuario(int id, String nombre, String correo, int tfno, String fecha_nac){
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.tfno = tfno;
        this.fecha_nac = fecha_nac;
    }

    public Usuario(int id, String nombre, String correo, int tfno, String fecha_nac, byte[] imagen){
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.tfno = tfno;
        this.fecha_nac = fecha_nac;
        this.imagen = imagen;
    }

    public Usuario(String nombre, String correo, int tfno, String fecha_nac, byte[] imagen){
        this.nombre = nombre;
        this.correo = correo;
        this.tfno = tfno;
        this.fecha_nac = fecha_nac;
        this.imagen = imagen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getTfno() {
        return tfno;
    }

    public void setTfno(int tfno) {
        this.tfno = tfno;
    }

    public String getFecha_nac() {
        return fecha_nac;
    }

    public void setFecha_nac(String fecha_nac) {
        this.fecha_nac = fecha_nac;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", tfno=" + tfno +
                ", fecha_nac='" + fecha_nac + '\'' +
                ", imagen=" + imagen +
                '}';
    }
}
