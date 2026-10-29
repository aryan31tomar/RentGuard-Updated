/**
 * RentGuard - Rental Property Verification & Scam Prevention Platform
 * Interactive Client Application
 */

const API_BASE = '/api';

const state = {
    token: localStorage.getItem('rentguard_token') || null,
    user: JSON.parse(localStorage.getItem('rentguard_user') || 'null'),
    listings: [],
    currentListing: null,
    activeTab: 'browse'
};

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    initAuthUI();
    loadListings();
    setupEventListeners();
});

// Toast notification utility
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// API Fetch Helper with JWT Header
async function apiFetch(endpoint, options = {}) {
    const headers = options.headers || {};
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'API request failed');
        }
        return data;
    } catch (err) {
        console.error(`API Error on ${endpoint}:`, err);
        throw err;
    }
}

// Setup Event Listeners
function setupEventListeners() {
    // Search form
    const searchForm = document.getElementById('search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            loadListings();
        });
    }

    // Role switcher pills
    document.querySelectorAll('.demo-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            const email = pill.getAttribute('data-email');
            quickLogin(email);
        });
    });
}

// Quick Demo Login
async function quickLogin(email) {
    try {
        const res = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password: 'Password123!' })
        });
        setSession(res.data);
        showToast(`Logged in as ${res.data.name} (${res.data.role.replace('ROLE_', '')})`, 'success');
        updateTabViews();
        loadListings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function setSession(authData) {
    state.token = authData.token;
    state.user = authData;
    localStorage.setItem('rentguard_token', authData.token);
    localStorage.setItem('rentguard_user', JSON.stringify(authData));
    initAuthUI();
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('rentguard_token');
    localStorage.removeItem('rentguard_user');
    initAuthUI();
    showToast('Logged out successfully', 'info');
    switchTab('browse');
    loadListings();
}

function initAuthUI() {
    const authActions = document.getElementById('auth-actions');
    const ownerTab = document.getElementById('tab-owner-btn');
    const adminTab = document.getElementById('tab-admin-btn');
    const tenantTab = document.getElementById('tab-tenant-btn');

    if (state.user) {
        authActions.innerHTML = `
            <div style="display:flex; align-items:center; gap:0.75rem;">
                <span style="font-size:0.85rem; font-weight:600; color:var(--slate-700)">
                    ${state.user.name} <span class="brand-badge">${state.user.role.replace('ROLE_', '')}</span>
                </span>
                <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
            </div>
        `;

        if (ownerTab) ownerTab.style.display = (state.user.role === 'ROLE_OWNER' || state.user.role === 'ROLE_ADMIN') ? 'block' : 'none';
        if (adminTab) adminTab.style.display = (state.user.role === 'ROLE_ADMIN') ? 'block' : 'none';
        if (tenantTab) tenantTab.style.display = (state.user.role === 'ROLE_TENANT' || state.user.role === 'ROLE_ADMIN') ? 'block' : 'none';
    } else {
        authActions.innerHTML = `
            <button class="btn btn-primary btn-sm" onclick="openLoginModal()">Login / Register</button>
        `;
        if (ownerTab) ownerTab.style.display = 'none';
        if (adminTab) adminTab.style.display = 'none';
        if (tenantTab) tenantTab.style.display = 'none';
    }
}

// Navigation Tabs
function switchTab(tabId) {
    state.activeTab = tabId;
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabId);
    });
    document.querySelectorAll('.tab-pane').forEach(pane => {
        pane.style.display = (pane.id === `pane-${tabId}`) ? 'block' : 'none';
    });

    if (tabId === 'browse') loadListings();
    if (tabId === 'owner') loadOwnerPortal();
    if (tabId === 'admin') loadAdminDashboard();
    if (tabId === 'tenant') loadTenantPortal();
}

function updateTabViews() {
    if (state.user) {
        if (state.user.role === 'ROLE_ADMIN') {
            switchTab('admin');
        } else if (state.user.role === 'ROLE_OWNER') {
            switchTab('owner');
        } else {
            switchTab('browse');
        }
    } else {
        switchTab('browse');
    }
}

