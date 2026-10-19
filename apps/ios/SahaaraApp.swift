import SwiftUI
@main struct SahaaraApp: App { var body: some Scene { WindowGroup { SahaaraHomeView() } } }
struct SahaaraHomeView: View {
 private let sections = ["Documents & records", "Pension and policies", "Healthcare support", "Trusted contacts", "Reminders"]
 var body: some View { NavigationStack { ScrollView { VStack(alignment: .leading, spacing: 20) { Text("Support for the things that matter.").font(.largeTitle.bold()).accessibilityAddTraits(.isHeader); Text("Your personal space").font(.title2.bold()); Text("Early preview. Sample items are not verified records."); ForEach(sections, id: \\.self) { item in VStack(alignment: .leading, spacing: 8) { Text(item).font(.title3.bold()); Text("Coming soon").foregroundStyle(.secondary) }.frame(maxWidth: .infinity, alignment: .leading).padding().background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16)) }; Text("Demo interface only — sign-in, synced data and service integrations are not available yet.").font(.footnote).foregroundStyle(.secondary) }.padding() }.navigationTitle("Sahaara") } }
}
#Preview { SahaaraHomeView() }
