package com.griffith.luckywheel.utils

// Utility to convert country names to flag emojis
object CountryFlags {
    
    // Convert country name to flag emoji
    fun getFlag(countryName: String): String {
        // Get country code from name, then convert to flag emoji
        val countryCode = countryNameToCode[countryName.lowercase()] ?: return "🌍"
        return countryCodeToEmoji(countryCode)
    }
    
    // Convert ISO country code to flag emoji
    fun getFlagFromCode(countryCode: String): String {
        if (countryCode.length != 2) return "🌍"
        return countryCodeToEmoji(countryCode.uppercase())
    }
    
    // Convert two-letter country code to flag emoji
    // Flag emojis are created using Regional Indicator Symbols (U+1F1E6 to U+1F1FF)
    private fun countryCodeToEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌍"
        
        val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
    
    // Get country code from country name
    fun getCountryCodeFromName(countryName: String): String {
        return countryNameToCode[countryName.lowercase()] ?: "XX"
    }
    
    // Map of common country names to ISO codes
    private val countryNameToCode = mapOf(
        "united states" to "US",
        "usa" to "US",
        "united kingdom" to "GB",
        "uk" to "GB",
        "canada" to "CA",
        "australia" to "AU",
        "germany" to "DE",
        "france" to "FR",
        "italy" to "IT",
        "spain" to "ES",
        "japan" to "JP",
        "china" to "CN",
        "india" to "IN",
        "brazil" to "BR",
        "mexico" to "MX",
        "russia" to "RU",
        "south korea" to "KR",
        "netherlands" to "NL",
        "sweden" to "SE",
        "norway" to "NO",
        "denmark" to "DK",
        "finland" to "FI",
        "poland" to "PL",
        "belgium" to "BE",
        "switzerland" to "CH",
        "austria" to "AT",
        "portugal" to "PT",
        "greece" to "GR",
        "ireland" to "IE",
        "new zealand" to "NZ",
        "singapore" to "SG",
        "south africa" to "ZA",
        "argentina" to "AR",
        "chile" to "CL",
        "colombia" to "CO",
        "peru" to "PE",
        "venezuela" to "VE",
        "egypt" to "EG",
        "turkey" to "TR",
        "saudi arabia" to "SA",
        "united arab emirates" to "AE",
        "uae" to "AE",
        "israel" to "IL",
        "thailand" to "TH",
        "vietnam" to "VN",
        "philippines" to "PH",
        "indonesia" to "ID",
        "malaysia" to "MY",
        "pakistan" to "PK",
        "bangladesh" to "BD",
        "nigeria" to "NG",
        "kenya" to "KE",
        "ethiopia" to "ET",
        "ghana" to "GH",
        "ukraine" to "UA",
        "czech republic" to "CZ",
        "hungary" to "HU",
        "romania" to "RO",
        "croatia" to "HR",
        "serbia" to "RS",
        "bulgaria" to "BG",
        "slovakia" to "SK",
        "slovenia" to "SI",
        "lithuania" to "LT",
        "latvia" to "LV",
        "estonia" to "EE"
    )
    
    // Get list of all countries with flags for dropdown
    fun getAllCountries(): List<Pair<String, String>> {
        return countryNameToCode.map { (name, code) ->
            val displayName = name.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
            Pair(displayName, countryCodeToEmoji(code))
        }.sortedBy { it.first }
    }
}
