/* Confeccion tickets y boletas */
document.addEventListener("DOMContentLoaded", () =>
  TG.listo(async () => {
    const $ = (id) => document.getElementById(id);
    const u = TG.usuario(),
      hoy = TG.hoyISO();
    const sumarDias = (n) => {
      const d = new Date();
      d.setDate(d.getDate() + n);
      return (
        d.getFullYear() +
        "-" +
        String(d.getMonth() + 1).padStart(2, "0") +
        "-" +
        String(d.getDate()).padStart(2, "0")
      );
    };

    /* Buscador de ordenes */
    const inputBusquedaOrden = $("inputBusquedaOrden");
    /* Solo deja escribir un DNI (8 números) o un código de ticket (T001-0083) */
    TG.filtroBusqueda(inputBusquedaOrden, "T");

    /* Referencias */
    const inpDni = $("inputDni"),
      inpNombres = $("inputNombres"),
      inpApellidos = $("inputApellidos"),
      inpDireccion = $("inputDireccion"),
      inpTelefono = $("inputTelefono"),
      inpCorreo = $("inputCorreo");
    const inpPrueba = $("inputPrimeraPrueba"),
      inpEntrega = $("inputEntregaFinal");
    const inputCostoTotal = $("pagoCostoTotal"),
      labelCostoTotal = $("labelCostoTotal"),
      campoAdelantoContainer = $("campoAdelantoContainer"),
      inputAdelantoFijo = $("pagoAdelantoFijo");
    const inputEntregado = $("pagoEntregado"),
      inputTercerCampo = $("inputTercerCampo"),
      labelTercerCampo = $("labelTercerCampo"),
      pagoGridContainer = $("pagoGridContainer");
    const ticketCodigo = $("ticketCodigo"),
      ticketCliente = $("ticketCliente"),
      ticketDni = $("ticketDni"),
      ticketEntrega = $("ticketEntrega"),
      ticketMetodoPago = $("ticketMetodoPago");
    const ticketTotal = $("ticketTotal"),
      ticketAdelantoPrevio = $("ticketAdelantoPrevio"),
      ticketSaldoPendiente = $("ticketSaldoPendiente"),
      ticketMontoPagado = $("ticketMontoPagado");
    const ticketVuelto = $("ticketVuelto"),
      ticketContenedorVuelto = $("ticketContenedorVuelto");
    const seccionDatosCliente = $("seccionDatosCliente"),
      seccionMedidas = $("seccionMedidasPersonalizacion"),
      seccionPlazos = $("seccionPlazos");
    const seccionModo = $("seccionModoOrden"),
      btnModoPrueba = $("btnModoPrueba"),
      btnModoEntrega = $("btnModoEntrega"),
      panelPrueba = $("panelPrueba"),
      pruebaComentario = $("pruebaComentario"),
      pruebaEntrega = $("pruebaEntrega"),
      pruebaHistorial = $("pruebaHistorial"),
      btnGuardarPrueba = $("btnGuardarPrueba"),
      seccionFinanzas = $("seccionFinanzas"),
      seccionComprobantes = $("seccionComprobantes");
    const lista = $("listaOrdenesProximas"),
      btnConfirmar = $("btnConfirmarPago");
    const datos = [
      inpNombres,
      inpApellidos,
      inpDireccion,
      inpTelefono,
      inpCorreo,
    ];

    const SELECTS = {
      diseno: "pDiseno",
      tela: "pTela",
      saco: "pColorSaco",
      pantalon: "pColorPantalon",
      chaleco: "pColorChaleco",
      solapa: "pSolapa",
      camisa: "pColorCamisa",
      corbata: "pColorCorbata",
    };
    const SALIDAS = {
      diseno: "tDiseno",
      tela: "tTela",
      saco: "tSaco",
      pantalon: "tPantalon",
      chaleco: "tChaleco",
      solapa: "tSolapa",
      camisa: "tCamisa",
      corbata: "tCorbata",
    };
    const MEDIDAS = {
      cuello: ["mCuello", "mOutCuello"],
      talle: ["mTalle", "mOutTalle"],
      pecho: ["mPecho", "mOutPecho"],
      cintura: ["mCintura", "mOutCintura"],
      cadera: ["mCadera", "mOutCadera"],
      manga: ["mManga", "mOutManga"],
      largo: ["mLargo", "mOutLargo"],
      bota: ["mBota", "mOutBota"],
      espalda: ["mEspalda", "mOutEspalda"],
    };

    let orden = null,
      clienteExistente = null,
      terminado = false,
      emitiendo = false,
      ordenes = [];
    const redondear = (n) =>
      Math.round((Number(n) + Number.EPSILON) * 100) / 100;
    const metodoActual = () =>
      document.querySelector(".metodo-btn.active")?.dataset.metodo ||
      "Efectivo";
    const saldoFijo = () =>
      Math.max(
        0,
        redondear(
          orden
            ? orden.costoTotal - orden.adelanto
            : parseFloat(inputCostoTotal.value) || 0,
        ),
      );
    /* Indica si el ticket ya esta pagado */
    const ordenPagada = () => !!orden && saldoFijo() <= 0;

    /* Validaciones */
    TG.vincular(inpDni, "dni");
    TG.vincular(inpNombres, "nombre");
    TG.vincular(inpApellidos, "nombre");
    TG.vincular(inpDireccion, "direccion");
    TG.vincular(inpTelefono, "celular");
    TG.vincular(inpCorreo, "correo");
    inpTelefono.removeAttribute("type");
    inpTelefono.setAttribute("inputmode", "numeric");
    inpDni.setAttribute("inputmode", "numeric");
    TG.vincular(inpPrueba, "fechaHoy");
    TG.vincular(inpEntrega, {
      ok: (v) => v >= hoy && v >= (inpPrueba.value || hoy),
      msg: "Fecha inválida: no puede ser anterior a hoy ni a la primera prueba",
    });
    TG.vincular(inputCostoTotal, "monto");
    const reglaAdelanto = {
      ok: (v) => Number(v) > 0,
      msg: "Monto inválido: debe ser mayor a 0",
    };
    const reglaSaldo = {
      ok: (v) => redondear(v) >= saldoFijo(),
      get msg() {
        return (
          "Monto insuficiente: faltan " +
          TG.money(Math.max(0, saldoFijo() - redondear(inputEntregado.value))) +
          " para cubrir el saldo de " +
          TG.money(saldoFijo())
        );
      },
    };
    TG.vincular(inputEntregado, reglaAdelanto);
    inpPrueba.value = sumarDias(3);
    inpEntrega.value = sumarDias(7);
    inpPrueba.min = hoy;
    inpEntrega.min = hoy;
    inpPrueba.addEventListener("change", () => {
      inpEntrega.min = inpPrueba.value > hoy ? inpPrueba.value : hoy;
      if (inpEntrega.value) {
        inpEntrega._tocado = true;
        TG.validarCampo(inpEntrega);
      }
      ticketEntrega.textContent = TG.fecha(inpEntrega.value);
    });
    ticketEntrega.textContent = TG.fecha(inpEntrega.value);
    $("ticketFecha").textContent = TG.fechaHora();
    $("ticketEmpleado").textContent = u ? u.nombre : "";

    /* Datos del cliente */
    const bloquear = (b) =>
      datos.forEach((el) => {
        el.readOnly = b;
        el.style.background = b ? "#f2f4f7" : "";
      });
    const vaciar = () =>
      datos.forEach((el) => {
        el.value = "";
        el._tocado = false;
        TG.marcar(el, null);
      });
    const buscarCliente = async (avisar) => {
      const d = inpDni.value;
      ticketDni.textContent = d || "---";
      if (d.length !== 8) {
        if (clienteExistente) {
          clienteExistente = null;
          bloquear(false);
          vaciar();
          ticketCliente.textContent = "---";
        }
        return;
      }
      const c = (await TG.clientes()).find((x) => x.dni === d);
      if (c) {
        clienteExistente = c;
        bloquear(true);
        inpNombres.value = c.nombres;
        inpApellidos.value = c.apellidos;
        inpDireccion.value = c.direccion;
        inpTelefono.value = c.celular;
        inpCorreo.value = c.correo || "";
        datos.forEach((el) => TG.marcar(el, null));
        ticketCliente.textContent = TG.nombre(c);
      } else {
        if (clienteExistente) vaciar();
        clienteExistente = null;
        bloquear(false);
        ticketCliente.textContent = "---";
        if (avisar) TG.toast("Cliente nuevo: completa sus datos", "info");
      }
    };
    inpDni.addEventListener("input", () =>
      buscarCliente(true).catch((e) => TG.toast(e.message, "error")),
    );
    [inpNombres, inpApellidos].forEach((el) =>
      el.addEventListener("input", () => {
        if (!clienteExistente)
          ticketCliente.textContent =
            (inpNombres.value + " " + inpApellidos.value).trim() || "---";
      }),
    );

    /* Ficha y diseno en el ticket */
    Object.keys(SELECTS).forEach((k) => {
      const s = $(SELECTS[k]),
        o = $(SALIDAS[k]);
      if (s && o) {
        o.textContent = s.value;
        s.addEventListener("change", () => {
          o.textContent = s.value;
        });
      }
    });
    Object.values(MEDIDAS).forEach(([i, o]) => {
      const inp = $(i),
        out = $(o);
      if (inp && out)
        inp.addEventListener("input", () => {
          out.textContent = inp.value || "0";
        });
    });
    const armarDetalle = () => {
      const d = {};
      Object.keys(SELECTS).forEach((k) => {
        d[k] = $(SELECTS[k]).value;
      });
      d.medidas = {};
      Object.keys(MEDIDAS).forEach((k) => {
        d.medidas[k] = $(MEDIDAS[k][0]).value || "0";
      });
      return JSON.stringify(d);
    };
    const mostrarDetalle = (texto) => {
      let d;
      try {
        d = JSON.parse(texto || "{}");
      } catch {
        return;
      }
      Object.keys(SALIDAS).forEach((k) => {
        if (d[k]) $(SALIDAS[k]).textContent = d[k];
      });
      Object.keys(MEDIDAS).forEach((k) => {
        if (d.medidas && d.medidas[k] !== undefined)
          $(MEDIDAS[k][1]).textContent = d.medidas[k];
      });
    };

    /* Finanzas */
    function ajustarGrid() {
      const transf = metodoActual() === "Transferencia";
      pagoGridContainer.classList.toggle("transferencia-layout", transf);
      pagoGridContainer.style.gridTemplateColumns = transf
        ? "1fr 1fr"
        : orden
          ? "repeat(3, 1fr)"
          : "1fr 1fr";
      campoAdelantoContainer.style.display = orden ? "block" : "none";
    }
    function calcular() {
      const costo = parseFloat(inputCostoTotal.value) || 0,
        pagado = parseFloat(inputEntregado.value) || 0,
        saldo = saldoFijo();
      if (metodoActual() === "Efectivo") {
        const vuelto = Math.max(0, redondear(pagado - (orden ? saldo : costo)));
        labelTercerCampo.textContent = "Vuelto automático (Calculado)";
        inputTercerCampo.value = TG.money(vuelto);
        inputTercerCampo.style.color = "#28a745";
        ticketContenedorVuelto.style.display = "";
        ticketVuelto.textContent = TG.money(vuelto);
      } else {
        labelTercerCampo.textContent = "Saldo pendiente restante";
        inputTercerCampo.value = TG.money(
          Math.max(0, redondear((orden ? saldo : costo) - pagado)),
        );
        inputTercerCampo.style.color = "#d9534f";
        ticketContenedorVuelto.style.display = "none";
      }
      ticketTotal.textContent = TG.money(costo);
      ticketAdelantoPrevio.textContent = TG.money(orden ? orden.adelanto : 0);
      ticketSaldoPendiente.textContent = TG.money(saldo);
      ticketMontoPagado.textContent = TG.money(pagado);
      if (orden && !terminado) pintarBoleta();
    }
    inputCostoTotal.addEventListener("input", calcular);
    inputEntregado.addEventListener("input", calcular);
    inputsSeleccion();
    function inputsSeleccion() {
      document.querySelectorAll('input[type="number"]').forEach((i) =>
        i.addEventListener("focus", function () {
          this.select();
        }),
      );
    }

    const metodos = document.querySelectorAll(".metodo-btn");
    metodos.forEach((b) =>
      b.addEventListener("click", () => {
        metodos.forEach((x) => x.classList.remove("active"));
        b.classList.add("active");
        ticketMetodoPago.textContent = b.dataset.metodo;
        ajustarGrid();
        /* Yape: QR + número · Transferencia: cuenta y CCI (datos guardados por el administrador) */
        TG.mostrarDatosPago(b.dataset.metodo);
        calcular();
      }),
    );

    /* Ordenes pendientes de entrega */
    const textoVence = (f) => {
      const d = TG.diasEntre(hoy, f);
      return d < 0
        ? "🔴 Vencida (" + TG.fecha(f) + ")"
        : d === 0
          ? "⚠️ Vence hoy (" + TG.fecha(f) + ")"
          : "⏳ Vence en " +
            d +
            " día" +
            (d === 1 ? "" : "s") +
            " (" +
            TG.fecha(f) +
            ")";
    };
    const pintarOrdenes = async () => {
      ordenes = (await TG.get("/api/confecciones"))
        .filter((o) => o.estado === "En confección")
        .sort((a, b) => a.entregaFinal.localeCompare(b.entregaFinal));
      lista.innerHTML = ordenes.length
        ? ordenes
            .map((o) => {
              const hoyFecha = hoy;
              const prueba = o.primeraPrueba;
              const fechaClave =
                prueba &&
                prueba >= hoyFecha &&
                (!o.entregaFinal || prueba <= o.entregaFinal)
                  ? prueba
                  : o.entregaFinal;
              const tipoPlazo = fechaClave === prueba ? "Prueba" : "Entrega";
              return (
                '<div class="orden-item-btn" data-id="' +
                o.id +
                '">' +
                '<div class="orden-identidad"><div class="avatar">' +
                TG.esc(TG.iniciales(TG.nombre(o.cliente))) +
                '</div><div class="orden-nombre">' +
                '<span class="dato-label">Cliente</span><strong>' +
                TG.esc(TG.nombre(o.cliente)) +
                "</strong></div></div>" +
                '<div class="orden-dato"><span class="dato-label">Ticket</span><strong>' +
                TG.esc(o.ticket) +
                "</strong></div>" +
                '<div class="orden-dato"><span class="dato-label">DNI</span><strong>' +
                TG.esc(o.cliente.dni) +
                "</strong></div>" +
                '<div class="orden-dato"><span class="dato-label">Teléfono</span><strong>' +
                TG.esc(o.cliente.celular || "—") +
                "</strong></div>" +
                '<div class="orden-dato orden-direccion"><span class="dato-label">Dirección</span><strong>' +
                TG.esc(o.cliente.direccion || "—") +
                "</strong></div>" +
                '<span class="orden-vence">' +
                tipoPlazo +
                ": " +
                textoVence(fechaClave) +
                "</span></div>"
              );
            })
            .join("")
        : '<p style="padding:16px;color:#667085;">No hay órdenes de confección pendientes de entrega.</p>';
    };

    /* Boleta de confeccion */
    function pintarBoleta(emitida) {
      const set = (id, v) => {
        const el = $(id);
        if (el) el.textContent = v;
      };
      const pagada = ordenPagada(),
        metodo = pagada ? orden.metodoPago || "Efectivo" : metodoActual(),
        efectivo = !pagada && metodo === "Efectivo";
      const costo = orden.costoTotal,
        adelanto = orden.adelanto || 0,
        saldo = saldoFijo(),
        pagadoHoy = parseFloat(inputEntregado.value) || 0;
      set("cbNumero", emitida ? "N.° " + emitida.numero : "N.° por emitir");
      set("cbCliente", TG.nombre(orden.cliente));
      set("cbDni", orden.cliente.dni);
      set("cbFecha", TG.fechaHora());
      set("cbEmpleado", u ? u.nombre : "");
      set("cbPago", metodo);
      set("cbTicket", orden.ticket);
      /* Si en una prueba se cambió la fecha de entrega, la boleta muestra la fecha nueva */
      set("cbEntrega", TG.fecha(orden.entregaFinal));
      set("cbCodigo", orden.ticket);
      set(
        "cbDetalle",
        "Modelo: " +
          $("tDiseno").textContent +
          " · Tela: " +
          $("tTela").textContent,
      );
      document.querySelectorAll(".cbCosto").forEach((el) => {
        el.textContent = TG.money(costo);
      });
      set("cbAdelanto", "- " + TG.money(adelanto));
      set("cbAdelanto2", "- " + TG.money(adelanto));
      set("cbSaldo", TG.money(saldo));
      /* Total en letras segun pago */
      set("cbLetras", TG.montoLetras(pagada ? costo : saldo));
      set(
        "cbEtqRecibido",
        pagada
          ? "Pagado con el ticket"
          : efectivo
            ? "Efectivo recibido"
            : "Monto pagado",
      );
      set("cbRecibido", TG.money(pagada ? adelanto : pagadoHoy));
      set("cbVuelto", TG.money(Math.max(0, redondear(pagadoHoy - saldo))));
      $("cbFilaVuelto").style.display = efectivo ? "" : "none";
      $("cbBorrador").hidden = !!emitida;
      /* Imprimir solo si ya se emitio */
      document.querySelectorAll("[data-print-receipt]").forEach((btn) => {
        btn.disabled = !emitida;
      });
      $("cpTicketConf").hidden = true;
      $("cpBoletaConf").hidden = false;
    }
    document
      .querySelectorAll("[data-print-receipt]")
      .forEach((btn) => btn.addEventListener("click", () => window.print()));

    /* Ticket pagado emite la boleta sola */
    function modoTicketPagado() {
      document.querySelector(".metodos-pago").style.display = "none";
      pagoGridContainer.style.display = "none";
      TG.mostrarDatosPago("Efectivo");
      $("avisoPagadoMetodo").textContent = orden.metodoPago || "Efectivo";
      $("avisoPagadoTotal").hidden = false;
      btnConfirmar.textContent = "Generar boleta >";
      emitirBoleta();
    }
    function seleccionarOrden(o, tarjeta) {
      /* Evita emitir dos veces */
      if (orden) return;
      orden = o;
      terminado = false;
      clienteExistente = o.cliente;
      ticketCliente.textContent = TG.nombre(o.cliente);
      ticketDni.textContent = o.cliente.dni;
      ticketCodigo.textContent = o.ticket;
      $("ticketTitulo").textContent = "TICKET DE CONFECCIÓN";
      ticketEntrega.textContent = TG.fecha(o.entregaFinal);
      mostrarDetalle(o.detalle);
      inputCostoTotal.value = o.costoTotal.toFixed(2);
      inputCostoTotal.readOnly = true;
      inputCostoTotal.style.backgroundColor = "#f5f5f5";
      TG.marcar(inputCostoTotal, null);
      labelCostoTotal.textContent = "Costo total (Fijo por Ticket)";
      inputAdelantoFijo.value = TG.money(o.adelanto);
      inputEntregado.value = "0.00";
      inputEntregado._tg.regla = reglaSaldo;
      inputEntregado._tocado = false;
      TG.marcar(inputEntregado, null);
      ajustarGrid();
      /* Dibuja la boleta */
      calcular();
      seccionDatosCliente.style.display = "none";
      seccionMedidas.style.display = "none";
      seccionPlazos.style.display = "none";
      lista.querySelectorAll(".orden-item-btn").forEach((b) => {
        b.style.display = "none";
      });
      const accionesOrden = $("accionesOrdenSeleccionada");
      if (accionesOrden) {
        accionesOrden.hidden = false;
        accionesOrden.innerHTML = "";
        const volver = document.createElement("button");
        volver.className = "btn-volver";
        volver.type = "button";
        volver.innerHTML = "← Volver a confección";
        volver.addEventListener("click", () => location.reload());
        accionesOrden.appendChild(volver);
      }
      if (tarjeta) {
        tarjeta.style.display = "grid";
        tarjeta.classList.add("orden-seleccionada");
        tarjeta.style.border = "1px solid #e4d7bb";
        tarjeta.style.background = "#fbf7ee";
      }
      /* Dos opciones: Prueba de confección o Entrega. Si ya se registró una prueba, propone Entrega */
      seccionModo.style.display = "";
      elegirModo((orden.pruebas || 0) > 0 ? "entrega" : "prueba");
    }

    /* Prueba / Entrega */
    let entregaIniciada = false;
    function elegirModo(m) {
      if (!orden || terminado) return;
      btnModoPrueba.classList.toggle("activo", m === "prueba");
      btnModoEntrega.classList.toggle("activo", m === "entrega");
      panelPrueba.style.display = m === "prueba" ? "" : "none";
      /* En la prueba no se cobra ni se muestra la boleta; eso es de la entrega */
      seccionFinanzas.style.display = m === "entrega" ? "" : "none";
      seccionComprobantes.style.display = m === "entrega" ? "" : "none";
      if (m === "prueba") {
        pruebaEntrega.min = hoy;
        pruebaEntrega.value = orden.entregaFinal || sumarDias(7);
        pintarHistorial();
      } else if (ordenPagada() && !entregaIniciada) {
        /* Ticket pagado completo: la boleta sale sola al elegir Entrega */
        entregaIniciada = true;
        modoTicketPagado();
      }
    }
    function pintarHistorial() {
      const h = (orden.comentarioPrueba || "").trim();
      pruebaHistorial.style.display = h ? "block" : "none";
      pruebaHistorial.textContent = h
        ? "Pruebas anteriores (" + (orden.pruebas || 0) + "):\n" + h
        : "";
    }
    btnModoPrueba.addEventListener("click", () => elegirModo("prueba"));
    btnModoEntrega.addEventListener("click", () => elegirModo("entrega"));
    btnGuardarPrueba.addEventListener("click", async () => {
      if (!orden || terminado) return;
      const comentario = pruebaComentario.value.trim();
      if (comentario.length < 5) {
        TG.marcar(pruebaComentario, "Escribe qué se puede mejorar (mínimo 5 caracteres)");
        pruebaComentario.focus();
        return;
      }
      TG.marcar(pruebaComentario, null);
      if (!pruebaEntrega.value || pruebaEntrega.value < hoy) {
        TG.marcar(pruebaEntrega, "Fecha inválida: no puede ser anterior a hoy");
        pruebaEntrega.focus();
        return;
      }
      TG.marcar(pruebaEntrega, null);
      btnGuardarPrueba.disabled = true;
      try {
        const r = await TG.put("/api/confecciones/prueba/" + orden.id, {
          comentario,
          entregaFinal: pruebaEntrega.value,
        });
        orden = { ...orden, ...r };
        ordenes = ordenes.map((x) => (x.id === orden.id ? orden : x));
        ticketEntrega.textContent = TG.fecha(orden.entregaFinal);
        pruebaComentario.value = "";
        pintarHistorial();
        pintarBoleta();
        TG.toast("Prueba registrada · entrega: " + TG.fecha(orden.entregaFinal));
      } catch (err) {
        TG.toast(err.message, "error");
      } finally {
        btnGuardarPrueba.disabled = false;
      }
    });
    lista.addEventListener("click", (e) => {
      const t = e.target.closest(".orden-item-btn");
      if (!t) return;
      const o = ordenes.find((x) => x.id === Number(t.dataset.id));
      if (o) seleccionarOrden(o, t);
    });
    $("btnEjecutarBusqueda")?.addEventListener("click", () => {
      const c = inputBusquedaOrden.value.trim().toUpperCase();
      if (!c) return;
      const o = ordenes.find(
        (x) =>
          x.ticket.toUpperCase() === c ||
          (/^T\d{3}-\d{1,4}$/.test(c) &&
            x.ticket ===
              "T" + c.slice(1, 4) + "-" + c.slice(5).padStart(4, "0")) ||
          x.cliente.dni === c,
      );
      if (orden) return;
      if (!o) {
        TG.toast("No se encontró ninguna orden con ese DNI o Ticket.", "error");
        return;
      }
      seleccionarOrden(
        o,
        lista.querySelector('.orden-item-btn[data-id="' + o.id + '"]'),
      );
    });

    /* Emitir la boleta */
    async function emitirBoleta() {
      if (emitiendo || terminado) return;
      emitiendo = true;
      btnConfirmar.disabled = true;
      const pagada = ordenPagada(),
        metodo = pagada ? orden.metodoPago || "Efectivo" : metodoActual();
      let emitida;
      try {
        emitida = await TG.post("/api/boletas/nuevo", {
          tipo: "Confección",
          cliente: { id: orden.cliente.id },
          confeccion: { id: orden.id },
          monto: orden.costoTotal,
          empleado: u ? u.nombre : "",
          metodoPago: metodo,
        });
      } catch (err) {
        emitiendo = false;
        btnConfirmar.disabled = false;
        TG.toast(err.message, "error");
        if (!pagada) TG.marcar(inputEntregado, err.message);
        return;
      }
      terminado = true;
      emitiendo = false;
      seccionModo.style.display = "none";
      pintarBoleta(emitida);
      btnConfirmar.disabled = false;
      btnConfirmar.textContent = "＋ Nueva confección";
      TG.toast("Boleta " + emitida.numero + " emitida");
    }

    /* Guarda el texto original del boton */
    const textoConfirmar = btnConfirmar.textContent;

    /* Deja el formulario listo para otra confeccion */
    function reiniciarFormulario() {
      [inpDni, ...datos].forEach((el) => {
        el.value = "";
        el._tocado = false;
        TG.marcar(el, null);
      });
      bloquear(false);
      clienteExistente = null;

      Object.values(MEDIDAS).forEach(([id]) => {
        const el = $(id);
        if (!el) return;
        el.value = "";
        el._tocado = false;
        TG.marcar(el, null);
      });

      inpPrueba.value = sumarDias(3);
      inpEntrega.value = sumarDias(7);
      inpEntrega.min = hoy;
      inputCostoTotal.value = "0.00";
      inputEntregado.value = "0.00";

      [inpPrueba, inpEntrega, inputCostoTotal, inputEntregado].forEach((el) => {
        el._tocado = false;
        TG.marcar(el, null);
      });

      inputTercerCampo.value = TG.money(0);
      terminado = false;
      btnConfirmar.textContent = textoConfirmar;
    }

    /* Confirmar pago */
    /* Evita perder el foco antes del clic */
    btnConfirmar?.addEventListener("mousedown", (e) => e.preventDefault());

    btnConfirmar?.addEventListener("click", async () => {
      if (terminado) {
        location.reload();
        return;
      }
      if (orden) {
        /* Entrega con boleta */
        if (!ordenPagada() && !TG.validarTodo([inputEntregado])) return;
        await emitirBoleta();
        return;
      }
      const metodo = metodoActual(),
        pagado = parseFloat(inputEntregado.value) || 0,
        costo = parseFloat(inputCostoTotal.value) || 0;
      const medidasObligatorias = Object.values(MEDIDAS).map(([id]) => $(id));
      const campos = [
        inpDni,
        ...datos,
        inpPrueba,
        inpEntrega,
        ...medidasObligatorias,
        inputCostoTotal,
        inputEntregado,
      ];
      if (
        medidasObligatorias.some(
          (el) => !el || !el.value.trim() || Number(el.value) <= 0,
        )
      ) {
        TG.toast(
          "Completa todas las medidas con valores mayores a cero antes de continuar.",
          "error",
        );
        medidasObligatorias
          .filter((el) => !el || !el.value.trim() || Number(el.value) <= 0)
          .forEach((el) => el && TG.marcar(el, "Medida obligatoria"));
        return;
      }
      if (!inpPrueba.value || !inpEntrega.value) {
        TG.toast(
          "La primera prueba y el plazo de entrega son obligatorios.",
          "error",
        );
        return;
      }
      if (!TG.validarTodo(campos)) return;
      btnConfirmar.disabled = true;
      try {
        /* Orden nueva con ticket */
        let c = clienteExistente;
        if (!c)
          c = await TG.post("/api/clientes/nuevo", {
            dni: inpDni.value,
            nombres: inpNombres.value.trim(),
            apellidos: inpApellidos.value.trim(),
            celular: inpTelefono.value,
            direccion: inpDireccion.value.trim(),
            correo: inpCorreo.value.trim(),
          });
        const detalleConfeccion = armarDetalle();
        const coloresSeleccionados = [
          "pColorSaco",
          "pColorPantalon",
          "pColorChaleco",
          "pColorCamisa",
          "pColorCorbata",
        ]
          .map((id) => $(id)?.value?.trim())
          .filter((v) => v && !/^seleccione/i.test(v));
        const r = await TG.post("/api/confecciones/nuevo", {
          cliente: { id: c.id },
          costoTotal: costo,
          adelanto: Math.min(pagado, costo),
          primeraPrueba: inpPrueba.value,
          entregaFinal: inpEntrega.value,
          metodoPago: metodo,
          empleado: u ? u.nombre : "",
          color: coloresSeleccionados.join(" / ") || "No especificado",
          detalle: detalleConfeccion,
        });
        ticketCodigo.textContent = r.ticket;
        $("ticketTitulo").textContent = "TICKET DE CONFECCIÓN";
        ticketCliente.textContent = TG.nombre(c);
        ticketDni.textContent = c.dni;
        ticketAdelantoPrevio.textContent = TG.money(0);
        ticketSaldoPendiente.textContent = TG.money(
          Math.max(0, redondear(costo - pagado)),
        );
        TG.toast("Ticket " + r.ticket + " registrado");

        /* Limpia el formulario para otra confeccion */
        reiniciarFormulario();
        btnConfirmar.disabled = false;

        /* Actualiza la lista de ordenes */
        try {
          await pintarOrdenes();
        } catch (e) {
          TG.toast(e.message, "error");
        }
      } catch (err) {
        btnConfirmar.disabled = false;
        TG.toast(err.message, "error");
        TG.marcar(inpDni, err.message);
      }
    });

    ajustarGrid();
    calcular();
    try {
      await pintarOrdenes();
    } catch (e) {
      TG.toast(e.message, "error");
    }
  }),
);