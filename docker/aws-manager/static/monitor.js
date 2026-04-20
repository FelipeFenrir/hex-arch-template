let queueStates = {}
let selectedQueueName = null
let activeTab = "monitor"
const notifySound = new Audio("/static/notify.mp3")
notifySound.volume = 0.5
let autoRefreshHandle = null

function reapplyTheme() {
    if (typeof applyTheme === "function") {
        applyTheme(localStorage.getItem("theme") === "dark")
    }
}

function prettyJson(value) {
    try {
        if (typeof value === "string") {
            return JSON.stringify(JSON.parse(value), null, 2)
        }
        return JSON.stringify(value ?? {}, null, 2)
    } catch {
        return String(value ?? "")
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
    url.pathname = "/monitor"
    url.searchParams.set("tab", tab)
    window.history.replaceState({}, "", url.toString())
}

function setActiveTab(tab, updateUrl = true) {
    const allowedTabs = new Set(["monitor", "create", "send"])
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

function showMessageDetails(queueName, message) {
    const panel = document.getElementById("messageDetailsPanel")
    document.getElementById("detailsQueue").textContent = `Fila: ${queueName} | MessageId: ${message.MessageId || "-"}`
    document.getElementById("detailsBody").textContent = prettyJson(message.Body)
    document.getElementById("detailsHeaders").textContent = prettyJson(message.MessageAttributes || {})
    document.getElementById("detailsAttributes").textContent = prettyJson(message.Attributes || {})
    panel.classList.remove("hidden")
}

function applyAutoRefresh() {
    const intervalEl = document.getElementById("autoRefreshInterval")
    const statusEl = document.getElementById("autoRefreshStatus")
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
        refreshSelectedQueue()
    }, intervalMs)
}

function refreshSelectedQueue() {
    if (!selectedQueueName || !queueStates[selectedQueueName]) return
    loadMessagesForSelected(true)
}

async function deleteQueueFromSqs(name, url) {
    const confirmed = window.confirm(`Deseja deletar a fila '${name}' no SQS?`)
    if (!confirmed) return

    const res = await fetch("/monitor/queues/sqs", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, url })
    })

    if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        if (typeof showToast === "function") {
            showToast(`Erro ao deletar fila: ${data.error || "falha desconhecida"}`, "error")
        } else {
            console.error(`Erro ao deletar fila: ${data.error || "falha desconhecida"}`)
        }
    }

    await loadQueues()
}

function renderMonitoredQueueList() {
    const container = document.getElementById("monitoredQueueList")
    container.innerHTML = ""

    const names = Object.keys(queueStates)
    if (!names.length) {
        container.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Nenhuma fila encontrada no emulator.</p>'
        reapplyTheme()
        return
    }

    names.forEach((name) => {
        const isActive = selectedQueueName === name
        const btn = document.createElement("button")
        btn.className = `w-full text-left border rounded p-3 ${isActive ? "bg-blue-600 text-white border-blue-600" : "bg-gray-50 theme-surface"}`
        btn.innerHTML = `<p class="font-semibold">${escapeHtml(name)}</p><p class="text-xs opacity-80">${escapeHtml(queueStates[name].url)}</p>`
        btn.onclick = () => setSelectedQueue(name)
        container.appendChild(btn)
    })
}

function setSelectedQueue(name) {
    if (!queueStates[name]) return
    selectedQueueName = name
    renderMonitoredQueueList()
    syncSelectedPanel()
    loadMessagesForSelected(true)
}

