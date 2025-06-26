document.addEventListener('DOMContentLoaded', function() {
    const input = document.getElementById('message-input');

    input.addEventListener('keypress', function(event) {
        // Verifica se a tecla pressionada foi Enter (código 13 ou 'Enter')
        if (event.key === 'Enter' || event.keyCode === 13) {
            event.preventDefault();
            enviarMensagem();
        }
    });
});

// --- URL Utilities ---
function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}

// --- Estado Global ---
const cpf = obterParametroUrl("cpf");

function enviarMensagem(){
    var msg = document.getElementById('message-input')
    if(!msg.value.trim()){
        msg.style.border = '2px solid red'
        setTimeout(()=>{
            msg.style.border = ''
        }, 3000);
        return
    }

    var status = document.getElementById('status')
    var btn = document.getElementById('btn-submit')
    var pergunta;

    status.style.display = 'block'
    status.innerHTML = 'Enviando...'
    btn.disabled = true
    btn.style.cursor = 'not-allowed'
    pergunta = msg.value
    msg.value = ''
    msg.disabled = true
    const originalContent = btn.innerHTML
    btn.innerHTML = '<div class="loader"></div>'

    fetch("http://localhost:8080/ecommerce_tenis_war_exploded/chatbot", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            "requisicao": {
                "cliente": {
                    "cpf": cpf
                },
                "pergunta": pergunta
            }
        })
    })
        .then(response => response.json()) // Converte a resposta para JSON
        .then(data => { // Renomeado para 'data' para maior clareza
            // Verifica se a resposta contém a propriedade 'resposta' (sucesso)
            if (data.resposta) {
                exibirHistorico(pergunta, data.resposta);
            }
            // Caso contrário, verifica se contém a propriedade 'erro'
            else if (data.erro) {
                exibirHistorico(pergunta, `Desculpe, ocorreu um erro: ${data.erro}`);
            }
            // Fallback para uma resposta inesperada
            else {
                exibirHistorico(pergunta, "Não foi possível obter uma resposta do assistente.");
            }
        })
        .catch((e) => {
            console.log("Error -> ", e);
            // Exibe uma mensagem amigável em caso de falha de rede ou erro de parsing
            exibirHistorico(pergunta, "Houve um problema de comunicação. Por favor, tente novamente.");
        })
        .finally(() => {
            // Código para reabilitar o botão de envio
            status.style.display = 'none';
            msg.disabled = false;
            btn.disabled = false;
            btn.style.cursor = 'pointer';
            btn.innerHTML = originalContent;
        });
}

function exibirHistorico(message, response){
    let div = document.getElementById("historic")

    //Mensagem Usuario
    let userMsg = document.createElement('div');
    userMsg.className = 'box-user-message'

    let myMessage = document.createElement('p');
    myMessage.className = 'user-message'
    myMessage.innerHTML = message

    userMsg.appendChild(myMessage)
    div.appendChild(userMsg)

    //Mensagem Bot
    let botMsg = document.createElement('div')
    botMsg.className = 'box-bot-message'

    let botMessage = document.createElement('p')
    botMessage.className = 'bot-message'
    botMessage.innerHTML = response

    botMsg.appendChild(botMessage)
    div.appendChild(botMsg)

    //Levar scroll para o final
    div.scrollTop = div.scrollHeight;
}