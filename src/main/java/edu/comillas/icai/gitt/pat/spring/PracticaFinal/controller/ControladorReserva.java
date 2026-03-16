package edu.comillas.icai.gitt.pat.spring.PracticaFinal.controller;



import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.servicios.ServicioReserva;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel")
public class ControladorReserva {

    private final ServicioReserva servicioReserva;
    private final RepositorioUsuario repositorioUsuario; // Añadimos esto
    private static final String USUARIO_SESION = "USUARIO_LOGUEADO";

    public ControladorReserva(ServicioReserva servicioReserva, RepositorioUsuario repositorioUsuario) {
        this.servicioReserva = servicioReserva;
        this.repositorioUsuario = repositorioUsuario;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloReserva crearReserva(@RequestBody ModeloReserva req, HttpSession session) {
        ModeloUsuario usuario = obtenerUsuarioDeSesion(session);
        return servicioReserva.crearReserva(usuario.getIdUsuario(), req);
    }

    @GetMapping("/reservations")
    public List<ModeloReserva> misReservas(
            HttpSession session,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        ModeloUsuario usuario = obtenerUsuarioDeSesion(session);
        return servicioReserva.listarMisReservas(usuario.getIdUsuario(), from, to);
    }

    @GetMapping("/reservations/{reservationId}")
    public ModeloReserva obtenerReserva(@PathVariable Long reservationId, HttpSession session) {
        ModeloUsuario usuario = obtenerUsuarioDeSesion(session);
        return servicioReserva.obtenerReserva(usuario.getIdUsuario(), reservationId);
    }

    // Método de apoyo para no repetir código
    private ModeloUsuario obtenerUsuarioDeSesion(HttpSession session) {
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        ModeloUsuario usuario = repositorioUsuario.findByEmail(email);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }
        return usuario;
    }
}
/*
@RestController
@RequestMapping("/pistaPadel")
public class ControladorReserva {

    private final ServicioReserva servicioReserva;

    public ControladorReserva(ServicioReserva servicioReserva) {
        this.servicioReserva = servicioReserva;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloReserva crearReserva(
            @RequestHeader(name="X-User-Id", required=false) Long userId,
            @RequestBody ModeloReserva req
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return servicioReserva.crearReserva(userId, req);
    }

    @PostMapping("/reservations/test")
    public String testHeader(@RequestHeader(name="X-User-Id", required=false) String userId) {
        return "HEADER=" + userId;
    }

    @GetMapping("/reservations")
    public List<ModeloReserva> misReservas(
            @RequestHeader(name="X-User-Id", required=false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        return servicioReserva.listarMisReservas(userId, from, to);
    }


    @GetMapping("/reservations/{reservationId}")
    public ModeloReserva obtenerReserva(
            @RequestHeader(name="X-User-Id", required=false) Long userId,
            @PathVariable Long reservationId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return servicioReserva.obtenerReserva(userId, reservationId);
    }


}*/
