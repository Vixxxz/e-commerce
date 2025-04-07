document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const pedido = JSON.parse(sessionStorage.getItem("pedidoJson")) || [];
    const produtosContainer = document.querySelector(".info-pedido-produtos");
    const totalSpan = document.getElementById("total-geral");
    const produtosSpan = document.getElementById("produtos-total");
    const btnProsseguir = document.getElementById("btnFinalizar");

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

            const preco = parseFloat(
                produto.preco.toString().replace("R$", "").replace(".", "").replace(",", ".")
            );

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

    async function carregarCupons() {
        const container = document.querySelector(".enderecos");
        const idCliente = pedido.pedido.clienteEndereco.cliente.id
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleEndereco?idCliente=${idCliente}&tipoEndereco=Entrega`;

        try {
            const resposta = await fetch(url);
            const enderecos = await resposta.json();
            container.innerHTML = "";

            enderecos.forEach(item => {
                const endereco = item.endereco;

                const div = document.createElement("div");
                div.classList.add("endereco-container");
                div.setAttribute("data-id", item.id);

                div.innerHTML = `
                    <img src="../../../img/local.svg" alt="pointer local">
                    <div class="endereco-info">
                        <span class="texto-gradient">CEP: ${endereco.cep}</span>
                        <span class="texto-gradient">Número: ${item.numero}</span>
                        <span class="texto-gradient">Logradouro: ${endereco.tipoLogradouro} ${endereco.logradouro}</span>
                    </div>
                `;

                div.addEventListener("click", () => {
                    document.querySelectorAll(".endereco-container").forEach(e => e.classList.remove("selecionado"));
                    div.classList.add("selecionado");
                    sessionStorage.setItem("enderecoSelecionado", item.id);
                    sessionStorage.setItem("enderecoID", endereco.id);
                });

                container.appendChild(div);
            });
        } catch (erro) {
            console.error("Erro ao buscar endereços:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar endereços.</p>";
        }
    }

    async function carregarCartoes() {
        const container = document.querySelector(".transportadoras");
        const url = "http://localhost:8080/ecommerce_tenis_war_exploded/controleFrete";

        try {
            const resposta = await fetch(url);
            const transportadoras = await resposta.json();
            container.innerHTML = "";

            transportadoras.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("agencia");
                div.setAttribute("data-id", item.id);
                div.setAttribute("data-valor", item.valor);

                div.innerHTML = `
                    <div class="agencia-info">
                        <span class="texto-gradient">${item.nome}</span>
                        <span class="texto-gradient">Valor: R$ ${item.valor.toFixed(2)}</span>
                    </div>
                `;

                div.addEventListener("click", () => {
                    document.querySelectorAll(".agencia").forEach(e => e.classList.remove("selecionado"));
                    div.classList.add("selecionado");
                    sessionStorage.setItem("transportadoraSelecionada", item.id);
                    atualizaPreco(item.valor); // atualiza totais com frete
                });

                container.appendChild(div);
            });
        } catch (erro) {
            console.error("Erro ao buscar transportadoras:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar transportadoras.</p>";
        }
    }

    const finalizarPedido = async () => {
        try {
            const transportadoraId = sessionStorage.getItem("transportadoraSelecionada");
            const clienteEnderecoId = sessionStorage.getItem("enderecoSelecionado");
            const enderecoId = sessionStorage.getItem("enderecoID");

            if (!transportadoraId || !clienteEnderecoId || !enderecoId) {
                alert("Selecione um endereço e uma transportadora antes de continuar.");
                return;
            }

            pedido.pedido.transportadora.id = transportadoraId;
            pedido.pedido.clienteEndereco.id = clienteEnderecoId;
            pedido.pedido.clienteEndereco.endereco.id = enderecoId;

            sessionStorage.setItem("pedidoJson", JSON.stringify(pedido));
            window.location.href = "../../vendas/cliente/clientePagamento.html";

        } catch (err) {
            console.error("Erro ao montar pedido:", err);
            alert("Erro ao montar o pedido");
        }
    };

    btnProsseguir.addEventListener("click", finalizarPedido);

    carregarCupons();
    carregarCartoes();
    renderProdutosResumo(); // carrega resumo sem frete inicialmente
});