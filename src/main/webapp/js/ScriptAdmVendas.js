// Obtenha os botões do título dos formulários
const vendasBtn = document.getElementById("vendas-title");
const trocasBtn = document.getElementById("trocas-title");
const produtosBtn = document.getElementById("produtos-title");
const dashboardBtn = document.getElementById("dashboard-title");

const vendas = document.getElementById("pagina-vendas");
const trocas = document.querySelector(".pagina-trocas");
const produtos = document.querySelector(".pagina-produtos");
const dashboard = document.getElementById("pagina-dashboard");



const manipulaForm = new ManipulaForm(
    vendasBtn, trocasBtn, vendas, trocas, produtosBtn, produtos, dashboardBtn, dashboard
);

//const manipulaForm = new ManipulaForm(vendasBtn, trocasBtn, vendas, trocas, produtosBtn, produtos, dashboardBtn, dashborad);


// function paginarTabela(idtabela) {
//     $(document).ready(function () {
//         $("#"+idtabela).DataTable({
//             "pageLength": 5,
//             "searching": false,
//             "lengthMenu": [3, 5, 10, 25, 50],  // Opções de quantidade de itens por página
//             "language": {
//                 "lengthMenu": "Mostrar _MENU_ registros por página",
//                 "zeroRecords": "Nenhum resultado encontrado",
//                 "info": "Página _PAGE_ de _PAGES_",
//                 "paginate": {
//                     "first": "Primeira",
//                     "last": "Última",
//                     "next": "Próxima",
//                     "previous": "Anterior"
//                 }
//             }
//         });
//     });
// }
//
// document.addEventListener("DOMContentLoaded", function () {
//     paginarTabela('table-vendas');
//
// });