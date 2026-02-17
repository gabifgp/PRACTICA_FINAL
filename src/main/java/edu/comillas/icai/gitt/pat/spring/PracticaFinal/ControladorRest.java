package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pistaPadel/auth")
public class ControladorRest {

    private final RepositorioUsuario repositorioUsuario;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String USUARIO_SESION = "USUARIO_LOGUEADO";

    // Inyección por constructor (como se ve en la lógica de Spring buena)
    public ControladorRest(RepositorioUsuario repositorioUsuario) {
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

        usuario.setRol(ModeloRol.USER);
        return repositorioUsuario.save(usuario);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        logger.info("Intento de login para: {}", loginRequest.getEmail());

        ModeloUsuario usuario = repositorioUsuario.findByEmail(loginRequest.getEmail());

        // Comprobamos si existe y si la contraseña coincide (aquí iría un encoder en el futuro)
        if (usuario == null || !usuario.getPassword().equals(loginRequest.getPassword())) {
            logger.error("Credenciales incorrectas para: {}", loginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

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