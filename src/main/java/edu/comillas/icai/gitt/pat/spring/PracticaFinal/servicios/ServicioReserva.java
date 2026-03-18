package edu.comillas.icai.gitt.pat.spring.PracticaFinal.servicios;

import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloPista;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloRol;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.dto.PatchReservationRequest;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioPista;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ServicioReserva {

    private static final Logger logger = LoggerFactory.getLogger(ServicioReserva.class);

    private final RepositorioReserva repositorioReserva;
    private final RepositorioPista repositorioPista;
    private final RepositorioUsuario repositorioUsuario;

    public ServicioReserva(RepositorioReserva repositorioReserva,
                           RepositorioPista repositorioPista,
                           RepositorioUsuario repositorioUsuario) {
        this.repositorioReserva = repositorioReserva;
        this.repositorioPista = repositorioPista;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Transactional
    public ModeloReserva crearReserva(Long userId, ModeloReserva req) {
        if (req.getFechaReserva().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reservar en una fecha pasada");
        }

        if (req.getFechaReserva().isEqual(LocalDate.now()) && req.getHoraInicio().isBefore(LocalTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio ya ha pasado");
        }

        logger.info("Solicitud de nueva reserva - Usuario ID: {}", userId);

        ModeloUsuario usuario = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        ModeloPista pista = repositorioPista.findById(req.getPista().getIdPista())
                .orElseThrow(() -> {
                    logger.error("Error: Intento de reserva en pista inexistente ID: {}", req.getPista().getIdPista());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
                });

        logger.debug("Validando solapes para Pista: {}, Fecha: {}, Inicio: {}",
                pista.getNombre(), req.getFechaReserva(), req.getHoraInicio());

        LocalTime horaFin = req.getHoraInicio().plusMinutes(req.getDuracionMinutos());
        // En creación pasamos null como reservaId porque es nueva
        boolean solape = repositorioReserva.existeSolapeActivo(
                pista.getIdPista(), req.getFechaReserva(), null, req.getHoraInicio(), horaFin);

        if (solape) {
            logger.warn("Conflicto de reserva: Horario ocupado en pista {}", pista.getNombre());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista ya está ocupada en ese horario");
        }

        req.setUsuario(usuario);
        req.setPista(pista);
        req.setEstado(ModeloReserva.Estado.ACTIVA);

        ModeloReserva guardada = repositorioReserva.save(req);
        logger.info("Reserva creada con éxito. ID Reserva: {}", guardada.getIdReserva());
        return guardada;
    }

    public List<ModeloReserva> listarMisReservas(Long userId, LocalDateTime from, LocalDateTime to) {
        logger.info("Consultando historial de reservas para Usuario ID: {}", userId);
        ModeloUsuario usuario = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<ModeloReserva> reservas = repositorioReserva.findByUsuarioOrderByFechaReservaAscHoraInicioAsc(usuario);

        // Filtrado por fechas si se proporcionan
        if (from != null) {
            reservas = reservas.stream()
                    .filter(r -> LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).isAfter(from) ||
                            LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).isEqual(from))
                    .toList();
        }
        if (to != null) {
            reservas = reservas.stream()
                    .filter(r -> LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).isBefore(to) ||
                            LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).isEqual(to))
                    .toList();
        }
        return reservas;
    }

    @Transactional
    public void cancelarReserva(Long userId, Long reservationId) {
        ModeloReserva reserva = obtenerReservaYValidarPropiedad(userId, reservationId);
        reserva.setEstado(ModeloReserva.Estado.CANCELADA);
        repositorioReserva.save(reserva);
        logger.info("Reserva ID: {} CANCELADA por usuario ID: {}", reservationId, userId);
    }

    public ModeloReserva obtenerReserva(Long userId, Long reservationId) {
        return obtenerReservaYValidarPropiedad(userId, reservationId);
    }

    @Transactional
    public ModeloReserva actualizarReserva(Long userId, Long reservationId, PatchReservationRequest body) {
        logger.info("Intento de actualización de reserva ID: {} por usuario ID: {}", reservationId, userId);

        // 1. Validar existencia y propiedad
        ModeloReserva reserva = obtenerReservaYValidarPropiedad(userId, reservationId);

        // 2. Aplicar cambios desde el DTO (usando los nombres de tu archivo PatchReservationRequest)
        if (body.getDate() != null) {
            reserva.setFechaReserva(body.getDate());
        }
        if (body.getStartTime() != null) {
            reserva.setHoraInicio(body.getStartTime());
        }
        if (body.getDurationMinutes() != null) {
            reserva.setDuracionMinutos(body.getDurationMinutes());
        }

        // 3. Validar solapes con el nuevo horario/pista
        logger.debug("Validando disponibilidad para la actualización de la reserva {}", reservationId);
        LocalTime horaFin = reserva.getHoraInicio().plusMinutes(reserva.getDuracionMinutos());

        // Pasamos reservationId para que el repositorio ignore esta misma reserva al buscar choques
        boolean haySolape = repositorioReserva.existeSolapeActivo(
                reserva.getPista().getIdPista(),
                reserva.getFechaReserva(),
                reservationId,
                reserva.getHoraInicio(),
                horaFin
        );

        if (haySolape) {
            logger.warn("No se puede actualizar: conflicto de horario con otra reserva");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nuevo horario no está disponible");
        }

        ModeloReserva actualizada = repositorioReserva.save(reserva);
        logger.info("Reserva ID: {} actualizada con éxito", actualizada.getIdReserva());
        return actualizada;
    }

    private ModeloReserva obtenerReservaYValidarPropiedad(Long userId, Long reservationId) {
        logger.debug("Verificando permisos para User: {}, Reserva: {}", userId, reservationId);

        ModeloUsuario solicitante = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        ModeloReserva reserva = repositorioReserva.findById(reservationId)
                .orElseThrow(() -> {
                    logger.error("Error: Reserva {} no encontrada", reservationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
                });

        boolean esAdmin = solicitante.getRol() == ModeloRol.ADMIN;
        boolean esDueno = reserva.getUsuario().getIdUsuario().equals(solicitante.getIdUsuario());

        if (!esAdmin && !esDueno) {
            logger.warn("Acceso denegado: Usuario {} no tiene permiso sobre reserva {}", userId, reservationId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso sobre esta reserva");
        }
        return reserva;
    }
}