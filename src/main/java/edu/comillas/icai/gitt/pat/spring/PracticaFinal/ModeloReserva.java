package edu.comillas.icai.gitt.pat.spring.PracticaFinal;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter

public class ModeloReserva {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;

    // Exactamente 1 usuario
    @Getter
    @Setter
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private ModeloUsuario usuario;


    // Exactamente 1 pista
    @Getter
    @Setter
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pista", nullable = false)
    private ModeloPista pista;


    // Dia de la reserva
    @Getter
    @Setter
    @NotNull
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    // Hora de inicio
    @Getter
    @Setter
    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    // Duracion en minutos
    @Getter
    @Setter
    @NotNull
    @Min(1)
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    // Hora fin (calculada)
    // La guardamos para facilitar consultas, pero se calcula automaticamente.
    @Getter
    @Setter
    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private Estado estado = Estado.ACTIVA;

    @Getter
    @Setter
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public ModeloReserva(ModeloUsuario usuario) {
        this.usuario = usuario;
    }

    public ModeloReserva() {

    }


    public @NotNull <U> LocalTime getHoraInicio() {
        return horaInicio;
    }

    @Getter
    public enum Estado {
        ACTIVA, CANCELADA
    }


    //public ModeloReserva(ModeloUsuario usuario) {
    //}

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