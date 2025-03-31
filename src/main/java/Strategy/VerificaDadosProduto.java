package Strategy;

import Dominio.EntidadeDominio;
import Dominio.Produto;

public class VerificaDadosProduto implements IStrategy{
    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        Produto produto = (Produto) entidade;
        if(isStringInvalida(produto.getSku())){
            sb.append("Sku é obrigatório");
        }
        if(isStringInvalida(produto.getNome())){
            sb.append("Nome do produto é obrigatório");
        }
        if(produto.getPreco() == null || produto.getPreco().isInfinite() || produto.getPreco().isNaN()) {
            sb.append("Utilize um valor adequado para o preço do produto");
        }
        if(isStringInvalida(produto.getModelo())){
            sb.append("Modelo do produto é obrigatório");
        }
        if(isStringInvalida(produto.getCor())){
            sb.append("Cor do produto é obrigatório");
        }
        if(produto.getTamanho() == null){
            sb.append("Tamanho do produto é obrigatório");
        }
        if(produto.getGenero() == null) {
            sb.append("Selecione um gênero para registrar o produto");
        }
        return null;
    }

    private boolean isStringInvalida(String value) {
        return value == null || value.isBlank();
    }
}
