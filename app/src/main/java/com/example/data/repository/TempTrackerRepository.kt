package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.DailyLog
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TempTrackerRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeDailyLog(date: String): Flow<DailyLog?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/daily_logs/$date"
        val docRef = db.collection("users").document(uid).collection("daily_logs").document(date)

        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(DailyLog::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeAllLogs(): Flow<List<DailyLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/daily_logs"
        val collectionRef = db.collection("users").document(uid).collection("daily_logs")
            .orderBy("date", Query.Direction.DESCENDING)

        emitAll(
            collectionRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DailyLog::class.java)
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveDailyLog(log: DailyLog): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(log.date.isNotBlank()) { "Date must not be blank" }
        val docRef = db.collection("users").document(uid).collection("daily_logs").document(log.date)
        val path = docRef.path

        try {
            val existingDoc = docRef.get().await()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "date" to log.date,
                "dayOfWeek" to log.dayOfWeek,
                "checkedBy" to log.checkedBy,
                "itemsCount" to log.itemsCount,
                "items" to log.items,
                "notes" to log.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!existingDoc.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = existingDoc.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()
            }

            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteDailyLog(date: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("daily_logs").document(date)
        val path = docRef.path
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
