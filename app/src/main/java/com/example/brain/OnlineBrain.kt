package com.example.brain

import com.example.data.model.Language
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnlineBrain {

    suspend fun queryOnlineModel(prompt: String, language: Language): BrainDecision = withContext(Dispatchers.IO) {
        try {
            val systemInstruction = "You are JARVIS AI, an intelligent personal assistant created for your user who is addressed with absolute loyalty as 'Boss'. Respond helpfully, politely, and concisely in ${language.label}. If writing code, provide clean, commented code."
            
            val model = Firebase.ai.generativeModel(
                modelName = "gemini-2.5-flash"
            )

            val fullPrompt = "$systemInstruction\n\nBoss says: $prompt"
            val response = model.generateContent(fullPrompt)
            val responseText = response.text ?: ""

            if (responseText.isNotBlank()) {
                BrainDecision.SpokenAnswer(
                    spokenResponse = responseText.trim(),
                    brainUsed = "Gemini AI"
                )
            } else {
                BrainDecision.SpokenAnswer(
                    spokenResponse = "Boss, the online server returned an empty response.",
                    brainUsed = "Gemini AI"
                )
            }
        } catch (e: Exception) {
            // Graceful fallback when Firebase AI is unprovisioned, rate limited, or offline
            val errorMessage = when (language) {
                Language.HINDI -> "बॉस, ऑनलाइन AI सेवा वर्तमान में अनुपलब्ध है। ऑफ़लाइन मेमोरी और सिस्टम टूल्स सक्रिय हैं।"
                Language.BENGALI -> "বস, অনলাইন এআই পরিষেবা সংযোগ করা যাচ্ছে না। অফলাইন মোড সক্রিয় রয়েছে।"
                Language.ENGLISH -> "Boss, online AI is currently unreachable (${e.localizedMessage ?: "Network/Service timeout"}). Reverting to Offline Brain."
            }
            BrainDecision.SpokenAnswer(errorMessage, "Offline Brain")
        }
    }
}
