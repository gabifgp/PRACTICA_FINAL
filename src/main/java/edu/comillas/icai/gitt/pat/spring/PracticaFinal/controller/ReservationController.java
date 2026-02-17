package edu.comillas.icai.gitt.pat.spring.PracticaFinal.controller;

import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.dto.PatchReservationRequest;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/pistaPadel")
public class ReservationController {

    private final ReservaRepository reservaRepository;

    public ReservationController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    // DELETE /pistaPadel/reservations/{id}
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancel(@PathVariable Long reservationId) {

        ModeloReserva reserva = reservaRepository.findById(reservationId).orElse(null);
        if (reserva == null) return ResponseEntity.notFound().build();

        // Si ya está cancelada, devolver 204 igualmente (idempotente) o 409 si tu profe lo pide.
        reserva.setEstado(ModeloReserva.Estado.CANCELADA);
        reservaRepository.save(reserva);

        return ResponseEntity.noContent().build(); // 204
    }

    // PATCH /pistaPadel/reservations/{id}
    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<ModeloReserva> patch(
            @PathVariable Long reservationId,
            @RequestBody PatchReservationRequest body
    ) {
        ModeloReserva reserva = reservaRepository.findById(reservationId).orElse(null);
        if (reserva == null) return ResponseEntity.notFound().build();

        // No permitir modificar canceladas
        if (reserva.getEstado() == ModeloReserva.Estado.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva cancelada no se puede modificar");
        }

        // Base = valores actuales
        LocalDate nuevaFecha = (body.getDate() != null) ? body.getDate() : reserva.getFechaReserva();
        LocalTime nuevoInicio = (body.getStartTime() != null) ? body.getStartTime() : reserva.getHoraInicio();
        Integer nuevaDuracion = (body.getDurationMinutes() != null) ? body.getDurationMinutes() : reserva.getDuracionMinutos();

        if (nuevaFecha == null || nuevoInicio == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha/hora inválidas");
        }
        if (nuevaDuracion == null || nuevaDuracion < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duración inválida");
        }

        // Aplica cambios (solo los campos presentes)
        if (body.getDate() != null) reserva.setFechaReserva(nuevaFecha);
        if (body.getStartTime() != null) reserva.setHoraInicio(nuevoInicio);
        if (body.getDurationMinutes() != null) reserva.setDuracionMinutos(nuevaDuracion);

        // horaFin se recalcula en @PreUpdate si lo tienes bien
        ModeloReserva actualizada = reservaRepository.save(reserva);
        return ResponseEntity.ok(actualizada);
    }
}
