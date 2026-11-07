package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"sahaara/backend/internal/httpapi"
)

func main() {
	addr := os.Getenv("SAHAARA_ADDR")
	if addr == "" {
		addr = ":8080"
	}

	mux := http.NewServeMux()
	mux.HandleFunc("GET /healthz", httpapi.Health)
	mux.HandleFunc("GET /readyz", httpapi.Ready)
	mux.HandleFunc("GET /v1/dashboard", httpapi.DemoDashboard)
	mux.HandleFunc("GET /v1/demo/profiles", httpapi.DemoProfiles)
	mux.HandleFunc("GET /v1/demo/summary", httpapi.DemoSummary)

	router := httpapi.SecurityHeaders(httpapi.Recoverer(mux))
	server := &http.Server{
		Addr:              addr,
		Handler:           router,
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       10 * time.Second,
		WriteTimeout:      15 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	shutdownSignals, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	serverErrors := make(chan error, 1)
	go func() {
		log.Printf("Sahaara API starter listening on %s (development only; auth not configured)", addr)
		serverErrors <- server.ListenAndServe()
	}()

	select {
	case err := <-serverErrors:
		if err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatal(err)
		}
	case <-shutdownSignals.Done():
		log.Println("Shutdown signal received; draining active requests")
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()

		if err := server.Shutdown(ctx); err != nil {
			log.Printf("Graceful shutdown failed: %v; forcing server close", err)
			if closeErr := server.Close(); closeErr != nil {
				log.Printf("Forced server close failed: %v", closeErr)
			}
		}

		if err := <-serverErrors; err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Printf("HTTP server stopped with error: %v", err)
		}
	}
}
