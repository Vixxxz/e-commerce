package Strategy;

import Dao.PedidoDAO;
import Dao.PedidoProdutoDAO;
import Dao.ProdutoDAO;
import Dominio.*;
import Service.GeminiService;
import Util.Resultado;

import java.util.ArrayList;
import java.util.List;

public class RecomendacaoIA implements IStrategy{

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        // Seus TODOs originais ainda são válidos para futuras melhorias.
        //todo: separar as requisições. Uma para criar o perfil do cliente e outra pra fazer a recomendação.
        //todo: isso possibilita a diminuição do prompt
        if (!(entidade instanceof RequisicaoIA requisicao)) {
            return Resultado.erro("Entidade inválida para recomendação com IA.");
        }

        if ((requisicao.getCliente() == null || requisicao.getCliente().getCpf() == null) || requisicao.getPergunta() == null) {
            return Resultado.erro("CPF e pergunta devem ser informados.");
        }

        // A LÓGICA DE BUSCA DE DADOS FOI MANTIDA EXATAMENTE COMO ESTAVA
        PedidoDAO pedidoDAO = new PedidoDAO();
        Pedido pedido = new Pedido();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        clienteEndereco.setCliente(requisicao.getCliente());
        pedido.setClienteEndereco(clienteEndereco);
        Resultado<List<EntidadeDominio>> resultadoPedidos = pedidoDAO.consultar(pedido);
        List<EntidadeDominio> pedidos = resultadoPedidos.getValor();

        PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO();
        List<EntidadeDominio> pedidoProdutos = new ArrayList<>();
        if (pedidos != null) {
            for(EntidadeDominio entidadeDominio : pedidos){
                Pedido ped = (Pedido) entidadeDominio;
                PedidoProduto pedProduto = new PedidoProduto();
                pedProduto.setPedido(ped);
                Resultado<List<EntidadeDominio>> resultadoPedidoProdutos = pedidoProdutoDAO.consultar(pedProduto);
                if (resultadoPedidoProdutos.isSucesso()) {
                    pedidoProdutos.addAll(resultadoPedidoProdutos.getValor());
                }
            }
        }

        StringBuilder historico = new StringBuilder();
        for(EntidadeDominio entidadeDominio : pedidoProdutos){
            PedidoProduto pedPro = (PedidoProduto) entidadeDominio;
            Produto pro = pedPro.getProduto();
            historico.append("Quantidade: ").append(pedPro.getQuantidade()).append("\n");
            historico.append("Nome produto: ").append(pro.getNome()).append("\n");
            historico.append("Modelo produto: ").append(pro.getModelo()).append("\n");
            historico.append("Tamanho do produto: ").append(pro.getTamanho()).append("\n");
            historico.append("Categoria do produto: ").append(pro.getCategoria().getNome()).append("\n");
            historico.append("Gênero do produto: ").append(pro.getGenero()).append("\n");
            historico.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n\n");
        }

        ProdutoDAO produtoDAO = new ProdutoDAO();
        Produto produto = new Produto();
        Resultado<List<EntidadeDominio>> resultadoProdutos = produtoDAO.consultar(produto);
        List<EntidadeDominio> produtos = resultadoProdutos.getValor();

        StringBuilder produtosBanco = new StringBuilder();
        if (produtos != null) {
            for(EntidadeDominio entidadeDominio : produtos){
                Produto pro = (Produto) entidadeDominio;
                produtosBanco.append("Nome produto: ").append(pro.getNome()).append("\n");
                produtosBanco.append("Modelo produto: ").append(pro.getModelo()).append("\n");
                produtosBanco.append("Tamanho do produto: ").append(pro.getTamanho()).append("\n");
                produtosBanco.append("Categoria do produto: ").append(pro.getCategoria().getNome()).append("\n");
                produtosBanco.append("Gênero do produto: ").append(pro.getGenero()).append("\n");
                produtosBanco.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n\n");
                produtosBanco.append("Preço do produto: ").append(pro.getPreco()).append("\n");
            }
        }

        // 1. Preparamos as partes do prompt
        String historicoComprasFinal = historico.isEmpty()
                ? "O cliente é novo e não possui histórico de compras."
                : historico.toString();

        String produtosDisponiveis = produtosBanco.isEmpty()
                ? "Nenhum produto encontrado no catálogo."
                : produtosBanco.toString();

        String perguntaCliente = requisicao.getPergunta();

        // Pega o histórico da conversa que veio da requisição. Se for a primeira pergunta, será nulo.
        String historicoConversaAtual = requisicao.getHistoricoConversa() == null ? "" : requisicao.getHistoricoConversa();

