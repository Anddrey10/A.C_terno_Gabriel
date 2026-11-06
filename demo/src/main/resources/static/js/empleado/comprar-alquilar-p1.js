/* Paso 1 */
(() => {
  class Paso1 {
    constructor(sesion, cabecera, state) {
      this.sesion = sesion;
      this.state = state;
    }

    async iniciar() {
      const { sesion, state } = this;
      const read = () => sesion.leer();
      const save = (d) => sesion.guardar(d);
      const operacion = () => sesion.operacion();

      const moneyCorto = (n) =>
        "S/ " +
        Number(n).toLocaleString("es-PE", {
          maximumFractionDigits: 2,
        });

      const inputDni = document.querySelector('input[name="dni"]');

      const btnBuscar = document.querySelector(
        ".search-input-group button",
      );

      const fichaCliente = document.querySelector(
        ".client-result-card",
      );

      const grid = document.querySelector(".catalog-grid-cards");

      let condicion = "all";
      let prendas = [];

      const TEXTOS = {
        color: "Todos los colores",
        categoria: "Todas las edades",
        estilo: "Todos los estilos",
        talla: "Todas las tallas",
      };

      const selects = [
        ...document.querySelectorAll(
          ".advanced-filters-bar [data-filter]",
        ),
      ];

      const checks = [
        ...document.querySelectorAll('input[name="prendas_ind"]'),
      ];

      const NOMBRE_COMP = {
        saco: "Saco",
        pantalon: "Pantalón",
        chaleco: "Chaleco",
        corbata: "Corbata / Michi",
        camisa: "Camisa",
      };

      selects.forEach((s) =>
        TG.llenarSelect(
          s,
          TG.OPCIONES[s.dataset.filter],
          TEXTOS[s.dataset.filter],
        ),
      );

      checks.forEach((c) => {
        c.checked = false;
      });

      /* Dibuja la ficha del cliente */
      const pintarCliente = (c) => {
        if (!fichaCliente) return;

        if (!c) {
          fichaCliente.innerHTML =
            '<div class="client-detail-col">' +
            "<span>Cliente</span>" +
            "<strong>Escribe el DNI y pulsa Buscar Cliente.</strong>" +
            "</div>";

          return;
        }

        const puntuacion = Math.max(
          0,
          Math.min(5, Math.round(Number(c.calificacion) || 0)),
        );

        const estrellas =
          '<span class="stars-filled">' +
          "★".repeat(puntuacion) +
          '</span><span class="stars-empty">' +
          "☆".repeat(5 - puntuacion) +
          "</span>";

        fichaCliente.innerHTML =
          '<div class="client-avatar-info">' +
          '<div class="avatar-circle">' +
          TG.esc(TG.iniciales(TG.nombre(c))) +
          "</div>" +
          "<div><strong>" +
          TG.esc(TG.nombre(c)) +
          "</strong></div>" +
          "</div>" +
          '<div class="client-detail-col">' +
          "<span>DNI</span><strong>" +
          TG.esc(c.dni) +
          "</strong></div>" +
          '<div class="client-detail-col">' +
          "<span>Calificación</span>" +
          '<strong class="client-stars">' +
          (puntuacion
            ? estrellas
            : '<span class="stars-empty">☆☆☆☆☆</span>') +
          "</strong></div>" +
          '<div class="client-detail-col">' +
          "<span>Dirección</span><strong>" +
          TG.esc(
            [c.direccion, c.distrito].filter(Boolean).join(", "),
          ) +
          "</strong></div>" +
          '<div class="client-detail-col">' +
          "<span>Teléfono</span><strong>" +
          TG.esc(TG.telefono(c)) +
          "</strong></div>";
      };

      /* Aviso de cliente no encontrado */
      const noEncontrado = (dni) => {
        if (!fichaCliente) return;

        fichaCliente.innerHTML =
          '<div class="client-detail-col">' +
          "<span>Cliente</span><strong>" +
          "No hay un cliente con DNI " +
          TG.esc(dni) +
          ". Podrás registrarlo en el siguiente paso." +
          "</strong></div>";
      };

      if (inputDni) {
        inputDni.type = "text";
        inputDni.inputMode = "numeric";
        inputDni.maxLength = 8;
        inputDni.autocomplete = "off";

        /* Restaura el DNI al volver */
        if (state.dni) inputDni.value = state.dni;

        inputDni.addEventListener("input", () => {
          const dniLimpio = inputDni.value
            .replace(/\D/g, "")
            .slice(0, 8);

          if (inputDni.value !== dniLimpio) {
            inputDni.value = dniLimpio;
          }

          const s = read();

          [
            "clienteId",
            "nombres",
            "apellidos",
            "telefono",
            "direccion",
            "correo",
          ].forEach((k) => delete s[k]);

          sesion.reemplazar(s);
          pintarCliente(null);
        });
      }

      /* Busca el cliente por DNI */
      const buscarCliente = async () => {
        const dni = (inputDni?.value || "").replace(/\D/g, "");

        if (dni.length !== 8) {
          TG.toast("El DNI debe tener 8 dígitos");
          return;
        }

        try {
          const c = (await TG.clientes()).find((x) => x.dni === dni);

          if (!c) {
            save({
              clienteId: null,
              dni,
              nombres: "",
              apellidos: "",
              telefono: "",
              direccion: "",
              correo: "",
            });

            noEncontrado(dni);
            return;
          }

          save({
            clienteId: c.id,
            dni: c.dni,
            nombres: c.nombres,
            apellidos: c.apellidos,
            telefono: c.celular,
            direccion: c.direccion,
            correo: c.correo || "",
          });

          pintarCliente(c);

          if (c.alquilerBloqueado && operacion() === "Alquilar") {
            TG.toast("Este cliente tiene el alquiler bloqueado");
          }
        } catch (err) {
          TG.toast(err.message);
        }
      };

      btnBuscar?.addEventListener("click", buscarCliente);

      inputDni?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          buscarCliente();
        }
      });

      pintarCliente(null);

      /* Restaura la ficha al volver */
      if (state.clienteId) {
        try {
          const cliente = (await TG.clientes()).find(
            (x) => x.id === state.clienteId,
          );

          pintarCliente(cliente || null);
        } catch {
          /* Sin acceso al cliente */
        }
      } else if (state.dni && state.dni.length === 8) {
        noEncontrado(state.dni);
      }

      /* Dibuja el catalogo con filtros */
      const pintarCatalogo = () => {
        if (!grid) return;

        const sel = read().prendaId;

        const f = {
          componentes: checks
            .filter((c) => c.checked)
            .map((c) => NOMBRE_COMP[c.value]),
        };

        selects.forEach((s) => {
          f[s.dataset.filter] = s.value;
        });

        const lista = prendas.filter(
          (p) =>
            (condicion === "all" ||
              p.tipo.toLowerCase() === condicion) &&
            TG.pasaFiltros(p, f),
        );

        grid.innerHTML = lista.length
          ? lista
              .map(
                (p) =>
                  '<article class="catalog-item-card' +
                  (p.id === sel ? " selected" : "") +
                  '" data-id="' +
                  p.id +
                  '">' +
                  '<div class="catalog-thumb">' +
                  '<img src="' +
                  TG.esc(
                    p.imagen ||
                      (/smoking/i.test(
                        (p.diseno || "") + " " + (p.estilo || ""),
                      )
                        ? "https://images.unsplash.com/photo-1617127365659-c47fa864d8bc?auto=format&fit=crop&w=700&q=85"
                        : "https://images.unsplash.com/photo-1618886614638-80e3c103d31a?auto=format&fit=crop&w=700&q=85"),
                  ) +
                  '" alt="' +
                  TG.esc(p.diseno) +
                  '" loading="lazy"></div>' +
                  '<div class="catalog-info">' +
                  "<strong>" +
                  TG.esc(p.diseno) +
                  "</strong>" +
                  "<span>" +
                  TG.esc(p.codigo) +
                  " · " +
                  TG.esc(p.tipo) +
                  " · Talla " +
                  TG.esc(p.talla) +
                  "</span>" +
                  '<span class="catalog-price">' +
                  moneyCorto(p.precioVenta) +
                  " <small>· Alq. " +
                  moneyCorto(p.precioAlquiler) +
                  "</small></span></div>" +
                  (p.id === sel
                    ? '<div class="check-badge">✓</div>'
                    : "") +
                  "</article>",
              )
              .join("")
          : '<p style="padding:16px;color:#667085;">' +
            "No hay prendas disponibles con ese filtro." +
            "</p>";
      };

      try {
        prendas = (await TG.prendas()).filter(
          (p) => p.estado === "Disponible" && !p.suspendida,
        );

        if (
          state.prendaId &&
          !prendas.some((p) => p.id === state.prendaId)
        ) {
          save({ prendaId: null, prenda: "" });
        }

        pintarCatalogo();
      } catch (err) {
        if (grid) {
          grid.innerHTML =
            '<p style="padding:16px;color:#b42318;">' +
            TG.esc(err.message) +
            "</p>";
        }
      }

      /* Seleccion de prenda */
      grid?.addEventListener("click", (e) => {
        const card = e.target.closest(".catalog-item-card");

        if (!card) return;

        const p = prendas.find(
          (x) => x.id === Number(card.dataset.id),
        );

        if (!p) return;

        save({
          prendaId: p.id,
          prenda: TG.descripcionPrenda(p),
          prendaDiseno: p.diseno,
          codigoPrenda: p.codigo,
          precioVenta: p.precioVenta,
          precioAlquiler: p.precioAlquiler,
        });

        pintarCatalogo();
      });

      document.querySelectorAll(".condition-btn").forEach((b) =>
        b.addEventListener("click", () => {
          condicion = b.dataset.condition;
          pintarCatalogo();
        }),
      );

      selects.forEach((s) =>
        s.addEventListener("change", pintarCatalogo),
      );

      checks.forEach((c) =>
        c.addEventListener("change", pintarCatalogo),
      );

      /* Exige prenda para pasar al paso 2 */
      document
        .querySelector('a[href$="paso-2"]')
        ?.addEventListener("click", (e) => {
          if (!read().prendaId) {
            e.preventDefault();
            alert("Selecciona una prenda del catálogo para continuar.");
          }
        });
    }
  }

  CA.arrancar(1, Paso1);
})();