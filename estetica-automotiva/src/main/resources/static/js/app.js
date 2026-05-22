// AutoBrilho — scripts auxiliares

document.addEventListener('DOMContentLoaded', function () {
    lucide.createIcons();
    initPlacasMask();
    initCpfMask();
    initTelefoneMask();
    initServicoSelector();
    autoCloseAlerts();
});

function initPlacasMask() {
    document.querySelectorAll('input[name="placas"], input[name="placa"]').forEach(el => {
        el.addEventListener('input', () => {
            el.value = el.value.toUpperCase().replace(/[^A-Z0-9\-]/g, '');
        });
    });
}

function initCpfMask() {
    const cpfInput = document.querySelector('input[name="cpf"]');
    if (!cpfInput) return;
    cpfInput.addEventListener('input', () => {
        let v = cpfInput.value.replace(/\D/g, '');
        if (v.length > 11) v = v.slice(0, 11);
        v = v.replace(/(\d{3})(\d)/, '$1.$2');
        v = v.replace(/(\d{3})(\d)/, '$1.$2');
        v = v.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
        cpfInput.value = v;
    });
}

function initTelefoneMask() {
    const tel = document.querySelector('input[name="telefone"]');
    if (!tel) return;
    tel.addEventListener('input', () => {
        let v = tel.value.replace(/\D/g, '');
        if (v.length > 11) v = v.slice(0, 11);
        if (v.length > 6) v = '(' + v.slice(0,2) + ') ' + v.slice(2,7) + '-' + v.slice(7);
        else if (v.length > 2) v = '(' + v.slice(0,2) + ') ' + v.slice(2);
        tel.value = v;
    });
}

function initServicoSelector() {
    document.querySelectorAll('.servico-selecionavel').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('.servico-selecionavel').forEach(c => c.classList.remove('selecionado'));
            card.classList.add('selecionado');
            const input = document.getElementById('servicoIdInput');
            if (input) input.value = card.dataset.id;
        });
    });
}

function autoCloseAlerts() {
    setTimeout(() => {
        document.querySelectorAll('.alert.fade.show').forEach(alert => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        });
    }, 5000);
}

function adicionarPlaca() {
    const container = document.getElementById('placasContainer');
    if (!container) return;
    const div = document.createElement('div');
    div.className = 'input-group mb-2';
    div.innerHTML = `
        <span class="input-group-text">
            <i data-lucide="car" style="width:14px;height:14px;"></i>
        </span>
        <input type="text" name="placas" class="form-control"
               placeholder="Ex: ABC-1234" maxlength="8"
               style="text-transform:uppercase;"/>
        <button type="button" class="btn btn-outline-danger btn-sm"
                onclick="this.parentElement.remove()">
            <i data-lucide="x" style="width:13px;height:13px;"></i>
        </button>
    `;
    container.appendChild(div);
    lucide.createIcons();
}
