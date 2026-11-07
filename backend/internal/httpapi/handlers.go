package httpapi

import (
	"embed"
	"encoding/csv"
	"encoding/json"
	"io"
	"log"
	"net/http"
	"runtime/debug"
	"strconv"
	"strings"
)

//go:embed india-500-synthetic-profiles.csv
var demoFiles embed.FS

type dashboardResponse struct {
	Mode string `json:"mode"`
	Message string `json:"message"`
	Reminders []demoReminder `json:"reminders"`
	Services []string `json:"services"`
}
type demoReminder struct { Title string `json:"title"`; Date string `json:"date"`; State string `json:"state"` }

type DemoProfile struct {
	ID string `json:"id"`
	Name string `json:"name"`
	Age int `json:"age"`
	City string `json:"city"`
	State string `json:"state"`
	AreaType string `json:"areaType"`
	Occupation string `json:"occupation"`
	WorkLifestyle string `json:"workLifestyle"`
	ExperienceYears int `json:"experienceYears"`
	MonthlyHouseholdIncomeINR int64 `json:"monthlyHouseholdIncomeINR"`
	MonthlyPersonalEarningsINR int64 `json:"monthlyPersonalEarningsINR"`
	LandAreaAcres float64 `json:"landAreaAcres"`
	EstimatedLandValuePerAcreINR int64 `json:"estimatedLandValuePerAcreINR"`
	ServiceCoveragePercent int `json:"serviceCoveragePercent"`
	PopulationDensityPerSqKm int `json:"populationDensityPerSqKm"`
	Pensioner bool `json:"pensioner"`
	Veteran bool `json:"veteran"`
	PropertyOwner bool `json:"propertyOwner"`
	DigitalComfort string `json:"digitalComfort"`
	CareSupportNeed string `json:"careSupportNeed"`
	DataLabel string `json:"dataLabel"`
}
type profilesResponse struct {
	Mode string `json:"mode"`
	Disclaimer string `json:"disclaimer"`
	Total int `json:"total"`
	Page int `json:"page"`
	PageSize int `json:"pageSize"`
	Profiles []DemoProfile `json:"profiles"`
}
type demoSummary struct {
	Mode string `json:"mode"`
	Disclaimer string `json:"disclaimer"`
	TotalProfiles int `json:"totalProfiles"`
	AverageAge float64 `json:"averageAge"`
	AverageMonthlyHouseholdIncomeINR float64 `json:"averageMonthlyHouseholdIncomeINR"`
	AverageServiceCoveragePercent float64 `json:"averageServiceCoveragePercent"`
	States int `json:"states"`
	Cities int `json:"cities"`
	Pensioners int `json:"pensioners"`
	Veterans int `json:"veterans"`
	PropertyOwners int `json:"propertyOwners"`
}

const demoDisclaimer = "Synthetic demonstration data only. These profiles do not represent real people or verified official, income, land-price, health, pension or coverage statistics."

func Health(w http.ResponseWriter, _ *http.Request) { writeJSON(w, http.StatusOK, map[string]string{"status":"ok"}) }
func Ready(w http.ResponseWriter, _ *http.Request) { writeJSON(w, http.StatusOK, map[string]string{"status":"ready", "checks":"process-only"}) }
func DemoDashboard(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, dashboardResponse{Mode:"demo", Message:demoDisclaimer, Reminders:[]demoReminder{{Title:"Review your policy documents", Date:"2026-11-15", State:"sample"},{Title:"Check life-certificate guidance", Date:"2026-11-30", State:"sample"}}, Services:[]string{"Documents", "Pension records", "Policies", "Healthcare", "Trusted contacts"}})
}

