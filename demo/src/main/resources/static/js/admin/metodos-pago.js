/* Método de pago (admin): el QR de Yape, el número y los datos de transferencia se guardan en la base de datos
   (tabla MetodoPago). El empleado los muestra al cobrar en "Comprar / Alquilar" paso 4 y en "Confección". */
document.addEventListener("DOMContentLoaded", () =>
  TG.listo(async () => {
    const $ = (id) => document.getElementById(id);
    let metodos = {};
    let qrNuevo = null; /* data URL elegido y aún no guardado */
    let quitarQr = false;

    const agrupar = (n, tam) =>
      String(n || "")
        .replace(new RegExp("(.{" + tam + "})(?=.)", "g"), "$1 ")
        .trim();
    const soloDigitos = (v) => String(v || "").replace(/\D/g, "");

    /* Dígitos de la cuenta según el banco (mismos valores que valida el servidor) */
    const LARGOS = { bcp: [13, 14], interbank: [13], scotiabank: [10], bbva: [18, 20], banbif: [10] };
    const largosBanco = (banco) => LARGOS[String(banco || "").trim().toLowerCase()] || null;
    const textoLargos = (l) => l.join(" o ");

    /* Cada campo numérico acepta solo dígitos y no deja pasar más de los permitidos */
    const soloNumeros = (id) =>
      $(id).addEventListener("input", (e) => {
        e.target.value = soloDigitos(e.target.value).slice(0, e.target.maxLength);
        e.target.classList.remove("invalido");
      });
    ["yapeNumero", "transNumero", "transCci"].forEach(soloNumeros);

    /* Cada contenedor siempre muestra "lo guardado + Editar". Al pulsar Editar aparece el formulario con
       los botones Confirmar y Cancelar; Cancelar descarta los cambios y vuelve a lo guardado. */
    const modo = (cual, editando) => {
      $(cual + "Vista").hidden = editando;
      $(cual + "Form").hidden = !editando;
    };
    const marcar = (id, mal) => $(id).classList.toggle("invalido", !!mal);
    const ultima = (m) =>
      m && m.modificado
        ? TG.fecha(m.modificado) + " · " + (m.modificadoPor || "Administrador")
        : "—";

    /* ---------- Pintar lo que hay en la base ---------- */
    const pintarQr = (src) => {
      const img = $("yapeQrImg");
      if (src) {
        img.src = src;
        img.hidden = false;
        $("yapeQrVacio").hidden = true;
      } else {
        img.removeAttribute("src");
        img.hidden = true;
        $("yapeQrVacio").hidden = false;
      }
    };

    const pintar = () => {
      const y = metodos.YAPE || {};
      const t = metodos.TRANSFERENCIA || {};
      qrNuevo = null;
      quitarQr = false;
      pintarQr(y.qr);
      $("yapeNumero").value = y.numero || "";
      $("yapeTitular").value = y.titular || "";
      $("yapeUltima").textContent = ultima(y);
      $("transBanco").value = t.banco || "";
      $("transTitular").value = t.titular || "";
      $("transNumero").value = t.numero || "";
      $("transCci").value = t.cci || "";
      $("transUltima").textContent = ultima(t);
      ayudaCuenta();

      /* Vista de lo que ya guardó el administrador */
      const vQr = $("yapeVistaImg");
      if (y.qr) {
        vQr.src = y.qr;
        vQr.hidden = false;
        $("yapeVistaQrVacio").hidden = true;
      } else {
        vQr.removeAttribute("src");
        vQr.hidden = true;
        $("yapeVistaQrVacio").hidden = false;
      }
      $("yapeVistaNumero").textContent = agrupar(y.numero, 3) || "—";
      $("yapeVistaTitular").textContent = y.titular || "—";
      $("transVistaBanco").textContent = t.banco || "—";
      $("transVistaTitular").textContent = t.titular || "—";
      $("transVistaNumero").textContent = t.numero || "—";
      $("transVistaCci").textContent = t.cci || "—";
      modo("yape", false);
      modo("trans", false);
    };

    /* Texto de ayuda: cuántos dígitos lleva la cuenta del banco elegido */
    function ayudaCuenta() {
      const l = largosBanco($("transBanco").value);
      $("transNumero").maxLength = l ? Math.max(...l) : 20;
      $("transNumeroAyuda").textContent = l
        ? "Cuenta " + $("transBanco").value.trim() + ": exactamente " + textoLargos(l) + " dígitos."
        : "Solo números (de 8 a 20 dígitos).";
    }
    $("transBanco").addEventListener("input", ayudaCuenta);

    $("yapeEditar").addEventListener("click", () => modo("yape", true));
    $("transEditar").addEventListener("click", () => modo("trans", true));
    $("yapeCancelar").addEventListener("click", pintar);
    $("transCancelar").addEventListener("click", pintar);

    /* ---------- Imagen QR: se reduce en el navegador antes de guardarla ---------- */
    const reducir = (archivo) =>
      new Promise((ok, fallo) => {
        const lector = new FileReader();
        lector.onerror = () => fallo(new Error("No se pudo leer la imagen"));
        lector.onload = () => {
          const img = new Image();
          img.onerror = () => fallo(new Error("El archivo no es una imagen válida"));
          img.onload = () => {
            const MAX = 600;
            const escala = Math.min(1, MAX / Math.max(img.width, img.height));
            const lienzo = document.createElement("canvas");
            lienzo.width = Math.round(img.width * escala);
            lienzo.height = Math.round(img.height * escala);
            const ctx = lienzo.getContext("2d");
            ctx.fillStyle = "#fff";
            ctx.fillRect(0, 0, lienzo.width, lienzo.height);
            ctx.drawImage(img, 0, 0, lienzo.width, lienzo.height);
            ok(lienzo.toDataURL("image/png"));
          };
          img.src = lector.result;
        };
        lector.readAsDataURL(archivo);
      });

    $("yapeQrArchivo").addEventListener("change", async (e) => {
      const archivo = e.target.files[0];
      if (!archivo) return;
      try {
        qrNuevo = await reducir(archivo);
        quitarQr = false;
        pintarQr(qrNuevo);
      } catch (err) {
        TG.toast(err.message);
      }
      e.target.value = "";
    });

    $("yapeQrQuitar").addEventListener("click", () => {
      qrNuevo = null;
      quitarQr = true;
      pintarQr(null);
    });

    /* ---------- Guardar ---------- */
    const guardar = async (boton, tipo, datos, mensaje) => {
      boton.disabled = true;
      try {
        const u = TG.usuario();
        metodos[tipo] = await TG.put("/api/metodos-pago/" + tipo, {
          ...datos,
          modificadoPor: u ? u.nombre : "Administrador",
        });
        pintar();
        TG.toast(mensaje);
      } catch (err) {
        alert(err.message);
      } finally {
        boton.disabled = false;
      }
    };

    $("yapeGuardar").addEventListener("click", () => {
      const numero = $("yapeNumero").value;
      const titular = $("yapeTitular").value.trim();
      marcar("yapeNumero", !/^9\d{8}$/.test(numero));
      marcar("yapeTitular", !titular);
      if (!/^9\d{8}$/.test(numero))
        return alert("El número de Yape debe tener exactamente 9 dígitos y empezar con 9");
      if (!titular) return alert("El titular de Yape es obligatorio");
      guardar(
        $("yapeGuardar"),
        "YAPE",
        {
          numero,
          titular,
          qr: qrNuevo || "",
          quitarQr,
        },
        "Yape actualizado",
      );
    });

    $("transGuardar").addEventListener("click", () => {
      const banco = $("transBanco").value.trim();
      const titular = $("transTitular").value.trim();
      const numero = $("transNumero").value;
      const cci = $("transCci").value;
      const largos = largosBanco(banco);
      const numeroOk = largos
        ? largos.includes(numero.length)
        : /^\d{8,20}$/.test(numero);
      marcar("transBanco", !banco);
      marcar("transTitular", !titular);
      marcar("transNumero", !numeroOk);
      marcar("transCci", !/^\d{20}$/.test(cci));
      if (!banco) return alert("El banco es obligatorio");
      if (!titular) return alert("El titular de la cuenta es obligatorio");
      if (!numeroOk)
        return alert(
          largos
            ? "El número de cuenta de " + banco + " debe tener exactamente " + textoLargos(largos) + " dígitos"
            : "El número de cuenta debe tener entre 8 y 20 dígitos",
        );
      if (!/^\d{20}$/.test(cci)) return alert("El CCI debe tener exactamente 20 dígitos");
      guardar(
        $("transGuardar"),
        "TRANSFERENCIA",
        { banco, titular, numero, cci },
        "Transferencia actualizada",
      );
    });

    /* ---------- Carga inicial ---------- */
    try {
      metodos = await TG.metodosPago();
    } catch (err) {
      TG.toast(err.message);
    }
    pintar();
  }),
);
