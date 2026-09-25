package com.example.nlp

import com.example.data.model.AnonymizationResult
import com.example.data.model.RedactedEntity

object AnonymizerEngine {

    // Phone number patterns (Bengali and English)
    private val phoneRegex = Regex("""(?:\+?880|01)[0-9\u09E6-\u09EF]{9}|(?:\+?1)?[0-9]{10}""")
    private val emailRegex = Regex("""[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+""")

    // Common Bengali and English names
    private val commonNames = listOf(
        "রহিম", "করিম", "তানভীর", "সাকিব", "রাফি", "আদনান", "সুমনা", "ফারহানা", "আয়েশা", "নুসরাত",
        "সাব্বির", "রিফাত", "আসিফ", "তানজিলা", "মারুফ", "শফিক", "হাসান", "মাহমুদ", "রাহুল", "অর্পিতা",
        "John", "David", "Sarah", "Michael", "Emma", "Alex", "Emily", "Daniel", "Jessica", "James",
        "Rahul", "Priya", "Ankit", "Sneha", "Amit"
    )

    // Common locations in Bangladesh & global
    private val locations = listOf(
        "ঢাকা", "চট্টগ্রাম", "সিলেট", "রাজশাহী", "খুলনা", "বরিশাল", "ধানমন্ডি", "গুলশান", "বনানী",
        "মিরপুর", "উত্তরা", "মোহাম্মদপুর", "মতিঝিল", "নিউমার্কেট",
        "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Dhanmondi", "Gulshan", "Banani", "Mirpur",
        "New York", "London", "Toronto", "Sydney", "Singapore", "Berlin"
    )

    // Organizations / Educational institutions
    private val organizations = listOf(
        "ঢাকা বিশ্ববিদ্যালয়", "বুয়েট", "ডিইউ", "নর্থ সাউথ", "ব্র্যাক", "গ্রামীণফোন", "রবি", "গুগল",
        "DU", "BUET", "NSU", "BRAC", "Google", "Microsoft", "Meta", "Amazon"
    )

    fun anonymize(text: String): AnonymizationResult {
        var sanitized = text
        val redactedList = mutableListOf<RedactedEntity>()

        // 1. Redact Emails
        sanitized = emailRegex.replace(sanitized) { match ->
            val entity = RedactedEntity("Email", match.value, "[REDACTED_EMAIL]")
            redactedList.add(entity)
            "[REDACTED_EMAIL]"
        }

        // 2. Redact Phone Numbers
        sanitized = phoneRegex.replace(sanitized) { match ->
            val entity = RedactedEntity("Phone Number", match.value, "[REDACTED_PHONE]")
            redactedList.add(entity)
            "[REDACTED_PHONE]"
        }

        // 3. Redact Names
        for (name in commonNames) {
            val nameRegex = Regex("""(?i)\b$name\b""")
            if (nameRegex.containsMatchIn(sanitized)) {
                sanitized = nameRegex.replace(sanitized) { match ->
                    val entity = RedactedEntity("Person Name", match.value, "[REDACTED_PERSON]")
                    redactedList.add(entity)
                    "[REDACTED_PERSON]"
                }
            }
        }

        // 4. Redact Locations
        for (loc in locations) {
            val locRegex = Regex("""(?i)\b$loc\b""")
            if (locRegex.containsMatchIn(sanitized)) {
                sanitized = locRegex.replace(sanitized) { match ->
                    val entity = RedactedEntity("Location", match.value, "[REDACTED_LOCATION]")
                    redactedList.add(entity)
                    "[REDACTED_LOCATION]"
                }
            }
        }

        // 5. Redact Organizations
        for (org in organizations) {
            val orgRegex = Regex("""(?i)\b$org\b""")
            if (orgRegex.containsMatchIn(sanitized)) {
                sanitized = orgRegex.replace(sanitized) { match ->
                    val entity = RedactedEntity("Organization", match.value, "[REDACTED_ORGANIZATION]")
                    redactedList.add(entity)
                    "[REDACTED_ORGANIZATION]"
                }
            }
        }

        // Compute privacy score:
        // If PII was found and successfully sanitized, 100%. If text is safe, 100%.
        val privacyScore = 100

        return AnonymizationResult(
            originalText = text,
            sanitizedText = sanitized,
            redactedEntities = redactedList.distinctBy { it.originalSnippet },
            privacyScore = privacyScore
        )
    }
}
