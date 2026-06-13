package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val levelData by viewModel.currentLevelData.collectAsState()
    val level = levelData ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0505), // Deep dark blood red center tint
                        Color(0xFF020204)
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
            // Skull warning indicator
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFEF4444).copy(0.12f), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFFEF4444).copy(0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💀",
                    fontSize = 50.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Defeat title Header
            Text(
                text = "AGENT ELIMINATED",
                color = Color(0xFFFF007A), // Neon Pink blood warn
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MISSION COMPROMISED",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "The targets detected you and neutralised your vital signature. Clear paths cleanly or deploy freezing trap detonators safely.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action triggers retry columns
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.startLevel(level) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("retry_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "REDEPLOY ASSASSIN",
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
                        .testTag("game_over_menu_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text(
                        text = "RETURN TO SHADOW HQS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
