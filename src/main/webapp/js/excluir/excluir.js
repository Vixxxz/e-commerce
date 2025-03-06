const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";

function obterParametroUrl(id) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(id);
}

const clienteId = obterParametroUrl("id");
console.log("Cliente ID:", clienteId);

document.addEventListener("DOMContentLoaded", () => {
    if (clienteId) {
        excluirCliente(clienteId);
    }
});

async function excluirCliente(id) {
    try {
        const response = await fetch(`${BASE_URL}/controlecliente?id=${id}`);
        const resultado = await response.json();

        console.log("Sucesso:", resultado);
    } catch (error) {
        console.error("Erro ao excluir cliente:", error);
    }
}

