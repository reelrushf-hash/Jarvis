package com.example.brain

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.model.Language
import com.example.data.model.OwnerProfile
import com.example.data.repository.JarvisMemoryRepository

class BrainRouter(
    private val context: Context,
    private val offlineBrain: OfflineBrain,
    private val onlineBrain: OnlineBrain
) {

    suspend fun route(
        input: String,
        language: Language,
        profile: OwnerProfile
    ): BrainDecision {
        val cleanInput = input.trim()
        val lower = cleanInput.lowercase()

        // 1. Direct hardware, local commands, and emergency stop are ALWAYS handled offline
        if (isDirectLocalCommand(lower)) {
            return offlineBrain.processCommand(cleanInput, language)
        }

        // 2. Check if Online AI is permitted and network is connected
        val isNetworkAvailable = isOnline()
        val shouldUseOnline = profile.onlineAiEnabled && isNetworkAvailable && !profile.lowRamMode

        // If online AI is enabled and network is active, try online brain for reasoning / open queries
        if (shouldUseOnline && isComplexQuery(lower)) {
            return onlineBrain.queryOnlineModel(cleanInput, language)
        }

        // 3. Fallback to offline brain
        return offlineBrain.processCommand(cleanInput, language)
    }

    private fun isDirectLocalCommand(lower: String): Boolean {
        val directKeywords = listOf(
            "stop", "abort", "call", "dial", "open", "launch", "youtube", "task",
            "remember", "battery", "ram", "calculate", "setting",
            "खोलो", "कॉल", "स्टॉप", "याद", "टास्क", "बैटरी",
            "খুলো", "কল", "থামো", "মনে", "টাস্ক", "ব্যাটারি"
        )
        return directKeywords.any { lower.contains(it) }
    }

    private fun isComplexQuery(lower: String): Boolean {
        val complexIndicators = listOf(
            "explain", "how to", "why does", "write code", "solve", "summarize",
            "biology", "science", "dna", "crispr", "quantum", "history",
            "समझाओ", "कैसे करें", "कोड लिखो",
            "ব্যাখ্যা করো", "কিভাবে", "কোড লেখো"
        )
        return lower.length > 25 || complexIndicators.any { lower.contains(it) }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
