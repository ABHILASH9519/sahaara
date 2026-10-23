package main

import (
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"context"
	"time"

	"sahaara/backend/internal/httpapi"
)

func main() {
	addr := os.Getenv("SAHAARA_ADDR")
	if addr == "" { addr = ":8080" }
	router := httpapi.SecurityHeaders(httpapi.Recoverer(newMux()))
	server := &http.Server{Addr: addr, Handler: router, ReadHeaderTimeout: 5*time.Second, ReadTimeout: 10*time.Second, WriteTimeout: 15*time.Second, IdleTimeout: 60*time.Second}
	log.Printf("Sahaara API starter listening on %s (development only; auth not configured; demo API enabled=%t)", addr, os.Getenv("SAHAARA_ENABLE_DEMO_API") == "true")
	serverErrors := make(chan error, 1)
	go func() {
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			serverErrors <- err
		}
	}()

	stop, stopSignal := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stopSignal()
	select {
	case err := <-serverErrors:
		log.Fatalf("API server failed: %v", err)
	case <-stop.Done():
		log.Printf("Shutdown signal received; draining active requests")
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := server.Shutdown(ctx); err != nil {
		log.Printf("Graceful shutdown timed out: %v", err)
		if closeErr := server.Close(); closeErr != nil {
			log.Printf("Force close failed: %v", closeErr)
		}
	}
}


// newMux keeps route registration testable without starting a listening server.
func newMux() *http.ServeMux {
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
	return mux
}
