document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const container = document.getElementById("carrinho-itens");
    const subtotalEl = document.getElementById("subtotal");
    const totalEl = document.getElementById("total");

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
            const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/reservarEstoque', {
                method: 'POST',
                body: JSON.stringify(payload),
                headers: { 'Content-Type': 'application/json' }
            });

            const data = await response.json();

            if (!data.success) {
                alert('Erro ao reservar estoque: ' + (data.message || 'Erro desconhecido'));
                return;
            }

            // Redireciona para checkout se tudo estiver OK
            window.location.href = "../../vendas/cliente/clienteIdentificacao.html";

        } catch (error) {
            console.error("Erro durante o processo de reserva:", error);
            alert("Ocorreu um erro inesperado durante a reserva do estoque.");
        }
    });

    renderCarrinho();
});