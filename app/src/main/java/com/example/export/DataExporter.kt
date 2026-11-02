package com.example.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.CatProfile
import com.example.data.model.MoodLog
import com.example.data.model.Vocalization
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DataExporter {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    fun generateCatDataCsv(
        cat: CatProfile,
        vocalizations: List<Vocalization>,
        moodLogs: List<MoodLog>
    ): String {
        val sb = StringBuilder()
        val exportTimestamp = isoDateFormat.format(Date())

        // 1. Profile Metadata Section
        sb.appendLine("# ==========================================")
        sb.appendLine("# MEOWLINGO - FELINE VOCAL & MOOD EXPORT")
        sb.appendLine("# Exported At: $exportTimestamp")
        sb.appendLine("# ==========================================")
        sb.appendLine()
        sb.appendLine("Section,Key,Value")
        sb.appendLine("Profile,Unique ID,${escapeCsv(cat.id)}")
        sb.appendLine("Profile,Name,${escapeCsv(cat.name)}")
        sb.appendLine("Profile,Breed,${escapeCsv(cat.breed)}")
        sb.appendLine("Profile,Age (Years),${escapeCsv(cat.ageYears)}")
        sb.appendLine("Profile,Baseline Pitch (Hz),${escapeCsv(cat.baselinePitchHz)}")
        sb.appendLine("Profile,Calibration Offset (Hz),${escapeCsv(cat.calibrationOffset)}")
        sb.appendLine("Profile,Vocal Quirks,${escapeCsv(cat.vocalQuirks)}")
        sb.appendLine("Profile,Favorite Triggers,${escapeCsv(cat.favoriteTriggers.joinToString("; "))}")
        sb.appendLine()

        // 2. Meow Vocalization History Section
        sb.appendLine("# ==========================================")
        sb.appendLine("# MEOW VOCALIZATION & TRANSLATION HISTORY (${vocalizations.size} records)")
        sb.appendLine("# ==========================================")
        val vocHeaders = listOf(
            "Record ID",
            "Timestamp UTC",
            "Cat ID",
            "Cat Name",
            "Pitch (Hz)",
            "Duration (ms)",
            "Volume (dB)",
            "Vocalization Type",
            "Ear State",
            "Tail State",
            "Eye State",
            "Mouth State",
            "Environmental Trigger",
            "Human Translation",
            "Emotional State",
            "Confidence Score",
            "User Feedback",
            "Manual Emotion Tag",
            "Manual Context Tag"
        )
        sb.appendLine(vocHeaders.joinToString(",") { escapeCsv(it) })

        vocalizations.forEach { voc ->
            val timeStr = voc.createdAt?.let {
                isoDateFormat.format(Date(it.seconds * 1000L))
            } ?: exportTimestamp

            val row = listOf(
                voc.id,
                timeStr,
                voc.catId,
                voc.catName,
                voc.pitchHz,
                voc.durationMs,
                voc.volumeDb,
                voc.vocalizationType,
                voc.earState,
                voc.tailState,
                voc.eyeState,
                voc.mouthState,
                voc.environmentalTrigger,
                voc.translation,
                voc.emotionalState,
                voc.confidenceScore,
                voc.userFeedbackTag,
                voc.manualEmotionTag,
                voc.manualContextTag
            )
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        sb.appendLine()

        // 3. Mood History Section
        sb.appendLine("# ==========================================")
        sb.appendLine("# 24-HOUR & HISTORICAL MOOD LOGS (${moodLogs.size} records)")
        sb.appendLine("# ==========================================")
        val moodHeaders = listOf(
            "Mood Log ID",
            "Timestamp UTC",
            "Cat ID",
            "Mood State",
            "Wellness Score (0.0 - 1.0)",
            "Vocalizations Count in Period"
        )
        sb.appendLine(moodHeaders.joinToString(",") { escapeCsv(it) })

        moodLogs.forEach { log ->
            val timeStr = log.createdAt?.let {
                isoDateFormat.format(Date(it.seconds * 1000L))
            } ?: exportTimestamp

            val row = listOf(
                log.id,
                timeStr,
                log.catId,
                log.mood,
                log.score,
                log.vocalizationCount
            )
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }

        return sb.toString()
    }

    fun writeCsvToUri(context: Context, uri: Uri, csvContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // UTF-8 Byte Order Mark (BOM) ensures Excel opens multilingual characters seamlessly
                outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            Log.e("DataExporter", "Failed to write CSV to Uri", e)
            false
        }
    }

    fun createShareIntent(context: Context, catName: String, csvContent: String): Intent? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanCatName = catName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val file = File(exportDir, "${cleanCatName}_meow_history_$dateStamp.csv")

            FileOutputStream(file).use { fos ->
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "$catName's Meow & Mood Data Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Intent.createChooser(shareIntent, "Export $catName's Meow History")
        } catch (e: Exception) {
            Log.e("DataExporter", "Failed to create share intent", e)
            null
        }
    }

    fun getDefaultFileName(catName: String): String {
        val cleanName = catName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "${cleanName}_meow_history_$dateStamp.csv"
    }
}
