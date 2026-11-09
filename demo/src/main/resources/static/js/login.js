document.addEventListener("DOMContentLoaded", () => {
  const usuario = document.getElementById("email"),
    password = document.getElementById("password"),
    error = document.getElementById("loginError");
  TG.ojo(password);
  document.querySelectorAll("[data-demo]").forEach((b) =>
    b.addEventListener("click", () => {
      if (b.dataset.demo === "empleado") {
        usuario.value = "empleado";
        password.value = "1234";
      } else {
        usuario.value = "admin";
        password.value = "admin";
      }
      error.hidden = true;
    }),
  );
  document.getElementById("forgotPassword")?.addEventListener("click", () => {
    error.textContent =
      "Solicita al administrador el restablecimiento de tu contraseña.";
    error.hidden = false;
  });

  const loginForm = document.getElementById("loginForm");

  const entrar = async (usr, clave) => {
    const u = await TG.post("/api/login", { usuario: usr, contrasena: clave });
    TG.cerrarSesion();
    TG.iniciarSesion({ nombre: u.nombre, rol: u.rol });
    location.href =
      u.rol === "Administrador" ? "/admin/inicio" : "/empleado/inicio";
  };

  loginForm?.addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      /* Valida contra la tabla empleado: solo entran usuarios Activos y se respeta su rol */
      await entrar(usuario.value.trim(), password.value);
    } catch (err) {
      error.textContent = err.message;
      error.hidden = false;
    }
  });
});
