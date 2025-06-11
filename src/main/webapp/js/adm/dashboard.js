let graficoVendas;

function initGrafico() {
    const ctx = document.getElementById('dashboard').getContext('2d');

    graficoVendas = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Período'
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Quantidade de Vendas'
                    }
                }
            },
            plugins: {
                title: {
                    display: true,
                    text: 'Tendência de Vendas Mensais'
                },
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            interaction: {
                mode: 'index',
                intersect: false,
            },
            elements: {
                line: {
                    tension: 0.4
                },
                point: {
                    radius: 5,
                    hoverRadius: 8
                }
            }
        }
    });
}

async function carregarDados() {
    const dataInicial = document.getElementById('dataInicial').value;
    const dataFinal = document.getElementById('dataFinal').value;

    if (!dataInicial || !dataFinal) {
        alert('Por favor, selecione ambas as datas (inicial e final)');
        return;
    }

    if (new Date(dataInicial) > new Date(dataFinal)) {
        alert('A data inicial deve ser anterior à data final');
        return;
    }

    try {
        const response = await fetch(
            `/api/vendas/por-categoria-periodo?dataInicial=${dataInicial}&dataFinal=${dataFinal}`
        );

        if (!response.ok) {
            throw new Error('Erro ao buscar dados do servidor');
        }

        const dados = await response.json();

        // Atualizar o gráfico
        graficoVendas.data.labels = dados.labels;
        graficoVendas.data.datasets = dados.datasets;
        graficoVendas.update();

    } catch (error) {
        console.error('Erro ao carregar dados:', error);
        alert('Erro ao carregar dados do servidor: ' + error.message);
    }
}

function atualizarGrafico() {
    carregarDados();
}

function limparFiltros() {
    document.getElementById('dataInicial').value = '';
    document.getElementById('dataFinal').value = '';

    // Limpar gráfico
    graficoVendas.data.labels = [];
    graficoVendas.data.datasets = [];
    graficoVendas.options.plugins.title.text = 'Tendência de Vendas Mensais';
    graficoVendas.update();
}

function formatarData(dataISO) {
    const data = new Date(dataISO + 'T00:00:00');
    return data.toLocaleDateString('pt-BR');
}

function definirDatasIniciais() {
    const hoje = new Date();
    const umMesAtras = new Date();
    umMesAtras.setMonth(hoje.getMonth() - 1);

    // Formatar para YYYY-MM-DD (formato do input date)
    const dataInicialFormatada = umMesAtras.toISOString().split('T')[0];
    const dataFinalFormatada = hoje.toISOString().split('T')[0];

    document.getElementById('dataInicial').value = dataInicialFormatada;
    document.getElementById('dataFinal').value = dataFinalFormatada;
}

// Event Listeners para os botões
function setupEventListeners() {
    // Botão Filtrar
    const btnFiltrar = document.getElementById('submit-filter-dashboard');
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', function(e) {
            e.preventDefault();
            carregarDados();
        });
    }

    // Formulário (para capturar Enter)
    const form = document.getElementById('filtroDashboard');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            carregarDados();
        });
    }

    const btnLimpar = document.querySelector('.btn.btn-danger');
    if (btnLimpar) {
        btnLimpar.addEventListener('click', function(e) {
            e.preventDefault();
            limparFiltros();
        });
    }

    const btnAtualizar = document.querySelector('.btn.btn-primary');
    if (btnAtualizar) {
        btnAtualizar.addEventListener('click', function(e) {
            e.preventDefault();
            atualizarGrafico();
        });
    }
}

// Verificar se a página do dashboard está visível
function isDashboardVisible() {
    const dashboard = document.getElementById('pagina-dashboard');
    return dashboard && dashboard.style.display !== 'none';
}

// Inicializar quando a página carregar
window.addEventListener('load', function() {
    if (isDashboardVisible()) {
        initGrafico();
        setupEventListeners();
        definirDatasIniciais(); // Define datas padrão
        carregarDados(); // Carrega dados iniciais
    }
});

// Função para ser chamada quando o dashboard for mostrado (se necessário)
function inicializarDashboard() {
    if (!graficoVendas) {
        initGrafico();
        setupEventListeners();
        definirDatasIniciais();
        carregarDados();
    }
}