package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import java.math.BigDecimal;

public class PistaPatchRequest {
    private BigDecimal precioHora;
    private Boolean activa;

    public BigDecimal getPrecioHora() { return precioHora; }
    public void setPrecioHora(BigDecimal precioHora) { this.precioHora = precioHora; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
