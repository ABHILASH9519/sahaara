package com.sahaara.android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { Scaffold { padding -> Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Sahaara", fontSize = 32.sp); Text("Support for the things that matter.", fontSize = 20.sp); Text("Your personal space", style = MaterialTheme.typography.titleLarge); Text("Early preview. Sample items are not verified records."); listOf("Documents & records", "Pension and policies", "Healthcare support", "Trusted contacts", "Reminders").forEach { label -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text(label, fontSize = 20.sp); Text("Coming soon") } } }; Text("Demo interface only — sign-in, synced data and service integrations are not available yet.") } } } } } }
