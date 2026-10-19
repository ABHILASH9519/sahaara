package com.sahaara.shared

/** Local reminder model; persistence and synchronization are not implemented yet. */
data class Reminder(val id: String, val title: String, val dueDateIso: String, val status: Status = Status.PENDING) {
 enum class Status { PENDING, COMPLETED }
 init { require(id.isNotBlank()); require(title.isNotBlank()); require(dueDateIso.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) }
}
