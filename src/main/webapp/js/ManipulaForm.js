class ManipulaForm {
    constructor(form1Btn, form2Btn, form1, form2, form3Btn = null, form3 = null, form4Btn = null, form4 = null) {
        this.forms = [form1, form2, form3, form4].filter(f => f !== null);
        this.buttons = [form1Btn, form2Btn, form3Btn, form4Btn].filter(b => b !== null);
        this.tables = {}; // Armazena instâncias do DataTables
        this.init();
    }

    init() {
        // Mostrar o primeiro formulário ao carregar a página
        window.addEventListener('load', () => {
            this.showForm(this.forms[0], this.buttons[0]);
        });

        // Adicionar eventos de clique para cada botão
        this.buttons.forEach((button, index) => {
            button.addEventListener('click', () => {
                this.showForm(this.forms[index], this.buttons[index]);
            });
        });
    }

    showForm(showForm, activeBtn) {
        // Esconder todas as abas e remover paginação
        this.forms.forEach(form => {
            if (form !== showForm) {
                this.destroyTable(form); // Destruir tabela ao sair da aba
                form.style.display = "none";
            }
        });

        // Resetar botões
        this.buttons.forEach(btn => btn.classList.remove('active-tab'));

        // Exibir a aba correta
        showForm.style.display = "block";
        activeBtn.classList.add('active-tab');

        // Reinicializar DataTables apenas se for necessário
        this.initTable(showForm);
    }

    initTable(form) {
        const table = form.querySelector("table"); // Encontra a tabela dentro do form
        if (table) {
            const tableId = table.id;
            if (!$.fn.DataTable.isDataTable(`#${tableId}`)) {
                // Somente inicializa se ainda não estiver inicializado
                this.tables[tableId] = $(table).DataTable();
            }
        }
    }

    destroyTable(form) {
        const table = form.querySelector("table");
        if (table) {
            const tableId = table.id;
            if ($.fn.DataTable.isDataTable(`#${tableId}`)) {
                // Se a tabela já está inicializada, destruí-la corretamente
                this.tables[tableId].destroy();
                $(`#${tableId}`).empty(); // Remove os elementos de paginação e cabeçalho
                delete this.tables[tableId]; // Remove a referência da instância
            }
        }
    }
}
