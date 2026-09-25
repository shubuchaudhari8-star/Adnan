package com.example.data.local

import com.example.data.local.entity.ExperienceEntity

object PreseededData {

    // Helper to format 16-dim normalized vector string
    fun vectorString(vararg weights: Float): String {
        var sumSquares = 0f
        for (w in weights) sumSquares += w * w
        val norm = if (sumSquares > 0f) Math.sqrt(sumSquares.toDouble()).toFloat() else 1f
        return weights.joinToString(",") { (it / norm).toString() }
    }

    fun getSeedExperiences(): List<ExperienceEntity> {
        val now = System.currentTimeMillis()
        val day = 86400000L

        return listOf(
            ExperienceEntity(
                id = 1,
                originalText = "আজকে ইন্টারভিউতে চূড়ান্ত রাউন্ড থেকে বাদ পড়লাম। ৩ মাস রাতদিন পড়াশোনা করেছিলাম। মনে হচ্ছে নিজেকে কখনোই প্রমাণ করতে পারব না।",
                sanitizedText = "আজকে [REDACTED_ORGANIZATION]-এর ইন্টারভিউতে চূড়ান্ত রাউন্ড থেকে বাদ পড়লাম। ৩ মাস অক্লান্ত পরিশ্রম করেছিলাম। মনে হচ্ছে কোনোদিন নিজের লক্ষ্য অর্জন করতে পারব না।",
                primaryEmotion = "হতাশা (Frustration)",
                secondaryEmotion = "ব্যর্থতার ভয় (Fear of Failure)",
                intensity = 0.85f,
                valence = "Negative",
                coreTopic = "চাকরি ও ক্যারিয়ারের হতাশা (Career Rejection)",
                keywords = "ইন্টারভিউ, প্রত্যাখ্যান, ক্যারিয়ার, চেষ্টা, হতাশা",
                embeddingVector = vectorString(-0.8f, 0.7f, 0.95f, 0.1f, 0.4f, 0.2f, 0.8f, 0.3f, 0.2f, 0.6f, 0.7f, 0.3f, 0.5f, 0.6f, 0.1f, 0.1f),
                empathyWisdom = "একটি দরোজা বন্ধ হওয়া মানে তোমার যোগ্যতার শেষ নয়। প্রত্যাখ্যান হলো জীবনের নতুন পথ উন্মুক্ত হওয়ার অদৃশ্য সোপান। সাময়িক বিশ্রাম নাও, কিন্তু থামবে না।",
                timestamp = now - 5 * day,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 2,
                originalText = "নতুন শহরে এসে কাউকে চিনি না। সন্ধ্যায় ঘরের এক কোণে বসে শুধু বুক ভেঙে কান্না পায়। এত মানুষের ভিড়েও নিজেকে তীব্র একা লাগে।",
                sanitizedText = "নতুন শহরে এসে কাউকে চিনি না। সন্ধ্যায় ঘরের এক কোণে বসে তীব্র বিষণ্ণতা গ্রাস করে। এত মানুষের ভিড়েও নিজেকে ভীষণ একা লাগে।",
                primaryEmotion = "একাকীত্ব (Loneliness)",
                secondaryEmotion = "বিষণ্ণতা (Melancholy)",
                intensity = 0.88f,
                valence = "Negative",
                coreTopic = "প্রবাস ও সামাজিক একাকীত্ব (Social Isolation)",
                keywords = "একাকীত্ব, নতুন শহর, বিষণ্ণতা, অপরিচিত, বন্ধুহীন",
                embeddingVector = vectorString(-0.85f, 0.4f, 0.1f, 0.3f, 0.95f, 0.5f, 0.6f, 0.1f, 0.3f, 0.2f, 0.5f, 0.1f, 0.4f, 0.2f, 0.7f, 0.2f),
                empathyWisdom = "নতুন ভূমিতে শিকড় গজাতে সময় লাগে। এই নিঃসঙ্গতা তোমার আত্ম-আবিষ্কারের নীরব সময়। জানালা খুলে একটু আলো বাতাস নাও, কোনো পার্কে যাও—একদিন এই শহরও তোমার আপন হবে।",
                timestamp = now - 4 * day,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 3,
                originalText = "৪ বছরের সম্পর্কের পর মানুষটা হুট করে বলল আর যোগাযোগ রেখো না। বুকটা ফেটে যাচ্ছে। পুরো অতীতটা যেন অর্থহীন মনে হচ্ছে।",
                sanitizedText = "দীর্ঘ সম্পর্কের পর ভালোবাসার মানুষটি হুট করে চলে গেল। বুকটা যেন ফাঁকা হয়ে গেছে। পুরো অতীত স্মৃতিগুলো শুধু যন্ত্রণা দিচ্ছে।",
                primaryEmotion = "মনভাঙা (Heartbreak)",
                secondaryEmotion = "গভীর দুঃখ (Deep Sorrow)",
                intensity = 0.94f,
                valence = "Negative",
                coreTopic = "সম্পর্কের বিচ্ছেদ ও অবসান (Relationship Breakup)",
                keywords = "বিচ্ছেদ, ব্রেকআপ, ভালোবাসা, কষ্ট, স্মৃতি",
                embeddingVector = vectorString(-0.95f, 0.8f, 0.1f, 0.98f, 0.7f, 0.8f, 0.5f, 0.4f, 0.1f, 0.5f, 0.6f, 0.1f, 0.2f, 0.1f, 0.8f, 0.1f),
                empathyWisdom = "মন ভেঙে যাওয়া পৃথিবীর অন্যতম তীব্র মানসিক যন্ত্রণা। নিজেকে দোষারোপ করো না। যে ভালোবাসা তুমি দিয়েছিলে তা তোমার হৃদয়ের সৌন্দর্য, অন্য কারো চলে যাওয়া সেটা মলিন করতে পারে না। ধীরে ধীরে ক্ষত সেরে ওঠে।",
                timestamp = now - 3 * day,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 4,
                originalText = "অফিসে একটানা ১৪ ঘণ্টা কাজ করেও বসের গালিগালাজ শুনতে হয়। শরীরের শেষ শক্তিটুকুও শেষ হয়ে গেছে। সকালে ঘুম ভাঙলে বিছানা ছাড়ার ইচ্ছে থাকে না।",
                sanitizedText = "কর্মস্থলে অতিরিক্ত কাজের চাপ এবং অবমূল্যায়নে নিঃশেষ হয়ে যাচ্ছি। শরীরের শেষ শক্তিটুকুও ক্ষয়ে গেছে। সকালে ঘুম ভাঙলে আর সামনে এগোনোর তাগিদ পাই না।",
                primaryEmotion = "মানসিক ক্লান্তি (Burnout)",
                secondaryEmotion = "ক্ষোভ ও অসহায়ত্ব (Helplessness)",
                intensity = 0.90f,
                valence = "Negative",
                coreTopic = "কাজের অতিরিক্ত চাপ ও বার্নআউট (Workplace Burnout)",
                keywords = "বার্নআউট, চাকরি, মানসিক চাপ, ক্লান্তি, অবমূল্যায়ন",
                embeddingVector = vectorString(-0.75f, 0.6f, 0.85f, 0.2f, 0.3f, 0.1f, 0.7f, 0.7f, 0.2f, 0.4f, 0.98f, 0.2f, 0.3f, 0.4f, 0.2f, 0.1f),
                empathyWisdom = "তোমার মানসিক ও শারীরিক স্বাস্থ্য কোনো চাকরির চেয়ে বড় নয়। বার্নআউট কোনো ব্যর্থতা নয়, এটা শরীরের পক্ষ থেকে সতর্কবার্তা। আজই নিজের জন্য কিছুটা সীমানা নির্ধারণ করো এবং বিশ্রামকে অপরাধ ভাবা বন্ধ করো।",
                timestamp = now - 2 * day,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 5,
                originalText = "মায়ের অসুস্থতার রিপোর্ট দেখে হাত পা কাঁপছে। যদি কিছু হয়ে যায় আমি কীভাবে বাঁচব? প্রতিটা নিঃশ্বাসে শুধু এক অজানা আতঙ্ক তাড়া করছে।",
                sanitizedText = "পরিবারের প্রিয়জনের গুরুতর অসুস্থতার খবরে দিশেহারা লাগছে। প্রতি মুহূর্তে এক চরম শঙ্কা ও আতঙ্ক মনের ভেতর চেপে বসে আছে।",
                primaryEmotion = "উদ্বেগ ও আতঙ্ক (Severe Anxiety)",
                secondaryEmotion = "স্বজন হারানোর ভয় (Fear of Loss)",
                intensity = 0.96f,
                valence = "Negative",
                coreTopic = "পরিবারের অসুস্থতা ও স্বাস্থ্য আতঙ্ক (Family Health Crisis)",
                keywords = "অসুস্থতা, আতঙ্ক, মা, পরিবার, ভয়, উদ্বেগ",
                embeddingVector = vectorString(-0.9f, 0.95f, 0.2f, 0.6f, 0.5f, 0.9f, 0.98f, 0.1f, 0.3f, 0.4f, 0.7f, 0.1f, 0.95f, 0.5f, 0.4f, 0.1f),
                empathyWisdom = "অনাকাঙ্ক্ষিত পরিস্থিতিতে আমাদের নিয়ন্ত্রণ হারিয়ে ফেলা স্বাভাবিক। গভীর শ্বাস নাও। এই মুহূর্তে তুমি যা করতে পারছ সেটাই যথেষ্ট। ভালোবাসার শক্তি সকল সংকট মোকাবিলা করতে মানুষকে সাহস জোগায়।",
                timestamp = now - 1 * day,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 6,
                originalText = "I worked on my startup for 18 months, invested all my savings, and yesterday our primary client cancelled. I don't know how to face my family.",
                sanitizedText = "I worked on my initiative for over a year, invested all savings, and recently our primary partner cancelled. I feel immense weight facing loved ones.",
                primaryEmotion = "হতাশা ও অনিশ্চয়তা (Despair & Uncertainty)",
                secondaryEmotion = "আর্থিক চাপ (Financial Dread)",
                intensity = 0.89f,
                valence = "Negative",
                coreTopic = "ব্যবসা ও আর্থিক বিপর্যয় (Financial & Startup Setback)",
                keywords = "startup, savings, failure, family, uncertainty, finance",
                embeddingVector = vectorString(-0.85f, 0.8f, 0.95f, 0.3f, 0.4f, 0.3f, 0.9f, 0.2f, 0.3f, 0.7f, 0.8f, 0.2f, 0.7f, 0.95f, 0.2f, 0.1f),
                empathyWisdom = "Building something daring takes immense bravery. A business setback is an event, never a definition of your worth as a human. Be honest with those who love you; vulnerability brings unexpected strength.",
                timestamp = now - 36 * 3600000L,
                isUserStory = false
            ),
            ExperienceEntity(
                id = 7,
                originalText = "অনেক কষ্টের পর অবশেষে প্রথম মাস্টার্সে স্কলারশিপ নিশ্চিত হলো। বাবা বেঁচে থাকলে আজ হয়তো সবচেয়ে বেশি আনন্দিত হতেন।",
                sanitizedText = "কঠিন পথচলার পর উচ্চশিক্ষার সম্মানজনক সুযোগ অর্জিত হলো। বাবা বেঁচে থাকলে আজ হয়তো আমার চেয়েও বেশি আনন্দিত হতেন।",
                primaryEmotion = "মিশ্র আনন্দ ও স্মৃতি (Bittersweet Joy)",
                secondaryEmotion = "কৃতজ্ঞতা ও শূন্যতা (Gratitude with Longing)",
                intensity = 0.82f,
                valence = "Mixed",
                coreTopic = "অর্জন ও প্রয়াত স্বজনের স্মৃতি (Academic Triumph & Remembrance)",
                keywords = "স্কলারশিপ, সাফল্য, বাবা, স্মৃতি, আনন্দ, কৃতজ্ঞতা",
                embeddingVector = vectorString(0.85f, 0.65f, 0.9f, 0.5f, 0.3f, 0.75f, 0.2f, 0.1f, 0.95f, 0.1f, 0.3f, 0.9f, 0.85f, 0.2f, 0.8f, 0.85f),
                empathyWisdom = "তোমার এই অর্জন তোমার অধ্যাবসায় এবং যারা তোমাকে ভালোবাসতেন তাদের দোয়ারই ফসল। তিনি যেখানেই থাকুন, তোমার সততা ও সফলতায় নিশ্চয়ই প্রশান্তি পাচ্ছেন। আনন্দকে সম্পূর্ণভাবে আলিঙ্গন করো।",
                timestamp = now - 12 * 3600000L,
                isUserStory = false
            )
        )
    }
}
