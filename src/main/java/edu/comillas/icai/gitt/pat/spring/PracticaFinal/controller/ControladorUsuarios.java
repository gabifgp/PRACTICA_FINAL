package edu.comillas.icai.gitt.pat.spring.PracticaFinal.controller;

import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloRol;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.repository.RepositorioUsuario;
import edu.comillas.icai.gitt.pat.spring.PracticaFinal.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pistaPadel/auth")
public class ControladorUsuarios {

    private final RepositorioUsuario repositorioUsuario;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String USUARIO_SESION = "USUARIO_LOGUEADO";

    // Inyección por constructor (como se ve en la lógica de Spring buena)
    public ControladorUsuarios(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloUsuario register(@Valid @RequestBody ModeloUsuario usuario) {
        logger.info("Intentando registrar usuario con email: {}", usuario.getEmail());

        if (repositorioUsuario.existsByEmail(usuario.getEmail())) {
            logger.warn("El email {} ya está registrado", usuario.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe");
        }

        // Si el usuario no trae rol en el JSON, le ponemos USER por defecto
        if (usuario.getRol() == null) {
            usuario.setRol(ModeloRol.USER);
        }
        // Si trae un rol, dejamos el que viene (ADMIN o USER)
        // Spring Boot se encarga de convertir el String del JSON al Enum ModeloRol automáticamente

        return repositorioUsuario.save(usuario);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        ModeloUsuario usuario = repositorioUsuario.findByEmail(loginRequest.getEmail());

        if (usuario == null || !usuario.getPassword().equals(loginRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        // Guardamos el email en la sesión
        session.setAttribute(USUARIO_SESION, usuario.getEmail());
        return "Login exitoso";
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        if (session.getAttribute(USUARIO_SESION) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        session.invalidate();
        logger.info("Sesión cerrada correctamente");
    }

    @GetMapping("/me")
    public ModeloUsuario getMe(HttpSession session) {
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return repositorioUsuario.findByEmail(email);
    }
}