package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DailyLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "",
    val dayOfWeek: String = "",
    val checkedBy: String = "",
    val itemsCount: Long = 0L,
    val items: List<Map<String, Any?>> = emptyList(),
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toProductRecords(): List<ProductTempRecord> {
        return items.map { map ->
            ProductTempRecord(
                productId = (map["productId"] as? String).orEmpty(),
                productName = (map["productName"] as? String).orEmpty(),
                category = (map["category"] as? String) ?: "Standard",
                coreTemp = (map["coreTemp"] as? Number)?.toDouble(),
                t1Temp = (map["t1Temp"] as? Number)?.toDouble(),
                notes = (map["notes"] as? String).orEmpty()
            )
        }
    }

    companion object {
        fun fromRecords(
            userId: String,
            date: String,
            dayOfWeek: String,
            checkedBy: String,
            records: List<ProductTempRecord>,
            notes: String? = null,
            createdAt: Timestamp? = null
        ): DailyLog {
            return DailyLog(
                id = date,
                userId = userId,
                date = date,
                dayOfWeek = dayOfWeek,
                checkedBy = checkedBy,
                itemsCount = records.size.toLong(),
                items = records.map { it.toMap() },
                notes = notes,
                createdAt = createdAt,
                updatedAt = null
            )
        }
    }
}
