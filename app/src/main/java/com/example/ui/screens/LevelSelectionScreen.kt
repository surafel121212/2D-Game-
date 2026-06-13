package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelData
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.gameProgress.collectAsState()
    val reachedLevel = progress?.currentLevel ?: 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MISSION SELECTOR",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenState.MainMenu) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color(0xFFF1F5F9),
                    navigationIconContentColor = Color(0xFFF1F5F9)
                )
            )
        },
        containerColor = Color(0xFF020617),
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF050608), Color(0xFF0F172A))
                    )
                )
        ) {
            // Tactical grid background decoration
            TacticalGridBackground(modifier = Modifier.fillMaxSize())

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(LevelData.LEVELS) { level ->
                    val isLocked = level.levelNumber > reachedLevel
                    val isCompleted = level.levelNumber < reachedLevel

                    LevelSelectionCard(
                        level = level,
                        isLocked = isLocked,
                        isCompleted = isCompleted,
                        onSelect = {
                            if (!isLocked) {
                                viewModel.startLevel(level)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelSelectionCard(
    level: LevelData,
    isLocked: Boolean,
    isCompleted: Boolean,
    onSelect: () -> Unit
) {
    val cardBackground = if (isLocked) Color(0xFF1E293B).copy(0.3f) else Color(0xFF1E293B).copy(0.8f)
    val cardBorderColor = when {
        isLocked -> Color(0xFF334155).copy(0.3f)
        isCompleted -> Color(0xFF10B981).copy(0.5f) // Glowing emerald border for completed levels
        else -> Color(0xFF22D3EE) // Glowing cyan core for current adventure level
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isLocked) { onSelect() }
            .padding(16.dp)
            .testTag("level_card_${level.levelNumber}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Section Index header
                Text(
                    text = "SECTOR 0${level.levelNumber}",
                    color = if (isLocked) Color(0xFF64748B) else Color(0xFF22D3EE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Level Name Title
                Text(
                    text = level.name,
                    color = if (isLocked) Color(0xFF64748B) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Level Specs metadata info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "👤 GUARDS: ${level.guards.size}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (level.lasers.isNotEmpty()) {
                        Text(
                            text = "⚡ LASERS: ${level.lasers.size}",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (level.freezeTraps.isNotEmpty()) {
                        Text(
                            text = "❄️ TRAPS: ${level.freezeTraps.size}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Lock / Play status marker overlay column
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isLocked -> Color(0xFF0F172A).copy(0.6f)
                            isCompleted -> Color(0xFF10B981).copy(0.15f)
                            else -> Color(0xFFEF4444).copy(0.15f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isLocked -> Color(0xFF334155)
                            isCompleted -> Color(0xFF10B981)
                            else -> Color(0xFFEF4444)
                        },
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isLocked -> "🔒"
                        isCompleted -> "✅"
                        else -> "▶️"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TacticalGridBackground(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val gridStep = 44.dp.toPx()

        var x = 0f
        while (x < width) {
            drawLine(
                color = Color(0x0F22D3EE),
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, height),
                strokeWidth = 1f
            )
            x += gridStep
        }

        var y = 0f
        while (y < height) {
            drawLine(
                color = Color(0x0F22D3EE),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = 1f
            )
            y += gridStep
        }
    }
}
