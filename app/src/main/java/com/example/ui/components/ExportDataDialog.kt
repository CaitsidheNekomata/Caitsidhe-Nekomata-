package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CatProfile
import com.example.data.model.MoodLog
import com.example.data.model.Vocalization
import com.example.export.DataExporter
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark

@Composable
fun ExportDataDialog(
    cat: CatProfile,
    vocalizations: List<Vocalization>,
    moodLogs: List<MoodLog>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val csvContent = remember(cat, vocalizations, moodLogs) {
        DataExporter.generateCatDataCsv(cat, vocalizations, moodLogs)
    }
    val defaultFilename = remember(cat) {
        DataExporter.getDefaultFileName(cat.name)
    }

    // Storage Access Framework launcher: user picks device storage destination
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val success = DataExporter.writeCsvToUri(context, uri, csvContent)
            if (success) {
                Toast.makeText(context, "Export saved to device: $defaultFilename", Toast.LENGTH_LONG).show()
                onDismiss()
            } else {
                Toast.makeText(context, "Failed to write export to storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("export_data_dialog"),
            color = CatSurfaceDark,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CatPrimaryAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "CSV Export",
                                tint = CatPrimaryAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Export Records (CSV)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_export_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceVariantDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Data Bundle for ${cat.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CatSecondaryTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• ${vocalizations.size} meow acoustic & translation records\n" +
                                   "• ${moodLogs.size} mood logs & 24h wellness assessments\n" +
                                   "• Unique vocal quirks, baseline pitch (${cat.baselinePitchHz.toInt()} Hz), and favorite triggers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Export as a spreadsheet CSV file to keep records for your vet, training logs, or personal backup:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action 1: Save directly to Device Storage (Downloads/Documents/SD)
                Button(
                    onClick = {
                        createDocumentLauncher.launch(defaultFilename)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_to_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download to Storage",
                        tint = CatSurfaceDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save to Device Storage",
                        fontWeight = FontWeight.Bold,
                        color = CatSurfaceDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Share / Send CSV via Intent
                OutlinedButton(
                    onClick = {
                        val shareIntent = DataExporter.createShareIntent(context, cat.name, csvContent)
                        if (shareIntent != null) {
                            context.startActivity(shareIntent)
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Could not open share options", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("share_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share CSV",
                        tint = CatSecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share / Send CSV File",
                        fontWeight = FontWeight.Bold,
                        color = CatSecondaryTeal
                    )
                }
            }
        }
    }
}
