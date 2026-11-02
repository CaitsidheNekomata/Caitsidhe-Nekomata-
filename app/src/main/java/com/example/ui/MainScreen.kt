package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.CatAudioAnalyzer
import com.example.audio.TalkBackSynthesizer
import com.example.data.auth.AuthManager
import com.example.data.model.CatProfile
import com.example.data.repository.CatRepository
import com.example.domain.MoodMeterEngine
import com.example.domain.TranslationEngine
import com.example.ui.tabs.CameraPostureTab
import com.example.ui.tabs.CatProfilesTab
import com.example.ui.tabs.DictionaryTab
import com.example.ui.tabs.MoodMeterTab
import com.example.ui.tabs.TranslatorTab
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark

enum class MainTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    TRANSLATOR("Translate", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq),
    CAMERA("Posture", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    MOOD("Mood", Icons.Filled.Speed, Icons.Outlined.Speed),
    DICTIONARY("Signals", Icons.Filled.Book, Icons.Outlined.Book),
    PROFILES("Cats", Icons.Filled.Pets, Icons.Outlined.Pets)
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val repository = remember { CatRepository(context) }
    val authManager = remember { AuthManager(context) }
    val audioAnalyzer = remember { CatAudioAnalyzer(coroutineScope) }
    val talkBackSynthesizer = remember { TalkBackSynthesizer() }
    val translationEngine = remember { TranslationEngine() }
    val moodMeterEngine = remember { MoodMeterEngine() }

    var currentTab by remember { mutableStateOf(MainTab.TRANSLATOR) }
    val cats by repository.cats.collectAsState(initial = emptyList())
    var selectedCat by remember { mutableStateOf<CatProfile?>(null) }

    LaunchedEffect(Unit) {
        repository.initializeDefaultDataIfEmpty()
        authManager.attemptAutoSignIn()
    }

    LaunchedEffect(cats) {
        if (selectedCat == null && cats.isNotEmpty()) {
            selectedCat = cats.first()
        }
    }

    // Custom state back handler
    BackHandler(enabled = currentTab != MainTab.TRANSLATOR) {
        currentTab = MainTab.TRANSLATOR
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = CatSurfaceDark,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CatSurfaceDark,
                            selectedTextColor = CatPrimaryAmber,
                            indicatorColor = CatPrimaryAmber,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tabTransition") { tab ->
                when (tab) {
                    MainTab.TRANSLATOR -> TranslatorTab(
                        cats = cats,
                        selectedCat = selectedCat,
                        onSelectCat = { selectedCat = it },
                        audioAnalyzer = audioAnalyzer,
                        translationEngine = translationEngine,
                        talkBackSynthesizer = talkBackSynthesizer,
                        repository = repository,
                        onNavigateToCamera = { currentTab = MainTab.CAMERA }
                    )
                    MainTab.CAMERA -> CameraPostureTab(
                        selectedCat = selectedCat,
                        audioAnalyzer = audioAnalyzer,
                        translationEngine = translationEngine,
                        repository = repository
                    )
                    MainTab.MOOD -> MoodMeterTab(
                        selectedCat = selectedCat,
                        repository = repository,
                        moodMeterEngine = moodMeterEngine
                    )
                    MainTab.DICTIONARY -> DictionaryTab()
                    MainTab.PROFILES -> CatProfilesTab(
                        cats = cats,
                        selectedCat = selectedCat,
                        onSelectCat = { selectedCat = it },
                        repository = repository,
                        authManager = authManager
                    )
                }
            }
        }
    }
}
