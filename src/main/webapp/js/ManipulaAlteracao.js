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

function preencherFormulario(clienteArray) {
    console.log("CLiente recebido para preencher: ", clienteArray);

    if (!Array.isArray(clienteArray) || clienteArray.length === 0) {
        console.error("Erro: Nenhum cliente encontrado!");
        return;
    }

    const cliente = clienteArray[0];

    document.getElementById("nome-altera").value = cliente.nome;
    document.getElementById("cpf-altera").value = cliente.cpf;
    document.getElementById("email-altera").value = cliente.email;
    document.getElementById("telefone-altera").value = cliente.telefone;
    document.getElementById("dataNascimento-altera").value = formatarDataParaInput(cliente.dataNascimento);
    preencherRadioButton("genero-altera",cliente.genero);
    preencherRadioButton('tipoTelefone-altera',cliente.tipoTelefone);

}

// Converte a data para o formato YYYY-MM-DD (usado nos inputs de data)
function formatarDataParaInput(data) {
    if (!data) return "";
    const dataObj = new Date(data);
    return dataObj.toISOString().split("T")[0]; // Retorna "YYYY-MM-DD"
}

function preencherRadioButton(name, valor) {
    const radios = document.getElementsByName(name);
    radios.forEach(radio => {
        if (radio.value === valor) {
            radio.checked = true;
        }
    });
}


/*--------------ALTERAR DADOS---------------*/
async function enviarDadosCliente(event) {
    event.preventDefault();

    // Captura os valores do formulário
    const cliente = {
        id: clienteId,
        nome: document.getElementById("nome-altera").value,
        cpf: document.getElementById("cpf-altera").value,
        email: document.getElementById("email-altera").value,
        telefone: document.getElementById("telefone-altera").value,
        dataNascimento: document.getElementById("dataNascimento-altera").value
    };

    console.log("Enviando cliente atualizado:", cliente);
    const url = `${BASE_URL}/controlecliente?${clienteId}`;

    try {
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(cliente)
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


/*--------------ENDERECO--------------------*/