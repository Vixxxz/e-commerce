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
        for (const produto of carrinho) {
            try {
                const response = await fetch(`http://localhost:8080/ecommerce_tenis_war_exploded/controleEstoque?sku=${encodeURIComponent(produto.sku)}`);
                if (!response.ok) throw new Error("Erro ao buscar estoque");

                const dados = await response.json();
                const estoqueAtual = dados
                    .filter(item => item.produto.sku === produto.sku)
                    .reduce((total, item) => total + item.movimentacao, 0);

                if (estoqueAtual < produto.quantidade) {
                    alert("Tentando comprar mais do que disponível no estoque")
                    return;
                }
            } catch (error) {
                console.error("Erro ao verificar estoque:", error);
                alert("Erro ao verificar estoque.");
                return;
            }
        }

        window.location.href = "../../vendas/cliente/clienteIdentificacao.html";
    });

    renderCarrinho();
});