const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";

document.addEventListener('DOMContentLoaded', () => {
    realizarConsultaClientes();
    document.getElementById('filtroForm').addEventListener('submit', (e) => {
        e.preventDefault();
        realizarConsultaClientes();
    });
});

async function realizarConsultaClientes() {
    const filtroForm = document.getElementById('filtroForm');
    const queryParams = criarQueryParams(new FormData(filtroForm));
    const url = `${BASE_URL}/controlecliente?${queryParams}`;

    try {
        const respostaJson = await fetchAPI(url, 'Erro ao buscar clientes');
        const clientes = Array.isArray(respostaJson) ? respostaJson : [respostaJson];

        clientes.length
            ? renderTabela(clientes)
            : mostrarErro('Nenhum cliente encontrado ou resposta inválida.');

    } catch (error) {
        mostrarErro('Erro ao buscar clientes.', error);
    }
}

// async function alterarCliente(clienteId, formData) {
//     if (!validarSenha(formData)) return;
//
//     const clienteJson = criarClienteJson(clienteId, formData);
//     try {
//         await sendRequest(`${BASE_URL}/controlecliente`, 'PUT', clienteJson);
//         alert('Cliente alterado com sucesso!');
//         realizarConsultaClientes();
//         fecharModal('modalAlterar');
//     } catch (error) {
//         mostrarErro('Erro ao alterar cliente.', error);
//     }
// }

async function confirmarExclusaoCliente(clienteId) {
    // exibirModal("modalExcluir", document.getElementById("modalExcluir"));
    if (!confirm(`Tem certeza que deseja excluir o cliente com ID ${clienteId}?`)) return;

    try {
        await sendRequest(`${BASE_URL}/controlecliente?id=${clienteId}`, 'DELETE');
        console.log("url utilizada: ${BASE_URL}/controlecliente?id=${clienteId}")
        alert('Cliente excluído com sucesso!');
        realizarConsultaClientes();
    } catch (error) {
        mostrarErro('Erro ao excluir cliente.', error);
    }
}

function renderTabela(clientes) {
    const tbody = document.querySelector('#table-clientes tbody');
    tbody.innerHTML = clientes.map(cliente => `
        <tr>
            <td class="idCliente">${escapeHtml(cliente.id || '')}</td>
            <td>${escapeHtml(cliente.nome || '')}</td>
            <td>${escapeHtml(cliente.cpf || '')}</td>
            <td>${escapeHtml(formatarData(cliente.dataNascimento) || '')}</td>
            <td>${escapeHtml(cliente.telefone || '')}</td>
            <td>${escapeHtml(cliente.email || '')}</td>
            <td>
                <a href="./alterar/alteraCliente.html?id=${cliente.id}"><button class="btn-warning btn btn-sm" data-id="${cliente.id}">Alterar</button></a>
                <button class="btn-danger btn btn-sm" data-id="${cliente.id}">Excluir</button>
            </td>
        </tr>
    `).join('');
    adicionarEventosTabela();
}

function formatarData(data) {
    console.log("Valor recebido:", data); // 🔍 Depuração

    if (!data) return ''; // Se for nulo ou undefined, retorna vazio
    try {
        const dataObj = new Date(data); // Converte para objeto Date
        console.log("Objeto Date gerado:", dataObj);
        return dataObj.toLocaleDateString('pt-BR'); // Retorna no formato DD/MM/YYYY
    } catch (error) {
        console.log("Erro ao formatar data:", data, error);
        return data; // Retorna a data original se houver erro
    }
}

function adicionarEventosTabela(){
    document.querySelectorAll('.btn-danger').forEach(btn => {
        btn.addEventListener('click', (e) => confirmarExclusaoCliente(e.target.getAttribute('data-id')));
    });
}

// function exibirFormularioAlteracao(clienteId) {
//     const conteudo = criarFormularioAlteracao(clienteId);
//     exibirModal('modalAlterar', conteudo);
//
//     document.getElementById('formAlterar').addEventListener('submit', async (e) => {
//         e.preventDefault();
//         const formData = new FormData(e.target);
//         alterarCliente(clienteId, formData);
//     });
//
//     document.getElementById('nome').focus();
// }

// function validarSenha(formData) {
//     const senha = formData.get('senha');
//     const confirmaSenha = formData.get('confirmaSenha');
//     if (senha !== confirmaSenha) {
//         alert('As senhas não coincidem. Por favor, verifique.');
//         return false;
//     }
//     return true;
// }

function criarQueryParams(formData) {
    const params = new URLSearchParams();
    formData.forEach((value, key) => {
        if (value.trim()) params.append(key, value.trim());
    });
    console.log(params.toString());
    return params.toString();
}

// function criarClienteJson(clienteId, formData) {
//     return {
//         Cliente: {
//             id: clienteId,
//             nome: formData.get('nome'),
//             genero: formData.get('genero'),
//             cpf: formData.get('cpf'),
//             tipoTelefone: formData.get('tipoTelefone'),
//             telefone: formData.get('telefone'),
//             email: formData.get('email'),
//             senha: formData.get('senha'),
//             dataNascimento: formData.get('dataNascimento')
//         }
//     };
// }

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

async function sendRequest(url, method, body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' },
    };
    if (body) options.body = JSON.stringify(body);

    const resposta = await fetch(url, options);
    if (!resposta.ok) throw new Error(`${method} failed: ${resposta.statusText}`);
    return await resposta.json();
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

function exibirModal(modalId, conteudo) {
    const modal = document.getElementById(modalId);
    modal.querySelector('.modal-content').innerHTML = conteudo;
    modal.style.display = 'flex';
    modal.querySelector('.close-modal').addEventListener('click', () => fecharModal(modalId));
}

function fecharModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.style.display = 'none';
}