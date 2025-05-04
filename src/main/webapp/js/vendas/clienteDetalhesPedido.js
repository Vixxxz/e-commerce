addEventListener("DOMContentLoaded", () => {
    carregarDadosPedidos();
});

function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}

const id = obterParametroUrl("id");

async function carregarDadosPedidos(){
    const container = document.querySelector('.detalhes-pedido');
    const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controlePedido?id=${id}`;

    try{
        const response = await fetch(url);
        const pedidos = await response.json();
        container.innerHTML = "";

        pedidos.forEach(item => {
            const dataHora = item.dtCadastro;
            const objData = new Date(dataHora);
            const dataPedido = objData.toLocaleDateString("pt-BR", { year: 'numeric', month: '2-digit', day: '2-digit' });

            const table = document.createElement("table");
            table.setAttribute("data-id", item.id);

            table.innerHTML = `
                <thead>
                    <tr>
                      <th>Nº Pedido</th>
                      <th>Data</th>
                      <th>Valor</th>
                      <th>Detalhes</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                      <td>${item.id}</td>
                      <td>${dataPedido}</td>
                      <td>R$ ${item.valorTotal}</td>
                      <td><button class="btn-detalhes" >Ver</button></td>
                    </tr>
                </tbody>
            `;

            addEventListener("click", (event) => {
                window.location.href = `clienteDetalhesPedido.html?id=${item.id}`;
            })

            container.appendChild(table);
        })

    }catch(erro){
        console.error("Erro ao buscar endereços:", erro);
        container.innerHTML = "<p style='color:red;'>Erro ao carregar endereços.</p>";
    }
}