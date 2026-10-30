package com.example.brain

import com.example.data.model.Language
import com.example.data.repository.JarvisMemoryRepository

class OfflineBrain(
    private val memoryRepository: JarvisMemoryRepository
) {

    suspend fun processCommand(rawInput: String, language: Language): BrainDecision {
        val input = rawInput.trim()
        val lower = input.lowercase()

        // 1. Emergency Stop (Immediate check across all 3 languages)
        if (isEmergencyStop(lower)) {
            return BrainDecision.EmergencyStop(
                when (language) {
                    Language.HINDI -> "इमरजेंसी स्टॉप सक्रिय कर दिया गया है, बॉस। सभी कार्य रोक दिए गए हैं।"
                    Language.BENGALI -> "জরুরি স্টপ সক্রিয় করা হয়েছে, বস। সমস্ত কাজ বন্ধ করা হয়েছে।"
                    Language.ENGLISH -> "Emergency stop engaged, Boss. All tasks suspended immediately."
                }
            )
        }

        // 2. Greetings & Identity Queries
        if (isGreeting(lower)) {
            val greeting = when (language) {
                Language.HINDI -> "नमस्ते बॉस! जार्विस आपके आदेश के लिए तैयार है। आज क्या करना है?"
                Language.BENGALI -> "নমস্কার বস! জার্ভিস আপনার আদেশের জন্য প্রস্তুত। কি সেবা করতে পারি?"
                Language.ENGLISH -> "Greetings Boss. JARVIS core online and listening. How may I assist you?"
            }
            return BrainDecision.SpokenAnswer(greeting, "Offline Brain")
        }

        if (isIdentityQuestion(lower)) {
            val answer = when (language) {
                Language.HINDI -> "मैं जार्विस हूँ, आपका निजी सहायक। मैं केवल आपको यानी मेरे बॉस को रिपोर्ट करता हूँ।"
                Language.BENGALI -> "আমি জার্ভিস, আপনার ব্যক্তিগত এআই সহকারী। আমি একমাত্র আমার বস-এর প্রতি দায়বদ্ধ।"
                Language.ENGLISH -> "I am JARVIS, your personal AI assistant. System protocols designate you as my sole authorized Boss."
            }
            return BrainDecision.SpokenAnswer(answer, "Offline Brain")
        }

        // 3. Phone Call Commands
        extractCallTarget(lower, input)?.let { target ->
            return BrainDecision.RequireConfirmation(
                actionType = ActionType.CALL,
                target = target,
                prompt = when (language) {
                    Language.HINDI -> "क्या आप $target को कॉल करना चाहते हैं, बॉस?"
                    Language.BENGALI -> "আপনি কি $target-কে কল করতে চান, বস?"
                    Language.ENGLISH -> "Would you like me to connect a call to $target, Boss?"
                }
            )
        }

        // 4. App Launching Commands
        extractOpenAppTarget(lower, input)?.let { appName ->
            return BrainDecision.ExecuteAction(
                actionType = ActionType.OPEN_APP,
                target = appName,
                spokenResponse = when (language) {
                    Language.HINDI -> "$appName खोल रहा हूँ, बॉस।"
                    Language.BENGALI -> "$appName ওপেন করছি, বস।"
                    Language.ENGLISH -> "Launching $appName now, Boss."
                }
            )
        }

        // 5. YouTube Search Commands
        extractYouTubeQuery(lower, input)?.let { ytQuery ->
            return BrainDecision.ExecuteAction(
                actionType = ActionType.SEARCH_YOUTUBE,
                target = ytQuery,
                spokenResponse = when (language) {
                    Language.HINDI -> "यूट्यूब पर $ytQuery खोज रहा हूँ, बॉस।"
                    Language.BENGALI -> "ইউটিউবে $ytQuery সার্চ করছি, বস।"
                    Language.ENGLISH -> "Searching YouTube for $ytQuery, Boss."
                }
            )
        }

        // 6. Memory: Remember / Save Fact
        extractRememberFact(lower, input)?.let { fact ->
            return BrainDecision.ExecuteAction(
                actionType = ActionType.SAVE_FACT,
                target = fact,
                spokenResponse = when (language) {
                    Language.HINDI -> "यह जानकारी मैंने स्थानीय मेमोरी में सुरक्षित कर ली है, बॉस।"
                    Language.BENGALI -> "এই তথ্যটি আমি স্থানীয় মেমরিতে সেভ করে রেখেছি, বস।"
                    Language.ENGLISH -> "Recorded into local memory vault, Boss: $fact"
                }
            )
        }

        // 7. Memory: Recall / Search Fact
        extractRecallQuery(lower, input)?.let { query ->
            val memories = memoryRepository.searchLocalMemories(query)
            val response = if (memories.isNotEmpty()) {
                val found = memories.first()
                when (language) {
                    Language.HINDI -> "बॉस, मुझे मेमोरी में यह मिला: $found"
                    Language.BENGALI -> "বস, আপনার মেমরি থেকে পাওয়া গেছে: $found"
                    Language.ENGLISH -> "Here is what I found in your vault, Boss: $found"
                }
            } else {
                when (language) {
                    Language.HINDI -> "माफ़ कीजिए बॉस, इससे संबंधित कोई जानकारी मेमोरी में नहीं मिली।"
                    Language.BENGALI -> "দুঃখিত বস, এই সম্পর্কিত কোনো তথ্য মেমরিতে পাওয়া যায়নি।"
                    Language.ENGLISH -> "I have no prior records regarding '$query' in your 500MB local memory, Boss."
                }
            }
            return BrainDecision.SpokenAnswer(response, "Offline Brain")
        }

        // 8. Task / Reminder Creation
        extractTaskTitle(lower, input)?.let { taskTitle ->
            return BrainDecision.ExecuteAction(
                actionType = ActionType.ADD_TASK,
                target = taskTitle,
                spokenResponse = when (language) {
                    Language.HINDI -> "टास्क जोड़ दिया गया है, बॉस: $taskTitle"
                    Language.BENGALI -> "নতুন টাস্ক তালিকায় যুক্ত করা হয়েছে, বস: $taskTitle"
                    Language.ENGLISH -> "Task added to your schedule, Boss: $taskTitle"
                }
            )
        }

        // 9. Device Status / Battery
        if (isDeviceStatusQuery(lower)) {
            return BrainDecision.ExecuteAction(
                actionType = ActionType.DEVICE_STATUS,
                spokenResponse = when (language) {
                    Language.HINDI -> "डिवाइस डायग्नोस्टिक्स स्क्रीन पर दिखा रहा हूँ, बॉस।"
                    Language.BENGALI -> "ডিভাইসের স্থিতি এবং ব্যাটারি পরীক্ষা করা হচ্ছে, বস।"
                    Language.ENGLISH -> "Displaying device telemetry and memory stats, Boss."
                }
            )
        }

        // 10. Simple Math Calculation
        calculateMath(lower)?.let { mathResult ->
            return BrainDecision.SpokenAnswer(
                when (language) {
                    Language.HINDI -> "बॉस, गणना का परिणाम $mathResult है।"
                    Language.BENGALI -> "বস, হিসাবের উত্তর $mathResult।"
                    Language.ENGLISH -> "The calculated result is $mathResult, Boss."
                },
                "Offline Brain"
            )
        }

        // 11. Bioscience & Science offline knowledge
        resolveBioscienceQuery(lower, language)?.let { scienceAnswer ->
            return BrainDecision.SpokenAnswer(scienceAnswer, "Offline Bioscience Brain")
        }

        // Default offline response
        val fallback = when (language) {
            Language.HINDI -> "आदेश समझ लिया गया है, बॉस। '$input' के लिए ऑफ़लाइन कमांड या ऑनलाइन AI से उत्तर प्राप्त किया जा सकता है।"
            Language.BENGALI -> "বস, আপনার বার্তা বুঝতে পেরেছি: '$input'। বিস্তারিত জানতে অনলাইন এআই বা নির্দিষ্ট কমান্ড ব্যবহার করুন।"
            Language.ENGLISH -> "Command received, Boss: '$input'. I am ready to execute this action or engage Online AI for extended reasoning."
        }
        return BrainDecision.SpokenAnswer(fallback, "Offline Brain")
    }

    private fun isEmergencyStop(text: String): Boolean {
        val keywords = listOf(
            "stop", "emergency stop", "abort", "shut down", "halt",
            "रुक जाओ", "रुको", "स्टॉप", "इमरजेंसी स्टॉप", "बंद करो",
            "থামো", "বন্ধ করো", "জরুরি স্টপ", "থামুন"
        )
        return keywords.any { text.contains(it) }
    }

    private fun isGreeting(text: String): Boolean {
        val keywords = listOf(
            "hello", "hi", "hey jarvis", "good morning", "good evening",
            "नमस्ते", "प्रणाम", "हेलो",
            "হ্যালো", "নমস্কার", "কেমন আছো", "শুভ সকাল"
        )
        return keywords.any { text.startsWith(it) || text == it }
    }

    private fun isIdentityQuestion(text: String): Boolean {
        val keywords = listOf(
            "who are you", "what is your name", "who is your boss",
            "तुम कौन हो", "तुम्हारा नाम क्या है", "तुम्हारा बॉस कौन है",
            "তুমি কে", "তোমার নাম কি", "তোমার বস কে"
        )
        return keywords.any { text.contains(it) }
    }

    private fun extractCallTarget(lower: String, raw: String): String? {
        val patterns = listOf(
            "call ", "dial ", "phone ",
            "कॉल करो ", "कॉल लगाओ ", "फोन करो ",
            "কল করো ", "ফোন করো "
        )
        for (p in patterns) {
            if (lower.startsWith(p)) {
                return raw.substring(p.length).trim()
            }
            if (lower.contains(p)) {
                val idx = lower.indexOf(p)
                return raw.substring(idx + p.length).trim()
            }
        }
        // Bengali suffix e.g. "মাকে কল করো"
        if (lower.contains("কে কল করো")) {
            val target = lower.substringBefore("কে কল করো").trim()
            if (target.isNotEmpty()) return target
        }
        return null
    }

    private fun extractOpenAppTarget(lower: String, raw: String): String? {
        val prefixes = listOf(
            "open ", "launch ", "start ",
            "खोलो ", "ओपन करो ", "चालू करो ",
            "খুলো ", "ওপেন করো "
        )
        for (p in prefixes) {
            if (lower.startsWith(p)) {
                return raw.substring(p.length).trim()
            }
        }
        // Hindi suffix: "यूट्यूब खोलो"
        if (lower.endsWith(" खोलो")) {
            return raw.substring(0, raw.length - 5).trim()
        }
        if (lower.endsWith(" খুলো")) {
            return raw.substring(0, raw.length - 5).trim()
        }
        return null
    }

    private fun extractYouTubeQuery(lower: String, raw: String): String? {
        val prefixes = listOf(
            "search youtube for ", "search youtube ", "youtube search ", "play on youtube ",
            "यूट्यूब पर सर्च करो ", "यूट्यूब पर खोजो ", "यूट्यूब पर ",
            "ইউটিউবে সার্চ করো ", "ইউটিউবে "
        )
        for (p in prefixes) {
            if (lower.contains(p)) {
                val idx = lower.indexOf(p)
                return raw.substring(idx + p.length).trim()
            }
        }
        return null
    }

    private fun extractRememberFact(lower: String, raw: String): String? {
        val prefixes = listOf(
            "remember that ", "remember ", "save fact ", "keep in mind that ",
            "याद रखो कि ", "याद रखो ", "मेमोरी में सेव करो ",
            "মনে রাখো যে ", "মনে রাখো "
        )
        for (p in prefixes) {
            if (lower.contains(p)) {
                val idx = lower.indexOf(p)
                return raw.substring(idx + p.length).trim()
            }
        }
        return null
    }

    private fun extractRecallQuery(lower: String, raw: String): String? {
        val prefixes = listOf(
            "what is my ", "tell me about my ", "recall ", "what did i say about ",
            "मेरी ", "मेरा ", "क्या है मेरा ",
            "আমার ", "কি আমার "
        )
        for (p in prefixes) {
            if (lower.contains(p)) {
                val idx = lower.indexOf(p)
                return raw.substring(idx + p.length).trim()
            }
        }
        return null
    }

    private fun extractTaskTitle(lower: String, raw: String): String? {
        val prefixes = listOf(
            "add task ", "create task ", "remind me to ", "new task ",
            "टास्क जोड़ो ", "टास्क बनाओ ", "मुझे याद दिलाना ",
            "টাস্ক তৈরি করো ", "টাস্ক যোগ করো "
        )
        for (p in prefixes) {
            if (lower.contains(p)) {
                val idx = lower.indexOf(p)
                return raw.substring(idx + p.length).trim()
            }
        }
        return null
    }

    private fun isDeviceStatusQuery(lower: String): Boolean {
        val keywords = listOf(
            "battery", "ram", "memory status", "device status", "system status",
            "बैटरी", "रैम", "सिस्टम स्टेटस",
            "ব্যাটারি", "ডিভাইস স্ট্যাটাস"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun calculateMath(lower: String): String? {
        val mathRegex = Regex("""(?:calculate|what is)?\s*(\d+(?:\.\d+)?)\s*([\+\-\*\/])\s*(\d+(?:\.\d+)?)""")
        val match = mathRegex.find(lower) ?: return null
        val num1 = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val num2 = match.groupValues[3].toDoubleOrNull() ?: return null

        val res = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> return null
        }
        return if (res.isNaN()) "Error (Division by zero)" else if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
    }

    private fun resolveBioscienceQuery(lower: String, language: Language): String? {
        // DNA / RNA / Genetics
        if (lower.contains("dna") || lower.contains("डीएनए") || lower.contains("ডিএনএ")) {
            return when (language) {
                Language.HINDI -> "बॉस, DNA (Deoxyribonucleic acid) जीवन का आनुवंशिक खाका है। यह Adenine, Thymine, Guanine और Cytosine नामक चार रासायनिक आधारों से बना डबल हेलिक्स होता है।"
                Language.BENGALI -> "বস, ডিএনএ (Deoxyribonucleic acid) হলো জীবনের জিনগত উপাদান। এটি অ্যাডেনিন, থাইমিন, গুয়ানিন এবং সাইটোসিন নামক নিউক্লিওটাইড নিয়ে ডাবল হেলিক্স গঠনে তৈরি।"
                Language.ENGLISH -> "Boss, DNA is the hereditary genetic molecule structured as a double helix composed of four nucleotides: Adenine, Thymine, Guanine, and Cytosine."
            }
        }

        // CRISPR / Gene editing
        if (lower.contains("crispr") || lower.contains("जीन एडिटिंग") || lower.contains("জিন এডিটিং")) {
            return when (language) {
                Language.HINDI -> "बॉस, CRISPR-Cas9 एक क्रांतिकारी बायोसाइंस टूल है जो वैज्ञानिकों को डीएनए अनुक्रम को अत्यंत सटीकता से संपादित करने की अनुमति देता है।"
                Language.BENGALI -> "বস, ক্রিসপার-ক্যাস৯ (CRISPR-Cas9) হলো জিনোম এডিটিং প্রযুক্তি যার সাহায্যে ডিএনএ সিকোয়েন্সে সুনির্দিষ্ট পরিবর্তন আনা সম্ভব।"
                Language.ENGLISH -> "Boss, CRISPR-Cas9 is a precision gene-editing technology derived from bacterial immune systems that allows targeted modifications in DNA sequences."
            }
        }

        // Photosynthesis
        if (lower.contains("photosynthesis") || lower.contains("प्रकाश संश्लेषण") || lower.contains("সালোকসংশ্লেষ")) {
            return when (language) {
                Language.HINDI -> "बॉस, प्रकाश संश्लेषण वह प्रक्रिया है जिससे पौधे सूर्य के प्रकाश, पानी और CO2 का उपयोग करके ग्लूकोज और ऑक्सीजन बनाते हैं।"
                Language.BENGALI -> "বস, সালোকসংশ্লেষ হলো উদ্ভিদের খাদ্য তৈরির জৈব-রাসায়নিক প্রক্রিয়া, যেখানে সূর্যালোক ও ক্লোরোফিলের উপস্থিতিতে পানি ও কার্বন ডাই-অক্সাইড থেকে গ্লুকোজ ও অক্সিজেন উৎপন্ন হয়।"
                Language.ENGLISH -> "Boss, photosynthesis is the biochemical reaction where photosynthetic organisms convert solar energy, water, and CO2 into chemical glucose and oxygen."
            }
        }

        // Vaccines / Immunology
        if (lower.contains("vaccine") || lower.contains("टीका") || lower.contains("টিকা") || lower.contains("ইমিউন")) {
            return when (language) {
                Language.HINDI -> "बॉस, वैक्सीन हमारे इम्यून सिस्टम को रोगजनकों (pathogens) को पहचानने और एंटीबॉडी बनाने के लिए प्रशिक्षित करती है।"
                Language.BENGALI -> "বস, ভ্যাকসিন বা টিকা রোগ প্রতিরোধ ব্যবস্থার মাধ্যমে অ্যান্টিবডি তৈরি করে নির্দিষ্ট সংক্রামক ভাইরাস বা ব্যাকটেরিয়ার বিরুদ্ধে সুরক্ষা দেয়।"
                Language.ENGLISH -> "Boss, vaccines trigger an adaptive immune response, stimulating memory B and T cells to produce neutralizing antibodies against specific pathogens without causing disease."
            }
        }

        // Mitochondria / Cell biology
        if (lower.contains("mitochondria") || lower.contains("माइटोकॉन्ड्रिया") || lower.contains("কোষ") || lower.contains("cell")) {
            return when (language) {
                Language.HINDI -> "बॉस, माइटोकॉन्ड्रिया कोशिका का पावरहाउस कहलाता है क्योंकि यह सेल्यूलर श्वसन के माध्यम से ATP (ऊर्जा) उत्पन्न करता है।"
                Language.BENGALI -> "বস, মাইটোকন্ড্রিয়া হলো কোষের পাওয়ার হাউস বা শক্তিকেন্দ্র, যা এটিপি (ATP) অণুর আকারে শক্তি তৈরি করে।"
                Language.ENGLISH -> "Boss, mitochondria are known as the cellular powerhouses, generating Adenosine Triphosphate (ATP) via oxidative phosphorylation and the Krebs cycle."
            }
        }

        return null
    }
}
