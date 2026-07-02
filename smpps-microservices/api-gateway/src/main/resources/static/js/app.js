const state = {
    products: [],
    orders: [],
    machines: [],
    availableMachines: [],
    machineLogs: [],
    inspections: [],
    workOrders: [],
    currentUser: null
};

const sectionRoles = {
    dashboard: [],
    products: ['ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR', 'QUALITY_INSPECTOR'],
    orders: ['ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR', 'QUALITY_INSPECTOR'],
    machines: ['ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR', 'MAINTENANCE_ENGINEER'],
    quality: ['ADMIN', 'QUALITY_INSPECTOR'],
    maintenance: ['ADMIN', 'MAINTENANCE_ENGINEER']
};

const sectionPaths = {
    dashboard: '/dashboard',
    products: '/products',
    orders: '/orders',
    machines: '/machines',
    quality: '/quality',
    maintenance: '/maintenance'
};

const pathSections = Object.fromEntries(Object.entries(sectionPaths).map(([section, path]) => [path, section]));

const loadPagePartials = async () => {
    const placeholders = Array.from(document.querySelectorAll('[data-page-partial]'));
    for (const placeholder of placeholders) {
        const partialPath = placeholder.dataset.pagePartial;
        const response = await fetch(partialPath);
        if (!response.ok) {
            throw new Error(`Unable to load page partial: ${partialPath}`);
        }
        placeholder.outerHTML = await response.text();
    }
};

const authToken = () => localStorage.getItem('smppsToken');

const logout = () => {
    localStorage.removeItem('smppsToken');
    window.location.href = '/login?logout';
};

const sectionFromLocation = () => {
    if (location.hash) {
        return location.hash.replace('#', '') || 'dashboard';
    }
    return pathSections[location.pathname] || 'dashboard';
};

const api = async (url, options = {}) => {
    const token = authToken();
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        if (response.status === 401) {
            localStorage.removeItem('smppsToken');
            window.location.href = '/login?expired';
            throw new Error('Please sign in to continue');
        }

        let message = `Request failed (${response.status})`;
        try {
            const body = await response.json();
            message = body.message || message;
        } catch (_) {
            // keep default message
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
};

const esc = value => String(value ?? '').replace(/[&<>'"]/g, ch => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;'
}[ch]));

const showAlert = (message, type = 'success') => {
    const container = document.getElementById('alert-container');
    container.innerHTML = `<div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${esc(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`;
};

const run = async (action, successMessage) => {
    try {
        await action();
        if (successMessage) showAlert(successMessage);
    } catch (error) {
        showAlert(error.message, 'danger');
    }
};

const badgeClass = value => {
    switch (value) {
        case 'ACTIVE':
        case 'PASS':
        case 'COMPLETED':
        case 'RUNNING':
        case 'AVAILABLE':
            return 'bg-success';
        case 'FAIL':
        case 'CANCELLED':
        case 'BREAKDOWN':
            return 'bg-danger';
        case 'REWORK':
        case 'IN_PROGRESS':
            return 'bg-warning text-dark';
        case 'RELEASED':
        case 'MAINTENANCE':
        case 'UNDER_MAINTENANCE':
            return 'bg-info text-dark';
        case 'UNAVAILABLE':
            return 'bg-dark';
        default:
            return 'bg-secondary';
    }
};

const hasRole = role => Boolean(state.currentUser?.roles?.includes(role));
const hasAnyRole = (...roles) => roles.length === 0 || roles.some(hasRole);
const canManageProducts = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER');
const canManageOrders = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER');
const canReleaseOrders = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER');
const canStartOrders = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR');
const canCompleteOrders = () => hasAnyRole('ADMIN', 'SHOP_FLOOR_SUPERVISOR');
const canUpdateProducedQuantity = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR');
const canCancelOrders = () => hasAnyRole('ADMIN', 'PRODUCTION_PLANNER', 'SHOP_FLOOR_SUPERVISOR');

const canAccessSection = sectionId => hasAnyRole(...(sectionRoles[sectionId] || []));

const canAccessElement = element => {
    const roles = element.dataset.roles?.split(',').map(role => role.trim()).filter(Boolean) || [];
    return hasAnyRole(...roles);
};

const applyRoleVisibility = () => {
    document.querySelectorAll('[data-roles]').forEach(element => {
        element.classList.toggle('d-none', !canAccessElement(element));
    });

    if (state.currentUser) {
        document.getElementById('current-user').textContent = state.currentUser.username;
        document.getElementById('current-roles').textContent = (state.currentUser.displayRoles || state.currentUser.roles).join(', ');
    }
};

const loadCurrentUser = async () => {
    state.currentUser = await api('/api/auth/me');
    applyRoleVisibility();
};

const showSection = (sectionId, updateUrl = true) => {
    if (!Object.prototype.hasOwnProperty.call(sectionRoles, sectionId)) {
        sectionId = 'dashboard';
    }

    if (!canAccessSection(sectionId)) {
        history.replaceState(null, '', sectionPaths.dashboard);
        showAlert('Access denied: your role cannot open that module', 'warning');
        sectionId = 'dashboard';
    }

    if (updateUrl && location.pathname !== sectionPaths[sectionId]) {
        history.pushState(null, '', sectionPaths[sectionId]);
    }

    document.querySelectorAll('.page-section').forEach(section => section.classList.add('d-none'));
    document.getElementById(sectionId).classList.remove('d-none');

    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        link.classList.toggle('active-link', link.dataset.section === sectionId);
    });

    loadSection(sectionId);
};

const loadSection = async sectionId => {
    switch (sectionId) {
        case 'dashboard': return loadDashboard();
        case 'products': return loadProducts();
        case 'orders': return loadOrders();
        case 'machines': return loadMachines();
        case 'quality': return loadQuality();
        case 'maintenance': return loadMaintenance();
    }
};

const renderSummaryCards = (containerId, cards) => {
    document.getElementById(containerId).innerHTML = cards.map(([title, value, border, bg]) => `
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
document.addEventListener('DOMContentLoaded', async () => {
    if (!authToken()) {
        window.location.href = '/login';
        return;
    }
    await run(loadPagePartials);
    await run(loadCurrentUser);
    if (!state.currentUser) {
        return;
    }
    document.getElementById('logout-button').addEventListener('click', logout);
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        if (!canAccessElement(link)) {
            link.classList.add('d-none');
            return;
        }
        link.addEventListener('click', event => {
            event.preventDefault();
            showSection(link.dataset.section);
        });
    });
    window.addEventListener('popstate', () => showSection(sectionFromLocation(), false));
    document.querySelectorAll('[data-refresh]').forEach(button => {
        button.addEventListener('click', () => loadSection(button.dataset.refresh));
    });
    registerProductHandlers();
    registerOrderHandlers();
    registerMachineHandlers();
    registerQualityHandlers();
    registerMaintenanceHandlers();
    const initialSection = sectionFromLocation();
    if (location.pathname === '/' || location.pathname === '/index.html' || location.hash) {
        history.replaceState(null, '', sectionPaths[initialSection] || sectionPaths.dashboard);
    }
    showSection(initialSection, false);
});
