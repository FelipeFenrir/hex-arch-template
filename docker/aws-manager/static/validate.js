function validateJSON(id, errorId) {
    const field = document.getElementById(id)
    const error = document.getElementById(errorId)

    try {
        if (field.value.trim() !== "") {
            JSON.parse(field.value)
        }
        field.classList.remove("border-red-500")
        error.classList.add("hidden")
        return true
    } catch {
        field.classList.add("border-red-500")
        error.classList.remove("hidden")
        return false
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("headers").oninput = () =>
        validateJSON("headers", "headersError")

    document.getElementById("body").oninput = () =>
        validateJSON("body", "bodyError")
})