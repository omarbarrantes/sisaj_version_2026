package com.sisaj.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sisaj.demo.model.Farmacia;
import com.sisaj.demo.repository.FarmaciaRepository;

@RestController
@RequestMapping("/api/farmacia")
@CrossOrigin(origins = "http://localhost:8080")
public class FarmaciaRestController {

    @Autowired
    private FarmaciaRepository farmaciaRepository; // Conexión real a MySQL

    // ==========================================
    // 1. LISTAR INVENTARIO (GET) - CORREGIDO
    // ==========================================
    @GetMapping
    public ResponseEntity<List<Farmacia>> listarMedicamentos() {
        try {
            List<Farmacia> inventario = farmaciaRepository.findAll();
            // Retorna la lista real. Si está vacía, Spring envía [] automáticamente (evita el error de JSON)
            return ResponseEntity.ok(inventario); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 2. REGISTRAR NUEVO LOTE (POST)
    // ==========================================
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Farmacia nuevoMedicamento) {
        try {
            // LÓGICA DE NEGOCIO: Calcular el IGV (18%) automáticamente antes de guardar
            if (nuevoMedicamento.getPrecioUnitario() != null) {
                double igvCalculado = nuevoMedicamento.getPrecioUnitario() * 0.18;
                // Redondear a 2 decimales para dinero
                nuevoMedicamento.setIgv(Math.round(igvCalculado * 100.0) / 100.0); 
            }
            
            Farmacia guardado = farmaciaRepository.save(nuevoMedicamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("{\"mensaje\": \"Error al guardar en MySQL.\"}");
        }
    }

    // ==========================================
    // 3. EDITAR PRECIO (PUT)
    // ==========================================
    @PutMapping("/{id}/precio")
    public ResponseEntity<?> cambiarPrecio(@PathVariable Integer id, @RequestParam Double nuevoPrecio) {
        try {
            Optional<Farmacia> medOpt = farmaciaRepository.findById(id);
            if (medOpt.isPresent()) {
                Farmacia med = medOpt.get();
                
                // Actualizamos el precio
                med.setPrecioUnitario(nuevoPrecio);
                
                // LÓGICA ESTRICTA: Si cambia el precio, el IGV DEBE cambiar también
                double nuevoIgv = nuevoPrecio * 0.18;
                med.setIgv(Math.round(nuevoIgv * 100.0) / 100.0);
                
                farmaciaRepository.save(med); // Guarda ambos cambios a la vez
                return ResponseEntity.ok().body("{\"mensaje\": \"Precio e IGV actualizados.\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // 4. DISMINUIR STOCK POR VENTA (PUT)
    // ==========================================
    @PutMapping("/{id}/vender")
    public ResponseEntity<?> descontarStock(@PathVariable Integer id, @RequestParam Integer cantidad) {
        try {
            Optional<Farmacia> medOpt = farmaciaRepository.findById(id);
            if (medOpt.isPresent()) {
                Farmacia med = medOpt.get();
                int stockActual = med.getCantidadDisponible();
                
                // Lógica de protección: No vender si piden más de lo que hay
                if (cantidad > stockActual) {
                    return ResponseEntity.badRequest().body("{\"mensaje\": \"Stock insuficiente.\"}");
                }
                
                // Resta el stock y guarda
                med.setCantidadDisponible(stockActual - cantidad);
                farmaciaRepository.save(med);
                
                return ResponseEntity.ok().body("{\"mensaje\": \"Venta procesada con éxito.\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // 5. ELIMINAR REGISTRO (DELETE)
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            if (!farmaciaRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            farmaciaRepository.deleteById(id);
            return ResponseEntity.ok().body("{\"mensaje\": \"Medicamento eliminado correctamente.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}