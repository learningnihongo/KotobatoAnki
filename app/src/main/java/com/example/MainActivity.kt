package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AddCardModernFab
import com.example.ui.components.AddCardOptionsBottomSheet
import com.example.ui.screens.AddEditCardDialog
import com.example.ui.screens.AddFlashcardScreen
import com.example.ui.screens.AnkiSyncDialog
import com.example.ui.screens.FormatGuideDialog
import com.example.ui.screens.CreateOrEditDeckDialog
import com.example.ui.screens.DeckListScreen
import com.example.ui.screens.StarredCardsScreen
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
    val allStarredCards by viewModel.allStarredCards.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var ankiSyncDeck by remember { mutableStateOf<DeckEntity?>(null) }
    var ankiSyncCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var cardToEdit by remember { mutableStateOf<CardEntity?>(null) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showCreateDeckDialog by remember { mutableStateOf(false) }
    var showAddCardBottomSheet by remember { mutableStateOf(false) }
    var showFormatGuideDialog by remember { mutableStateOf(false) }

    val fileImportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val (newDeck, count) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        viewModel.repository.importFromUri(context, uri)
                    }
                    android.widget.Toast.makeText(context, "${newDeck.name} မှ ကတ် ${count} ခု ထည့်သွင်းပြီးပါပြီ!", android.widget.Toast.LENGTH_LONG).show()
                    viewModel.onImportDone()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "သွင်းယူမှု မအောင်မြင်ပါ: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Intercept hardware/system back button on sub-screens
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    val isTopLevelScreen = currentScreen == Screen.DASHBOARD || currentScreen == Screen.DECKS_MANAGER

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (isTopLevelScreen) {
                AddCardModernFab(
                    onClick = { showAddCardBottomSheet = true }
                )
            }
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline Notification Banner
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offline_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "အော့ဖ်လိုင်းစနစ် (Offline Mode) • အင်တာနက်မရှိဘဲ ကတ်များအားလုံး ဖုန်းထဲတွင် အပြည့်အဝ အသုံးပြုနိုင်ပါသည်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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
                            },
                            onOpenStarredReview = {
                                viewModel.navigateTo(Screen.STARRED_LIST)
                            },
                            onSelectDeckForShuffleStudy = { deck ->
                                viewModel.startShuffledStudy(deck)
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
                                onAddNewCard = { viewModel.navigateTo(Screen.ADD_CARD) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                    }

                    Screen.ADD_CARD -> {
                        AddFlashcardScreen(
                            decks = allDecks,
                            selectedDeck = selectedDeck,
                            onSaveFlashcard = { front, back, deckName, notes, onDone ->
                                viewModel.addFlashcard(
                                    front = front,
                                    back = back,
                                    deckName = deckName,
                                    notes = notes,
                                    onSuccess = { onDone() }
                                )
                            },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    Screen.STARRED_LIST -> {
                        StarredCardsScreen(
                            starredCards = allStarredCards,
                            decks = allDecks,
                            onSpeak = { text -> viewModel.soundHelper.speakJapanese(text) },
                            onToggleStar = { card -> viewModel.toggleStar(card) },
                            onEditCard = { card -> cardToEdit = card },
                            onDeleteCard = { card -> viewModel.deleteCard(card) },
                            onStudyStarred = { cardsToStudy ->
                                viewModel.studyStarredCards(cardsToStudy)
                            },
                            onQuizStarred = { cardsToQuiz ->
                                viewModel.quizStarredCards(cardsToQuiz)
                            },
                            onBack = { viewModel.navigateBack() }
                        )
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
                val currentDeckId = selectedDeck?.id ?: allDecks.firstOrNull()?.id ?: 1L
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

            // Create Deck Dialog from FAB
            if (showCreateDeckDialog) {
                CreateOrEditDeckDialog(
                    initialDeck = null,
                    onDismiss = { showCreateDeckDialog = false },
                    onConfirm = { name, desc ->
                        viewModel.createDeck(name, desc)
                        showCreateDeckDialog = false
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

            // Add Card Options Bottom Sheet
            if (showAddCardBottomSheet) {
                AddCardOptionsBottomSheet(
                    onDismiss = { showAddCardBottomSheet = false },
                    onQuickAddCard = { showAddCardDialog = true },
                    onNavigateToAddCard = { viewModel.navigateTo(Screen.ADD_CARD) },
                    onImportFile = { fileImportLauncher.launch(arrayOf("*/*")) },
                    onShowFormatGuide = { showFormatGuideDialog = true },
                    onCreateNewDeck = { showCreateDeckDialog = true }
                )
            }

            // Standalone Format Guide Dialog
            if (showFormatGuideDialog) {
                FormatGuideDialog(
                    onDismiss = { showFormatGuideDialog = false },
                    onImportClick = {
                        showFormatGuideDialog = false
                        fileImportLauncher.launch(arrayOf("*/*"))
                    }
                )
            }
        }
    }
}
}