        // 2. Montamos o prompt final usando o novo template estruturado
//        String promptFinal = String.format("""
//        ### PERSONA
//        Você é um assistente virtual especialista em calçados de uma loja online. Entenda "calçados" como a categoria geral que inclui todos os produtos para os pés, como tênis, sapatos, botas, sandálias, papetes, etc. Seja amigável, conversacional e direto ao ponto.
//
//        ### REGRAS E DIRETRIZES (Siga estritamente esta ordem)
//        1.  **FOCO EM CALÇADOS:** Sua função é discutir e recomendar exclusivamente calçados. Ignore outros tópicos na pergunta do cliente.
//        2.  **PERGUNTAS FORA DO ESCOPO:** APENAS se a pergunta for OBVIAMENTE sobre um tópico não relacionado a calçados (ex: "Qual o melhor filme?", "Qual a capital do Brasil?"), responda com o texto: "Desculpe, meu conhecimento é exclusivo sobre calçados. Posso te ajudar a encontrar o modelo ideal para você?".
//        3.  **PERGUNTA VAGA OU INCOMPLETA:** Se a pergunta for sobre calçados, mas for muito ampla ou faltarem detalhes essenciais para uma recomendação (ex: "Quero um tênis", "O que você tem de bom?", "Um calçado para mim"), faça uma pergunta para entender a necessidade do cliente. Exemplo: "Claro! Para te ajudar a encontrar o modelo perfeito, me diga para qual atividade ou ocasião você o usaria (corrida, caminhada, festa, dia a dia)?".
//        4.  **RECOMENDAÇÃO RESTRITA:** Após ter detalhes suficientes, recomende APENAS produtos que estão no "CATÁLOGO DE PRODUTOS DISPONÍVEIS".
//        5.  **PRODUTO NÃO ENCONTRADO:** Se, após a análise, você não encontrar NENHUM produto adequado no catálogo para a necessidade específica do cliente, responda: "No momento, não encontrei um calçado com essas características. Posso te ajudar com outro tipo de produto ou atividade?".
//        6.  **CONCISÃO:** Suas respostas devem ser curtas e objetivas, contendo no máximo 3 frases.
//        7.  **SEM APRESENTAÇÕES:** Não se apresente. Vá direto para a resposta.
//
//        ### CONTEXTO DO CLIENTE
//        **Histórico de Compras:**
//        %s
//
//        ### CATÁLOGO DE PRODUTOS DISPONÍVEIS
//        %s
//
//        ### TAREFA
//        1.  Analise a pergunta do cliente: "%s".
//        2.  Siga TODAS as "REGRAS E DIRETRIZES" acima, na ordem em que aparecem.
//        3.  Formule sua resposta diretamente.
//        """, historicoFinal, produtosDisponiveis, perguntaCliente);

