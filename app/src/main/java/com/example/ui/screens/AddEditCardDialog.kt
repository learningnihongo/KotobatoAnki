package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.CardEntity

@Composable
fun AddEditCardDialog(
    deckId: Long,
    cardToEdit: CardEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CardEntity) -> Unit
) {
    var kanji by remember { mutableStateOf(cardToEdit?.kanji ?: "") }
    var kana by remember { mutableStateOf(cardToEdit?.kana ?: "") }
    var burmese by remember { mutableStateOf(cardToEdit?.burmeseMeaning ?: "") }
    var notes by remember { mutableStateOf(cardToEdit?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_edit_card_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (cardToEdit == null) "ကတ်အသစ်ထည့်မည် (Add Card)" else "ကတ်ပြင်ဆင်မည် (Edit Card)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = kanji,
                    onValueChange = { kanji = it },
                    label = { Text("Front / Kanji (ရှေ့မျက်နှာ ဥပမာ: 血液型)") },
                    placeholder = { Text("ဥပမာ: 血液型") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_front_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = kana,
                    onValueChange = { kana = it },
                    label = { Text("Kana / Reading (ひらがな)") },
                    placeholder = { Text("ဥပမာ: けつけきがた") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_card_kana"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = burmese,
                    onValueChange = { burmese = it },
                    label = { Text("Back / Meaning (နောက်မျက်နှာ မြန်မာအဓိပ္ပာယ်)") },
                    placeholder = { Text("ဥပမာ: သွေးအမျိုးအစား") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_back_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("မှတ်စု (Optional Notes / Example)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_card_notes"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (kana.isBlank() && kanji.isBlank()) {
                                errorMessage = "ဂျပန်စာ (Kanji သို့မဟုတ် Kana) ထည့်သွင်းပါ"
                                return@Button
                            }
                            if (burmese.isBlank()) {
                                errorMessage = "မြန်မာအဓိပ္ပာယ် ထည့်သွင်းပါ"
                                return@Button
                            }
                            val finalKanji = if (kanji.isBlank()) kana else kanji
                            val finalKana = if (kana.isBlank()) kanji else kana

                            val card = cardToEdit?.copy(
                                kanji = finalKanji,
                                kana = finalKana,
                                burmeseMeaning = burmese,
                                notes = notes
                            ) ?: CardEntity(
                                deckId = deckId,
                                kanji = finalKanji,
                                kana = finalKana,
                                burmeseMeaning = burmese,
                                notes = notes
                            )
                            onSave(card)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_card_button")
                    ) {
                        Text("သိမ်းဆည်းမည်")
                    }
                }
            }
        }
    }
}
