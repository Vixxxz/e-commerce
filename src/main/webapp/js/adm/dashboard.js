// Variável global para guardar a instância do gráfico e evitar reinicialização
let graficoVendas;
const API_BASE_URL = 'http://localhost:8080/ecommerce_tenis_war_exploded';

/**
 * Ponto de entrada: Adiciona um listener na aba do dashboard.
 * O gráfico só será criado e carregado na primeira vez que o usuário clicar nesta aba.
 */
document.getElementById('dashboard-title').addEventListener('click', function() {
    // Se a variável 'graficoVendas' ainda não foi criada, inicializa todo o dashboard.
    if (!graficoVendas) {
        console.log('Inicializando o dashboard pela primeira vez...');
        inicializarComponentesDashboard();
    }
});

/**
 * Orquestra a inicialização completa dos componentes do dashboard.
 */
function inicializarComponentesDashboard() {
    initGrafico();
    setupEventListeners();
    definirDatasIniciais();
    carregarDados(); // Carrega os dados com o período padrão
}

/**
 * Cria a instância do gráfico Chart.js no canvas com id 'dashboard'.
 */
function initGrafico() {
    const canvas = document.getElementById('dashboard');
    if (!canvas) {
        console.error('Elemento <canvas id="dashboard"> não encontrado!');
        return;
    }

    const ctx = canvas.getContext('2d');
    graficoVendas = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false, // Importante para o canvas se adaptar ao container
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Período',
                        font: { size: 14 }
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Quantidade de Vendas',
                        font: { size: 14 }
                    }
                }
            },
            plugins: {
                title: {
                    display: false // O título já está no HTML
                },
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            interaction: {
                mode: 'index',
                intersect: false,
            }
        }
    });
}

/**
 * Configura os listeners de evento para os botões de filtrar, limpar e atualizar.
 */
function setupEventListeners() {
    const form = document.getElementById('filtroDashboard');
    const btnFiltrar = document.getElementById('submit-filter-dashboard');
    const btnLimpar = form.querySelector('.btn.btn-danger');
    const btnAtualizar = form.querySelector('.btn.btn-primary');

    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', (e) => {
            e.preventDefault();
            carregarDados();
        });
    }

    if (btnLimpar) {
        btnLimpar.addEventListener('click', (e) => {
            e.preventDefault();
            limparFiltros();
        });
    }

    if (btnAtualizar) {
        btnAtualizar.addEventListener('click', (e) => {
            e.preventDefault();
            carregarDados();
        });
    }
}


/**
 * Busca os dados na API, transforma e atualiza o gráfico.
 */
async function carregarDados() {
    if (!graficoVendas) return;

    const dataInicial = document.getElementById('dataInicial').value;
    const dataFinal = document.getElementById('dataFinal').value;
    const btnFiltrar = document.getElementById('submit-filter-dashboard');

    if (!dataInicial || !dataFinal) {
        alert('Por favor, selecione as datas inicial e final.');
        return;
    }
    if (new Date(dataInicial) > new Date(dataFinal)) {
        alert('A data inicial não pode ser posterior à data final.');
        return;
    }

    if (btnFiltrar) btnFiltrar.disabled = true;

    try {
        const response = await fetch(`${API_BASE_URL}/dashboard?dataInicial=${dataInicial}&dataFinal=${dataFinal}`);
        if (!response.ok) {
            throw new Error(`Erro na resposta do servidor: ${response.statusText} (Status: ${response.status})`);
        }
        const dados = await response.json();

        if (dados.length === 0) {
            alert('Nenhum dado encontrado para o período selecionado.');
        }

        // Passa as datas para a função de transformação para que ela possa gerar o intervalo completo
        const dadosChart = transformarDadosParaChart(dados, dataInicial, dataFinal);
        graficoVendas.data.labels = dadosChart.labels;
        graficoVendas.data.datasets = dadosChart.datasets;
        graficoVendas.update();

    } catch (error) {
        console.error('Falha ao carregar dados:', error);
        alert(`Erro ao carregar dados: ${error.message}.`);
    } finally {
        if (btnFiltrar) btnFiltrar.disabled = false;
    }
}

