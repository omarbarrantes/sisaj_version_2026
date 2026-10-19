document.addEventListener('DOMContentLoaded', function () {

  var btnModo = document.getElementById('modoOscuroBtn');
  var icono = document.getElementById('iconoModo');
  var texto = document.getElementById('textoModo');

  // Aplicar modo guardado
  var modoGuardado = localStorage.getItem('modo');

  if (modoGuardado === 'oscuro') {
    document.body.classList.add('dark-mode');
    if (icono) icono.classList.replace('fa-moon', 'fa-sun');
    if (texto) texto.textContent = 'Modo Claro';
  }

  // Si existe el botón, agregar evento
  if (btnModo) {
    btnModo.addEventListener('click', function () {

      if (document.body.classList.contains('dark-mode')) {
        document.body.classList.remove('dark-mode');
        if (icono) icono.classList.replace('fa-sun', 'fa-moon');
        if (texto) texto.textContent = 'Modo Oscuro';
        localStorage.setItem('modo', 'claro');
      } else {
        document.body.classList.add('dark-mode');
        if (icono) icono.classList.replace('fa-moon', 'fa-sun');
        if (texto) texto.textContent = 'Modo Claro';
        localStorage.setItem('modo', 'oscuro');
      }

    });
  }

});
