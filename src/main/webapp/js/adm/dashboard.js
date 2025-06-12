let graficoVendas;

function initGrafico() {
    console.log('Iniciando gráfico...');

    const canvas = document.getElementById('dashboard');
    console.log('Canvas encontrado?', canvas);

    if (!canvas) {
        console.error('Canvas não encontrado');
        return;
    }

    const ctx = canvas.getContext('2d');
    console.log('Context 2D:', ctx);

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

    console.log('Gráfico criado:', graficoVendas);
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
            `http://localhost:8080/ecommerce_tenis_war_exploded/controleGrafico?dataInicial=${dataInicial}&dataFinal=${dataFinal}`
        );

        if (!response.ok) {
            throw new Error('Erro ao buscar dados do servidor');
        }

        const dados = await response.json();
        console.log('Dados da API:', dados);

        const dadosChart = transformarDados(dados);
        console.log('Dados transformados:', dadosChart);

        graficoVendas.data.labels = dadosChart.labels;
        graficoVendas.data.datasets = dadosChart.datasets;
        graficoVendas.update();

        graficoVendas.options.plugins.title.text =
            `Tendência de Vendas - ${formatarData(dataInicial)} até ${formatarData(dataFinal)}`;
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


function isDashboardVisible() {
    const dashboard = document.getElementById('pagina-dashboard');
    console.log('🔍 Verificando dashboard:', dashboard);

    if (!dashboard) {
        console.log('Elemento pagina-dashboard não encontrado');
        return false;
    }

    const style = window.getComputedStyle(dashboard);
    const isVisible = style.display !== 'none';
    console.log('Display atual:', style.display, 'Visível?', isVisible);

    return dashboard && dashboard.style.display !== 'none';
}

// Event Listeners para os botões
function setupEventListeners() {
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


const dashboard = document.getElementById('pagina-dashboard');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOMContentLoaded executado');

    // Verificar se a página do dashboard está visível
    const dashboardVisivel = isDashboardVisible();
    console.log('Dashboard visível?', dashboardVisivel);

    if (dashboardVisivel) {
        console.log('Iniciando dashboard...');
        initGrafico();
        setupEventListeners();
        definirDatasIniciais();
        carregarDados();
    } else {
        console.log('Dashboard não está visível');
        console.log('Element pagina-dashboard:', document.getElementById('pagina-dashboard'));
    }
});


function inicializarDashboard() {
    console.log('🔄 Forçando inicialização do dashboard...');

    if (!graficoVendas) {
        initGrafico();
        setupEventListeners();
        definirDatasIniciais();
        carregarDados();
    } else {
        console.log('⚠️ Gráfico já existe, apenas carregando dados...');
        carregarDados();
    }
}

// 🆘 FUNÇÃO DE EMERGÊNCIA - Execute no console se nada aparecer
function forcarInicializacao() {
    console.log('🚨 FORÇANDO INICIALIZAÇÃO...');

    // Mostrar a div se estiver oculta
    const dashboard = document.getElementById('pagina-dashboard');
    if (dashboard) {
        dashboard.style.display = 'block';
        console.log('📱 Dashboard mostrado');
    }

    // Aguardar um pouco e inicializar
    setTimeout(() => {
        initGrafico();
        setupEventListeners();
        definirDatasIniciais();
        carregarDados();
    }, 100);
}

// Função para transformar os dados da API para o formato do Chart.js
function transformarDados(dadosRaw) {
    // Extrair todos os meses únicos (labels do eixo X)
    const mesesUnicos = [...new Set(dadosRaw.map(item => item.mesAno))].sort();

    // Extrair todas as categorias únicas
    const categoriasUnicas = [...new Set(dadosRaw.map(item => item.categoria))];

    // Cores para as categorias (você pode personalizar)
    const cores = [
        '#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6',
        '#1abc9c', '#34495e', '#e67e22', '#95a5a6', '#c0392b'
    ];

    // Criar datasets para cada categoria
    const datasets = categoriasUnicas.map((categoria, index) => {
        // Para cada mês, encontrar a venda dessa categoria
        const dadosCategoria = mesesUnicos.map(mes => {
            const item = dadosRaw.find(d => d.categoria === categoria && d.mesAno === mes);
            return item ? item.vendas : 0; // Se não encontrar, assume 0
        });

        return {
            label: categoria,
            data: dadosCategoria,
            borderColor: cores[index % cores.length],
            backgroundColor: cores[index % cores.length] + '20', // Adiciona transparência
            tension: 0.4,
            fill: false,
            pointRadius: 5,
            pointHoverRadius: 8
        };
    });

    // Formatar labels dos meses para exibição
    const labelsFormatados = mesesUnicos.map(mesAno => {
        const [ano, mes] = mesAno.split('-');
        const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun',
            'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
        return `${meses[parseInt(mes) - 1]}/${ano}`;
    });

    return {
        labels: labelsFormatados,
        datasets: datasets
    };
}