package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DailyLog
import com.example.data.model.ProductTempRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun escapeCsv(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }

    fun exportDayToCsv(context: Context, log: DailyLog): File {
        val fileName = "TempReport_${log.date.ifEmpty { "log" }}.csv"
        val file = File(getReportsDir(context), fileName)
        val records = log.toProductRecords()

        file.printWriter(Charsets.UTF_8).use { out ->
            out.print("\uFEFF") // UTF-8 BOM for Microsoft Excel / Google Sheets
            out.println("DAILY FOOD TEMPERATURE COMPLIANCE REPORT")
            out.println("Date,${log.date}")
            out.println("Day,${log.dayOfWeek}")
            out.println("Checked By,${escapeCsv(log.checkedBy)}")
            out.println("General Notes,${escapeCsv(log.notes ?: "")}")
            out.println()
            out.println("Product ID,Product Name,Category,Core Temp (°C),Core Status (>=200°C),T1 Temp (°C),T1 Status (>=81°C),Overall Compliance,Notes")

            for (record in records) {
                val coreStr = record.coreTemp?.let { "${it.toInt()}" } ?: "N/A"
                val coreStatus = when {
                    record.coreTemp == null -> "Not Recorded"
                    record.isCoreCompliant -> "PASS"
                    else -> "FAIL (<200°C)"
                }
                val t1Str = record.t1Temp?.let { "${it.toInt()}" } ?: "N/A"
                val t1Status = when {
                    record.t1Temp == null -> "Not Recorded"
                    record.isT1Compliant -> "PASS"
                    else -> "FAIL (<81°C)"
                }
                val overall = when {
                    record.coreTemp == null && record.t1Temp == null -> "PENDING"
                    record.isCoreCompliant && record.isT1Compliant -> "PASS"
                    else -> "NON-COMPLIANT"
                }

                out.println(
                    "${escapeCsv(record.productId)}," +
                    "${escapeCsv(record.productName)}," +
                    "${escapeCsv(record.category)}," +
                    "$coreStr," +
                    "${escapeCsv(coreStatus)}," +
                    "$t1Str," +
                    "${escapeCsv(t1Status)}," +
                    "${escapeCsv(overall)}," +
                    escapeCsv(record.notes)
                )
            }
        }
        return file
    }

    fun exportAllToCsv(context: Context, logs: List<DailyLog>): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "TempReport_All_${timeStamp}.csv"
        val file = File(getReportsDir(context), fileName)

        file.printWriter(Charsets.UTF_8).use { out ->
            out.print("\uFEFF") // UTF-8 BOM for Microsoft Excel / Google Sheets
            out.println("HISTORICAL DAILY FOOD TEMPERATURE LOGS EXPORT")
            out.println("Export Date,$timeStamp")
            out.println("Total Log Days,${logs.size}")
            out.println()
            out.println("Date,Day,Checked By,Product Name,Category,Core Temp (°C),Core Status,T1 Temp (°C),T1 Status,Overall Status,Notes")

            for (log in logs) {
                for (record in log.toProductRecords()) {
                    val coreStr = record.coreTemp?.let { "${it.toInt()}" } ?: "N/A"
                    val coreStatus = if (record.isCoreCompliant) "PASS" else if (record.coreTemp != null) "FAIL" else "N/A"
                    val t1Str = record.t1Temp?.let { "${it.toInt()}" } ?: "N/A"
                    val t1Status = if (record.isT1Compliant) "PASS" else if (record.t1Temp != null) "FAIL" else "N/A"
                    val overall = if (record.isCoreCompliant && record.isT1Compliant) "PASS" else "CHECK"

                    out.println(
                        "${escapeCsv(log.date)}," +
                        "${escapeCsv(log.dayOfWeek)}," +
                        "${escapeCsv(log.checkedBy)}," +
                        "${escapeCsv(record.productName)}," +
                        "${escapeCsv(record.category)}," +
                        "$coreStr," +
                        "${escapeCsv(coreStatus)}," +
                        "$t1Str," +
                        "${escapeCsv(t1Status)}," +
                        "${escapeCsv(overall)}," +
                        escapeCsv(record.notes)
                    )
                }
            }
        }
        return file
    }

    fun exportDayToPdf(context: Context, log: DailyLog): File {
        val fileName = "TempReport_${log.date.ifEmpty { "log" }}.pdf"
        val file = File(getReportsDir(context), fileName)
        val records = log.toProductRecords()

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 page
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header Background
        paint.color = Color.rgb(27, 54, 93) // #1B365D Deep Navy
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DAILY PRODUCT TEMPERATURE LOG", 24f, 35f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Food Hygiene & Safety Compliance Check Sheet", 24f, 55f, paint)

        // Metadata box
        paint.color = Color.rgb(245, 247, 250)
        canvas.drawRect(24f, 85f, 571f, 130f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date: ${log.date} (${log.dayOfWeek})", 34f, 105f, paint)
        canvas.drawText("Checked By: ${log.checkedBy.ifEmpty { "Unassigned" }}", 220f, 105f, paint)

        val passCount = records.count { it.isCoreCompliant && it.isT1Compliant }
        val totalCount = records.size
        paint.color = if (passCount == totalCount) Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
        canvas.drawText("Status: $passCount / $totalCount Compliant", 420f, 105f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Standard Targets: Core Temp >= 200°C | T1 Temp >= 81°C", 34f, 122f, paint)

        // Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(24f, 140f, 571f, 160f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("#", 28f, 154f, paint)
        canvas.drawText("Product Name", 48f, 154f, paint)
        canvas.drawText("Category", 230f, 154f, paint)
        canvas.drawText("Core (°C)", 340f, 154f, paint)
        canvas.drawText("T1 (°C)", 415f, 154f, paint)
        canvas.drawText("Result", 490f, 154f, paint)

        // Rows
        var y = 175f
        val rowHeight = 15f
        paint.textSize = 8f

        for (i in records.indices) {
            val record = records[i]
            if (y > 770f) break // Fit on A4 sheet

            // Zebra background
            if (i % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(24f, y - 11f, 571f, y + 4f, paint)
            }

            // Index
            paint.color = Color.rgb(100, 116, 139)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("${i + 1}", 28f, y, paint)

            // Name
            paint.color = Color.rgb(15, 23, 42)
            val truncatedName = if (record.productName.length > 32) record.productName.take(30) + ".." else record.productName
            canvas.drawText(truncatedName, 48f, y, paint)

            // Category
            paint.color = Color.rgb(71, 85, 105)
            val truncatedCat = if (record.category.length > 18) record.category.take(16) + ".." else record.category
            canvas.drawText(truncatedCat, 230f, y, paint)

            // Core Temp (whole number)
            val coreStr = record.coreTemp?.let { "${it.toInt()}°C" } ?: "-"
            paint.color = if (record.isCoreCompliant) Color.rgb(46, 125, 50) else if (record.coreTemp != null) Color.rgb(211, 47, 47) else Color.GRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(coreStr, 340f, y, paint)

            // T1 Temp (whole number)
            val t1Str = record.t1Temp?.let { "${it.toInt()}°C" } ?: "-"
            paint.color = if (record.isT1Compliant) Color.rgb(46, 125, 50) else if (record.t1Temp != null) Color.rgb(211, 47, 47) else Color.GRAY
            canvas.drawText(t1Str, 415f, y, paint)

            // Status Badge
            val status = when {
                record.coreTemp == null && record.t1Temp == null -> "PENDING"
                record.isCoreCompliant && record.isT1Compliant -> "PASS"
                else -> "CHECK"
            }
            paint.color = when (status) {
                "PASS" -> Color.rgb(46, 125, 50)
                "CHECK" -> Color.rgb(211, 47, 47)
                else -> Color.GRAY
            }
            canvas.drawText(status, 490f, y, paint)

            y += rowHeight
        }

        // Sign off footer
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(24f, 790f, 571f, 790f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Manager / Supervisor Signature: ___________________________", 24f, 810f, paint)
        canvas.drawText("Generated by TempTracker Cloud", 420f, 810f, paint)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun shareReport(context: Context, file: File, mimeType: String, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.temptracker.kxmpvq.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Export Report via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
