const machineLabel = machineId => {
    const machine = state.machines.find(item => item.machineId === Number(machineId));
    return machine ? `${machine.machineId} - ${machine.machineName}` : machineId;
};

const machineOption = machine =>
    `<option value="${esc(machine.machineId)}">${esc(machine.machineId)} - ${esc(machine.machineName)} (${esc(machine.availability)})</option>`;

const fillMachineSelects = () => {
    const allOptions = '<option value="">Select Machine</option>' + state.machines.map(machineOption).join('');
    const schedulableMachines = state.availableMachines.filter(machine => machine.availability === 'AVAILABLE');
    const availableOptions = '<option value="">Select Available Machine</option>' + schedulableMachines.map(machineOption).join('');
    document.getElementById('runtimeMachineId').innerHTML = allOptions;
    document.getElementById('downtimeMachineId').innerHTML = allOptions;
    document.getElementById('maintenanceMachineId').innerHTML = availableOptions;
};

const renderFactoryMachines = () => {
    document.getElementById('factory-machines-table').innerHTML = state.machines.map(machine => `
        <tr>
            <td>${esc(machine.machineId)}</td>
            <td>${esc(machine.machineName)}</td>
            <td><span class="badge ${badgeClass(machine.availability)}">${esc(machine.availability)}</span></td>
            <td data-roles="ADMIN,MAINTENANCE_ENGINEER">
                ${machine.availability === 'AVAILABLE' ? `<button class="btn btn-sm btn-outline-danger" onclick="setMachineAvailability(${machine.machineId}, 'UNAVAILABLE')">Set Unavailable</button>` : ''}
                ${machine.availability === 'UNAVAILABLE' ? `<button class="btn btn-sm btn-outline-success" onclick="setMachineAvailability(${machine.machineId}, 'AVAILABLE')">Set Available</button>` : ''}
                ${machine.availability === 'UNDER_MAINTENANCE' ? '<span class="text-muted small">Controlled by maintenance work order</span>' : ''}
            </td>
        </tr>`).join('');
    applyRoleVisibility();
};

const renderOeeSummary = summary => {
    const cardClass = summary.overallOee >= 80 ? 'border-success' : summary.overallOee >= 60 ? 'border-warning' : 'border-danger';
    const cards = [
        ['Overall OEE', `${summary.overallOee}%`, cardClass, 'bg-primary'],
        ['Total Runtime', `${summary.totalRuntimeHours} hrs`, 'border-success', 'bg-success'],
        ['Total Downtime', `${summary.totalDowntimeHours} hrs`, 'border-danger', 'bg-danger'],
        ['Avg Runtime / Machine', `${summary.averageRuntimeHours} hrs`, 'border-info', 'bg-info'],
        ['Avg Downtime / Machine', `${summary.averageDowntimeHours} hrs`, 'border-warning', 'bg-warning'],
        ['Downtime Events', summary.downtimeEvents, 'border-secondary', 'bg-secondary'],
        ['Machines Tracked', summary.machineCount, 'border-dark', 'bg-dark'],
        ['Machine Logs', summary.logCount, 'border-secondary', 'bg-secondary']
    ];

    document.getElementById('machine-oee-cards').innerHTML = cards.map(([title, value, border, bg]) => `
        <div class="col-md-3">
            <div class="card h-100 shadow-sm ${border}">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small">${esc(title)}</div>
                        <h4 class="mb-0">${esc(value)}</h4>
                    </div>
                    <span class="rounded-circle ${bg} d-inline-block" style="width:14px;height:14px;"></span>
                </div>
            </div>
        </div>`).join('');
};

