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

import java.util.List;

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

    @PatchMapping("/me")
    public ModeloUsuario actualizarPerfil(HttpSession session, @RequestBody ModeloUsuario datosNuevos) {
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        ModeloUsuario usuario = repositorioUsuario.findByEmail(email);

        // Solo actualizamos si el campo viene en el JSON
        if (datosNuevos.getNombre() != null) usuario.setNombre(datosNuevos.getNombre());
        if (datosNuevos.getApellidos() != null) usuario.setApellidos(datosNuevos.getApellidos());
        if (datosNuevos.getPassword() != null) usuario.setPassword(datosNuevos.getPassword());

        return repositorioUsuario.save(usuario);
    }

    // Añade esto al final de ControladorUsuarios.java
    @GetMapping("/users")
    public List<ModeloUsuario> listarTodosLosUsuarios(HttpSession session) {
        validarAdmin(session); // Reutiliza la lógica de validarAdmin que tienes en otros controllers
        return repositorioUsuario.findAll();
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id, HttpSession session) {
        validarAdmin(session);
        repositorioUsuario.deleteById(id);
    }

    // Método útil para que otros controladores verifiquen el rol
    public void validarAdmin(HttpSession session) {
        String email = (String) session.getAttribute(USUARIO_SESION);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        ModeloUsuario usuario = repositorioUsuario.findByEmail(email);
        if (usuario == null || usuario.getRol() != ModeloRol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Se requiere ser Administrador");
        }
    }
}