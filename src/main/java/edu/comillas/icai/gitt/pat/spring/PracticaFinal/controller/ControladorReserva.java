package edu.comillas.icai.gitt.pat.spring.PracticaFinal.controller;



import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.servicios.ServicioReserva;
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


}
