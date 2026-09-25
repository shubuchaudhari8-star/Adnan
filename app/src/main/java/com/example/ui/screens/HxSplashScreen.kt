package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmpathyRose
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.PrivacyEmerald
import kotlinx.coroutines.delay

/**
 * Splash screen featuring the 'HX The human technology process is getting started.'
 * choreographed animation sequence implemented using Jetpack Compose AnimatedVisibility.
 */
@Composable
fun HxSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step1LogoVisible by remember { mutableStateOf(false) }
    var step2TitleVisible by remember { mutableStateOf(false) }
    var step3TaglineVisible by remember { mutableStateOf(false) }
    var step4Process1Visible by remember { mutableStateOf(false) }
    var step5Process2Visible by remember { mutableStateOf(false) }
    var step6Process3Visible by remember { mutableStateOf(false) }
    var step7ReadyButtonVisible by remember { mutableStateOf(false) }

    // Pulsing aura animation behind the HX monogram
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Choreographed animation sequence using Coroutines
    LaunchedEffect(Unit) {
        delay(150)
        step1LogoVisible = true

        delay(350)
        step2TitleVisible = true

        delay(350)
        step3TaglineVisible = true

        delay(350)
        step4Process1Visible = true

        delay(300)
        step5Process2Visible = true

        delay(300)
        step6Process3Visible = true

        delay(350)
        step7ReadyButtonVisible = true

        // Auto transition after user has experienced the animation sequence
        delay(1600)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A12),
                        Color(0xFF0F172A),
                        Color(0xFF0A0E1A)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient decorative background glows
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeuralViolet.copy(alpha = pulseAlpha * 0.45f),
                            EmpathyRose.copy(alpha = pulseAlpha * 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // STEP 1: HX Futuristic Logo / Monogram
            AnimatedVisibility(
                visible = step1LogoVisible,
                enter = fadeIn(animationSpec = tween(700)) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glowing ring
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    NeuralViolet,
                                    EmpathyRose,
                                    PrivacyEmerald,
                                    NeuralViolet
                                )
                            )
                        )
                    ) {}

                    // Inner badge
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF131B2E).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "HX",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.SansSerif,
                                style = LocalTextStyle.current.copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF60A5FA),
                                            NeuralViolet,
                                            EmpathyRose
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // STEP 2 & 3: Main Headline & Sequence Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = step2TitleVisible,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = tween(600)
                    ),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "HUMAN EXPERIENCE",
                        color = Color(0xFFA5B4FC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 4.sp
                    )
                }

                AnimatedVisibility(
                    visible = step3TaglineVisible,
                    enter = fadeIn(animationSpec = tween(700)) + expandVertically(
                        animationSpec = tween(700)
                    ) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = tween(700)
                    ),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "“HX The human technology process is getting started.”",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // STEP 4, 5, 6: Sequential Process Pipeline Checkpoints
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = step4Process1Visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInHorizontally(
                        initialOffsetX = { -40 },
                        animationSpec = tween(500)
                    )
                ) {
                    ProcessStepItem(
                        icon = Icons.Default.Psychology,
                        label = "Neural Empathy Engine",
                        status = "Synthesizing",
                        color = EmpathyRose
                    )
                }

                AnimatedVisibility(
                    visible = step5Process2Visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInHorizontally(
                        initialOffsetX = { -40 },
                        animationSpec = tween(500)
                    )
                ) {
                    ProcessStepItem(
                        icon = Icons.Default.Memory,
                        label = "Room & Coroutines ML Pipelines",
                        status = "Calibrated",
                        color = NeuralViolet
                    )
                }

                AnimatedVisibility(
                    visible = step6Process3Visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInHorizontally(
                        initialOffsetX = { -40 },
                        animationSpec = tween(500)
                    )
                ) {
                    ProcessStepItem(
                        icon = Icons.Default.Security,
                        label = "On-Device Privacy Sandbox",
                        status = "Active & Protected",
                        color = PrivacyEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // STEP 7: Interactive Quick-Enter Action with Progress
            AnimatedVisibility(
                visible = step7ReadyButtonVisible,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(500)
                ),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(180.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeuralViolet,
                        trackColor = Color(0xFF334155)
                    )

                    Button(
                        onClick = onSplashFinished,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("splash_enter_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D72FE)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "প্রবেশ করুন / Get Started",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessStepItem(
    icon: ImageVector,
    label: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131B2E).copy(alpha = 0.8f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrivacyEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = status,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
