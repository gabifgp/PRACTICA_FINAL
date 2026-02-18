package edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository;

import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<ModeloReserva, Long> {

    @Query("""
        SELECT r
        FROM ModeloReserva r
        WHERE (:date IS NULL OR r.fechaReserva = :date)
          AND (:courtId IS NULL OR r.pista.idPista = :courtId)
          AND (:userId IS NULL OR r.usuario.idUsuario = :userId)
        ORDER BY r.fechaReserva, r.horaInicio
        """)
    List<ModeloReserva> adminFindAllWithFilters(
            @Param("date") LocalDate date,
            @Param("courtId") Long courtId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM ModeloReserva r
        WHERE r.estado = edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva$Estado.ACTIVA
          AND r.pista.idPista = :pistaId
          AND r.fechaReserva = :fecha
          AND r.idReserva <> :reservaId
          AND r.horaInicio < :nuevoFin
          AND r.horaFin > :nuevoInicio
        """)
    boolean existeSolapeActivo(
            @Param("pistaId") Long pistaId,
            @Param("fecha") LocalDate fecha,
            @Param("nuevoInicio") LocalTime nuevoInicio,
            @Param("nuevoFin") LocalTime nuevoFin,
            @Param("reservaId") Long reservaId
    );
}
