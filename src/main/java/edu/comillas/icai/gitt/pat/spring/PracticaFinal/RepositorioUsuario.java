package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioUsuario extends CrudRepository<ModeloUsuario, Long> {
    ModeloUsuario findByEmail(String email);
    boolean existsByEmail(String email);
}
