// ------------------------------
// MENU SUPERIOR FIXO
// ------------------------------

function injectNavbar() {
    const isHomePage = window.location.pathname === "/"
    const ministackIndicatorClass = isHomePage ? "flex" : "hidden"

    const navbar = document.createElement("div")
    navbar.className =
        "fixed top-0 left-0 right-0 h-14 shadow flex items-center px-6 justify-between z-50 transition-colors duration-500"
    navbar.id = "navbar"

    navbar.innerHTML = `
        <div class="flex gap-6 font-semibold">
            <a href="/" class="hover:underline">🏠 Home</a>
            <a href="/monitor" class="hover:underline">📨 SQS</a>
            <a href="/sns" class="hover:underline">📣 SNS</a>
            <a href="/s3" class="hover:underline">🪣 S3</a>
        </div>

        <div class="flex items-center gap-3">
            <div id="navbarMinistackStatus" class="${ministackIndicatorClass} items-center gap-2">
                <span id="navbarMinistackStatusIcon" class="status-dot status-dot-lg status-unknown" title="Status da MiniStack"></span>
                <span id="navbarMinistackStatusText" class="text-xs font-medium theme-muted">MiniStack: verificando...</span>
            </div>

            <button id="themeToggle"
                class="px-3 py-1 rounded bg-gray-300 dark:bg-gray-700 transition-colors duration-500">
                🌙
            </button>

            <a href="/apis" class="hover:underline font-semibold">🧭 APIs</a>
        </div>
    `

    document.body.appendChild(navbar)

    // empurra o conteúdo para baixo
    document.body.style.paddingTop = "4rem"
}

function injectToastContainer() {
    if (document.getElementById("toastContainer")) return

    const container = document.createElement("div")
    container.id = "toastContainer"
    container.className = "fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm"
    document.body.appendChild(container)
}

function showToast(message, type = "info", durationMs = 3000) {
    injectToastContainer()
    const container = document.getElementById("toastContainer")
    if (!container) return

    const toast = document.createElement("div")
    const toastTypeClass = type === "error" ? "toast-error" : type === "success" ? "toast-success" : "toast-info"
    toast.className = `toast-card ${toastTypeClass}`
    toast.setAttribute("role", "status")
    toast.textContent = String(message || "")

    container.appendChild(toast)

    setTimeout(() => {
        toast.classList.add("toast-hide")
        setTimeout(() => toast.remove(), 250)
    }, durationMs)
}

window.showToast = showToast

// ------------------------------
// ATALHOS DE TECLADO
// ------------------------------

function initShortcuts() {
    document.addEventListener("keydown", (e) => {
        if (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA") return

        if (e.key.toLowerCase() === "m") {
            window.location.href = "/monitor"
        }

        if (e.key.toLowerCase() === "n") {
            window.location.href = "/sns"
        }

        if (e.key.toLowerCase() === "b") {
            window.location.href = "/s3"
        }

        if (e.key.toLowerCase() === "h") {
            window.location.href = "/"
        }

        if (e.key.toLowerCase() === "a") {
            window.location.href = "/apis"
        }

        if (e.key.toLowerCase() === "t") {
            document.getElementById("themeToggle")?.click()
        }
    })
}

// ------------------------------
// INICIALIZAÇÃO GERAL
// ------------------------------

function initLayout() {
    injectNavbar()
    injectToastContainer()
    initShortcuts()
}