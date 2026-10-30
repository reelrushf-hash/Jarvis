package com.example.system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.Language
import com.example.data.repository.JarvisMemoryRepository

class CallManager(
    private val context: Context,
    private val memoryRepository: JarvisMemoryRepository
) {

    // Resolves contact number either from local memory or from device contacts
    suspend fun resolveContact(target: String): ContactInfo? {
        val cleanTarget = target.trim()

        // 1. If user passed direct digits
        if (cleanTarget.all { it.isDigit() || it == '+' || it == ' ' || it == '-' } && cleanTarget.replace("[^0-9]".toRegex(), "").length >= 3) {
            return ContactInfo(
                displayName = cleanTarget,
                phoneNumber = cleanTarget.replace(" ", "")
            )
        }

        // 2. Search local memory repository first (offline-first & fast!)
        val localContacts = memoryRepository.findContact(cleanTarget)
        if (localContacts.isNotEmpty()) {
            val best = localContacts.first()
            return ContactInfo(
                displayName = best.name,
                phoneNumber = best.phoneNumber,
                relationship = best.relationship,
                source = "JARVIS Vault"
            )
        }

        // 3. Search Device Contacts if permission granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
                val selectionArgs = arrayOf("%$cleanTarget%")
                val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        return ContactInfo(
                            displayName = name,
                            phoneNumber = number,
                            source = "Device Contacts"
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore contact query failure
            }
        }

        return null
    }

    fun initiateCall(phoneNumber: String, isDirectCall: Boolean = false): CallResult {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val uri = Uri.parse("tel:$cleanNumber")

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return if (hasCallPermission && isDirectCall) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                CallResult(
                    isSuccess = true,
                    spokenMessage = "Placing direct call to $phoneNumber, Boss.",
                    actionLabel = "Direct Call"
                )
            } catch (e: Exception) {
                fallbackToDialer(uri, phoneNumber)
            }
        } else {
            fallbackToDialer(uri, phoneNumber)
        }
    }

    private fun fallbackToDialer(uri: Uri, phoneNumber: String): CallResult {
        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            CallResult(
                isSuccess = true,
                spokenMessage = "Dialer opened for $phoneNumber, Boss.",
                actionLabel = "Opened Dialer"
            )
        } catch (e: Exception) {
            CallResult(
                isSuccess = false,
                spokenMessage = "Unable to open dialer on this device, Boss.",
                actionLabel = "Call Failed"
            )
        }
    }
}

data class ContactInfo(
    val displayName: String,
    val phoneNumber: String,
    val relationship: String = "",
    val source: String = "Unknown"
)

data class CallResult(
    val isSuccess: Boolean,
    val spokenMessage: String,
    val actionLabel: String
)
