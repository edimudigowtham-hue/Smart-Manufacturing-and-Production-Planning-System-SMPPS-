const loadQuality = () => run(async () => {
    state.orders = await api('/api/orders');
    state.inspections = await api('/api/quality/inspections');
    fillOrderSelects();
    renderQuality();
});

const fillOrderSelects = () => {
    document.getElementById('qualityOrderId').innerHTML = '<option value="">Select Order</option>' + state.orders.map(o =>
        `<option value="${esc(o.orderId)}">Order #${esc(o.orderId)} (${esc(o.orderStatus)}, Produced: ${esc(o.producedQuantity || 0)})</option>`
    ).join('');
    updateQualitySampleLimit();
};

const selectedQualityOrder = () => {
    const orderId = document.getElementById('qualityOrderId').value;
    return state.orders.find(o => String(o.orderId) === orderId);
};

const updateQualitySampleLimit = () => {
    const order = selectedQualityOrder();
    const sampleSize = document.getElementById('sampleSize');
    const producedQuantity = order?.producedQuantity || 0;
    sampleSize.max = producedQuantity || '';
    sampleSize.placeholder = producedQuantity ? `Sample Size (max ${producedQuantity})` : 'Sample Size';
};

const renderQuality = () => {
    const totalInspections = state.inspections.length;
    const passCount = state.inspections.filter(q => q.inspectionResult === 'PASS').length;
    const reworkCount = state.inspections.filter(q => q.inspectionResult === 'REWORK').length;
    const failCount = state.inspections.filter(q => q.inspectionResult === 'FAIL').length;
    const totalDefects = state.inspections.reduce((sum, q) => sum + Number(q.defectCount || 0), 0);
    const criticalCount = state.inspections.filter(q => q.severity === 'CRITICAL').length;
    const defectLogs = state.inspections.filter(q => q.defectType || q.severity || q.defectDescription).length;
    const passRate = totalInspections === 0 ? 0 : Math.round((passCount / totalInspections) * 10000) / 100;

    renderSummaryCards('quality-summary-cards', [
        ['Inspections', totalInspections, 'border-primary', 'bg-primary'],
        ['Pass Rate', `${passRate}%`, 'border-success', 'bg-success'],
        ['PASS', passCount, 'border-success', 'bg-success'],
        ['REWORK', reworkCount, 'border-warning', 'bg-warning'],
        ['FAIL', failCount, 'border-danger', 'bg-danger'],
        ['Total Defects', totalDefects, 'border-secondary', 'bg-secondary'],
        ['Critical Defects', criticalCount, 'border-danger', 'bg-danger'],
        ['Defect Logs', defectLogs, 'border-info', 'bg-info']
    ]);

    document.getElementById('quality-table').innerHTML = state.inspections.map(q => `
        <tr>
            <td>${esc(q.inspectionId)}</td>
            <td>${esc(q.orderId)}</td>
            <td>${esc(q.inspectionDate)}</td>
            <td>${esc(q.sampleSize)}</td>
            <td>${esc(q.defectCount)}</td>
            <td>${esc(q.defectType)}</td>
            <td>${esc(q.severity)}</td>
            <td>${esc(q.defectDescription)}</td>
            <td><span class="badge ${badgeClass(q.inspectionResult)}">${esc(q.inspectionResult)}</span></td>
            <td>
                <button class="btn btn-sm btn-secondary" onclick="logDefect(${q.inspectionId})">Log Defect</button>
                <button class="btn btn-sm btn-success" onclick="qualityAction(${q.inspectionId}, 'approve')">Approve</button>
                <button class="btn btn-sm btn-danger" onclick="qualityAction(${q.inspectionId}, 'reject')">Reject</button>
            </td>
        </tr>`).join('');
};

window.logDefect = id => run(async () => {
    const current = state.inspections.find(q => q.inspectionId === id) || {};
    const defectType = prompt('Defect type', current.defectType || '');
    if (defectType === null) return;
    const severity = prompt('Severity: MINOR, MAJOR, or CRITICAL', current.severity || 'MINOR');
    if (severity === null) return;
    const defectDescription = prompt('Defect description / non-conformance notes', current.defectDescription || '');
    if (defectDescription === null) return;

    await api(`/api/quality/inspections/${id}/defect`, { method: 'POST', body: JSON.stringify({ defectType, severity, defectDescription }) });
    await loadQuality();
}, 'Defect details logged');

window.qualityAction = (id, action) => run(async () => {
    await api(`/api/quality/inspections/${id}/${action}`, { method: 'POST' });
    await loadQuality();
}, `Inspection ${action} completed`);

const registerQualityHandlers = () => {
    document.getElementById('qualityOrderId').addEventListener('change', updateQualitySampleLimit);

    document.getElementById('quality-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        const order = selectedQualityOrder();
        const sampleSize = Number(document.getElementById('sampleSize').value);
        const defectCount = Number(document.getElementById('defectCount').value);
        const producedQuantity = order?.producedQuantity || 0;
        if (!order) {
            throw new Error('Select a production order');
        }
        if (producedQuantity <= 0) {
            throw new Error('Inspection cannot be recorded because no quantity has been produced for this order');
        }
        if (sampleSize > producedQuantity) {
            throw new Error(`Sample size cannot be greater than produced quantity (${producedQuantity})`);
        }
        if (defectCount > sampleSize) {
            throw new Error('Defect count cannot be greater than sample size');
        }
        await api('/api/quality/inspections', { method: 'POST', body: JSON.stringify({
            orderId: Number(document.getElementById('qualityOrderId').value),
            inspectionDate: document.getElementById('inspectionDate').value,
            sampleSize,
            defectCount
        }) });
        event.target.reset();
        await loadQuality();
        await loadDashboard();
    }, 'Inspection recorded'));
};
