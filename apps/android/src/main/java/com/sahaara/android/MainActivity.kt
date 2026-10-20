package com.sahaara.android

import android.os.Bundle
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Forest = Color(0xFF205C4B)
private val Sage = Color(0xFFDCE9DF)
private val Ivory = Color(0xFFFAF8F2)
private val Ink = Color(0xFF24312D)
private val services = listOf(
    Triple("Documents & records","Keep important papers organised","Documents"),
    Triple("Pension & policies","Track records and important dates","Pension"),
    Triple("Healthcare support","Appointments and care options","Healthcare"),
    Triple("Trusted contacts","Family and support network","Contacts"),
    Triple("Property & land","Explore property record fields","Property"),
    Triple("Community insights","Explore 500 synthetic profiles","Insights"),
    Triple("Care services","Browse care-support demo options","Care"),
    Triple("Opportunities","Explore work and experience profiles","Opportunities")
)
private data class ReminderItem(val id:Int,val title:String,val due:String,val done:Boolean=false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { SahaaraApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SahaaraApp() {
    val context = LocalContext.current
    var hindi by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDueMillis by remember { mutableStateOf(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(3) }
    var reminders by remember { mutableStateOf(listOf(ReminderItem(1,"Review policy documents","15 Nov 2026"),ReminderItem(2,"Check life-certificate guidance","30 Nov 2026"))) }
    var remindersLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("sahaara_local", Context.MODE_PRIVATE)
        val saved = prefs.getString("reminders_json", null)
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                reminders = (0 until array.length()).map { index ->
                    val item = array.getJSONObject(index)
                    ReminderItem(item.getInt("id"), item.getString("title"), item.getString("due"), item.optBoolean("done", false))
                }
                nextId = (reminders.maxOfOrNull { it.id } ?: 0) + 1
            } catch (_: Exception) { /* Keep the safe built-in demo reminders if local data is unreadable. */ }
        }
        remindersLoaded = true
    }
    LaunchedEffect(reminders, remindersLoaded) {
        if (remindersLoaded) {
            val array = JSONArray()
            reminders.forEach { item -> array.put(JSONObject().put("id", item.id).put("title", item.title).put("due", item.due).put("done", item.done)) }
            context.getSharedPreferences("sahaara_local", Context.MODE_PRIVATE).edit().putString("reminders_json", array.toString()).apply()
        }
    }
    MaterialTheme(colorScheme=lightColorScheme(primary=Forest,secondary=Sage,background=Ivory,surface=Color.White)) {
        Scaffold(containerColor=Ivory,topBar={
            TopAppBar(
                navigationIcon={ if(selected!=null) TextButton(onClick={selected=null}) { Text("‹ Back",color=Forest) } },
                title={ Column { Text("Sahaara",fontWeight=FontWeight.Bold,color=Forest); Text(if(hindi) "आपकी ज़रूरी सेवाएँ, एक जगह" else "Support for the things that matter",fontSize=12.sp,color=Ink) } },
                actions={ TextButton(onClick={hindi=!hindi}) { Text(if(hindi) "English" else "हिन्दी") } },
                colors=TopAppBarDefaults.topAppBarColors(containerColor=Ivory)
            )
        }) { padding ->
            if(selected==null) {
                LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    item { Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(if(hindi) "नमस्ते" else "Namaste",color=Color.White,fontSize=15.sp)
                        Text(if(hindi) "आपकी ज़रूरी चीज़ें, व्यवस्थित रखें।" else "Your important things, organised in one place.",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold,lineHeight=29.sp)
                        Text("DEMO MODE · 500 synthetic profiles · No real people or verified records",color=Color(0xFFE3F0E8),fontSize=12.sp)
                    } } }
                    item { Text(if(hindi) "आपकी सेवाएँ" else "Explore services",fontSize=21.sp,fontWeight=FontWeight.Bold,color=Ink) }
                    items(services) { service ->
                        Card(Modifier.fillMaxWidth().clickable { selected=service.third },colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                                Box(Modifier.size(44.dp).background(Sage,CircleShape),contentAlignment=Alignment.Center) { Text(service.first.take(1),color=Forest,fontSize=20.sp,fontWeight=FontWeight.Bold) }
                                Column(Modifier.weight(1f)) { Text(service.first,fontWeight=FontWeight.SemiBold,fontSize=16.sp,color=Ink); Text(service.second,fontSize=12.sp,color=Color(0xFF61716B)) }
                                Text("›",fontSize=24.sp,color=Forest)
                            }
                        }
                    }
                    item { Row(verticalAlignment=Alignment.CenterVertically) { Text("Reminders",fontSize=21.sp,fontWeight=FontWeight.Bold,color=Ink,modifier=Modifier.weight(1f)); TextButton(onClick={showAdd=true}) { Text("+ Add") } } }
                    items(reminders,key={it.id}) { reminder ->
                        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                                Checkbox(checked=reminder.done,onCheckedChange={checked->reminders=reminders.map{if(it.id==reminder.id)it.copy(done=checked) else it}})
                                Column(Modifier.weight(1f)) { Text(reminder.title,color=if(reminder.done)Color.Gray else Ink); Text(reminder.due,fontSize=12.sp,color=Color.Gray) }
                            }
                        }
                    }
                    item { Text("Demo reminders are saved locally on this device. No sign-in, secure sync or official integrations are enabled.",fontSize=12.sp,color=Color(0xFF61716B)) }
                }
            } else {
                ServiceDetail(selected=selected!!,onAction={ action -> selected=action },onHome={selected=null})
            }
        }
        if(showAdd) AlertDialog(
            onDismissRequest={showAdd=false},
            title={Text("Add a reminder")},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value=newTitle,onValueChange={newTitle=it},label={Text("Reminder title")},singleLine=true)
                    OutlinedButton(onClick={showDatePicker=true},modifier=Modifier.fillMaxWidth()) {
                        Text("Due date: ${Instant.ofEpochMilli(newDueMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                    }
                }
            },
            confirmButton={TextButton(onClick={
                val title=newTitle.trim()
                if(title.isNotEmpty()) {
                    val due=Instant.ofEpochMilli(newDueMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    reminders=reminders+ReminderItem(nextId++,title,due)
                    newTitle=""
                    showAdd=false
                }
            }){Text("Add")}},
            dismissButton={TextButton(onClick={showAdd=false}){Text("Cancel")}}
        )
        if(showDatePicker) {
            val dateState = rememberDatePickerState(initialSelectedDateMillis=newDueMillis)
            DatePickerDialog(
                onDismissRequest={showDatePicker=false},
                confirmButton={TextButton(onClick={
                    dateState.selectedDateMillis?.let { newDueMillis=it }
                    showDatePicker=false
                }){Text("Set date")}},
                dismissButton={TextButton(onClick={showDatePicker=false}){Text("Cancel")}}
            ) { DatePicker(state=dateState) }
        }
    }
}

