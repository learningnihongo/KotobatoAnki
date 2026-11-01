package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.data.FlashcardRepository
import com.example.data.LocalCacheManager
import com.example.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    DECKS_MANAGER,
    STUDY_FLIP,
    QUIZ_GAME,
    MATCH_GAME,
    VOCAB_LIST,
    ADD_CARD,
    STARRED_LIST
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = FlashcardRepository(database.deckDao(), database.cardDao(), database.flashcardDao())
    val soundHelper = SoundHelper(application)
    val networkMonitor = NetworkMonitor(application)
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val _cacheStats = MutableStateFlow<LocalCacheManager.CacheStats?>(null)
    val cacheStats: StateFlow<LocalCacheManager.CacheStats?> = _cacheStats.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedDeck = MutableStateFlow<DeckEntity?>(null)
    val selectedDeck: StateFlow<DeckEntity?> = _selectedDeck.asStateFlow()

    private val _allDecks = repository.getAllDecks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val allDecks: StateFlow<List<DeckEntity>> = _allDecks

    // Reactive list of all starred cards across all decks
    val allStarredCards: StateFlow<List<CardEntity>> = repository.getAllStarredCardsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Map of deckId to List<CardEntity>
    private val _cardsByDeck = MutableStateFlow<Map<Long, List<CardEntity>>>(emptyMap())
    val cardsByDeck: StateFlow<Map<Long, List<CardEntity>>> = _cardsByDeck.asStateFlow()

    // Cards for currently selected deck
    private val _currentDeckCards = MutableStateFlow<List<CardEntity>>(emptyList())
    val currentDeckCards: StateFlow<List<CardEntity>> = _currentDeckCards.asStateFlow()

    // FlashcardEntity reactive list from FlashcardDao
    val allFlashcards: StateFlow<List<com.example.data.FlashcardEntity>> = (repository.getAllFlashcardsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        // First restore from offline cache if local DB was freshly initialized and empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreFromOfflineDiskBackupIfNeeded(application)
            _cacheStats.value = repository.getCacheStats(application)
        }

        // Observe decks and refresh cards
        viewModelScope.launch {
            allDecks.collect { decks ->
                if (_selectedDeck.value == null && decks.isNotEmpty()) {
                    _selectedDeck.value = decks.first()
                }
                refreshAllCards(decks)
            }
        }
    }

    private suspend fun refreshAllCards(decks: List<DeckEntity>) {
        val map = mutableMapOf<Long, List<CardEntity>>()
        for (deck in decks) {
            val list = repository.getCardsForDeck(deck.id)
            map[deck.id] = list
        }
        _cardsByDeck.value = map

        val curDeckId = _selectedDeck.value?.id
        if (curDeckId != null) {
            _currentDeckCards.value = map[curDeckId] ?: emptyList()
        }

        // Always keep the secondary offline disk backup perfectly updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncOfflineDiskBackup(getApplication())
            _cacheStats.value = repository.getCacheStats(getApplication())
        }
    }

    fun navigateTo(screen: Screen, deck: DeckEntity? = null) {
        if (deck != null) {
            _selectedDeck.value = deck
            _currentDeckCards.value = _cardsByDeck.value[deck.id] ?: emptyList()
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = Screen.DASHBOARD
    }

    fun startShuffledStudy(deck: DeckEntity) {
        _selectedDeck.value = deck
        val cards = (_cardsByDeck.value[deck.id] ?: emptyList()).shuffled()
        _currentDeckCards.value = cards
        _currentScreen.value = Screen.STUDY_FLIP
    }

    fun studyStarredCards(customCards: List<CardEntity>? = null) {
        val targetCards = customCards ?: allStarredCards.value
        if (targetCards.isNotEmpty()) {
            val virtualDeck = DeckEntity(
                id = -999L,
                name = "★ Starred Cards",
                description = "Challenging vocabulary review"
            )
            _selectedDeck.value = virtualDeck
            _currentDeckCards.value = targetCards
            _currentScreen.value = Screen.STUDY_FLIP
        }
    }

    fun quizStarredCards(customCards: List<CardEntity>? = null) {
        val targetCards = customCards ?: allStarredCards.value
        if (targetCards.isNotEmpty()) {
            val virtualDeck = DeckEntity(
                id = -999L,
                name = "★ Starred Cards",
                description = "Challenging vocabulary review"
            )
            _selectedDeck.value = virtualDeck
            _currentDeckCards.value = targetCards
            _currentScreen.value = Screen.QUIZ_GAME
        }
    }

    fun rateCard(card: CardEntity, rating: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCardSrs(card, rating)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun toggleStar(card: CardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleStar(card)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun saveCard(card: CardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (card.id == 0L) {
                repository.insertCard(card)
            } else {
                repository.updateCard(card)
            }
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun addFlashcard(
        front: String,
        back: String,
        deckName: String,
        notes: String = "",
        onSuccess: (com.example.data.FlashcardEntity) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repository.addFlashcard(front, back, deckName, notes)
            allDecks.value.let { refreshAllCards(it) }
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onSuccess(entity)
            }
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCard(card)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun createDeck(name: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertDeck(DeckEntity(name = name, description = desc))
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun updateDeck(deck: DeckEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDeck(deck)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun duplicateDeck(deck: DeckEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val cards = repository.getCardsForDeck(deck.id)
            val newDeckId = repository.insertDeck(deck.copy(id = 0, name = "${deck.name} (Copy)"))
            val newCards = cards.map { it.copy(id = 0, deckId = newDeckId) }
            repository.insertCards(newCards)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun deleteDeck(deck: DeckEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeck(deck)
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    fun onImportDone() {
        viewModelScope.launch(Dispatchers.IO) {
            allDecks.value.let { refreshAllCards(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper.shutdown()
        networkMonitor.unregister()
    }
}
