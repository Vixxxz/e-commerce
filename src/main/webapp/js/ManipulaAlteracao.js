const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";

let clienteRanking = null;

function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}

const clienteId = obterParametroUrl("id");
console.log("Cliente ID:", clienteId);

document.addEventListener("DOMContentLoaded", () => {
    if (clienteId) {
        carregarDadosCliente(clienteId);
    }
});

document.querySelectorAll('#submit-button-altera').forEach(btn => {
    btn.addEventListener('click', (e) => enviarDadosCliente(e));
});

async function carregarDadosCliente(id) {
    try {
        const response = await fetch(`${BASE_URL}/controlecliente?id=${id}`);
        const cliente = await response.json();

        console.log("Cliente recebido:", cliente);
        preencherFormulario(cliente);

        // Armazena o ranking retornado pela API
        if (Array.isArray(cliente) && cliente.length > 0) {
            clienteRanking = cliente[0].ranking;
            console.log("Ranking do cliente:", clienteRanking);
        }
    } catch (error) {
        console.error("Erro ao carregar cliente:", error);
    }
}

function preencherFormulario(clienteArray) {
    console.log("Cliente recebido para preencher:", clienteArray);

    if (!Array.isArray(clienteArray) || clienteArray.length === 0) {
        console.error("Erro: Nenhum cliente encontrado!");
        return;
    }

    const cliente = clienteArray[0];

    document.getElementById("nome-altera").value = cliente.nome || "";
    document.getElementById("cpf-altera").value = cliente.cpf || "";
    document.getElementById("email-altera").value = cliente.email || "";
    document.getElementById("telefone-altera").value = cliente.telefone || "";
    document.getElementById("dataNascimento-altera").value = formatarDataParaInput(cliente.dataNascimento) || "";
    preencherRadioButton("genero-altera", cliente.genero);
    preencherRadioButton("tipoTelefone-altera", cliente.tipoTelefone);
}

function formatarDataParaInput(data) {
    if (!data) return "";
    const dataObj = new Date(data);
    return dataObj.toISOString().split("T")[0];
}

function preencherRadioButton(name, valor) {
    const radios = document.getElementsByName(name);
    radios.forEach(radio => {
        if (radio.value === valor) {
            radio.checked = true;
        }
    });
}

async function enviarDadosCliente(event) {
    event.preventDefault();

    const cliente = montaJson(clienteId);

    console.log("Enviando cliente atualizado:", cliente);
    const url = `${BASE_URL}/controlecliente`;

    try {
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: cliente
        });

        const resultado = await response.json();

        if (response.ok) {
            alert("Cliente atualizado com sucesso!");
        } else {
            alert("Erro ao atualizar cliente: " + resultado.mensagem);
        }
    } catch (error) {
        console.error("Erro na requisição:", error);
        alert("Erro ao enviar os dados.");
    }
}

function montaJson(id) {
    return JSON.stringify({
        Cliente: {
            id: id,
            ranking: clienteRanking,
            nome: document.getElementById("nome-altera").value,
            genero: document.querySelector('input[name="genero-altera"]:checked').value,
            cpf: document.getElementById("cpf-altera").value,
            tipoTelefone: document.querySelector('input[name="tipoTelefone-altera"]:checked').value,
            telefone: document.getElementById("telefone-altera").value,
            email: document.getElementById("email-altera").value,
            senha: document.getElementById("senha-altera").value,
            dataNascimento: document.getElementById("dataNascimento-altera").value
        }
    });
}