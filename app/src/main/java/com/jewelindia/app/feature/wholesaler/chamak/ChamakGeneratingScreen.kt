// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakGeneratingView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakQuote
import kotlinx.coroutines.delay

@Composable
fun ChamakGeneratingScreen(
    vm: ChamakViewModel,
    modifier: Modifier = Modifier
) {
    var shapeIndex by remember { mutableIntStateOf(0) }

    // Rotate shapes every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            shapeIndex = (shapeIndex + 1) % 4
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    val currentQuote = ChamakQuote.quotes[vm.activeQuoteIndex % ChamakQuote.quotes.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.White,
                    0.25f to Color(0xFFFAF5F7),
                    0.55f to Color(0xFFF3E8FF),
                    1f to Color(0xFFFCE7F3)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Radial brushes reproduce the iOS diffused auras without large offscreen blur layers.
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.width * 0.65f
            for ((x, color) in listOf(0.15f to Color(0xFFC084FC), 0.85f to Color(0xFFF472B6))) {
                val center = Offset(size.width * x, size.height * 0.65f)
                drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent), center, radius), radius, center)
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val contentHeight = maxHeight
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = contentHeight)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            val analyzing = vm.step == ChamakViewModel.Step.Analyzing
            val isSet = vm.mode == ChamakMode.SetCreation
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (analyzing) { if (isSet) "Preparing" else "Analyzing" }
                        else { if (isSet) "Staging" else "Crafting" },
                    fontFamily = Cirka, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A), textAlign = TextAlign.Center
                )
                Text(
                    text = if (analyzing) { if (isSet) "your pieces" else "your designs" }
                        else { if (isSet) "your set" else "your design" },
                    fontFamily = Cirka, fontSize = 38.sp, fontStyle = FontStyle.Italic,
                    color = Color(0xFFDE6B7C), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (analyzing) {
                        if (isSet) "Checking photos and preparing your staging studio."
                        else "Breaking down your designs to craft a fresh perspective."
                    } else {
                        if (isSet) "Arranging your pieces with master jewellery composition."
                        else "Synthesizing elements into a bespoke jewellery masterpiece."
                    },
                    fontFamily = Manrope, fontSize = 14.sp, lineHeight = 21.sp,
                    color = Color(0xFF6B7280), textAlign = TextAlign.Center
                )
            }
            // Blooming Aura & Morphing Jewel Emblem Center
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer soft halo
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(rippleScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f * rippleAlpha))
                        .blur(24.dp)
                )

                // Middle ripple circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(rippleScale)
                        .border(1.5.dp, Color.White.copy(alpha = rippleAlpha), CircleShape)
                )

                // Inner radiant glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color(0xFFF472B6).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(16.dp)
                )

                // Dynamic morphing gem shape
                AnimatedContent(
                    targetState = shapeIndex,
                    transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
                    label = "gemShape"
                ) { index ->
                    val shape = when (index) {
                        0 -> SoftBlossomShape()
                        1 -> SoftCabochonTrillionShape()
                        2 -> SoftCushionGemShape()
                        else -> SoftTeardropGemShape()
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulseScale)
                            .clip(shape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFDE6B7C)))
                    Text("Creating your design…", fontFamily = Manrope, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFF6B465C))
                }
                Spacer(Modifier.height(16.dp))
            // Rotating quote card
            AnimatedContent(
                targetState = currentQuote,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "quote"
            ) { quote ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "“${quote.text}”",
                        fontFamily = Cirka,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— ${quote.author}",
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            }
        }
        }
    }
}
