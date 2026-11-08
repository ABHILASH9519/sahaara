package httpapi

import "testing"

func TestEmbeddedDemoProfilesHaveStableUniqueIDsAndValidRanges(t *testing.T) {
	profiles, err := loadDemoProfiles()
	if err != nil {
		t.Fatalf("load embedded demo profiles: %v", err)
	}
	if len(profiles) != 500 {
		t.Fatalf("expected 500 synthetic profiles, got %d", len(profiles))
	}

	seen := make(map[string]struct{}, len(profiles))
	for _, profile := range profiles {
		if profile.ID == "" {
			t.Fatal("profile ID must not be empty")
		}
		if _, exists := seen[profile.ID]; exists {
			t.Fatalf("duplicate profile ID %q", profile.ID)
		}
		seen[profile.ID] = struct{}{}

		if profile.Age < 0 || profile.Age > 150 {
			t.Errorf("profile %s has out-of-range age %d", profile.ID, profile.Age)
		}
		if profile.ServiceCoveragePercent < 0 || profile.ServiceCoveragePercent > 100 {
			t.Errorf("profile %s has out-of-range service coverage %d", profile.ID, profile.ServiceCoveragePercent)
		}
		if profile.MonthlyHouseholdIncomeINR < 0 || profile.MonthlyPersonalEarningsINR < 0 {
			t.Errorf("profile %s has negative income values", profile.ID)
		}
		if profile.LandAreaAcres < 0 || profile.EstimatedLandValuePerAcreINR < 0 {
			t.Errorf("profile %s has negative land values", profile.ID)
		}
	}
}
