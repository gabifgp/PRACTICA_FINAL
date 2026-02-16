package edu.comillas.icai.gitt.pat.spring.PracticaFinal;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email")
        }
)
public class ModeloUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 150)
    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @NotBlank
    @Email
    @Size(max = 200)
    @Column(name = "email", nullable = false, length = 200)
    private String email;

    // Guardar siempre hash, nunca texto plano
    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Size(max = 30)
    @Column(name = "telefono", length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeloRol rol;


    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // 0..n reservas
    // Cuando creeis ModeloReserva, ajustad mappedBy al nombre del atributo en Reserva.
    @OneToMany(mappedBy = "usuario")
    private List<ModeloReserva> reservas;



    public ModeloUsuario() {
    }

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}