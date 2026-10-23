package main

import (
	"log"
	"net/http"
	"os"
	"time"

	"sahaara/backend/internal/httpapi"
)

func main() {
	addr := os.Getenv("SAHAARA_ADDR")
	if addr == "" { addr = ":8080" }
	mux := http.NewServeMux()
	mux.HandleFunc("GET /healthz", httpapi.Health)
	mux.HandleFunc("GET /readyz", httpapi.Ready)
	// Demo endpoints expose synthetic records and are disabled by default to prevent
	// accidental exposure when this starter is deployed. Enable only in a trusted
	// development environment; this is not a substitute for production auth.
	if os.Getenv("SAHAARA_ENABLE_DEMO_API") == "true" {
		mux.HandleFunc("GET /v1/dashboard", httpapi.DemoDashboard)
		mux.HandleFunc("GET /v1/demo/profiles", httpapi.DemoProfiles)
		mux.HandleFunc("GET /v1/demo/summary", httpapi.DemoSummary)
	}
	router := httpapi.SecurityHeaders(httpapi.Recoverer(mux))
	server := &http.Server{Addr: addr, Handler: router, ReadHeaderTimeout: 5*time.Second, ReadTimeout: 10*time.Second, WriteTimeout: 15*time.Second, IdleTimeout: 60*time.Second}
	log.Printf("Sahaara API starter listening on %s (development only; auth not configured; demo API enabled=%t)", addr, os.Getenv("SAHAARA_ENABLE_DEMO_API") == "true")
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed { log.Fatal(err) }
}
