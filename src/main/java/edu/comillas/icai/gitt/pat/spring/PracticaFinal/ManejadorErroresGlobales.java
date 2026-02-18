package edu.comillas.icai.gitt.pat.spring.PracticaFinal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ManejadorErroresGlobales {

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Void> errorLanzado(ResponseStatusException ex) {
        // Retorna el código de estado de la excepción (401, 404, 409...)
        return new ResponseEntity<>(ex.getStatusCode());
    }
}