// Load and Render Listings
async function loadListings() {
    const grid = document.getElementById('listings-grid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column:1/-1; text-align:center; padding:3rem; color:var(--slate-400)">Loading verified listings...</div>';

    const city = document.getElementById('filter-city')?.value || '';
    const locality = document.getElementById('filter-locality')?.value || '';
    const bedrooms = document.getElementById('filter-bhk')?.value || '';
    const maxRisk = document.getElementById('filter-max-risk')?.value || '';

    let queryParams = [];
    if (city) queryParams.push(`city=${encodeURIComponent(city)}`);
    if (locality) queryParams.push(`locality=${encodeURIComponent(locality)}`);
    if (bedrooms) queryParams.push(`bedrooms=${bedrooms}`);
    if (maxRisk) queryParams.push(`maxRisk=${maxRisk}`);

    const queryString = queryParams.length ? `?${queryParams.join('&')}` : '';

    try {
        const res = await apiFetch(`/listings${queryString}`);
        state.listings = res.data;
        renderListings(state.listings);
    } catch (err) {
        grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:2rem; color:var(--risk-high)">Failed to load listings: ${err.message}</div>`;
    }
}

function renderListings(listings) {
    const grid = document.getElementById('listings-grid');
    const countSpan = document.getElementById('listings-count');
    if (countSpan) countSpan.textContent = `${listings.length} properties found`;

    if (!listings.length) {
        grid.innerHTML = `
            <div style="grid-column:1/-1; text-align:center; padding:4rem 1rem; background:#fff; border-radius:var(--radius-lg); border:1px solid var(--slate-200)">
                <div style="font-size:2rem; margin-bottom:0.5rem">🏠</div>
                <h3 style="font-size:1.2rem; color:var(--slate-800)">No properties match your filter</h3>
                <p style="color:var(--slate-500); margin-top:0.25rem">Try resetting filters or expanding risk score threshold.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = listings.map(l => {
        const isLow = l.riskScore <= 29;
        const isMed = l.riskScore >= 30 && l.riskScore <= 59;
        const badgeClass = isLow ? 'badge-low' : isMed ? 'badge-med' : 'badge-high';
        const badgeIcon = isLow ? '🛡️ LOW RISK' : isMed ? '⚠️ MEDIUM RISK' : '🚨 HIGH RISK';

        const coverImg = (l.images && l.images.length > 0)
            ? l.images[0].filePath
            : 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop';

        const prop = l.property || {};
        const isSuspended = l.status === 'SUSPENDED';

        return `
            <div class="listing-card ${isSuspended ? 'opacity-75' : ''}">
                <div class="listing-image-wrapper">
                    <img src="${coverImg}" class="listing-image" alt="Property Photo" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800'">
                    <div class="listing-risk-badge ${badgeClass}">
                        ${badgeIcon} (${l.riskScore}/100)
                    </div>
                </div>
                <div class="listing-body">
                    <div class="listing-price-row">
                        <div class="listing-price">₹${Number(l.rent).toLocaleString('en-IN')} <span>/mo</span></div>
                        <div style="font-size:0.8rem; color:var(--slate-500)">Deposit: ₹${Number(l.deposit).toLocaleString('en-IN')}</div>
                    </div>
                    <div class="listing-title">${prop.bedrooms || 2} BHK • ${prop.locality || 'Locality'}, ${prop.city || 'City'}</div>
                    <div class="listing-location">📍 ${prop.address || 'Address'}</div>

                    <div class="listing-specs">
                        <div class="spec-item">🛏️ ${prop.bedrooms || 2} BHK</div>
                        <div class="spec-item">📐 ${prop.area || 1100} sq ft</div>
                        <div class="spec-item">🛋️ ${(prop.furnishing || 'SEMI_FURNISHED').replace('_', ' ')}</div>
                    </div>

                    <!-- Verification Signals Card (Section 17 Preview) -->
                    <div class="verification-signals">
                        <div class="signal-row">
                            <span class="${prop.ownerId && prop.ownerName ? 'signal-check' : 'signal-alert'}">
                                ${prop.verificationStatus === 'VERIFIED' ? '✓' : '⚠️'}
                            </span>
                            <span>${prop.verificationStatus === 'VERIFIED' ? 'Owner Identity Verified' : 'Owner Unverified'}</span>
                        </div>
                        <div class="signal-row">
                            <span class="${isLow ? 'signal-check' : 'signal-alert'}">${isLow ? '✓' : '⚠️'}</span>
                            <span>Risk Score: <strong>${l.riskScore}/100</strong> — ${l.riskLabel}</span>
                        </div>
                    </div>

                    <div class="listing-actions">
                        <button class="btn btn-secondary btn-sm" style="flex:1" onclick="openRiskModal(${l.id})">
                            🔍 Inspect Risk
                        </button>
                        <button class="btn btn-primary btn-sm" style="flex:1" onclick="openVisitModal(${l.id})" ${isSuspended ? 'disabled' : ''}>
                            📅 Request Visit
                        </button>
                        <button class="btn btn-secondary btn-sm" style="padding:0.25rem 0.6rem" title="Report Scam" onclick="openReportModal(${l.id})">
                            🚩
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Risk Breakdown Modal (Explainable Detection Engine)
async function openRiskModal(listingId) {
    try {
        const [res, reviewsRes] = await Promise.all([
            apiFetch(`/listings/${listingId}`),
            apiFetch(`/listings/${listingId}/reviews`)
        ]);
        const listing = res.data;
        const reviews = reviewsRes.data || [];
        const prop = listing.property || {};
        const isLow = listing.riskScore <= 29;
        const isMed = listing.riskScore >= 30 && listing.riskScore <= 59;
        const meterClass = isLow ? 'low' : isMed ? 'medium' : 'high';

        let eventsHtml = '';
        if (listing.riskEvents && listing.riskEvents.length > 0) {
            eventsHtml = listing.riskEvents.map(e => `
                <div class="risk-event-item">
                    <div class="risk-event-title">
                        <span>⚠️ ${e.ruleCode.replace(/_/g, ' ')}</span>
                        <span>+${e.points} pts</span>
                    </div>
                    <div class="risk-event-desc">${e.explanation}</div>
                </div>
            `).join('');
        } else {
            eventsHtml = `
                <div style="background:var(--risk-low-bg); padding:1rem; border-radius:var(--radius-md); border:1px solid var(--risk-low-border); color:#065f46; font-size:0.9rem">
                    ✓ Clean Audit: No suspicious scam patterns or duplicate records identified for this listing.
                </div>
            `;
        }

        let positiveSignalsHtml = '';
        if (listing.positiveSignals && listing.positiveSignals.length > 0) {
            positiveSignalsHtml = `
                <div style="margin-top:1.25rem">
                    <h5 style="font-size:0.85rem; text-transform:uppercase; color:var(--slate-500); margin-bottom:0.5rem">Verified Trust Signals</h5>
                    <div style="display:flex; flex-direction:column; gap:0.4rem; font-size:0.85rem; color:#065f46">
                        ${listing.positiveSignals.map(s => `<div><span style="font-weight:bold">✓</span> ${s}</div>`).join('')}
                    </div>
                </div>
            `;
        }

        let reviewsHtml = '';
        if (reviews.length > 0) {
            reviewsHtml = `
                <div style="margin-top:1.5rem">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem">
                        <h5 style="font-size:0.85rem; text-transform:uppercase; color:var(--slate-500)">Tenant Reviews (${reviews.length})</h5>
                        <button class="btn btn-secondary btn-sm" onclick="closeModal(); openReviewModal(${listing.id})">⭐ Add Review</button>
                    </div>
                    <div style="display:flex; flex-direction:column; gap:0.6rem">
                        ${reviews.map(r => `
                            <div style="background:var(--slate-50); border:1px solid var(--slate-200); padding:0.75rem 1rem; border-radius:var(--radius-sm)">
                                <div style="display:flex; justify-content:space-between; font-size:0.8rem; margin-bottom:0.25rem">
                                    <strong>${r.tenantName || 'Tenant'}</strong>
                                    <span style="color:#f59e0b">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</span>
                                </div>
                                <div style="font-size:0.85rem; color:var(--slate-700)">${r.comment}</div>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        } else {
            reviewsHtml = `
                <div style="margin-top:1.5rem; display:flex; justify-content:space-between; align-items:center; background:var(--slate-50); padding:0.75rem 1rem; border-radius:var(--radius-sm)">
                    <span style="font-size:0.85rem; color:var(--slate-500)">No tenant reviews submitted yet.</span>
                    <button class="btn btn-secondary btn-sm" onclick="closeModal(); openReviewModal(${listing.id})">⭐ Write First Review</button>
                </div>
            `;
        }

        const modalBody = document.getElementById('modal-body');
        document.getElementById('modal-title').textContent = `Risk & Verification Audit — Listing #${listing.id}`;

        modalBody.innerHTML = `
            <div class="risk-meter-box ${meterClass}">
                <div class="risk-meter-header">
                    <div>
                        <div style="font-size:0.8rem; font-weight:700; text-transform:uppercase; letter-spacing:0.05em">Explainable Risk Level</div>
                        <div style="font-size:1.1rem; font-weight:800">${listing.riskLabel}</div>
                    </div>
                    <div class="risk-score-big">${listing.riskScore} <span style="font-size:1rem; font-weight:600">/ 100</span></div>
                </div>
                <div class="risk-bar">
                    <div class="risk-bar-fill" style="width:${listing.riskScore}%; background:${listing.riskColorCode}"></div>
                </div>
                <div style="font-size:0.75rem; color:var(--slate-500); display:flex; justify-content:space-between">
                    <span>0 (Safe)</span>
                    <span>30 (Medium)</span>
                    <span>60+ (High Risk Flag)</span>
                </div>
            </div>

            <h4 style="font-size:0.95rem; font-weight:700; margin-bottom:0.75rem; color:var(--slate-800)">
                Risk Factors Breakdown (Java Rule & Statistical Engine)
            </h4>
            ${eventsHtml}
            ${positiveSignalsHtml}
            ${reviewsHtml}

            <div style="margin-top:1.5rem; padding:1rem; background:var(--slate-50); border-radius:var(--radius-md); border:1px solid var(--slate-200); font-size:0.8rem; color:var(--slate-600)">
                <strong>ℹ️ How RentGuard Evaluates Risk:</strong> RentGuard calculates explainable points using address normalization, geo-distance tolerance (<150m), SHA-256 image hashes, locality median price deviation, and admin KYC status. Capped at 100.
            </div>
        `;

        document.getElementById('modal-footer').innerHTML = `
            <button class="btn btn-secondary" onclick="closeModal()">Close</button>
            <button class="btn btn-primary" onclick="closeModal(); openVisitModal(${listing.id})">Schedule Visit</button>
        `;

        openModal();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Visit Request Modal
function openVisitModal(listingId) {
    if (!state.user) {
        showToast('Please login to request a property visit', 'error');
        openLoginModal();
        return;
    }

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minDate = tomorrow.toISOString().split('T')[0];

    document.getElementById('modal-title').textContent = `Request Property Visit — Listing #${listingId}`;
    document.getElementById('modal-body').innerHTML = `
        <form id="visit-form" onsubmit="submitVisitRequest(event, ${listingId})">
            <div class="form-group" style="margin-bottom:1rem">
                <label>Preferred Visit Date</label>
                <input type="date" id="visit-date" class="form-control" min="${minDate}" required value="${minDate}">
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>Message / Timing Preferences for Owner</label>
                <textarea id="visit-notes" class="form-control" rows="3" placeholder="e.g. Prefer morning 11 AM, would like to inspect parking space..."></textarea>
            </div>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="document.getElementById('visit-form').requestSubmit()">Submit Visit Request</button>
    `;

    openModal();
}

async function submitVisitRequest(e, listingId) {
    e.preventDefault();
    const date = document.getElementById('visit-date').value;
    const notes = document.getElementById('visit-notes').value;

    try {
        await apiFetch(`/listings/${listingId}/visits`, {
            method: 'POST',
            body: JSON.stringify({ requestedDate: date, notes })
        });
        closeModal();
        showToast('Visit request successfully submitted to property owner!', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Report Scam Modal
function openReportModal(listingId) {
    if (!state.user) {
        showToast('Please login to submit a scam report', 'error');
        openLoginModal();
        return;
    }

    document.getElementById('modal-title').textContent = `Report Suspicious Listing #${listingId}`;
    document.getElementById('modal-body').innerHTML = `
        <form id="report-form" onsubmit="submitScamReport(event, ${listingId})">
            <div class="form-group" style="margin-bottom:1rem">
                <label>Scam Category / Reason</label>
                <select id="report-reason" class="form-control" required>
                    <option value="PRICE_SCAM">Price scam / Advance fee requested</option>
                    <option value="DUPLICATE_LISTING">Duplicate listing of another property</option>
                    <option value="FAKE_PHOTOS">Fake or stolen property photos</option>
                    <option value="ADVANCE_FEE_FRAUD">Demand advance deposit before viewing</option>
                    <option value="UNAVAILABLE_PROPERTY">Property does not exist or unavailable</option>
                    <option value="SUSPICIOUS_IDENTITY">Owner identity appears fake</option>
                    <option value="OTHER">Other fraud / safety concern</option>
                </select>
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>Detailed Description</label>
                <textarea id="report-desc" class="form-control" rows="4" required placeholder="Please describe what the owner asked or why this listing is fraudulent..."></textarea>
            </div>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
        <button class="btn btn-danger" onclick="document.getElementById('report-form').requestSubmit()">Submit Report</button>
    `;

    openModal();
}

async function submitScamReport(e, listingId) {
    e.preventDefault();
    const reason = document.getElementById('report-reason').value;
    const description = document.getElementById('report-desc').value;

    try {
        await apiFetch(`/listings/${listingId}/report`, {
            method: 'POST',
            body: JSON.stringify({ reason, description })
        });
        closeModal();
        showToast('Report submitted! Listing risk score updated and flagged for admin investigation.', 'success');
        loadListings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Review Modal
function openReviewModal(listingId) {
    if (!state.user) {
        showToast('Please login to leave a rating and review', 'error');
        openLoginModal();
        return;
    }

    document.getElementById('modal-title').textContent = `Write a Review — Listing #${listingId}`;
    document.getElementById('modal-body').innerHTML = `
        <form id="review-form" onsubmit="submitReview(event, ${listingId})">
            <div class="form-group" style="margin-bottom:1rem">
                <label>Rating</label>
                <select id="review-rating" class="form-control" required>
                    <option value="5">⭐⭐⭐⭐⭐ 5 Stars - Excellent & Genuine</option>
                    <option value="4">⭐⭐⭐⭐ 4 Stars - Very Good</option>
                    <option value="3">⭐⭐⭐ 3 Stars - Average</option>
                    <option value="2">⭐⭐ 2 Stars - Disappointing</option>
                    <option value="1">⭐ 1 Star - Poor / Misleading</option>
                </select>
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>Review Comment</label>
                <textarea id="review-comment" class="form-control" rows="3" required placeholder="Share your experience inspecting the flat, meeting the owner, or checking documents..."></textarea>
            </div>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="document.getElementById('review-form').requestSubmit()">Post Review</button>
    `;

    openModal();
}

async function submitReview(e, listingId) {
    e.preventDefault();
    const rating = parseInt(document.getElementById('review-rating').value);
    const comment = document.getElementById('review-comment').value;

    try {
        await apiFetch(`/listings/${listingId}/review`, {
            method: 'POST',
            body: JSON.stringify({ rating, comment })
        });
        closeModal();
        showToast('Review posted successfully! Thank you for helping the community.', 'success');
        loadListings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Owner Portal
async function loadOwnerPortal() {
    const pane = document.getElementById('pane-owner');
    pane.innerHTML = '<div style="text-align:center; padding:3rem">Loading Owner Dashboard...</div>';

    try {
        const [listingsRes, visitsRes, propsRes, docsRes] = await Promise.all([
            apiFetch('/listings/my'),
            apiFetch('/visits/owner'),
            apiFetch('/properties'),
            apiFetch('/verification/my')
        ]);

        const myListings = listingsRes.data || [];
        const myVisits = visitsRes.data || [];
        const myProps = propsRes.data || [];
        const myDocs = docsRes.data || [];

        pane.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem">
                <div>
                    <h2 style="font-size:1.5rem; font-weight:800; color:var(--slate-900)">Owner Dashboard</h2>
                    <p style="color:var(--slate-500)">Manage your rental properties, listings, KYC verification, and tenant visit requests.</p>
                </div>
                <div style="display:flex; gap:0.5rem">
                    <button class="btn btn-secondary" onclick="openKycUploadModal()">📄 Upload KYC Document</button>
                    <button class="btn btn-primary" onclick="openCreateListingModal()">➕ Add New Listing</button>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">My Registered Properties</div>
                    <div class="stat-value">${myProps.length}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Active Listings</div>
                    <div class="stat-value">${myListings.filter(l => l.status === 'ACTIVE').length}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">KYC Documents</div>
                    <div class="stat-value">${myDocs.length}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Visit Requests</div>
                    <div class="stat-value">${myVisits.length}</div>
                </div>
            </div>

            <!-- My Listings Table -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>My Property Listings</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Property</th>
                            <th>Rent / Deposit</th>
                            <th>Risk Score</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${myListings.length ? myListings.map(l => `
                            <tr>
                                <td>#${l.id}</td>
                                <td>${l.property?.address || 'Property'}</td>
                                <td>₹${Number(l.rent).toLocaleString()} / ₹${Number(l.deposit).toLocaleString()}</td>
                                <td>
                                    <span class="brand-badge" style="background:${l.riskColorCode}20; color:${l.riskColorCode}; font-weight:700">
                                        ${l.riskScore}/100 — ${l.riskLabel}
                                    </span>
                                </td>
                                <td><strong>${l.status}</strong></td>
                                <td>
                                    <button class="btn btn-secondary btn-sm" onclick="openRiskModal(${l.id})">Risk Audit</button>
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--slate-400)">No listings published yet. Click "Add New Listing" to publish.</td></tr>'}
                    </tbody>
                </table>
            </div>

            <!-- Visit Requests Table -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>Received Visit Requests</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Listing</th>
                            <th>Tenant</th>
                            <th>Requested Date</th>
                            <th>Notes</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${myVisits.length ? myVisits.map(v => `
                            <tr>
                                <td>#${v.id}</td>
                                <td>#${v.listingId} (${v.propertyAddress || ''})</td>
                                <td>${v.tenantName || 'Tenant'} (${v.tenantPhone || 'N/A'})</td>
                                <td>${v.requestedDate}</td>
                                <td>${v.notes || '—'}</td>
                                <td><strong>${v.status}</strong></td>
                                <td>
                                    ${v.status === 'PENDING' ? `
                                        <button class="btn btn-success btn-sm" onclick="updateVisitStatus(${v.id}, 'ACCEPTED')">Accept</button>
                                        <button class="btn btn-danger btn-sm" onclick="updateVisitStatus(${v.id}, 'DECLINED')">Decline</button>
                                    ` : `<span style="font-size:0.8rem; color:var(--slate-400)">${v.status}</span>`}
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="7" style="text-align:center; padding:2rem; color:var(--slate-400)">No visit requests received yet.</td></tr>'}
                    </tbody>
                </table>
            </div>
        `;
    } catch (err) {
        pane.innerHTML = `<div style="color:var(--risk-high); padding:2rem">Failed to load owner portal: ${err.message}</div>`;
    }
}

async function updateVisitStatus(visitId, status) {
    const notes = prompt(`Optional message for the tenant upon ${status.toLowerCase()}ing:`);
    try {
        await apiFetch(`/visits/${visitId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status, responseNotes: notes || '' })
        });
        showToast(`Visit request ${status.toLowerCase()} successfully`, 'success');
        loadOwnerPortal();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Modal for Creating Listing
function openCreateListingModal() {
    document.getElementById('modal-title').textContent = 'Add New Property & Publish Listing';
    document.getElementById('modal-body').innerHTML = `
        <form id="create-listing-form" onsubmit="submitNewListing(event)">
            <h5 style="font-size:0.85rem; text-transform:uppercase; color:var(--slate-500); margin-bottom:0.75rem">1. Property Specifications</h5>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Address</label>
                <input type="text" id="prop-address" class="form-control" required placeholder="e.g. Flat 301, Tower C, Emerald Heights">
            </div>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.75rem; margin-bottom:0.75rem">
                <div class="form-group">
                    <label>Locality</label>
                    <input type="text" id="prop-locality" class="form-control" required placeholder="e.g. Sector 62">
                </div>
                <div class="form-group">
                    <label>City</label>
                    <input type="text" id="prop-city" class="form-control" required placeholder="e.g. Noida">
                </div>
            </div>
            <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:0.75rem; margin-bottom:0.75rem">
                <div class="form-group">
                    <label>Type</label>
                    <select id="prop-type" class="form-control">
                        <option value="APARTMENT">Apartment</option>
                        <option value="INDEPENDENT_HOUSE">Independent House</option>
                        <option value="VILLA">Villa</option>
                        <option value="STUDIO">Studio</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Bedrooms (BHK)</label>
                    <input type="number" id="prop-bhk" class="form-control" min="1" max="10" value="2" required>
                </div>
                <div class="form-group">
                    <label>Area (sq ft)</label>
                    <input type="number" id="prop-area" class="form-control" min="200" value="1150" required>
                </div>
            </div>

            <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.75rem; margin-bottom:1.25rem">
                <div class="form-group">
                    <label>Latitude</label>
                    <input type="number" step="0.0001" id="prop-lat" class="form-control" value="28.6280" required>
                </div>
                <div class="form-group">
                    <label>Longitude</label>
                    <input type="number" step="0.0001" id="prop-lng" class="form-control" value="77.3649" required>
                </div>
            </div>

            <h5 style="font-size:0.85rem; text-transform:uppercase; color:var(--slate-500); margin-bottom:0.75rem">2. Listing Financials & Terms</h5>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.75rem; margin-bottom:0.75rem">
                <div class="form-group">
                    <label>Monthly Rent (₹)</label>
                    <input type="number" id="list-rent" class="form-control" min="1000" value="18000" required>
                </div>
                <div class="form-group">
                    <label>Security Deposit (₹)</label>
                    <input type="number" id="list-deposit" class="form-control" min="0" value="36000" required>
                </div>
            </div>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Description</label>
                <textarea id="list-desc" class="form-control" rows="3" required placeholder="Describe amenities, connectivity, terms..."></textarea>
            </div>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Image URL (SHA-256 hash is generated automatically for reuse check)</label>
                <input type="url" id="list-image" class="form-control" placeholder="https://..." value="https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800">
            </div>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="document.getElementById('create-listing-form').requestSubmit()">Publish & Run Risk Engine</button>
    `;

    openModal();
}

async function submitNewListing(e) {
    e.preventDefault();
    try {
        // Step 1: Create Property
        const propData = {
            address: document.getElementById('prop-address').value,
            locality: document.getElementById('prop-locality').value,
            city: document.getElementById('prop-city').value,
            latitude: parseFloat(document.getElementById('prop-lat').value),
            longitude: parseFloat(document.getElementById('prop-lng').value),
            type: document.getElementById('prop-type').value,
            bedrooms: parseInt(document.getElementById('prop-bhk').value),
            area: parseFloat(document.getElementById('prop-area').value),
            furnishing: 'SEMI_FURNISHED'
        };

        const propRes = await apiFetch('/properties', {
            method: 'POST',
            body: JSON.stringify(propData)
        });

        const propertyId = propRes.data.id;

        // Step 2: Create Listing
        const imgUrl = document.getElementById('list-image').value;
        const listData = {
            propertyId,
            rent: parseFloat(document.getElementById('list-rent').value),
            deposit: parseFloat(document.getElementById('list-deposit').value),
            description: document.getElementById('list-desc').value,
            imageUrls: imgUrl ? [imgUrl] : []
        };

        const listRes = await apiFetch('/listings', {
            method: 'POST',
            body: JSON.stringify(listData)
        });

        closeModal();
        showToast(`Listing #${listRes.data.id} published! Evaluated Risk Score: ${listRes.data.riskScore}/100 (${listRes.data.riskLabel})`, 'success');
        loadOwnerPortal();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Modal for KYC Upload
function openKycUploadModal() {
    document.getElementById('modal-title').textContent = 'Submit KYC / Property Ownership Verification';
    document.getElementById('modal-body').innerHTML = `
        <form id="kyc-form" onsubmit="submitKycDoc(event)">
            <div class="form-group" style="margin-bottom:1rem">
                <label>Verification Document Type</label>
                <select id="kyc-type" class="form-control" required>
                    <option value="GOVERNMENT_ID">Government ID (Aadhaar / Passport / Voter ID)</option>
                    <option value="PROPERTY_TAX_RECEIPT">Property Tax Receipt / Registry</option>
                    <option value="ELECTRICITY_BILL">Electricity / Utility Bill</option>
                    <option value="TITLE_DEED">Title Deed / Sale Deed</option>
                    <option value="RENTAL_AGREEMENT">Rental Agreement</option>
                </select>
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>Document Reference / File Identifier</label>
                <input type="text" id="kyc-file" class="form-control" required placeholder="e.g. kyc_aadhaar_card_doc.pdf" value="kyc_aadhaar_card_doc.pdf">
                <span style="font-size:0.75rem; color:var(--slate-500)">For college demonstration, enter document file reference or filename.</span>
            </div>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="document.getElementById('kyc-form').requestSubmit()">Submit for Admin Verification</button>
    `;

    openModal();
}

async function submitKycDoc(e) {
    e.preventDefault();
    const type = document.getElementById('kyc-type').value;
    const filePath = document.getElementById('kyc-file').value;

    try {
        await apiFetch('/verification/submit', {
            method: 'POST',
            body: JSON.stringify({ type, filePath })
        });
        closeModal();
        showToast('Verification document submitted! Moved to Admin Queue for approval.', 'success');
        loadOwnerPortal();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Tenant Portal
async function loadTenantPortal() {
    const pane = document.getElementById('pane-tenant');
    pane.innerHTML = '<div style="text-align:center; padding:3rem">Loading Tenant Portal...</div>';

    try {
        const [visitsRes, reportsRes] = await Promise.all([
            apiFetch('/visits/tenant'),
            apiFetch('/reports/my')
        ]);

        const visits = visitsRes.data || [];
        const reports = reportsRes.data || [];

        pane.innerHTML = `
            <div style="margin-bottom:1.5rem">
                <h2 style="font-size:1.5rem; font-weight:800; color:var(--slate-900)">Tenant Activity Dashboard</h2>
                <p style="color:var(--slate-500)">Track scheduled property visits, visit confirmations, and reports submitted to platform moderators.</p>
            </div>

            <!-- Visits Table -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>My Scheduled Property Visits</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Listing</th>
                            <th>Address</th>
                            <th>Visit Date</th>
                            <th>My Notes</th>
                            <th>Owner Status & Notes</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${visits.length ? visits.map(v => `
                            <tr>
                                <td>#${v.listingId} (₹${Number(v.rent).toLocaleString()}/mo)</td>
                                <td>${v.propertyAddress || 'Address'}</td>
                                <td><strong>${v.requestedDate}</strong></td>
                                <td>${v.notes || '—'}</td>
                                <td>
                                    <span class="brand-badge" style="background:${v.status === 'ACCEPTED' ? '#ecfdf5' : v.status === 'DECLINED' ? '#fef2f2' : '#fffbeb'}; color:${v.status === 'ACCEPTED' ? '#065f46' : v.status === 'DECLINED' ? '#991b1b' : '#92400e'}">
                                        ${v.status}
                                    </span>
                                    ${v.ownerResponse ? `<div style="font-size:0.8rem; margin-top:0.25rem">Owner: ${v.ownerResponse}</div>` : ''}
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--slate-400)">No visits requested yet. Browse properties and click "Request Visit".</td></tr>'}
                    </tbody>
                </table>
            </div>

            <!-- Reports Table -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>My Submitted Scam Reports</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Report ID</th>
                            <th>Listing</th>
                            <th>Reason</th>
                            <th>Description</th>
                            <th>Investigation Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${reports.length ? reports.map(r => `
                            <tr>
                                <td>#${r.id}</td>
                                <td>#${r.listingId}</td>
                                <td><strong>${r.reasonDescription || r.reason}</strong></td>
                                <td>${r.description}</td>
                                <td>
                                    <span class="brand-badge">${r.status}</span>
                                    ${r.adminNotes ? `<div style="font-size:0.8rem; margin-top:0.25rem">Admin: ${r.adminNotes}</div>` : ''}
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--slate-400)">No scam reports filed by your account.</td></tr>'}
                    </tbody>
                </table>
            </div>
        `;
    } catch (err) {
        pane.innerHTML = `<div style="color:var(--risk-high); padding:2rem">Failed to load tenant portal: ${err.message}</div>`;
    }
}

// Admin Dashboard & Analytics (Sections 14, 18)
async function loadAdminDashboard() {
    const pane = document.getElementById('pane-admin');
    pane.innerHTML = '<div style="text-align:center; padding:3rem">Loading Admin Analytics & Queues...</div>';

    try {
        const [analyticsRes, verificationsRes, reportsRes, auditRes] = await Promise.all([
            apiFetch('/admin/analytics'),
            apiFetch('/admin/verifications'),
            apiFetch('/admin/reports'),
            apiFetch('/admin/audit-logs')
        ]);

        const a = analyticsRes.data;
        const pendingVerifications = verificationsRes.data || [];
        const reports = reportsRes.data || [];
        const auditLogs = auditRes.data || [];

        const riskDist = a.riskScoreDistribution || {};

        pane.innerHTML = `
            <div style="margin-bottom:1.5rem">
                <h2 style="font-size:1.5rem; font-weight:800; color:var(--slate-900)">Admin Fraud & Scam Analytics Dashboard</h2>
                <p style="color:var(--slate-500)">Platform surveillance, KYC verification queues, scam moderation, and explainable risk telemetry.</p>
            </div>

            <!-- Stats Grid (Section 18) -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Total Listings</div>
                    <div class="stat-value">${a.totalListings}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Verified Owners</div>
                    <div class="stat-value" style="color:var(--risk-low)">${a.verifiedOwners}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Pending KYC Verifications</div>
                    <div class="stat-value" style="color:var(--risk-med)">${a.pendingVerifications}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">High-Risk Listings (Score ≥ 60)</div>
                    <div class="stat-value" style="color:var(--risk-high)">${a.highRiskListings}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Open Scam Reports</div>
                    <div class="stat-value" style="color:var(--risk-high)">${a.openReports}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Suspended Listings</div>
                    <div class="stat-value">${a.suspendedListings}</div>
                </div>
            </div>

            <!-- Visual Risk Distribution Bar -->
            <div style="background:#fff; border:1px solid var(--slate-200); border-radius:var(--radius-lg); padding:1.5rem; margin-bottom:2rem">
                <h4 style="font-size:1rem; font-weight:700; margin-bottom:0.75rem">Platform Risk Score Distribution (Explainable Java Logic)</h4>
                <div style="height:1.25rem; border-radius:var(--radius-full); overflow:hidden; display:flex; margin-bottom:0.75rem">
                    <div style="width:${(riskDist.LOW || 0) / (a.totalListings || 1) * 100}%; background:var(--risk-low)" title="Low Risk: ${riskDist.LOW || 0}"></div>
                    <div style="width:${(riskDist.MEDIUM || 0) / (a.totalListings || 1) * 100}%; background:var(--risk-med)" title="Medium Risk: ${riskDist.MEDIUM || 0}"></div>
                    <div style="width:${(riskDist.HIGH || 0) / (a.totalListings || 1) * 100}%; background:var(--risk-high)" title="High Risk: ${riskDist.HIGH || 0}"></div>
                </div>
                <div style="display:flex; gap:1.5rem; font-size:0.85rem">
                    <div><span style="color:var(--risk-low); font-weight:bold">●</span> Low Risk (0–29): <strong>${riskDist.LOW || 0} listings</strong></div>
                    <div><span style="color:var(--risk-med); font-weight:bold">●</span> Medium Risk (30–59): <strong>${riskDist.MEDIUM || 0} listings</strong></div>
                    <div><span style="color:var(--risk-high); font-weight:bold">●</span> High Risk (60–100): <strong>${riskDist.HIGH || 0} listings</strong></div>
                </div>
            </div>

            <!-- Pending Verification Queue (1-Click Approve / Reject) -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>Pending Owner KYC & Property Verification Queue (${pendingVerifications.length})</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Doc ID</th>
                            <th>Owner / Email</th>
                            <th>Document Type</th>
                            <th>File Reference</th>
                            <th>Submitted On</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${pendingVerifications.length ? pendingVerifications.map(doc => `
                            <tr>
                                <td>#${doc.id}</td>
                                <td><strong>${doc.ownerName || 'Owner'}</strong><br><span style="font-size:0.75rem; color:var(--slate-500)">${doc.ownerEmail || ''}</span></td>
                                <td>${doc.type}</td>
                                <td><code>${doc.filePath}</code></td>
                                <td>${doc.createdAt?.split('T')[0] || 'Today'}</td>
                                <td>
                                    <button class="btn btn-success btn-sm" onclick="processAdminVerification(${doc.id}, 'APPROVED')">✓ Approve KYC</button>
                                    <button class="btn btn-danger btn-sm" onclick="processAdminVerification(${doc.id}, 'REJECTED')">✕ Reject</button>
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--slate-400)">✓ Verification queue clear! All documents reviewed.</td></tr>'}
                    </tbody>
                </table>
            </div>

            <!-- Reports Moderation Queue -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>Open Scam Complaints & Reports Queue (${reports.length})</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Report ID</th>
                            <th>Listing</th>
                            <th>Reporter</th>
                            <th>Scam Reason</th>
                            <th>Details</th>
                            <th>Status</th>
                            <th>Moderation Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${reports.length ? reports.map(r => `
                            <tr>
                                <td>#${r.id}</td>
                                <td>#${r.listingId}</td>
                                <td>${r.reporterName}</td>
                                <td><span class="brand-badge" style="background:#fee2e2; color:#991b1b">${r.reasonDescription || r.reason}</span></td>
                                <td>${r.description}</td>
                                <td><strong>${r.status}</strong></td>
                                <td>
                                    <div style="display:flex; gap:0.35rem">
                                        <button class="btn btn-danger btn-sm" onclick="suspendListingDirect(${r.listingId}, 'Substantiated scam report #${r.id}')">Suspend Listing</button>
                                        <button class="btn btn-secondary btn-sm" onclick="resolveReportDirect(${r.id})">Mark Resolved</button>
                                    </div>
                                </td>
                            </tr>
                        `).join('') : '<tr><td colspan="7" style="text-align:center; padding:2rem; color:var(--slate-400)">No active scam reports found.</td></tr>'}
                    </tbody>
                </table>
            </div>

            <!-- Audit Logs Trail -->
            <div class="data-table-container">
                <div class="table-header-title">
                    <h4>Recent Security & Admin Audit Trail</h4>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Timestamp</th>
                            <th>Actor</th>
                            <th>Action</th>
                            <th>Entity</th>
                            <th>Audit Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${auditLogs.slice(0, 10).map(log => `
                            <tr>
                                <td style="font-size:0.75rem">${log.timestamp?.replace('T', ' ').substring(0, 19)}</td>
                                <td>${log.actorEmail || 'System'}</td>
                                <td><code>${log.action}</code></td>
                                <td>${log.entityType} #${log.entityId}</td>
                                <td>${log.details}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (err) {
        pane.innerHTML = `<div style="color:var(--risk-high); padding:2rem">Failed to load admin analytics: ${err.message}</div>`;
    }
}

async function processAdminVerification(docId, status) {
    const notes = prompt(`Enter administrative review notes for this ${status} decision:`,
        status === 'APPROVED' ? 'Government ID and ownership documents verified valid.' : 'Document is unclear or unverifiable.');
    if (notes === null) return;

    try {
        await apiFetch(`/admin/verifications/${docId}`, {
            method: 'PUT',
            body: JSON.stringify({ status, notes })
        });
        showToast(`Document #${docId} has been ${status}. Owner status and listing risks updated!`, 'success');
        loadAdminDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function suspendListingDirect(listingId, reason) {
    if (!confirm(`Are you sure you want to suspend Listing #${listingId}? It will be removed from tenant searches.`)) return;
    try {
        await apiFetch(`/admin/listings/${listingId}/suspend`, {
            method: 'PUT',
            body: JSON.stringify({ reason })
        });
        showToast(`Listing #${listingId} suspended successfully`, 'success');
        loadAdminDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function resolveReportDirect(reportId) {
    const notes = prompt('Enter resolution notes:', 'Investigated and resolved by platform administrator.');
    if (notes === null) return;
    try {
        await apiFetch(`/admin/reports/${reportId}`, {
            method: 'PUT',
            body: JSON.stringify({ status: 'RESOLVED', adminNotes: notes, suspendListing: false })
        });
        showToast(`Report #${reportId} marked as RESOLVED`, 'success');
        loadAdminDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Authentication Modal
function openLoginModal() {
    document.getElementById('modal-title').textContent = 'Account Sign In / Register';
    document.getElementById('modal-body').innerHTML = `
        <div class="tabs" style="margin-bottom:1rem">
            <button class="tab-btn active" id="auth-tab-login" onclick="toggleAuthForm('login')">Sign In</button>
            <button class="tab-btn" id="auth-tab-register" onclick="toggleAuthForm('register')">Register New Account</button>
        </div>

        <form id="login-form" onsubmit="handleAuthLogin(event)">
            <div class="form-group" style="margin-bottom:1rem">
                <label>Email Address</label>
                <input type="email" id="login-email" class="form-control" required placeholder="name@example.com">
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>Password</label>
                <input type="password" id="login-password" class="form-control" required placeholder="••••••••">
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%">Sign In</button>
        </form>

        <form id="register-form" style="display:none" onsubmit="handleAuthRegister(event)">
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Full Name</label>
                <input type="text" id="reg-name" class="form-control" required placeholder="e.g. John Doe">
            </div>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Email Address</label>
                <input type="email" id="reg-email" class="form-control" required placeholder="john@example.com">
            </div>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Password</label>
                <input type="password" id="reg-password" class="form-control" required minlength="6" placeholder="At least 6 characters">
            </div>
            <div class="form-group" style="margin-bottom:0.75rem">
                <label>Phone Number</label>
                <input type="tel" id="reg-phone" class="form-control" placeholder="+91 98765 43210">
            </div>
            <div class="form-group" style="margin-bottom:1rem">
                <label>I want to join as</label>
                <select id="reg-role" class="form-control" required>
                    <option value="ROLE_TENANT">Tenant (Search & Rent Properties)</option>
                    <option value="ROLE_OWNER">Property Owner / Landlord</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%">Create Account</button>
        </form>
    `;

    document.getElementById('modal-footer').innerHTML = `
        <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
    `;

    openModal();
}

function toggleAuthForm(mode) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginTab = document.getElementById('auth-tab-login');
    const registerTab = document.getElementById('auth-tab-register');

    if (mode === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        loginTab.classList.remove('active');
        registerTab.classList.add('active');
    }
}

async function handleAuthLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const res = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        setSession(res.data);
        closeModal();
        showToast(`Welcome back, ${res.data.name}!`, 'success');
        updateTabViews();
        loadListings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleAuthRegister(e) {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const phone = document.getElementById('reg-phone').value;
    const role = document.getElementById('reg-role').value;

    try {
        const res = await apiFetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, password, phone, role })
        });
        setSession(res.data);
        closeModal();
        showToast(`Account registered successfully as ${role.replace('ROLE_', '')}!`, 'success');
        updateTabViews();
        loadListings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Modal Helpers
function openModal() {
    document.getElementById('app-modal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('app-modal').style.display = 'none';
}
