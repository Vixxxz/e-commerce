package Strategy;

import Dao.PedidoDAO;
import Dao.PedidoProdutoDAO;
import Dao.ProdutoDAO;
import Dominio.*;
import Service.GeminiService;
import Util.Resultado;

import java.util.ArrayList;
import java.util.List;

public class RecomendacaoIA implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof RequisicaoIA requisicao)) {
            return Resultado.erro("Entidade inválida para recomendação com IA.");
        }

        if ((requisicao.getCliente() == null || requisicao.getCliente().getCpf() == null) || requisicao.getPergunta() == null) {
            return Resultado.erro("CPF e pergunta devem ser informados.");
        }

        // --- ORQUESTRAÇÃO ---

        // ETAPA 1: Primeira requisição à IA para gerar o perfil do cliente.
        Resultado<String> resultadoPerfil = gerarPerfilCliente(requisicao.getCliente());
        if (!resultadoPerfil.isSucesso()) {
            // Se a geração do perfil falhar, ainda podemos prosseguir com um aviso.
            sb.append("Aviso: Não foi possível gerar o perfil do cliente. Erro: ").append(resultadoPerfil.getErro());
        }
        String perfilCliente = resultadoPerfil.getValor() != null ? resultadoPerfil.getValor() : "Não foi possível carregar o perfil do cliente.";

        // ETAPA 2: Segunda requisição à IA, agora para obter a recomendação, usando o perfil gerado.
        return obterRecomendacao(
                perfilCliente,
                requisicao.getHistoricoConversa(),
                requisicao.getPergunta()
        );
    }

    /**
     * REQUISIÇÃO 1: Gera o perfil do cliente.
     * Busca o histórico de compras e chama a IA para criar um resumo.
     */
    private Resultado<String> gerarPerfilCliente(Cliente cliente) {
        // 1. Busca de Dados
        String historicoDeCompras = buscarHistorico(cliente);

        // 2. Montagem do Prompt de Perfil
        String promptPerfil = String.format("""
            ### TAREFA
            Analise o seguinte histórico de compras e crie um perfil conciso em uma única frase. Foque nas preferências de categoria, marca e gênero.

            ### HISTÓRICO DE COMPRAS
            %s

            ### EXEMPLO DE SAÍDA
            "Cliente com preferência por tênis casuais da marca Puma."
            ou
            "Cliente novo, sem histórico de compras."

            ### PERFIL DO CLIENTE (gere apenas uma frase)
            """, historicoDeCompras);

        // 3. Chamada à IA para criar o perfil
        return GeminiService.consultarIA(promptPerfil);
    }

    /**
     * REQUISIÇÃO 2: Obtém a recomendação.
     * Busca o catálogo de produtos e usa o perfil já gerado para chamar a IA.
     */
    private Resultado<String> obterRecomendacao(String perfilCliente, String historicoConversa, String pergunta) {
        // 1. Busca do catálogo de produtos
        String catalogoProdutos = buscarCatalogo();

        // 2. Montagem do Prompt de Recomendação
        String historicoFinal = historicoConversa == null ? "" : historicoConversa;

        String promptFinal = String.format("""
            ### 🧠 PERSONA
            Você é um **assistente virtual de vendas**, especialista em calçados, com memória de contexto e foco total em conversão.

            ### 📜 REGRAS E DIRETRIZES
                1. **🧩 MEMÓRIA E CONTEXTO (REGRA MAIS IMPORTANTE):**
                   - Mantenha memória de tudo o que já foi dito ou decidido.
                   - **Jamais repita perguntas** ou reinicie o funil sem necessidade.
                   - Se o cliente for vago (ex: "qualquer modelo", "tanto faz"), use o histórico e **assuma a necessidade mais provável**.
                   - ❗ Antes de qualquer resposta, valide se sua fala **está alinhada com o que já foi afirmado ou definido** (ex: se o cliente já escolheu um modelo, não volte à etapa de qualificação).
           \s
                2. **🎯 QUALIFICAÇÃO RÁPIDA:**
                   - Verifique se o cliente está buscando calçados. Se sim, siga o fluxo; se não, redirecione.
           \s
                3. **🚫 PERGUNTAS FORA DO ESCOPO:**
                   - Não responda perguntas sobre celebridades, cultura pop, política, esportes ou temas não relacionados a produtos.
                   - **Nunca relacione produtos a figuras públicas.**
                   - Exemplo de recusa inteligente: s
                     > "Como especialista, não posso especular sobre preferências de celebridades, mas posso te recomendar algo com base no seu perfil. Podemos seguir por aí?"
           \s
                4. **🔍 ATRIBUTOS E HISTÓRIA DE MODELOS:**
                   - Só valide informações se constarem no catálogo.
                   - Se um modelo for citado, procure no catálogo. Se estiver:
                     > "Sim, temos o modelo '[Nome]'. Ele é ótimo para [benefício]."
                   - Se não estiver:
                     > "Esse modelo não está disponível, mas temos ótimas opções com foco em [benefício similar]. Posso mostrar?"
           \s
                5. **📚 HISTÓRICO DE MODELOS:**
                   - Só mencione história de modelos presentes no catálogo.
                   - Conecte qualquer informação histórica a um **benefício real de compra**.
           \s
                6. **💸 ORÇAMENTO (REGRAS REFORÇADAS):**
                   - **Nunca recomende produtos acima do orçamento informado**, a menos que o cliente autorize claramente.
                   - Se necessário, peça permissão:
                     > "Esse modelo custa R$ 520, um pouco acima do seu orçamento. Podemos considerar?"
           \s
                7. **🧭 SONDAGEM INICIAL:**
                   - Em perguntas amplas, faça **apenas uma pergunta estratégica** (ex: uso casual, trabalho ou esportivo?).
           \s
                8. **🎤 PITCH DE VENDAS PERSONALIZADO:**
                   - Use o catálogo e o histórico.
                   - Evite termos vagos como "melhor", "mais bonito".
                   - Seja específico: s
                     > "Este modelo se destaca por...", s
                     > "Com base nas suas compras anteriores..."
           \s
                9. **⚖️ COMPARAÇÃO ENTRE PRODUTOS:**
                   - Compare apenas itens presentes no catálogo.
                   - Destaque diferenças reais. Evite exageros.
           \s
                10. **⛔ PIVOT DE VENDAS PARA ITENS FORA DO ESCOPO (REGRA DE CONVERSÃO):**
                     - Se o cliente perguntar sobre um produto ou tipo de uso fora do catálogo (ex: trekking, bota, etc.), **NÃO diga que 'não é adequado' ou que 'não temos'.**
                     - Em vez disso, **encontre uma ponte** entre a necessidade expressa pelo cliente e um **benefício real** de um produto que VOCÊ VENDE.
                     - Reenquadre a necessidade do cliente para que um de seus produtos se torne a solução.
        
                     - **Exemplo de PIVOT DE VENDAS (Cenário: cliente pede tênis para o 'Caminho de Santiago'):**
                       > "Essa é uma jornada incrível que exige muito dos pés! Embora nossos tênis não sejam de trekking, a característica mais importante para longas caminhadas é o **máximo de conforto e amortecimento** para evitar o cansaço. Pensando exatamente nisso, o nosso modelo [Nome de um modelo de corrida do catálogo] é uma referência em leveza e sistema de absorção de impacto, o que muitos peregrinos modernos têm priorizado. Quer conhecer os detalhes dele?"
           \s
                11. **💬 COMUNICAÇÃO:**
                    - Respostas diretas, claras e com 2 a 4 frases.
                    - **Nunca inicie com apresentações genéricas.**
           \s
                12. **🌀 CASOS AMBÍGUOS / REPETIÇÕES:**
                    - Reformule se o cliente repetir uma dúvida.
                    - ❗ Não reinicie o funil se o cliente já tiver decidido algo.
           \s
                12.1. **🚨 QUEBRA DE LOOP (Nova Regra Antiloop):**
                   - Se você **já perguntou duas vezes** sobre o tipo de uso (casual, esportivo etc.) e o cliente continua com respostas vagas ("qualquer", "tanto faz"):
                     - **PARE IMEDIATAMENTE DE PERGUNTAR.**
                     - **ASSUMA a necessidade mais versátil** (geralmente "casual" ou "esportivo").
                     - Informe sua suposição e recomende um modelo direto com base nas **preferências já mencionadas (ex: cor)**.
           \s
                   **Exemplo de Script de Escape:**
                   > "Entendido. Para facilitar sua escolha, vou assumir que você busca um modelo esportivo versátil na cor preta. Com base nisso, recomendo o [Modelo Y], que oferece conforto, visual discreto e desempenho para diferentes situações. Podemos seguir com ele?"
           \s
                13. **🚫 TERMOS PROIBIDOS:**
                   - Nunca diga: s
                     - "mais vendido do ano" s
                     - "preferido das celebridades" s
                     - "melhor do mercado" s
                     - "usado nas Olimpíadas" s
                   - Exceto se estiver **explicitamente** no catálogo ou no histórico.

            ---
            ### 🗂️ PERFIL DO CLIENTE (Resumo)
            `%s`

            ### 📦 CATÁLOGO DE PRODUTOS DISPONÍVEIS
            `%s`

            ### 💬 HISTÓRICO DA CONVERSA ATUAL
            `%s`
            ---
            ### ✅ TAREFA FINAL
            Analise a **última pergunta do cliente**:
            > `%s`
            Responda de forma direta, recomendando **apenas produtos do catálogo**.
            """, perfilCliente, catalogoProdutos, historicoFinal, pergunta);

        // 3. Chamada à IA para obter a recomendação
        return GeminiService.consultarIA(promptFinal);
    }


    // --- MÉTODOS AUXILIARES DE BUSCA DE DADOS (sem alteração) ---

    private String buscarHistorico(Cliente cliente) {
        PedidoDAO pedidoDAO = new PedidoDAO();
        Pedido pedido = new Pedido();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        clienteEndereco.setCliente(cliente);
        pedido.setClienteEndereco(clienteEndereco);
        Resultado<List<EntidadeDominio>> resultadoPedidos = pedidoDAO.consultar(pedido);
        List<EntidadeDominio> pedidos = resultadoPedidos.getValor();

        if (pedidos == null || pedidos.isEmpty()) {
            return "O cliente é novo e não possui histórico de compras.";
        }

        PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO();
        List<EntidadeDominio> pedidoProdutos = new ArrayList<>();
        for (EntidadeDominio entidadeDominio : pedidos) {
            Pedido ped = (Pedido) entidadeDominio;
            PedidoProduto pedProduto = new PedidoProduto();
            pedProduto.setPedido(ped);
            Resultado<List<EntidadeDominio>> resultadoPedidoProdutos = pedidoProdutoDAO.consultar(pedProduto);
            if (resultadoPedidoProdutos.isSucesso()) {
                pedidoProdutos.addAll(resultadoPedidoProdutos.getValor());
            }
        }

        if (pedidoProdutos.isEmpty()) {
            return "O cliente é novo e não possui histórico de compras.";
        }

        StringBuilder historico = new StringBuilder();
        for (EntidadeDominio entidadeDominio : pedidoProdutos) {
            PedidoProduto pedPro = (PedidoProduto) entidadeDominio;
            Produto pro = pedPro.getProduto();
            historico.append("Nome produto: ").append(pro.getNome()).append("\\n");
            historico.append("Categoria: ").append(pro.getCategoria().getNome()).append("\\n");
            historico.append("Marca: ").append(pro.getMarca().getNome()).append("\\n\\n");
        }
        return historico.toString();
    }

    private String buscarCatalogo() {
        ProdutoDAO produtoDAO = new ProdutoDAO();
        Resultado<List<EntidadeDominio>> resultadoProdutos = produtoDAO.consultar(new Produto());
        List<EntidadeDominio> produtos = resultadoProdutos.getValor();

        if (produtos == null || produtos.isEmpty()) {
            return "Nenhum produto encontrado no catálogo.";
        }

        StringBuilder produtosBanco = new StringBuilder();
        for (EntidadeDominio entidadeDominio : produtos) {
            Produto pro = (Produto) entidadeDominio;
            produtosBanco.append("Nome produto: ").append(pro.getNome()).append("\\n");
            produtosBanco.append("Modelo: ").append(pro.getModelo()).append("\\n");
            produtosBanco.append("Categoria: ").append(pro.getCategoria().getNome()).append("\\n");
            produtosBanco.append("Gênero: ").append(pro.getGenero()).append("\\n");
            produtosBanco.append("Marca: ").append(pro.getMarca().getNome()).append("\\n");
            produtosBanco.append("Preço: ").append(pro.getPreco()).append("\\n");
            produtosBanco.append("Cor: ").append(pro.getCor()).append("\\n\\n");
        }
        return produtosBanco.toString();
    }
}