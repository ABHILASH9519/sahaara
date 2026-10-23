package main

import (
	"net/http/httptest"
	"testing"
)

func TestDemoRoutesDisabledByDefault(t *testing.T) {
	t.Setenv("SAHAARA_ENABLE_DEMO_API", "false")
	handler := newMux()

	for _, path := range []string{"/v1/dashboard", "/v1/demo/profiles", "/v1/demo/summary"} {
		t.Run(path, func(t *testing.T) {
			recorder := httptest.NewRecorder()
			handler.ServeHTTP(recorder, httptest.NewRequest("GET", path, nil))
			if recorder.Code != 404 {
				t.Fatalf("status = %d, want 404 when demo API is disabled", recorder.Code)
			}
		})
	}
}

func TestDemoRoutesEnabledExplicitly(t *testing.T) {
	t.Setenv("SAHAARA_ENABLE_DEMO_API", "true")
	handler := newMux()

	for _, path := range []string{"/v1/dashboard", "/v1/demo/profiles", "/v1/demo/summary"} {
		t.Run(path, func(t *testing.T) {
			recorder := httptest.NewRecorder()
			handler.ServeHTTP(recorder, httptest.NewRequest("GET", path, nil))
			if recorder.Code != 200 {
				t.Fatalf("status = %d, want 200 when demo API is enabled; body=%s", recorder.Code, recorder.Body.String())
			}
		})
	}
}

func TestHealthRemainsAvailableWhenDemoRoutesDisabled(t *testing.T) {
	t.Setenv("SAHAARA_ENABLE_DEMO_API", "false")
	handler := newMux()
	recorder := httptest.NewRecorder()
	handler.ServeHTTP(recorder, httptest.NewRequest("GET", "/healthz", nil))
	if recorder.Code != 200 {
		t.Fatalf("health status = %d, want 200", recorder.Code)
	}
}
