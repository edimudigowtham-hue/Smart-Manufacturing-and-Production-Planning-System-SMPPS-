const loadOrders = () => run(async () => {
    state.products = await api('/api/products');
    state.orders = await api('/api/orders');
    if (canManageOrders() || canStartOrders() || canUpdateProducedQuantity()) {
        await loadMachineLists();
    }
    fillProductSelects();
    renderOrders();
});

const productLabel = productId => {
    const p = state.products.find(item => item.productId === productId);
    return p ? `${p.productCode} - ${p.productName}` : productId;
};

const isProducedQuantityEditable = order => Boolean(
    order
    && order.workCenterId
    && !['COMPLETED', 'CANCELLED'].includes(order.orderStatus)
);

const renderOrders = () => {
    const totalOrders = state.orders.length;
    const plannedCount = state.orders.filter(o => o.orderStatus === 'PLANNED').length;
    const inProgressCount = state.orders.filter(o => o.orderStatus === 'IN_PROGRESS').length;
    const completedCount = state.orders.filter(o => o.orderStatus === 'COMPLETED').length;
    const cancelledCount = state.orders.filter(o => o.orderStatus === 'CANCELLED').length;
    const totalPlanned = state.orders.reduce((sum, o) => sum + Number(o.plannedQuantity || 0), 0);
    const totalProduced = state.orders.reduce((sum, o) => sum + Number(o.producedQuantity || 0), 0);
    const scheduledCount = state.orders.filter(o => o.workCenterId).length;

    renderSummaryCards('order-summary-cards', [
        ['Orders', totalOrders, 'border-primary', 'bg-primary'],
        ['PLANNED', plannedCount, 'border-secondary', 'bg-secondary'],
        ['IN PROGRESS', inProgressCount, 'border-warning', 'bg-warning'],
        ['COMPLETED', completedCount, 'border-success', 'bg-success'],
        ['CANCELLED', cancelledCount, 'border-dark', 'bg-dark'],
        ['Planned Qty', totalPlanned, 'border-primary', 'bg-primary'],
        ['Produced Qty', totalProduced, 'border-success', 'bg-success'],
        ['Scheduled Machines', scheduledCount, 'border-info', 'bg-info']
    ]);

    document.getElementById('orders-table').innerHTML = state.orders.map(o => `
        <tr>
            <td>${esc(o.orderId)}</td>
            <td>${esc(productLabel(o.productId))}</td>
            <td>${esc(o.plannedQuantity)}</td>
            <td>${esc(o.producedQuantity)}</td>
            <td>${esc(o.startDate)}</td>
            <td>${esc(o.endDate)}</td>
            <td><span class="badge ${badgeClass(o.orderStatus)}">${esc(o.orderStatus)}</span></td>
            <td>${esc(o.workCenterId ? machineLabel(o.workCenterId) : '-')}</td>
            <td>
                ${canManageOrders() && o.orderStatus !== 'COMPLETED' && o.orderStatus !== 'CANCELLED' ? `<button class="btn btn-sm btn-warning" onclick="editOrder(${o.orderId})">Edit</button>` : ''}
                ${canReleaseOrders() && o.orderStatus === 'PLANNED' ? `<button class="btn btn-sm btn-success" onclick="orderAction(${o.orderId}, 'release')">Release</button>` : ''}
                ${canStartOrders() && o.orderStatus === 'RELEASED' ? `<button class="btn btn-sm btn-primary" onclick="startOrder(${o.orderId})">Start</button>` : ''}
                ${canUpdateProducedQuantity() && isProducedQuantityEditable(o) ? `<button class="btn btn-sm btn-outline-primary" onclick="updateProducedQuantity(${o.orderId})">Update Qty</button>` : ''}
                ${canCompleteOrders() && o.orderStatus === 'IN_PROGRESS' ? `<button class="btn btn-sm btn-success" onclick="orderAction(${o.orderId}, 'complete')">Complete</button>` : ''}
                ${canCancelOrders() && ['PLANNED', 'RELEASED', 'IN_PROGRESS'].includes(o.orderStatus) ? `<button class="btn btn-sm btn-danger" onclick="orderAction(${o.orderId}, 'cancel')">Cancel</button>` : ''}
                <button class="btn btn-sm btn-info text-white" onclick="orderProgress(${o.orderId})">Progress</button>
            </td>
        </tr>`).join('');
};

