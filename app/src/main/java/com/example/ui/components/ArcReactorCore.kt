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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
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

@Composable
fun ArcReactorCore(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only animate when actively listening or speaking to conserve battery
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
        isSpeaking -> "CARO LINE is vocalizing. Tap to interrupt."
        else -> "Activate CARO LINE voice assistant"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Outer interactive cyan circular button
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
                    isListening || isSpeaking -> {
                        VoiceWaveform(color = coreBorderColor)
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
                            contentDescription = "CARO LINE Idle",
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

@Composable
fun VoiceWaveform(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformBars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 26f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 24f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 32f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
