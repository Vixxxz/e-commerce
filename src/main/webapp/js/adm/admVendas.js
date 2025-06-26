const AdmVendas = (() => {
    const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";
    const Status = Object.freeze([
        "REPROVADA",
        "APROVADA",
        "EM_PROCESSAMENTO",
        "EM_TRANSITO",
        "ENTREGUE"
    ]);

    function init() {
        if (!document.getElementById('table-vendas')) return;
        realizarConsultaPedidos();
        document.getElementById('filtroFormVenda').addEventListener('submit', (e) => {
            e.preventDefault();
            realizarConsultaPedidos();
        });
    }

    async function realizarConsultaPedidos() {
        const filtroForm = document.getElementById('filtroFormVenda');
        const queryParams = filtroForm ? criarQueryParams(new FormData(filtroForm)) : '';
        const url = `${BASE_URL}/controlePedido${queryParams ? `?${queryParams}` : ''}`;

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
                    <button class="btn btn-warning" onclick="AdmVendas.proximaEtapa(${venda.id}, '${venda.status}')">Próxima Etapa</button>
                    <button class="btn btn-danger" onclick="AdmVendas.excluirPedido(${venda.id})">Excluir</button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    async function proximaEtapa(id, status) {
        try {
            if (!id || !status) {
                throw new Error("Dados da venda inválidos ou incompletos");
            }

            const index = Status.indexOf(status);

            if (index === -1) {
                throw new Error(`Status atual "${status}" não encontrado na lista`);
            }

            if (index === Status.length - 1) {
                throw new Error("Não há próximo status disponível - já está no status final");
            }

            if(index === 0){
                throw new Error("O pedido está cancelado");
            }

            const novoStatus = Status[index + 1];
            const pedidoJson = {
                Pedido: {
                    id: id,
                    status: novoStatus
                }
            };

            const resposta = await fetch(`${BASE_URL}/controlePedido`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(pedidoJson)
            });

            if (!resposta.ok) {
                const errorData = await resposta.json().catch(() => null);
                throw new Error(`Erro na requisição: ${resposta.status} - ${errorData?.message || 'Sem mensagem de erro'}`);
            }

            await realizarConsultaPedidos();

        } catch (error) {
            console.error("Erro ao avançar para próxima etapa:", error.message);
            alert(`Erro: ${error.message}`);
        }
    }

    async function excluirPedido(id) {
        try {
            const confirmar = confirm("Tem certeza que deseja excluir este pedido?");
            if (!confirmar) return;

            const resposta = await fetch(`${BASE_URL}/controlePedido?id=${id}`, {
                method: 'DELETE',
            });

            if (!resposta.ok) {
                throw new Error(`Erro ao excluir: ${resposta.status}`);
            }

            await realizarConsultaPedidos();

        } catch (error) {
            console.error("Erro ao excluir pedido:", error.message);
            alert(`Erro: ${error.message}`);
        }
    }

    function formatarData(data) {
        if (!data) return '';
        try {
            return new Date(data).toLocaleDateString('pt-BR');
        } catch (error) {
            console.log("Erro ao formatar data:", data, error);
            return data;
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

    return {
        init,
        proximaEtapa,
        excluirPedido
    };
})();

document.addEventListener('DOMContentLoaded', AdmVendas.init);
document.getElementById('vendas-title').addEventListener('click', AdmVendas.init);