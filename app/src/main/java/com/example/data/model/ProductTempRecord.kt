package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductTempRecord(
    val productId: String = "",
    val productName: String = "",
    val category: String = "Standard",
    val coreTemp: Double? = null,
    val t1Temp: Double? = null,
    val notes: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "productId" to productId,
            "productName" to productName,
            "category" to category,
            "coreTemp" to coreTemp,
            "t1Temp" to t1Temp,
            "notes" to notes
        ).filterValues { it != null }
    }

    // Food safety compliance: Core temp must be >= 200°C, T1 must be >= 81°C
    val isCoreCompliant: Boolean
        get() = coreTemp != null && coreTemp >= 200.0

    val isT1Compliant: Boolean
        get() = t1Temp != null && t1Temp >= 81.0

    val hasAnyTemp: Boolean
        get() = coreTemp != null || t1Temp != null
}
