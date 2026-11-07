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


func TestDemoProfilesRejectsOutOfRangeFilters(t *testing.T) {
	tests := []struct {
		name string
		query string
	}{
		{name: "negative minimum age", query: "age_min=-1"},
		{name: "age above supported range", query: "age_max=151"},
		{name: "negative income", query: "income_min=-1"},
		{name: "negative maximum income", query: "income_max=-1"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			rr := httptest.NewRecorder()
			DemoProfiles(rr, httptest.NewRequest(http.MethodGet, "/v1/demo/profiles?"+tt.query, nil))
			if rr.Code != http.StatusBadRequest {
				t.Fatalf("status=%d body=%s", rr.Code, rr.Body.String())
			}
		})
	}
}

func TestDemoProfilesRejectsOversizedTextFilters(t *testing.T) {
	query := "/v1/demo/profiles?q=" + strings.Repeat("a", 121)
	rr := httptest.NewRecorder()
	DemoProfiles(rr, httptest.NewRequest(http.MethodGet, query, nil))
	if rr.Code != http.StatusBadRequest {
		t.Fatalf("status=%d body=%s", rr.Code, rr.Body.String())
	}
}
