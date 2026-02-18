package edu.comillas.icai.gitt.pat.spring.PracticaFinal;


import edu.comillas.icai.gitt.pat.spring.PracticaFinal.ModeloUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioUsuario extends JpaRepository<ModeloUsuario, Long> {
}
