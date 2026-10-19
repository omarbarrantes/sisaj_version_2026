package com.sisaj.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sisaj.demo.model.Farmacia;


@Repository
public interface FarmaciaRepository extends JpaRepository<Farmacia, Integer> {

    // Nota: El método vender() pasará a la capa de SERVICIO, 
    // ya que requiere lógica de negocio (validar stock antes de restar).

    // Método especializado: Actualizar precio usando JPQL (Query Language de JPA)
    @Modifying
    @Transactional
    @Query("UPDATE Farmacia f SET f.precioUnitario = :nuevoPrecio WHERE f.idFarmacia = :id")
    void actualizarPrecio(Integer id, Double nuevoPrecio);
}