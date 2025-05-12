document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const container = document.getElementById("carrinho-itens");
    const subtotalEl = document.getElementById("subtotal");
    const totalEl = document.getElementById("total");
    const sessionId = sessionStorage.getItem("sessionId");

    let subtotal = 0;

    const renderCarrinho = () => {
        container.innerHTML = "";
        subtotal = 0;

        carrinho.forEach((produto, index) => {
            const preco = parseFloat(produto.preco) * produto.quantidade;
            subtotal += preco;

            const item = document.createElement("div");
            item.className = "item-carrinho";

            item.innerHTML = `
                <img src="${produto.imagem}" alt="img-tenis">
                <div class="item-carrinho-container">
                    <span>${produto.sku}</span>
                    <h4>${produto.nome}</h4>
                    <h6>R$ ${produto.preco.toFixed(2).replace(".", ",")}</h6>
                    <span>Tamanho: ${produto.tamanho}</span>
                    <input type="number" value="${produto.quantidade}" min="1" data-index="${index}">
                    <button class="remover-item" data-index="${index}" style="margin-top: 10px; background-color: red; color: white; border: none; padding: 5px 10px; cursor: pointer;">
                        Remover
                    </button>
                </div>
            `;

            container.appendChild(item);
        });

        subtotalEl.innerText = `R$ ${subtotal.toFixed(2).replace(".", ",")}`;
        totalEl.innerText = subtotalEl.innerText;
        sessionStorage.setItem("totalCarrinho", subtotal.toFixed(2));
    };

    const atualizarQuantidade = (index, novaQtd) => {
        if (novaQtd < 1) return;
        carrinho[index].quantidade = novaQtd;
        sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
        renderCarrinho();
    };

    const removerItem = (index) => {
        carrinho.splice(index, 1);
        sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
        renderCarrinho();
    };

    const removerItemSilencioso = (index) => {
        carrinho.splice(index, 1);
        sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
    };

    const removerMultiplosItens = (indices) => {
        // Remove de trás para frente para não alterar os índices
        indices.sort((a, b) => b - a).forEach(i => removerItemSilencioso(i));
        renderCarrinho();
    };

    container.addEventListener("input", (e) => {
        if (e.target.matches("input[type='number']")) {
            const index = parseInt(e.target.dataset.index);
            const novaQtd = parseInt(e.target.value);
            atualizarQuantidade(index, novaQtd);
        }
    });

    container.addEventListener("click", (e) => {
        if (e.target.classList.contains("remover-item")) {
            const index = parseInt(e.target.dataset.index);
            removerItem(index);
        }
    });

    document.querySelector("button").addEventListener("click", async () => {
        try {
            // Verifica se há produtos no carrinho
            if (!carrinho || carrinho.length === 0) {
                alert('Carrinho vazio!');
                return;
            }

            // Estrutura correta do JSON conforme requisito
            const payload = {
                reserva: carrinho.map(item => ({
                    produto: { id: item.idTenis },
                    marca: { id: item.marca },
                    quantidade: item.quantidade
                }))
            };

            // Envia uma única requisição com todos os itens
            const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque', {
                method: 'POST',
                body: JSON.stringify(payload),
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.status === 201) {
                const sessionId = await response.json();  // Recebe a string diretamente

                if (sessionId) {
                    sessionStorage.setItem("sessionId", sessionId);
                    console.log('[Reserva] sessionId armazenado:', sessionId);
                }

                window.location.href = "../../vendas/cliente/clienteIdentificacao.html";
            } else {
                const data = await response.json();
                const errorMsg = data.erro || data.message || 'Erro desconhecido ao reservar estoque';
                alert(errorMsg);
            }

        } catch (error) {
            console.error("Erro durante o processo de reserva:", error);
            alert("Ocorreu um erro inesperado durante a reserva do estoque.");
        }
    });

    const verificarReservas = async () => {
        console.log('Iniciando verificação de reservas...');
        console.log('sessionId:', sessionId);
        console.log('carrinho:', carrinho);

        if (!sessionId || !carrinho.length) {
            console.log('Retornando - sessionId ou carrinho vazio');
            return;
        }

        try {
            console.log('Fazendo requisição para verificar status das reservas...');
            const url = `http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque`;
            console.log('URL:', url);

            const res = await fetch(url);
            console.log('Resposta recebida, status:', res.status);
            if (!res.ok) {
                console.log('Retornando - resposta não OK');
                return;
            }

            const reservas = await res.json();
            console.log('Reservas recebidas:', reservas);

            // Coleta os IDs de produtos com reserva e os que têm reserva ativa
            const idsComReserva = reservas.map(r => r.produto.id);
            const idsComReservaAtiva = reservas
                .filter(r => r.status === "ATIVO")
                .map(r => r.produto.id);

            console.log('IDs com reserva:', idsComReserva);
            console.log('IDs com reserva ativa:', idsComReservaAtiva);

            const indicesParaRemover = [];
            const skusRemovidos = [];

            carrinho.forEach((item, index) => {
                console.log(`Verificando item ${index}:`, item);

                // Se o produto tem reserva, mas não está ativa, removemos
                if (idsComReserva.includes(item.idTenis) && !idsComReservaAtiva.includes(item.idTenis)) {
                    console.log(`Item ${item.sku} tem reserva expirada - marcado para remoção`);
                    indicesParaRemover.push(index);
                    skusRemovidos.push(item.sku);
                } else {
                    console.log(`Item ${item.sku} está OK - mantido no carrinho`);
                }
            });

            console.log('Índices para remover:', indicesParaRemover);
            console.log('SKUs removidos:', skusRemovidos);

            if (indicesParaRemover.length > 0) {
                console.log('Removendo itens do carrinho...');
                removerMultiplosItens(indicesParaRemover);
                alert(`Os seguintes produtos foram removidos do carrinho por terem a reserva expirada:\n\n${skusRemovidos.join("\n")}`);
            } else {
                console.log('Nenhum item para remover - todas as reservas estão ativas');
            }
        } catch (err) {
            console.error("Erro ao verificar reservas:", err);
        } finally {
            console.log('Verificação de reservas concluída');
        }
    };

    void verificarReservas();
    setInterval(verificarReservas, 30000);
    renderCarrinho();
});