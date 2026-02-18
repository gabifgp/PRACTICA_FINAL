package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioUsuario extends CrudRepository<ModeloUsuario, Long> {
    List<ModeloUsuario> findAll();

    ModeloUsuario findByEmail(String email);
    boolean existsByEmail(String email);
}


