package com.sisaj.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifario_venta") // <-- Ajustado al nombre real que usas en tu consulta SQL
@Data
public class TarifarioVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "fecha_emitida")
    private Date fechaEmitida;

    @Column(name = "hora_emitida")
    private Time horaEmitida;

    @Column(name = "id_medico")
    private Integer idMedico;

    @Column(name = "id_paciente")
    private Integer idPaciente;

    private Double subtotal;
    private Double igv;
    private Double total;

    @Column(name = "tipo_pago")
    private String tipoPago;

    @Column(name = "registrado_por")
    private Integer registradoPor;

    // 👇 ¡NUEVOS!: Columnas de auditoría física que aparecieron en tu captura
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultima_modificacion")
    private LocalDateTime LabUltimaModificacion;

    // 👇 CORREGIDOS: Se les quitó el @Transient porque SÍ existen físicamente en tu MySQL
    @Column(name = "alias_registrador")
    private String aliasRegistrador;

    @Column(name = "medico_nombre")
    private String medicoNombre;

    @Column(name = "paciente_nombre")
    private String pacienteNombre;

    // ========================================================
    // --- CAMPOS DE SOLO LECTURA (UI / FRONTEND LAYER) ---
    // ========================================================
    // Estos campos NO existen en tu captura de la tabla, así que SÍ se quedan como @Transient
    @Transient
    private String dniPaciente;
    
    @Transient
    private String apellidoPaterno;
    
    @Transient
    private String apellidoMaterno;
    
    @Transient
    private String nombresPaciente;
    
    @Transient
    private Date fechaNacimientoPaciente;
}