internal data class DemoProfile(
    val id:String, val name:String, val age:Int, val city:String, val state:String,
    val area:String, val occupation:String, val lifestyle:String, val experience:Int,
    val householdIncome:Long, val personalEarnings:Long, val landAcres:Double,
    val landValuePerAcre:Long, val coverage:Int, val density:Int,
    val pensioner:Boolean, val veteran:Boolean, val propertyOwner:Boolean,
    val digitalComfort:String, val careNeed:String
)

@Composable
private fun ServiceDetail(selected:String,onAction:(String)->Unit,onHome:()->Unit) {
    val context = LocalContext.current
    val title=services.firstOrNull{it.third==selected}?.first ?: selected
    val details=when(selected) {
        "Documents" -> listOf("Document categories","Identity & address (demo labels only)","Property documents","Insurance papers","Upload flow: not connected")
        "Pension" -> listOf("Pension record checklist","Life-certificate reminder","Policy renewal tracker","No government portal is connected")
        "Healthcare" -> listOf("Appointment checklist","Medicine reminder demo","Care-support preference","No medical records or provider is connected")
        "Contacts" -> listOf("Trusted contact setup preview","Consent and invitation flow not connected","Never add real contact details to this demo")
        "Property" -> listOf("Land area and indicative value fields","City and rural/urban comparisons","Synthetic values only — not market valuations")
        "Insights" -> listOf("500 synthetic Indian demo profiles","Search by name, city, state or occupation","Filter by minimum age","Income, land, coverage and density fields")
        "Care" -> listOf("Care need: none / occasional / regular","Browse sample care-service categories","No booking or payment integration")
        "Opportunities" -> listOf("Occupation and experience fields","Work lifestyle and earning ranges","Synthetic profiles only — not real job listings")
        else -> listOf("Demo section")
    }
    var profiles by remember { mutableStateOf<List<DemoProfile>>(emptyList()) }
    var loadError by remember { mutableStateOf(false) }
    var dataSource by remember { mutableStateOf("Loading demo profiles…") }
    var query by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("") }
    var minAge by remember { mutableStateOf("") }
    var pensionersOnly by remember { mutableStateOf(false) }
    LaunchedEffect(selected) {
        if (selected == "Insights") {
            loadError = false
            dataSource = "Loading demo profiles…"
            val remoteProfiles = SahaaraApiClient.loadProfiles()
            if (remoteProfiles != null && remoteProfiles.isNotEmpty()) {
                profiles = remoteProfiles
                dataSource = "Go API · synthetic demo data"
            } else {
                try {
                    profiles = context.assets.open("demo-profiles.csv").bufferedReader().useLines { lines ->
                        lines.drop(1).mapNotNull { line ->
                            val f = line.split(",")
                            if (f.size < 20) null else try {
                                DemoProfile(
                                    f[0], f[1], f[2].toInt(), f[3], f[4], f[5], f[6], f[7],
                                    f[8].toInt(), f[9].toLong(), f[10].toLong(),
                                    f[11].toDouble(), f[12].toLong(), f[13].toInt(), f[14].toInt(),
                                    f[15].equals("true", true), f[16].equals("true", true),
                                    f[17].equals("true", true), f[18], f[19]
                                )
                            } catch (_: Exception) { null }
                        }.toList()
                    }
                    dataSource = if (SahaaraApiClient.configuredBaseUrl() == null) {
                        "Offline dataset · endpoint not configured"
                    } else {
                        "Offline dataset · API unavailable, invalid, or returned no profiles"
                    }
                    loadError = profiles.isEmpty()
                } catch (_: Exception) {
                    loadError = true
                    dataSource = "Profile data unavailable"
                }
            }
        }
    }
    val filtered = remember(profiles, query, cityFilter, minAge, pensionersOnly) {
        val q = query.trim().lowercase()
        val city = cityFilter.trim().lowercase()
        val age = minAge.toIntOrNull() ?: 0
        profiles.filter { p ->
            (q.isEmpty() || listOf(p.name,p.city,p.state,p.occupation,p.id).any { it.lowercase().contains(q) }) &&
            (city.isEmpty() || p.city.lowercase().contains(city) || p.state.lowercase().contains(city)) &&
            p.age >= age && (!pensionersOnly || p.pensioner)
        }
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text(title,fontSize=25.sp,fontWeight=FontWeight.Bold,color=Forest); Text("Interactive preview · demo data only",fontSize=13.sp,color=Color.Gray) }
        if (selected == "Insights") {
            item {
                Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text("Offline profile explorer",color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold)
                        Text(if(loadError) "Dataset could not be loaded. Reinstall the latest build." else "${profiles.size} synthetic profiles · $dataSource",color=Color.White,fontSize=14.sp)
                        Text("Illustrative values only. Not real people, verified statistics, land valuations or eligibility data.",color=Color(0xFFE3F0E8),fontSize=12.sp)
                    }
                }
            }
            item { OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth(),label={Text("Search name, city, state, occupation or ID")},singleLine=true) }
            item { OutlinedTextField(value=cityFilter,onValueChange={cityFilter=it},modifier=Modifier.fillMaxWidth(),label={Text("City or state contains…")},singleLine=true) }
            item { OutlinedTextField(value=minAge,onValueChange={minAge=it.filter(Char::isDigit).take(3)},modifier=Modifier.fillMaxWidth(),label={Text("Minimum age (optional)")},singleLine=true) }
            item { Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(checked=pensionersOnly,onCheckedChange={pensionersOnly=it}); Text("Pensioner profiles only",color=Ink) } }
            item { Text("${filtered.size} matching profiles",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Forest) }
            item {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Pensioners" to filtered.count { it.pensioner }.toString(),
                        "Veterans" to filtered.count { it.veteran }.toString(),
                        "Property owners" to filtered.count { it.propertyOwner }.toString()
                    ).forEach { (label, value) ->
                        Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=Sage),shape=RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(vertical=12.dp,horizontal=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(value,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Forest)
                                Text(label,fontSize=10.sp,color=Ink)
                            }
                        }
                    }
                }
            }
            if (loadError) item { Text("No profile data available.",color=Color(0xFF9B2C2C)) }
            items(filtered.take(100),key={it.id}) { p ->
                Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text(p.name + " · " + p.age, fontSize=17.sp, fontWeight=FontWeight.Bold, color=Ink)
                        Text(p.city + ", " + p.state + " · " + p.area, color=Forest, fontSize=13.sp)
                        Text(p.occupation + " · " + p.experience + " years experience · " + p.lifestyle, color=Ink, fontSize=13.sp)
                        Divider(color=Sage)
                        Text("Household income: ₹${p.householdIncome}/month",color=Ink,fontSize=13.sp)
                        Text("Personal earnings: ₹${p.personalEarnings}/month",color=Ink,fontSize=13.sp)
                        Text("Land: ${p.landAcres} acres · illustrative value/acre: ₹${p.landValuePerAcre}",color=Ink,fontSize=13.sp)
                        Text("Service coverage: ${p.coverage}% · population density: ${p.density}/km²",color=Ink,fontSize=13.sp)
                        Text("Pensioner: ${if(p.pensioner) "Yes" else "No"} · Veteran: ${if(p.veteran) "Yes" else "No"} · Property owner: ${if(p.propertyOwner) "Yes" else "No"}",color=Ink,fontSize=12.sp)
                        Text("Digital comfort: ${p.digitalComfort} · Care support: ${p.careNeed}",color=Ink,fontSize=12.sp)
                        Text("SYNTHETIC DEMO — NOT A REAL PERSON",color=Color(0xFF61716B),fontSize=10.sp,fontWeight=FontWeight.Bold)
                    }
                }
            }
            if(filtered.size > 100) item { Text("Showing first 100 matches. Refine your search to narrow results.",fontSize=12.sp,color=Color.Gray) }
        } else {
            items(details) { line -> Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)) { Text(line,Modifier.fillMaxWidth().padding(16.dp),color=Ink) } }
            item { Text("Available demo actions",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Ink) }
            item { Button(onClick={onAction("Insights")},modifier=Modifier.fillMaxWidth()) { Text("Explore dataset insights") } }
            item { OutlinedButton(onClick={onAction("Documents")},modifier=Modifier.fillMaxWidth()) { Text("Open documents preview") } }
            item { OutlinedButton(onClick={onAction("Pension")},modifier=Modifier.fillMaxWidth()) { Text("Open pension preview") } }
        }
        item { TextButton(onClick=onHome,modifier=Modifier.fillMaxWidth()) { Text("Return to home") } }
        item { Text("Local demo only: no sign-in, secure sync, uploads, bookings, live government integrations or verified financial data are connected.",fontSize=12.sp,color=Color(0xFF61716B)) }
    }
}
