package com.sisaj.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sisaj.demo.model.AtencionMedico;

@Repository
public interface AtencionMedicoRepository extends JpaRepository<AtencionMedico, Integer> {

    // 1. GLOBAL: El Administrador (Rol 1) ve y busca en todo
    @Query("SELECT a FROM AtencionMedico a WHERE " +
           "LOWER(a.paciente.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.paciente.apellidoPaterno) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.estado) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    Page<AtencionMedico> buscarAtencionesGlobal(@Param("filtro") String filtro, Pageable pageable);

    // 2. FILTRADO: El Médico (Rol 2) solo lista sus propios pacientes
    Page<AtencionMedico> findByIdMedico(Integer idMedico, Pageable pageable);

    // 3. BUSCADOR FILTRADO: El Médico busca palabras clave SOLO dentro de sus pacientes
    @Query("SELECT a FROM AtencionMedico a WHERE a.idMedico = :idMedico AND (" +
           "LOWER(a.paciente.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.paciente.apellidoPaterno) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.estado) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<AtencionMedico> buscarAtencionesPorMedico(@Param("idMedico") Integer idMedico, @Param("filtro") String filtro, Pageable pageable);
}