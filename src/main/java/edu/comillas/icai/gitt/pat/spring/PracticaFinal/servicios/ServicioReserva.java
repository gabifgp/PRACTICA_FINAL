package edu.comillas.icai.gitt.pat.spring.PracticaFinal.servicios;


import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloPista;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioPista;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalTime;


@Service
public class ServicioReserva {

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
        //400
        if (req == null
                || req.getPista() == null
                || req.getPista().getIdPista() == null   // AQUI
                || req.getFechaReserva() == null
                || req.getHoraInicio() == null
                || req.getDuracionMinutos() == null
                || req.getDuracionMinutos() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request invalida");
        }
        // 401
        ModeloUsuario usuario = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        //404
        ModeloPista pista = repositorioPista.findById(req.getPista().getIdPista())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));

        // Si tu ModeloPista tiene "isActiva()", descomenta:
        // if (!pista.isActiva()) {
        //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pista inactiva");
        // }

        LocalTime inicio = req.getHoraInicio();
        LocalTime fin = inicio.plusMinutes(req.getDuracionMinutos());
        //409
        boolean solapa = repositorioReserva
                .existsByPistaAndFechaReservaAndEstadoAndHoraInicioLessThanAndHoraFinGreaterThan(
                        pista,
                        req.getFechaReserva(),
                        ModeloReserva.Estado.ACTIVA,
                        fin,     // horaInicioLessThan = fin nueva
                        inicio   // horaFinGreaterThan = inicio nueva
                );

        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La franja ya esta reservada");
        }




        ModeloReserva reserva = new ModeloReserva();
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(req.getFechaReserva());
        reserva.setHoraInicio(inicio);
        reserva.setDuracionMinutos(req.getDuracionMinutos());
        reserva.setEstado(ModeloReserva.Estado.ACTIVA);

        return repositorioReserva.save(reserva);
    }

    public List<ModeloReserva> listarMisReservas(Long userId, LocalDateTime from, LocalDateTime to) {

        ModeloUsuario usuario = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Base: solo activas (si quieres incluir canceladas, quita el estado)
        List<ModeloReserva> reservas = repositorioReserva
                .findByUsuarioAndEstadoOrderByFechaReservaAscHoraInicioAsc(usuario, ModeloReserva.Estado.ACTIVA);

        // Filtros opcionales
        if (from != null) {
            reservas = reservas.stream()
                    .filter(r -> LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).compareTo(from) >= 0)
                    .toList();
        }
        if (to != null) {
            reservas = reservas.stream()
                    .filter(r -> LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio()).compareTo(to) <= 0)
                    .toList();
        }
        return reservas;
    }

    public ModeloReserva obtenerReserva(Long userId, Long reservationId) {
        if (reservationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id invalido");
        }

        ModeloUsuario solicitante = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));

        ModeloReserva reserva = repositorioReserva.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        boolean esAdmin = solicitante.getRol() != null && solicitante.getRol().toString().equalsIgnoreCase("ADMIN");
        boolean esDueno = reserva.getUsuario() != null
                && reserva.getUsuario().getIdUsuario() != null
                && reserva.getUsuario().getIdUsuario().equals(solicitante.getIdUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return reserva;
    }

}
