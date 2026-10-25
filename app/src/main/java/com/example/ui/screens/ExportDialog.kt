package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyLog
import com.example.util.ReportExporter

@Composable
fun ExportDialog(
    currentLog: DailyLog,
    allLogs: List<DailyLog>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Temperature Reports",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select format to download or share compliance check sheets:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExportOptionCard(
                    title = "Export Today as PDF",
                    subtitle = "Printable formal audit sheet with compliance signatures (${currentLog.date})",
                    icon = Icons.Default.PictureAsPdf,
                    testTag = "export_pdf_button",
                    onClick = {
                        try {
                            val pdfFile = ReportExporter.exportDayToPdf(context, currentLog)
                            ReportExporter.shareReport(
                                context,
                                pdfFile,
                                "application/pdf",
                                "Food Temperature Report - ${currentLog.date}"
                            )
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExportOptionCard(
                    title = "Export Today as CSV",
                    subtitle = "Spreadsheet table of Core & T1 temps for Excel / Google Sheets",
                    icon = Icons.Default.TableChart,
                    testTag = "export_csv_button",
                    onClick = {
                        try {
                            val csvFile = ReportExporter.exportDayToCsv(context, currentLog)
                            ReportExporter.shareReport(
                                context,
                                csvFile,
                                "text/csv",
                                "Food Temperature CSV - ${currentLog.date}"
                            )
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExportOptionCard(
                    title = "Export All Historical Logs (CSV)",
                    subtitle = "Complete archive of all recorded days (${allLogs.size} days in cloud)",
                    icon = Icons.Default.Description,
                    testTag = "export_all_csv_button",
                    onClick = {
                        try {
                            val csvFile = ReportExporter.exportAllToCsv(context, allLogs.ifEmpty { listOf(currentLog) })
                            ReportExporter.shareReport(
                                context,
                                csvFile,
                                "text/csv",
                                "Complete Historical Food Temperature Log"
                            )
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
