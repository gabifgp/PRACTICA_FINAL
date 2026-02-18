package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepositorioPista extends JpaRepository<ModeloPista, Long> {

    // Cambia "activa" por el nombre real del boolean en ModeloPista
    List<ModeloPista> findByActiva(boolean activa);
}
