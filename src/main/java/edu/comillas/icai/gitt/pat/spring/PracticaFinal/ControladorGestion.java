package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
public class ControladorGestion {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPista repositorioPista;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String USUARIO_SESION = "USUARIO_LOGUEADO";

    public ControladorGestion(RepositorioUsuario repositorioUsuario, RepositorioPista repositorioPista) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPista = repositorioPista;
    }

    // --- ENDPOINTS DE USUARIOS ---

    // 1. (ADMIN) Listado de usuarios
    @GetMapping("/pistaPadel/users")
    public List<ModeloUsuario> getAllUsers(HttpSession session) {
        verificarRol(session, ModeloRol.ADMIN);
        logger.info("ADMIN solicitando lista de usuarios");
        return repositorioUsuario.findAll();
    }

    // 2. (ADMIN o dueño) Obtener un usuario por id
    @GetMapping("/pistaPadel/users/{userId}")
    public ModeloUsuario getUserById(@PathVariable Long userId, HttpSession session) {
        ModeloUsuario solicitante = obtenerUsuarioSesion(session);
        ModeloUsuario objetivo = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Regla de negocio: ADMIN o el propio usuario
        if (solicitante.getRol() != ModeloRol.ADMIN && !solicitante.getIdUsuario().equals(userId)) {
            logger.warn("Usuario {} intentó acceder a datos de {}", solicitante.getEmail(), userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return objetivo;
    }

    // 3. (ADMIN o dueño) Actualizar datos parciales
    @PatchMapping("/pistaPadel/users/{userId}")
    public ModeloUsuario updateUser(@PathVariable Long userId, @RequestBody ModeloUsuario datosNuevos, HttpSession session) {
        ModeloUsuario solicitante = obtenerUsuarioSesion(session);
        ModeloUsuario usuarioAEditar = repositorioUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (solicitante.getRol() != ModeloRol.ADMIN && !solicitante.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Actualizamos solo lo permitido
        if (datosNuevos.getNombre() != null) usuarioAEditar.setNombre(datosNuevos.getNombre());
        if (datosNuevos.getApellidos() != null) usuarioAEditar.setApellidos(datosNuevos.getApellidos());
        if (datosNuevos.getTelefono() != null) usuarioAEditar.setTelefono(datosNuevos.getTelefono());

        logger.info("Datos actualizados para el usuario ID: {}", userId);
        return repositorioUsuario.save(usuarioAEditar);
    }

    // --- ENDPOINTS DE PISTAS ---

    // 4. (ADMIN) Crear pista
    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloPista createCourt(@Valid @RequestBody ModeloPista pista, HttpSession session) {
        verificarRol(session, ModeloRol.ADMIN);

        // El nombre debe ser único según la guía
        if (repositorioPista.findByNombre(pista.getNombre())!= null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la pista ya existe");
        }

        logger.info("Nueva pista '{}' creada por ADMIN", pista.getNombre());
        return repositorioPista.save(pista);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private ModeloUsuario obtenerUsuarioSesion(HttpSession session) {
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión activa");
        }
        return repositorioUsuario.findByEmail(email);
    }

    private void verificarRol(HttpSession session, ModeloRol rolRequerido) {
        ModeloUsuario user = obtenerUsuarioSesion(session);
        if (user.getRol() != rolRequerido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permisos insuficientes");
        }
    }
}
