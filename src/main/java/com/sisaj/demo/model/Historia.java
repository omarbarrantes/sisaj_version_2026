package com.sisaj.demo.model;

import jakarta.persistence.*;
import lombok.Data; // <-- Importación de Lombok
import java.time.LocalDateTime;

@Entity
@Table(name = "historia")
@Data // <-- Esto genera mágicamente los Getters y Setters invisibles en segundo plano
public class Historia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historia")
    private Long idHistoria;

    // El puente uno a uno hacia la atención médica
    @OneToOne
    @JoinColumn(name = "id_atencion", referencedColumnName = "id_atencion")
    private AtencionMedico atencionMedico;

    @Column(name = "diagnostico", length = 500)
    private String diagnostico;

    @Column(name = "tratamiento", length = 500)
    private String tratamiento;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}