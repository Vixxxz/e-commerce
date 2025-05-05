document.addEventListener("DOMContentLoaded", () => {
    carregarDadosPedidos();
});

// --- URL Utilities ---
function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}

// --- Estado Global ---
const id = obterParametroUrl("id");

const trocaSelecionada = {
    trocaSolicitada: {
        pedido: {
            id: id
        },
        cliente: {
            id: null
        },
        valorTotal: 0,
        status: null
    },
    trocaProdutos: []
};

// --- Inicialização ---
function inicializarBotoesTroca(itensPedido) {
    document.querySelectorAll('.solicitar-troca button').forEach((button, index) => {
        button.addEventListener('click', () => {
            const inputQuantidade = document.getElementById(`quantidade${index}`);
            const quantidade = parseInt(inputQuantidade.value);

            if (isNaN(quantidade) || quantidade <= 0 || quantidade > itensPedido[index].quantidade) {
                alert(`Quantidade inválida. Máximo permitido: ${itensPedido[index].quantidade}`);
                return;
            }

            adicionarItemTroca(itensPedido[index], quantidade, index);
            inputQuantidade.value = '';
        });
    });
}

// --- Operações de Troca ---
function adicionarItemTroca(itemPedido, quantidade) {
    if (trocaSelecionada.trocaSolicitada.cliente.id === null || !trocaSelecionada.trocaSolicitada.cliente.id) {
        trocaSelecionada.trocaSolicitada.cliente.id = itemPedido.pedido.clienteEndereco.cliente.id;
    }

    if(trocaSelecionada.trocaSolicitada.status === null || !trocaSelecionada.trocaSolicitada.status){
        trocaSelecionada.trocaSolicitada.status = 'TROCA_SOLICITADA';
    }

    const produtoExistenteIndex = trocaSelecionada.trocaProdutos.findIndex(
        item => item.produto.id === itemPedido.produto.id
    );

    if (produtoExistenteIndex >= 0) {
        trocaSelecionada.trocaProdutos[produtoExistenteIndex].quantidade = quantidade;
    } else {
        trocaSelecionada.trocaProdutos.push({
            id: null,
            trocaSolicitada: { id: null },
            produto: {
                id: itemPedido.produto.id,
                nome: itemPedido.produto.nome,
                preco: itemPedido.produto.preco
            },
            quantidade: quantidade
        });
    }

    trocaSelecionada.trocaSolicitada.valorTotal = trocaSelecionada.trocaProdutos.reduce(
        (total, item) => total + (item.produto.preco * item.quantidade), 0
    );

    console.log('JSON de troca atualizado:', trocaSelecionada);
    alert(`Produto "${itemPedido.produto.nome}" adicionado para troca.`);
}

async function enviarSolicitacaoTroca() {
    if (trocaSelecionada.trocaProdutos.length === 0) {
        alert('Nenhum produto selecionado para troca.');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/controleTroca', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(trocaSelecionada)
        });

        if (response.ok) {
            const resultado = await response.json();
            alert('Solicitação de troca enviada com sucesso!');
            console.log('Resposta do servidor:', resultado);
            trocaSelecionada.trocaProdutos = [];
            trocaSelecionada.trocaSolicitada.valorTotal = 0;
        } else {
            throw new Error('Erro ao enviar solicitação');
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao enviar solicitação de troca. Por favor, tente novamente.');
    }
}

