class ManipulaForm {
    constructor(form1Btn, form2Btn, form1, form2) {
        this.form1Btn = form1Btn;
        this.form2Btn = form2Btn;
        this.form1 = form1;
        this.form2 = form2;
        this.init();
    }

    init() {
        window.onload = () => {
            this.showForm(this.form1, this.form2, this.form1Btn, this.form2Btn);
        };

        this.form1Btn.addEventListener('click', () => {
            this.showForm(this.form1, this.form2, this.form1Btn, this.form2Btn);
        });

        this.form2Btn.addEventListener('click', () => {
            this.showForm(this.form2, this.form1, this.form2Btn, this.form1Btn);
        });
    }

    showForm(showForm, hideForm, activeBtn, inactiveBtn) {
        showForm.style.display = "block";
        hideForm.style.display = "none";
        activeBtn.classList.add('active-tab');
        inactiveBtn.classList.remove('active-tab');
    }
}