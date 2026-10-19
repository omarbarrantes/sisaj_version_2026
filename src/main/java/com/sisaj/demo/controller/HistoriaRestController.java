package com.sisaj.demo.controller;

import com.sisaj.demo.model.Historia;
import com.sisaj.demo.model.AtencionMedico;
import com.sisaj.demo.repository.HistoriaRepository;
import com.sisaj.demo.repository.AtencionMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/historias")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HistoriaRestController {

    @Autowired
    private HistoriaRepository historiaRepository;

    @Autowired
    private AtencionMedicoRepository atencionMedicoRepository;

    // ========================================================
    // 1. LECTURA: Buscar historial por DNI (Consume tu buscador)
    // ========================================================
    @GetMapping("/paciente/{dni}")
    public ResponseEntity<?> listarHistorialPorDni(@PathVariable String dni, HttpServletRequest request) {
        // Guardián de seguridad del servidor
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Sesión inválida o expirada.\"}");
        }

        List<Historia> historial = historiaRepository.buscarHistorialPorDniPaciente(dni);
        return ResponseEntity.ok(historial);
    }

    // ========================================================
    // 2. ESCRITURA: Registrar nueva historia clínica
    // ========================================================
    @PostMapping
    public ResponseEntity<?> registrarHistoria(@RequestBody Historia nuevaHistoria, HttpServletRequest request) {
        // Guardián de seguridad del servidor
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Sesión inválida o expirada.\"}");
        }

        // Validación estricta: Comprobamos si mandaste el ID de la atención
        if (nuevaHistoria.getAtencionMedico() == null || nuevaHistoria.getAtencionMedico().getIdAtencion() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"mensaje\": \"El ID de la atención médica es obligatorio.\"}");
        }

        // Buscamos la cita médica real en MySQL para evitar quiebres de integridad
        Integer idAtencionInput = nuevaHistoria.getAtencionMedico().getIdAtencion();
        Optional<AtencionMedico> atencionDb = atencionMedicoRepository.findById(idAtencionInput);
        
        if (!atencionDb.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\": \"La atención médica especificada no existe en el sistema.\"}");
        }

        // Enlazamos la entidad completa recuperada de la BD
        nuevaHistoria.setAtencionMedico(atencionDb.get());
        
        // Seteamos la estampa de tiempo de auditoría interna
        nuevaHistoria.setFechaCreacion(LocalDateTime.now());
        
        Historia guardado = historiaRepository.save(nuevaHistoria);
        return ResponseEntity.ok(guardado);
    }
}