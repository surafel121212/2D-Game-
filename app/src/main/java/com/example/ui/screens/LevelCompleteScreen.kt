package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

@Composable
fun LevelCompleteScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.gameProgress.collectAsState()
    val currentLevelData by viewModel.currentLevelData.collectAsState()
    val levelDiamonds by viewModel.diamondsCollectedThisLevel.collectAsState()

    val levelNum = currentLevelData?.levelNumber ?: 1
    val levelName = currentLevelData?.name ?: "Mission Complete"

    // Pulse animation to make victory glow!
    val infiniteTransition = rememberInfiniteTransition(label = "win_golds")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF020617), // Rich dark
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Success victory insignia icon tag
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF10B981).copy(0.12f), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF10B981).copy(glowIntensity), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚁",
                    fontSize = 50.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete title Header
            Text(
                text = "MISSION SECURED",
                color = Color(0xFF00F2FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SECTOR 0$levelNum CLEANSE",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "\"$levelName\"",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loot earnings summaries
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF050608))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // SECURED LOOT COUNTER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAMOND LOOT",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "💎 +$levelDiamonds",
                            color = Color(0xFF00F2FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // MISSION GRADE COMPLIANCE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HUNT STATUS",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "CLASS A ASSASSIN",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action triggers flow columns
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.advanceToNextLevel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("next_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "NEXT CAMPAIGN",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenState.MainMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("victory_menu_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text(
                        text = "RETURN TO HEADQUARTERS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
