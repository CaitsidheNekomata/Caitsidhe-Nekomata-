package com.example.ui.tabs

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.ExportDataDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.auth.AuthManager
import com.example.data.model.CatProfile
import com.example.data.model.Vocalization
import com.example.data.repository.CatRepository
import com.example.ui.theme.CatAubergineDark
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.MoodHappy
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CatProfilesTab(
    cats: List<CatProfile>,
    selectedCat: CatProfile?,
    onSelectCat: (CatProfile) -> Unit,
    repository: CatRepository,
    authManager: AuthManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var showAddCatDialog by remember { mutableStateOf(false) }
    var editingCat by remember { mutableStateOf<CatProfile?>(null) }
    var exportingCat by remember { mutableStateOf<CatProfile?>(null) }

    val currentUser = authManager.currentUser
    val catVocs by (selectedCat?.let { repository.getVocalizationsForCat(it.id) }
        ?: repository.vocalizations).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("cat_profiles_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cloud Sync Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentUser != null) CatSecondaryTeal.copy(alpha = 0.12f) else CatSurfaceDark
                ),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Cloud,
                                contentDescription = "Cloud Sync",
                                tint = if (currentUser != null) MoodHappy else CatPrimaryAmber
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentUser != null) "Multi-Device Cloud Sync Active" else "Enable Multi-Device Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser?.email ?: "Sign in to synchronize cat profiles and meows",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (currentUser != null) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        syncMessage = repository.syncAllWithCloud()
                                    }
                                },
                                modifier = Modifier.testTag("sync_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync Now",
                                    tint = CatSecondaryTeal
                                )
                            }
                        }
                    }

                    if (currentUser == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isSigningIn = true
                                coroutineScope.launch {
                                    authManager.signInWithGoogle(context as Activity)
                                    isSigningIn = false
                                }
                            },
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CatSurfaceDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Sign in with Google", color = CatSurfaceDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (syncMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = syncMessage!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = CatSecondaryTeal
                        )
                    }
                }
            }
        }

        // Cat Profiles Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Cats (${cats.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddCatDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_cat_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Cat",
                        tint = CatSurfaceDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Cat", color = CatSurfaceDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Cat Profiles Cards
        items(cats) { cat ->
            val isSelected = cat.id == selectedCat?.id
            val catColor = try {
                Color(android.graphics.Color.parseColor(cat.avatarColor))
            } catch (e: Exception) {
                CatPrimaryAmber
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCat(cat) }
                    .testTag("cat_card_${cat.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CatSurfaceVariantDark else CatSurfaceDark
                ),
                shape = RoundedCornerShape(20.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, catColor) else androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(catColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = CatSurfaceDark
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${cat.breed} • ${cat.ageYears} yrs old",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { exportingCat = cat },
                                modifier = Modifier.testTag("export_cat_${cat.id}_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export CSV Data",
                                    tint = CatPrimaryAmber
                                )
                            }
                            IconButton(onClick = { editingCat = cat }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Calibrate & Edit",
                                    tint = CatSecondaryTeal
                                )
                            }
                            if (cats.size > 1) {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        repository.deleteCat(cat.id)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Vocal Quirks: ${cat.vocalQuirks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Baseline: ${cat.baselinePitchHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatSecondaryTeal
                        )
                        Text(
                            text = "• Calibration Offset: ${cat.calibrationOffset.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatPrimaryAmber
                        )
                    }

                    if (cat.favoriteTriggers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Triggers: ${cat.favoriteTriggers.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // History of Manually Tagged Meows for Model Refinement
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Model Refinement",
                                tint = CatSecondaryTeal
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Model Refinement Engine",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MoodHappy.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Adaptive Tuning Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MoodHappy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val taggedCount = catVocs.count { it.manualEmotionTag != "none" }
                    Text(
                        text = "The system has integrated $taggedCount manually tagged meow samples for ${selectedCat?.name ?: "your cat"} to weight acoustic thresholds and posture correlations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // List of Tagged Meow History
        item {
            Text(
                text = "Tagged Meows History (${selectedCat?.name ?: "Cat"})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val taggedVocs = catVocs.filter { it.manualEmotionTag != "none" }
        if (taggedVocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceVariantDark.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No manually tagged meows yet. When a meow is recorded on the Translator tab, tap 'Tag & Refine' to train your cat's model!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(taggedVocs) { voc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tag: ${voc.manualEmotionTag.uppercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CatPrimaryAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Context: ${voc.manualContextTag}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CatSecondaryTeal
                                )
                            }
                            Text(
                                text = "\"${voc.translation}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Acoustics: ${voc.pitchHz.toInt()} Hz • ${voc.durationMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CatSecondaryTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Refined",
                                style = MaterialTheme.typography.labelSmall,
                                color = CatSecondaryTeal
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    // Add Cat Dialog
    if (showAddCatDialog) {
        CatEditDialog(
            cat = null,
            onDismiss = { showAddCatDialog = false },
            onSave = { newCat ->
                coroutineScope.launch {
                    repository.saveCat(newCat)
                    showAddCatDialog = false
                }
            }
        )
    }

    // Edit Cat Dialog
    if (editingCat != null) {
        CatEditDialog(
            cat = editingCat,
            onDismiss = { editingCat = null },
            onSave = { updatedCat ->
                coroutineScope.launch {
                    repository.saveCat(updatedCat)
                    editingCat = null
                }
            }
        )
    }

    if (exportingCat != null) {
        val catForExport = exportingCat!!
        val vocsToExport by repository.getVocalizationsForCat(catForExport.id).collectAsState(initial = emptyList())
        val moodsToExport by repository.getMoodLogsForCat(catForExport.id).collectAsState(initial = emptyList())
        ExportDataDialog(
            cat = catForExport,
            vocalizations = vocsToExport,
            moodLogs = moodsToExport,
            onDismiss = { exportingCat = null }
        )
    }
}

@Composable
fun CatEditDialog(
    cat: CatProfile?,
    onDismiss: () -> Unit,
    onSave: (CatProfile) -> Unit
) {
    var name by remember { mutableStateOf(cat?.name ?: "") }
    var breed by remember { mutableStateOf(cat?.breed ?: "Domestic Shorthair") }
    var ageText by remember { mutableStateOf((cat?.ageYears ?: 2.0).toString()) }
    var quirks by remember { mutableStateOf(cat?.vocalQuirks ?: "Raspy meow when hungry, high chirrup when greeting") }
    var baselinePitch by remember { mutableStateOf((cat?.baselinePitchHz ?: 540.0).toFloat()) }
    var calibrationOffset by remember { mutableStateOf((cat?.calibrationOffset ?: 0.0).toFloat()) }
    var selectedColor by remember { mutableStateOf(cat?.avatarColor ?: "#FF8A50") }

    val avatarColors = listOf("#FF8A50", "#4DD0E1", "#CE93D8", "#66BB6A", "#FFD54F", "#FF7043")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("cat_edit_dialog"),
            color = CatSurfaceDark,
            tonalElevation = 6.dp
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Text(
                        text = if (cat == null) "New Cat Profile" else "Edit ${cat.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Cat Name") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breed
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_breed_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Age
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age (Years)") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_age_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Vocal Quirks
                    OutlinedTextField(
                        value = quirks,
                        onValueChange = { quirks = it },
                        label = { Text("Unique Vocal Quirks") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_quirks_input"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar Color
                    Text(
                        text = "Avatar Color:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        avatarColors.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = hex }
                                    .border(
                                        if (selectedColor == hex) 2.dp else 0.dp,
                                        Color.White,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Baseline Pitch Slider
                    Text(
                        text = "Baseline Meow Frequency: ${baselinePitch.toInt()} Hz",
                        style = MaterialTheme.typography.labelMedium,
                        color = CatSecondaryTeal
                    )
                    Slider(
                        value = baselinePitch,
                        onValueChange = { baselinePitch = it },
                        valueRange = 300f..900f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calibration Offset
                    Text(
                        text = "Calibration Pitch Offset: ${calibrationOffset.toInt()} Hz",
                        style = MaterialTheme.typography.labelMedium,
                        color = CatPrimaryAmber
                    )
                    Slider(
                        value = calibrationOffset,
                        onValueChange = { calibrationOffset = it },
                        valueRange = -50f..50f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val newCat = (cat ?: CatProfile()).copy(
                                        name = name,
                                        breed = breed,
                                        ageYears = ageText.toDoubleOrNull() ?: 2.0,
                                        vocalQuirks = quirks,
                                        avatarColor = selectedColor,
                                        baselinePitchHz = baselinePitch.toDouble(),
                                        calibrationOffset = calibrationOffset.toDouble()
                                    )
                                    onSave(newCat)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                            modifier = Modifier.testTag("save_cat_button")
                        ) {
                            Text("Save Profile", color = CatSurfaceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
