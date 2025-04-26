document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const produtosContainer = document.querySelector(".info-pedido-produtos");
    const totalSpan = document.getElementById("total-geral");
    const produtosSpan = document.getElementById("produtos-total");
    const btnProsseguir = document.querySelector("button");
    const cpfInput = document.getElementById("cpf-ident");

    const calcularTotalCarrinho = () => {
        return carrinho.reduce((total, produto) => {
            return total + (parseFloat(produto.preco) * (produto.quantidade || 1));
        }, 0);
    };

    const renderProdutosResumo = () => {
        produtosContainer.innerHTML = "";

        carrinho.forEach(produto => {
            const wrapper = document.createElement("div");
            wrapper.className = "info-pedido-produto-wrapper";

            console.log("preco antes de formatar: " + produto.preco);

            const preco = parseFloat(
                produto.preco
                    .toString()
                    .replace(",", ".") // Substitui vírgula por ponto para o decimal
            );

            console.log("o preco formato e: " + preco);

            wrapper.innerHTML = `
            <div class="info-pedido-produto">
                <img src="${produto.imagem}" alt="img-tenis">
                <div class="info-pedido-produto-container">
                    <span>${produto.sku}</span>
                    <h6>${produto.nome}</h6>
                    <p>R$ ${(preco * (produto.quantidade || 1)).toFixed(2).replace(".", ",")}</p>
                    <span>Tamanho: ${produto.tamanho}</span>
                    <span>Quantidade: ${produto.quantidade || 1}</span>
                </div>
            </div>
        `;

            produtosContainer.appendChild(wrapper);
        });

        const totalCarrinho = calcularTotalCarrinho();

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
            const cliente = clienteData[0]?.cliente || clienteData[0];

            console.log(carrinho);
            const pedidoJson = {
                pedido: {
                    valorTotal: null,
                    status: "APROVADA",
                    transportadora: {
                        id: null
                    },
                    clienteEndereco: {
                        id: null,
                        cliente: {
                            id: cliente.id
                        },
                        endereco: {
                            id: null
                        }
                    }
                },
                PedidoProdutos: carrinho.map((prod) => ({
                    produto: {
                        id: prod.idTenis,
                        marca: {
                            id: prod.marca
                        },
                        categoria: {
                            id: prod.categoria
                        }
                    },
                    quantidade: prod.quantidade
                })),
                CartaoPedido: [] // vazio por enquanto
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