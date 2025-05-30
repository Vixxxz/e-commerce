// --- URL Utilities ---
function obterParametroUrl(parametro) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(parametro);
}

// --- Estado Global ---
const cpf = obterParametroUrl("cpf");

function enviarMensagem(){
    var msg = document.getElementById('message-input')
    if(!msg.value){
        msg.style.border = '2px solid red'
        return
    }
    msg.style.border = 'none'

    var status = document.getElementById('status')
    var btn = document.getElementById('btn-submit')

    status.innerHTML = 'Enviando...'
    btn.disabled = true
    btn.cursor = 'not-allowed'
    msg.disabled = true

    fetch("http://localhost:8080/ecommerce_tenis_war_exploded/chatbot",{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({

        "requisicao": {
        "cliente":{
            "cpf": cpf
        },
        "pergunta": msg.value
    }
        })
    })
        .then((response) =>response.json())
        .then((response)=> {
            let r = response.resposta
            exibirHistorico(msg.value, r)
        })
        .catch((e) =>{
            console.log("Error -> ", e)
        })
        .finally(()=>{
            status.style.display = 'none'
            btn.disabled = false
            btn.cursor = 'pointer'
            msg.disabled = false
            msg.value = ''
        })
}

function exibirHistorico(message, response){
    div = document.getElementById("historic")

    //Mensagem Usuario
    var userMsg = document.createElement('div')
    userMsg.className = 'box-user-message'

    var myMessage = document.createElement('p')
    myMessage.className = 'user-message'
    myMessage.innerHTML = message

    userMsg.appendChild(myMessage)
    div.appendChild(userMsg)


    //Mensagem Bot
    var botMsg = document.createElement('div')
    botMsg.className = 'box-bot-message'

    var botMessage = document.createElement('p')
    botMessage.className = 'bot-message'
    botMessage.innerHTML = reponse

    botMsg.appendChild(botMessage)
    div.appendChild(botMsg)

    //Levar scroll para o final
    historic.scrollTop = historic.scrollHeight;
}