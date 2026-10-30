package com.example.system

import android.content.Context
import android.content.Intent
import android.net.Uri

class MessagingManager(private val context: Context) {

    fun sendSms(phoneNumber: String, message: String): MessageResult {
        return try {
            val cleanPhone = phoneNumber.replace(" ", "")
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            MessageResult(
                isSuccess = true,
                feedback = "Drafting SMS to $phoneNumber, Boss.",
                platform = "SMS"
            )
        } catch (e: Exception) {
            MessageResult(false, "No SMS app available, Boss.", "SMS")
        }
    }

    fun sendWhatsApp(phoneNumber: String, message: String): MessageResult {
        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
        val encodedMessage = Uri.encode(message)
        val url = if (cleanPhone.isNotEmpty()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMessage"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            MessageResult(true, "Opening WhatsApp with your message, Boss.", "WhatsApp")
        } catch (e: Exception) {
            // Fallback to browser or generic share intent
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                MessageResult(true, "Opening WhatsApp Web draft, Boss.", "WhatsApp Web")
            } catch (err: Exception) {
                MessageResult(false, "WhatsApp is not installed on this device, Boss.", "WhatsApp")
            }
        }
    }

    fun openMessenger(recipient: String = "", message: String = ""): MessageResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/")).apply {
                setPackage("com.facebook.orca")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            MessageResult(true, "Launching Messenger, Boss.", "Messenger")
        } catch (e: Exception) {
            MessageResult(false, "Messenger is not installed, Boss.", "Messenger")
        }
    }

    fun openInstagramDirect(message: String = ""): MessageResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/direct/inbox/")).apply {
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            MessageResult(true, "Opening Instagram Direct, Boss.", "Instagram")
        } catch (e: Exception) {
            MessageResult(false, "Instagram is not installed, Boss.", "Instagram")
        }
    }
}

data class MessageResult(
    val isSuccess: Boolean,
    val feedback: String,
    val platform: String
)
