package com.sisaj.demo.model;

import java.sql.Date;
import java.text.SimpleDateFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Table(name = "farmacia") // Ajusta al nombre real de tu tabla
@Data
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_farmacia")
    private Integer idFarmacia;

    @Column(name = "nombre_medicamento")
    private String nombreMedicamento;

    private String presentacion;

    // Reemplaza tu validación manual del setter
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa.")
    @Column(name = "cantidad_disponible")
    private Integer cantidadDisponible;

    // Reemplaza tu validación manual del setter
    @Positive(message = "El precio unitario debe ser mayor que cero.")
    @Column(name = "precio_unitario")
    private Double precioUnitario;

    private Double igv;

    @Column(name = "fecha_ingreso")
    private Date fechaIngreso;

    @Column(name = "fecha_vencimiento")
    private Date fechaVencimiento;

    // ¡Puedes conservar tus métodos personalizados!
    // Lombok generará los getters y setters normales por debajo, 
    // pero respetará y no sobreescribirá estos métodos que escribas tú.
    public String getFormattedFechaIngreso() {
        return (fechaIngreso != null) ? new SimpleDateFormat("yyyy-MM-dd").format(fechaIngreso) : "";
    }

    public String getFormattedFechaVencimiento() {
        return (fechaVencimiento != null) ? new SimpleDateFormat("yyyy-MM-dd").format(fechaVencimiento) : "";
    }
}