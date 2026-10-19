document.addEventListener("DOMContentLoaded", function() {
    const rolActual = localStorage.getItem('sisaj_rol'); // Lee el rol de la sesión
    
    // 🔍 LÍNEA DE DIAGNÓSTICO: Veremos en la consola qué valor exacto llega
    console.log("[SISAJ DEBUG] El rol detectado en esta vista es:", rolActual);

    if (!rolActual) {
        console.warn("[SISAJ DEBUG] ¡Alerta! No se encontró ningún rol en el localStorage.");
        return;
    }

    // Convertimos a String y limpiamos espacios por seguridad lógica
    const rolClean = String(rolActual).trim().toUpperCase();

    // Si el rol no es ADMIN (Id: 1 o texto "ADMIN")
    if (rolClean !== "1" && rolClean !== "ADMIN") {
        console.log("[SISAJ DEBUG] Aplicando restricciones de navegación para no-admin...");
        
        // Los no-admin jamás deben ver gestión de usuarios
        document.getElementById('menu-usuarios')?.classList.add('d-none');
        document.getElementById('menu-crear')?.classList.add('d-none');
        
        // Filtros específicos por cada rol médico/asistencial
        if (rolClean === "2" || rolClean === "MEDICO") {
            console.log("[SISAJ DEBUG] Ocultando Farmacia y Pagos para Médico.");
            document.getElementById('menu-farmacia')?.classList.add('d-none');
            document.getElementById('menu-pagos')?.classList.add('d-none');
        } 
        else if (rolClean === "3" || rolClean === "FARMACIA") {
            console.log("[SISAJ DEBUG] Ocultando Médico y Pagos para Farmacia.");
            document.getElementById('menu-medico')?.classList.add('d-none');
            document.getElementById('menu-pagos')?.classList.add('d-none');
        } 
        else if (rolClean === "4" || rolClean === "REGISTRADOR_CAJA") {
            console.log("[SISAJ DEBUG] Ocultando Médico y Farmacia para Caja.");
            document.getElementById('menu-medico')?.classList.add('d-none');
            document.getElementById('menu-farmacia')?.classList.add('d-none');
        }
    } else {
        console.log("[SISAJ DEBUG] Acceso total concedido: Usuario es ADMINISTRADOR.");
    }
});