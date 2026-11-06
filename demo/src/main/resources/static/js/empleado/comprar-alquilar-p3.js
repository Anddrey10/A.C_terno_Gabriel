/* Paso 3 */
(() => {
  class Paso3 {
    constructor(sesion) {
      this.sesion = sesion;
    }

    iniciar() {
      const terms = document.getElementById("aceptaTerminos");
      const continueTerms = document.getElementById("continueTerms");

      const updateTerms = () => {
        if (!terms || !continueTerms) return;

        continueTerms.setAttribute(
          "aria-disabled",
          String(!terms.checked),
        );

        continueTerms.classList.toggle("is-disabled", !terms.checked);
      };

      if (!terms) return;

      terms.addEventListener("change", () => {
        this.sesion.guardar({ aceptaTerminos: terms.checked });
        updateTerms();
      });

      updateTerms();

      continueTerms?.addEventListener("click", (e) => {
        if (!terms.checked) {
          e.preventDefault();
          alert("Debes aceptar los términos para continuar.");
        }
      });
    }
  }

  CA.arrancar(3, Paso3);
})();