const renderDailyMachineLogs = logs => {
    const registeredMachineIds = new Set(state.machines.map(machine => Number(machine.machineId)));
    const grouped = new Map();

    logs
        .filter(log => registeredMachineIds.has(Number(log.machineId)))
        .forEach(log => {
            const key = `${log.machineId}|${log.logDate}`;
            const existing = grouped.get(key) || {
                machineId: Number(log.machineId),
                logDate: log.logDate,
                runtimeHours: 0,
                downtimeHours: 0,
                downtimeReason: '',
                runtimeStatus: null,
                downtimeStatus: null
            };

            const runtimeHours = Number(log.runtimeHours || 0);
            const downtimeHours = Number(log.downtimeHours || 0);
            existing.runtimeHours += runtimeHours;
            existing.downtimeHours += downtimeHours;
            if (log.downtimeReason) {
                existing.downtimeReason = existing.downtimeReason
                    ? `${existing.downtimeReason}; ${log.downtimeReason}`
                    : log.downtimeReason;
            }
            if (downtimeHours > 0 && log.machineStatus) {
                existing.downtimeStatus = log.machineStatus;
            } else if (runtimeHours > 0 && log.machineStatus) {
                existing.runtimeStatus = log.machineStatus;
            }
            grouped.set(key, existing);
        });

    const rows = Array.from(grouped.values())
        .sort((a, b) => String(b.logDate).localeCompare(String(a.logDate)) || a.machineId - b.machineId);

    document.getElementById('machine-oee-table').innerHTML = rows.map(row => {
        const totalHours = row.runtimeHours + row.downtimeHours;
        const dailyOee = totalHours === 0 ? 0 : Math.round((row.runtimeHours / totalHours) * 10000) / 100;
        const status = row.downtimeHours > 0 ? row.downtimeStatus || 'BREAKDOWN' : row.runtimeStatus || (row.runtimeHours > 0 ? 'RUNNING' : null);
        return `
        <tr>
            <td>${esc(machineLabel(row.machineId))}</td>
            <td>${esc(row.logDate)}</td>
            <td>${esc(Math.round(row.runtimeHours * 100) / 100)} hrs</td>
            <td>${esc(Math.round(row.downtimeHours * 100) / 100)} hrs</td>
            <td><span class="badge ${dailyOee >= 80 ? 'bg-success' : dailyOee >= 60 ? 'bg-warning text-dark' : 'bg-danger'}">${esc(dailyOee)}%</span></td>
            <td>${esc(row.downtimeReason || '-')}</td>
            <td>${status ? `<span class="badge ${badgeClass(status)}">${esc(status)}</span>` : '<span class="text-muted">No status</span>'}</td>
        </tr>`;
    }).join('') || '<tr><td colspan="7" class="text-center text-muted">No daily machine logs available for registered machines.</td></tr>';
};

const loadMachineLists = async () => {
    state.machines = await api('/api/machines');
    state.availableMachines = await api('/api/machines/available');
    fillMachineSelects();
};

const selectAvailableMachine = (title, current = '') => {
    const schedulableMachines = state.availableMachines.filter(machine => machine.availability === 'AVAILABLE');
    if (schedulableMachines.length === 0) {
        throw new Error('No available machines for scheduling');
    }
    const available = schedulableMachines.map(machine => `${machine.machineId} - ${machine.machineName}`).join('\n');
    const workCenterId = prompt(`${title}\n\nAvailable machines:\n${available}\n\nEnter machine ID`, current);
    if (!workCenterId) {
        return null;
    }
    if (!schedulableMachines.some(machine => String(machine.machineId) === String(workCenterId).trim())) {
        throw new Error('Select one of the available machine IDs shown');
    }
    return String(workCenterId).trim();
};

const loadMachines = () => run(async () => {
    await loadMachineLists();
    state.machineLogs = await api('/api/machines/logs');
    const oeeSummary = await api('/api/machines/oee/summary');
    renderFactoryMachines();
    renderOeeSummary(oeeSummary);
    renderDailyMachineLogs(state.machineLogs);
});

window.setMachineAvailability = (machineId, availability) => run(async () => {
    const machine = state.machines.find(item => Number(item.machineId) === Number(machineId));
    const label = machine ? `${machine.machineId} - ${machine.machineName}` : machineId;
    if (!confirm(`Change ${label} to ${availability}?`)) {
        return;
    }
    await api(`/api/machines/${machineId}/availability`, {
        method: 'POST',
        body: JSON.stringify({ availability })
    });
    await loadMachines();
    await loadMaintenance();
    await loadDashboard();
}, 'Machine availability updated');

const registerMachineHandlers = () => {
    document.getElementById('machine-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        await api('/api/machines', { method: 'POST', body: JSON.stringify({
            machineId: Number(document.getElementById('factoryMachineId').value),
            machineName: document.getElementById('factoryMachineName').value,
            availability: document.getElementById('factoryMachineAvailability').value
        }) });
        event.target.reset();
        await loadMachines();
        await loadDashboard();
    }, 'Machine added'));

    document.getElementById('runtime-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        await api('/api/machines/runtime', { method: 'POST', body: JSON.stringify({
            machineId: Number(document.getElementById('runtimeMachineId').value),
            logDate: document.getElementById('runtimeLogDate').value,
            runtimeHours: Number(document.getElementById('runtimeHours').value)
        }) });
        event.target.reset();
        await loadMachines();
        await loadDashboard();
    }, 'Runtime recorded'));

    document.getElementById('downtime-form').addEventListener('submit', event => run(async () => {
        event.preventDefault();
        await api('/api/machines/downtime', { method: 'POST', body: JSON.stringify({
            machineId: Number(document.getElementById('downtimeMachineId').value),
            logDate: document.getElementById('downtimeLogDate').value,
            downtimeHours: Number(document.getElementById('downtimeHours').value),
            downtimeReason: document.getElementById('downtimeReason').value
        }) });
        event.target.reset();
        await loadMachines();
        await loadDashboard();
    }, 'Downtime logged'));
};
