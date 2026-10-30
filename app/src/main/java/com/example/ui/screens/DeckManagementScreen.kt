package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoodBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeckSortOrder {
    NAME_ASC,
    CARD_COUNT_DESC,
    DATE_CREATED_DESC
}

@Composable
fun DeckManagementScreen(
    decks: List<DeckEntity>,
    cardsByDeck: Map<Long, List<CardEntity>>,
    onStudyDeck: (DeckEntity) -> Unit,
    onViewDeckCards: (DeckEntity) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onUpdateDeck: (DeckEntity) -> Unit,
    onDuplicateDeck: (DeckEntity) -> Unit,
    onDeleteDeck: (DeckEntity) -> Unit,
    onAnkiExport: (DeckEntity, List<CardEntity>) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(DeckSortOrder.DATE_CREATED_DESC) }

    // Dialog States
    var showCreateDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<DeckEntity?>(null) }
    var deckToDelete by remember { mutableStateOf<DeckEntity?>(null) }

    val filteredAndSortedDecks = remember(decks, cardsByDeck, searchQuery, sortOrder) {
        val filtered = if (searchQuery.isBlank()) {
            decks
        } else {
            val q = searchQuery.trim().lowercase()
            decks.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }

        when (sortOrder) {
            DeckSortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            DeckSortOrder.CARD_COUNT_DESC -> filtered.sortedByDescending { cardsByDeck[it.id]?.size ?: 0 }
            DeckSortOrder.DATE_CREATED_DESC -> filtered.sortedByDescending { it.createdAt }
        }
    }

    val totalDecks = decks.size
    val totalCards = cardsByDeck.values.sumOf { it.size }
    val totalMastered = cardsByDeck.values.flatten().count { it.srsLevel == 3 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("deck_management_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("deck_manage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Decks စီမံခန့်ခွဲခြင်း",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "View, Edit & Create Flashcard Decks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("deck_manage_add_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Deck",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Deck အမည် သို့မဟုတ် ရှင်းလင်းချက် ရှာဖွေပါ...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deck_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sorting Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = sortOrder == DeckSortOrder.DATE_CREATED_DESC,
                        onClick = { sortOrder = DeckSortOrder.DATE_CREATED_DESC },
                        label = { Text("အသစ်ဆုံး (Recent)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = sortOrder == DeckSortOrder.CARD_COUNT_DESC,
                        onClick = { sortOrder = DeckSortOrder.CARD_COUNT_DESC },
                        label = { Text("ကတ်အများဆုံး (Cards)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = sortOrder == DeckSortOrder.NAME_ASC,
                        onClick = { sortOrder = DeckSortOrder.NAME_ASC },
                        label = { Text("အမည် (A-Z)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Stats Pill Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalDecks",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Decks စုစုပေါင်း",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCards",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "ကတ်စုစုပေါင်း",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val masteryPercent = if (totalCards > 0) (totalMastered * 100) / totalCards else 0
                        Text(
                            text = "$masteryPercent%",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EasyGreen
                        )
                        Text(
                            text = "ကျက်ပြီးနှုန်း",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Decks List
            if (filteredAndSortedDecks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "ရှာဖွေမှုနှင့် ကိုက်ညီသော Deck မရှိပါ" else "Deck မရှိသေးပါ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deck အသစ် ဖန်တီးမည်")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSortedDecks, key = { it.id }) { deck ->
                        val cards = cardsByDeck[deck.id] ?: emptyList()
                        DeckManagerCardItem(
                            deck = deck,
                            cards = cards,
                            onStudy = { onStudyDeck(deck) },
                            onViewCards = { onViewDeckCards(deck) },
                            onEdit = { deckToEdit = deck },
                            onDuplicate = { onDuplicateDeck(deck) },
                            onDelete = { deckToDelete = deck },
                            onAnkiExport = { onAnkiExport(deck, cards) },
                            canDelete = decks.size > 1
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }

        // Floating Action Button to Create New Deck
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_deck_fab"),
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Deck")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Deck",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Create Deck Dialog
    if (showCreateDialog) {
        CreateOrEditDeckDialog(
            initialDeck = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreateDeck(name, desc)
                showCreateDialog = false
            }
        )
    }

    // Edit Deck Dialog
    deckToEdit?.let { deck ->
        CreateOrEditDeckDialog(
            initialDeck = deck,
            onDismiss = { deckToEdit = null },
            onConfirm = { name, desc ->
                onUpdateDeck(deck.copy(name = name, description = desc))
                deckToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    deckToDelete?.let { deck ->
        val cardCount = cardsByDeck[deck.id]?.size ?: 0
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = {
                Text(
                    text = "Deck အား ဖျက်မည်လား?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "\"${deck.name}\" နှင့် ၎င်းထဲရှိ ကတ် ${cardCount} ခုစလုံးကို အပြီးတိုင် ဖျက်ပစ်ပါမည်။ ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ရယူနိုင်မည် မဟုတ်ပါ။"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDeck(deck)
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ဖျက်မည်")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("မဖျက်ပါ")
                }
            }
        )
    }
}

@Composable
private fun DeckManagerCardItem(
    deck: DeckEntity,
    cards: List<CardEntity>,
    onStudy: () -> Unit,
    onViewCards: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAnkiExport: () -> Unit,
    canDelete: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    val masteredCount = cards.count { it.srsLevel == 3 }
    val learningCount = cards.count { it.srsLevel in 1..2 }
    val newCount = cards.count { it.srsLevel == 0 }

    val dateFormatted = remember(deck.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(deck.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_manager_item_${deck.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Title, Date, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Created $dateFormatted",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("deck_options_menu_${deck.id}")
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ပြင်ဆင်မည် (Edit)") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("ကူးယူပွားမည် (Duplicate)") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Anki သို့ Export (.apkg)") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAnkiExport()
                            }
                        )

                        if (canDelete) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "ဖျက်မည် (Delete)",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            if (deck.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress & Stats Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cards.size} Cards",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (masteredCount > 0) {
                            Text(
                                text = "★ $masteredCount Mastered",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = EasyGreen
                            )
                        }
                        if (learningCount > 0) {
                            Text(
                                text = "• $learningCount Learning",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GoodBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val progressFraction = if (cards.isNotEmpty()) {
                    masteredCount.toFloat() / cards.size.toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EasyGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Study Button
                Button(
                    onClick = onStudy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("manage_deck_study_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("လေ့ကျင့်မည်")
                }

                // View Cards in Deck Button
                OutlinedButton(
                    onClick = onViewCards,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("manage_deck_cards_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ကတ်များ")
                }

                // Quick Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("manage_deck_edit_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Deck",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CreateOrEditDeckDialog(
    initialDeck: DeckEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(initialDeck?.name ?: "") }
    var desc by remember { mutableStateOf(initialDeck?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val presetTemplates = listOf(
        "JLPT N3 Vocabulary",
        "JLPT N2 Vocabulary",
        "Kanji Mastery",
        "Daily Conversation",
        "Travel Japanese"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_edit_deck_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (initialDeck == null) "Deck အသစ် ဖန်တီးမည်" else "Deck အား ပြင်ဆင်မည်",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (initialDeck == null) "Create a new flashcard deck" else "Update deck title & description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (error != null) error = null
                    },
                    label = { Text("Deck အမည် (Name) *") },
                    placeholder = { Text("ဥပမာ: JLPT N2 Kanji") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = error != null
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("ရှင်းလင်းချက် (Description)") },
                    placeholder = { Text("ဥပမာ: Essential words and phrases") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_desc_input"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )

                if (initialDeck == null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "အကြံပြုခေါင်းစဉ်များ (Suggested Names):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetTemplates) { template ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    name = template
                                    if (desc.isBlank()) {
                                        desc = "Collection of $template cards."
                                    }
                                }
                            ) {
                                Text(
                                    text = template,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("မလုပ်တော့ပါ")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                error = "Deck အမည် ထည့်သွင်းပေးပါ"
                                return@Button
                            }
                            onConfirm(name.trim(), desc.trim())
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_deck_button")
                    ) {
                        Text(if (initialDeck == null) "ဖန်တီးမည်" else "သိမ်းဆည်းမည်")
                    }
                }
            }
        }
    }
}
