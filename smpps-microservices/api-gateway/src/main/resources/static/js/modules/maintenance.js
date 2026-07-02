const todayDateValue = () => {
    const today = new Date();
    today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
    return today.toISOString().slice(0, 10);
};

const loadMaintenance = () => run(async () => {
    await loadMachineLists();
    state.workOrders = await api('/api/maintenance/work-orders');
    const totalWorkOrders = state.workOrders.length;
    const openCount = state.workOrders.filter(w => w.workOrderStatus === 'OPEN').length;
    const inProgressCount = state.workOrders.filter(w => w.workOrderStatus === 'IN_PROGRESS').length;
    const completedCount = state.workOrders.filter(w => w.workOrderStatus === 'COMPLETED').length;
    const cancelledCount = state.workOrders.filter(w => w.workOrderStatus === 'CANCELLED').length;
    const preventiveCount = state.workOrders.filter(w => w.maintenanceType === 'PREVENTIVE').length;
    const breakdownCount = state.workOrders.filter(w => w.maintenanceType === 'BREAKDOWN').length;
    const spareIssuedCount = state.workOrders.filter(w => w.spareParts).length;

    renderSummaryCards('maintenance-summary-cards', [
        ['Work Orders', totalWorkOrders, 'border-primary', 'bg-primary'],
        ['OPEN', openCount, 'border-secondary', 'bg-secondary'],
        ['IN PROGRESS', inProgressCount, 'border-warning', 'bg-warning'],
        ['COMPLETED', completedCount, 'border-success', 'bg-success'],
        ['CANCELLED', cancelledCount, 'border-dark', 'bg-dark'],
        ['Preventive', preventiveCount, 'border-info', 'bg-info'],
        ['Breakdown', breakdownCount, 'border-danger', 'bg-danger'],
        ['Spare Issued', spareIssuedCount, 'border-secondary', 'bg-secondary']
    ]);

    const canAssignTechnician = w => w.workOrderStatus === 'OPEN';
    const canIssueSpare = w => w.workOrderStatus === 'IN_PROGRESS' && Boolean(w.technician) && !w.spareParts;
    const canCloseWorkOrder = w => w.workOrderStatus === 'IN_PROGRESS' && Boolean(w.technician) && Boolean(w.spareParts);
    const canCancelWorkOrder = w => !['COMPLETED', 'CANCELLED'].includes(w.workOrderStatus);

    document.getElementById('maintenance-table').innerHTML = state.workOrders.map(w => `
        <tr>
            <td>${esc(w.workOrderId)}</td>
            <td>${esc(w.machineId)}</td>
            <td>${esc(w.maintenanceType)}</td>
            <td>${esc(w.scheduledDate)}</td>
            <td>${esc(w.completionDate)}</td>
            <td>${esc(w.technician)}</td>
            <td>${esc(w.spareParts)}</td>
            <td><span class="badge ${badgeClass(w.workOrderStatus)}">${esc(w.workOrderStatus)}</span></td>
            <td>
                ${canAssignTechnician(w) ? `<button class="btn btn-sm btn-warning" onclick="assignTechnician(${w.workOrderId})">Assign</button>` : ''}
                ${canIssueSpare(w) ? `<button class="btn btn-sm btn-info text-white" onclick="issueSpare(${w.workOrderId})">Spare</button>` : ''}
                ${canCloseWorkOrder(w) ? `<button class="btn btn-sm btn-success" onclick="closeWorkOrder(${w.workOrderId})">Close</button>` : ''}
                ${canCancelWorkOrder(w) ? `<button class="btn btn-sm btn-outline-danger" onclick="cancelWorkOrder(${w.workOrderId})">Cancel</button>` : ''}
            </td>
        </tr>`).join('');
});

window.assignTechnician = id => {
    const technician = prompt('Technician name');
    if (!technician) return;
    run(async () => {
        await api(`/api/maintenance/work-orders/${id}/assign`, { method: 'POST', body: JSON.stringify({ technician }) });
        await loadMaintenance();
    }, 'Technician assigned');
};

window.issueSpare = id => {
    const spareParts = prompt('Spare parts issued');
    if (!spareParts) return;
    run(async () => {
        await api(`/api/maintenance/work-orders/${id}/spare`, { method: 'POST', body: JSON.stringify({ spareParts }) });
        await loadMaintenance();
    }, 'Spare parts issued');
};

window.closeWorkOrder = id => run(async () => {
    await api(`/api/maintenance/work-orders/${id}/close`, { method: 'POST' });
    await loadMaintenance();
    await loadDashboard();
}, 'Work order closed');

window.cancelWorkOrder = id => run(async () => {
    if (!confirm('Cancel this maintenance work order? The machine will become AVAILABLE again.')) {
        return;
    }
    await api(`/api/maintenance/work-orders/${id}/cancel`, { method: 'POST' });
    await loadMaintenance();
    await loadMachines();
    await loadDashboard();
}, 'Work order cancelled');

const registerMaintenanceHandlers = () => {
    const startDateInput = document.getElementById('scheduledDate');
    startDateInput.min = todayDateValue();

    document.getElementById('maintenance-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        const startDate = document.getElementById('scheduledDate').value;
        if (startDate < todayDateValue()) {
            throw new Error('Start date cannot be in the past');
        }
        await api('/api/maintenance/work-orders', { method: 'POST', body: JSON.stringify({
            machineId: Number(document.getElementById('maintenanceMachineId').value),
            maintenanceType: document.getElementById('maintenanceType').value,
            scheduledDate: startDate
        }) });
        event.target.reset();
        await loadMaintenance();
        await loadDashboard();
    }, 'Work order created'));
};
