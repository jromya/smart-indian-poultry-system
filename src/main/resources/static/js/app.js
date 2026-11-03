/**
 * Smart Indian Poultry Farm Management System - Core Frontend Engine
 * Handles REST API calls, RBAC Shell, 11-Language Switching, Web Speech Voice Input/TTS & Offline Drafts
 */
const PoultryApp = {
    currentUser: null,

    async apiFetch(url, options = {}) {
        const headers = Object.assign({ "Accept": "application/json" }, options.headers || {});
        if (options.body && !(options.body instanceof FormData)) {
            headers["Content-Type"] = "application/json";
        }
        const response = await fetch(url, Object.assign({}, options, {
            headers,
            credentials: "same-origin"
        }));

        let payload = null;
        try {
            payload = await response.json();
        } catch (err) {
            payload = { success: false, message: "Invalid server response" };
        }

        if (response.status === 401 && !url.includes("/api/auth/login") && !url.includes("/api/auth/me")) {
            window.location.href = "/login.html";
            throw new Error("Session expired. Redirecting to login...");
        }

        if (!response.ok || (payload && payload.success === false)) {
            const msg = (payload && payload.message) ? payload.message : `Request failed (${response.status})`;
            throw new Error(msg);
        }
        return payload;
    },

    async requireAuth(allowedRoles = null) {
        try {
            const res = await this.apiFetch("/api/auth/me");
            this.currentUser = res.data;
            if (this.currentUser && this.currentUser.preferredLanguage) {
                localStorage.setItem("poultry_lang", this.currentUser.preferredLanguage);
            }
            if (allowedRoles && !allowedRoles.includes(this.currentUser.role)) {
                this.showToast("Access restricted for role: " + this.currentUser.roleDisplayName, "danger");
                setTimeout(() => { window.location.href = "/dashboard.html"; }, 900);
                return null;
            }
            return this.currentUser;
        } catch (e) {
            window.location.href = "/login.html";
            return null;
        }
    },

    buildLanguageOptionsHtml() {
        const current = getCurrentLang();
        return Object.entries(POULTRY_I18N.languageNames)
            .map(([code, label]) => `<option value="${code}" ${code === current ? "selected" : ""}>🌐 ${label}</option>`)
            .join("");
    },

    async changeLanguage(langCode, syncBackend = true) {
        localStorage.setItem("poultry_lang", langCode);
        applyPageTranslations();
        if (syncBackend && this.currentUser) {
            try {
                await this.apiFetch("/api/auth/language", {
                    method: "PATCH",
                    body: JSON.stringify({ preferredLanguage: langCode })
                });
            } catch (err) {
                console.warn("Language saved locally; backend sync skipped:", err.message);
            }
        }
    },

    renderProtectedShell(activePage) {
        const navContainer = document.getElementById("appNavbarMount");
        if (!navContainer || !this.currentUser) return;

        const role = this.currentUser.role;
        const isFarmRole = ["SUPER_ADMIN", "FARM_ADMIN", "FARM_MANAGER", "FARMER"].includes(role);
        const isFarmBizRole = ["SUPER_ADMIN", "FARM_ADMIN", "FARM_MANAGER"].includes(role);
        const isCommerceRole = ["SUPER_ADMIN", "FARM_ADMIN", "FARM_MANAGER", "RETAILER", "CHICKEN_SHOP_OWNER"].includes(role);
        const canManageUsers = ["SUPER_ADMIN", "FARM_ADMIN"].includes(role);

        navContainer.innerHTML = `
        <nav class="navbar navbar-expand-lg agri-navbar sticky-top py-2">
            <div class="container-fluid px-3">
                <a class="navbar-brand" href="/dashboard.html">
                    <span>🐓</span> <span data-i18n="app_title">Smart Indian Poultry</span>
                </a>
                <button class="navbar-toggler bg-light" type="button" data-bs-toggle="collapse" data-bs-target="#mainAgriNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainAgriNav">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0 gap-1">
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'dashboard' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/dashboard.html">
                                📊 <span data-i18n="nav_dashboard">Dashboard</span>
                            </a>
                        </li>
                        ${isFarmRole ? `
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['farms','houses'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                🏡 <span data-i18n="nav_farms">My Farms</span>
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/farms.html">🏡 <span data-i18n="nav_farms">My Farms</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/poultry-houses.html">🏗️ <span data-i18n="nav_houses">Poultry Sheds</span></a></li>
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${['batches','create-batch'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/batches.html">
                                🐣 <span data-i18n="nav_batches">Batches</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'daily' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/daily-records.html">
                                📝 <span data-i18n="nav_daily_records">Daily Entry</span>
                            </a>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['broiler','layer','weight','eggs'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                📈 Flock Ops
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/broiler-management.html">🍗 <span data-i18n="nav_broiler">Broiler FCR</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/layer-management.html">🥚 <span data-i18n="nav_layer">Layer & Eggs</span></a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/weight-management.html">⚖️ <span data-i18n="nav_weight">Bird Weight</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/egg-production.html">🧺 <span data-i18n="nav_egg_entry">Egg Collection</span></a></li>
                            </ul>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['feed','medicine','vaccination'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                🌾 Health & Feed
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/feed-management.html">🌾 <span data-i18n="nav_feed">Feed Management</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/medicine.html">💊 <span data-i18n="nav_medicine">Medicine & Vet</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/vaccination.html">💉 <span data-i18n="nav_vaccination">Vaccination Schedule</span></a></li>
                            </ul>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['inventory','suppliers','purchases'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                📦 <span data-i18n="nav_inventory">Inventory & Stock</span>
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/inventory.html">📦 <span data-i18n="nav_inventory">Inventory & Stock</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/suppliers.html">🚚 <span data-i18n="nav_suppliers">Suppliers</span></a></li>
                                ${role !== 'FARMER' ? `<li><a class="dropdown-item py-2 fw-semibold" href="/purchases.html">🧾 <span data-i18n="nav_purchases">Purchase Invoices</span></a></li>` : ''}
                            </ul>
                        </li>` : ""}
                        ${role === 'RETAILER' ? `
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'retailers' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/retailers.html">
                                🏪 <span data-i18n="nav_retailers">Retail Bird & Egg Store</span>
                            </a>
                        </li>` : ""}
                        ${role === 'CHICKEN_SHOP_OWNER' ? `
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'chicken-shop' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/chicken-shop.html">
                                🔪 <span data-i18n="nav_chicken_shop">Chicken Cutting Shop</span>
                            </a>
                        </li>` : ""}
                        ${isCommerceRole ? `
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['wholesalers','retailers','chicken-shop','customers','sales','expenses','payments'].includes(activePage) && role !== 'RETAILER' && role !== 'CHICKEN_SHOP_OWNER' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                💰 Sales & Finance
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/sales.html">💰 <span data-i18n="nav_sales">Sales & Profit/Loss</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/expenses.html">📉 <span data-i18n="nav_expenses">Expenses Ledger</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/payments.html">💳 <span data-i18n="nav_payments">Payments & Dues</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/customers.html">🤝 <span data-i18n="nav_customers">Customers & Buyers</span></a></li>
                                ${isFarmBizRole ? `
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/wholesalers.html">🚛 <span data-i18n="nav_wholesalers">Wholesale Dispatch</span></a></li>` : ""}
                                ${["SUPER_ADMIN", "FARM_ADMIN"].includes(role) ? `
                                <li><a class="dropdown-item py-2 fw-semibold" href="/retailers.html">🏪 <span data-i18n="nav_retailers">Retail Bird & Egg Store</span></a></li>
                                <li><a class="dropdown-item py-2 fw-semibold" href="/chicken-shop.html">🔪 <span data-i18n="nav_chicken_shop">Chicken Cutting Shop</span></a></li>` : ""}
                            </ul>
                        </li>` : ""}
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white fw-semibold ${['analytics','reports'].includes(activePage) ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="#" role="button" data-bs-toggle="dropdown">
                                📉 Insights & PDF
                            </a>
                            <ul class="dropdown-menu rounded-3 shadow">
                                <li><a class="dropdown-item py-2 fw-semibold" href="/analytics.html">📈 <span data-i18n="nav_analytics">Analytics (15 Charts)</span></a></li>
                                ${isFarmRole ? `<li><a class="dropdown-item py-2 fw-semibold" href="/reports.html">📄 <span data-i18n="nav_reports">PDF Reports & Backup</span></a></li>` : ""}
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'breeds' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/breeds.html">
                                🐔 <span data-i18n="nav_breeds">Indian Breeds</span>
                            </a>
                        </li>
                        ${canManageUsers ? `
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'users' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/users.html">
                                👥 <span data-i18n="nav_users">Staff & Users</span>
                            </a>
                        </li>` : ""}
                        <li class="nav-item">
                            <a class="nav-link text-white fw-semibold ${activePage === 'settings' ? 'bg-white bg-opacity-25 rounded-3 px-3' : 'px-2'}" href="/settings.html">
                                ⚙️ <span data-i18n="nav_settings">Settings</span>
                            </a>
                        </li>
                    </ul>
                    <div class="d-flex flex-wrap align-items-center gap-2">
                        <select class="form-select form-select-sm lang-selector-dropdown fw-semibold" style="width:auto; min-height:38px;"
                                onchange="PoultryApp.changeLanguage(this.value, true)">
                            ${this.buildLanguageOptionsHtml()}
                        </select>
                        <span class="badge bg-warning text-dark px-3 py-2 rounded-pill fw-bold">
                            👤 ${this.escapeHtml(this.currentUser.fullName)} (${this.escapeHtml(this.currentUser.roleDisplayName)})
                        </span>
                        <button class="btn btn-sm btn-light fw-bold px-3 py-2 rounded-3" onclick="PoultryApp.logout()">
                            🚪 <span data-i18n="nav_logout">Logout</span>
                        </button>
                    </div>
                </div>
            </div>
        </nav>`;
        applyPageTranslations();
    },

    async logout() {
        try {
            await this.apiFetch("/api/auth/logout", { method: "POST" });
        } finally {
            window.location.href = "/login.html";
        }
    },

    speakText(textOrKey) {
        if (!("speechSynthesis" in window)) {
            this.showToast("Audio playback is not supported in this browser.", "warning");
            return;
        }
        window.speechSynthesis.cancel();
        const message = POULTRY_I18N.translations.en[textOrKey] ? t(textOrKey) : textOrKey;
        const utterance = new SpeechSynthesisUtterance(message);
        const lang = getCurrentLang();
        utterance.lang = POULTRY_I18N.speechLocales[lang] || "en-IN";
        utterance.rate = 0.95;
        window.speechSynthesis.speak(utterance);
    },

    startVoiceInput(btnElement, targetInputId) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            this.showToast("Voice input requires Google Chrome or Microsoft Edge.", "warning");
            return;
        }
        const target = document.getElementById(targetInputId);
        if (!target) return;

        const recognition = new SpeechRecognition();
        const lang = getCurrentLang();
        recognition.lang = POULTRY_I18N.speechLocales[lang] || "en-IN";
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;

        btnElement.classList.add("listening");
        recognition.onresult = (event) => {
            const spoken = event.results[0][0].transcript;
            target.value = spoken;
            target.dispatchEvent(new Event("input", { bubbles: true }));
            this.showToast("🎤 Voice captured: " + spoken, "success");
        };
        recognition.onerror = () => {
            this.showToast("Could not hear clearly. Please try speaking again.", "warning");
        };
        recognition.onend = () => {
            btnElement.classList.remove("listening");
        };
        recognition.start();
    },

    bindOfflineDraft(formId, storageKey) {
        const form = document.getElementById(formId);
        if (!form) return;

        const savedJson = localStorage.getItem(storageKey);
        if (savedJson) {
            try {
                const draft = JSON.parse(savedJson);
                Object.entries(draft).forEach(([name, val]) => {
                    const input = form.elements.namedItem(name);
                    if (input && input.type !== "password") {
                        input.value = val;
                    }
                });
                const banner = document.getElementById(formId + "DraftBanner");
                if (banner) banner.classList.remove("d-none");
            } catch (e) {
                localStorage.removeItem(storageKey);
            }
        }

        form.addEventListener("input", () => {
            const data = {};
            Array.from(form.elements).forEach(el => {
                if (el.name && el.type !== "password" && el.type !== "submit") {
                    data[el.name] = el.value;
                }
            });
            localStorage.setItem(storageKey, JSON.stringify(data));
        });
    },

    clearOfflineDraft(formId, storageKey) {
        localStorage.removeItem(storageKey);
        const banner = document.getElementById(formId + "DraftBanner");
        if (banner) banner.classList.add("d-none");
    },

    showToast(message, variant = "success") {
        let container = document.getElementById("globalToastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "globalToastContainer";
            document.body.appendChild(container);
        }
        const bgClass = variant === "danger" ? "alert-danger" : (variant === "warning" ? "alert-warning" : "alert-success");
        const icon = variant === "danger" ? "⚠️" : (variant === "warning" ? "🔔" : "✅");
        const alertEl = document.createElement("div");
        alertEl.className = `alert ${bgClass} shadow-lg border-2 d-flex align-items-center justify-content-between py-3 px-3 mb-2 rounded-4 fw-semibold`;
        alertEl.innerHTML = `<span>${icon} ${this.escapeHtml(message)}</span>
            <button type="button" class="btn-close ms-2" onclick="this.parentElement.remove()"></button>`;
        container.appendChild(alertEl);
        setTimeout(() => { if (alertEl.parentElement) alertEl.remove(); }, 4800);
    },

    escapeHtml(str) {
        if (str === null || str === undefined) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }
};
