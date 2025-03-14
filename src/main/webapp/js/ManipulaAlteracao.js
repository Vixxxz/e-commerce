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

function formatarData(data) {
    if (!data) return "";

    if (!isNaN(Date.parse(data))) {
        return new Date(data).toLocaleDateString("pt-BR");
    }

    // Converter manualmente datas em português
    const meses = {
        "jan.": "01", "fev.": "02", "mar.": "03", "abr.": "04",
        "mai.": "05", "jun.": "06", "jul.": "07", "ago.": "08",
        "set.": "09", "out.": "10", "nov.": "11", "dez.": "12"
    };

    const partes = data.split(" ");
    const mes = meses[partes[0].toLowerCase()];
    const dia = partes[1].replace(",", "");
    const ano = partes[2];

    if (mes && dia && ano) {
        const dataFormatada = `${ano}-${mes}-${dia}`;
        return new Date(dataFormatada).toLocaleDateString("pt-BR");
    }

    return "Data inválida";
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

document.getElementById('endereco-consulta-title').addEventListener('click', (e) => {
    realizarConsultaEndereco(e);
});

document.getElementById('filter-endereco').addEventListener('click', (e) => {
    realizarConsultaEndereco(e);
});

async function realizarConsultaEndereco(e) {
    e.preventDefault();
    const filtroForm = document.getElementById('filtroEndereco');
    const queryParams = criarQueryParams(new FormData(filtroForm));
    const url = `${BASE_URL}/controleEndereco?idCliente=${clienteId}${queryParams}`;

    try {
        const respostaJson = await fetchAPI(url, 'Erro ao buscar endereços');
        const enderecos = Array.isArray(respostaJson) ? respostaJson : [respostaJson];

        enderecos.length
            ? renderTabela(enderecos)
            : mostrarErro('Nenhum endereco encontrado ou resposta inválida.');

    } catch (error) {
        mostrarErro('Erro ao buscar clientes.', error);
    }
}

function criarQueryParams(formData) {
    const params = new URLSearchParams();
    formData.forEach((value, key) => {
        if (value.trim()) params.append(key, value.trim());
    });
    console.log("parametros: " + params.toString());
    return params.toString();
}

function renderTabela(enderecos) {
    const tbody = document.querySelector('#table-endereco tbody');

    enderecos.forEach(endereco => {
        console.log(endereco.tipoResidencia);
        console.log(endereco.logradouro);
    });

    tbody.innerHTML = enderecos.map(endereco => `
        <tr>
            <td>${escapeHtml(endereco.endereco.cep || '')}</td>
            <td>${escapeHtml(endereco.numero || '')}</td>
            <td>${escapeHtml(endereco.endereco.logradouro || '')}</td>
            <td>${escapeHtml(endereco.tipoResidencia || '')}</td>
            <td>${escapeHtml(endereco.tipoEndereco || '')}</td>
            <td>
                <button class="btn-warning btn btn-sm" data-id="${endereco.id}">Alterar</button>
                <button class="btn-danger btn btn-sm" data-id="${endereco.id}">Excluir</button>
            </td>
        </tr>
    `).join('');

    adicionarEventosTabela();
}

function adicionarEventosTabela(){
    document.querySelectorAll('.btn-danger').forEach(btn => {
        btn.addEventListener('click', (e) => confirmarExclusaoCliente(e.target.getAttribute('data-id')));
    });

    document.querySelectorAll('.btn-warning').forEach(btn => {
        btn.addEventListener('click', (e) => confirmarExclusaoCliente(e.target));
    })
}

async function fetchAPI(url, mensagemErro) {
    try {
        const resposta = await fetch(url);
        if (!resposta.ok) throw new Error(`${mensagemErro}: ${resposta.statusText}`);
        return await resposta.json();
    } catch (error) {
        console.error(mensagemErro, error);
        alert(mensagemErro);
        throw error;
    }
}

function mostrarErro(mensagem, error) {
    console.error(mensagem, error);
    alert(mensagem);
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.innerText = str;
    return div.innerHTML;
}
