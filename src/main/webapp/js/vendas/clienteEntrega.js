document.addEventListener("DOMContentLoaded", async () => {
    const pedidoJson = JSON.parse(sessionStorage.getItem("pedidoJson"));
    const idCliente = pedidoJson?.pedido?.cliente?.id;
    const enderecoContainer = document.querySelector(".enderecos");
    const entregaContainer = document.querySelector(".tipo-entrega");
    const btnProsseguir = document.querySelector("button");

    let enderecoSelecionadoId = null;
    let transportadoraSelecionadaId = null;

    const renderEnderecos = async () => {
        try {
            const res = await fetch(`http://localhost:8080/ecommerce_tenis_war_exploded/controleEndereco?idCliente=${idCliente}&tipoEndereco=Entrega`);
            if (!res.ok) throw new Error("Erro ao buscar endereços");
            const enderecos = await res.json();

            enderecoContainer.innerHTML = "";

            enderecos.forEach(endereco => {
                const div = document.createElement("div");
                div.classList.add("endereco-container");
                div.innerHTML = `
                    <img src="../../../img/local.svg" alt="pointer local">
                    <div class="endereco-info">
                        <span class="texto-gradient">CEP: ${endereco.cep}</span>
                        <span class="texto-gradient">Número: ${endereco.numero}</span>
                        <span class="texto-gradient">Logradouro: ${endereco.logradouro}</span>
                        <span class="texto-gradient">Tipo Residência: ${endereco.tipoResidencia}</span>
                    </div>
                `;
                div.addEventListener("click", () => {
                    document.querySelectorAll(".endereco-container").forEach(e => e.classList.remove("selecionado"));
                    div.classList.add("selecionado");
                    enderecoSelecionadoId = endereco.id;
                });
                enderecoContainer.appendChild(div);
            });

        } catch (err) {
            console.error(err);
            alert("Erro ao carregar endereços.");
        }
    };

    const renderTransportadoras = async () => {
        try {
            const res = await fetch("http://localhost:8080/ecommerce_tenis_war_exploded/controleFrete");
            if (!res.ok) throw new Error("Erro ao buscar transportadoras");
            const transportadoras = await res.json();

            entregaContainer.innerHTML = "";

            transportadoras.forEach(transp => {
                const div = document.createElement("div");
                div.classList.add("agencia");
                div.innerHTML = `<span>${transp.nome}</span>`;
                div.addEventListener("click", () => {
                    document.querySelectorAll(".agencia").forEach(e => e.classList.remove("selecionado"));
                    div.classList.add("selecionado");
                    transportadoraSelecionadaId = transp.id;
                });
                entregaContainer.appendChild(div);
            });

        } catch (err) {
            console.error(err);
            alert("Erro ao carregar transportadoras.");
        }
    };

    btnProsseguir.addEventListener("click", () => {
        if (!enderecoSelecionadoId || !transportadoraSelecionadaId) {
            alert("Selecione um endereço e uma transportadora.");
            return;
        }

        pedidoJson.pedido.transportadora = {id: transportadoraSelecionadaId};
        pedidoJson.pedido.clienteEndereco = {
            endereco: {id: enderecoSelecionadoId},
            cliente: {id: idCliente}
        };

        sessionStorage.setItem("pedidoJson", JSON.stringify(pedidoJson));
        window.location.href = "pagamento.html"; // próxima tela
    });

    await renderEnderecos();
    await renderTransportadoras();
});