window.editOrder = id => {
    const o = state.orders.find(item => item.orderId === id);
    if (!o) return;
    document.getElementById('orderId').value = o.orderId;
    document.getElementById('orderProductId').value = o.productId;
    document.getElementById('plannedQuantity').value = o.plannedQuantity;
    document.getElementById('startDate').value = o.startDate;
    document.getElementById('endDate').value = o.endDate;
};

window.orderAction = (id, action) => run(async () => {
    await api(`/api/orders/${id}/${action}`, { method: 'POST' });
    await loadOrders();
    await loadDashboard();
}, `Order ${action} completed`);

window.scheduleWorkCenter = id => run(async () => {
    await loadMachineLists();
    const current = state.orders.find(order => order.orderId === id)?.workCenterId || '';
    const workCenterId = selectAvailableMachine('Schedule work center', current);
    if (!workCenterId) return;

    await api(`/api/orders/${id}/schedule`, { method: 'POST', body: JSON.stringify({ workCenterId }) });
    await loadOrders();
    showAlert('Work center scheduled');
});

window.startOrder = id => run(async () => {
    await loadMachineLists();
    const current = state.orders.find(order => order.orderId === id)?.workCenterId || '';
    const workCenterId = selectAvailableMachine('Assign machine before starting production', current);
    if (!workCenterId) return;

    await api(`/api/orders/${id}/start`, { method: 'POST', body: JSON.stringify({ workCenterId }) });
    await loadOrders();
    await loadDashboard();
}, 'Order started with scheduled work center');

window.updateProducedQuantity = id => run(async () => {
    const order = state.orders.find(item => item.orderId === id);
    if (!isProducedQuantityEditable(order)) {
        throw new Error('Assign a machine/work center before updating produced quantity');
    }

    const value = prompt(`Produced quantity for order ${id}\nPlanned quantity: ${order.plannedQuantity}`, order.producedQuantity || 0);
    if (value === null) return;

    const producedQuantity = Number(value);
    if (!Number.isInteger(producedQuantity) || producedQuantity < 0 || producedQuantity > Number(order.plannedQuantity)) {
        throw new Error(`Produced quantity must be a whole number between 0 and ${order.plannedQuantity}`);
    }

    await api(`/api/orders/${id}/produced-quantity`, {
        method: 'POST',
        body: JSON.stringify({ producedQuantity })
    });
    await loadOrders();
    await loadDashboard();
}, 'Produced quantity updated');

window.orderProgress = id => run(async () => {
    const result = await api(`/api/orders/${id}/progress`);
    showAlert(result.progress, 'info');
});

const resetOrderForm = () => {
    document.getElementById('order-form').reset();
    document.getElementById('orderId').value = '';
};

const registerOrderHandlers = () => {
    document.getElementById('order-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        const id = document.getElementById('orderId').value;
        const existing = state.orders.find(o => String(o.orderId) === id);
        const payload = {
            productId: Number(document.getElementById('orderProductId').value),
            plannedQuantity: Number(document.getElementById('plannedQuantity').value),
            producedQuantity: existing?.producedQuantity || 0,
            startDate: document.getElementById('startDate').value,
            endDate: document.getElementById('endDate').value,
            workCenterId: existing?.workCenterId || null,
            orderStatus: existing?.orderStatus || 'PLANNED'
        };
        await api(id ? `/api/orders/${id}` : '/api/orders', { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
        resetOrderForm();
        await loadOrders();
        await loadDashboard();
    }, 'Order saved'));
};
