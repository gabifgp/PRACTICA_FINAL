package edu.comillas.icai.gitt.pat.spring.PracticaFinal;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reservas")
public class ModeloReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;

    // Exactamente 1 usuario
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private ModeloUsuario usuario;


    // Exactamente 1 pista
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pista", nullable = false)
    private ModeloPista pista;


    // Dia de la reserva
    @NotNull
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    // Hora de inicio
    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    // Duracion en minutos
    @NotNull
    @Min(1)
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    // Hora fin (calculada)
    // La guardamos para facilitar consultas, pero se calcula automaticamente.
    @Getter
    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private Estado estado = Estado.ACTIVA;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public ModeloReserva() {

    }

    public @NotNull <U> LocalTime getHoraInicio() {
        return horaInicio;
    }

    public enum Estado {
        ACTIVA, CANCELADA
    }


    public ModeloReserva(ModeloUsuario usuario) {
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        recalcularHoraFin();
    }

    @PreUpdate
    public void preUpdate() {
        recalcularHoraFin();
    }

    private void recalcularHoraFin() {
        if (horaInicio != null && duracionMinutos != null) {
            this.horaFin = horaInicio.plusMinutes(duracionMinutos);
        }
    }
}