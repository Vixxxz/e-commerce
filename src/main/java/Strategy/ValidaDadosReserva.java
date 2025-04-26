package Strategy;

import Dominio.EntidadeDominio;
import Dominio.ReservaEstoque;

public class ValidaDadosReserva implements IStrategy{
    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        ReservaEstoque reserva = (ReservaEstoque) entidade;

        if (reserva.getProduto() == null || reserva.getProduto().getId() == null) {
            sb.append("Produto é obrigatório e deve ter um ID válido.\n");
        }

        if (reserva.getMarca() == null || reserva.getMarca().getId() == null) {
            sb.append("Marca é obrigatória e deve ter um ID válido.\n");
        }

        if (reserva.getQuantidade() == null || reserva.getQuantidade() <= 0) {
            sb.append("Quantidade deve ser um número positivo.\n");
        }

        if (reserva.getSessao() == null || reserva.getSessao().trim().isEmpty()) {
            sb.append("Sessão é obrigatória.\n");
        }
        return null;
    }
}
