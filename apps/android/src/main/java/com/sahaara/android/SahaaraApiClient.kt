package com.sahaara.android

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Optional adapter for the Go synthetic-demo API.
 *
 * No endpoint is configured by default. An endpoint must be supplied at build
 * time with -PSAHAARA_API_BASE_URL=https://your-host. Non-HTTPS endpoints are
 * rejected except localhost/Android emulator loopback for local development.
 */
internal object SahaaraApiClient {
    private const val PAGE_SIZE = 100
    private const val MAX_PROFILES = 500

    fun configuredBaseUrl(): String? {
        val configured = BuildConfig.SAHAARA_API_BASE_URL.trim().trimEnd('/')
        if (configured.isEmpty()) return null
        val uri = runCatching { Uri.parse(configured) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()
        val localHost = host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2"
        if (host.isNullOrBlank() || (scheme != "https" && !(scheme == "http" && localHost))) return null
        return configured
    }

    suspend fun loadProfiles(): List<DemoProfile>? = withContext(Dispatchers.IO) {
        val baseUrl = configuredBaseUrl() ?: return@withContext null
        val first = getPage(baseUrl, 1) ?: return@withContext null
        val result = first.first.toMutableList()
        val total = first.second.coerceAtMost(MAX_PROFILES)
        val pageCount = ((total + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
        for (page in 2..pageCount) {
            val next = getPage(baseUrl, page) ?: return@withContext null
            result.addAll(next.first)
        }
        result.take(MAX_PROFILES)
    }

    private fun getPage(baseUrl: String, page: Int): Pair<List<DemoProfile>, Int>? {
        val connection = runCatching {
            URL("$baseUrl/v1/demo/profiles?page=$page&page_size=$PAGE_SIZE").openConnection() as HttpURLConnection
        }.getOrNull() ?: return null
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 4_000
            connection.readTimeout = 6_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Cache-Control", "no-store")
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            if (root.optString("mode") != "synthetic_demo") return null
            val array = root.optJSONArray("profiles") ?: return null
            val profiles = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    if (id.isBlank()) continue
                    add(
                        DemoProfile(
                            id = id,
                            name = item.optString("name"),
                            age = item.optInt("age"),
                            city = item.optString("city"),
                            state = item.optString("state"),
                            area = item.optString("areaType"),
                            occupation = item.optString("occupation"),
                            lifestyle = item.optString("workLifestyle"),
                            experience = item.optInt("experienceYears"),
                            householdIncome = item.optLong("monthlyHouseholdIncomeINR"),
                            personalEarnings = item.optLong("monthlyPersonalEarningsINR"),
                            landAcres = item.optDouble("landAreaAcres"),
                            landValuePerAcre = item.optLong("estimatedLandValuePerAcreINR"),
                            coverage = item.optInt("serviceCoveragePercent"),
                            density = item.optInt("populationDensityPerSqKm"),
                            pensioner = item.optBoolean("pensioner"),
                            veteran = item.optBoolean("veteran"),
                            propertyOwner = item.optBoolean("propertyOwner"),
                            digitalComfort = item.optString("digitalComfort"),
                            careNeed = item.optString("careSupportNeed")
                        )
                    )
                }
            }
            profiles to root.optInt("total", profiles.size)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
