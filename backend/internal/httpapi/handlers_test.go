package httpapi

import (
	"encoding/json"
	"strings"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHealth(t *testing.T) {
	rr := httptest.NewRecorder()
	Health(rr, httptest.NewRequest(http.MethodGet, "/healthz", nil))
	if rr.Code != http.StatusOK { t.Fatalf("status=%d", rr.Code) }
	var body map[string]string
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil { t.Fatal(err) }
	if body["status"] != "ok" { t.Fatalf("body=%v", body) }
}

func TestDemoDashboardIsMarkedDemo(t *testing.T) {
	rr := httptest.NewRecorder()
	DemoDashboard(rr, httptest.NewRequest(http.MethodGet, "/v1/dashboard", nil))
	if rr.Code != http.StatusOK { t.Fatalf("status=%d", rr.Code) }
	var body map[string]any
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil { t.Fatal(err) }
	if body["mode"] != "demo" { t.Fatalf("expected demo marker, got %v", body["mode"]) }
	if body["message"] == "" { t.Fatal("expected demo disclaimer") }
}

func TestSecurityHeaders(t *testing.T) {
	h := SecurityHeaders(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) { w.WriteHeader(http.StatusNoContent) }))
	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, httptest.NewRequest(http.MethodGet, "/", nil))
	if rr.Header().Get("X-Content-Type-Options") != "nosniff" { t.Fatal("missing nosniff header") }
	if rr.Header().Get("Cache-Control") != "no-store" { t.Fatal("missing no-store header") }
	if rr.Header().Get("Referrer-Policy") != "no-referrer" { t.Fatal("missing referrer policy") }
}

func TestDemoProfilesPaginationAndFilters(t *testing.T) {
 req:=httptest.NewRequest(http.MethodGet,"/v1/demo/profiles?page=1&page_size=10",nil)
 rr:=httptest.NewRecorder(); DemoProfiles(rr,req)
 if rr.Code!=http.StatusOK { t.Fatalf("status=%d body=%s",rr.Code,rr.Body.String()) }
 var body profilesResponse
 if err:=json.Unmarshal(rr.Body.Bytes(),&body);err!=nil{t.Fatal(err)}
 if body.Total!=500 || len(body.Profiles)!=10 || body.Mode!="synthetic_demo" { t.Fatalf("unexpected response total=%d records=%d mode=%s",body.Total,len(body.Profiles),body.Mode) }
 if !strings.Contains(body.Disclaimer,"Synthetic") { t.Fatal("missing synthetic-data disclaimer") }
}
func TestDemoProfilesRejectsInvalidPagination(t *testing.T) {
 rr:=httptest.NewRecorder(); DemoProfiles(rr,httptest.NewRequest(http.MethodGet,"/v1/demo/profiles?page=0",nil))
 if rr.Code!=http.StatusBadRequest { t.Fatalf("status=%d",rr.Code) }
}
func TestDemoProfilesHandlesVeryLargePage(t *testing.T) {
	req := httptest.NewRequest(http.MethodGet, "/v1/demo/profiles?page=9223372036854775807&page_size=100", nil)
	rr := httptest.NewRecorder()
	DemoProfiles(rr, req)
	if rr.Code != http.StatusOK {
		t.Fatalf("status=%d body=%s", rr.Code, rr.Body.String())
	}
	var body profilesResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil {
		t.Fatal(err)
	}
	if len(body.Profiles) != 0 || body.Total != 500 {
		t.Fatalf("expected empty page and total 500, got records=%d total=%d", len(body.Profiles), body.Total)
	}
}

func TestDemoSummary(t *testing.T) {
 rr:=httptest.NewRecorder(); DemoSummary(rr,httptest.NewRequest(http.MethodGet,"/v1/demo/summary",nil))
 if rr.Code!=http.StatusOK { t.Fatalf("status=%d body=%s",rr.Code,rr.Body.String()) }
 var body demoSummary
 if err:=json.Unmarshal(rr.Body.Bytes(),&body);err!=nil{t.Fatal(err)}
 if body.TotalProfiles!=500 || body.States<1 || body.Cities<1 { t.Fatalf("unexpected summary: %+v",body) }
}


func TestDemoProfilesRejectsInvalidNumericFilters(t *testing.T) {
	tests := []struct {
		name string
		query string
	}{
		{name: "invalid minimum age", query: "age_min=not-a-number"},
		{name: "invalid maximum age", query: "age_max=not-a-number"},
		{name: "invalid minimum income", query: "income_min=not-a-number"},
		{name: "invalid maximum income", query: "income_max=not-a-number"},
		{name: "minimum age exceeds maximum", query: "age_min=80&age_max=60"},
		{name: "minimum income exceeds maximum", query: "income_min=50000&income_max=10000"},
	}
	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			req := httptest.NewRequest(http.MethodGet, "/v1/demo/profiles?"+tc.query, nil)
			rr := httptest.NewRecorder()
			DemoProfiles(rr, req)
			if rr.Code != http.StatusBadRequest {
				t.Fatalf("status=%d body=%s, want %d", rr.Code, rr.Body.String(), http.StatusBadRequest)
			}
			if !strings.Contains(rr.Header().Get("Content-Type"), "application/json") {
				t.Fatalf("content type = %q, want JSON", rr.Header().Get("Content-Type"))
			}
		})
	}
}

func TestDemoProfilesCapsPageSize(t *testing.T) {
	req := httptest.NewRequest(http.MethodGet, "/v1/demo/profiles?page=1&page_size=1000", nil)
	rr := httptest.NewRecorder()
	DemoProfiles(rr, req)
	if rr.Code != http.StatusOK {
		t.Fatalf("status=%d body=%s", rr.Code, rr.Body.String())
	}
	var body profilesResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil {
		t.Fatal(err)
	}
	if body.PageSize != 100 || len(body.Profiles) != 100 {
		t.Fatalf("pageSize=%d profiles=%d, want capped page size 100", body.PageSize, len(body.Profiles))
	}
}

func TestDemoProfilesAppliesAgeAndIncomeFilters(t *testing.T) {
	req := httptest.NewRequest(http.MethodGet, "/v1/demo/profiles?age_min=65&age_max=90&income_min=0&income_max=100000000&page_size=100", nil)
	rr := httptest.NewRecorder()
	DemoProfiles(rr, req)
	if rr.Code != http.StatusOK {
		t.Fatalf("status=%d body=%s", rr.Code, rr.Body.String())
	}
	var body profilesResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil {
		t.Fatal(err)
	}
	for _, p := range body.Profiles {
		if p.Age < 65 || p.Age > 90 {
			t.Fatalf("profile %s age %d outside requested range", p.ID, p.Age)
		}
		if p.MonthlyHouseholdIncomeINR < 0 || p.MonthlyHouseholdIncomeINR > 100000000 {
			t.Fatalf("profile %s income %d outside requested range", p.ID, p.MonthlyHouseholdIncomeINR)
		}
	}
}
