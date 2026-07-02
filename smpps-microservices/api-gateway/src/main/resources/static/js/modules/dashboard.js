const loadDashboard = () => run(async () => {
    const data = await api('/api/dashboard');
    const cards = [
        ['Products', data.productCount, '📦'],
        ['Orders', data.orderCount, '📝'],
        ['Machine Logs', data.machineCount, '⚙️'],
        ['Inspections', data.qualityCount, '✅'],
        ['Maintenance', data.maintenanceCount, '🛠️']
    ];
    document.getElementById('dashboard-cards').innerHTML = cards.map(([title, count, icon]) => `
        <div class="col-md-3">
            <div class="card text-center shadow-sm h-100">
                <div class="card-body">
                    <h5>${icon} ${esc(title)}</h5>
                    <h2>${esc(count)}</h2>
                </div>
            </div>
        </div>`).join('');
});
