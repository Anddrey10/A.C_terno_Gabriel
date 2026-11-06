// Clientes (empleado): consulta y administración de clientes registrados
document.addEventListener("DOMContentLoaded", () =>
  TG.listo(async () => {
    const tbody = document.querySelector(".data-table tbody");
    const searchInput = document.querySelector("[data-table-search]");

    const pintar = async () => {
      const [clientes, boletas, penalidades] = await Promise.all([
        TG.clientes(),
        TG.boletas(),
        TG.penalidades(),
      ]);
      const lista = clientes
        .filter((c) => !TG.clienteEliminado(c, boletas))
        .sort((a, b) => b.id - a.id);
      tbody.innerHTML = lista.length
        ? lista
          .map((c) => {
            const estado = TG.estadoCliente(c, penalidades);
            return (
              "<tr><td>" +
              TG.esc(c.dni) +
              "</td><td>" +
              TG.esc(TG.nombre(c)) +
              "</td><td>" +
              TG.esc([c.direccion, c.distrito].filter(Boolean).join(", ")) +
              "</td><td>" +
              TG.esc(TG.telefono(c)) +
              "</td><td>" +
              TG.esc(c.correo || "—") +
              "</td><td>" +
              TG.ultimaOperacion(c, boletas) +
              "</td><td>" +
              TG.estrellas(c.calificacion, c.promedio) +
              '</td><td><span class="status-badge ' +
              (estado === "Activo" ? "active" : "debt") +
              '"><span class="dot"></span> ' +
              estado +
              "</span></td></tr>"
            );
          })
          .join("")
        : '<tr><td colspan="8" style="text-align:center;color:#667085;padding:20px;">Aún no hay clientes registrados.</td></tr>';
      filtrar();
    };

    const filtrar = () => {
      const q = (searchInput?.value || "").toLowerCase().trim();
      tbody.querySelectorAll("tr").forEach((row) => {
        row.style.display = row.textContent.toLowerCase().includes(q)
          ? ""
          : "none";
      });
    };
    searchInput?.addEventListener("input", filtrar);

    try {
      await pintar();
    } catch (err) {
      TG.toast(err.message);
    }

    /* La calificación se recalcula sola cuando otro empleado califica: se refresca sin recargar la página */
    setInterval(() => {
      if (!document.hidden) pintar().catch(() => { });
    }, 10000);
    document.addEventListener("visibilitychange", () => {
      if (!document.hidden) pintar().catch(() => { });
    });
  }),
);