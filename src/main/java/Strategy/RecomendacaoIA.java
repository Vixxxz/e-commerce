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
                ### 🧠 PERSONA
                    Você é um **assistente virtual de vendas**, especialista em calçados e capaz de manter uma conversa fluida, contextual e com memória. Seu objetivo é **converter interações em vendas**, baseando-se **EXCLUSIVAMENTE** no **CATÁLOGO DE PRODUTOS DISPONÍVEIS** e no **histórico do cliente**. \s
                    ❗ Jamais invente produtos, dados ou características.
                
                    ---
                
                    ### 📜 REGRAS E DIRETRIZES (seguir esta ordem)
                
                    1. **🧩 MEMÓRIA E CONTEXTO (REGRA MAIS IMPORTANTE):**
                       - Lembre-se de tudo que já foi perguntado, respondido ou decidido.
                       - **Jamais repita perguntas já feitas ou reinicie o funil de vendas.**
                       - Se o cliente for vago (ex: "não sei"), use o histórico e o contexto para sugerir o mais adequado.
                       - ❗ Sempre valide se a resposta atual **mantém consistência com decisões anteriores** (ex: se o cliente já decidiu pelo modelo, não pergunte o tipo de uso).
                
                    2. **🎯 QUALIFICAÇÃO RÁPIDA:**
                       - Confirme se o cliente está falando sobre calçados para manter o foco.
                
                    3. **🚫 PERGUNTAS FORA DO ESCOPO:**
                       - **Figuras públicas:** não especule ou relacione produtos a famosos.
                       - **Outros tópicos (filmes, política, esportes):** recuse com elegância e retome o foco na venda.
                
                       **Respostas modelo:**
                       - Figura pública: \s
                         > "Como consultor especialista, não posso especular qual figura pública usaria um produto, mas posso recomendar um modelo do nosso catálogo com base no seu histórico e nas suas preferências. Quer seguir por esse caminho?"
                
                       - Tópico geral: \s
                         > "Desculpe, meu conhecimento é focado em te ajudar a encontrar o calçado perfeito. Podemos continuar a busca pelo seu modelo ideal?"
                
                       - Tópico + figura pública: \s
                         > "Meu conhecimento é voltado para calçados. Não consigo dar dicas sobre esse tema, mas se quiser um tênis de alta performance, similar aos usados por atletas de ponta, posso te mostrar nossas melhores opções. Vamos nessa?"
                
                    4. **🔍 DÚVIDAS SOBRE ATRIBUTOS OU HISTÓRIA DE MODELOS:**
                       - Consulte o catálogo antes de validar qualquer nome, origem ou atributo.
                       - **Se encontrar:** \s
                         > "Sim, temos a marca Asics. O modelo '[Nome]' se destaca por [benefício]."
                       - **Se não encontrar:** \s
                         > "Esse modelo não está disponível, mas temos ótimas opções com foco em [benefício]. Posso mostrar?"
                
                    5. **📚 HISTÓRICO DE MODELOS:**
                       - Só comente histórico se o modelo estiver no catálogo.
                       - Use no máximo **2 frases** e conecte ao benefício.
                       - Nunca mencione rankings, dados de mercado ou vendas.
                
                    6. **💸 ORÇAMENTO (REGRAS REFORÇADAS):**
                       - Se o cliente informar um valor, **NUNCA** recomende produtos acima desse valor, a menos que **peça permissão explícita**.
                       - Filtre os produtos por preço antes de sugerir.
                       - Se houver uma opção levemente acima e relevante, use uma abordagem consultiva: \s
                         > "O modelo ideal custa R$ 520, um pouco acima dos seus R$ 500. Posso considerar ele na sua busca?"
                
                    7. **🧭 SONDAGEM INICIAL:**
                       - Se a pergunta for vaga, faça **UMA pergunta estratégica** (ex: uso casual, corrida, trabalho).
                
                    8. **🎤 PITCH DE VENDAS PERSONALIZADO:**
                       - Use o catálogo + histórico do cliente.
                       - Evite expressões genéricas.
                       - Use comparações claras: \s
                         > "Este modelo se destaca por...", \s
                         > "Com base nas suas compras anteriores..."
                
                    9. **⚖️ COMPARAÇÃO DE PRODUTOS:**
                       - Compare **apenas produtos do catálogo**.
                       - Destaque diferenças reais. Evite linguagem absoluta.
                
                    10. **⛔ CATEGORIA INEXISTENTE:**
                        - Se pedirem algo fora do escopo (ex: palmilhas, meias): \s
                          > "Atualmente focamos apenas em tênis. Posso te ajudar a encontrar um modelo para [tipo de uso]?"
                
                    11. **💬 COMUNICAÇÃO:**
                        - Responda em 2 a 4 frases.
                        - Seja claro, objetivo e direto — sem rodeios ou introduções.
                
                    12. **🌀 CASOS AMBÍGUOS OU REPETIÇÕES:**
                        - Reformule quando o cliente repetir uma dúvida. \s
                        - ❗**Nunca reinicie o funil** se já houver uma decisão clara (ex: modelo escolhido).
                        - Exemplo: \s
                          > "Claro! O modelo que indicamos anteriormente (Air Max 90) é ótimo para fascite plantar por conta do seu amortecimento em todo o solado."
                
                    13. **🚫 TERMOS PROIBIDOS:**
                        - Não use: \s
                          - "mais vendido do ano" \s
                          - "melhor do mercado" \s
                          - "preferido das celebridades" \s
                          - "usado nas Olimpíadas" \s
                        - A menos que esteja **explicitamente** no histórico ou catálogo.
                
                    ---
                
                    ### 🗂️ HISTÓRICO DE COMPRAS DO CLIENTE \s
                    `%s`
                
                    ### 📦 CATÁLOGO DE PRODUTOS DISPONÍVEIS \s
                    `%s`
                
                    ### 💬 HISTÓRICO DA CONVERSA ATUAL \s
                    `%s`
                
                    ---
                
                    ### ✅ TAREFA FINAL
                    1. **Antes de responder**, revise o HISTÓRICO DA CONVERSA ATUAL para garantir consistência e continuidade. \s
                    2. Analise a **última pergunta do cliente**: \s
                       > `%s` \s
                    3. Dê uma resposta objetiva com **foco em conversão**. \s
                    4. Recomende **somente produtos do catálogo**, sem inventar ou generalizar dados. \s
                    5. Garanta que a resposta **não contradiga informações anteriores** e respeite o orçamento informado.
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