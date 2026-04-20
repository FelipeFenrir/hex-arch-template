function applyClassSet(element, dark, darkClasses, lightClasses) {
    if (!element) return

    element.classList.remove(...darkClasses, ...lightClasses)
    element.classList.add(...(dark ? darkClasses : lightClasses))
}

function styleTabButton(button, dark) {
    const isActive = button.dataset.active === "true"

    button.classList.remove(
        "bg-blue-600", "border-blue-600", "text-white",
        "bg-blue-500", "border-blue-500",
        "bg-white", "border-gray-300", "text-gray-900",
        "bg-gray-700", "border-gray-600", "text-gray-100"
    )

    if (isActive) {
        button.classList.add(dark ? "bg-blue-500" : "bg-blue-600", dark ? "border-blue-500" : "border-blue-600", "text-white")
        return
    }

    if (dark) {
        button.classList.add("bg-gray-700", "border-gray-600", "text-gray-100")
    } else {
        button.classList.add("bg-white", "border-gray-300", "text-gray-900")
    }
}

function applyTheme(dark) {
    const body = document.getElementById("pageBody")
    const card = document.getElementById("mainCard")
    const navbar = document.getElementById("navbar")
    const btn = document.getElementById("themeToggle")

    if (body) {
        body.className = dark
            ? "bg-gray-900 text-gray-100 transition-colors duration-500"
            : "bg-gray-100 text-gray-900 transition-colors duration-500"
    }

    applyClassSet(card, dark, ["bg-gray-800", "text-gray-100"], ["bg-white", "text-gray-900"])
    applyClassSet(navbar, dark, ["bg-gray-800", "text-gray-100", "border-b", "border-gray-700"], ["bg-white", "text-gray-900", "border-b", "border-gray-200"])

    if (btn) {
        btn.innerHTML = dark ? "☀️" : "🌙"
        btn.className = dark
            ? "px-3 py-1 rounded bg-gray-700 text-gray-100 transition-colors duration-500"
            : "px-3 py-1 rounded bg-gray-300 text-gray-900 transition-colors duration-500"
    }

    document.querySelectorAll(".theme-field").forEach(el => {
        applyClassSet(el, dark, ["bg-gray-700", "text-gray-100", "border-gray-600"], ["bg-gray-100", "text-gray-900", "border-gray-300"])
    })

    document.querySelectorAll(".theme-surface").forEach(el => {
        applyClassSet(el, dark, ["bg-gray-800", "text-gray-100", "border-gray-700"], ["bg-gray-50", "text-gray-900", "border-gray-200"])
    })

    document.querySelectorAll(".theme-card").forEach(el => {
        applyClassSet(el, dark, ["bg-gray-800", "text-gray-100", "border-gray-700"], ["bg-gray-50", "text-gray-900", "border-gray-200"])
    })

    document.querySelectorAll(".theme-muted").forEach(el => {
        applyClassSet(el, dark, ["text-gray-400"], ["text-gray-600"])
    })

    document.querySelectorAll(".theme-tab").forEach(el => {
        styleTabButton(el, dark)
    })

    document.querySelectorAll(".theme-code").forEach(el => {
        el.classList.remove("bg-white", "bg-gray-100", "text-gray-900")
        el.classList.add("bg-gray-900", "text-gray-100")
    })
}

function initTheme() {
    let dark = localStorage.getItem("theme") === "dark"
    applyTheme(dark)

    document.addEventListener("click", (e) => {
        if (e.target.id === "themeToggle") {
            dark = !dark
            localStorage.setItem("theme", dark ? "dark" : "light")
            applyTheme(dark)
        }
    })
}