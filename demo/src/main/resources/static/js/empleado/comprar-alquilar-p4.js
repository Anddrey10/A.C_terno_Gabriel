/* Paso 4 */
(() => {
  class Paso4 {
    constructor(sesion) {
      this.sesion = sesion;
    }

    iniciar() {
      const { sesion } = this;
      const read = () => sesion.leer();
      const save = (d) => sesion.guardar(d);
      const money = TG.money;

      const metodosBtns = document.querySelectorAll(".metodo-btn");

      const campoTercero = document.getElementById(
        "campoDinamicoTercero",
      );

      const pagoGrid = document.getElementById("pagoGridContainer");
      const inputEntregado = document.getElementById("pagoEntregado");
      const totalInput = document.getElementById("pagoTotalCobrar");
      const inputTercero = document.getElementById("inputTercerCampo");
      const labelTercero = document.getElementById("labelTercerCampo");

      if (!totalInput) return;

      const total = sesion.precioFinal();

      save({ total });

      totalInput.value = money(total);

      const titulo = document.querySelector(".section-title h2");

      if (titulo && sesion.operacion() === "Comprar") {
        titulo.textContent = "Pago de la compra";
      }

      const subtitulo = document.querySelector(".finanzas-header h3");

      if (subtitulo && sesion.operacion() === "Comprar") {
        subtitulo.textContent = "Método y pago de compra";
      }

      const entregadoPrevio = read().entregado;

      inputEntregado.value =
        entregadoPrevio !== undefined && entregadoPrevio !== ""
          ? Number(entregadoPrevio).toFixed(2)
          : "0.00";

      /* Calcula el vuelto y guarda el pago */
      const recalcular = () => {
        const entregado = Number(inputEntregado.value) || 0;
        const vuelto = Math.max(0, entregado - total);

        if (inputTercero) {
          inputTercero.value = money(vuelto);
        }

        save({
          total,
          entregado: inputEntregado.value,
          vuelto,
        });
      };

      inputEntregado.addEventListener("focus", function () {
        this.select();
      });

      inputEntregado.addEventListener("input", recalcular);

      inputEntregado.addEventListener("blur", function () {
        const v = Number(this.value);

        this.value = isNaN(v) ? "0.00" : v.toFixed(2);

        recalcular();
      });

      /* Cambia el metodo de pago */
      const elegirMetodo = (m) => {
        metodosBtns.forEach((b) =>
          b.classList.toggle("active", b.dataset.metodo === m),
        );

        if (m === "Efectivo") {
          pagoGrid?.classList.remove("transferencia-layout");

          if (campoTercero) {
            campoTercero.style.display = "block";
          }

          if (labelTercero) {
            labelTercero.textContent = "Vuelto automático";
          }
        } else {
          pagoGrid?.classList.add("transferencia-layout");

          if (campoTercero) {
            campoTercero.style.display = "none";
          }

          inputEntregado.value = total.toFixed(2);
        }

        /* Yape: QR + número · Transferencia: cuenta y CCI (datos guardados por el administrador) */
        TG.mostrarDatosPago(m);

        save({ metodo: m });
        recalcular();
      };

      metodosBtns.forEach((b) =>
        b.addEventListener("click", () =>
          elegirMetodo(b.dataset.metodo),
        ),
      );

      elegirMetodo(read().metodo || "Efectivo");

      TG.vincular(inputEntregado, {
        ok: (v) => Number(v) >= total,
        msg:
          "Monto inválido: no cubre el total a cobrar (" +
          money(total) +
          ")",
      });

      /* Valida el monto antes del paso 5 */
      document
        .querySelector('a[href$="paso-5"]')
        ?.addEventListener("click", (e) => {
          if (!TG.validarTodo([inputEntregado])) {
            e.preventDefault();
          }
        });
    }
  }

  CA.arrancar(4, Paso4);
})();