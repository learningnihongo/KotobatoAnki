package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AddEditCardDialog
import com.example.ui.screens.AnkiSyncDialog
import com.example.ui.screens.DeckListScreen
import com.example.ui.screens.DeckManagementScreen
import com.example.ui.screens.MatchGameScreen
import com.example.ui.screens.QuizGameScreen
import com.example.ui.screens.StudyFlipScreen
import com.example.ui.screens.VocabListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allDecks by viewModel.allDecks.collectAsState()
    val selectedDeck by viewModel.selectedDeck.collectAsState()
    val cardsByDeck by viewModel.cardsByDeck.collectAsState()
    val currentDeckCards by viewModel.currentDeckCards.collectAsState()

    var ankiSyncDeck by remember { mutableStateOf<DeckEntity?>(null) }
    var ankiSyncCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var cardToEdit by remember { mutableStateOf<CardEntity?>(null) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    // Intercept hardware/system back button on sub-screens
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    val isTopLevelScreen = currentScreen == Screen.DASHBOARD || currentScreen == Screen.DECKS_MANAGER

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val defaultDeck = selectedDeck ?: allDecks.firstOrNull()

                    NavigationBarItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.DECKS_MANAGER,
                        onClick = { viewModel.navigateTo(Screen.DECKS_MANAGER) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Decks") },
                        label = { Text("Decks") },
                        modifier = Modifier.testTag("nav_item_decks_manager")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (defaultDeck != null) viewModel.navigateTo(Screen.STUDY_FLIP, defaultDeck)
                        },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Flashcards") },
                        label = { Text("Study") },
                        modifier = Modifier.testTag("nav_item_study")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (defaultDeck != null) viewModel.navigateTo(Screen.QUIZ_GAME, defaultDeck)
                        },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                        label = { Text("Quiz") },
                        modifier = Modifier.testTag("nav_item_quiz")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (defaultDeck != null) viewModel.navigateTo(Screen.MATCH_GAME, defaultDeck)
                        },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Match") },
                        label = { Text("Match") },
                        modifier = Modifier.testTag("nav_item_match")
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
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    Screen.DASHBOARD -> {
                        DeckListScreen(
                            decks = allDecks,
                            cardsByDeck = cardsByDeck,
                            onSelectDeckForStudy = { deck ->
                                viewModel.navigateTo(Screen.STUDY_FLIP, deck)
                            },
                            onSelectDeckForQuiz = { deck ->
                                viewModel.navigateTo(Screen.QUIZ_GAME, deck)
                            },
                            onSelectDeckForMatch = { deck ->
                                viewModel.navigateTo(Screen.MATCH_GAME, deck)
                            },
                            onSelectDeckForList = { deck ->
                                viewModel.navigateTo(Screen.VOCAB_LIST, deck)
                            },
                            onOpenAnkiSync = { deck, cards ->
                                ankiSyncDeck = deck
                                ankiSyncCards = cards
                            },
                            onCreateDeck = { name, desc ->
                                viewModel.createDeck(name, desc)
                            },
                            onDeleteDeck = { deck ->
                                viewModel.deleteDeck(deck)
                            },
                            onNavigateToDeckManager = {
                                viewModel.navigateTo(Screen.DECKS_MANAGER)
                            }
                        )
                    }

                    Screen.DECKS_MANAGER -> {
                        DeckManagementScreen(
                            decks = allDecks,
                            cardsByDeck = cardsByDeck,
                            onStudyDeck = { deck ->
                                viewModel.navigateTo(Screen.STUDY_FLIP, deck)
                            },
                            onViewDeckCards = { deck ->
                                viewModel.navigateTo(Screen.VOCAB_LIST, deck)
                            },
                            onCreateDeck = { name, desc ->
                                viewModel.createDeck(name, desc)
                            },
                            onUpdateDeck = { deck ->
                                viewModel.updateDeck(deck)
                            },
                            onDuplicateDeck = { deck ->
                                viewModel.duplicateDeck(deck)
                            },
                            onDeleteDeck = { deck ->
                                viewModel.deleteDeck(deck)
                            },
                            onAnkiExport = { deck, cards ->
                                ankiSyncDeck = deck
                                ankiSyncCards = cards
                            },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    Screen.STUDY_FLIP -> {
                        val activeDeck = selectedDeck ?: allDecks.firstOrNull()
                        if (activeDeck != null) {
                            StudyFlipScreen(
                                deck = activeDeck,
                                cards = currentDeckCards,
                                onSpeak = { text -> viewModel.soundHelper.speakJapanese(text) },
                                onRateCard = { card, rating -> viewModel.rateCard(card, rating) },
                                onToggleStar = { card -> viewModel.toggleStar(card) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                    }

                    Screen.QUIZ_GAME -> {
                        QuizGameScreen(
                            cards = currentDeckCards,
                            soundHelper = viewModel.soundHelper,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    Screen.MATCH_GAME -> {
                        MatchGameScreen(
                            cards = currentDeckCards,
                            soundHelper = viewModel.soundHelper,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    Screen.VOCAB_LIST -> {
                        val activeDeck = selectedDeck ?: allDecks.firstOrNull()
                        if (activeDeck != null) {
                            VocabListScreen(
                                deck = activeDeck,
                                cards = currentDeckCards,
                                onSpeak = { text -> viewModel.soundHelper.speakJapanese(text) },
                                onToggleStar = { card -> viewModel.toggleStar(card) },
                                onEditCard = { card -> cardToEdit = card },
                                onDeleteCard = { card -> viewModel.deleteCard(card) },
                                onAddNewCard = { showAddCardDialog = true },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }

            // Anki Sync Modal Dialog
            ankiSyncDeck?.let { deck ->
                AnkiSyncDialog(
                    deck = deck,
                    cards = ankiSyncCards,
                    repository = viewModel.repository,
                    onDismiss = {
                        ankiSyncDeck = null
                        ankiSyncCards = emptyList()
                    },
                    onImportSuccess = { _, _ ->
                        viewModel.onImportDone()
                    }
                )
            }

            // Add Card Dialog
            if (showAddCardDialog) {
                val currentDeckId = selectedDeck?.id ?: 1L
                AddEditCardDialog(
                    deckId = currentDeckId,
                    cardToEdit = null,
                    onDismiss = { showAddCardDialog = false },
                    onSave = { newCard ->
                        viewModel.saveCard(newCard)
                        showAddCardDialog = false
                    }
                )
            }

            // Edit Card Dialog
            cardToEdit?.let { card ->
                AddEditCardDialog(
                    deckId = card.deckId,
                    cardToEdit = card,
                    onDismiss = { cardToEdit = null },
                    onSave = { updatedCard ->
                        viewModel.saveCard(updatedCard)
                        cardToEdit = null
                    }
                )
            }
        }
    }
}
