const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";

document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('table-vendas')) {
        console.warn('admVendas.js carregado em página incorreta. Script ignorado.');
        return; // Encerra a execução se não for a página de vendas
    }

    realizarConsultaPedidos();
    document.getElementById('filtroForm').addEventListener('submit', (e) => {
        e.preventDefault();
        realizarConsultaPedidos();
    });
});

async function realizarConsultaPedidos() {
    const filtroForm = document.getElementById('filtroForm');
    const queryParams = filtroForm ? criarQueryParams(new FormData(filtroForm)) : '';
    const url = `${BASE_URL}/controlePedido${queryParams ? `?${queryParams}` : ''}`;

    console.log(url);

    try {
        const resposta = await fetch(url, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!resposta.ok) {
            throw new Error(`Erro HTTP: ${resposta.status}`);
        }

        const respostaJson = await resposta.json();
        const vendas = Array.isArray(respostaJson) ? respostaJson : [respostaJson];

        vendas.length
            ? renderTabela(vendas)
            : mostrarErro('Nenhuma venda encontrada.');

    } catch (error) {
        mostrarErro('Erro ao buscar vendas.', error);
    }
}

function renderTabela(vendas) {
    const tbody = document.querySelector('#table-vendas tbody');
    tbody.innerHTML = '';

    vendas.forEach(venda => {
        const tr = document.createElement('tr');

        tr.innerHTML = `
            <td>${venda.id ?? ''}</td>
            <td>${venda.valorTotal?.toFixed(2) ?? '0.00'}</td>
            <td>${venda.dtCadastro ? formatarData(new Date(venda.dtCadastro)) : ''}</td>
            <td>${venda.status ?? ''}</td>
            <td>${venda.transportadora?.nome ?? 'Transportadora não informada'}</td>
            <td>${venda.clienteEndereco?.cliente?.cpf ?? ''}</td>
            <td>
                <button class="btn btn-primary" onclick="verDetalhes(${venda.id})">Detalhes</button>
            </td>
        `;

        tbody.appendChild(tr);
    });
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

function criarQueryParams(formData) {
    const params = new URLSearchParams();

    formData.forEach((value, key) => {
        if (value.trim()) params.append(key, value.trim());
    });

    params.append('statusList', 'APROVADA, REPROVADA, EM_PROCESSAMENTO, EM_TRANSITO, ENTREGUE');

    return params.toString();
}

function mostrarErro(mensagem, error) {
    console.error(mensagem, error);
    alert(mensagem);
}