/**
 * Gera um array com todos os meses (no formato 'YYYY-MM') dentro de um intervalo de datas.
 * @param {string} dataInicioStr - A data de início no formato 'YYYY-MM-DD'.
 * @param {string} dataFimStr - A data de fim no formato 'YYYY-MM-DD'.
 * @returns {string[]} - Um array de meses, ex: ['2025-01', '2025-02'].
 */
function gerarTodosOsMesesNoIntervalo(dataInicioStr, dataFimStr) {
    const meses = [];
    // Adiciona 'T00:00:00' para evitar problemas de fuso horário
    let dataAtual = new Date(dataInicioStr + 'T00:00:00');
    const dataFim = new Date(dataFimStr + 'T00:00:00');

    // Garante que o loop comece no primeiro dia do mês para consistência
    dataAtual.setDate(1);

    while (dataAtual <= dataFim) {
        const ano = dataAtual.getFullYear();
        // getMonth() é base 0 (0-11), então adicionamos 1 e formatamos
        const mes = (dataAtual.getMonth() + 1).toString().padStart(2, '0');
        meses.push(`${ano}-${mes}`);

        // Avança para o próximo mês
        dataAtual.setMonth(dataAtual.getMonth() + 1);
    }
    return meses;
}


/**
 * Transforma o array de dados da API no formato que o Chart.js espera.
 * APRIMORADO: Agora gera todos os meses do intervalo, preenchendo com 0 os que não têm vendas.
 * @param {Array} dadosApi - Os dados retornados pelo backend.
 * @param {string} dataInicial - A data inicial do filtro ('YYYY-MM-DD').
 * @param {string} dataFinal - A data final do filtro ('YYYY-MM-DD').
 * @returns {Object} - Um objeto pronto para ser usado pelo Chart.js.
 */
function transformarDadosParaChart(dadosApi, dataInicial, dataFinal) {
    // Passo 1: Gerar todos os meses que deveriam aparecer no gráfico.
    const todosOsMeses = gerarTodosOsMesesNoIntervalo(dataInicial, dataFinal);

    // Passo 2: Encontrar todas as categorias únicas que vieram da API.
    const categoriasUnicas = [...new Set(dadosApi.map(item => item.categoria))];
    const cores = ['#0d6efd', '#dc3545', '#ffc107', '#198754', '#6f42c1', '#fd7e14'];

    // Passo 3: Criar um dataset para cada categoria.
    const datasets = categoriasUnicas.map((categoria, index) => {
        // Para cada mês do intervalo completo, procurar pela venda correspondente.
        const dadosDaCategoria = todosOsMeses.map(mes => {
            const itemEncontrado = dadosApi.find(d => d.categoria === categoria && d.mesAno === mes);
            // Se encontrar, usa o valor. Se não, a venda é 0.
            return itemEncontrado ? itemEncontrado.vendas : 0;
        });

        return {
            label: categoria,
            data: dadosDaCategoria,
            borderColor: cores[index % cores.length],
            backgroundColor: cores[index % cores.length] + '40', // Cor com 25% de opacidade
            fill: false,
            tension: 0.3
        };
    });

    // Passo 4: Formatar os labels dos meses para exibição (ex: '01/2025').
    const labelsFormatados = todosOsMeses.map(mesAno => {
        const [ano, mes] = mesAno.split('-');
        return `${mes}/${ano}`;
    });

    return {
        labels: labelsFormatados,
        datasets: datasets
    };
}

/**
 * Limpa os filtros de data e o gráfico.
 */
function limparFiltros() {
    if (!graficoVendas) return;

    document.getElementById('dataInicial').value = '';
    document.getElementById('dataFinal').value = '';

    graficoVendas.data.labels = [];
    graficoVendas.data.datasets = [];
    graficoVendas.update();
}

/**
 * Define as datas padrão nos inputs (do último mês até hoje).
 */
function definirDatasIniciais() {
    const hoje = new Date();
    const umMesAtras = new Date();
    umMesAtras.setMonth(hoje.getMonth() - 1);

    document.getElementById('dataInicial').value = umMesAtras.toISOString().split('T')[0];
    document.getElementById('dataFinal').value = hoje.toISOString().split('T')[0];
}