let topicStates = {}
let selectedTopicArn = null
let activeTab = "monitor"
let autoRefreshHandle = null

function reapplyTheme() {
    if (typeof applyTheme === "function") {
        applyTheme(localStorage.getItem("theme") === "dark")
    }
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;")
}

function updateTabUrl(tab) {
    const url = new URL(window.location.href)
    url.pathname = "/sns"
    url.searchParams.set("tab", tab)
    window.history.replaceState({}, "", url.toString())
}

function setActiveTab(tab, updateUrl = true) {
    const allowedTabs = new Set(["monitor", "create", "publish", "subscribe"])
    activeTab = allowedTabs.has(tab) ? tab : "monitor"

    document.querySelectorAll("[data-tab-button]").forEach((button) => {
        button.dataset.active = button.dataset.tabButton === activeTab ? "true" : "false"
    })

    document.querySelectorAll("[data-tab-panel]").forEach((panel) => {
        panel.classList.toggle("hidden", panel.dataset.tabPanel !== activeTab)
    })

    if (updateUrl) {
        updateTabUrl(activeTab)
    }

    reapplyTheme()
}

function getInitialTab() {
    const urlTab = new URLSearchParams(window.location.search).get("tab")
    const bodyTab = document.getElementById("pageBody")?.dataset.activeTab
    return urlTab || bodyTab || "monitor"
}

function applyAutoRefresh() {
    const intervalEl = document.getElementById("snsAutoRefreshInterval")
    const statusEl = document.getElementById("snsAutoRefreshStatus")
    const intervalMs = Number(intervalEl.value || 0)

    if (autoRefreshHandle) {
        clearInterval(autoRefreshHandle)
        autoRefreshHandle = null
    }

    if (!intervalMs) {
        statusEl.textContent = "Auto-refresh desativado"
        return
    }

    statusEl.textContent = `Auto-refresh ativo a cada ${Math.round(intervalMs / 1000)}s`
    autoRefreshHandle = setInterval(() => {
        refreshSelectedTopic()
    }, intervalMs)
}

function refreshSelectedTopic() {
    if (!selectedTopicArn || !topicStates[selectedTopicArn]) return
    loadSubscriptionsForSelected(true)
}

function renderTopicList() {
    const container = document.getElementById("topicList")
    container.innerHTML = ""

    const arns = Object.keys(topicStates)
    if (!arns.length) {
        container.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Nenhum topico encontrado no emulator.</p>'
        reapplyTheme()
        return
    }

    arns.forEach((topicArn) => {
        const topic = topicStates[topicArn]
        const isActive = selectedTopicArn === topicArn

        const btn = document.createElement("button")
        btn.className = `w-full text-left border rounded p-3 ${isActive ? "bg-blue-600 text-white border-blue-600" : "bg-gray-50 theme-surface"}`
        btn.innerHTML = `<p class="font-semibold">${escapeHtml(topic.name)}</p><p class="text-xs opacity-80">${escapeHtml(topic.arn)}</p>`
        btn.onclick = () => setSelectedTopic(topicArn)

        container.appendChild(btn)
    })
}

function setSelectedTopic(topicArn) {
    if (!topicStates[topicArn]) return
    selectedTopicArn = topicArn
    renderTopicList()
    syncSelectedTopicPanel()
    loadSubscriptionsForSelected(true)
}

function syncSelectedTopicPanel() {
    const hasSelection = !!(selectedTopicArn && topicStates[selectedTopicArn])
    const titleEl = document.getElementById("selectedTopicTitle")
    const arnEl = document.getElementById("selectedTopicArn")
    const subscriptionsEl = document.getElementById("selectedTopicSubscriptions")
    const refreshBtn = document.getElementById("selectedTopicRefreshBtn")
    const deleteBtn = document.getElementById("selectedTopicDeleteBtn")

    if (!hasSelection) {
        titleEl.textContent = "Selecione um topico"
        arnEl.textContent = "-"
        subscriptionsEl.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Escolha um topico para listar as assinaturas.</p>'
    } else {
        const topic = topicStates[selectedTopicArn]
        titleEl.textContent = topic.name
        arnEl.textContent = topic.arn
    }

    refreshBtn.disabled = !hasSelection
    deleteBtn.disabled = !hasSelection
    reapplyTheme()
}

