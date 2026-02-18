package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioPista extends CrudRepository<ModeloPista, Long> {
    ModeloPista findByNombre(String nombre);
}