func DemoProfiles(w http.ResponseWriter, r *http.Request) {
	profiles, err := loadDemoProfiles()
	if err != nil { writeJSON(w, http.StatusInternalServerError, map[string]string{"error":"demo_dataset_unavailable"}); return }
	q := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("q")))
	city := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("city")))
	state := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("state")))
	area := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("area_type")))
	occupation := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("occupation")))
	if len(q) > 120 || len(city) > 80 || len(state) > 80 || len(area) > 40 || len(occupation) > 80 {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error":"filter_value_too_long"})
		return
	}
	minAge, err := queryInt(r, "age_min", 0); if err != nil { writeJSON(w, http.StatusBadRequest, map[string]string{"error":"invalid_age_min"}); return }
	maxAge, err := queryInt(r, "age_max", 150); if err != nil { writeJSON(w, http.StatusBadRequest, map[string]string{"error":"invalid_age_max"}); return }
	minIncome, err := queryInt64(r, "income_min", 0); if err != nil { writeJSON(w, http.StatusBadRequest, map[string]string{"error":"invalid_income_min"}); return }
	maxIncome, err := queryInt64(r, "income_max", int64(^uint64(0)>>1)); if err != nil { writeJSON(w, http.StatusBadRequest, map[string]string{"error":"invalid_income_max"}); return }
	if minAge < 0 || maxAge > 150 || minIncome < 0 || maxIncome < 0 {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error":"filter_range_out_of_bounds"})
		return
	}
	if minAge > maxAge || minIncome > maxIncome { writeJSON(w, http.StatusBadRequest, map[string]string{"error":"minimum_must_not_exceed_maximum"}); return }
	filtered := make([]DemoProfile, 0, len(profiles))
	for _, p := range profiles {
		haystack := strings.ToLower(strings.Join([]string{p.ID,p.Name,p.City,p.State,p.Occupation,p.WorkLifestyle}, " "))
		if q != "" && !strings.Contains(haystack,q) { continue }
		if city != "" && !strings.Contains(strings.ToLower(p.City),city) { continue }
		if state != "" && !strings.Contains(strings.ToLower(p.State),state) { continue }
		if area != "" && !strings.EqualFold(p.AreaType,area) { continue }
		if occupation != "" && !strings.Contains(strings.ToLower(p.Occupation),occupation) { continue }
		if p.Age < minAge || p.Age > maxAge { continue }
		if p.MonthlyHouseholdIncomeINR < minIncome || p.MonthlyHouseholdIncomeINR > maxIncome { continue }
		filtered = append(filtered,p)
	}
	page, err := queryInt(r,"page",1); if err != nil || page < 1 { writeJSON(w,http.StatusBadRequest,map[string]string{"error":"page_must_be_positive_integer"}); return }
	pageSize, err := queryInt(r,"page_size",25); if err != nil || pageSize < 1 { writeJSON(w,http.StatusBadRequest,map[string]string{"error":"page_size_must_be_positive_integer"}); return }
	if pageSize > 100 { pageSize=100 }
	// Clamp before multiplying so a very large page number cannot overflow int
	// and produce a negative slice index.
	start := len(filtered)
	lastPage := (len(filtered) + pageSize - 1) / pageSize
	if page <= lastPage {
		start = (page - 1) * pageSize
	}
	end := start + pageSize
	if end > len(filtered) { end = len(filtered) }
	writeJSON(w,http.StatusOK,profilesResponse{Mode:"synthetic_demo",Disclaimer:demoDisclaimer,Total:len(filtered),Page:page,PageSize:pageSize,Profiles:filtered[start:end]})
}

func DemoSummary(w http.ResponseWriter, _ *http.Request) {
	profiles, err := loadDemoProfiles()
	if err != nil { writeJSON(w,http.StatusInternalServerError,map[string]string{"error":"demo_dataset_unavailable"}); return }
	s := demoSummary{Mode:"synthetic_demo",Disclaimer:demoDisclaimer,TotalProfiles:len(profiles)}
	states,cities:=map[string]bool{},map[string]bool{}
	for _,p:=range profiles {
		s.AverageAge += float64(p.Age)
		s.AverageMonthlyHouseholdIncomeINR += float64(p.MonthlyHouseholdIncomeINR)
		s.AverageServiceCoveragePercent += float64(p.ServiceCoveragePercent)
		states[p.State]=true; cities[p.City]=true
		if p.Pensioner { s.Pensioners++ }; if p.Veteran { s.Veterans++ }; if p.PropertyOwner { s.PropertyOwners++ }
	}
	if len(profiles)>0 { n:=float64(len(profiles)); s.AverageAge/=n; s.AverageMonthlyHouseholdIncomeINR/=n; s.AverageServiceCoveragePercent/=n }
	s.States=len(states); s.Cities=len(cities)
	writeJSON(w,http.StatusOK,s)
}

