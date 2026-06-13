package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeroDefinition
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSelectionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.gameProgress.collectAsState()
    val states by viewModel.heroStates.collectAsState()

    var activeTabPremium by remember { mutableStateOf(false) }

    val currentDiamonds = progress?.diamonds ?: 100
    val selectedHeroId = progress?.selectedHeroId ?: "std_green"

    // Group heroes based on tabs (Regular vs Premium)
    val displayedHeroes = remember(activeTabPremium) {
        HeroDefinition.HEROES.filter { it.isPremium == activeTabPremium }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CHOOSE YOUR HERO",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
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
                actions = {
                    // Balance info chip in toolbar
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0).copy(0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$currentDiamonds",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9),
                            fontSize = 14.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab row: Regular vs Premium (Screenshot style tab selection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                // Regular selection tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!activeTabPremium) Color(0xFFEF4444) else Color.Transparent)
                        .clickable { activeTabPremium = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Regular",
                        fontWeight = FontWeight.Bold,
                        color = if (!activeTabPremium) Color.White else Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }

                // Premium selection tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTabPremium) Color(0xFFEF4444) else Color.Transparent)
                        .clickable { activeTabPremium = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Premium",
                        fontWeight = FontWeight.Bold,
                        color = if (activeTabPremium) Color.White else Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }

            // Lazy Grid list of Hero options
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayedHeroes) { hero ->
                    val isUnlocked = states.find { it.heroId == hero.id }?.isUnlocked ?: (hero.cost == 0)
                    val isSelected = hero.id == selectedHeroId

                    HeroOptionCard(
                        hero = hero,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        canAfford = currentDiamonds >= hero.cost,
                        onSelect = { viewModel.selectHero(hero) },
                        onUnlock = { viewModel.unlockHero(hero) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroOptionCard(
    hero: HeroDefinition,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_card_${hero.id}")
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> Color(0xFFEF4444) // Gold highlights for active choice
                    isUnlocked -> Color(0xFF334155) // Standard card border
                    else -> Color(0xFF1E293B)
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E1E2F) else Color(0xFF0F172A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live-rendered Canvas Top-Down Hero display
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF020617), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                TopDownCharacterRenderer(
                    baseColor = hero.baseColor,
                    accentColor = hero.accentColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Name Title
            Text(
                text = hero.name,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF1F5F9),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Performance spec tag
            Text(
                text = hero.desc,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic Action Button overlays
            when {
                isSelected -> {
                    // Selected state label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEF4444).copy(0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(0.4f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "SELECTED",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                isUnlocked -> {
                    // Clickable Select button block
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            "CHOOSE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9),
                            fontSize = 11.sp
                        )
                    }
                }
                else -> {
                    // Buy unlock button with Diamond requirements
                    Button(
                        onClick = onUnlock,
                        enabled = canAfford,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981), // Emerald bright
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("💎", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${hero.cost}",
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) Color.White else Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopDownCharacterRenderer(
    baseColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Draw double shoulder pads
        drawCircle(
            color = baseColor,
            radius = cx * 0.75f,
            center = Offset(cx, cy)
        )

        // Draw central harness
        drawCircle(
            color = Color(0xFF111827),
            radius = cx * 0.45f,
            center = Offset(cx, cy)
        )

        // Draw hair/head cover details
        drawCircle(
            color = accentColor,
            radius = cx * 0.35f,
            center = Offset(cx, cy)
        )

        // Draw tactical knife/weapon held in right hand (right-hand offset)
        val weaponX = cx + cos(0.35f) * cx * 0.65f
        val weaponY = cy + sin(0.35f) * cy * 0.65f

        // Hand fist circle
        drawCircle(
            color = accentColor,
            radius = cx * 0.15f,
            center = Offset(weaponX, weaponY)
        )

        // Steel Knife blade line
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(weaponX, weaponY),
            end = Offset(weaponX + 18f, weaponY - 14f),
            strokeWidth = 6f
        )
    }
}
