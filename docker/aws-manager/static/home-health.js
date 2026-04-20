const HEALTH_INTERVAL_KEY = "ministackHealthIntervalMinutes"
const INTERVAL_TO_MS = {
    1: 60 * 1000,
    5: 5 * 60 * 1000,
    15: 15 * 60 * 1000,
}

let homeHealthTimerId = null

const SERVICE_DOT_IDS = {
    sqs: "sqsCardStatusIcon",
    sns: "snsCardStatusIcon",
    s3: "s3CardStatusIcon",
    dynamodb: "dynamodbCardStatusIcon",
    lambda: "lambdaCardStatusIcon",
    cloudwatch: "cloudwatchCardStatusIcon",
    ecs: "ecsCardStatusIcon",
    ec2: "ec2CardStatusIcon",
    iam: "iamCardStatusIcon",
}

function setDotStatus(element, status) {
    if (!element) return

    element.classList.remove("status-online", "status-offline", "status-unknown")

    if (status === "online") {
        element.classList.add("status-online")
        return
    }

    if (status === "offline") {
        element.classList.add("status-offline")
        return
    }

    element.classList.add("status-unknown")
}

function updateLastCheckLabel(text) {
    const label = document.getElementById("homeMinistackLastCheck")
    if (label) {
        label.textContent = text
    }
}

function updateServiceCardIndicators(isMinistackOnline, services) {
    Object.entries(SERVICE_DOT_IDS).forEach(([serviceName, elementId]) => {
        const dot = document.getElementById(elementId)
        if (!dot) return

        if (!isMinistackOnline) {
            setDotStatus(dot, "offline")
            return
        }

        if (Object.prototype.hasOwnProperty.call(services, serviceName)) {
            setDotStatus(dot, services[serviceName] ? "online" : "offline")
            return
        }

        setDotStatus(dot, "unknown")
    })
}

function initNotImplementedServiceCards() {
    document.querySelectorAll('[data-service-implemented="false"]').forEach((card) => {
        card.addEventListener("click", (event) => {
            event.preventDefault()
            if (typeof showToast === "function") {
                showToast("Serviço não implementado", "info")
            } else {
                console.info("Serviço não implementado")
            }
        })
    })
}

function updateMinistackIndicators(payload) {
    const isOnline = Boolean(payload?.online)
    const services = payload?.services || {}

    const homeStatusDot = document.getElementById("homeMinistackStatusIcon")
    const navbarStatusDot = document.getElementById("navbarMinistackStatusIcon")
    const homeStatusText = document.getElementById("homeMinistackStatusText")
    const navbarStatusText = document.getElementById("navbarMinistackStatusText")

    setDotStatus(homeStatusDot, isOnline ? "online" : "offline")
    setDotStatus(navbarStatusDot, isOnline ? "online" : "offline")

    if (homeStatusText) {
        homeStatusText.textContent = isOnline ? "MiniStack: online" : "MiniStack: offline"
    }

    if (navbarStatusText) {
        navbarStatusText.textContent = isOnline ? "MiniStack online" : "MiniStack offline"
    }

    updateServiceCardIndicators(isOnline, services)

    const checkedAt = payload?.checked_at ? new Date(payload.checked_at) : new Date()
    updateLastCheckLabel(`Ultima verificacao: ${checkedAt.toLocaleString("pt-BR")}`)
}

function updateOfflineFromError(errorMessage) {
    updateMinistackIndicators({
        online: false,
        services: {
            sqs: false,
            sns: false,
            s3: false,
            dynamodb: false,
            lambda: false,
            cloudwatch: false,
            ecs: false,
            ec2: false,
            iam: false,
        },
        checked_at: new Date().toISOString(),
    })

    if (errorMessage) {
        updateLastCheckLabel(`Falha na verificacao: ${errorMessage}`)
    }
}

async function checkMinistackHealth() {
    try {
        const response = await fetch("/ministack/health", { cache: "no-store" })
        const payload = await response.json()

        if (!response.ok) {
            updateOfflineFromError(payload?.error || "MiniStack indisponivel")
            return
        }

        updateMinistackIndicators(payload)
    } catch (error) {
        updateOfflineFromError("Nao foi possivel consultar a MiniStack")
    }
}

function readSelectedIntervalMinutes() {
    const select = document.getElementById("healthIntervalSelect")
    const minutes = Number(select?.value || 5)
    return INTERVAL_TO_MS[minutes] ? minutes : 5
}

function applyStoredInterval() {
    const select = document.getElementById("healthIntervalSelect")
    if (!select) return 5

    const stored = Number(localStorage.getItem(HEALTH_INTERVAL_KEY) || 5)
    const minutes = INTERVAL_TO_MS[stored] ? stored : 5
    select.value = String(minutes)
    return minutes
}

function scheduleHealthPolling() {
    if (homeHealthTimerId) {
        clearInterval(homeHealthTimerId)
    }

    const minutes = readSelectedIntervalMinutes()
    localStorage.setItem(HEALTH_INTERVAL_KEY, String(minutes))

    homeHealthTimerId = setInterval(() => {
        checkMinistackHealth()
    }, INTERVAL_TO_MS[minutes])
}

function initHomeHealth() {
    if (window.location.pathname !== "/") return

    initNotImplementedServiceCards()
    applyStoredInterval()

    const select = document.getElementById("healthIntervalSelect")
    if (select) {
        select.addEventListener("change", () => {
            scheduleHealthPolling()
            checkMinistackHealth()
        })
    }

    checkMinistackHealth()
    scheduleHealthPolling()
}

