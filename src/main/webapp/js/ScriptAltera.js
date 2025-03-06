// Obtenha os botões do título dos formulários
const dadoPessoalBtn = document.getElementById("dados-pessoais-title");
const enderecoBtn = document.getElementById("endereco-consulta-title");
const cartaoBtn = document.getElementById("cartao-consulta-title")


const dadoPessoal = document.getElementById("form-dados-pessoais");
const endereco = document.getElementById("endereco-consulta");
const cartao = document.getElementById("cartao-consulta")

console.log(dadoPessoal);
console.log(endereco);
console.log(dadoPessoalBtn);
console.log(enderecoBtn);


const manipulaForm = new ManipulaForm(dadoPessoalBtn, enderecoBtn, dadoPessoal, endereco, cartaoBtn, cartao);