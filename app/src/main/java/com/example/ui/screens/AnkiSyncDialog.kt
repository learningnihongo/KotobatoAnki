package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun AnkiSyncDialog(
    deck: DeckEntity,
    cards: List<CardEntity>,
    repository: FlashcardRepository,
    onDismiss: () -> Unit,
    onImportSuccess: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for importing .apkg or .txt
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                try {
                    val (newDeck, count) = withContext(Dispatchers.IO) {
                        repository.importFromUri(context, uri)
                    }
                    isLoading = false
                    Toast.makeText(context, "Anki မှ ${count} ကတ် ထည့်သွင်းပြီးပါပြီ!", Toast.LENGTH_LONG).show()
                    onImportSuccess(newDeck.name, count)
                    onDismiss()
                } catch (e: Exception) {
                    isLoading = false
                    statusMessage = "ထည့်သွင်းမှု မအောင်မြင်ပါ: ${e.localizedMessage}"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("anki_sync_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Anki ထဲသို့ ချိတ်ဆက်ခြင်း",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Anki .apkg & TSV Format Support",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("anki_dialog_close")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Deck summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${cards.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = deck.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "စုစုပေါင်း ဝေါဟာရ ${cards.size} ခု",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ဖိုင်ထုတ်လုပ် / ဖတ်ရှုနေပါသည်...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Export (Anki ထဲသို့ ထုတ်ယူရန်)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Action 1: Export .apkg and Share
                AnkiOptionCard(
                    title = "Anki .apkg ဖိုင် ထုတ်ယူ / မျှဝေရန်",
                    subtitle = "AnkiDroid သို့မဟုတ် Anki Desktop သို့ တိုက်ရိုက် သွင်းနိုင်သည် (.apkg)",
                    icon = Icons.Default.Share,
                    tag = "export_apkg_button",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                val apkgFile = withContext(Dispatchers.IO) {
                                    repository.exportToApkg(context, deck, cards)
                                }
                                isLoading = false
                                shareApkgFile(context, apkgFile, deck.name)
                            } catch (e: Exception) {
                                isLoading = false
                                statusMessage = "Export မအောင်မြင်ပါ: ${e.localizedMessage}"
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Export .txt file
                AnkiOptionCard(
                    title = "Anki Text (.txt) ဖိုင် ထုတ်ယူ / မျှဝေရန်",
                    subtitle = "AnkiDroid Import ထဲတွင် .txt ဖိုင် ရွေးချယ်ပြီး သွင်းနိုင်သည်",
                    icon = Icons.Default.FileDownload,
                    tag = "export_txt_button",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                val txtFile = withContext(Dispatchers.IO) {
                                    repository.exportToTextFile(context, cards, deck.name)
                                }
                                isLoading = false
                                shareTextFile(context, txtFile, deck.name)
                            } catch (e: Exception) {
                                isLoading = false
                                statusMessage = "Text Export မအောင်မြင်ပါ: ${e.localizedMessage}"
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 3: Export CSV file
                AnkiOptionCard(
                    title = "CSV (.csv) ဖိုင် ထုတ်ယူ / မျှဝေရန်",
                    subtitle = "Excel, Google Sheets သို့မဟုတ် အခြား App များတွင် အလွယ်တကူ ဖွင့်နိုင်သည်",
                    icon = Icons.Default.Share,
                    tag = "export_csv_button",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                val csvFile = withContext(Dispatchers.IO) {
                                    repository.exportToCsvFile(context, cards, deck.name)
                                }
                                isLoading = false
                                shareCsvFile(context, csvFile, deck.name)
                            } catch (e: Exception) {
                                isLoading = false
                                statusMessage = "CSV Export မအောင်မြင်ပါ: ${e.localizedMessage}"
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 4: Copy TSV for Anki Quick Import
                AnkiOptionCard(
                    title = "Anki Text (TSV) ကူးယူရန်",
                    subtitle = "Clipboard သို့ ကူးယူပြီး Anki ထဲတွင် ချက်ချင်း Paste လုပ်နိုင်သည်",
                    icon = Icons.Default.ContentCopy,
                    tag = "copy_tsv_button",
                    onClick = {
                        val tsvText = repository.exportToTsv(cards)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Anki Cards", tsvText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Anki TSV စာသားကို Copy ကူးပြီးပါပြီ!", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Import (ဖိုင်ထည့်သွင်းခြင်းနှင့် လမ်းညွှန်)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Action 5: Import from Anki .apkg, .csv, or .txt
                AnkiOptionCard(
                    title = "TXT / CSV / Anki (.apkg) ဖိုင် သွင်းရန်",
                    subtitle = "ဖုန်းထဲရှိ Text (.txt), CSV (.csv) သို့မဟုတ် Anki (.apkg) ဖိုင်ကို ရွေးချယ်ပါ",
                    icon = Icons.Default.FileUpload,
                    tag = "import_apkg_button",
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 6: Format Guide Dialog
                var showFormatGuide by remember { mutableStateOf(false) }
                if (showFormatGuide) {
                    FormatGuideDialog(
                        onDismiss = { showFormatGuide = false },
                        onImportClick = {
                            showFormatGuide = false
                            importLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }

                AnkiOptionCard(
                    title = "TXT & CSV ရေးသားနည်း လမ်းညွှန် (Format Guide)",
                    subtitle = "Excel သို့မဟုတ် Notepad တွင် ဖိုင်ရေးသားနည်းနှင့် နမူနာများ ကြည့်ရန်",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    tag = "show_format_guide_button",
                    onClick = {
                        showFormatGuide = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tip Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "လမ်းညွှန်: Anki .apkg ဖိုင်ကို Export ပြီးပါက AnkiDroid App ဖြင့် တိုက်ရိုက်ဖွင့်နိုင်ပါသည်။ Flashcard ၏ Back View တွင် Hiragana အသံထွက်နှင့် မြန်မာအဓိပ္ပာယ်ကို ရှင်းလင်းစွာ တွဲလျက် ပြသပေးမည် ဖြစ်ပါသည်။",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnkiOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shareApkgFile(context: Context, file: File, deckName: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$deckName (Anki Deck)")
            putExtra(Intent.EXTRA_TEXT, "Here is the Anki deck: $deckName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Anki .apkg မျှဝေရန်")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing .apkg: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun shareTextFile(context: Context, file: File, deckName: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$deckName (Anki TSV)")
            putExtra(Intent.EXTRA_TEXT, file.readText())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Anki Text (.txt) မျှဝေရန်")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing .txt: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun shareCsvFile(context: Context, file: File, deckName: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$deckName (CSV Cards)")
            putExtra(Intent.EXTRA_TEXT, file.readText())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "CSV (.csv) ဖိုင် မျှဝေရန်")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing .csv: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
