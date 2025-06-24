//todo: Revisar remover do carrinho, revisar se esta funcionando direito
document.addEventListener("DOMContentLoaded", () => {
    let carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const container = document.getElementById("carrinho-itens");
    const subtotalEl = document.getElementById("subtotal");
    const totalEl = document.getElementById("total");
    const sessionId = sessionStorage.getItem("sessionId");

    /**
     * Função auxiliar para enviar atualizações de reserva para o backend.
     * Ela será usada para atualizar quantidade ou remover um item.
     */
    const atualizarReservaBackend = async (produto, novaQuantidade) => {
        const payload = {
            reserva: [{
                produto: { id: produto.idTenis },
                marca: { id: produto.marca },
                quantidade: novaQuantidade
            }]
        };

        try {
            // Usamos o metodo POST, pois nossa API já está configurada
            // para criar ou atualizar com base na existência da reserva.
            const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque', {
                method: 'POST',
                body: JSON.stringify(payload),
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) {
                const erro = await response.json();
                console.error("Erro ao atualizar reserva no backend:", erro.erro);
                // Opcional: Reverter a mudança no frontend se o backend falhar
                // renderCarrinho();
                return false;
            }

            const returnedSessionId = await response.json();
            if (returnedSessionId && !sessionStorage.getItem("sessionId")) {
                sessionStorage.setItem("sessionId", returnedSessionId);
            }

            return true;
        } catch (error) {
            console.error("Erro de comunicação ao atualizar reserva:", error);
            return false;
        }
    };

    // Função para renderizar os itens do carrinho na tela
    const renderCarrinho = () => {
        // Esta função não precisa de alteração, pois ela lerá a variável 'carrinho' do escopo superior.
        container.innerHTML = "";
        let subtotal = 0;

        // Agora, quando esta função for chamada, 'carrinho' terá o valor mais recente.
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
                    <h6>R$ ${parseFloat(produto.preco).toFixed(2).replace(".", ",")}</h6>
                    <span>Tamanho: ${produto.tamanho}</span>
                    <input type="number" value="${produto.quantidade}" min="1" data-index="${index}" class="quantidade-item">
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

    const atualizarQuantidade = async (index, novaQtd) => {
        if (novaQtd < 1) return;

        const produto = carrinho[index];
        const sucesso = await atualizarReservaBackend(produto, novaQtd);

        if (sucesso) {
            carrinho[index].quantidade = novaQtd;
            sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
            renderCarrinho();
        } else {
            alert("Não foi possível atualizar a quantidade do item. Tente novamente.");
            // Recarrega o carrinho do sessionStorage para reverter a mudança visual
            renderCarrinho();
        }
    };

    const removerItem = async (index) => {
        const produto = carrinho[index];
        // Para remover, podemos simplesmente atualizar a quantidade da reserva para 0
        const sucesso = await atualizarReservaBackend(produto, 0);

        if (sucesso) {
            carrinho.splice(index, 1);
            sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
            renderCarrinho();
        } else {
            alert("Não foi possível remover o item. Tente novamente.");
        }
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

    // O botão "PROSSEGUIR" agora apenas redireciona, pois as reservas já estão sincronizadas
    document.querySelector("button").addEventListener("click", () => {
        if (!carrinho || carrinho.length === 0) {
            alert('Seu carrinho está vazio!');
            return;
        }
        window.location.href = "../../vendas/cliente/clienteIdentificacao.html";
    });

    /**
     * Esta é a função central que atende à sua necessidade.
     * Ela é chamada assim que a página do carrinho carrega e, depois, a cada 30 segundos.
     */
    const verificarReservas = async () => {
        if (!sessionId || !carrinho.length) {
            return;
        }

        try {
            const url = `http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque`;
            const res = await fetch(url);
            if (!res.ok) return;

            const reservasDoBackend = await res.json();

            const idsComReservaExpirada = reservasDoBackend
                .filter(r => r.status === "EXPIRADO")
                .map(r => r.produto.id);

            if (idsComReservaExpirada.length === 0) {
                return;
            }

            const skusRemovidos = [];

            // MUDANÇA 2: Em vez de criar uma nova constante, reatribua a variável 'carrinho' original.
            carrinho = carrinho.filter(item => {
                if (idsComReservaExpirada.includes(item.idTenis)) {
                    skusRemovidos.push(item.sku);
                    return false;
                }
                return true;
            });

            if (skusRemovidos.length > 0) {
                sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
                renderCarrinho(); // Agora esta chamada irá renderizar o carrinho CORRETO e atualizado.
                alert(`Os seguintes produtos foram removidos do seu carrinho pois a reserva de estoque expirou:\n\n- ${skusRemovidos.join("\n- ")}`);
            }
        } catch (err) {
            console.error("Erro ao verificar status das reservas:", err);
        }
    };


    // Renderiza o carrinho inicial e inicia as verificações
    renderCarrinho();
    void verificarReservas();
    setInterval(verificarReservas, 30000); // Verifica a cada 30 segundos
});