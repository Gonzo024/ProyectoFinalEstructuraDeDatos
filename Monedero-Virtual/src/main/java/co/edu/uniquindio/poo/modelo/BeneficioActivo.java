package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.modelo.enums.TipoBeneficio;

import java.time.LocalDateTime;

/** Beneficio temporal que un cliente canjeó. Vale desde inicio hasta fin. */
public class BeneficioActivo {

    private final String clienteId;
    private final TipoBeneficio tipo;
    private final LocalDateTime inicio;
    private final LocalDateTime fin;

    public BeneficioActivo(String clienteId, TipoBeneficio tipo,
                           LocalDateTime inicio, LocalDateTime fin) {
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.inicio = inicio;
        this.fin = fin;
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return !ahora.isBefore(inicio) && ahora.isBefore(fin);
    }

    public String getClienteId() { return clienteId; }
    public TipoBeneficio getTipo() { return tipo; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
}