async function loadTopics(preferredSelection = null) {
    const errorEl = document.getElementById("topicLoadError")
    const previous = topicStates
    topicStates = {}
    errorEl.classList.add("hidden")
    errorEl.textContent = ""

    try {
        const res = await fetch("/sns/topics")
        const data = await res.json()

        if (!res.ok) {
            throw new Error(data.error || "Falha ao listar topicos")
        }

        data.topics.forEach((topic) => {
            const prev = previous[topic.arn]
            topicStates[topic.arn] = {
                name: topic.name,
                arn: topic.arn,
                subscriptions: prev?.subscriptions || [],
                loading: false,
            }
        })

        if (preferredSelection && topicStates[preferredSelection]) {
            selectedTopicArn = preferredSelection
        } else if (selectedTopicArn && topicStates[selectedTopicArn]) {
            selectedTopicArn = selectedTopicArn
        } else {
            selectedTopicArn = Object.keys(topicStates)[0] || null
        }

        renderTopicList()
        syncSelectedTopicPanel()
        if (selectedTopicArn) {
            await loadSubscriptionsForSelected(true)
        }

        reapplyTheme()
    } catch (e) {
        selectedTopicArn = null
        renderTopicList()
        syncSelectedTopicPanel()
        errorEl.textContent = `Erro ao carregar topicos: ${e.message || e}`
        errorEl.classList.remove("hidden")
    }
}

async function loadSubscriptionsForSelected(reset = false) {
    if (!selectedTopicArn || !topicStates[selectedTopicArn]) return

    const topic = topicStates[selectedTopicArn]
    if (topic.loading) return

    const loadingEl = document.getElementById("topicLoading")
    const subscriptionsEl = document.getElementById("selectedTopicSubscriptions")

    topic.loading = true
    loadingEl.classList.remove("hidden")
    if (reset) subscriptionsEl.innerHTML = ""

    try {
        const res = await fetch(`/sns/subscriptions?topicArn=${encodeURIComponent(topic.arn)}`)
        const data = await res.json()

        if (!res.ok) {
            throw new Error(data.error || "Falha ao listar assinaturas")
        }

        topic.subscriptions = data.subscriptions || []

        if (!topic.subscriptions.length) {
            subscriptionsEl.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Este topico ainda nao possui assinaturas.</p>'
            reapplyTheme()
            return
        }

        subscriptionsEl.innerHTML = ""
        topic.subscriptions.forEach((sub) => {
            const div = document.createElement("div")
            div.className = "p-3 border rounded bg-gray-50 theme-surface"
            div.innerHTML = `
                <p class="text-sm"><span class="font-semibold">Protocolo:</span> ${escapeHtml(sub.protocol)}</p>
                <p class="text-sm"><span class="font-semibold">Endpoint:</span> ${escapeHtml(sub.endpoint)}</p>
                <p class="text-xs text-gray-500 theme-muted">SubscriptionArn: ${escapeHtml(sub.subscriptionArn)}</p>
            `
            subscriptionsEl.appendChild(div)
        })

        reapplyTheme()
    } catch (e) {
        subscriptionsEl.innerHTML = `<p class="text-sm text-red-600">Erro ao carregar assinaturas: ${escapeHtml(e.message || e)}</p>`
    } finally {
        loadingEl.classList.add("hidden")
        topic.loading = false
    }
}

async function deleteSelectedTopic() {
    if (!selectedTopicArn || !topicStates[selectedTopicArn]) return

    const topic = topicStates[selectedTopicArn]
    const confirmed = window.confirm(`Deseja deletar o topico '${topic.name}'?`)
    if (!confirmed) return

    const res = await fetch("/sns/topics", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ topicArn: topic.arn }),
    })

    if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        if (typeof showToast === "function") {
            showToast(`Erro ao deletar topico: ${data.error || "falha desconhecida"}`, "error")
        } else {
            console.error(`Erro ao deletar topico: ${data.error || "falha desconhecida"}`)
        }
    }

    await loadTopics()
}

document.addEventListener("DOMContentLoaded", () => {
    setActiveTab(getInitialTab())
    loadTopics()

    document.querySelectorAll("[data-tab-button]").forEach((button) => {
        button.onclick = () => setActiveTab(button.dataset.tabButton)
    })

    document.getElementById("refreshTopicsBtn").onclick = async () => {
        await loadTopics(selectedTopicArn)
    }

    document.getElementById("selectedTopicRefreshBtn").onclick = () => {
        loadSubscriptionsForSelected(true)
    }

    document.getElementById("selectedTopicDeleteBtn").onclick = () => {
        deleteSelectedTopic()
    }

    document.getElementById("snsAutoRefreshInterval").onchange = () => {
        applyAutoRefresh()
    }

    applyAutoRefresh()
})

