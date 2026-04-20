let bucketStates = {}
let selectedBucketName = null
let activeTab = "monitor"
let autoRefreshHandle = null

function reapplyTheme() {
    if (typeof applyTheme === "function") {
        applyTheme(localStorage.getItem("theme") === "dark")
    }
}

function prettyJson(value) {
    try {
        return JSON.stringify(value ?? {}, null, 2)
    } catch {
        return "{}"
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

function formatBytes(bytes) {
    const value = Number(bytes || 0)
    if (value <= 0) return "0 B"

    const units = ["B", "KB", "MB", "GB", "TB"]
    let index = 0
    let result = value

    while (result >= 1024 && index < units.length - 1) {
        result /= 1024
        index += 1
    }

    return `${result.toFixed(index === 0 ? 0 : 2)} ${units[index]}`
}

function updateTabUrl(tab) {
    const url = new URL(window.location.href)
    url.pathname = "/s3"
    url.searchParams.set("tab", tab)
    window.history.replaceState({}, "", url.toString())
}

function setActiveTab(tab, updateUrl = true) {
    const allowedTabs = new Set(["monitor", "create", "settings"])
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
    const intervalEl = document.getElementById("s3AutoRefreshInterval")
    const statusEl = document.getElementById("s3AutoRefreshStatus")
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
        refreshSelectedBucket()
    }, intervalMs)
}

function refreshSelectedBucket() {
    if (!selectedBucketName || !bucketStates[selectedBucketName]) return
    loadSelectedBucketDetails(true)
}

function renderBucketList() {
    const container = document.getElementById("bucketList")
    container.innerHTML = ""

    const names = Object.keys(bucketStates)
    if (!names.length) {
        container.innerHTML = '<p class="text-sm text-gray-500 theme-muted">Nenhum bucket encontrado no emulator.</p>'
        reapplyTheme()
        return
    }

    names.forEach((bucketName) => {
        const bucket = bucketStates[bucketName]
        const isActive = selectedBucketName === bucketName

        const btn = document.createElement("button")
        btn.className = `w-full text-left border rounded p-3 ${isActive ? "bg-blue-600 text-white border-blue-600" : "bg-gray-50 theme-surface"}`
        btn.innerHTML = `<p class="font-semibold">${escapeHtml(bucket.name)}</p><p class="text-xs opacity-80">Criado em: ${escapeHtml(bucket.createdAt || "-")}</p>`
        btn.onclick = () => setSelectedBucket(bucketName)

        container.appendChild(btn)
    })
}

function setSelectedBucket(bucketName) {
    if (!bucketStates[bucketName]) return
    selectedBucketName = bucketName
    renderBucketList()
    syncSelectedBucketPanel()
    loadSelectedBucketDetails(true)
    fillSettingsFormWithSelection()
}

function syncSelectedBucketPanel() {
    const hasSelection = !!(selectedBucketName && bucketStates[selectedBucketName])
    const titleEl = document.getElementById("selectedBucketTitle")
    const metaEl = document.getElementById("selectedBucketMeta")
    const versioningEl = document.getElementById("selectedBucketVersioning")
    const statsEl = document.getElementById("selectedBucketStats")
    const tagsEl = document.getElementById("selectedBucketTags")
    const refreshBtn = document.getElementById("selectedBucketRefreshBtn")
    const deleteBtn = document.getElementById("selectedBucketDeleteBtn")

    if (!hasSelection) {
        titleEl.textContent = "Selecione um bucket"
        metaEl.textContent = "-"
        versioningEl.textContent = "Versioning: -"
        statsEl.textContent = "Objetos: - | Tamanho total: -"
        tagsEl.textContent = "{}"
    } else {
        const state = bucketStates[selectedBucketName]
        titleEl.textContent = state.name
        metaEl.textContent = `Criado em: ${state.createdAt || "-"} | Regiao: ${state.location || "-"}`
        versioningEl.textContent = `Versioning: ${state.versioningStatus || "Disabled"}`
        statsEl.textContent = `Objetos: ${state.objectCount ?? "-"} | Tamanho total: ${formatBytes(state.totalSizeBytes)}`
        tagsEl.textContent = prettyJson(state.tags || {})
    }

    refreshBtn.disabled = !hasSelection
    deleteBtn.disabled = !hasSelection
    reapplyTheme()
}

function fillSettingsFormWithSelection() {
    if (!selectedBucketName || !bucketStates[selectedBucketName]) return

    const state = bucketStates[selectedBucketName]
    const bucketField = document.getElementById("settingsBucketName")
    const versioningField = document.getElementById("settingsVersioning")
    const tagsField = document.getElementById("settingsTags")

    if (bucketField) bucketField.value = state.name
    if (versioningField) versioningField.value = state.versioningStatus || "Disabled"
    if (tagsField) tagsField.value = prettyJson(state.tags || {})
}

