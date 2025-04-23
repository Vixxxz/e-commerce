document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const pedido = JSON.parse(sessionStorage.getItem("pedidoJson")) || [];
    const produtosContainer = document.querySelector(".info-pedido-produtos");
    const totalSpan = document.getElementById("total-geral");
    const produtosSpan = document.getElementById("produtos-total");
    const btnProsseguir = document.getElementById("btnFinalizar");
    const formCupom = document.getElementById("form-cupom");

    formCupom.addEventListener("submit", async (event) => {
        event.preventDefault(); // Impede o refresh da página

        const codCupom = document.getElementById("promocional").value.trim();

        if (!codCupom) {
            alert("Digite um código de cupom.");
            return;
        }
        await carregarCuponsPromocional(codCupom);
    });

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
                produto.preco
                    .toString()
                    .replace(",", ".") // Substitui vírgula por ponto para o decimal
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
        totalSpan.innerText = `R$ ${pedido.valorTotal.toFixed(2).replace(".", ",")}`;
    };

    async function carregarCuponsPromocional(codCupom) {
        const container = document.querySelector(".cupom-input.col #resultado-cupom");
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleCupom?codigo=${codCupom}`;

        try {
            const resposta = await fetch(url);
            const cupom = await resposta.json();

            container.innerHTML = ""; // Limpa antes de adicionar novo conteúdo

            if (!cupom || cupom.length === 0) {
                container.innerHTML = "<p style='color: red;'>Cupom não encontrado.</p>";
                return;
            }

            cupom.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("cupom-detalhes");
                div.innerHTML = `
                <p><strong>Nome:</strong> ${item.nome}</p>
                <p><strong>Desconto:</strong> ${item.tipoDesconto === "PORCENTAGEM" ? item.valor + "%" : "R$ " + item.valor}</p>
                <p><strong>Validade:</strong> ${item.validade}</p>
            `;
                container.appendChild(div);
            });

        } catch (erro) {
            console.error("Erro ao buscar cupom:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar cupom.</p>";
        }
    }

    async function carregarCuponsTroca() {
        const idCliente = pedido?.pedido?.clienteEndereco?.cliente?.id;
        if (!idCliente) return;

        const container = document.querySelector(".cupom-radio.col");
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleCupom?idCliente=${encodeURIComponent(idCliente)}`;

        try {
            const resposta = await fetch(url);
            if (!resposta.ok) throw new Error("Erro ao buscar cupons");

            const cupons = await resposta.json();
            container.innerHTML = "<h5>Cupom Troca</h5>";

            if (!Array.isArray(cupons) || cupons.length === 0) {
                container.innerHTML += "<p style='color: red;'>Nenhum cupom disponível.</p>";
                return;
            }

            cupons.forEach((item, index) => {
                const div = document.createElement("div");
                div.classList.add("cupom-radio-container");

                const isValido = item.valido === true;
                const checked = index === 0 && isValido ? "checked" : "";
                const disabled = isValido ? "" : "disabled";

                div.innerHTML = `
                <input type="radio" name="cupom" value="${item.codigo}" ${checked} ${disabled}>
                <span class="circulo"></span>
                <span>${item.nome}</span>
            `;

                container.appendChild(div);
            });

        } catch (erro) {
            console.error("Erro ao buscar cupons:", erro);
            container.innerHTML += "<p style='color:red;'>Erro ao carregar cupons.</p>";
        }
    }

    async function carregarCartoes() {
        const idCliente = pedido?.pedido?.clienteEndereco?.cliente?.id;
        if (!idCliente) return;

        const container = document.querySelector("#cartoes");
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleCartao?idCliente=${idCliente}`;

        try {
            const resposta = await fetch(url);
            if (!resposta.ok) throw new Error("Erro na requisição de cartões");

            const cartoes = await resposta.json();
            container.innerHTML = "";

            cartoes.forEach(item => {
                const div = document.createElement("div");
                div.classList.add("cartao-container");
                div.setAttribute("data-id", item.id);

                div.innerHTML = `
            <img src="../../../img/credit-card.svg" alt="Cartão de crédito">
            <div class="card-info">
              <span class="texto-gradient">Bandeira: ${item.bandeira.nomeBandeira}</span>
              <span class="texto-gradient">Nome: ${item.nomeImpresso}</span>
              <span class="texto-gradient">Número: ${item.numero}</span>
              <input type="number" class="valor-input" min="10" placeholder="Valor (mín. R$10)" />
            </div>
          `;

                const input = div.querySelector(".valor-input");

                input.addEventListener("input", () => {
                    const valor = parseFloat(input.value);
                    if (valor < 10 && valor !== 0) {
                        input.setCustomValidity("Valor mínimo é R$10 por cartão.");
                    } else {
                        input.setCustomValidity("");
                    }
                });

                div.addEventListener("click", () => {
                    div.classList.toggle("selecionado");
                });

                container.appendChild(div);
            });
        } catch (erro) {
            console.error("Erro ao buscar cartoes:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar cartoes.</p>";
        }
    }

    function obterCartoesSelecionados() {
        const selecionados = document.querySelectorAll(".cartao-container.selecionado");

        return Array.from(selecionados).map(div => {
            const id = parseInt(div.getAttribute("data-id"));
            const input = div.querySelector(".valor-input");
            const valor = input ? parseFloat(input.value) : null;

            return {
                id: id,
                valor: valor
            };
        });
    }


    const finalizarPedido = async () => {
        try {
            const cartaoId = document.querySelector(".cartao-container.selecionado")
                .dataset.id;

            if (!cartaoId) {
                alert("Selecione um cartão antes de finalizar o pedido.");
                return;
            }

            const cartoesSelecionados = obterCartoesSelecionados();

            pedido.CartaoPedido = cartoesSelecionados.map(cartao => ({
                cartao: {
                    id: cartao.id
                },
                valor: cartao.valor
            }));

            sessionStorage.setItem("pedidoJson", JSON.stringify(pedido));
            console.log(JSON.stringify(pedido));

            // const resposta = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/controlePedido', {
            //     method: 'POST',
            //     headers: {
            //         'Content-Type': 'application/json'
            //     },
            //     body: sessionStorage.getItem("pedidoJson")
            // });
            //
            // console.log('pedido gerado, salvando...');
            // if (!resposta.ok) throw new Error("Erro ao finalizar pedido");
            // console.log('pedido salvo com sucesso!');
            // window.location.href = "../../vendas/cliente/clientePagamento.html";

        } catch (err) {
            console.error("Erro ao montar pedido:", err);
            alert("Erro ao montar o pedido");
        }
    };

    btnProsseguir.addEventListener("click", finalizarPedido);

    carregarCuponsTroca();
    carregarCartoes();
    renderProdutosResumo();
});