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

struct Reminder: Identifiable, Codable, Equatable {
    var id: UUID
    var title: String
    var due: String
    var done: Bool

    init(id: UUID = UUID(), title: String, due: String, done: Bool = false) {
        self.id = id
        self.title = title
        self.due = due
        self.done = done
    }
}

struct SahaaraHomeView: View {
    @State private var hindi = false
    @State private var showAdd = false
    @State private var selectedService: String? = nil
    @State private var newTitle = ""
    @State private var newDueDate = Calendar.current.date(byAdding: .day, value: 1, to: Date()) ?? Date()
    @State private var reminders = [
        Reminder(title: "Review policy documents", due: "15 Nov 2026"),
        Reminder(title: "Check life-certificate guidance", due: "30 Nov 2026")
    ]
    private let remindersStorageKey = "sahaara.local.reminders.v1"

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
                    welcomeHeader
                    servicesSection
                    remindersSection
                    localStorageNotice
                }
                .padding(20)
            }
            .background(SahaaraStyle.ivory.ignoresSafeArea())
            .onAppear(perform: loadReminders)
            .onChange(of: reminders) { newValue in saveReminders(newValue) }
            .navigationTitle("Sahaara")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(hindi ? "English" : "हिन्दी") { hindi.toggle() }
                }
            }
            .sheet(isPresented: $showAdd) { reminderEditor }
            .sheet(isPresented: Binding(get: { selectedService != nil }, set: { if !$0 { selectedService = nil } })) { serviceDetail }
        }
    }

    private var welcomeHeader: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(hindi ? "नमस्ते 👋" : "Namaste 👋")
                .font(.subheadline).foregroundStyle(.white.opacity(0.9))
            Text(hindi ? "आपकी ज़रूरी चीज़ें, व्यवस्थित और सुरक्षित।" : "Your important things, organised in one place.")
                .font(.system(size: 26, weight: .bold, design: .rounded))
                .foregroundStyle(.white)
            Text(hindi ? "प्रारंभिक डेमो · रिकॉर्ड सत्यापित नहीं हैं" : "Preview mode · No records are verified or synced.")
                .font(.caption).foregroundStyle(.white.opacity(0.85))
        }
        .padding(22)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SahaaraStyle.forest, in: RoundedRectangle(cornerRadius: 24))
    }

    private var servicesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(hindi ? "आपकी सेवाएँ" : "Your services")
                .font(.title2.bold()).foregroundStyle(SahaaraStyle.ink)
            ForEach(services, id: \.1) { service in
                Button { selectedService = service.1 } label: {
                    HStack(spacing: 14) {
                        Image(systemName: service.0)
                            .font(.title2).foregroundStyle(SahaaraStyle.forest)
                            .frame(width: 48, height: 48)
                            .background(SahaaraStyle.sage, in: RoundedRectangle(cornerRadius: 14))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(hindi ? translated(service.1) : service.1).font(.headline)
                            Text(hindi ? translated(service.2) : service.2)
                                .font(.subheadline).foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right").foregroundStyle(SahaaraStyle.forest)
                    }
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(.white, in: RoundedRectangle(cornerRadius: 18))
                    .contentShape(RoundedRectangle(cornerRadius: 18))
                }
                .buttonStyle(.plain)
                .accessibilityHint("Opens the service preview")
            }
        }
    }

    private var serviceDetail: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Image(systemName: services.first(where: { $0.1 == selectedService })?.0 ?? "info.circle")
                    .font(.system(size: 36))
                    .foregroundStyle(SahaaraStyle.forest)
                Text(selectedService ?? "Service")
                    .font(.title2.bold())
                    .foregroundStyle(SahaaraStyle.ink)
                Text(serviceDescription(for: selectedService))
                    .font(.body)
                    .foregroundStyle(.secondary)
                Text("Preview only. No official portal, secure upload, booking, account, or external service is connected.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(SahaaraStyle.sage.opacity(0.6), in: RoundedRectangle(cornerRadius: 14))
                Spacer()
            }
            .padding(22)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(SahaaraStyle.ivory.ignoresSafeArea())
            .navigationTitle("Service preview")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Close") { selectedService = nil }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func serviceDescription(for service: String?) -> String {
        switch service {
        case "Documents & records": return "Organise a checklist of important identity, property, and insurance papers. Secure document uploads are not enabled."
        case "Pension & policies": return "Keep personal reminders for policy reviews and pension-related paperwork. No pension authority or insurer is connected."
        case "Healthcare support": return "Prepare appointment and care-support checklists. This preview does not store medical records or connect to providers."
        case "Trusted contacts": return "Preview how family support could be organised. Invitations, consent management, and contact syncing are not enabled."
        default: return "Explore this Sahaara service preview. Any sample information is illustrative and must not be treated as verified advice or official records."
        }
    }

    private var remindersSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(hindi ? "याद दिलाने वाली बातें" : "Reminders")
                    .font(.title2.bold())
                Spacer()
                Button(hindi ? "+ जोड़ें" : "+ Add") { showAdd = true }
                    .font(.headline).tint(SahaaraStyle.forest)
            }
            ForEach($reminders) { $reminder in
                HStack(spacing: 12) {
                    Toggle(reminder.title, isOn: $reminder.done).labelsHidden()
                        .tint(SahaaraStyle.forest)
                    VStack(alignment: .leading, spacing: 4) {
                        Text(reminder.title).font(.headline).strikethrough(reminder.done)
                        Text(reminder.due).font(.caption).foregroundStyle(.secondary)
                    }
                    Spacer(minLength: 0)
                }
                .padding(14)
                .background(.white, in: RoundedRectangle(cornerRadius: 16))
            }
        }
    }

    private var localStorageNotice: some View {
        Text(hindi
            ? "डेमो: रिमाइंडर केवल इस डिवाइस पर सहेजे जाते हैं।"
            : "Demo only: reminders are saved locally on this device, not synced or stored in a secure document vault. Sign-in, uploads and official integrations are not enabled.")
            .font(.footnote)
            .foregroundStyle(.secondary)
    }

    private var reminderEditor: some View {
        NavigationStack {
            Form {
                Section(hindi ? "याद दिलाने वाली बात" : "Reminder details") {
                    TextField(hindi ? "शीर्षक" : "Reminder title", text: $newTitle)
                        .textInputAutocapitalization(.sentences)
                    DatePicker(
                        hindi ? "तारीख" : "Due date",
                        selection: $newDueDate,
                        in: Calendar.current.startOfDay(for: Date())...,
                        displayedComponents: .date
                    )
                }
            }
            .navigationTitle(hindi ? "रिमाइंडर जोड़ें" : "Add reminder")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(hindi ? "रद्द करें" : "Cancel") {
                        newTitle = ""
                        showAdd = false
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(hindi ? "जोड़ें" : "Add") {
                        let title = newTitle.trimmingCharacters(in: .whitespacesAndNewlines)
                        if !title.isEmpty {
                            reminders.append(Reminder(title: title, due: formattedDate(newDueDate)))
                        }
                        newTitle = ""
                        showAdd = false
                    }
                    .disabled(newTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func formattedDate(_ date: Date) -> String {
        date.formatted(.dateTime.day().month(.abbreviated).year())
    }

    private func loadReminders() {
        guard let data = UserDefaults.standard.data(forKey: remindersStorageKey),
              let saved = try? JSONDecoder().decode([Reminder].self, from: data) else { return }
        reminders = saved
    }

    private func saveReminders(_ value: [Reminder]) {
        guard let data = try? JSONEncoder().encode(value) else { return }
        UserDefaults.standard.set(data, forKey: remindersStorageKey)
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