async function loadBuckets(preferredSelection = null) {
    const errorEl = document.getElementById("bucketLoadError")
    const previous = bucketStates
    bucketStates = {}
    errorEl.classList.add("hidden")
    errorEl.textContent = ""

    try {
        const res = await fetch("/s3/buckets")
        const data = await res.json()

        if (!res.ok) {
            throw new Error(data.error || "Falha ao listar buckets")
        }

        data.buckets.forEach((bucket) => {
            const prev = previous[bucket.name]
            bucketStates[bucket.name] = {
                name: bucket.name,
                createdAt: bucket.createdAt,
                location: prev?.location || null,
                versioningStatus: prev?.versioningStatus || "Disabled",
                tags: prev?.tags || {},
                objectCount: prev?.objectCount,
                totalSizeBytes: prev?.totalSizeBytes,
                loading: false,
            }
        })

        if (preferredSelection && bucketStates[preferredSelection]) {
            selectedBucketName = preferredSelection
        } else if (selectedBucketName && bucketStates[selectedBucketName]) {
            selectedBucketName = selectedBucketName
        } else {
            selectedBucketName = Object.keys(bucketStates)[0] || null
        }

        renderBucketList()
        syncSelectedBucketPanel()

        if (selectedBucketName) {
            await loadSelectedBucketDetails(true)
            fillSettingsFormWithSelection()
        }

        reapplyTheme()
    } catch (error) {
        selectedBucketName = null
        renderBucketList()
        syncSelectedBucketPanel()
        errorEl.textContent = `Erro ao carregar buckets: ${error.message || error}`
        errorEl.classList.remove("hidden")
    }
}

async function loadSelectedBucketDetails(forceRender = false) {
    if (!selectedBucketName || !bucketStates[selectedBucketName]) return

    const state = bucketStates[selectedBucketName]
    if (state.loading) return

    state.loading = true
    const loadingEl = document.getElementById("bucketLoading")
    loadingEl.classList.remove("hidden")

    try {
        const [detailsRes, statsRes] = await Promise.all([
            fetch(`/s3/buckets/details?name=${encodeURIComponent(state.name)}`),
            fetch(`/s3/buckets/stats?name=${encodeURIComponent(state.name)}`),
        ])

        const details = await detailsRes.json()
        const stats = await statsRes.json()

        if (!detailsRes.ok) {
            throw new Error(details.error || "Falha ao carregar detalhes do bucket")
        }

        if (!statsRes.ok) {
            throw new Error(stats.error || "Falha ao carregar estatisticas do bucket")
        }

        state.location = details.location || "us-east-1"
        state.versioningStatus = details.versioningStatus || "Disabled"
        state.tags = details.tags || {}
        state.objectCount = Number(stats.objectCount || 0)
        state.totalSizeBytes = Number(stats.totalSizeBytes || 0)

        if (forceRender) {
            syncSelectedBucketPanel()
            fillSettingsFormWithSelection()
        }
    } catch (error) {
        if (typeof showToast === "function") {
            showToast(`Erro ao carregar bucket: ${error.message || error}`, "error")
        }
    } finally {
        loadingEl.classList.add("hidden")
        state.loading = false
    }
}

async function deleteSelectedBucket() {
    if (!selectedBucketName || !bucketStates[selectedBucketName]) return

    const forceDelete = document.getElementById("selectedBucketForceDelete")?.checked || false
    const confirmed = window.confirm(`Deseja deletar o bucket '${selectedBucketName}'?`)
    if (!confirmed) return

    const res = await fetch("/s3/buckets", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            bucketName: selectedBucketName,
            forceDelete,
        }),
    })

    const data = await res.json().catch(() => ({}))

    if (!res.ok) {
        if (typeof showToast === "function") {
            showToast(`Erro ao deletar bucket: ${data.error || "falha desconhecida"}`, "error")
        }
        return
    }

    if (typeof showToast === "function") {
        showToast(`Bucket '${selectedBucketName}' deletado com sucesso.`, "success")
    }

    await loadBuckets()
}

document.addEventListener("DOMContentLoaded", () => {
    setActiveTab(getInitialTab())
    loadBuckets()

    document.querySelectorAll("[data-tab-button]").forEach((button) => {
        button.onclick = () => setActiveTab(button.dataset.tabButton)
    })

    document.getElementById("refreshBucketsBtn").onclick = async () => {
        await loadBuckets(selectedBucketName)
    }

    document.getElementById("selectedBucketRefreshBtn").onclick = () => {
        loadSelectedBucketDetails(true)
    }

    document.getElementById("selectedBucketDeleteBtn").onclick = () => {
        deleteSelectedBucket()
    }

    document.getElementById("s3AutoRefreshInterval").onchange = () => {
        applyAutoRefresh()
    }

    document.getElementById("settingsBucketName")?.addEventListener("focus", () => {
        if (selectedBucketName && !document.getElementById("settingsBucketName").value) {
            fillSettingsFormWithSelection()
        }
    })

    applyAutoRefresh()
})

