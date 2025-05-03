const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";
const searchInput = document.getElementById("pesquisa");
const categorySelect = document.getElementById("categorias");
const produtosContainer = document.getElementById("produtos-grid");

let produtosOriginais = [];

document.addEventListener("DOMContentLoaded", function() {
    consultaProdutos();
});

async function consultaProdutos() {
    try {
        const response = await fetchAPI(`${BASE_URL}/controleProduto`, "Erro ao buscar produtos");
        const produtos = Array.isArray(response) ? response : [response];

        produtosOriginais = produtos;
        produtos.length ? renderProdutos(produtos) : mostrarErro('Nenhum produto encontrado ou resposta inválida.');
    } catch (error) {
        mostrarErro('Erro ao buscar produtos.', error);
    }
}

function renderProdutos(produtos) {
    produtosContainer.innerHTML = "";
    if (produtos.length === 0) {
        produtosContainer.innerHTML = "<p>Nenhum tênis encontrado.</p>";
        return;
    }

    const modelosUnicos = new Map();
    produtos.forEach(produto => {
        // Se já tiver o modelo, pula
        if (!modelosUnicos.has(produto.modelo)) {
            modelosUnicos.set(produto.modelo, produto);
        }
    });

    modelosUnicos.forEach(produto => {
        const div = document.createElement("div");
        div.classList.add("card");

        const parcela = (produto.preco / 10).toFixed(2);

        div.innerHTML = `
            <img src="../../../img/${escapeHtml(produto.caminhoFoto || '')}" alt="${escapeHtml(produto.nome || '')}"/>
            <h2>${escapeHtml(produto.nome || '')}</h2>
            <p class="price">R$ ${escapeHtml(produto.preco.toFixed(2))}</p>
            <p>Até 10x de R$ ${parcela}</p>
            <a href="clienteProduto.html?modelo=${escapeHtml(produto.modelo || '')}">
                <button>COMPRAR</button>
            </a>
        `;

        produtosContainer.appendChild(div);
    });
}

function filtrarTenis() {
    const busca = searchInput.value.toLowerCase();
    const categoria = categorySelect.value;

    const filtrados = produtosOriginais.filter(produto => {
        const nomeMatch = produto.nome.toLowerCase().includes(busca);
        const categoriaMatch = !categoria || produto.categoria?.nome === categoria;
        return nomeMatch && categoriaMatch;
    });

    renderProdutos(filtrados);
}

// Eventos
searchInput.addEventListener("input", filtrarTenis);
categorySelect.addEventListener("change", filtrarTenis);

function escapeHtml(str) {
    const div = document.createElement('div');
    div.innerText = str;
    return div.innerHTML;
}

function mostrarErro(mensagem, error) {
    console.error(mensagem, error);
    alert(mensagem);
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