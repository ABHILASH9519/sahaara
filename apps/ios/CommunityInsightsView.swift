import SwiftUI

private struct DemoProfilesResponse: Decodable {
    let mode: String
    let disclaimer: String
    let total: Int
    let page: Int
    let pageSize: Int
    let profiles: [DemoProfile]
}

private struct DemoProfile: Decodable, Identifiable {
    let id: String
    let name: String
    let age: Int
    let city: String
    let state: String
    let areaType: String
    let occupation: String
    let workLifestyle: String
    let experienceYears: Int
    let monthlyHouseholdIncomeINR: Int64
    let monthlyPersonalEarningsINR: Int64
    let landAreaAcres: Double
    let estimatedLandValuePerAcreINR: Int64
    let serviceCoveragePercent: Int
    let populationDensityPerSqKm: Int
    let pensioner: Bool
    let veteran: Bool
    let propertyOwner: Bool
    let digitalComfort: String
    let careSupportNeed: String
}

private enum InsightsState {
    case notConfigured
    case loading
    case loaded
    case unavailable(String)
}

struct CommunityInsightsView: View {
    @State private var profiles: [DemoProfile] = []
    @State private var state: InsightsState = .notConfigured
    @State private var query = ""
    @State private var city = ""
    @State private var minimumAge = ""
    @State private var pensionersOnly = false

    private var filtered: [DemoProfile] {
        let term = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let place = city.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let age = Int(minimumAge) ?? 0
        return profiles.filter { profile in
            let searchable = [profile.id, profile.name, profile.city, profile.state, profile.occupation].joined(separator: " ").lowercased()
            return (term.isEmpty || searchable.contains(term))
                && (place.isEmpty || profile.city.lowercased().contains(place) || profile.state.lowercased().contains(place))
                && profile.age >= age
                && (!pensionersOnly || profile.pensioner)
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                Text("Community insights")
                    .font(.title2.bold())
                Text("Synthetic demo profiles only. These are not real people or verified official, income, land-price, healthcare, pension, or coverage statistics.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                Group {
                    switch state {
                    case .notConfigured:
                        statusCard(title: "API not configured", message: "The app has no server URL configured. Configure SAHAARA_API_BASE_URL at build time to load the Go demo API.")
                    case .loading:
                        HStack(spacing: 12) {
                            ProgressView()
                            Text("Loading synthetic profiles…")
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(.white, in: RoundedRectangle(cornerRadius: 16))
                    case .loaded:
                        Text("\(profiles.count) profiles loaded · Go demo API")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.secondary)
                    case .unavailable(let message):
                        statusCard(title: "Profiles unavailable", message: message)
                        Button("Try again") { Task { await loadProfiles() } }
                            .buttonStyle(.borderedProminent)
                    }
                }
                TextField("Search name, city, state, occupation or ID", text: $query)
                    .textFieldStyle(.roundedBorder)
                    .accessibilityLabel("Search synthetic profiles")
                TextField("City or state contains…", text: $city)
                    .textFieldStyle(.roundedBorder)
                    .accessibilityLabel("Filter by city or state")
                TextField("Minimum age (optional)", text: $minimumAge)
                    .keyboardType(.numberPad)
                    .textFieldStyle(.roundedBorder)
                Toggle("Pensioner profiles only", isOn: $pensionersOnly)
                Text("\(filtered.count) matching profiles")
                    .font(.headline)
                if case .loaded = state {
                    ForEach(filtered.prefix(100)) { profile in
                        profileCard(profile)
                    }
                    if filtered.count > 100 {
                        Text("Showing the first 100 matches. Refine your search to narrow results.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .padding(20)
        }
        .background(SahaaraStyle.ivory.ignoresSafeArea())
        .task { await loadProfiles() }
    }

    private func statusCard(title: String, message: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.headline)
            Text(message).font(.subheadline).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.white, in: RoundedRectangle(cornerRadius: 16))
    }

    private func profileCard(_ profile: DemoProfile) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            Text("\(profile.name) · \(profile.age)")
                .font(.headline)
                .foregroundStyle(SahaaraStyle.ink)
            Text("\(profile.city), \(profile.state) · \(profile.areaType)")
                .font(.subheadline.weight(.medium))
                .foregroundStyle(SahaaraStyle.forest)
            Text("\(profile.occupation) · \(profile.experienceYears) years experience · \(profile.workLifestyle)")
                .font(.subheadline)
            Divider()
            Text("Household income: ₹\(profile.monthlyHouseholdIncomeINR)/month")
            Text("Personal earnings: ₹\(profile.monthlyPersonalEarningsINR)/month")
            Text("Land: \(profile.landAreaAcres, specifier: "%.2f") acres · illustrative value/acre: ₹\(profile.estimatedLandValuePerAcreINR)")
            Text("Service coverage: \(profile.serviceCoveragePercent)% · population density: \(profile.populationDensityPerSqKm)/km²")
            Text("Pensioner: \(profile.pensioner ? "Yes" : "No") · Veteran: \(profile.veteran ? "Yes" : "No") · Property owner: \(profile.propertyOwner ? "Yes" : "No")")
            Text("Digital comfort: \(profile.digitalComfort) · Care support: \(profile.careSupportNeed)")
                .font(.footnote)
            Text("SYNTHETIC DEMO — NOT A REAL PERSON")
                .font(.caption2.bold())
                .foregroundStyle(.secondary)
        }
        .font(.subheadline)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.white, in: RoundedRectangle(cornerRadius: 16))
    }

    @MainActor
    private func loadProfiles() async {
        guard let rawBase = Bundle.main.object(forInfoDictionaryKey: "SAHAARA_API_BASE_URL") as? String else {
            state = .notConfigured
            return
        }
        let base = rawBase.trimmingCharacters(in: .whitespacesAndNewlines).trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        guard let baseURL = URL(string: base), baseURL.scheme?.lowercased() == "https", baseURL.host != nil else {
            state = .unavailable("The configured API URL is invalid. Use an HTTPS base URL; no insecure HTTP endpoint is accepted.")
            return
        }
        state = .loading
        do {
            var components = URLComponents(url: baseURL.appendingPathComponent("v1/demo/profiles"), resolvingAgainstBaseURL: false)
            components?.queryItems = [URLQueryItem(name: "page", value: "1"), URLQueryItem(name: "page_size", value: "100")]
            guard let url = components?.url else { throw URLError(.badURL) }
            var request = URLRequest(url: url)
            request.httpMethod = "GET"
            request.timeoutInterval = 10
            request.setValue("application/json", forHTTPHeaderField: "Accept")
            request.setValue("no-store", forHTTPHeaderField: "Cache-Control")
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
                throw URLError(.badServerResponse)
            }
            let result = try JSONDecoder().decode(DemoProfilesResponse.self, from: data)
            guard result.mode == "synthetic_demo" else {
                throw URLError(.cannotDecodeContentData)
            }
            profiles = Array(result.profiles.prefix(100))
            guard !profiles.isEmpty else {
                state = .unavailable("The API responded but returned no profiles. Confirm the demo API is enabled and the dataset is available.")
                return
            }
            state = .loaded
        } catch {
            state = .unavailable("Could not reach the demo API or parse its response. Check network access, the server URL, and whether SAHAARA_ENABLE_DEMO_API=true is set on the server.")
        }
    }
}
