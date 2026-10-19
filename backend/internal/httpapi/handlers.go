package httpapi

import (
	"encoding/json"
	"log"
	"net/http"
	"runtime/debug"
)

type dashboardResponse struct {
	Mode string `json:"mode"`
	Message string `json:"message"`
	Reminders []demoReminder `json:"reminders"`
	Services []string `json:"services"`
}
type demoReminder struct { Title string `json:"title"`; Date string `json:"date"`; State string `json:"state"` }

func Health(w http.ResponseWriter, _ *http.Request) { writeJSON(w, http.StatusOK, map[string]string{"status":"ok"}) }
func Ready(w http.ResponseWriter, _ *http.Request) { writeJSON(w, http.StatusOK, map[string]string{"status":"ready", "checks":"process-only"}) }
func DemoDashboard(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, dashboardResponse{Mode:"demo", Message:"This is sample content, not verified pension or government information.", Reminders:[]demoReminder{{Title:"Review your policy documents", Date:"2026-11-15", State:"sample"},{Title:"Check life-certificate guidance", Date:"2026-11-30", State:"sample"}}, Services:[]string{"Documents", "Pension records", "Policies", "Healthcare", "Trusted contacts"}})
}
func Recoverer(next http.Handler) http.Handler { return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { defer func(){ if recovered := recover(); recovered != nil { log.Printf("panic recovered: %v\\n%s", recovered, debug.Stack()); http.Error(w, `{"error":"internal_error"}`, http.StatusInternalServerError) } }(); next.ServeHTTP(w,r) }) }
func SecurityHeaders(next http.Handler) http.Handler { return http.HandlerFunc(func(w http.ResponseWriter,r *http.Request){ w.Header().Set("X-Content-Type-Options","nosniff"); w.Header().Set("Cache-Control","no-store"); w.Header().Set("Referrer-Policy","no-referrer"); next.ServeHTTP(w,r) }) }
func writeJSON(w http.ResponseWriter, status int, value any) { w.Header().Set("Content-Type","application/json; charset=utf-8"); w.WriteHeader(status); if err := json.NewEncoder(w).Encode(value); err != nil { log.Printf("write response: %v",err) } }
