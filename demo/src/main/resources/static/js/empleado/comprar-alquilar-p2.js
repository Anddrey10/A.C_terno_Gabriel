/* Paso 2 */
(() => {
  class Paso2 {
    constructor(sesion, cabecera) {
      this.sesion = sesion;
      this.cabecera = cabecera;
    }

    iniciar() {
      const { sesion, cabecera } = this;
      const save = (d) => sesion.guardar(d);
      const operacion = () => sesion.operacion();

      const f = (n) => document.querySelector('[name="' + n + '"]');

      const dni = f("dni");
      const nombres = f("nombres");
      const apellidos = f("apellidos");
      const tel = f("telefono");
      const dir = f("direccion");
      const correo = f("correo");
      const recojo = f("fechaRecojo");
      const devol = f("fechaDevolucion");

      const datos = [nombres, apellidos, tel, dir, correo];

      const hoy = TG.hoyISO();
      let existente = false;

      TG.vincular(dni, "dni");
      TG.vincular(nombres, "nombre");
      TG.vincular(apellidos, "nombre");
      TG.vincular(tel, "celular");
      TG.vincular(dir, "direccion");
      TG.vincular(correo, "correo", { requerido: false });
      TG.vincular(recojo, "fechaHoy");

      TG.vincular(
        devol,
        {
          ok: (v) => v >= hoy && v >= recojo.value,
          msg: "Fecha inválida: revisa la fecha de devolución",
        },
        { requerido: operacion() === "Alquilar" },
      );

      /* Suma un dia a la fecha */
      const sumarUnDiaLocal = (fechaStr) => {
        const [anio, mes, dia] = fechaStr.split("-").map(Number);
        const d = new Date(anio, mes - 1, dia);

        d.setDate(d.getDate() + 1);

        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");

        return `${yyyy}-${mm}-${dd}`;
      };

      if (!recojo.value || recojo.value < hoy) {
        recojo.value = hoy;
        save({ fechaRecojo: hoy });
      }

      recojo.min = hoy;

      if (
        operacion() === "Alquilar" &&
        (!devol.value || devol.value <= recojo.value)
      ) {
        const defDevol = sumarUnDiaLocal(recojo.value);

        devol.value = defDevol;
        save({ fechaDevolucion: defDevol });
      }

      devol.min = recojo.value;

      /* Muestra u oculta la devolucion */
      cabecera.alCambiar = () => {
        const alq = operacion() === "Alquilar";

        devol.closest("label").style.display = alq ? "" : "none";
        devol._tg.requerido = alq;

        if (alq) {
          if (!devol.value || devol.value <= recojo.value) {
            const defDevol = sumarUnDiaLocal(recojo.value);

            devol.value = defDevol;
            save({ fechaDevolucion: defDevol });
          }
        } else {
          devol.value = "";
          devol._tocado = false;
          TG.marcar(devol, null);
          save({ fechaDevolucion: "" });
        }
      };

      cabecera.alCambiar();

      recojo.addEventListener("change", () => {
        recojo.min = hoy;
        devol.min = recojo.value;

        if (operacion() === "Alquilar") {
          const nuevaDef = sumarUnDiaLocal(recojo.value);

          devol.value = nuevaDef;
          save({ fechaDevolucion: nuevaDef });
        }

        if (devol.value) {
          devol._tocado = true;
          TG.validarCampo(devol);
        }
      });

      const bloquear = (b) =>
        datos.forEach((el) => {
          el.readOnly = b;
          el.style.background = b ? "#f2f4f7" : "";
        });

      const guardarDatos = () =>
        save({
          nombres: nombres.value,
          apellidos: apellidos.value,
          telefono: tel.value,
          direccion: dir.value,
          correo: correo.value,
        });

      const vaciar = () => {
        datos.forEach((el) => {
          el.value = "";
          el._tocado = false;
          TG.marcar(el, null);
        });

        guardarDatos();
      };

      /* Busca el DNI y bloquea datos si existe */
      const buscarAuto = async (avisar) => {
        const d = dni.value;

        if (d.length !== 8) {
          save({ clienteId: null });

          if (existente) {
            existente = false;
            bloquear(false);
            vaciar();
          }

          return;
        }

        const c = (await TG.clientes()).find((x) => x.dni === d);

        if (c) {
          existente = true;

          nombres.value = c.nombres;
          apellidos.value = c.apellidos;
          tel.value = c.celular;
          dir.value = c.direccion;
          correo.value = c.correo || "";

          datos.forEach((el) => TG.marcar(el, null));

          bloquear(true);

          save({
            clienteId: c.id,
            dni: c.dni,
          });

          guardarDatos();

          if (c.alquilerBloqueado && operacion() === "Alquilar") {
            TG.toast("Este cliente tiene el alquiler bloqueado");
          }
        } else {
          if (existente) vaciar();

          existente = false;
          bloquear(false);

          save({
            clienteId: null,
            dni: d,
          });

          if (avisar) {
            TG.toast("Cliente nuevo: completa sus datos");
          }
        }
      };

      dni.type = "text";
      dni.inputMode = "numeric";
      dni.maxLength = 8;
      dni.autocomplete = "off";

      dni.addEventListener("input", () => {
        const dniLimpio = dni.value.replace(/\D/g, "").slice(0, 8);

        if (dni.value !== dniLimpio) {
          dni.value = dniLimpio;
        }

        buscarAuto(true).catch((err) => TG.toast(err.message));
      });

      /* Revisa el DNI guardado al volver */
      if (dni.value.length === 8) {
        buscarAuto(false).catch((err) => TG.toast(err.message));
      }
    }
  }

  CA.arrancar(2, Paso2);
})();