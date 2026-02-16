package edu.comillas.icai.gitt.pat.spring.PracticaFinal;


public enum ModeloRol {
    USER("Usuario normal"),
    ADMIN("Administrador");

    private final String descripcion;

    ModeloRol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
