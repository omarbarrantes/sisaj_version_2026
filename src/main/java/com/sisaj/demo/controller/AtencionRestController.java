package com.sisaj.demo.controller;

import com.sisaj.demo.model.AtencionMedico;
import com.sisaj.demo.repository.AtencionMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/atenciones")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AtencionRestController {

    @Autowired
    private AtencionMedicoRepository atencionMedicoRepository;

    @GetMapping
    public ResponseEntity<?> listarAtenciones(
            @RequestHeader(value = "Rol-Usuario", required = false) String rolRecibido,
            @RequestHeader(value = "Usuario-Id", required = false) String idRecibido, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String buscar,
            HttpServletRequest httpRequest) {
        
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Sesión inválida o expirada. Debe loguearse de nuevo.\"}");
        }

        if (rolRecibido == null || rolRecibido.equals("null") || rolRecibido.equals("undefined")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"mensaje\": \"Sin credenciales\"}");
        }

        try {
            int idRol = Integer.parseInt(rolRecibido);
            Pageable paginacion = PageRequest.of(page, size);
            Page<AtencionMedico> paginaAtenciones;
            
            boolean tieneFiltroTexto = (buscar != null && !buscar.trim().isEmpty());

            // REGLA 1: SI ES ADMIN (1) O REGISTRADOR_CAJA (4) -> TOTAL ACCESO
            if (idRol == 1 || idRol == 4) {
                if (tieneFiltroTexto) {
                    paginaAtenciones = atencionMedicoRepository.buscarAtencionesGlobal(buscar.trim(), paginacion);
                } else {
                    paginaAtenciones = atencionMedicoRepository.findAll(paginacion);
                }
            } 
            // REGLA 2: SI ES MEDICO (2) -> COMPORTAMIENTO SEGMENTADO
            else if (idRol == 2) {
                if (idRecibido == null || idRecibido.equals("null") || idRecibido.equals("undefined")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"mensaje\": \"ID de médico ausente en la cabecera.\"}");
                }
                
                Integer idMedicoLogueado = Integer.parseInt(idRecibido);
                
                if (tieneFiltroTexto) {
                    paginaAtenciones = atencionMedicoRepository.buscarAtencionesPorMedico(idMedicoLogueado, buscar.trim(), paginacion);
                } else {
                    paginaAtenciones = atencionMedicoRepository.findByIdMedico(idMedicoLogueado, paginacion);
                }
            } 
            else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"mensaje\": \"No tienes permisos para ver esta sección.\"}");
            }
            
            return ResponseEntity.ok(paginaAtenciones);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"mensaje\": \"Formato de rol inválido\"}");
        }
    }
}