function syncSelectedPanel() {
    const hasSelection = !!(selectedQueueName && queueStates[selectedQueueName])
    const titleEl = document.getElementById("selectedQueueTitle")
    const urlEl = document.getElementById("selectedQueueUrl")
    const statsEl = document.getElementById("selectedQueueStats")
    const searchEl = document.getElementById("selectedSearch")
    const headerKeyEl = document.getElementById("selectedHeaderKey")
    const headerValueEl = document.getElementById("selectedHeaderValue")
    const timelineEl = document.getElementById("selectedTimeline")
    const refreshBtn = document.getElementById("selectedRefreshBtn")
    const deleteBtn = document.getElementById("selectedDeleteBtn")

    if (!hasSelection) {
        titleEl.textContent = "Selecione uma fila"
        urlEl.textContent = "-"
        statsEl.textContent = "Visiveis: - | Em processamento: - | Delayed: -"
        searchEl.value = ""
        headerKeyEl.value = ""
        headerValueEl.value = ""
        timelineEl.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Escolha uma fila para ver detalhes e mensagens.</p>'
    } else {
        const state = queueStates[selectedQueueName]
        titleEl.textContent = selectedQueueName
        urlEl.textContent = state.url
        searchEl.value = state.search
        headerKeyEl.value = state.headerKey
        headerValueEl.value = state.headerValue
    }

    searchEl.disabled = !hasSelection
    headerKeyEl.disabled = !hasSelection
    headerValueEl.disabled = !hasSelection
    refreshBtn.disabled = !hasSelection
    deleteBtn.disabled = !hasSelection

    reapplyTheme()
}

async function loadQueues(preferredSelection = null) {
    const errorEl = document.getElementById("queueLoadError")
    const previous = queueStates
    queueStates = {}
    errorEl.classList.add("hidden")
    errorEl.textContent = ""

    try {
        const res = await fetch("/monitor/queues")
        const data = await res.json()

        if (!res.ok) {
            throw new Error(data.error || "Falha ao listar filas")
        }

        data.queues.forEach((q) => {
            const prev = previous[q.name]
            queueStates[q.name] = {
                url: q.url,
                search: prev?.search || "",
                headerKey: prev?.headerKey || "",
                headerValue: prev?.headerValue || "",
                loading: false,
                seen: prev?.seen || new Set()
            }
        })

        if (preferredSelection && queueStates[preferredSelection]) {
            selectedQueueName = preferredSelection
        } else if (selectedQueueName && queueStates[selectedQueueName]) {
            selectedQueueName = selectedQueueName
        } else {
            selectedQueueName = Object.keys(queueStates)[0] || null
        }

        renderMonitoredQueueList()
        syncSelectedPanel()
        if (selectedQueueName) {
            await loadMessagesForSelected(true)
        }

        reapplyTheme()
    } catch (e) {
        selectedQueueName = null
        renderMonitoredQueueList()
        syncSelectedPanel()
        errorEl.textContent = `Erro ao carregar filas: ${e.message || e}`
        errorEl.classList.remove("hidden")
    }
}

async function loadQueueStats(queueName) {
    const state = queueStates[queueName]
    if (!state) return

    try {
        const res = await fetch(`/monitor/queue-stats?queue=${encodeURIComponent(state.url)}`)
        const data = await res.json()
        if (!res.ok) throw new Error(data.error || "Falha ao carregar estatisticas")

        if (selectedQueueName === queueName) {
            document.getElementById("selectedQueueStats").textContent = `Visiveis: ${data.visible} | Em processamento: ${data.inflight} | Delayed: ${data.delayed}`
        }
    } catch {
        if (selectedQueueName === queueName) {
            document.getElementById("selectedQueueStats").textContent = "Estatisticas indisponiveis"
        }
    }
}

async function loadMessagesForSelected(reset = false) {
    if (!selectedQueueName) return
    await loadMessagesForQueue(selectedQueueName, reset)
}

