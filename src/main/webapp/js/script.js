import ManipulaForm from "./ManipulaForm.js";
import ManipulaDataForm from "./ManipulaDataForm.js";

// Obtenha os botões do título dos formulários
const dadoPessoalBtn = document.getElementById("dados-pessoais-title");
const enderecoBtn = document.getElementById("endereco-title"); // Corrigido o id

const dadoPessoal = document.getElementById("form-dados-pessoais");
const endereco = document.getElementById("form-endereco");

const manipulaForm = new ManipulaForm(dadoPessoalBtn, enderecoBtn, dadoPessoal, endereco);
const manipulaDataForm = new ManipulaDataForm(dadoPessoal, endereco);