// --- Carregamento de Pedido ---
async function carregarDadosPedidos() {
    const container = document.querySelector('.detalhes-pedido');
    const url = `http://localhost:8080/ecommerce_tenis_war_exploded/controlePedidoProduto?idPedido=${id}`;

    try {
        const response = await fetch(url);
        const itensPedido = await response.json();
        container.innerHTML = "";

        if (itensPedido.length === 0) {
            container.innerHTML = '<p>Nenhum item encontrado neste pedido.</p>';
            return;
        }

        const statusPedido = itensPedido[0].pedido.status;
        container.insertAdjacentHTML('beforeend', montarStatusPedido(statusPedido));

        itensPedido.forEach((item, index) => {
            container.insertAdjacentHTML('beforeend', montarProdutoPedido(item, index));
        });

        container.insertAdjacentHTML('beforeend', `
            <div class="finalizar-troca">
                <button id="finalizarTroca" style="padding: 10px">Finalizar Troca</button>
            </div>
        `);

        atualizarBarraProgresso(statusPedido);
        inicializarBotoesTroca(itensPedido);
        document.getElementById('finalizarTroca').addEventListener('click', enviarSolicitacaoTroca);

    } catch (error) {
        console.error('Erro ao carregar dados do pedido:', error);
        container.innerHTML = '<p>Erro ao carregar informações do pedido.</p>';
    }
}

// --- UI Helpers ---
function montarStatusPedido(statusAtual) {
    const statusMap = {
        'APROVADA': 'Aprovada',
        'EM_PROCESSAMENTO': 'Em Processamento',
        'EM_TRANSITO': 'Em Trânsito',
        'ENTREGUE': 'Entregue',
        'TROCA_SOLICITADA': 'Troca Solicitada',
        'TROCA_AUTORIZADA': 'Troca Autorizada',
        'TROCADO': 'Trocado'
    };

    const statusPossiveis = Object.keys(statusMap).map(id => ({ id, label: statusMap[id] }));

    const steps = statusPossiveis.map(status => {
        const isActive = statusAtual === status.id;
        return `
            <div class="step ${isActive ? 'active' : ''}" data-status="${status.id}">
                <div class="step-icon"></div>
                <div class="step-label">${status.label}</div>
            </div>
        `;
    }).join('');

    return `
        <div class="status-pedido">
            <div class="barra-progresso"></div>
            <div class="status">${steps}</div>
        </div>
    `;
}

function montarProdutoPedido(item, index) {
    const produto = item.produto;
    const valorTotalItem = produto.preco * item.quantidade;

    return `
        <div class="produto-pedido">
            <div class="info-detalhes-pedido">
                <img src="../../../img/${produto.caminhoFoto}" alt="${produto.nome}">
                <div class="info-detalhes-tenis">
                    <p>${produto.nome}</p>
                    <p>Modelo: <span>${produto.modelo}</span></p>
                    <p>Cor: <span>${produto.cor}</span></p>
                    <p>Tamanho: <span>${produto.tamanho}</span></p>
                    <p>Gênero: <span>${formatarGenero(produto.genero)}</span></p>
                </div>
                <div class="info-detalhes-valor">
                    <p>Valor unitário: <span>${formatarMoeda(produto.preco)}</span></p>
                    <p>Quantidade: <span>${item.quantidade}</span></p>
                    <p>Subtotal: <span>${formatarMoeda(valorTotalItem)}</span></p>
                </div>
            </div>
            <div class="solicitar-troca">
                <label for="quantidade${index}">Quantidade para trocar</label>
                <input type="number" id="quantidade${index}" name="quantidade" placeholder="Quantidade" max="${item.quantidade}" min="0">
                <button>Incluir para Troca</button>
            </div>
        </div>
    `;
}

function formatarMoeda(valor) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(valor);
}

function formatarGenero(genero) {
    const generos = {
        'MASCULINO': 'Masculino',
        'FEMININO': 'Feminino',
        'UNISEX': 'Unissex'
    };
    return generos[genero] || genero;
}

function atualizarBarraProgresso(statusAtual) {
    const statusOrdem = [
        'APROVADA', 'EM_PROCESSAMENTO', 'EM_TRANSITO',
        'ENTREGUE', 'TROCA_SOLICITADA', 'TROCA_AUTORIZADA', 'TROCADO'
    ];

    const indexAtual = statusOrdem.indexOf(statusAtual);
    const progresso = (indexAtual / (statusOrdem.length - 1)) * 100;

    const barraProgresso = document.querySelector('.barra-progresso');
    if (barraProgresso) barraProgresso.style.width = `${progresso}%`;
}