async function loadMessagesForQueue(queueName, reset = false) {
    const state = queueStates[queueName]
    if (!state || state.loading) return

    state.loading = true
    const loadingEl = document.getElementById("selectedLoading")
    loadingEl.classList.remove("hidden")

    const params = new URLSearchParams({
        queue: state.url,
        q: state.search,
        headerKey: state.headerKey,
        headerValue: state.headerValue
    })

    const timeline = document.getElementById("selectedTimeline")
    if (reset) timeline.innerHTML = ""

    try {
        const res = await fetch(`/monitor/messages?${params.toString()}`)
        const messages = await res.json()

        if (!res.ok) {
            throw new Error(messages.error || "Falha ao carregar mensagens")
        }

        if (!messages.length) {
            timeline.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Nenhuma mensagem encontrada para os filtros atuais.</p>'
        }

        messages.forEach((m) => {
            const div = document.createElement("div")
            const isNew = !state.seen.has(m.MessageId)
            state.seen.add(m.MessageId)

            div.className = "p-3 border rounded bg-gray-50 theme-surface"
            if (isNew) {
                div.classList.add("new-message")
                notifySound.play().catch(() => {})
            }

            const bodyPreview = escapeHtml(prettyJson(m.Body))
            const headersPreview = escapeHtml(prettyJson(m.MessageAttributes || {}))
            div.innerHTML = `
                <p class="text-sm text-gray-500 theme-muted">ID: ${escapeHtml(m.MessageId)}</p>
                <pre class="text-xs mt-2 theme-code p-2 rounded">${bodyPreview}</pre>
                <p class="text-xs mt-2 font-semibold">Headers</p>
                <pre class="text-xs theme-code p-2 rounded">${headersPreview}</pre>

                <div class="flex gap-2 mt-2">
                    <button class="bg-blue-600 text-white px-2 py-1 rounded btn-read">Ver detalhes</button>
                    <button class="bg-indigo-600 text-white px-2 py-1 rounded btn-lock">Marcar como lida</button>
                    <button class="bg-red-600 text-white px-2 py-1 rounded btn-delete">Excluir mensagem</button>
                </div>
            `

            div.querySelector(".btn-read").onclick = () => showMessageDetails(queueName, m)
            div.querySelector(".btn-lock").onclick = () => readMessage(queueName, m.ReceiptHandle)
            div.querySelector(".btn-delete").onclick = () => deleteMessage(queueName, m.ReceiptHandle)

            timeline.appendChild(div)
        })

        await loadQueueStats(queueName)
        reapplyTheme()
    } catch (e) {
        timeline.innerHTML = `<p class="text-sm text-red-600">Erro ao carregar mensagens: ${escapeHtml(e.message || e)}</p>`
    } finally {
        loadingEl.classList.add("hidden")
        state.loading = false
    }
}

async function readMessage(queueName, receipt) {
    await fetch("/monitor/read", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            queue: queueStates[queueName].url,
            receipt
        })
    })
}

async function deleteMessage(queueName, receipt) {
    await fetch("/monitor/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            queue: queueStates[queueName].url,
            receipt
        })
    })
    loadMessagesForSelected(true)
}

document.addEventListener("DOMContentLoaded", () => {
    setActiveTab(getInitialTab())
    loadQueues()

    document.querySelectorAll("[data-tab-button]").forEach((button) => {
        button.onclick = () => setActiveTab(button.dataset.tabButton)
    })

    document.getElementById("refreshQueuesBtn").onclick = async () => {
        await loadQueues(selectedQueueName)
    }

    document.getElementById("autoRefreshInterval").onchange = () => {
        applyAutoRefresh()
    }

    document.getElementById("selectedSearch").oninput = (e) => {
        if (!selectedQueueName) return
        queueStates[selectedQueueName].search = e.target.value
        loadMessagesForSelected(true)
    }

    document.getElementById("selectedHeaderKey").oninput = (e) => {
        if (!selectedQueueName) return
        queueStates[selectedQueueName].headerKey = e.target.value
        loadMessagesForSelected(true)
    }

    document.getElementById("selectedHeaderValue").oninput = (e) => {
        if (!selectedQueueName) return
        queueStates[selectedQueueName].headerValue = e.target.value
        loadMessagesForSelected(true)
    }

    document.getElementById("selectedRefreshBtn").onclick = () => {
        loadMessagesForSelected(true)
    }


    document.getElementById("selectedDeleteBtn").onclick = () => {
        if (!selectedQueueName) return
        const state = queueStates[selectedQueueName]
        deleteQueueFromSqs(selectedQueueName, state.url)
    }

    applyAutoRefresh()

    document.getElementById("closeDetails").onclick = () => {
        document.getElementById("messageDetailsPanel").classList.add("hidden")
    }
})