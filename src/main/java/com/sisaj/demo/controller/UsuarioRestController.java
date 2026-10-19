package com.sisaj.demo.controller;

import com.sisaj.demo.model.Usuario; 
import com.sisaj.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
// Configuración CORS explícita para evitar bloqueos del navegador al compartir cookies de sesión
@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*", allowCredentials = "true")
public class UsuarioRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ==========================================
    // 1. LISTAR TODOS CON PAGINACIÓN / O LISTAR ROL PARA COMBOS
    // ==========================================
    @GetMapping
    public ResponseEntity<?> listarUsuarios(
            @RequestParam(value = "rol", required = false) Integer rolFiltro, // <-- NUEVO: Captura el ?rol=2 de la caja
            @RequestHeader(value = "Rol-Usuario", required = false) String rolRecibido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String dni,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        
        // Validador universal de sesión viva en el servidor
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("usuario_rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Sesión inválida o expirada. Debe loguearse de nuevo.\"}");
        }

        // 🛠️ SOLUCIÓN PARA LA CARGA DE COMBOS (Evita la exigencia de cabeceras de administración)
        if (rolFiltro != null) {
            // Buscamos de forma segura usando métodos estándar y filtramos con streams los usuarios activos de ese rol
            java.util.List<Usuario> listaFiltrada = usuarioRepository.findAll().stream()
                    .filter(u -> u.getIdRol() != null && u.getIdRol().equals(rolFiltro) && (u.getEstado() == null || u.getEstado()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(listaFiltrada); // Retorna el JSON Array plano que el JS espera recorrer
        }

        // LÓGICA ORIGINAL PARA LA TABLA ADMINISTRATIVA (Requiere cabecera)
        if (rolRecibido == null || rolRecibido.equals("null") || rolRecibido.equals("undefined")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"mensaje\": \"Sin credenciales administrativas\"}");
        }

        try {
            int idRol = Integer.parseInt(rolRecibido);
            boolean tieneAcceso = (idRol == 1 || idRol == 2 || idRol == 3 || idRol == 4);
            
            if (!tieneAcceso) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"mensaje\": \"No tienes permisos para ver esta tabla\"}");
            }
            
            Pageable paginacion = PageRequest.of(page, size);
            Page<Usuario> paginaUsuarios;
            
            if (dni != null && !dni.trim().isEmpty()) {
                paginaUsuarios = usuarioRepository.findByDniContaining(dni, paginacion);
            } else {
                paginaUsuarios = usuarioRepository.findAll(paginacion);
            }
            
            return ResponseEntity.ok(paginaUsuarios);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"mensaje\": \"Formato de rol inválido\"}");
        }
    }

    // ==========================================
    // 2. INICIAR SESIÓN (LOGIN)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario request, HttpServletRequest httpRequest) {
        Optional<Usuario> usuarioDb = usuarioRepository.findByAliasAndContrasena(request.getAlias(), request.getContrasena());
        
        if (usuarioDb.isPresent()) {
            Usuario usuario = usuarioDb.get();
            
            String rolString = (usuario.getRol() != null) ? usuario.getRol().toString() : "";
            
            if (usuario.getEstado() != null && !usuario.getEstado()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"mensaje\": \"Su usuario se encuentra desactivado. Comuníquese con el administrador.\"}");
            }
            
            if (rolString.contains("estado=false") || rolString.contains("activo=false")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"mensaje\": \"El rol asignado a su cuenta está temporalmente desactivado.\"}");
            }

            HttpSession session = httpRequest.getSession(true);
            Integer idRolEncontrado = usuario.getIdRol();
            
            String nombreRolTexto = "Sin Rango";
            if (idRolEncontrado != null) {
                if (idRolEncontrado == 1) nombreRolTexto = "ADMIN";
                else if (idRolEncontrado == 2) nombreRolTexto = "MEDICO";
                else if (idRolEncontrado == 3) nombreRolTexto = "FARMACIA";
                else if (idRolEncontrado == 4) nombreRolTexto = "REGISTRADOR_CAJA";
            }
            
            // Importante: Guardamos el id del usuario en la sesión para la auditoría de ventas
            session.setAttribute("usuario_id", usuario.getCodigoUsuario());
            session.setAttribute("usuario_rol", idRolEncontrado);
            
            String jsonRespuesta = String.format(
                "{\"codigoUsuario\": %d, \"alias\": \"%s\", \"dni\": \"%s\", \"nombres\": \"%s\", \"rol\": {\"idRol\": %d, \"nombreRol\": \"%s\"}}",
                usuario.getCodigoUsuario(),
                usuario.getAlias(),
                usuario.getDni(),
                usuario.getNombres(),
                idRolEncontrado,
                nombreRolTexto
            );
            
            return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonRespuesta);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"mensaje\": \"Credenciales incorrectas\"}");
        }
    }
    
    // ==========================================
    // 3. CAMBIAR ESTADO RÁPIDO (TABLA)
    // ==========================================
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Integer id, @RequestParam Boolean activar) {
        Optional<Usuario> usuarioDb = usuarioRepository.findById(id);
        
        if (usuarioDb.isPresent()) {
            Usuario usuario = usuarioDb.get();
            usuario.setEstado(activar);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("{\"mensaje\": \"Estado actualizado con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\": \"Usuario no encontrado\"}");
        }
    }
    
    // ==========================================
    // 4. OBTENER USUARIO POR ID (PARA EDITAR)
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Integer id) {
        Optional<Usuario> usuarioDb = usuarioRepository.findById(id);
        
        if (usuarioDb.isPresent()) {
            return ResponseEntity.ok(usuarioDb.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\": \"El código de usuario no existe en el sistema.\"}");
        }
    }

    // ==========================================
    // 5. ACTUALIZAR USUARIO COMPLETO (GUARDAR CAMBIOS)
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuarioCompleto(@PathVariable Integer id, @RequestBody Usuario usuarioActualizado) {
        Optional<Usuario> usuarioDb = usuarioRepository.findById(id);
        
        if (usuarioDb.isPresent()) {
            Usuario usuarioExistente = usuarioDb.get();
            
            usuarioExistente.setSueldo(usuarioActualizado.getSueldo());
            usuarioExistente.setEstado(usuarioActualizado.getEstado());
            
            if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().trim().isEmpty()) {
                usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
            }
            
            usuarioRepository.save(usuarioExistente);
            return ResponseEntity.ok("{\"mensaje\": \"Usuario actualizado con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"mensaje\": \"El usuario a editar no existe\"}");
        }
    }
    
    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            // Validaciones lógicas básicas antes de guardar
            if (usuarioRepository.existsByAlias(usuario.getAlias())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"mensaje\": \"El alias ya se encuentra registrado.\"}");
            }
            if (usuarioRepository.existsByDni(usuario.getDni())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"mensaje\": \"El DNI ya pertenece a otro usuario.\"}");
            }

            // Inyección automática para nuevos usuarios de control interno
            usuario.setPrimerIngreso(1); // 1 = Sí es su primer ingreso (para obligar a cambiar clave si deseas)
            
            // Pasar textos obligatorios a Mayúsculas por estándar de base de datos
            usuario.setNombres(usuario.getNombres().toUpperCase());
            usuario.setApellidoPaterno(usuario.getApellidoPaterno().toUpperCase());
            usuario.setApellidoMaterno(usuario.getApellidoMaterno().toUpperCase());

            Usuario nuevoUsuario = usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"mensaje\": \"Error crítico al procesar el registro del usuario.\"}");
        }
    }


    // ==========================================
    // 6. CERRAR SESIÓN (LOGOUT)
    // ==========================================
    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesionBackend(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("{\"mensaje\": \"Sesión finalizada exitosamente en el servidor\"}");
    }
}