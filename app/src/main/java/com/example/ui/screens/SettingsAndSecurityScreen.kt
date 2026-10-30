package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.AuditLogEntity
import com.example.data.model.OwnerProfile
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreenOnline
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsAndSecurityScreen(
    profile: OwnerProfile,
    auditLogs: List<AuditLogEntity>,
    onUpdateProfile: (OwnerProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ownerName by remember(profile.name) { mutableStateOf(profile.name) }
    var securityPin by remember(profile.securityPin) { mutableStateOf(profile.securityPin) }
    var onlineAiEnabled by remember(profile.onlineAiEnabled) { mutableStateOf(profile.onlineAiEnabled) }
    var lowRamMode by remember(profile.lowRamMode) { mutableStateOf(profile.lowRamMode) }
    var speechSpeed by remember(profile.speechSpeed) { mutableFloatStateOf(profile.speechSpeed) }
    var speechPitch by remember(profile.speechPitch) { mutableFloatStateOf(profile.speechPitch) }
    var floatingButtonEnabled by remember(profile.floatingButtonEnabled) { mutableStateOf(profile.floatingButtonEnabled) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
    ) {
        // --- Owner Identity Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "Owner", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BOSS IDENTITY & CREDENTIALS",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Designation / Title (Default: Boss)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = securityPin,
                        onValueChange = { securityPin = it },
                        label = { Text("Security PIN for Sensitive Actions") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("security_pin_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val updated = profile.copy(
                                name = ownerName.ifBlank { "Boss" },
                                securityPin = securityPin.ifBlank { "1122" }
                            )
                            onUpdateProfile(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Owner Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Brain & AI Architecture ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BRAIN ROUTER & LOW-RAM POLICIES",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Online AI (Gemini Integration)", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Enables complex reasoning and online queries when network is active", color = JarvisTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = onlineAiEnabled,
                            onCheckedChange = {
                                onlineAiEnabled = it
                                onUpdateProfile(profile.copy(onlineAiEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceElevated)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("2 GB RAM Optimization Mode", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Throttles memory allocations and caches for budget Xiaomi / Redmi phones", color = JarvisTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = lowRamMode,
                            onCheckedChange = {
                                lowRamMode = it
                                onUpdateProfile(profile.copy(lowRamMode = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisGreenOnline, checkedTrackColor = JarvisSurfaceElevated)
                        )
                    }
                }
            }
        }

        // --- Voice Parameters ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Voice", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VOICE ENGINE TUNING (TTS)",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Speech Speed: ${String.format("%.1fx", speechSpeed)}", color = JarvisTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            onUpdateProfile(profile.copy(speechSpeed = it))
                        },
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Voice Pitch: ${String.format("%.1fx", speechPitch)}", color = JarvisTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            onUpdateProfile(profile.copy(speechPitch = it))
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )
                }
            }
        }

        // --- Floating Button Overlay Settings ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Widgets, contentDescription = "Overlay", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FLOATING OVERLAY ASSISTANT",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Floating Arc Reactor Button", color = JarvisTextPrimary, fontSize = 13.sp)
                            Text("Shows floating widget over other apps. Requires permission.", color = JarvisTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = floatingButtonEnabled,
                            onCheckedChange = { enabled ->
                                floatingButtonEnabled = enabled
                                onUpdateProfile(profile.copy(floatingButtonEnabled = enabled))
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceElevated)
                        )
                    }
                }
            }
        }

        // --- Security Audit Log ---
        item {
            Text(
                text = "SECURITY AUDIT TRAIL (${auditLogs.size})",
                color = JarvisTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (auditLogs.isEmpty()) {
            item {
                Text("No privileged security events recorded yet.", color = JarvisTextMuted, fontSize = 12.sp)
            }
        } else {
            items(auditLogs, key = { it.id }) { log ->
                val dateStr = remember(log.timestamp) {
                    SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()).format(Date(log.timestamp))
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(10.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = log.actionName, color = JarvisCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "[${log.riskLevel}]", color = if (log.riskLevel == "HIGH") JarvisAmber else JarvisCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text(text = log.details, color = JarvisTextPrimary, fontSize = 11.sp)
                            Text(text = dateStr, color = JarvisTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
