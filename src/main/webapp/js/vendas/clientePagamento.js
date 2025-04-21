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

    window.addEventListener('DOMContentLoaded', ()=>{
        carregarCupons();
        carregarCartoes();
        renderProdutosResumo(); //carregar produtos inicalmente
    })

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
        const container = document.querySelector("#cartoes");
        const url = "http://localhost:8080/ecommerce_tenis_war_exploded/controleCartao";

        try {
            const resposta = await fetch(url);
            const cartoes = await resposta.json();
            container.innerHTML = "";

            cartoes.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("endereco-container");
                //TODO: ver o que precisa do Cartao para passar
                div.setAttribute("data-id", item.id);

                div.innerHTML = `
                    <img src="../../../img/credit-card.svg" alt="pointer local">
                    <div class="card-info">
                        <span class="texto-gradient">Bandeira: ${item.bandeira.nomeBandeira}</span>
                        <span class="texto-gradient">Nome: ${item.nomeImpresso}</span>
                        <span class="texto-gradient">Número: ${item.numero}</span>
                    </div>
                `;

                div.addEventListener("click", () => {
                    document.querySelectorAll(".agencia").forEach(e => e.classList.remove("selecionado"));
                    div.classList.add("selecionado");
                });

                container.appendChild(div);
            });
        } catch (erro) {
            console.error("Erro ao buscar cartoes:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar cartoes.</p>";
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
            const resposta = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/controlePedido', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(sessionStorage)
            });
            window.location.href = "../../vendas/cliente/clienteConfirmcacao.html";

        } catch (err) {
            console.error("Erro ao montar pedido:", err);
            alert("Erro ao montar o pedido");
        }
    };

    btnProsseguir.addEventListener("click", finalizarPedido);
});