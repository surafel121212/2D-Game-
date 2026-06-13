package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
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
import com.example.data.LevelData
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.gameProgress.collectAsState()

    val currentDiamonds = progress?.diamonds ?: 100
    val reachedLevel = progress?.currentLevel ?: 1

    val speedLevel by viewModel.speedUpgradeLevel.collectAsState()
    val stealthLevel by viewModel.stealthUpgradeLevel.collectAsState()
    val punchLevel by viewModel.punchUpgradeLevel.collectAsState()
    val armorLevel by viewModel.armorUpgradeLevel.collectAsState()

    var isShopOpen by remember { mutableStateOf(false) }

    // Infinite pulsing visual indicator for cinematic game theme
    val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Deep Slate dark
                        Color(0xFF020617)  // Pitch black bottom
                    )
                )
            )
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // High-fidelity background stealth radar grid visual
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1F22D3EE), // Ice Cyan highlight
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Game Stats / Profile Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Diamond chip display
                Row(
                    modifier = Modifier
                        .background(Color(0x221E293B), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0).copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💎",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "$currentDiamonds",
                        color = Color(0xFFF1F5F9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Level Completed Tag
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF22D3EE).copy(0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "RANK LEVEL: $reachedLevel",
                        color = Color(0xFF22D3EE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Cinematic Main Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "SHADOW",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF1F5F9),
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "HUNTER",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFEF4444).copy(alpha = alphaPulse), // Pulsing blood red
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "TOP-DOWN STEALTH ASSASSIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // Central Navigation Controls Action Stack
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Start Play Button
                Button(
                    onClick = {
                        // Launch the active level progression
                        val activeLevel = LevelData.LEVELS.find { it.levelNumber == reachedLevel } 
                            ?: LevelData.LEVELS[0]
                        viewModel.startLevel(activeLevel)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("play_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444) // Bright red
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START CAMPAIGN",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Choose Hero Button
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(ScreenState.HeroSelection) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("choose_hero_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE2E8F0)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Heroes",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF22D3EE)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HEROES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Select Level Button
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(ScreenState.LevelSelection) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("choose_level_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE2E8F0)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Levels",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LEVELS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // SECURE UPGRADE SHOP ACCIDENT BUTTON
                Button(
                    onClick = { isShopOpen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22D3EE).copy(0.4f))
                ) {
                    Text(
                        text = "⚡ ARMORY UPGRADES (TAC-SHOP)",
                        color = Color(0xFF22D3EE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // FULL TACTICAL TAC-SHOP DIALOG PANEL OVERLAY
        if (isShopOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.85f))
                    .clickable { /* trap clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(Color(0xFF0F172A), RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF22D3EE).copy(0.8f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ HEAVY WEAPONS & STEALTH SHOP",
                        color = Color(0xFF22D3EE),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "UPGRADE EQUIPMENT TO INFILTRATE EXPANDED OFFICE SECTORS",
                        color = Color.White.copy(0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Diamonds display
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF020617), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎 ACCOUNT BALANCE:  ", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Text("$currentDiamonds Gems", color = Color(0xFFF1F5F9), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // STAT 1: SPEED BOOTS
                    UpgradeItemRow(
                        title = "👟 Tactical Speed Boots",
                        level = speedLevel,
                        description = "Increases run & roll velocity modifiers",
                        cost = (speedLevel + 1) * 35,
                        currentGold = currentDiamonds,
                        onUpgrade = { viewModel.upgradeStat("speed", (speedLevel + 1) * 35) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // STAT 2: STEALTH SUIT
                    UpgradeItemRow(
                        title = "🧥 Camouflage Silent Suit",
                        level = stealthLevel,
                        description = "Cuts standard guard sight detection field ranges",
                        cost = (stealthLevel + 1) * 35,
                        currentGold = currentDiamonds,
                        onUpgrade = { viewModel.upgradeStat("stealth", (stealthLevel + 1) * 35) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // STAT 3: MELEE GLOVES
                    UpgradeItemRow(
                        title = "🥊 Iron Punch Melee Knuckles",
                        level = punchLevel,
                        description = "Extends freeze sweep & stun disable seconds",
                        cost = (punchLevel + 1) * 35,
                        currentGold = currentDiamonds,
                        onUpgrade = { viewModel.upgradeStat("punch", (punchLevel + 1) * 35) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // STAT 4: CARBON SHIELDPLATE
                    UpgradeItemRow(
                        title = "🛡️ Titanium Carbon Chest Plating",
                        level = armorLevel,
                        description = "Adds protective maximum HP shield boundaries",
                        cost = (armorLevel + 1) * 35,
                        currentGold = currentDiamonds,
                        onUpgrade = { viewModel.upgradeStat("armor", (armorLevel + 1) * 35) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { isShopOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("❌ Close Shop (መልሰህ ዝጋ)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradeItemRow(
    title: String,
    level: Int,
    description: String,
    cost: Int,
    currentGold: Int,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF020617), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lvl $level",
                    color = Color(0xFF22D3EE),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(Color(0xFF22D3EE).copy(0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Text(text = description, color = Color.White.copy(0.5f), fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }

        Button(
            onClick = onUpgrade,
            enabled = currentGold >= cost && level < 5,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                disabledContainerColor = Color(0xFF1E293B)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
        ) {
            Text(
                text = if (level >= 5) "MAX" else "💎 $cost",
                color = if (level >= 5 || currentGold < cost) Color.White.copy(0.4f) else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
