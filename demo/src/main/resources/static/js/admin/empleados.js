document.addEventListener("DOMContentLoaded", () =>
  TG.listo(async () => {
    const tbody = document.querySelector(".data-table tbody");
    const contador = document.getElementById("contadorEmpleados");
    const buscador = document.getElementById("buscadorCodigo");
    const filtroEstado = document.getElementById("filtroEstado");
    const msg = document.getElementById("msgBusqueda");
    const pestanas = [...document.querySelectorAll("[data-pestana]")];
    let empleados = [],
      pestana = "empleados";

    buscador.addEventListener("input", () => {
      buscador.value = TG.reglas.codigoEmpleado.filtro(buscador.value);
      pintarTabla();
    });

    const cargar = async () => {
      empleados = await TG.get("/api/empleados");
      pintarTabla();
    };

    /* En la columna NOMBRE se muestran nombre y apellido juntos (los empleados antiguos pueden no tener apellido) */
    const completo = (e) => ((e.nombre || "") + " " + (e.apellido || "")).trim();
    /* Contraseña oculta que se muestra al editar: si no se toca, la contraseña actual no cambia */
    const CLAVE_OCULTA = "••••••••";

    const badgeRol = (r) =>
      '<span class="badge badge-role ' +
      (r === "Administrador" ? "role-admin" : "role-empleado") +
      '"><span class="dot"></span>' +
      TG.esc(r) +
      "</span>";

    const badgeEstado = (e) =>
      e === "Suspendido"
        ? TG.insigniaEstado("Suspendido")
        : '<span class="badge badge-status ' +
        (e === "Activo" ? "status-activo" : "status-inactivo") +
        '"><span class="dot"></span>' +
        TG.esc(e) +
        "</span>";

    const fila = (e) => {
      const sus = e.estado === "Suspendido";
      const editar =
        '<button class="btn-mini" type="button" data-editar="' +
        e.id +
        '">✎ Editar</button>';
      const acciones = sus
        ? '<div class="acciones-fila">' +
        editar +
        '<button class="btn-mini ok" type="button" data-reactivar="' +
        e.id +
        '">↺ Restablecer</button></div>'
        : '<div class="acciones-fila">' +
        editar +
        '<button class="btn-mini danger" type="button" data-suspender="' +
        e.id +
        '">⏸ Suspender</button>' +
        '<button class="btn-mini" type="button" data-clave="' +
        e.id +
        '">🔑 Restablecer contraseña</button></div>';
      return (
        '<tr data-id="' +
        e.id +
        '"><td class="col-name"><strong>' +
        TG.esc(e.codigo || "—") +
        '</strong></td><td class="col-name">' +
        TG.esc(completo(e)) +
        "</td><td>" +
        TG.esc(e.correo || "—") +
        "</td><td>" +
        TG.esc(
          e.telefono
            ? e.telefono.replace(/(\d{3})(\d{3})(\d{3})/, "$1 $2 $3")
            : "—",
        ) +
        "</td><td>" +
        TG.esc(e.area || "—") +
        "</td><td>" +
        badgeRol(e.rol) +
        "</td><td>" +
        badgeEstado(e.estado) +
        '</td><td class="col-date">' +
        TG.fecha(e.desde) +
        "</td><td>" +
        acciones +
        "</td></tr>"
      );
    };

    function pintarTabla() {
      const sus = pestana === "suspendidos";
      const grupo = empleados.filter(
        (e) => (e.estado === "Suspendido") === sus,
      );
      contador.textContent = sus
        ? grupo.length + " suspendidos"
        : grupo.length +
        (grupo.length === 1 ? " empleado" : " empleados") +
        " con acceso al sistema";
      filtroEstado.style.display = sus ? "none" : "";

      let lista = grupo,
        aviso = "";
      const q = buscador.value;
      if (q.length === 4 && TG.reglas.codigoEmpleado.ok(q)) {
        const encontrado = empleados.find((x) => x.codigo === q);
        if (!encontrado) aviso = "Código no existe";
        else if (!sus && encontrado.estado === "Suspendido")
          aviso = "El empleado se encuentra suspendido";
        else if (sus && encontrado.estado !== "Suspendido")
          aviso = "El empleado no está suspendido";
        lista = aviso ? [] : [encontrado];
      } else if (!sus && filtroEstado.value !== "Todos") {
        lista = grupo.filter((x) => x.estado === filtroEstado.value);
      }
      msg.style.display = aviso ? "block" : "none";
      msg.textContent = aviso;
      tbody.innerHTML = lista.length
        ? lista
          .slice()
          .sort((a, b) => (a.codigo || "").localeCompare(b.codigo || ""))
          .map(fila)
          .join("")
        : '<tr><td colspan="9" class="aviso-vacio">' +
        (aviso
          ? ""
          : sus
            ? "No hay empleados suspendidos."
            : "No hay empleados para mostrar.") +
        "</td></tr>";
    }

    pestanas.forEach((b) =>
      b.addEventListener("click", () => {
        pestanas.forEach((x) => x.classList.toggle("active", x === b));
        pestana = b.dataset.pestana;
        buscador.value = "";
        pintarTabla();
      }),
    );
    filtroEstado.addEventListener("change", pintarTabla);

    tbody.addEventListener("click", async (e) => {
      const bEd = e.target.closest("[data-editar]"),
        bSu = e.target.closest("[data-suspender]"),
        bRe = e.target.closest("[data-reactivar]"),
        bCl = e.target.closest("[data-clave]");
      try {
        if (bEd) {
          abrirEdicion(empleados.find((x) => x.id === Number(bEd.dataset.editar)));
          return;
        }
        if (bSu) {
          const emp = empleados.find(
            (x) => x.id === Number(bSu.dataset.suspender),
          );
          const yo = TG.usuario();
          if (yo && emp.nombre === yo.nombre) {
            TG.toast("No puedes suspender tu propia cuenta");
            return;
          }
          if (
            !confirm(
              "¿Suspender a " +
              completo(emp) +
              "? Ya no podrá ingresar al sistema.",
            )
          )
            return;
          await TG.put("/api/empleados/suspender/" + emp.id);
          TG.toast("Empleado suspendido");
          await cargar();
        }
        if (bRe) {
          await TG.put("/api/empleados/reactivar/" + bRe.dataset.reactivar);
          TG.toast("Empleado restablecido");
          await cargar();
        }
        if (bCl) {
          /* Sin contraseña temporal: se abre directo el formulario para escribir la contraseña nueva */
          const emp = empleados.find((x) => x.id === Number(bCl.dataset.clave));
          TG.modal({
            titulo: "Nueva contraseña",
            subtitulo: "Escribe la contraseña nueva de " + completo(emp) + ".",
            textoOk: "Guardar contraseña",
            campos: [
              {
                name: "contrasena",
                label: "Contraseña nueva",
                type: "password",
                placeholder: "Mínimo 4 caracteres",
                regla: "contrasena",
                autocomplete: false,
              },
            ],
            onOk: async (v) => {
              await TG.put("/api/empleados/contrasena/" + emp.id, {
                contrasena: v.contrasena,
              });
              TG.toast("Contraseña actualizada");
            },
          });
        }
      } catch (err) {
        TG.toast(err.message);
      }
    });

    /* Campos del formulario (los mismos para agregar y para editar). El código, el estado y la fecha de ingreso
       los genera el sistema, por eso no aparecen. */
    const camposEmpleado = (e) => {
      const ed = !!e;
      /* limpiarAlClic: al editar, cada campo conserva su valor hasta que se hace clic en él */
      const base = (c) => (ed ? { ...c, limpiarAlClic: true } : c);
      return [
        base({
          name: "nombre",
          label: "Nombre",
          placeholder: "Ej. Luis Alberto",
          regla: "nombre",
          value: ed ? e.nombre : undefined,
        }),
        base({
          name: "apellido",
          label: "Apellido",
          placeholder: "Ej. Salazar Ortiz",
          regla: "nombre",
          value: ed ? e.apellido || "" : undefined,
        }),
        base({
          name: "usuario",
          label: "Usuario",
          placeholder: "Ej. l.salazar",
          regla: "usuario",
          value: ed ? e.usuario : undefined,
        }),
        base({
          name: "contrasena",
          label: "Contraseña",
          placeholder: "Mínimo 4 caracteres",
          regla: "contrasena",
          type: ed ? "password" : undefined,
          value: ed ? CLAVE_OCULTA : undefined,
        }),
        base({
          name: "correo",
          label: "Correo electrónico",
          placeholder: "Ej. nombre@gmail.com",
          regla: "correo",
          value: ed ? e.correo : undefined,
        }),
        base({
          name: "telefono",
          label: "Teléfono",
          placeholder: "Ej. 987654321",
          regla: "celular",
          value: ed ? e.telefono : undefined,
        }),
        base({
          name: "area",
          label: "Área o puesto de trabajo",
          placeholder: "Ej. Ventas",
          regla: "area",
          value: ed ? e.area : undefined,
        }),
        {
          name: "rol",
          label: "Rol",
          options: ["Empleado", "Administrador"],
          value: ed ? e.rol : "Empleado",
        },
      ];
    };

    function abrirEdicion(emp) {
      if (!emp) return;
      TG.modal({
        titulo: "Editar empleado " + (emp.codigo || ""),
        subtitulo:
          "Haz clic en un campo para escribir un dato nuevo; los que no toques se quedan igual. Todos son obligatorios.",
        textoOk: "Guardar cambios",
        campos: camposEmpleado(emp),
        onOk: async (v) => {
          const datos = {
            nombre: v.nombre,
            apellido: v.apellido,
            usuario: v.usuario,
            correo: v.correo,
            telefono: v.telefono,
            area: v.area,
            rol: v.rol,
          };
          /* Si la contraseña sigue oculta no se envía y se conserva la actual */
          if (v.contrasena !== CLAVE_OCULTA) datos.contrasena = v.contrasena;
          await TG.put("/api/empleados/actualizar/" + emp.id, datos);
          TG.toast("Empleado actualizado");
          await cargar();
        },
      });
    }

    document
      .getElementById("btnAgregarEmpleado")
      .addEventListener("click", () => {
        TG.modal({
          titulo: "Agregar empleado",
          subtitulo: "El código se genera automáticamente.",
          textoOk: "Guardar empleado",
          campos: camposEmpleado(null),
          onOk: async (v) => {
            await TG.post("/api/empleados/nuevo", { ...v, estado: "Activo" });
            TG.toast("Empleado agregado");
            await cargar();
          },
        });
      });

    try {
      await cargar();
    } catch (err) {
      TG.toast(err.message);
    }
  }),
);