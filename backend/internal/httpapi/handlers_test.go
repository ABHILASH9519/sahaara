package httpapi

import (
	"encoding/json"
	"net/http/httptest"
	"testing"
)
func TestHealth(t *testing.T) { rr:=httptest.NewRecorder(); Health(rr,httptest.NewRequest("GET","/healthz",nil)); if rr.Code != 200 { t.Fatalf("status=%d",rr.Code) }; var body map[string]string; if err:=json.Unmarshal(rr.Body.Bytes(),&body); err!=nil { t.Fatal(err) }; if body["status"]!="ok" { t.Fatalf("body=%v",body) } }
func TestDemoDashboardIsMarkedDemo(t *testing.T) { rr:=httptest.NewRecorder(); DemoDashboard(rr,httptest.NewRequest("GET","/v1/dashboard",nil)); if rr.Code != 200 { t.Fatalf("status=%d",rr.Code) }; var body map[string]any; if err:=json.Unmarshal(rr.Body.Bytes(),&body); err!=nil { t.Fatal(err) }; if body["mode"]!="demo" { t.Fatalf("expected demo marker, got %v",body["mode"]) } }
func TestSecurityHeaders(t *testing.T) { h:=SecurityHeaders(httptest.HandlerFunc(func(w http.ResponseWriter,_ *http.Request){w.WriteHeader(204)})); rr:=httptest.NewRecorder(); h.ServeHTTP(rr,httptest.NewRequest("GET","/",nil)); if rr.Header().Get("X-Content-Type-Options")!="nosniff" { t.Fatal("missing nosniff header") }; if rr.Header().Get("Cache-Control")!="no-store" { t.Fatal("missing no-store header") } }
