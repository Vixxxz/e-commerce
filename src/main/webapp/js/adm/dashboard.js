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
    // Encontra os botões pela classe dentro do formulário do dashboard
    const btnLimpar = form.querySelector('.btn.btn-danger');
    const btnAtualizar = form.querySelector('.btn.btn-primary');

    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', (e) => {
            e.preventDefault(); // Impede o envio do formulário
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
            carregarDados(); // A função "Atualizar" pode simplesmente recarregar os dados
        });
    }
}


/**
 * Busca os dados na API, transforma e atualiza o gráfico.
 */
async function carregarDados() {
    if (!graficoVendas) return; // Não faz nada se o gráfico não foi inicializado

    const dataInicial = document.getElementById('dataInicial').value;
    const dataFinal = document.getElementById('dataFinal').value;
    const btnFiltrar = document.getElementById('submit-filter-dashboard');

    if (!dataInicial || !dataFinal) {
        alert('Por favor, selecione as datas inicial e final.');
        return;
    }

    // Desabilita o botão para evitar cliques duplos durante o carregamento
    if (btnFiltrar) btnFiltrar.disabled = true;

    try {
        const response = await fetch(`${API_BASE_URL}/dashboard?dataInicio=${dataInicial}&dataFim=${dataFinal}`);
        if (!response.ok) {
            throw new Error(`Erro na resposta do servidor: ${response.statusText} (Status: ${response.status})`);
        }
        const dados = await response.json();

        if (dados.length === 0) {
            alert('Nenhum dado encontrado para o período selecionado.');
        }

        const dadosChart = transformarDadosParaChart(dados);
        graficoVendas.data.labels = dadosChart.labels;
        graficoVendas.data.datasets = dadosChart.datasets;
        graficoVendas.update();

    } catch (error) {
        console.error('Falha ao carregar dados:', error);
        alert(`Erro ao carregar dados: ${error.message}. Verifique a conexão com o servidor e as configurações de CORS no backend.`);
    } finally {
        // Reabilita o botão após a conclusão da requisição
        if (btnFiltrar) btnFiltrar.disabled = false;
    }
}

/**
 * Transforma o array de dados da API no formato que o Chart.js espera.
 */
function transformarDadosParaChart(dadosApi) {
    const mesesUnicos = [...new Set(dadosApi.map(item => item.mesAno))].sort();
    const categoriasUnicas = [...new Set(dadosApi.map(item => item.categoria))];
    const cores = ['#0d6efd', '#dc3545', '#ffc107', '#198754', '#6f42c1', '#fd7e14'];

    const datasets = categoriasUnicas.map((categoria, index) => {
        const dadosDaCategoria = mesesUnicos.map(mes => {
            const itemEncontrado = dadosApi.find(d => d.categoria === categoria && d.mesAno === mes);
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

    const labelsFormatados = mesesUnicos.map(mesAno => {
        const [ano, mes] = mesAno.split('-');
        return `${mes.padStart(2, '0')}/${ano}`;
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

    // Formata a data para o formato YYYY-MM-DD, que é o que o input[type=date] espera
    document.getElementById('dataInicial').value = umMesAtras.toISOString().split('T')[0];
    document.getElementById('dataFinal').value = hoje.toISOString().split('T')[0];
}