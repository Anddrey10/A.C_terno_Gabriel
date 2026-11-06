/* Paso 5 */
(() => {
  class Paso5 {
    constructor(sesion) {
      this.sesion = sesion;
    }

    async iniciar() {
      const { sesion } = this;
      const read = () => sesion.leer();
      const save = (d) => sesion.guardar(d);
      const money = TG.money;

      const esAlquiler = sesion.operacion() === "Alquilar";
      const u = TG.usuario();

      let boleta = null;
      let config = {};

      /* Emite la boleta o reutiliza la existente */
      try {
        config = await TG.configuracion();

        let s = read();

        if (s.boletaId) {
          boleta =
            (await TG.boletas()).find((b) => b.id === s.boletaId) ||
            null;
        }

        if (!boleta) {
          if (!s.clienteId) {
            const nuevo = await TG.post("/api/clientes/nuevo", {
              dni: s.dni,
              nombres: (s.nombres || "").trim(),
              apellidos: (s.apellidos || "").trim(),
              celular: s.telefono,
              direccion: (s.direccion || "").trim(),
              correo: (s.correo || "").trim(),
            });

            save({ clienteId: nuevo.id });
            s = read();
          }

          boleta = await TG.post("/api/boletas/nuevo", {
            tipo: esAlquiler ? "Alquiler" : "Venta",
            cliente: { id: s.clienteId },
            prenda: { id: s.prendaId },
            fechaDevolucion: esAlquiler ? s.fechaDevolucion : null,
            monto: sesion.precioFinal(),
            empleado: u ? u.nombre : "",
            metodoPago: s.metodo || "Efectivo",
          });

          save({ boletaId: boleta.id });

          TG.toast("Boleta " + boleta.numero + " registrada");
        }
      } catch (err) {
        const tarjeta = document.querySelector(".flow-card");

        tarjeta.innerHTML =
          '<div style="padding:24px;">' +
          '<h2 style="margin:0 0 8px;">No se pudo emitir la boleta</h2>' +
          '<p style="color:#b42318;">' +
          TG.esc(err.message) +
          "</p>" +
          '<a class="btn secondary" href="/empleado/comprar-alquilar" ' +
          'style="display:inline-block;margin-top:12px;">' +
          "Volver al inicio del proceso" +
          "</a></div>";

        return;
      }

      const s = read();
      const total = Number(boleta.monto);
      const entregado = Number(s.entregado || total);
      const vuelto = Math.max(0, entregado - total);

      const metodo = boleta.metodoPago || s.metodo || "Efectivo";

      const fechaPactada = boleta.fechaDevolucion
        ? TG.fecha(boleta.fechaDevolucion) + " · 6:00 p. m."
        : "—";

      /* Valores que se muestran en la boleta */
      const valores = {
        fechaHora: TG.fechaHora(),
        fechaCabecera: TG.fechaCabecera(),
        tipoBoleta: esAlquiler
          ? "BOLETA DE ALQUILER"
          : "BOLETA DE COMPRA",
        operacion: esAlquiler ? "Alquiler" : "Compra",
        metodo,
        prenda: TG.descripcionPrenda(boleta.prenda),
        codigoPrenda: boleta.prenda.codigo,
        clienteNombre: TG.nombre(boleta.cliente),
        clienteDni: boleta.cliente.dni,
        empleado: boleta.empleado || (u ? u.nombre : ""),
        devolucion: fechaPactada,
        total: money(total),
        entregado: money(entregado),
        vuelto: money(vuelto),
        letras: TG.montoLetras(total),
      };

      document.querySelectorAll("[data-receipt]").forEach((el) => {
        if (valores[el.dataset.receipt] !== undefined) {
          el.textContent = valores[el.dataset.receipt];
        }
      });

      const num = document.querySelector("[data-receipt-numero]");

      if (num) {
        num.textContent = "N.° " + boleta.numero;
      }

      if (!esAlquiler) {
        const a = document.getElementById("rowDevolucion");
        const b = document.getElementById("cajaPenalizaciones");

        if (a) a.style.display = "none";
        if (b) b.style.display = "none";
      }

      const efectivo = metodo === "Efectivo";
      const cv = document.getElementById("containerVuelto");

      if (cv) {
        cv.style.display = efectivo ? "" : "none";
      }

      const lr = document.querySelector("[data-label-recibido]");

      if (lr) {
        lr.textContent = efectivo
          ? "Efectivo recibido"
          : "Monto pagado";
      }

      const pen = document.querySelector("#cajaPenalizaciones p");

      if (
        pen &&
        config.penalidadDemora &&
        config.penalidadMalEstado
      ) {
        pen.textContent =
          "Demora: " +
          money(config.penalidadDemora.valor) +
          " por día de retraso · Mal estado: " +
          money(config.penalidadMalEstado.valor) +
          " por manchas, roturas o daños.";
      }

      document
        .querySelectorAll("[data-print-receipt]")
        .forEach((btn) =>
          btn.addEventListener("click", () => window.print()),
        );

      /* Pide calificar una sola vez por boleta */
      if (read().calificadoBoleta !== boleta.id) {
        await TG.calificarObligatorio(boleta.cliente);
        save({ calificadoBoleta: boleta.id });
      }

      const finalizado = read();

      sesion.reemplazar({
        boletaId: boleta.id,
        operacion: finalizado.operacion || sesion.operacion(),
        calificadoBoleta: boleta.id,
      });
    }
  }

  CA.arrancar(5, Paso5);
})();