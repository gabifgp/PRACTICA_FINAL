package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepositorioReserva extends CrudRepository<ModeloReserva, Long> {
    List<ModeloReserva> findByUsuario(ModeloUsuario usuario);
    List<ModeloReserva> findByPistaAndFechaReserva(ModeloPista pista, LocalDate fecha);
}
