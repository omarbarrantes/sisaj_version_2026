package com.sisaj.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "atencion_medico")
@Data // <--- Genera automáticamente getters, setters y toString para evitar errores con Jackson
public class AtencionMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atencion")
    private Integer idAtencion;

    @Column(name = "id_medico")
    private Integer idMedico; // Almacena el ID numérico del médico de tu BD

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id_paciente")
    private Paciente paciente;

    @Column(name = "fecha_atencion")
    private LocalDate fechaAtencion;

    @Column(name = "hora_atencion")
    private LocalTime horaAtencion;

    // NUEVO: Mapeo del estado que se visualiza en tu interfaz gráfica
    @Column(name = "estado")
    private String estado; // Guardará "Completado" o "Pendiente" en tu MySQL

    @Column(name = "registrado_por")
    private Integer registradoPor;
}