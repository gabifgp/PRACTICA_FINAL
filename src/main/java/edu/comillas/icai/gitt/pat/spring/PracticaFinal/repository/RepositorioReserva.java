package edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository;

import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloPista;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RepositorioReserva extends JpaRepository<ModeloReserva, Long> {

    // Para disponibilidad: reservas de una pista en un dia (solo activas)
    List<ModeloReserva> findByPistaAndFechaReservaAndEstadoOrderByHoraInicioAsc(
            ModeloPista pista,
            LocalDate fechaReserva,
            ModeloReserva.Estado estado
    );

    // Para "mis reservas": reservas de un usuario (solo activas o todas, segun uses)
    List<ModeloReserva> findByUsuarioAndEstadoOrderByFechaReservaAscHoraInicioAsc(
            ModeloUsuario usuario,
            ModeloReserva.Estado estado
    );

    List<ModeloReserva> findByUsuarioOrderByFechaReservaAscHoraInicioAsc(ModeloUsuario usuario);


    // Para 409 (solape): existe reserva activa que pisa el intervalo [inicioNueva, finNueva)
    boolean existsByPistaAndFechaReservaAndEstadoAndHoraInicioLessThanAndHoraFinGreaterThan(
            ModeloPista pista,
            LocalDate fechaReserva,
            ModeloReserva.Estado estado,
            LocalTime finNueva,   // finNueva
            LocalTime inicioNueva    // inicioNueva
    );

    /*boolean existsByPistaAndFechaReservaAndEstadoAndHoraInicioLessThanAndHoraFinGreaterThan(
            ModeloPista pista,
            LocalDate fechaReserva,
            ModeloReserva.Estado estado,
            LocalTime horaInicioNuevaFin,
            LocalTime horaFinNuevaIni
    );*/

    List<ModeloReserva> findByUsuarioAndFechaReservaGreaterThanEqualOrderByFechaReservaAscHoraInicioAsc(
            ModeloUsuario usuario, LocalDate from
    );

    List<ModeloReserva> findByUsuarioAndFechaReservaLessThanEqualOrderByFechaReservaAscHoraInicioAsc(
            ModeloUsuario usuario, LocalDate to
    );

    List<ModeloReserva> findByUsuarioAndFechaReservaBetweenOrderByFechaReservaAscHoraInicioAsc(
            ModeloUsuario usuario, LocalDate from, LocalDate to
    );
  
    List<ModeloReserva> findByUsuario(ModeloUsuario usuario);
    List<ModeloReserva> findByPistaAndFechaReserva(ModeloPista pista, LocalDate fecha);

}

