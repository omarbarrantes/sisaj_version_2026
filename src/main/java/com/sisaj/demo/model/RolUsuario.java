package com.sisaj.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "rol_usuario") // Ajusta al nombre real de tu tabla
public class RolUsuario {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol; // <--- El nombre de esta variable define el nombre del getter (idRol -> getIdRol)

    @Column(name = "nombre_rol")
    private String nombreRol;
    
    // NADA MÁS. Fíjate que aquí no hay @ManyToOne.
}