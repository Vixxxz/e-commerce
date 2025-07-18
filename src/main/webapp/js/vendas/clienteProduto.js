const BASE_URL = "http://localhost:8080/ecommerce_tenis_war_exploded";
const produtoScreen = document.querySelector('#produto');

function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}
const produtoModelo = obterParametroUrl("modelo");

document.addEventListener("DOMContentLoaded", () => {
    if (produtoModelo) {
        carregarDadosProduto(produtoModelo);
    }
    document.addEventListener("click", (event) => {
        if (event.target.closest("#add-carrinho")) {
            const sku = document.querySelector("#produto-sku")?.innerText;
            const nome = document.querySelector("#produto-nome")?.innerText;
            const preco = document.querySelector("#produto-preco")?.innerText.replace("R$ ", "").replace(",", ".");
            const imagem = document.querySelector("#produto-imagem")?.getAttribute("src");
            const tamanhoSelecionado = document.querySelector("input[name='tamanho']:checked")?.value;
            const produtoStorage = sessionStorage.getItem("produto");
            const produto = produtoStorage ? JSON.parse(produtoStorage) : null;

            if (!sku || !tamanhoSelecionado) {
                alert("Erro ao identificar produto ou tamanho.");
                return;
            }

            adicionarProdutoAoCarrinho({
                idTenis: produto.id,
                marca: produto.marca.id,
                sku,
                nome,
                preco: parseFloat(preco),
                tamanho: parseInt(tamanhoSelecionado),
                imagem,
                quantidade: 1,
                categoria: produto.categoria.id
            });

            alert("Produto adicionado ao carrinho!");
        }
    });
});

//todo: revisar a função, quando tenta comprar mais de 1 tenis do mesmo modelo de tamanho diferentes da erro pois envia o msm id para os produtos
async function carregarDadosProduto(modelo) {
    try {
        const response = await fetchAPI(`${BASE_URL}/controleProduto?modelo=${modelo}`, "Erro ao buscar produto");
        const produto = Array.isArray(response) ? response : [response];

        sessionStorage.setItem("produto", JSON.stringify(Array.isArray(response) ? response[0] : response));

        console.log("Produto recebido:", JSON.stringify(Array.isArray(response) ? response[0] : response, null, 2));
        preencherDados(produto);

    } catch (error) {
        console.error("Erro ao carregar produto:", error);
    }
}

async function adicionarProdutoAoCarrinho(produto) {
    // 1. Prepara o payload para a reserva
    const payloadReserva = {
        reserva: [ // A API espera uma lista, então enviamos o item dentro de um array
            {
                produto: { id: produto.idTenis },
                marca: { id: produto.marca },
                quantidade: 1 // Reserva inicial de 1 unidade
            }
        ]
    };

    try {
        // 2. Envia a requisição para criar a reserva no backend
        const response = await fetch('http://localhost:8080/ecommerce_tenis_war_exploded/reservaEstoque', {
            method: 'POST',
            body: JSON.stringify(payloadReserva),
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const erro = await response.json();
            // Se a reserva falhar (ex: sem estoque), exibe o erro e não adiciona ao carrinho
            alert(`Não foi possível adicionar ao carrinho: ${erro}`);
            return; // Interrompe a execução
        }


        // Captura o sessionId retornado pelo backend
        const returnedSessionId = await response.json();

        // Salva o sessionId no sessionStorage se ele ainda não existir
        if (returnedSessionId && !sessionStorage.getItem("sessionId")) {
            sessionStorage.setItem("sessionId", returnedSessionId);
            console.log("Session ID salvo:", returnedSessionId);
        }

        // 3. Se a reserva foi bem-sucedida, prossegue com a lógica de adicionar ao sessionStorage
        const carrinho = JSON.parse(sessionStorage.getItem("carrinho")) || [];
        const index = carrinho.findIndex(item =>
            item.sku === produto.sku &&
            item.tamanho === produto.tamanho
        );

        if (index > -1) {
            carrinho[index].quantidade += 1;
        } else {
            carrinho.push(produto);
        }

        sessionStorage.setItem("carrinho", JSON.stringify(carrinho));
        alert("Produto adicionado ao carrinho!");

    } catch (error) {
        console.error("Erro ao criar reserva:", error);
        alert("Ocorreu um erro de comunicação ao tentar adicionar o produto ao carrinho.");
    }
}

function preencherDados(produtos) {
    if (!produtoScreen) {
        console.error("Elemento com ID 'produto-screen' não encontrado no DOM");
        return;
    }

    produtoScreen.innerHTML = "";
    if (produtos.length === 0) {
        produtoScreen.innerHTML = "<p>Nenhum tênis encontrado.</p>";
        return;
    }

    const produto = produtos[0];

    // Extrair tamanhos únicos
    const tamanhosUnicos = [...new Set(produtos.map(p => p.tamanho))];

    produtoScreen.innerHTML = `
        <div>
            <img src="../../../img/${escapeHtml(produto.caminhoFoto)}" alt="Tenis" id="produto-imagem">
        </div>
        <div class="info-produto">
            <div style="display: flex; flex-direction: column">
                <span id="caminho">Home/Tênis/${escapeHtml(produto.nome)}</span>
                <h3 id="produto-nome">${escapeHtml(produto.nome)}</h3>
                <span id="produto-sku">${escapeHtml(produto.sku)}</span>
                <p id="produto-preco">R$ ${escapeHtml(produto.preco)}</p>
                <h5 id="tamanho">Tamanhos</h5>
                <div class="tamanhos-container">
                  ${tamanhosUnicos.map((tam, index) => `
                    <label class="btn-tamanho${index === 0 ? ' checked' : ''}">
                      <input 
                        type="radio" 
                        name="tamanho" 
                        value="${tam}" 
                        ${index === 0 ? 'checked' : ''}
                      />
                      ${tam}
                    </label>
                  `).join('')}
                </div>
                <button id="add-carrinho" class="add-carrinho">
                    <img src="../../../img/Vector.svg" alt="tela">Adicionar ao carrinho
                </button>
            </div>
        </div>
    `;

    document.querySelectorAll('input[name="tamanho"]').forEach(input => {
        input.addEventListener('change', () => {
            document.querySelectorAll('.btn-tamanho').forEach(label => {
                label.classList.remove('checked');
            });

            input.closest('label').classList.add('checked');
        });
    });
}



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
