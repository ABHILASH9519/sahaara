import SwiftUI

private enum SahaaraStyle {
    static let forest = Color(red: 32/255, green: 92/255, blue: 75/255)
    static let sage = Color(red: 220/255, green: 233/255, blue: 223/255)
    static let ivory = Color(red: 250/255, green: 248/255, blue: 242/255)
    static let ink = Color(red: 36/255, green: 49/255, blue: 45/255)
}

@main
struct SahaaraApp: App {
    var body: some Scene { WindowGroup { SahaaraHomeView() } }
}

struct Reminder: Identifiable {
    let id = UUID()
    var title: String
    var due: String
    var done = false
}

struct SahaaraHomeView: View {
    @State private var hindi = false
    @State private var showAdd = false
    @State private var newTitle = ""
    @State private var reminders = [
        Reminder(title: "Review policy documents", due: "15 Nov 2026"),
        Reminder(title: "Check life-certificate guidance", due: "30 Nov 2026")
    ]

    private let services: [(String, String, String)] = [
        ("doc.text", "Documents & records", "Keep important papers organised"),
        ("indianrupeesign.circle", "Pension & policies", "Keep your records together"),
        ("cross.case", "Healthcare support", "Appointments and support"),
        ("person.2", "Trusted contacts", "Stay connected with family")
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(hindi ? "नमस्ते 👋" : "Namaste 👋")
                            .font(.subheadline).foregroundStyle(.white.opacity(0.9))
                        Text(hindi ? "आपकी ज़रूरी चीज़ें, व्यवस्थित और सुरक्षित।" : "Your important things, organised in one place.")
                            .font(.system(size: 26, weight: .bold, design: .rounded))
                            .foregroundStyle(.white)
                        Text(hindi ? "प्रारंभिक डेमो · रिकॉर्ड सत्यापित नहीं हैं" : "Preview mode · No records are verified or synced.")
                            .font(.caption).foregroundStyle(.white.opacity(0.85))
                    }
                    .padding(22).frame(maxWidth: .infinity, alignment: .leading)
                    .background(SahaaraStyle.forest, in: RoundedRectangle(cornerRadius: 24))

                    Text(hindi ? "आपकी सेवाएँ" : "Your services")
                        .font(.title2.bold()).foregroundStyle(SahaaraStyle.ink)
                    ForEach(services, id: \.1) { service in
                        HStack(spacing: 14) {
                            Image(systemName: service.0)
                                .font(.title2).foregroundStyle(SahaaraStyle.forest)
                                .frame(width: 48, height: 48)
                                .background(SahaaraStyle.sage, in: RoundedRectangle(cornerRadius: 14))
                            VStack(alignment: .leading, spacing: 4) {
                                Text(hindi ? translated(service.1) : service.1).font(.headline)
                                Text(hindi ? translated(service.2) : service.2).font(.subheadline).foregroundStyle(.secondary)
                            }
                            Spacer()
                            Image(systemName: "chevron.right").foregroundStyle(SahaaraStyle.forest)
                        }
                        .padding(14).background(.white, in: RoundedRectangle(cornerRadius: 18))
                        .accessibilityElement(children: .combine)
                    }

                    HStack {
                        Text(hindi ? "याद दिलाने वाली बातें" : "Reminders").font(.title2.bold())
                        Spacer()
                        Button(hindi ? "+ जोड़ें" : "+ Add") { showAdd = true }
                            .font(.headline).tint(SahaaraStyle.forest)
                    }
                    ForEach($reminders) { $reminder in
                        HStack(spacing: 12) {
                            Toggle(reminder.title, isOn: $reminder.done).labelsHidden()
                                .tint(SahaaraStyle.forest)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(reminder.title).font(.headline)
                                    .strikethrough(reminder.done)
                                Text(reminder.due).font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer(minLength: 0)
                        }
                        .padding(14).background(.white, in: RoundedRectangle(cornerRadius: 16))
                    }
                    Text("Demo only: reminders stay in this screen's memory and reset when the app restarts. Sign-in, secure sync, uploads and official integrations are not enabled.")
                        .font(.footnote).foregroundStyle(.secondary)
                }
                .padding(20)
            }
            .background(SahaaraStyle.ivory.ignoresSafeArea())
            .navigationTitle("Sahaara")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(hindi ? "English" : "हिन्दी") { hindi.toggle() }
                }
            }
            .alert(hindi ? "नई याद दिलाने वाली बात" : "Add a reminder", isPresented: $showAdd) {
                TextField(hindi ? "शीर्षक" : "Reminder title", text: $newTitle)
                Button(hindi ? "रद्द करें" : "Cancel", role: .cancel) { newTitle = "" }
                Button(hindi ? "जोड़ें" : "Add") {
                    let title = newTitle.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !title.isEmpty { reminders.append(Reminder(title: title, due: hindi ? "तारीख जोड़ें" : "Date to be set")) }
                    newTitle = ""
                }
            }
        }
    }

    private func translated(_ value: String) -> String {
        switch value {
        case "Documents & records": return "दस्तावेज़"
        case "Keep important papers organised": return "महत्वपूर्ण कागज़ात व्यवस्थित करें"
        case "Pension & policies": return "पेंशन और पॉलिसी"
        case "Keep your records together": return "अपने रिकॉर्ड एक जगह रखें"
        case "Healthcare support": return "स्वास्थ्य सहायता"
        case "Appointments and support": return "अपॉइंटमेंट और सहायता"
        case "Trusted contacts": return "विश्वसनीय संपर्क"
        case "Stay connected with family": return "परिवार से जुड़े रहें"
        default: return value
        }
    }
}

#Preview { SahaaraHomeView() }
