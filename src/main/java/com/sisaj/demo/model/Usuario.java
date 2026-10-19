package com.sisaj.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_usuario")
    private Integer codigoUsuario;

    private String alias;
    private String dni;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    private String nombres;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento; 

    private Double sueldo;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER) 
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private RolUsuario rol;

    // SOLUCIÓN TRUCO JPA: Mapeamos el ID directamente desde la columna física de la BD
    // insertable=false y updatable=false evitan conflictos con la relación @ManyToOne
    @Column(name = "id_rol", insertable = false, updatable = false)
    private Integer idRol;

    @Column(name = "contraseña") 
    private String contrasena;

    private Boolean estado;

    @Column(name = "primer_ingreso")
    private Integer primerIngreso;

    // REPARACIÓN COMPLETADA: Ya no llamamos a 'this.rol.getIdRol()'
    // Retornamos directamente el campo idRol que JPA llena automáticamente desde la BD
    public Integer getIdRol() {
        return this.idRol;
    }

    public Integer getCodigoUsuario() {
        return this.codigoUsuario;
    }
}