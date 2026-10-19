package httpapi

import (
	"encoding/json"
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