func loadDemoProfiles() ([]DemoProfile,error) {
	f,err:=demoFiles.Open("india-500-synthetic-profiles.csv"); if err!=nil{return nil,err}; defer f.Close()
	reader:=csv.NewReader(f); header,err:=reader.Read(); if err!=nil{return nil,err}
	index:=map[string]int{}; for i,h:=range header { index[strings.TrimSpace(h)]=i }
	get:=func(row []string,name string) string { i,ok:=index[name]; if !ok||i>=len(row){return ""}; return strings.TrimSpace(row[i]) }
	parseInt:=func(s string)(int,error){v,e:=strconv.Atoi(s);return v,e}
	parseInt64:=func(s string)(int64,error){v,e:=strconv.ParseInt(s,10,64);return v,e}
	parseFloat:=func(s string)(float64,error){v,e:=strconv.ParseFloat(s,64);return v,e}
	parseBool:=func(s string)bool{return strings.EqualFold(s,"true")}
	out:=make([]DemoProfile,0,500)
	for { row,e:=reader.Read(); if e==io.EOF {break}; if e!=nil{return nil,e}
		age,e:=parseInt(get(row,"Age"));if e!=nil{return nil,e}
		exp,e:=parseInt(get(row,"Experience Years"));if e!=nil{return nil,e}
		house,e:=parseInt64(get(row,"Monthly Household Income INR"));if e!=nil{return nil,e}
		earn,e:=parseInt64(get(row,"Monthly Personal Earnings INR"));if e!=nil{return nil,e}
		land,e:=parseFloat(get(row,"Land Area Acres"));if e!=nil{return nil,e}
		landValue,e:=parseInt64(get(row,"Estimated Land Value Per Acre INR"));if e!=nil{return nil,e}
		coverage,e:=parseInt(get(row,"Service Coverage Percent"));if e!=nil{return nil,e}
		density,e:=parseInt(get(row,"Population Density Per Sq Km"));if e!=nil{return nil,e}
		out=append(out,DemoProfile{ID:get(row,"Profile ID"),Name:get(row,"Display Name"),Age:age,City:get(row,"City"),State:get(row,"State"),AreaType:get(row,"Area Type"),Occupation:get(row,"Occupation"),WorkLifestyle:get(row,"Work Lifestyle"),ExperienceYears:exp,MonthlyHouseholdIncomeINR:house,MonthlyPersonalEarningsINR:earn,LandAreaAcres:land,EstimatedLandValuePerAcreINR:landValue,ServiceCoveragePercent:coverage,PopulationDensityPerSqKm:density,Pensioner:parseBool(get(row,"Pensioner")),Veteran:parseBool(get(row,"Veteran")),PropertyOwner:parseBool(get(row,"Property Owner")),DigitalComfort:get(row,"Digital Comfort"),CareSupportNeed:get(row,"Care Support Need"),DataLabel:get(row,"Data Label")})
	}
	return out,nil
}
func queryInt(r *http.Request,key string,def int)(int,error){v:=r.URL.Query().Get(key);if v==""{return def,nil};return strconv.Atoi(v)}
func queryInt64(r *http.Request,key string,def int64)(int64,error){v:=r.URL.Query().Get(key);if v==""{return def,nil};return strconv.ParseInt(v,10,64)}

func Recoverer(next http.Handler) http.Handler { return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { defer func(){ if recovered := recover(); recovered != nil { log.Printf("panic recovered: %v\\n%s", recovered, debug.Stack()); http.Error(w, `{"error":"internal_error"}`, http.StatusInternalServerError) } }(); next.ServeHTTP(w,r) }) }
func SecurityHeaders(next http.Handler) http.Handler { return http.HandlerFunc(func(w http.ResponseWriter,r *http.Request){ w.Header().Set("X-Content-Type-Options","nosniff"); w.Header().Set("Cache-Control","no-store"); w.Header().Set("Referrer-Policy","no-referrer"); next.ServeHTTP(w,r) }) }
func writeJSON(w http.ResponseWriter, status int, value any) { w.Header().Set("Content-Type","application/json; charset=utf-8"); w.WriteHeader(status); if err := json.NewEncoder(w).Encode(value); err != nil { log.Printf("write response: %v",err) } }
