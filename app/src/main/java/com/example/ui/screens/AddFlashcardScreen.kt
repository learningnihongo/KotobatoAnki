package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeckEntity
import com.example.data.FlashcardEntity
import com.example.ui.theme.EasyGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Compose Input Form Screen for adding new flashcards (Front/Back)
 * and persisting them directly to the Room database via the DAO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlashcardScreen(
    decks: List<DeckEntity>,
    selectedDeck: DeckEntity?,
    onSaveFlashcard: (front: String, back: String, deckName: String, notes: String, onDone: () -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var selectedDeckName by remember {
        mutableStateOf(selectedDeck?.name ?: decks.firstOrNull()?.name ?: "2015/12 JLPT N3 Vocabulary")
    }

    var frontError by remember { mutableStateOf<String?>(null) }
    var backError by remember { mutableStateOf<String?>(null) }
    var isFrontFlipped by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var savedCardSummary by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (frontText.isBlank()) {
            frontError = "ရှေ့မျက်နှာစာသား ထည့်သွင်းပါ (Front text cannot be empty)"
            isValid = false
        } else {
            frontError = null
        }

        if (backText.isBlank()) {
            backError = "နောက်မျက်နှာ အဓိပ္ပာယ် ထည့်သွင်းပါ (Back text cannot be empty)"
            isValid = false
        } else {
            backError = null
        }

        return isValid
    }

    fun handleSave(clearAfter: Boolean, closeAfter: Boolean) {
        if (!validate()) return

        val frontToSave = frontText
        val backToSave = backText
        val deckToSave = selectedDeckName
        val notesToSave = notesText

        onSaveFlashcard(frontToSave, backToSave, deckToSave, notesToSave) {
            savedCardSummary = "“$frontToSave” saved to Room database"
            showSuccessMessage = true

            if (clearAfter) {
                frontText = ""
                backText = ""
                notesText = ""
                frontError = null
                backError = null
            }

            coroutineScope.launch {
                delay(2500)
                showSuccessMessage = false
                if (closeAfter) {
                    onBack()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "ကတ်အသစ်ထည့်မည်",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Add Flashcard (Room DAO)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("add_card_form_scrollable"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Feedback Banner
            item {
                AnimatedVisibility(
                    visible = showSuccessMessage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EasyGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EasyGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_success_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = EasyGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ကတ်ကို Room Database ထဲသို့ အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                savedCardSummary?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Deck Selector Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ကတ်အုပ်စု (Target Deck)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = selectedDeckName,
                            onValueChange = { selectedDeckName = it },
                            label = { Text("Deck Name (အုပ်စုအမည်)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_deck_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (decks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "လက်ရှိ အုပ်စုများမှ ရွေးချယ်ပါ:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(decks) { deck ->
                                    FilterChip(
                                        selected = selectedDeckName == deck.name,
                                        onClick = { selectedDeckName = deck.name },
                                        label = { Text(deck.name) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card Input Fields (Front & Back)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "ကတ်အချက်အလက်များ ထည့်သွင်းပါ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Compose TextFields via Room FlashcardDao",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Front TextField
                        OutlinedTextField(
                            value = frontText,
                            onValueChange = {
                                frontText = it
                                if (frontError != null) frontError = null
                            },
                            label = { Text("Front (ရှေ့မျက်နှာ - ဂျပန်စာ / မေးခွန်း / Prompt)") },
                            placeholder = { Text("ဥပမာ: 血液型 (けつけきがた) သို့မဟုတ် Question") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_front_input"),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 2,
                            isError = frontError != null,
                            supportingText = {
                                if (frontError != null) {
                                    Text(
                                        text = frontError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.testTag("front_error_text")
                                    )
                                } else {
                                    Text("ကတ်၏ အရှေ့ဘက်တွင် ပြသမည့် စာသား")
                                }
                            },
                            trailingIcon = {
                                if (frontText.isNotEmpty()) {
                                    IconButton(onClick = { frontText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear front")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Back TextField
                        OutlinedTextField(
                            value = backText,
                            onValueChange = {
                                backText = it
                                if (backError != null) backError = null
                            },
                            label = { Text("Back (နောက်မျက်နှာ - မြန်မာအဓိပ္ပာယ် / အဖြေ / Answer)") },
                            placeholder = { Text("ဥပမာ: သွေးအမျိုးအစား (Blood type)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_back_input"),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 3,
                            isError = backError != null,
                            supportingText = {
                                if (backError != null) {
                                    Text(
                                        text = backError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.testTag("back_error_text")
                                    )
                                } else {
                                    Text("ကတ်ကို လှန်လိုက်လျှင် ပြသမည့် အဖြေနှင့် အဓိပ္ပာယ်")
                                }
                            },
                            trailingIcon = {
                                if (backText.isNotEmpty()) {
                                    IconButton(onClick = { backText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear back")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Notes TextField (Optional)
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("မှတ်စု / ဥပမာဝါကျ (Notes / Example - Optional)") },
                            placeholder = { Text("ဥပမာ: 血液型は何ですか？ (သင့်သွေးအမျိုးအစားက ဘာလဲ)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_notes_input"),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 1,
                            singleLine = false
                        )
                    }
                }
            }

            // Live Card Preview
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ကတ်ပုံစံ အကြိုကြည့်ရှုရန် (Live Preview)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedButton(
                                onClick = { isFrontFlipped = !isFrontFlipped },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Flip preview",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFrontFlipped) "Front ကြည့်ရန်" else "Back ကြည့်ရန်")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Mini Preview Card
                        Surface(
                            onClick = { isFrontFlipped = !isFrontFlipped },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_live_preview")
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isFrontFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isFrontFlipped) "BACK (နောက်မျက်နှာ)" else "FRONT (ရှေ့မျက်နှာ)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isFrontFlipped) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val displayText = if (isFrontFlipped) {
                                    backText.ifBlank { "အဓိပ္ပာယ် ဤနေရာတွင် ပေါ်မည်..." }
                                } else {
                                    frontText.ifBlank { "မေးခွန်း / စာလုံး ဤနေရာတွင် ပေါ်မည်..." }
                                }

                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (isFrontFlipped && notesText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = notesText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "👆 ကတ်ကိုနှိပ်၍ ရှေ့/နောက် လှန်ကြည့်နိုင်ပါသည်",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main Save Button
                    Button(
                        onClick = { handleSave(clearAfter = false, closeAfter = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_card_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ကတ်သိမ်းဆည်းမည် (Save to Room)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Save & Add Another Button
                    OutlinedButton(
                        onClick = { handleSave(clearAfter = true, closeAfter = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_add_another_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "သိမ်းပြီး နောက်ထပ်ကတ်ထည့်မည် (Save & Add Another)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_card_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("မလုပ်တော့ပါ (Cancel)")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
