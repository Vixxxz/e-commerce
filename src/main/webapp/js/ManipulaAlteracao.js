const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";

function obterParametroUrl(id) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(id);
}

// Pegando o ID do cliente da URL
const clienteId = obterParametroUrl("id");
console.log("Cliente ID:", clienteId);

document.addEventListener("DOMContentLoaded", () => {
    if (clienteId) {
        carregarDadosCliente(clienteId);
    }
});


async function carregarDadosCliente(id) {
    try {
        const response = await fetch(`${BASE_URL}/controlecliente?id=${id}`);
        const cliente = await response.json();

        console.log("Cliente recebido:", cliente);
        preencherFormulario(cliente);
    } catch (error) {
        console.error("Erro ao carregar cliente:", error);
    }
}

function preencherFormulario(cliente) {
    document.getElementById("nome").value = cliente.nome;
    document.getElementById("cpf").value = cliente.cpf;
    document.getElementById("email").value = cliente.email;
    document.getElementById("telefone").value = cliente.telefone;
    document.getElementById("dataNascimento").value = formatarDataParaInput(cliente.dataNascimento);
}

// Converte a data para o formato YYYY-MM-DD (usado nos inputs de data)
function formatarDataParaInput(data) {
    if (!data) return "";
    const dataObj = new Date(data);
    return dataObj.toISOString().split("T")[0]; // Retorna "YYYY-MM-DD"
}

