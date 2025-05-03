document.addEventListener("DOMContentLoaded", () => {
    const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
    const pedido = JSON.parse(sessionStorage.getItem("pedidoJson")) || [];
    const produtosContainer = document.querySelector(".info-pedido-produtos");
    const totalSpan = document.getElementById("total-geral");
    const produtosSpan = document.getElementById("produtos-total");
    const btnProsseguir = document.getElementById("btnFinalizar");
    const formCupom = document.getElementById("form-cupom");

    formCupom.addEventListener("submit", async (event) => {
        event.preventDefault();

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

    //todo: descontar o valor do cupom escolhido no valor total
    const renderProdutosResumo = () => {
        produtosContainer.innerHTML = "";

        carrinho.forEach(produto => {
            const wrapper = document.createElement("div");
            wrapper.className = "info-pedido-produto-wrapper";

            const preco = sessionStorage.getItem("totalCarrinho");

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
        totalSpan.innerText = `R$ ${pedido.pedido.valorTotal.toFixed(2).replace(".", ",")}`;
    };

    async function carregarCuponsPromocional(codCupom) {
        const container = document.querySelector(".cupom-input.col #resultado-cupom");
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleCupom?codigo=${codCupom}&tipo=PROMOCIONAL`;

        try {
            const resposta = await fetch(url);
            const cupom = await resposta.json();

            container.innerHTML = "";

            if (!cupom || cupom.length === 0) {
                container.innerHTML = "<p style='color: red;'>Cupom não encontrado.</p>";
                return;
            }

            cupom.forEach((item, index) => {
                const div = document.createElement("div");
                div.classList.add("cupom-radio-container");

                const checked = index === 0 ? "checked" : "";

                const valor = item.valor;

                div.innerHTML = `
                <input type="radio" name="cupomPromocional" value="${item.id}" ${checked}>
                <span class="circulo"></span>
                <span>R$ ${valor}</span>
            `;
                div.addEventListener("click", () => {
                    div.classList.toggle("selecionado");
                });
                container.appendChild(div);
            });

        } catch (erro) {
            console.error("Erro ao buscar cupom:", erro);
            container.innerHTML = "<p style='color:red;'>Erro ao carregar cupom.</p>";
        }
    }

    //todo: verificar exibição dos cupons troca
    async function carregarCuponsTroca() {
        const idCliente = pedido?.pedido?.clienteEndereco?.cliente?.id;
        if (!idCliente) return;

        const container = document.querySelector(".cupom-radio.col");
        const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controleCupom?idCliente=${idCliente}&tipo=TROCA&status=ATIVO`;

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

                const checked = index === 0 ? "checked" : "";

                div.innerHTML = `
                <input type="radio" name="cupomTroca" value="${item.id}" ${checked}>
                <span class="circulo"></span>
                <span>R$ ${item.valor}</span>
            `;
                div.addEventListener("click", () => {
                    div.classList.toggle("selecionado");
                });
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
              <!--<input type="number" class="valor-input" min="10" placeholder="Valor (mín. R$10)" />-->
            </div>
          `;

                // const input = div.querySelector(".valor-input");

                // input.addEventListener("input", () => {
                //     const valor = parseFloat(input.value);
                //     if (valor < 10 && valor !== 0) {
                //         input.setCustomValidity("Valor mínimo é R$10 por cartão.");
                //     } else {
                //         input.setCustomValidity("");
                //     }
                // });

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

    function obterCupomTrocaSelecionado() {
        const selecionado = document.querySelector('input[name="cupomTroca"]:checked');
        if (!selecionado) return null;

        const codigo = selecionado.value;
        const container = selecionado.closest(".cupom-radio-container");
        const valorSpan = container?.querySelector("span:nth-of-type(2)");
        const valorTexto = valorSpan?.textContent?.replace(/[^\d,.-]+/g, "").replace(",", ".");
        const valor = valorTexto ? parseFloat(valorTexto) : null;

        return {
            codigo,
            valor
        };
    }

    function obterCupomPromocionalSelecionado() {
        const selecionado = document.querySelector('input[name="cupomPromocional"]:checked');
        if (!selecionado) return null;

        const codigo = selecionado.value;
        const container = selecionado.closest(".cupom-radio-container");
        const valorSpan = container?.querySelector("span:nth-of-type(2)");
        const valorTexto = valorSpan?.textContent?.replace(/[^\d,.-]+/g, "").replace(",", ".");
        const valor = valorTexto ? parseFloat(valorTexto) : null;

        return {
            codigo,
            valor
        };
    }


    //todo: verificar envio do copum junto ao pedido
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

            const cupons = [];

            const cupomSelecionado = obterCupomTrocaSelecionado();
            if (cupomSelecionado) {
                cupons.push({
                    tipo: "TROCA",
                    id: cupomSelecionado.codigo,
                    valor: cupomSelecionado.valor
                });
            }

            const cupomPromocional = obterCupomPromocionalSelecionado();
            if (cupomPromocional) {
                cupons.push({
                    tipo: "PROMOCIONAL",
                    id: cupomPromocional.codigo,
                    valor: cupomPromocional.valor
                });
            }

            pedido.cupons = cupons;

            sessionStorage.setItem("pedidoJson", JSON.stringify(pedido));
            console.log(JSON.stringify(pedido));

            const resposta = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/controlePedido', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: sessionStorage.getItem("pedidoJson")
            });

            console.log('pedido gerado, salvando...');
            if (!resposta.ok) {
                const mensagemErro = await resposta.text(); // Captura o texto completo
                // Tente processar como JSON, se possível
                try {
                    const erroDetalhado = JSON.parse(mensagemErro);
                    throw new Error(erroDetalhado.erro || "Erro ao finalizar pedido.");
                } catch {
                    // Se não for um JSON, lança o texto como erro
                    throw new Error(mensagemErro);
                }
            }

            const payload = {
                reserva: carrinho.map(item => ({
                    produto: { id: item.idTenis },
                    marca: { id: item.marca },
                    quantidade: item.quantidade
                }))
            };

            const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque', {
                method: 'PUT',
                body: JSON.stringify(payload),
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.status !== 201) {
                const data = await response.json();
                const errorMsg = data.erro || data.message || 'Erro desconhecido ao concluir reserva';
                alert(errorMsg);
            }

            console.log('pedido salvo com sucesso!');
            alert('pedido salvo com sucesso!')
            window.location.href = "../../vendas/cliente/clientePagamento.html";

        } catch (err) {
            console.error("Erro ao montar pedido:", err);

            let errorMessage = "Erro desconhecido ao montar o pedido.";

            if (err.message.includes("{")) {
                try {
                    const parsedError = JSON.parse(err.message);
                    errorMessage = parsedError.erro || "Erro desconhecido na resposta do servidor.";
                } catch (parseError) {
                    console.error("Erro ao processar JSON na mensagem de erro:", parseError);
                }
            } else if (err instanceof TypeError) {
                errorMessage = "Erro interno na aplicação";
            } else {
                errorMessage = err.message || errorMessage;
            }

            alert("Erro ao montar o pedido: " + errorMessage);
        }

    };

    btnProsseguir.addEventListener("click", finalizarPedido);

    carregarCuponsTroca();
    carregarCartoes();
    renderProdutosResumo();
});