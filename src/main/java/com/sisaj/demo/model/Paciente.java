package com.sisaj.demo.model;

import jakarta.persistence.*;
import lombok.Data;



@Entity
@Table(name = "paciente") // 👈 ASEGÚRATE DE QUE DICE "paciente" Y NO "tb_paciente"
@Data
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer idPaciente;

    private String dni;
    private String nombres;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;
    
    // ... el resto de tus atributos locales del paciente
}