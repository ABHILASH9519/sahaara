package com.sahaara.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Forest = Color(0xFF205C4B)
private val Sage = Color(0xFFDCE9DF)
private val Ivory = Color(0xFFFAF8F2)
private val Ink = Color(0xFF24312D)

private data class ReminderItem(val id: Int, val title: String, val due: String, val done: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SahaaraApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SahaaraApp() {
    var hindi by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(3) }
    var reminders by remember {
        mutableStateOf(listOf(
            ReminderItem(1, "Review policy documents", "15 Nov 2026"),
            ReminderItem(2, "Check life-certificate guidance", "30 Nov 2026")
        ))
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = Forest, secondary = Sage, background = Ivory, surface = Color.White)) {
        Scaffold(containerColor = Ivory, topBar = {
            TopAppBar(
                title = { Column { Text("Sahaara", fontWeight = FontWeight.Bold, color = Forest); Text(if (hindi) "आपकी ज़रूरी सेवाएँ, एक जगह" else "Support for the things that matter", fontSize = 12.sp, color = Ink) } },
                actions = { TextButton(onClick = { hindi = !hindi }) { Text(if (hindi) "English" else "हिन्दी") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Ivory)
            )
        }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Forest), shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (hindi) "नमस्ते 👋" else "Namaste 👋", color = Color.White, fontSize = 15.sp)
                            Text(if (hindi) "आपकी ज़रूरी चीज़ें, व्यवस्थित और सुरक्षित।" else "Your important things, organised in one place.", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
                            Text(if (hindi) "यह एक प्रारंभिक डेमो है। कोई रिकॉर्ड सत्यापित नहीं है।" else "Preview mode · No records are verified or synced.", color = Color(0xFFE3F0E8), fontSize = 12.sp)
                        }
                    }
                }
                item { Text(if (hindi) "आपकी सेवाएँ" else "Your services", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink) }
                items(listOf(
                    Triple("▤", if (hindi) "दस्तावेज़" else "Documents & records", if (hindi) "महत्वपूर्ण कागज़ात व्यवस्थित करें" else "Keep important papers organised"),
                    Triple("₹", if (hindi) "पेंशन और पॉलिसी" else "Pension & policies", if (hindi) "अपने रिकॉर्ड एक जगह रखें" else "Keep your records together"),
                    Triple("✚", if (hindi) "स्वास्थ्य सहायता" else "Healthcare support", if (hindi) "अपॉइंटमेंट और सहायता" else "Appointments and support"),
                    Triple("♡", if (hindi) "विश्वसनीय संपर्क" else "Trusted contacts", if (hindi) "परिवार से जुड़े रहें" else "Stay connected with family")
                )) { service ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(Modifier.size(44.dp).background(Sage, CircleShape), contentAlignment = Alignment.Center) {
                                Text(service.first, color = Forest, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(service.second, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
                                Text(service.third, fontSize = 12.sp, color = Color(0xFF61716B))
                            }
                            Text("›", fontSize = 24.sp, color = Forest)
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hindi) "याद दिलाने वाली बातें" else "Reminders", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showAdd = true }) { Text(if (hindi) "+ जोड़ें" else "+ Add") }
                    }
                }
                items(reminders, key = { it.id }) { reminder ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Checkbox(checked = reminder.done, onCheckedChange = { checked ->
                                reminders = reminders.map { if (it.id == reminder.id) it.copy(done = checked) else it }
                            })
                            Column(Modifier.weight(1f)) {
                                Text(reminder.title, fontWeight = FontWeight.Medium, color = if (reminder.done) Color.Gray else Ink)
                                Text(reminder.due, fontSize = 12.sp, color = Color(0xFF61716B))
                            }
                            Text(if (reminder.done) "✓" else "•", color = Forest, fontSize = 18.sp)
                        }
                    }
                }
                item {
                    Text("Demo only: data stays in this screen's memory and resets when the app restarts. Sign-in, secure sync, uploads and official integrations are not enabled.", fontSize = 12.sp, color = Color(0xFF61716B), lineHeight = 17.sp)
                }
            }
        }
        if (showAdd) {
            AlertDialog(
                onDismissRequest = { showAdd = false },
                title = { Text(if (hindi) "नई याद दिलाने वाली बात" else "Add a reminder") },
                text = { OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text(if (hindi) "शीर्षक" else "Reminder title") }, singleLine = true) },
                confirmButton = { TextButton(onClick = {
                    val title = newTitle.trim()
                    if (title.isNotEmpty()) {
                        reminders = reminders + ReminderItem(nextId++, title, if (hindi) "तारीख जोड़ें" else "Date to be set")
                        newTitle = ""
                        showAdd = false
                    }
                }) { Text(if (hindi) "जोड़ें" else "Add") } },
                dismissButton = { TextButton(onClick = { showAdd = false }) { Text(if (hindi) "रद्द करें" else "Cancel") } }
            )
        }
    }
}
