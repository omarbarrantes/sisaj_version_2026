package com.sisaj.demo.controller;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisaj.demo.model.TarifarioVenta;
import com.sisaj.demo.repository.TarifarioVentaRepository;
import com.sisaj.demo.repository.PacienteRepository; // 👈 NUEVO: Importación del repositorio de pacientes

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@RestController
@RequestMapping("/api/ventas")
// 🛠️ Ajustado con allowCredentials = true para evitar la pérdida de cookies de sesión entre la interfaz y el backend
@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*", allowCredentials = "true")
public class TarifarioVentaRestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TarifarioVentaRepository tarifarioVentaRepository;

    @Autowired
    private PacienteRepository pacienteRepository; // 👈 NUEVO: Conexión relacional para el auto-registro

    // ========================================================
    // 1. REGISTRAR VENTA / ATENCIÓN (Con soporte de auditoría e hilos nulos)
    // ========================================================
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody TarifarioVenta venta, HttpServletRequest request) {
        // Guardián de seguridad del servidor
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Sesión inválida o expirada. Vuelva a loguearse.\"}");
        }

        try {
            // ========================================================
            // INTERCEPCIÓN COLOZAL: AUTO-REGISTRO DEL PACIENTE SI ES NUEVO (404)
            // ========================================================
            if (venta.getIdPaciente() == null) {
                if (venta.getDniPaciente() == null || venta.getPacienteNombre() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("{\"mensaje\": \"Datos insuficientes para el auto-registro del paciente.\"}");
                }

                // Instanciamos el modelo relacional mapeado a la tabla limpia 'paciente'
                com.sisaj.demo.model.Paciente nuevoPac = new com.sisaj.demo.model.Paciente();
                nuevoPac.setDni(venta.getDniPaciente());
                nuevoPac.setNombres(venta.getPacienteNombre().toUpperCase());
                nuevoPac.setApellidoPaterno(""); // Inicializado en blanco por integridad de campos obligatorios
                nuevoPac.setApellidoMaterno("");

                // Guardamos en MySQL y recuperamos el id_paciente autogenerado
                nuevoPac = pacienteRepository.save(nuevoPac);
                venta.setIdPaciente(nuevoPac.getIdPaciente());
            }

            // Desglose matemático exacto del subtotal e IGV (18%)
            double total = venta.getTotal();
            double subtotal = total / 1.18;
            double igv = total - subtotal;

            venta.setSubtotal(Math.round(subtotal * 100.0) / 100.0);
            venta.setIgv(Math.round(igv * 100.0) / 100.0);
            
            // Inyección automática de tiempos del servidor
            venta.setFechaEmitida(Date.valueOf(LocalDate.now()));
            venta.setHoraEmitida(Time.valueOf(LocalTime.now()));
            
            // ========================================================
            // SOLUCIÓN AL ERROR 500: Llenado físico de campos de auditoría
            // ========================================================
            venta.setFechaRegistro(LocalDateTime.now());
            venta.setLabUltimaModificacion(LocalDateTime.now());

            // Captura del ID del cajero logueado desde la sesión
            Integer idCajero = (Integer) session.getAttribute("usuario_id");
            if (idCajero == null) {
                idCajero = 1;
            }
            venta.setRegistradoPor(idCajero);

            // ========================================================
            // 👇 SOLUCIÓN AL ERROR: "Column 'alias_registrador' cannot be null"
            // Rellenamos estas columnas obsoletas para satisfacer a MySQL.
            // JasperReports las ignorará porque usa el INNER JOIN.
            // ========================================================
            venta.setAliasRegistrador("CAJA");
            if (venta.getMedicoNombre() == null) {
                venta.setMedicoNombre("ASIGNADO");
            }
            if (venta.getPacienteNombre() == null) {
                venta.setPacienteNombre("REGISTRADO");
            }

            // Guardado final libre de nulos obligatorios
            TarifarioVenta ventaGuardada = tarifarioVentaRepository.save(venta);
            return ResponseEntity.status(HttpStatus.CREATED).body(ventaGuardada);

        } catch (Exception e) {
            System.out.println("❌ EXCEPCIÓN DETECTADA EN CONTROLADOR DE CAJA SISAJ:");
            e.printStackTrace(); // Imprime la traza completa en la consola de Eclipse para auditoría interna
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"mensaje\": \"Error crítico al procesar la transacción financiera.\"}");
        }
    }

    // ========================================================
    // 2. IMPRIMIR TICKET EN PDF (JasperReports versión 6.21.5)
    // ========================================================
    @GetMapping("/{idVenta}/pdf")
    public ResponseEntity<byte[]> imprimirTicket(@PathVariable Integer idVenta) {
        try (Connection connection = dataSource.getConnection()) {
            
            // Ruta del archivo compilado en resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/ticket_venta.jrxml");
            if (reportStream == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 1. DECLARACIÓN DEL MAPA (Soluciona el error "cannot be resolved")
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idVenta", idVenta);

            // Carga del logo desde el classpath
            InputStream logoStream = getClass().getResourceAsStream("/static/img/logo.png");
            if (logoStream != null) {
                parametros.put("logoEmpresa", logoStream);
            } else {
                parametros.put("logoEmpresa", null); // Evita caídas si falta el diseño físico del logo
            }

            // Construcción del PDF cruzando plantilla y base de datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            // 2. CABECERAS CORREGIDAS (Declaradas una sola vez, soluciona el "Duplicate local variable")
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ticket_" + idVenta + ".pdf\"");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}