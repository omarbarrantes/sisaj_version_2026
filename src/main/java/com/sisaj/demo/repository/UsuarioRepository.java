package com.sisaj.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisaj.demo.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Spring genera: SELECT * FROM tb_usuario WHERE alias = ? AND contraseña = ?
	Optional<Usuario> findByAliasAndContrasena(String alias, String contrasena);

    // Spring genera: SELECT * FROM tb_usuario WHERE dni = ?
    List<Usuario> findByDni(String dni);

    // Spring genera: SELECT * FROM tb_usuario WHERE id_rol = ?
    List<Usuario> findByRolIdRol(Integer idRol);

    // Para la paginación y búsqueda por DNI al mismo tiempo
    Page<Usuario> findByDniContaining(String dni, Pageable pageable);
    
    // Cuenta cuántos usuarios tienen un DNI específico
    long countByDni(String dni);

    boolean existsByAlias(String alias);
    boolean existsByDni(String dni);
}