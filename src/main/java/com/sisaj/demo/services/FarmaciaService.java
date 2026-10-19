package com.sisaj.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sisaj.demo.model.Farmacia;
import com.sisaj.demo.repository.FarmaciaRepository;



@Service // Esta anotación le dice a Spring: "Este es el cerebro de las operaciones de farmacia"
public class FarmaciaService {

    // Inyectamos el repositorio. Spring crea el objeto automáticamente.
    @Autowired
    private FarmaciaRepository farmaciaRepository;

    // 1. Listar todos (CRUD básico)
    public List<Farmacia> listarTodos() {
        return farmaciaRepository.findAll();
    }

    // 2. Obtener por ID (CRUD básico)
    public Optional<Farmacia> obtenerPorId(Integer id) {
        return farmaciaRepository.findById(id);
    }

    // 3. Registrar o Actualizar medicamento (CRUD básico)
    public Farmacia registrar(Farmacia farmacia) {
        return farmaciaRepository.save(farmacia);
    }

    // 4. Eliminar (CRUD básico)
    public void eliminar(Integer id) {
        farmaciaRepository.deleteById(id);
    }

    // ==========================================
    // LÓGICA DE NEGOCIO (Lo que antes hacías en el DAO manual)
    // ==========================================

    // 5. Vender un medicamento (Regla de negocio: Validar stock)
    public void vender(Integer idMedicamento, Integer cantidadSolicitada) {
        
        // Buscamos el medicamento en la BD
        Farmacia medicamento = farmaciaRepository.findById(idMedicamento)
                .orElseThrow(() -> new RuntimeException("Error: El medicamento no existe en el sistema."));

        // Validamos lógicamente el stock
        if (medicamento.getCantidadDisponible() < cantidadSolicitada) {
            throw new IllegalArgumentException("Stock insuficiente. Solo quedan " 
                    + medicamento.getCantidadDisponible() + " unidades de " + medicamento.getNombreMedicamento());
        }

        // Restamos el stock y guardamos
        int nuevoStock = medicamento.getCantidadDisponible() - cantidadSolicitada;
        medicamento.setCantidadDisponible(nuevoStock);
        
        farmaciaRepository.save(medicamento);
    }

    // 6. Actualizar precio usando la consulta personalizada que hicimos en el Repositorio
    public void actualizarPrecio(Integer idMedicamento, Double nuevoPrecio) {
        if (nuevoPrecio <= 0) {
            throw new IllegalArgumentException("El precio no puede ser cero o negativo.");
        }
        farmaciaRepository.actualizarPrecio(idMedicamento, nuevoPrecio);
    }
}