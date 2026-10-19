package com.sisaj.demo.repository;

import com.sisaj.demo.model.Historia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriaRepository extends JpaRepository<Historia, Long> {

	@Query("SELECT h FROM Historia h WHERE h.atencionMedico.paciente.dni = :dni ORDER BY h.idHistoria DESC")
    List<Historia> buscarHistorialPorDniPaciente(@Param("dni") String dni);
}