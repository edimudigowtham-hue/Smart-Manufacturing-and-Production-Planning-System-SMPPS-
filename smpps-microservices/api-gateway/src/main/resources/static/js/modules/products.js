const loadProducts = () => run(async () => {
    state.products = await api('/api/products');
    state.orders = await api('/api/orders');
    renderProducts();
    fillProductSelects();
});

const productOrderCount = productId => state.orders.filter(order => Number(order.productId) === Number(productId)).length;

const renderProducts = () => {
    const totalProducts = state.products.length;
    const activeCount = state.products.filter(p => p.productStatus === 'ACTIVE').length;
    const inactiveCount = state.products.filter(p => p.productStatus === 'INACTIVE').length;
    const phasedOutCount = state.products.filter(p => p.productStatus === 'PHASED_OUT').length;
    const activeBomCount = state.products.filter(p => p.activeBomId).length;
    const protectedProducts = state.products.filter(p => productOrderCount(p.productId) > 0).length;
    const averageCost = totalProducts === 0 ? 0 : Math.round((state.products.reduce((sum, p) => sum + Number(p.standardCost || 0), 0) / totalProducts) * 100) / 100;

    renderSummaryCards('product-summary-cards', [
        ['Products', totalProducts, 'border-primary', 'bg-primary'],
        ['ACTIVE', activeCount, 'border-success', 'bg-success'],
        ['INACTIVE', inactiveCount, 'border-secondary', 'bg-secondary'],
        ['PHASED OUT', phasedOutCount, 'border-dark', 'bg-dark'],
        ['Active BOMs', activeBomCount, 'border-info', 'bg-info'],
        ['Used in Orders', protectedProducts, 'border-warning', 'bg-warning'],
        ['Avg Standard Cost', averageCost, 'border-secondary', 'bg-secondary'],
        ['Deletable', totalProducts - protectedProducts, 'border-success', 'bg-success']
    ]);

    document.getElementById('products-table').innerHTML = state.products.map(p => `
        <tr>
            <td>${esc(p.productId)}</td>
            <td>${esc(p.productName)}</td>
            <td>${esc(p.productCode)}</td>
            <td>${esc(p.activeBomId ? p.bomVersion : '-')}</td>
            <td>${esc(p.standardCost)}</td>
            <td><span class="badge ${badgeClass(p.productStatus)}">${esc(p.productStatus)}</span></td>
            <td>
                ${canManageProducts() ? `<button class="btn btn-sm btn-warning" onclick="editProduct(${p.productId})">Edit</button>` : ''}
                <button class="btn btn-sm btn-info text-white" onclick="showProductStructure(${p.productId})">Structure</button>
                ${canManageProducts() && productOrderCount(p.productId) === 0 ? `<button class="btn btn-sm btn-danger" onclick="deleteProduct(${p.productId})">Delete</button>` : ''}
                ${productOrderCount(p.productId) > 0 ? `<span class="badge bg-warning text-dark">Used in ${productOrderCount(p.productId)} order(s)</span>` : ''}
            </td>
        </tr>`).join('');
};

const fillProductSelects = () => {
    const options = '<option value="">Select Product</option>' + state.products.map(p =>
        `<option value="${esc(p.productId)}">${esc(p.productCode)} - ${esc(p.productName)}</option>`
    ).join('');
    const activeOptions = '<option value="">Select Active Product</option>' + state.products
        .filter(p => p.productStatus === 'ACTIVE')
        .map(p => `<option value="${esc(p.productId)}">${esc(p.productCode)} - ${esc(p.productName)}</option>`)
        .join('');
    document.getElementById('orderProductId').innerHTML = activeOptions;
    document.getElementById('bomVersionProductId').innerHTML = options;
    document.getElementById('componentProductId').innerHTML = options;
};

const loadBomOptions = async productId => {
    const select = document.getElementById('componentBomId');
    if (!productId) {
        select.innerHTML = '<option value="">Select BOM Version</option>';
        return [];
    }
    const boms = await api(`/api/products/${productId}/boms`);
    select.innerHTML = '<option value="">Select BOM Version</option>' + boms.map(bom =>
        `<option value="${esc(bom.bomId)}">${esc(bom.bomVersion)}</option>`
    ).join('');
    return boms;
};

window.editProduct = id => {
    const p = state.products.find(item => item.productId === id);
    if (!p) return;
    document.getElementById('productId').value = p.productId;
    document.getElementById('productName').value = p.productName;
    document.getElementById('productCode').value = p.productCode;
    document.getElementById('standardCost').value = p.standardCost;
    document.getElementById('productStatus').value = p.productStatus;
};

