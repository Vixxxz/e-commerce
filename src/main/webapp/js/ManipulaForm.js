class ManipulaForm {
    constructor(form1Btn, form2Btn, form1, form2, form3Btn = null, form3 = null) {
        this.form1Btn = form1Btn;
        this.form2Btn = form2Btn;
        this.form1 = form1;
        this.form2 = form2;
        this.form3Btn = form3Btn;
        this.form3 = form3;
        this.init();
    }

    init() {
        if (this.form3Btn && this.form3) {
            // Caso haja um terceiro formulário e botão
            window.addEventListener('load', () => {
                this.showForm(this.form1, [this.form2, this.form3], this.form1Btn, [this.form2Btn, this.form3Btn]);
            });

            this.form1Btn.addEventListener('click', () => {
                this.showForm(this.form1, [this.form2, this.form3], this.form1Btn, [this.form2Btn, this.form3Btn]);
            });

            this.form2Btn.addEventListener('click', () => {
                this.showForm(this.form2, [this.form1, this.form3], this.form2Btn, [this.form1Btn, this.form3Btn]);
            });

            this.form3Btn.addEventListener('click', () => {
                this.showForm(this.form3, [this.form1, this.form2], this.form3Btn, [this.form1Btn, this.form2Btn]);
            });
        } else {
            // Caso haja apenas dois formulários e botões
            window.addEventListener('load', () => {
                this.showForm(this.form1, [this.form2], this.form1Btn, [this.form2Btn]);
            });

            this.form1Btn.addEventListener('click', () => {
                this.showForm(this.form1, [this.form2], this.form1Btn, [this.form2Btn]);
            });

            this.form2Btn.addEventListener('click', () => {
                this.showForm(this.form2, [this.form1], this.form2Btn, [this.form1Btn]);
            });
        }
    }

    showForm(showForm, hideForms, activeBtn, inactiveBtns) {
        showForm.style.display = "block";
        hideForms.forEach(form => form.style.display = "none");

        activeBtn.classList.add('active-tab');
        inactiveBtns.forEach(btn => btn.classList.remove('active-tab'));
    }
}