package com.example.data.model

enum class Language(val code: String, val label: String, val bcp47: String, val greetingBoss: String) {
    ENGLISH(
        code = "en",
        label = "English",
        bcp47 = "en-US",
        greetingBoss = "At your command, Boss."
    ),
    HINDI(
        code = "hi",
        label = "हिंदी",
        bcp47 = "hi-IN",
        greetingBoss = "हाँ बॉस, बताइए क्या हुक्म है।"
    ),
    BENGALI(
        code = "bn",
        label = "বাংলা",
        bcp47 = "bn-IN",
        greetingBoss = "বলুন বস, আপনার আদেশের অপেক্ষায় আছি।"
    );

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
