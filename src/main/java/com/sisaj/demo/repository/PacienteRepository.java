package com.sisaj.demo.repository;

import com.sisaj.demo.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    
    // Spring genera: SELECT * FROM paciente WHERE dni = ?
    Optional<Paciente> findByDni(String dni);
}