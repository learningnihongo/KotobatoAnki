package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File

/**
 * Format Guide Dialog explaining how to structure Text (.txt) and CSV (.csv) files
 * for importing flashcards with Japanese Kanji, Hiragana reading, and Myanmar meaning.
 */
@Composable
fun FormatGuideDialog(
    onDismiss: () -> Unit,
    onImportClick: () -> Unit
) {
    val context = LocalContext.current

    val sampleCsvContent = """
        Kanji,Kana,BurmeseMeaning,Notes
        食べる,たべる,စားသည်,N5 Group 1 verb
        飲む,のむ,သောက်သည်,Group 1 verb
        勉強,べんきょう,စာကျက်ခြင်း / လေ့လာခြင်း,Noun / Suru verb
        日本語,にほんご,ဂျပန်ဘာသာစကား,Language
        ありがとう,ありがとう,ကျေးဇူးတင်ပါသည်,Greeting
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("format_guide_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TXT & CSV ရေးသားနည်း",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Flashcard Input Format Guide",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("format_guide_close")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Introduction text
                Text(
                    text = "သင်၏ ဖုန်းထဲရှိ Text (.txt) သို့မဟုတ် CSV (.csv) ဖိုင်မှတစ်ဆင့် ဝေါဟာရများကို တစ်ပြိုင်နက် App ထဲသို့ သွင်းယူနိုင်ပါသည်။ Excel, Google Sheets သို့မဟုတ် Notepad တွင် အောက်ပါအတိုင်း ပြင်ဆင်နိုင်ပါသည်။",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Format 1: 3-Column CSV
                FormatCard(
                    badge = "စံပြု ပုံစံ ၁ (အသုံးများဆုံး)",
                    title = "၃ ကော်လံ ပုံစံ (Kanji, Kana, Meaning)",
                    description = "ကော်လံ (၃) ခုဖြင့် စာလုံး၊ အသံထွက် နှင့် မြန်မာအဓိပ္ပာယ်ကို သီးခြားစီ သတ်မှတ်ခြင်း ဖြစ်ပါသည်။",
                    syntax = "Kanji, အသံထွက်(Hiragana), မြန်မာအဓိပ္ပာယ်",
                    examples = listOf(
                        "食べる,たべる,စားသည်",
                        "飲む,のむ,သောက်သည်",
                        "勉強,べんきょう,စာကျက်ခြင်း"
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Format 2: 4-Column CSV
                FormatCard(
                    badge = "စံပြု ပုံစံ ၂ (မှတ်စုအပါအဝင်)",
                    title = "၄ ကော်လံ ပုံစံ (Kanji, Kana, Meaning, Notes)",
                    description = "နောက်ဆုံးကော်လံတွင် အပိုမှတ်စု (ဥပမာ N5 Verb, ဥပမာဝါကျ) ထည့်သွင်းနိုင်ပါသည်။",
                    syntax = "Kanji, အသံထွက်, မြန်မာအဓိပ္ပာယ်, မှတ်စု",
                    examples = listOf(
                        "日本語,にほんご,ဂျပန်စကား,JLPT N5",
                        "先生,せんせい,ဆရာ/ဆရာမ,Honorific"
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Format 3: Simple 2-Column
                FormatCard(
                    badge = "စံပြု ပုံစံ ၃ (ရိုးရှင်း ပုံစံ)",
                    title = "၂ ကော်လံ ပုံစံ (Word, Meaning)",
                    description = "Kanji မပါသော စကားလုံးများ သို့မဟုတ် Bracket ဖြင့် တွဲထားသော ပုံစံဖြစ်ပါသည်။",
                    syntax = "စကားလုံး, မြန်မာအဓိပ္ပာယ်",
                    examples = listOf(
                        "ありがとう,ကျေးဇူးတင်ပါသည်",
                        "先生 [せんせい],ဆရာ / ဆရာမ"
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sample Code Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "နမူနာ CSV စာသား (Sample Template)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Sample Flashcards CSV", sampleCsvContent)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "နမူနာ Template ကို Copy ကူးယူပြီးပါပြီ!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Template",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = sampleCsvContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Copy Template & Share Sample File & Import Now
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Sample Flashcards CSV", sampleCsvContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "နမူနာ CSV ကို ကူးယူပြီးပါပြီ!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy နမူနာ", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            shareSampleCsv(context, sampleCsvContent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ဖိုင်ထုတ်ယူ", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Primary Import Button
                Button(
                    onClick = {
                        onDismiss()
                        onImportClick()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("format_guide_import_now")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TXT / CSV ဖိုင် ရွေးချယ်ပြီး သွင်းမည်",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatCard(
    badge: String,
    title: String,
    description: String,
    syntax: String,
    examples: List<String>
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Syntax Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "ဖွဲ့စည်းပုံ: $syntax",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "နမူနာများ:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            examples.forEach { example ->
                Text(
                    text = "• $example",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun shareSampleCsv(context: Context, content: String) {
    try {
        val sampleFile = File(context.cacheDir, "sample_nihongo_cards.csv")
        sampleFile.writeText(content, Charsets.UTF_8)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            sampleFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sample Nihongo Flashcards CSV")
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "နမူနာ CSV ဖိုင် မျှဝေ / သိမ်းဆည်းရန်")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing sample: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
