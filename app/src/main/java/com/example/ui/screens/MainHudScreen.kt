package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.JarvisUiState
import com.example.ui.components.ArcReactorCore
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreenOnline
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun MainHudScreen(
    state: JarvisUiState,
    onMicToggle: () -> Unit,
    onSendCommand: (String) -> Unit,
    onLanguageSelect: (Language) -> Unit,
    onEmergencyStop: () -> Unit,
    onReplayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // --- Top Bar: Minimal Logo, Status, Emergency Stop, Settings ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Brand Logo & Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CARO LINE AI",
                    color = JarvisCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isOnlineBrain = state.ownerProfile.onlineAiEnabled && !state.diagnostics.isLowMemory
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isOnlineBrain) Icons.Default.Cloud else Icons.Default.CloudOff,
                        contentDescription = "Status",
                        tint = if (isOnlineBrain) JarvisGreenOnline else JarvisAmber,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnlineBrain) "ONLINE" else "OFFLINE",
                        color = if (isOnlineBrain) JarvisGreenOnline else JarvisAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right side: Emergency Abort + Settings Gear
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onEmergencyStop,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("emergency_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Emergency Abort",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "ABORT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisBorder, CircleShape)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings and Memory Vault",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Language Selector Pill (Hindi / English / Bengali) ---
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisSurfaceElevated)
                .border(1.dp, JarvisBorder, RoundedCornerShape(16.dp))
                .padding(2.dp)
        ) {
            Language.entries.forEach { lang ->
                val isSelected = state.currentLanguage == lang
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) JarvisCyan else Color.Transparent)
                        .clickable { onLanguageSelect(lang) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("lang_select_${lang.code}")
                ) {
                    Text(
                        text = lang.label,
                        color = if (isSelected) Color(0xFF021017) else JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- Single Cyan Circular JARVIS Voice Button ---
        ArcReactorCore(
            isListening = state.isListening,
            isSpeaking = state.isSpeaking,
            isProcessing = state.isProcessing,
            onTap = onMicToggle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Live Recognized Command Display ---
        if (state.partialTranscript.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Recognized Command",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"${state.partialTranscript}\"",
                        color = JarvisCyanLight,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- Small Response Area ---
        Card(
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (state.isSpeaking) JarvisAmber else JarvisCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CARO LINE",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${state.lastBrainUsed}",
                            color = JarvisTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onReplayAudio,
                            modifier = Modifier.size(28.dp).testTag("replay_audio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play aloud",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (state.isSpeaking) {
                            IconButton(
                                onClick = onStopAudio,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop voice",
                                    tint = JarvisAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = state.lastResponse,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Streamlined Command Bar (Text Field + Send/Mic) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Command in ${state.currentLanguage.label}...",
                        color = JarvisTextMuted,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            onSendCommand(textInput)
                            textInput = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = JarvisSurfaceElevated,
                    unfocusedContainerColor = JarvisSurfaceElevated
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_text_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (textInput.isNotBlank()) {
                IconButton(
                    onClick = {
                        onSendCommand(textInput)
                        textInput = ""
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF021017),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onMicToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (state.isListening) JarvisRedAlert else JarvisSurfaceElevated)
                        .border(1.dp, if (state.isListening) JarvisRedAlert else JarvisCyan, CircleShape)
                        .testTag("mic_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (state.isListening) Color.White else JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
