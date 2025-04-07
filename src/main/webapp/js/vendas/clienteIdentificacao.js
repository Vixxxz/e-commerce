document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const totalCarrinho = parseFloat(sessionStorage.getItem("totalCarrinho")) || 0;
    const produtosContainer = document.querySelector(".info-pedido-produtos");
    const totalSpan = document.querySelector(".info-pedido-container span:last-child");
    const produtosSpan = document.querySelector(".info-pedido-container span:first-child + span");
    const btnProsseguir = document.querySelector("button");
    const cpfInput = document.getElementById("cpf-ident");

    const renderProdutosResumo = () => {
        produtosContainer.innerHTML = ""; // Limpa os produtos fixos

        carrinho.forEach(produto => {
            const wrapper = document.createElement("div");
            wrapper.className = "info-pedido-produto-wrapper";

            wrapper.innerHTML = `
            <div class="info-pedido-produto">
                <img src="${produto.imagem}" alt="img-tenis">
                <div class="info-pedido-produto-container">
                    <span>${produto.sku}</span>
                    <h6>${produto.nome}</h6>
                    <p>R$ ${parseFloat(produto.preco).toFixed(2).replace(".", ",")}</p>
                    <span>Tamanho: ${produto.tamanho}</span>
                </div>
            </div>
        `;

            produtosContainer.appendChild(wrapper);
        });

        produtosSpan.innerText = `R$ ${totalCarrinho.toFixed(2).replace(".", ",")}`;
        totalSpan.innerText = `R$ ${totalCarrinho.toFixed(2).replace(".", ",")}`;
    };

    const montarEstruturaInicialPedido = async () => {
        const cpf = cpfInput.value.trim();

        if (!cpf) {
            alert("Digite um CPF válido.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/ecommerce_tenis_war_exploded/controlecliente?cpf=${encodeURIComponent(cpf)}`);
            if (!response.ok) throw new Error("Erro ao buscar cliente");

            const clienteData = await response.json();
            const cliente = clienteData[0]?.cliente || clienteData[0]; // adaptável ao seu backend

            // JSON do pedido inicial
            const pedidoJson = {
                pedido: {
                    valorTotal: totalCarrinho,
                    status: "APROVADA",
                    cliente: {
                        id: cliente.id
                    }
                },
                PedidoProdutos: carrinho.map(prod => ({
                    produto: {
                        id: prod.id,
                        marca: {
                            id: prod.marca_id || 1
                        },
                        categoria: {
                            id: prod.categoria_id || 1
                        }
                    },
                    quantidade: prod.quantidade
                }))
            };

            sessionStorage.setItem("pedidoJson", JSON.stringify(pedidoJson));
            window.location.href = "../../vendas/cliente/clienteEntrega.html";

        } catch (err) {
            console.error("Erro ao montar pedido:", err);
            alert("Erro ao buscar cliente. Verifique o CPF.");
        }
    };

    btnProsseguir.addEventListener("click", montarEstruturaInicialPedido);

    renderProdutosResumo();
});
