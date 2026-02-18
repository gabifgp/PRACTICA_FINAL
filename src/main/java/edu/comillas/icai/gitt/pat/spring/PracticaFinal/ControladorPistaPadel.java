package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel/courts")
public class ControladorPistaPadel {

    private final RepositorioPista repositorioPista;
    private final RepositorioUsuario repositorioUsuario;

    private static final String USUARIO_SESION = "USUARIO_LOGUEADO";

    public ControladorPistaPadel(RepositorioPista repositorioPista, RepositorioUsuario repositorioUsuario) {
        this.repositorioPista = repositorioPista;
        this.repositorioUsuario = repositorioUsuario;
    }

    // 1) GET /pistaPadel/courts?active=true/false
    @GetMapping
    public List<ModeloPista> listarPistas(@RequestParam(required = false) Boolean active) {
        if (active == null) {
            return repositorioPista.findAll();
        }
        return repositorioPista.findByActiva(active);
    }

    // 2) GET /pistaPadel/courts/{courtId}
    @GetMapping("/{courtId}")
    public ModeloPista obtenerPista(@PathVariable Long courtId) {
        return repositorioPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));
    }

    // 3) PATCH /pistaPadel/courts/{courtId} (ADMIN)
    @PatchMapping("/{courtId}")
    public ModeloPista modificarPista(
            @PathVariable Long courtId,
            @RequestBody PistaPatchRequest body,
            HttpSession session) {

        // 401: no autenticado
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // buscar usuario (si no existe, 401)
        ModeloUsuario usuario = repositorioUsuario.findByEmail(email);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // 403: no admin
        if (usuario.getRol() != ModeloRol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // 404: pista no encontrada
        ModeloPista pista = repositorioPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));

        // 400: body vacío
        if (body.getPrecioHora() == null && body.getActiva() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay campos para modificar");
        }

        // actualizar precio
        if (body.getPrecioHora() != null) {
            if (body.getPrecioHora().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora no puede ser negativo");
            }
            pista.setPrecioHora(body.getPrecioHora());
        }

        // actualizar activa
        if (body.getActiva() != null) {
            pista.setActiva(body.getActiva());
        }

        return repositorioPista.save(pista);
    }

    // 4) DELETE /pistaPadel/courts/{courtId} (ADMIN) -> desactivar pista
    @DeleteMapping("/{courtId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPista(
            @PathVariable Long courtId,
            HttpSession session) {

        // 401: no autenticado
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // buscar usuario (si no existe, 401)
        ModeloUsuario usuario = repositorioUsuario.findByEmail(email);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // 403: no admin
        if (usuario.getRol() != ModeloRol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // 404: pista no encontrada
        ModeloPista pista = repositorioPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));

        // "Eliminar" = desactivar
        pista.setActiva(false);
        repositorioPista.save(pista);
    }
}