window.showProductStructure = id => run(async () => {
    const structure = await api(`/api/products/${id}/structure`);
    const components = structure.components || [];
    document.getElementById('product-structure-title').textContent = `Structure - ${structure.productCode}`;
    document.getElementById('product-structure-summary').innerHTML = `
        <div class="row g-3">
            <div class="col-md-6"><strong>Product:</strong> ${esc(structure.productName)}</div>
            <div class="col-md-3"><strong>Code:</strong> ${esc(structure.productCode)}</div>
            <div class="col-md-3"><strong>Status:</strong> ${esc(structure.productStatus)}</div>
            <div class="col-md-6"><strong>Active BOM:</strong> ${esc(structure.bomVersion || 'Not assigned')}</div>
            <div class="col-md-3"><strong>Standard Cost:</strong> ${esc(structure.standardCost)}</div>
            <div class="col-md-3"><strong>Components:</strong> ${esc(components.length)}</div>
        </div>`;
    document.getElementById('product-structure-components').innerHTML = components.length
        ? components.map(component => `
            <tr>
                <td>${esc(component.componentCode)}</td>
                <td>${esc(component.componentName)}</td>
                <td>${esc(component.quantity)}</td>
                <td>${esc(component.unitOfMeasure)}</td>
                <td>${esc(component.bomVersion)}</td>
            </tr>`).join('')
        : '<tr><td colspan="5" class="text-center text-muted">No BOM components added for the active BOM.</td></tr>';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('product-structure-modal')).show();
});

window.deleteProduct = id => {
    if (productOrderCount(id) > 0) {
        showAlert(`Cannot delete product because it is used in ${productOrderCount(id)} production order(s)`, 'warning');
        return;
    }
    if (!confirm('Delete this product?')) return;
    run(async () => {
        await api(`/api/products/${id}`, { method: 'DELETE' });
        await loadProducts();
        await loadDashboard();
    }, 'Product deleted');
};

const resetProductForm = () => document.getElementById('product-form').reset() || (document.getElementById('productId').value = '');

const registerProductHandlers = () => {
    document.getElementById('product-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        const id = document.getElementById('productId').value;
        const payload = {
            productName: document.getElementById('productName').value,
            productCode: document.getElementById('productCode').value,
            standardCost: Number(document.getElementById('standardCost').value),
            productStatus: document.getElementById('productStatus').value
        };
        await api(id ? `/api/products/${id}` : '/api/products', { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        resetProductForm();
        await loadProducts();
        await loadDashboard();
    }, 'Product saved'));

    document.getElementById('create-bom-button').addEventListener('click', () => run(async () => {
        const productId = document.getElementById('bomVersionProductId').value;
        const payload = {
            bomVersion: document.getElementById('newBomVersion').value
        };
        await api(`/api/products/${productId}/boms`, { method: 'POST', body: JSON.stringify(payload) });
        document.getElementById('newBomVersion').value = '';
        await loadProducts();
        if (document.getElementById('componentProductId').value === productId) {
            await loadBomOptions(productId);
        }
    }, 'BOM version created'));

    document.getElementById('componentProductId').addEventListener('change', event => run(async () => {
        await loadBomOptions(event.target.value);
    }));

    document.getElementById('activate-bom-button').addEventListener('click', () => run(async () => {
        const productId = document.getElementById('componentProductId').value;
        const bomId = document.getElementById('componentBomId').value;
        if (!productId || !bomId) {
            throw new Error('Select a product and BOM version to activate');
        }
        await api(`/api/products/${productId}/boms/${bomId}/activate`, { method: 'PATCH' });
        await loadProducts();
        await loadBomOptions(productId);
    }, 'BOM version activated'));

    document.getElementById('bom-component-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        const productId = document.getElementById('componentProductId').value;
        const bomId = document.getElementById('componentBomId').value;
        const payload = {
            componentCode: document.getElementById('componentCode').value,
            componentName: document.getElementById('componentName').value,
            quantity: Number(document.getElementById('componentQuantity').value),
            unitOfMeasure: document.getElementById('unitOfMeasure').value
        };
        await api(`/api/products/${productId}/boms/${bomId}/components`, { method: 'POST', body: JSON.stringify(payload) });
        document.getElementById('componentCode').value = '';
        document.getElementById('componentName').value = '';
        document.getElementById('componentQuantity').value = '';
        document.getElementById('unitOfMeasure').value = '';
    }, 'BOM component added'));
};
