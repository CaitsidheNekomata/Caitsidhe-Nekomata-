package com.example.ui.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.DictionaryData
import com.example.data.model.BehaviorSignal
import com.example.data.model.SignalCategory
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.MoodHappy

@Composable
fun DictionaryTab() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SignalCategory?>(null) }

    val filteredSignals = remember(searchQuery, selectedCategory) {
        DictionaryData.allSignals.filter { signal ->
            (selectedCategory == null || signal.category == selectedCategory) &&
            (searchQuery.isBlank() ||
             signal.name.contains(searchQuery, ignoreCase = true) ||
             signal.primaryEmotion.contains(searchQuery, ignoreCase = true) ||
             signal.description.contains(searchQuery, ignoreCase = true) ||
             signal.whatCatIsSaying.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dictionary_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Feline Behavioral Signals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visual & acoustic body language dictionary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CatPrimaryAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Dictionary",
                        tint = CatPrimaryAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search signals (e.g. slow blink, trill, ears)...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = CatSecondaryTeal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_dictionary_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CatSurfaceDark,
                    unfocusedContainerColor = CatSurfaceDark,
                    focusedBorderColor = CatSecondaryTeal,
                    unfocusedBorderColor = CatCardBorderDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryPill(
                        label = "All Signals (${DictionaryData.allSignals.size})",
                        isSelected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                }
                items(SignalCategory.values()) { category ->
                    CategoryPill(
                        label = category.displayName,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        items(filteredSignals) { signal ->
            SignalCard(signal = signal)
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CatPrimaryAmber else CatSurfaceDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CatSurfaceDark else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SignalCard(signal: BehaviorSignal) {
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
                Text(
                    text = signal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CatSecondaryTeal.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = signal.primaryEmotion,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CatSecondaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = signal.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // What the cat is saying bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CatSurfaceVariantDark)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "What Cat Means:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CatPrimaryAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = signal.whatCatIsSaying,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Acoustic Correlation
            Text(
                text = "🔊 Acoustic Match: ${signal.acousticCorrelation}",
                style = MaterialTheme.typography.labelSmall,
                color = CatSecondaryTeal
            )

            Spacer(modifier = Modifier.height(4.dp))

            // How to respond tip
            Text(
                text = "💡 How to Respond: ${signal.howToRespond}",
                style = MaterialTheme.typography.labelSmall,
                color = MoodHappy
            )
        }
    }
}
