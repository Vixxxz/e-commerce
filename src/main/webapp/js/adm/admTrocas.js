const AdmTrocas = (() => {
    const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";
    const Status = Object.freeze([
        "TROCA_SOLICITADA",
        "TROCA_AUTORIZADA",
        "TROCA_RECUSADA",
        "TROCADO"
    ]);

    function init() {
        if (!document.getElementById('table-trocas')) return;
        realizarConsultaTrocas();
        document.getElementById('filtroFormTroca').addEventListener('submit', (e) => {
            e.preventDefault();
            realizarConsultaTrocas();
        });
    }

    async function realizarConsultaTrocas() {
        const filtroForm = document.getElementById('filtroFormTroca');
        const queryParams = filtroForm ? criarQueryParams(new FormData(filtroForm)) : '';
        const url = `${BASE_URL}/controleTroca${queryParams ? `?${queryParams}` : ''}`;

        try {
            const resposta = await fetch(url, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!resposta.ok) {
                throw new Error(`Erro HTTP: ${resposta.status}`);
            }

            const respostaJson = await resposta.json();
            const trocas = Array.isArray(respostaJson) ? respostaJson : [respostaJson];

            trocas.length
                ? renderTabela(trocas)
                : mostrarErro('Nenhuma troca encontrada.');

        } catch (error) {
            mostrarErro('Erro ao buscar trocas.', error);
        }
    }

    function renderTabela(trocas) {
        const tbody = document.querySelector('#table-trocas tbody');
        tbody.innerHTML = '';

        trocas.forEach(troca => {
            const tr = document.createElement('tr');

            tr.innerHTML = `
                <td>${troca.id ?? ''}</td>
                <td>${troca.valorTotal?.toFixed(2) ?? '0.00'}</td>
                <td>${troca.dtCadastro ? formatarData(new Date(troca.dtCadastro)) : ''}</td>
                <td>${troca.status ?? ''}</td>
                <td>${troca.cliente?.cpf ?? ''}</td>
                <td>
                    <button class="btn btn-warning" onclick="AdmTrocas.proximaEtapa(${troca.id}, '${troca.status}')">Próxima Etapa</button>
                    <button class="btn btn-danger" onclick="AdmTrocas.excluirPedido(${troca.id})">Excluir</button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    async function proximaEtapa(id, status) {
        try {
            if (!id || !status) {
                throw new Error("Dados da troca inválidos ou incompletos");
            }

            const index = Status.indexOf(status);

            if (index === -1) {
                throw new Error(`Status atual "${status}" não encontrado na lista`);
            }

            if (index === Status.length - 1) {
                throw new Error("Não há próximo status disponível - já está no status final");
            }

            const novoStatus = Status[index + 1];
            const trocaJson = {
                troca: {
                    id: id,
                    status: novoStatus
                }
            };

            const resposta = await fetch(`${BASE_URL}/controleTroca`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(trocaJson)
            });

            if (!resposta.ok) {
                const errorData = await resposta.json().catch(() => null);
                throw new Error(`Erro na requisição: ${resposta.status} - ${errorData?.message || 'Sem mensagem de erro'}`);
            }

            await realizarConsultaTrocas();

        } catch (error) {
            console.error("Erro ao avançar para próxima etapa:", error.message);
            alert(`Erro: ${error.message}`);
        }
    }

    async function excluirPedido(id) {
        try {
            const confirmar = confirm("Tem certeza que deseja excluir esta troca?");
            if (!confirmar) return;

            const resposta = await fetch(`${BASE_URL}/controleTroca?id=${id}`, {
                method: 'DELETE',
            });

            if (!resposta.ok) {
                throw new Error(`Erro ao excluir: ${resposta.status}`);
            }

            await realizarConsultaTrocas();

        } catch (error) {
            console.error("Erro ao excluir troca:", error.message);
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

        params.append('statusList', 'TROCA_SOLICITADA, TROCA_AUTORIZADA, TROCA_RECUSADA, TROCADO');

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

document.addEventListener('DOMContentLoaded', AdmTrocas.init);