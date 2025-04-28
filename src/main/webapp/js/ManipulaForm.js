class ManipulaForm {
    constructor(form1Btn, form2Btn, form1, form2, form3Btn = null, form3 = null, form4Btn = null, form4 = null) {
        this.forms = [form1, form2, form3, form4].filter(f => f !== null);
        this.buttons = [form1Btn, form2Btn, form3Btn, form4Btn].filter(b => b !== null);
        this.tables = {};
        this.callbacks = {}; // Armazena callbacks para cada aba
        this.init();
    }

    onShowTab(tabId, callback) {
        this.callbacks[tabId] = callback;
    }

    init() {
        window.addEventListener('load', () => {
            this.showForm(this.forms[0], this.buttons[0]);
        });

        this.buttons.forEach((button, index) => {
            button.addEventListener('click', () => {
                this.showForm(this.forms[index], this.buttons[index]);
            });
        });
    }

    showForm(showForm, activeBtn) {
        // Esconder todas as abas
        this.forms.forEach(form => {
            if (form !== showForm) {
                this.destroyTable(form);
                form.style.display = "none";
            }
        });

        // Atualizar botões ativos
        this.buttons.forEach(btn => btn.classList.remove('active-tab'));
        activeBtn.classList.add('active-tab');

        // Mostrar a aba selecionada
        showForm.style.display = "block";

        // Inicializar tabela
        this.initTable(showForm);

        // Executar callback específico da aba
        const tabId = showForm.id;
        if (this.callbacks[tabId]) {
            this.callbacks[tabId]();
        }
    }

    initTable(form) {
        const table = form.querySelector("table");
        if (table) {
            const tableId = table.id;
            if (!$.fn.DataTable.isDataTable(`#${tableId}`)) {
                if (['table-vendas', 'table-trocas', 'table-produtos'].includes(tableId)) {
                    // Aplica configuração especial só para tabelas específicas
                    this.tables[tableId] = $(table).DataTable({
                        pageLength: 5,
                        searching: false,
                        lengthMenu: [3, 5, 10, 25, 50],
                        language: {
                            lengthMenu: "Mostrar _MENU_ registros por página",
                            zeroRecords: "Nenhum resultado encontrado",
                            info: "Página _PAGE_ de _PAGES_",
                            paginate: {
                                first: "Primeira",
                                last: "Última",
                                next: "Próxima",
                                previous: "Anterior"
                            }
                        }
                    });
                } else {
                    // Para outras tabelas, configuração padrão
                    this.tables[tableId] = $(table).DataTable();
                }
            }
        }
    }


    destroyTable(form) {
        const table = form.querySelector("table");
        if (table) {
            const tableId = table.id;
            if ($.fn.DataTable.isDataTable(`#${tableId}`)) {
                this.tables[tableId].destroy();
                delete this.tables[tableId]; // Apenas remove a referência
                // NÃO dar .empty(), deixa a tabela no DOM
            }
        }
    }
}
