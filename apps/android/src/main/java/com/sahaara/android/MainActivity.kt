package com.sahaara.android

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var hindi by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(3) }
    var reminders by remember { mutableStateOf(listOf(ReminderItem(1,"Review policy documents","15 Nov 2026"),ReminderItem(2,"Check life-certificate guidance","30 Nov 2026"))) }
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
                    item { Text("Demo data stays on this device screen and resets on restart. No sign-in, secure sync or official integrations are enabled.",fontSize=12.sp,color=Color(0xFF61716B)) }
                }
            } else {
                ServiceDetail(selected=selected!!,onAction={ action -> selected=action },onHome={selected=null})
            }
        }
        if(showAdd) AlertDialog(onDismissRequest={showAdd=false},title={Text("Add a reminder")},text={OutlinedTextField(value=newTitle,onValueChange={newTitle=it},label={Text("Reminder title")},singleLine=true)},confirmButton={TextButton(onClick={val title=newTitle.trim();if(title.isNotEmpty()){reminders=reminders+ReminderItem(nextId++,title,"Date to be set");newTitle="";showAdd=false}}){Text("Add")}},dismissButton={TextButton(onClick={showAdd=false}){Text("Cancel")}})
    }
}

@Composable
private fun ServiceDetail(selected:String,onAction:(String)->Unit,onHome:()->Unit) {
    val title=services.firstOrNull{it.third==selected}?.first ?: selected
    val details=when(selected) {
        "Documents" -> listOf("Document categories","Identity & address (demo labels only)","Property documents","Insurance papers","Upload flow: not connected")
        "Pension" -> listOf("Pension record checklist","Life-certificate reminder","Policy renewal tracker","No government portal is connected")
        "Healthcare" -> listOf("Appointment checklist","Medicine reminder demo","Care-support preference","No medical records or provider is connected")
        "Contacts" -> listOf("Trusted contact setup preview","Consent and invitation flow not connected","Never add real contact details to this demo")
        "Property" -> listOf("Land area and indicative value fields","City and rural/urban comparisons","Synthetic values only — not market valuations")
        "Insights" -> listOf("500 synthetic Indian demo profiles","Filters: location, age, occupation, income and lifestyle","Density, land-value and coverage fields","Open the CSV dataset in the repository to inspect all rows")
        "Care" -> listOf("Care need: none / occasional / regular","Browse sample care-service categories","No booking or payment integration")
        "Opportunities" -> listOf("Occupation and experience fields","Work lifestyle and earning ranges","Synthetic profiles only — not real job listings")
        else -> listOf("Demo section")
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text(title,fontSize=25.sp,fontWeight=FontWeight.Bold,color=Forest); Text("Interactive preview · demo data only",fontSize=13.sp,color=Color.Gray) }
        items(details) { line -> Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)) { Text(line,Modifier.fillMaxWidth().padding(16.dp),color=Ink) } }
        item { Text("Available demo actions",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Ink) }
        item { Button(onClick={onAction("Insights")},modifier=Modifier.fillMaxWidth()) { Text("Explore dataset insights") } }
        item { OutlinedButton(onClick={onAction("Documents")},modifier=Modifier.fillMaxWidth()) { Text("Open documents preview") } }
        item { OutlinedButton(onClick={onAction("Pension")},modifier=Modifier.fillMaxWidth()) { Text("Open pension preview") } }
        item { TextButton(onClick=onHome,modifier=Modifier.fillMaxWidth()) { Text("Return to home") } }
        item { Text("Not production-ready: actions navigate between local demo screens. Data persistence, authentication, uploads, bookings and live government integrations are not connected.",fontSize=12.sp,color=Color(0xFF61716B)) }
    }
}
