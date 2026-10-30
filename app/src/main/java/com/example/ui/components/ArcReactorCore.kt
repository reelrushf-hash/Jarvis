package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisRedAlert

@Composable
fun ArcReactorCore(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only animate when actively listening or speaking to conserve battery on 2GB RAM devices
    val isActive = isListening || isSpeaking || isProcessing
    val pulseScale = if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "activePulse"
        )
        scale
    } else {
        1.0f
    }

    val coreBorderColor = when {
        isListening -> JarvisCyanLight
        isSpeaking -> JarvisAmber
        isProcessing -> JarvisCyan
        else -> JarvisCyan.copy(alpha = 0.85f)
    }

    val coreBgColor = when {
        isListening -> Color(0xFF041926)
        isSpeaking -> Color(0xFF1E1705)
        else -> Color(0xFF050E1A)
    }

    val actionDescription = when {
        isListening -> "Listening to Boss command. Tap to stop."
        isSpeaking -> "JARVIS is speaking. Tap to interrupt."
        else -> "Activate JARVIS voice command"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Outer interactive cyan circular JARVIS button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(coreBgColor)
                .border(2.5.dp, coreBorderColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 65.dp),
                    onClick = onTap
                )
                .semantics { contentDescription = actionDescription }
                .testTag("arc_reactor_core_button")
        ) {
            // Inner core ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF02060C))
                    .border(1.5.dp, coreBorderColor.copy(alpha = 0.6f), CircleShape)
            ) {
                when {
                    isListening -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    isSpeaking -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = JarvisAmber,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    isProcessing -> {
                        Text(
                            text = "THINK",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "JARVIS Idle",
                            tint = JarvisCyan,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // State indicator text (simple, high-contrast, zero CPU overhead)
        val stateLabel = when {
            isListening -> "● LISTENING..."
            isSpeaking -> "● SPEAKING..."
            isProcessing -> "● PROCESSING..."
            else -> "TAP TO SPEAK"
        }

        val stateColor = when {
            isListening -> JarvisCyanLight
            isSpeaking -> JarvisAmber
            isProcessing -> JarvisCyan
            else -> Color(0xFF64B5F6)
        }

        Text(
            text = stateLabel,
            color = stateColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
    }
}