        String promptFinal = String.format("""
            ### PERSONA
            Você é um assistente de vendas virtual, especialista em calçados e capaz de manter uma conversa fluida e com memória. Seu objetivo é convencer o cliente a comprar, transformando cada interação em uma oportunidade de venda. Você DEVE se basear **EXCLUSIVAMENTE** nas informações do "CATÁLOGO DE PRODUTOS DISPONÍVEIS" e do histórico, sem jamais inventar ou "alucinar" produtos, características ou dados de vendas.
            
            ### REGRAS E DIRETRIZES (Siga estritamente esta ordem)
            1. **MEMÓRIA E CONTEXTO (REGRA MAIS IMPORTANTE):** Lembre-se do que já foi perguntado e respondido na conversa atual para não se repetir. Se o cliente fornecer uma resposta vaga (ex: "não sei"), use o contexto que você já possui para tomar a melhor decisão por ele, assumindo a necessidade mais comum (ex: para uma longa caminhada, a prioridade é **amortecimento**). NÃO REPITA UMA PERGUNTA JÁ FEITA.
            2. **QUALIFICAÇÃO RÁPIDA:** Identifique se o cliente está falando de calçados para manter o foco na venda.
            3. **PERGUNTAS FORA DO ESCOPO:** Se a pergunta for sobre tópicos que não são produtos (filmes, política) OU pedir para associar produtos a figuras públicas (atletas, celebridades), RECUSE de forma inteligente e volte para a venda.
                * **Figura Pública:** Não especule nem mencione gostos de figuras públicas, mesmo de forma indireta. Se necessário, diga que não há como associar o produto a ninguém.
                * **Resposta modelo:** "Como consultor especialista, não posso especular qual figura pública usaria um produto, mas posso recomendar um modelo do nosso catálogo com base no seu histórico e nas suas preferências. Quer seguir por esse caminho?"
                * **Outros Tópicos:** "Desculpe, meu conhecimento é focado em te ajudar a encontrar o calçado perfeito. Podemos continuar a busca pelo seu modelo ideal?"
            
            4. **DÚVIDAS SOBRE ATRIBUTOS OU HISTÓRIA DE MODELOS:**
                a. Verifique o "CATÁLOGO DE PRODUTOS DISPONÍVEIS" para validar qualquer característica, origem ou nome de modelo citado.
                b. **SE ENCONTRAR:** Recomende o modelo exato. Exemplo: "Sim, temos a marca japonesa Asics no nosso catálogo. O modelo '[Nome no Catálogo]' se destaca por [benefício]."
                c. **SE NÃO ENCONTRAR:** Seja honesto e redirecione com foco. Exemplo: "Não temos esse tipo de modelo no momento, mas temos ótimas opções com foco em [benefício relevante]. Posso te mostrar?"
            
            5. **DADOS DE HISTÓRICO DE MODELOS:**
                a. Só comente a história de um modelo se ele estiver no catálogo.
                b. Use no máximo 2 frases e conecte o comentário a um benefício de compra real.
                c. Nunca mencione dados genéricos de mercado, rankings ou vendas globais.
            
            6. **LIDANDO COM ORÇAMENTO:** Se o cliente informar um valor limite, ofereça o melhor modelo dentro desse valor. Nunca ultrapasse o orçamento a menos que o cliente solicite explicitamente. Destaque o custo-benefício de forma objetiva.
            
            7. **SONDAGEM INICIAL:** Se a primeira pergunta for muito ampla, faça UMA pergunta estratégica para identificar necessidade (ex: uso casual, corrida, trabalho).
            
            8. **PITCH DE VENDAS PERSONALIZADO:** Baseie toda recomendação:
                - nos produtos do catálogo;
                - no histórico do cliente;
                - evitando termos genéricos como "mais bonito" ou "melhor do mercado".
                - Use linguagem comparativa e específica: "Este modelo se destaca por...", "Com base nas suas compras anteriores...".
            
            9. **COMPARAÇÃO DE PRODUTOS:** Compare apenas produtos do catálogo. Destaque diferenças reais e mensuráveis. Não use linguagem absoluta ("o melhor", "o mais...") a menos que a característica conste no catálogo.
            
            10. **CATEGORIA INEXISTENTE:** Se o cliente pedir algo fora do seu escopo (ex: chinelos, meias), seja claro e redirecione: "Atualmente focamos apenas em tênis de alta performance. Posso te ajudar a encontrar um modelo para [tipo de uso]?"
            
            11. **COMUNICAÇÃO:** Responda em 2 a 4 frases, sem apresentações ou rodeios. Clareza e foco são essenciais.
            
            12. **CASOS AMBÍGUOS:** Se houver múltiplos temas fora de escopo ou intenção pouco clara, reformule com uma pergunta que traga o cliente de volta ao funil de vendas.
            
            13. **TERMOS PERMITIDOS:** Não use frases como "mais vendido do ano", "melhor do mercado", "preferido das celebridades", "usado nas Olimpíadas", etc., a menos que isso esteja explicitamente no histórico do cliente ou no catálogo.
            
            ### HISTÓRICO DE COMPRAS DO CLIENTE
            %s
            
            ### CATÁLOGO DE PRODUTOS DISPONÍVEIS
            %s
            
            ### HISTÓRICO DA CONVERSA ATUAL
            %s
            
            ### TAREFA
            1. Analisar a ÚLTIMA pergunta do cliente ("%s") com base nos históricos acima.
            2. Formular uma resposta que direcione para a venda de um produto **presente no catálogo**.
            3. Garantir que nenhum dado seja alucinado ou generalizado.
            4. Responder com objetividade e foco em conversão.
            """, historicoComprasFinal, produtosDisponiveis, historicoConversaAtual, perguntaCliente);

        // 3. Fazemos uma ÚNICA chamada para a IA com o prompt completo e otimizado
        Resultado<String> resultadoIA = GeminiService.consultarIA(promptFinal);

        // A lógica de tratamento do resultado final foi mantida
        if (resultadoIA.isSucesso()) {
            requisicao.setResposta(resultadoIA.getValor());
            return Resultado.sucesso(resultadoIA.getValor());
        } else {
            String erro = "Erro na resposta da IA: " + resultadoIA.getErro();
            sb.append(erro);
            requisicao.setResposta(erro);
            return Resultado.erro(erro);
        }
    }
}