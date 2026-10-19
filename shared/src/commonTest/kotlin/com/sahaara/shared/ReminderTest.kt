package com.sahaara.shared
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
class ReminderTest {
 @Test fun createsPendingReminderByDefault() { assertEquals(Reminder.Status.PENDING, Reminder("1", "Review policy", "2026-11-15").status) }
 @Test fun rejectsBlankTitle() { assertFailsWith<IllegalArgumentException> { Reminder("1", " ", "2026-11-15") } }
}
