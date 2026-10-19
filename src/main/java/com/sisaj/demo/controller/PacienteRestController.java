package com.sisaj.demo.controller;

import com.sisaj.demo.model.Paciente;
import com.sisaj.demo.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
// Mismo CrossOrigin con credenciales activas para no perder las cookies de sesión de la caja
@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*", allowCredentials = "true")
public class PacienteRestController {

    @Autowired
    private PacienteRepository pacienteRepository;

    // ========================================================
    // OBTENER PACIENTE POR DNI (Ruta exacta requerida por la caja)
    // ========================================================
    @GetMapping("/dni/{dni}")
    public ResponseEntity<?> obtenerPacientePorDni(@PathVariable String dni, HttpServletRequest request) {
        
        // Guardián de seguridad: Verifica que el cajero tenga sesión activa antes de buscar
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Acceso denegado. Sesión inválida.\"}");
        }

        Optional<Paciente> pacienteDb = pacienteRepository.findByDni(dni);
        
        if (pacienteDb.isPresent()) {
            // Caso A: El paciente existe, mandamos su JSON plano (200 OK)
            return ResponseEntity.ok(pacienteDb.get());
        } else {
            // Caso B: No existe (404 Not Found) -> Esto activa el modo de escritura manual en tu HTML
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\": \"El DNI ingresado no pertenece a ningún paciente registrado.\"}");
        }
    }
}