// Obtenha os botões do título dos formulários
const vendasBtn = document.getElementById("vendas-title");
const trocasBtn = document.getElementById("trocas-title");
const produtosBtn = document.getElementById("produtos-title");
const dashboardBtn = document.getElementById("dashboard-title");


const vendas = document.getElementById("table-vendas");
const trocas = document.getElementById("table-trocas");
const produtos = document.getElementById("table-produtos");
const dashborad = document.getElementById("table-dashborad");



const manipulaForm = new ManipulaForm(
    document.getElementById("vendas-title"),
    document.getElementById("trocas-title"),
    document.getElementById("pagina-vendas"),
    document.getElementById("pagina-trocas"),
    document.getElementById("produtos-title"),
    document.getElementById("pagina-produtos"),
    document.getElementById("dashboard-title"),
    document.getElementById("pagina-dashboard"),
);

//const manipulaForm = new ManipulaForm(vendasBtn, trocasBtn, vendas, trocas, produtosBtn, produtos, dashboardBtn, dashborad);


function paginarTabela(idtabela) {
    $(document).ready(function () {
        $("#"+idtabela).DataTable({
            "pageLength": 5,
            "searching": false,
            "lengthMenu": [3, 5, 10, 25, 50],  // Opções de quantidade de itens por página
            "language": {
                "lengthMenu": "Mostrar _MENU_ registros por página",
                "zeroRecords": "Nenhum resultado encontrado",
                "info": "Página _PAGE_ de _PAGES_",
                "paginate": {
                    "first": "Primeira",
                    "last": "Última",
                    "next": "Próxima",
                    "previous": "Anterior"
                }
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", function () {
    paginarTabela('table-vendas');

});