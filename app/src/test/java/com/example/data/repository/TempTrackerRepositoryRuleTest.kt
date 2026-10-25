package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DailyLog
import com.example.data.model.ProductTempRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TempTrackerRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveDailyLog_validPayload_createsDocumentSuccessfully(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TempTrackerRepository(firestore)

    val log = DailyLog.fromRecords(
      userId = uid,
      date = "2026-10-25",
      dayOfWeek = "Sunday",
      checkedBy = "Inspector Alice",
      records = listOf(
        ProductTempRecord("p01", "bacon cheese turnover", "Bakes", 204.0, 85.0)
      )
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveDailyLog(log)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun observeDailyLog_authenticatedOwner_returnsMatchingLog(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TempTrackerRepository(firestore)

    val log = DailyLog.fromRecords(
      userId = uid,
      date = "2026-10-26",
      dayOfWeek = "Monday",
      checkedBy = "Alice",
      records = listOf(
        ProductTempRecord("p02", "cheese onion lattice bake", "Bakes", 205.5, 90.0)
      )
    )
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveDailyLog(log)
    }

    val emittedLog = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeDailyLog("2026-10-26").first { it != null }
    }
    assertNotNull(emittedLog)
    assertEquals("2026-10-26", emittedLog?.date)
  }

  @Test
  fun observeDailyLog_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()
    val repository = TempTrackerRepository(firestore)

    var failed = false
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeDailyLog("2026-10-25").first()
      }
    } catch (e: Exception) {
      failed = true
    }
    assertTrue("Unauthenticated observation must fail", failed)
  }

  private companion object {
    const val ALICE_EMAIL = "alice@example.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
