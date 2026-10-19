package com.sisaj.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisaj.demo.model.TarifarioVenta;


@Repository
public interface TarifarioVentaRepository extends JpaRepository<TarifarioVenta, Integer> {

    // Ya tienes save() para registrar, findAll() para listar y reportes, 
    // y findById() para obtener una venta específica.

    // Si en el futuro necesitas un reporte de ventas entre dos fechas, 
    // Spring te lo crea solo con este nombre:
    // List<TarifarioVenta> findByFechaEmitidaBetween(Date inicio, Date fin);
}