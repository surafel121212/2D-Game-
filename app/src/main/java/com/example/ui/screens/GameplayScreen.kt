package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeroDefinition
import com.example.data.LevelData
import com.example.ui.viewmodel.*
import com.example.util.GridPoint
import kotlin.math.*

@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val levelData by viewModel.currentLevelData.collectAsState()
    val playerX by viewModel.playerX.collectAsState()
    val playerY by viewModel.playerY.collectAsState()
    val playerRot by viewModel.playerRotation.collectAsState()
    val playerHP by viewModel.playerHP.collectAsState()
    val maxPlayerHP by viewModel.maxPlayerHP.collectAsState()
    val guards by viewModel.levelGuards.collectAsState()
    val gems by viewModel.levelGems.collectAsState()
    val lasers by viewModel.levelLasers.collectAsState()
    val inactiveTraps by viewModel.activeFreezeTrapsState.collectAsState()
    val visualEffects by viewModel.visualEffects.collectAsState()
    val pathPoints by viewModel.playerPath.collectAsState()
    val levelDiamondsCollected by viewModel.diamondsCollectedThisLevel.collectAsState()
    val isWinReady by viewModel.gameWinMessage.collectAsState()
    val helicopterStatus by viewModel.helicopterStatus.collectAsState()
    val helicopterX by viewModel.helicopterX.collectAsState()
    val helicopterY by viewModel.helicopterY.collectAsState()
    val helicopterScale by viewModel.helicopterScale.collectAsState()
    val trackedGuardId by viewModel.trackedGuardId.collectAsState()

    // NEW CUSTOM SYSTEM STATE COLLECTIONS
    val unlockedDoors by viewModel.unlockedDoors.collectAsState()
    val guardsKilled by viewModel.guardsKilled.collectAsState()
    val policeSpawned by viewModel.policeSpawned.collectAsState()
    val disguiseProgressPercentage by viewModel.disguiseProgressPercentage.collectAsState()
    val yellowBoxCamoTimeLeft by viewModel.yellowBoxCamoTimeLeft.collectAsState()
    val yellowBoxPowerUpLocation by viewModel.yellowBoxPowerUpLocation.collectAsState()
    val isTacticalMapOpen by viewModel.isTacticalMapOpen.collectAsState()
    val isGamePaused by viewModel.isGamePaused.collectAsState()
    val cameraZoom by viewModel.cameraZoom.collectAsState()
    val cameraAngle by viewModel.cameraAngle.collectAsState()
    val customForceWeather by viewModel.customForceWeather.collectAsState()
    var isSettingsOpen by remember { mutableStateOf(true) }

    val level = levelData ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        // LAYER 1: Full-screen Interactive map grid backdrop (full-bleed immersive coverage)
        val progress by viewModel.gameProgress.collectAsState()
        val selectedId = progress?.selectedHeroId ?: "std_green"
        val selectedHero = com.example.data.model.HeroDefinition.HEROES.find { it.id == selectedId } ?: com.example.data.model.HeroDefinition.HEROES[0]

        GameViewMapGrid(
            level = level,
            playerX = playerX,
            playerY = playerY,
            playerRot = playerRot,
            guards = guards,
            gems = gems,
            lasers = lasers,
            explodedTraps = inactiveTraps,
            effects = visualEffects,
            pathPoints = pathPoints,
            selectedHero = selectedHero,
            helicopterStatus = helicopterStatus,
            helicopterX = helicopterX,
            helicopterY = helicopterY,
            helicopterScale = helicopterScale,
            trackedGuardId = trackedGuardId,
            unlockedDoors = unlockedDoors,
            yellowBoxPowerUpLocation = yellowBoxPowerUpLocation,
            yellowBoxCamoTimeLeft = yellowBoxCamoTimeLeft,
            disguiseProgressPercentage = disguiseProgressPercentage,
            cameraZoom = cameraZoom,
            cameraAngle = cameraAngle,
            customForceWeather = customForceWeather,
            onTapPoint = { worldX, worldY ->
                viewModel.handleLevelTap(worldX, worldY)
            },
            modifier = Modifier.fillMaxSize()
        )

        // LAYER 2: Centered tactical target targeting reticle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(140.dp)
                .graphicsLayer { alpha = 0.08f },
            contentAlignment = Alignment.Center
        ) {
            RadarPulseCircle()
        }

        // LAYER 3: HUD HEADER PANEL floated at the top of the viewport
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            HudHeaderPanel(
                levelName = level.name,
                levelNumber = level.levelNumber,
                hp = playerHP,
                maxHp = maxPlayerHP,
                diamondsEarned = levelDiamondsCollected,
                liveEnemies = guards.count { it.status != GuardStatus.Dead },
                onSettingsClick = { isSettingsOpen = !isSettingsOpen },
                onQuit = { viewModel.navigateTo(ScreenState.MainMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xD9050608)) // Dark glassmorphic background
                    .border(1.dp, Color(0xFF00F2FF).copy(0.25f), RoundedCornerShape(12.dp))
            )
        }

        // LAYER 4: TRANSLUCENT RADAR MINIMAP floated at Top-Left
        Box(
            modifier = Modifier
                .padding(top = 90.dp, start = 12.dp)
                .align(Alignment.TopStart)
                .size(100.dp)
                .background(Color(0xD1050A14), CircleShape)
                .border(1.5.dp, Color(0xFF00F2FF).copy(0.5f), CircleShape)
                .clickable { viewModel.toggleTacticalBlueprintMap(!isTacticalMapOpen) }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapW = level.width
                val mapH = level.height
                val mCellW = size.width / mapW
                val mCellH = size.height / mapH
                
                level.walls.forEach { w ->
                    drawRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(w.x * mCellW, w.y * mCellH),
                        size = androidx.compose.ui.geometry.Size(mCellW, mCellH)
                    )
                }
                level.lockedDoors.forEach { d ->
                    val isOp = unlockedDoors.contains(d)
                    drawRect(
                        color = if (isOp) Color(0xFF10B981) else Color(0xFFEF4444),
                        topLeft = Offset(d.x * mCellW, d.y * mCellH),
                        size = androidx.compose.ui.geometry.Size(mCellW, mCellH)
                    )
                }
                level.cashStashes.forEach { stash ->
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 2.0f,
                        center = Offset(stash.x * mCellW + mCellW/2, stash.y * mCellH + mCellH/2)
                    )
                }
                yellowBoxPowerUpLocation?.let { loc ->
                    drawRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(loc.x * mCellW, loc.y * mCellH),
                        size = androidx.compose.ui.geometry.Size(mCellW, mCellH)
                    )
                }
                guards.forEach { g ->
                    if (g.status != GuardStatus.Dead) {
                        val dotColor = if (g.isSecurityRobot) Color(0xFF06B6D4) else Color(0xFFFF0055)
                        drawCircle(color = dotColor, radius = 2.5f, center = Offset(g.x * mCellW, g.y * mCellH))
                    }
                }
                drawCircle(color = Color(0xFF10B981), radius = 3.5f, center = Offset(playerX * mCellW, playerY * mCellH))
            }
            Text(
                text = "TAP MAP",
                color = Color(0xFF00F2FF),
                fontWeight = FontWeight.Black,
                fontSize = 7.5.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
        }

        // LAYER 5: FLOATING GAME SETTINGS DECK floated at Top-Right
        if (isSettingsOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 12.dp)
                    .width(175.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEA080D1A))
                    .border(1.2.dp, Color(0xFF00F2FF).copy(0.4f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "⚙️ የጌም ሲቲንግ (DECK)",
                        color = Color(0xFF00F2FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )

                    Divider(color = Color(0xFF1E293B), thickness = 0.5.dp)

                    // Zoom adjustment slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ዙም (Zoom)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("${((cameraZoom * 10).toInt() / 10f)}x", color = Color(0xFF00F2FF), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = cameraZoom,
                            onValueChange = { viewModel.updateCameraZoom(it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00F2FF),
                                activeTrackColor = Color(0xFF00F2FF),
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.height(16.dp)
                        )
                    }

                    // Angle Rotate slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("እይታ (Angle)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("${cameraAngle.toInt()}°", color = Color(0xFF00F2FF), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = cameraAngle,
                            onValueChange = { viewModel.updateCameraAngle(it) },
                            valueRange = -180f..180f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00F2FF),
                                activeTrackColor = Color(0xFF00F2FF),
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.height(16.dp)
                        )
                    }

                    // Climate weather select grid
                    Text("አየር ሁኔታ (CLIMATE)", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    val weathersList = listOf("Default", "Day", "Night", "Snow", "Rain")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        weathersList.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { w ->
                                    val isSelected = customForceWeather == w
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF00F2FF) else Color(0xFF111827))
                                            .border(0.5.dp, if (isSelected) Color(0xFF00F2FF) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                            .clickable { viewModel.updateCustomForceWeather(w) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (w) {
                                                "Default" -> "ኖርማል"
                                                "Day" -> "ቀን"
                                                "Night" -> "ጨለማ"
                                                "Snow" -> "በረዶ"
                                                "Rain" -> "ዝናብ"
                                                else -> w
                                            },
                                            color = if (isSelected) Color(0xFF020617) else Color.White,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // LAYER 6: INTERACTIVE ACTION BUTTON FLOATING OVERLAY (Floated on bottom right above controls bar)
        Column(
            modifier = Modifier
                .padding(bottom = 100.dp, end = 16.dp)
                .align(Alignment.BottomEnd),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Door open button
            val nearDoorPoint = level.lockedDoors.firstOrNull { door ->
                val dx = playerX - (door.x + 0.5f)
                val dy = playerY - (door.y + 0.5f)
                kotlin.math.sqrt(dx * dx + dy * dy) < 1.3f && !unlockedDoors.contains(door)
            }
            if (nearDoorPoint != null) {
                Button(
                    onClick = { viewModel.unlockOfficeDoor(nearDoorPoint) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(10.dp))
                ) {
                    Text(text = "🚪 OPEN DOOR (ክፈት)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            // Disguise box hide button
            val nearWall = level.walls.any { w ->
                val dx = playerX - (w.x + 0.5f)
                val dy = playerY - (w.y + 0.5f)
                kotlin.math.sqrt(dx * dx + dy * dy) < 1.25f
            }
            val dpPercent = disguiseProgressPercentage
            Button(
                onClick = { viewModel.startProximityDisguise() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dpPercent == 100) Color(0xFFD97706) else Color(0xFF334155).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(10.dp))
            ) {
                val buttonText = when {
                    dpPercent == 100 -> "📦 UNHIDE (ውጣ)"
                    dpPercent != null && dpPercent < 100 -> "⏳ DRESSING $dpPercent%"
                    nearWall -> "📦 HIDE IN BOX (ተደበቅ)"
                    else -> "📦 DISGUISE (ያለህበት)"
                }
                Text(text = buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Stun melee punch button
            Button(
                onClick = { viewModel.performMeleePunch() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(10.dp))
            ) {
                Text(text = "👊 STUN PUNCH (ጡጫ)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // LAYER 7: EXIT HELIPAD ESCAPE NOTIFICATION OVERLAY
        if (isWinReady) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 95.dp)
                    .background(Color(0xFF10B981).copy(0.9f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "🚁 ALL GUARDS ELIMINATED! ESCAPE TO HELIPAD NOW!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // LAYER 8: SATELLITE RADAR TACTICAL BLUEPRINT OVERLAY
        if (isTacticalMapOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE9020617))
                    .clickable { /* grab clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color(0xFF0B1528), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFF00F2FF).copy(0.8f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚨 SATELLITE TRACER PATHWAYS",
                        color = Color(0xFF00F2FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TACTICAL BLUEPRINT HOLOGRAPH [GAME PAUSED]",
                        color = Color.White.copy(0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ROOM DESIGNATION MAP LEGEND:",
                            color = Color(0xFFFF0055),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("🤖 OFFICE DRONE = Sweeper patrol robots scanning hallway sectors.", color = Color.White, fontSize = 11.sp)
                        Text("💰 OFFICE CASH TREASURY = Stacks of diamond cash bills.", color = Color.White, fontSize = 11.sp)
                        Text("🚪 LOCK OFFICE DOOR = Secure gates. Stand near and open gates.", color = Color.White, fontSize = 11.sp)
                        Text("🚁 EXTRACTION PORTAL = Destination helipad escape target [H].", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color(0xFF040A15), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF00F2FF).copy(0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val mCellW = size.width / level.width
                            val mCellH = size.height / level.height

                            level.walls.forEach { w ->
                                drawRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(w.x * mCellW, w.y * mCellH),
                                    size = androidx.compose.ui.geometry.Size(mCellW, mCellH)
                                )
                            }

                            val hX = level.helipad.x * mCellW + mCellW/2
                            val hY = level.helipad.y * mCellH + mCellH/2
                            drawCircle(color = Color(0xFFF59E0B), radius = 10f, center = Offset(hX, hY))

                            guards.forEach { g ->
                                if (g.status != GuardStatus.Dead) {
                                    val c = if (g.isSecurityRobot) Color(0xFF06B6D4) else Color(0xFFFF0055)
                                    drawCircle(color = c, radius = 5f, center = Offset(g.x * mCellW, g.y * mCellH))
                                }
                            }

                            drawCircle(color = Color(0xFF10B981), radius = 6.5f, center = Offset(playerX * mCellW, playerY * mCellH))
                        }

                        Text("H", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.align(Alignment.Center))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.toggleTacticalBlueprintMap(!isTacticalMapOpen) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("❌ Resume Mission (ተመለስ)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // LAYER 9: FLOATING SYSTEM COMMAND BAR floated at BottomCenter
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BottomControlsPanel(
                onPause = { viewModel.navigateTo(ScreenState.MainMenu) },
                activeLevel = level,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xD90A0F1D)) // Sophisticated glassmorphic panel
                    .border(1.2.dp, Color(0xFF00F2FF).copy(0.25f), RoundedCornerShape(14.dp))
            )
        }
    }
}

@Composable
fun HudHeaderPanel(
    levelName: String,
    levelNumber: Int,
    hp: Float,
    maxHp: Float,
    diamondsEarned: Int,
    liveEnemies: Int,
    onSettingsClick: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthRatio = (hp / maxHp).coerceIn(0f, 1f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Commander status indicator
        Column {
            Text(
                text = "COMMANDER ASSASSIN // MISSION $levelNumber",
                color = Color(0xFF00F2FF), // Neon Cyan title
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Vital health progress bar row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(healthRatio)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00F2FF), Color(0xFF008CFF))
                                )
                            )
                    )
                }
                Text(
                    text = "${(healthRatio * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Column: Diamonds score
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LOOT SECURED",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "💎 +$diamondsEarned",
                color = Color(0xFF00F2FF),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Right Column: Target Status and Close Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TARGETS",
                    color = Color(0xFFFF007A), // Neon pink target
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (liveEnemies > 0) "$liveEnemies ALIVE" else "CLEAR!",
                    color = if (liveEnemies > 0) Color.White else Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .testTag("settings_popup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open camera and weather settings",
                    tint = Color(0xFF00F2FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onQuit,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .testTag("exit_mission_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Abort mission",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GameViewMapGrid(
    level: LevelData,
    playerX: Float,
    playerY: Float,
    playerRot: Float,
    guards: List<GuardState>,
    gems: List<GemPickupState>,
    lasers: List<LaserState>,
    explodedTraps: Set<GridPoint>,
    effects: List<VisualEffect>,
    pathPoints: List<GridPoint>,
    selectedHero: HeroDefinition,
    helicopterStatus: HelicopterStatus = HelicopterStatus.None,
    helicopterX: Float = 0f,
    helicopterY: Float = 0f,
    helicopterScale: Float = 1f,
    trackedGuardId: Int? = null,
    unlockedDoors: Set<GridPoint> = emptySet(),
    yellowBoxPowerUpLocation: GridPoint? = null,
    yellowBoxCamoTimeLeft: Long = 0L,
    disguiseProgressPercentage: Int? = null,
    cameraZoom: Float = 1.0f,
    cameraAngle: Float = 0f,
    customForceWeather: String = "Default",
    onTapPoint: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect animation phases to pulsate lights, lasers, alarm indicators
    val infiniteTransition = rememberInfiniteTransition(label = "gameplay_loops")
    val laserIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_flicker"
    )

    val gemFloatAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gem_float"
    )

    // FOOTPRINT REAL-TIME STATE TRACKERS
    val footprints = remember { mutableStateListOf<Footprint>() }
    var lastPlayerPos by remember { mutableStateOf(Offset(playerX, playerY)) }
    val lastGuardPositions = remember { mutableStateMapOf<Int, Offset>() }

    LaunchedEffect(level.levelNumber) {
        footprints.clear()
        lastPlayerPos = Offset(playerX, playerY)
        lastGuardPositions.clear()
    }

    val rawTheme = getThemeForLevel(level.levelNumber)
    val isSnowTheme = rawTheme.isSnowMode || customForceWeather == "Snow"
    val isDaytimeActive = rawTheme.isDaytime || customForceWeather == "Day"

    if (isSnowTheme) {
        val playerOffset = Offset(playerX, playerY)
        val pDist = (playerOffset - lastPlayerPos).getDistance()
        if (pDist > 0.35f) {
            footprints.add(
                Footprint(
                    x = playerX,
                    y = playerY,
                    isPlayer = true,
                    timestamp = System.currentTimeMillis(),
                    angle = playerRot
                )
            )
            lastPlayerPos = playerOffset
        }

        guards.forEach { g ->
            if (g.status != GuardStatus.Dead) {
                val lastPos = lastGuardPositions[g.id] ?: Offset(g.x, g.y)
                val guardOffset = Offset(g.x, g.y)
                val gDist = (guardOffset - lastPos).getDistance()
                if (gDist > 0.35f) {
                    footprints.add(
                        Footprint(
                            x = g.x,
                            y = g.y,
                            isPlayer = false,
                            timestamp = System.currentTimeMillis(),
                            angle = g.directionAngle
                        )
                    )
                    lastGuardPositions[g.id] = guardOffset
                }
            }
        }

        if (footprints.size > 80) {
            footprints.removeAt(0)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(level, playerX, playerY, cameraZoom, cameraAngle) {
                detectTapGestures { offset ->
                    val cellPixelW = 56.dp.toPx()
                    val cellPixelH = 56.dp.toPx()

                    val playerCenterX = playerX * cellPixelW
                    val playerCenterY = playerY * cellPixelH

                    val mapWidth = level.width * cellPixelW
                    val mapHeight = level.height * cellPixelH
                    val visibleW = size.width / cameraZoom
                    val visibleH = size.height / cameraZoom

                    val targetCenterX = if (mapWidth <= visibleW) {
                        mapWidth / 2f
                    } else {
                        playerCenterX.coerceIn(visibleW / 2f, mapWidth - visibleW / 2f)
                    }

                    val targetCenterY = if (mapHeight <= visibleH) {
                        mapHeight / 2f
                    } else {
                        playerCenterY.coerceIn(visibleH / 2f, mapHeight - visibleH / 2f)
                    }

                    val cameraOffsetX = size.width / 2f - targetCenterX
                    val cameraOffsetY = size.height / 2f - targetCenterY

                    // First: Undo the rotation of touch relative to screen center
                    val CX = size.width / 2f
                    val CY = size.height / 2f
                    val radians = Math.toRadians(-cameraAngle.toDouble())
                    val cos = Math.cos(radians)
                    val sin = Math.sin(radians)
                    val dx = offset.x - CX
                    val dy = offset.y - CY
                    val rotatedX = CX + (dx * cos - dy * sin).toFloat()
                    val rotatedY = CY + (dx * sin + dy * cos).toFloat()

                    // Second: Undo the scaling relative to screen center
                    val zoomedX = CX + (rotatedX - CX) / cameraZoom
                    val zoomedY = CY + (rotatedY - CY) / cameraZoom

                    val worldX = (zoomedX - cameraOffsetX) / cellPixelW
                    val worldY = (zoomedY - cameraOffsetY) / cellPixelH
                    onTapPoint(worldX, worldY)
                }
            }
    ) {
        val cellW = 56.dp.toPx()
        val cellH = 56.dp.toPx()

        val playerCenterX = playerX * cellW
        val playerCenterY = playerY * cellH

        val mapWidth = level.width * cellW
        val mapHeight = level.height * cellH
        val visibleW = size.width / cameraZoom
        val visibleH = size.height / cameraZoom

        val targetCenterX = if (mapWidth <= visibleW) {
            mapWidth / 2f
        } else {
            playerCenterX.coerceIn(visibleW / 2f, mapWidth - visibleW / 2f)
        }

        val targetCenterY = if (mapHeight <= visibleH) {
            mapHeight / 2f
        } else {
            playerCenterY.coerceIn(visibleH / 2f, mapHeight - visibleH / 2f)
        }

        val cameraOffsetX = size.width / 2f - targetCenterX
        val cameraOffsetY = size.height / 2f - targetCenterY

        val theme = when (customForceWeather) {
            "Night" -> rawTheme.copy(isDaytime = false)
            "Day" -> rawTheme.copy(isDaytime = true)
            else -> rawTheme
        }

        withTransform({
            scale(scaleX = cameraZoom, scaleY = cameraZoom, pivot = Offset(size.width / 2f, size.height / 2f))
            rotate(degrees = cameraAngle, pivot = Offset(size.width / 2f, size.height / 2f))
            translate(left = cameraOffsetX, top = cameraOffsetY)
        }) {
            // Draw background floor fill over the bounds of grid level mapping (with vibrant daylight orange and snow mode white soils)
            if (isDaytimeActive) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFDFD09E), // Smooth desert rustic yellow-sand
                            Color(0xFFEADAA4), // Sandy light gold
                            Color(0xFFD4C48F)  // Warm dry sand
                        )
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(level.width * cellW, level.height * cellH)
                )
            } else if (isSnowTheme) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFAFAFA), // Snowy White Base
                            Color(0xFFE2E8F0), // Cool Blue-Grey shadow soil
                            Color(0xFFF1F5F9)  // Sparkling snow
                        )
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(level.width * cellW, level.height * cellH)
                )
            } else {
                drawRect(
                    color = theme.floorColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(level.width * cellW, level.height * cellH)
                )
            }

            // Draw muddy floor details, radiant sunspots (Day), or deep snowy drifts (Snow)
            if (isSnowTheme) {
                // Soil drift patterns (በረዶ አፈር)
                for (driftsId in 1..8) {
                    val dxRatio = (driftsId * 3.7f) % level.width
                    val dyRatio = (driftsId * 2.9f) % level.height
                    drawCircle(
                        color = Color(0xFFFFFFFF).copy(alpha = 0.65f),
                        radius = cellW * 0.7f,
                        center = Offset(dxRatio * cellW + cellW/2f, dyRatio * cellH + cellH/2f)
                    )
                    drawCircle(
                        color = Color(0x2238BDF8),
                        radius = cellW * 0.9f,
                        center = Offset(dxRatio * cellW + cellW/2f, dyRatio * cellH + cellH/2f),
                        style = Stroke(width = 1.5f)
                    )
                }
            } else if (!isDaytimeActive) {
                for ( puddleId in 1..4 ) {
                    val pxOffset = (puddleId * 2.7f) % level.width
                    val pyOffset = (puddleId * 3.3f) % level.height
                    drawCircle(
                        color = Color.Black.copy(0.18f),
                        radius = cellW * 0.95f,
                        center = Offset(pxOffset * cellW + cellW / 2, pyOffset * cellH + cellH / 2)
                    )
                }
            } else {
                // Dusty wind/sand detail patches or radiant sunshine solar patches (ሁሉም ነገር ደመቅ ይበል)
                for ( patchId in 1..6 ) {
                    val pxOffset = (patchId * 2.9f) % level.width
                    val pyOffset = (patchId * 3.3f) % level.height
                    drawCircle(
                        color = Color(0xFFFFEB3B).copy(0.20f), // Radiant halo
                        radius = cellW * 1.5f,
                        center = Offset(pxOffset * cellW + cellW / 2, pyOffset * cellH + cellH / 2)
                    )
                }
            }

            // DRAW COOL PROGRESSIVE ROTATED FOOTPRINTS FOR PLAYER AND GUARDS
            if (isSnowTheme) {
                footprints.forEachIndexed { idx, footprint ->
                    val ageRatio = (idx.toFloat() / footprints.size.coerceAtLeast(1).toFloat()).coerceIn(0.15f, 1.0f)
                    val alpha = ageRatio * 0.45f
                    val fx = footprint.x * cellW
                    val fy = footprint.y * cellH

                    withTransform({
                        rotate(degrees = footprint.angle, pivot = Offset(fx, fy))
                    }) {
                        val isLeft = ((footprint.x * 123 + footprint.y * 456).toInt() % 2) == 0
                        val stepSideOffset = if (isLeft) -cellW * 0.12f else cellW * 0.12f
                        
                        // Sinker footprint shadow indentation
                        drawCircle(
                            color = Color(0xFF64748B).copy(alpha = alpha),
                            radius = cellW * 0.08f,
                            center = Offset(fx + stepSideOffset, fy)
                        )
                        // Snow edge contrast highlight
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.8f),
                            radius = cellW * 0.04f,
                            center = Offset(fx + stepSideOffset, fy - cellW * 0.03f)
                        )
                    }
                }
            }

            // 1. DRAW SUBTLE INDUSTRIAL RADAR MATRIX BASE GRID FLOOR
            if (theme.isGridVisible) {
                for (i in 0 until level.width) {
                    for (j in 0 until level.height) {
                        drawRect(
                            color = theme.gridColor,
                            topLeft = Offset(i * cellW, j * cellH),
                            size = Size(cellW, cellH),
                            style = Stroke(width = 0.5f)
                        )
                    }
                }
            }

            // 2. DRAW TARGET/EXIT HELIPAD ESCAPE POINT
            val hpad = level.helipad
            val hpadCenterX = hpad.x * cellW + cellW / 2
            val hpadCenterY = hpad.y * cellH + cellH / 2
            val hpadRadius = cellW * 1.3f

            drawCircle(
                color = Color(0xFFF59E0B).copy(0.12f),
                radius = hpadRadius,
                center = Offset(hpadCenterX, hpadCenterY)
            )
            drawCircle(
                color = Color(0xFFF59E0B).copy(0.35f),
                radius = hpadRadius,
                center = Offset(hpadCenterX, hpadCenterY),
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = Color(0xFFF59E0B).copy(0.15f),
                radius = hpadRadius * 0.7f,
                center = Offset(hpadCenterX, hpadCenterY),
                style = Stroke(width = 1.5f)
            )

            // Center Landing 'H' letter manual vectors
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(hpadCenterX - cellW * 0.3f, hpadCenterY - cellH * 0.4f),
                end = Offset(hpadCenterX - cellW * 0.3f, hpadCenterY + cellH * 0.4f),
                strokeWidth = 6f
            )
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(hpadCenterX + cellW * 0.3f, hpadCenterY - cellH * 0.4f),
                end = Offset(hpadCenterX + cellW * 0.3f, hpadCenterY + cellH * 0.4f),
                strokeWidth = 6f
            )
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(hpadCenterX - cellW * 0.3f, hpadCenterY),
                end = Offset(hpadCenterX + cellW * 0.3f, hpadCenterY),
                strokeWidth = 6f
            )

            // 3. DRAW EXTRUDED 2.5D PERSPECTIVE WALLS (CONTAINERS / BOX CRASTES / GENERATORS)
            level.walls.forEach { wall ->
                val left = wall.x * cellW
                val top = wall.y * cellH
                val wallType = level.wallTypes[wall] ?: 1 // types: 1 = Shipping Container, 2 = Wooden Crate, 3 = Generator Core

                val wallHeight = cellH * 0.28f

                // Ground drop shadow beneath wall
                drawRect(
                    color = Color(0x7F000000),
                    topLeft = Offset(left + 6f, top + 6f),
                    size = Size(cellW, cellH)
                )

                // Front/side wall facade depth (3D Extrusion)
                when (wallType) {
                    1 -> { // Shipping Container (Corrugated metal vertical ribs)
                        drawRect(
                            color = theme.wallSideColor,
                            topLeft = Offset(left, top - wallHeight + cellH),
                            size = Size(cellW, wallHeight)
                        )
                        for (i in 2..10 step 2) {
                            val ribX = left + cellW * (i.toFloat() / 12f)
                            drawLine(
                                color = Color.Black.copy(0.4f),
                                start = Offset(ribX, top - wallHeight + cellH),
                                end = Offset(ribX, top + cellH),
                                strokeWidth = 3f
                            )
                            drawLine(
                                color = Color.White.copy(0.12f),
                                start = Offset(ribX + 1.5f, top - wallHeight + cellH),
                                end = Offset(ribX + 1.5f, top + cellH),
                                strokeWidth = 1f
                            )
                        }
                    }
                    2 -> { // Wooden Crate (Framed woodwork facade)
                        drawRect(
                            color = theme.wallSideColor,
                            topLeft = Offset(left, top - wallHeight + cellH),
                            size = Size(cellW, wallHeight)
                        )
                        drawRect(
                            color = Color.Black.copy(0.35f),
                            topLeft = Offset(left + 4f, top - wallHeight + cellH + 2f),
                            size = Size(cellW - 8f, wallHeight - 4f),
                            style = Stroke(width = 2.5f)
                        )
                        drawLine(
                            color = Color.Black.copy(0.35f),
                            start = Offset(left + 4f, top - wallHeight + cellH + 2f),
                            end = Offset(left + cellW - 4f, top + cellH - 2f),
                            strokeWidth = 3f
                        )
                    }
                    3 -> { // Steel high-voltage generators (Ventilation slats and hazard panels)
                        drawRect(
                            color = theme.wallSideColor,
                            topLeft = Offset(left, top - wallHeight + cellH),
                            size = Size(cellW, wallHeight)
                        )
                        for (i in 0..6) {
                            val stripeOffset = left + (i * 8f)
                            drawLine(
                                color = Color.Black.copy(0.42f),
                                start = Offset(stripeOffset, top - wallHeight + cellH),
                                end = Offset(stripeOffset + 8f, top + cellH),
                                strokeWidth = 3.5f
                            )
                        }
                    }
                }

                // Top lid representing upper surface height
                val lidColor = when (wallType) {
                    1 -> theme.wallTopColor
                    2 -> Color(0xFFD97706)
                    else -> Color(0xFF475569)
                }
                drawRect(
                    color = lidColor,
                    topLeft = Offset(left, top - wallHeight),
                    size = Size(cellW, cellH)
                )

                // PARTIAL FLUFFY SNOW BLANKET COVER OVER CRATES / GENERATORS
                if (isSnowTheme) {
                    // Left soft heap
                    drawCircle(
                        color = Color(0xF0FAFAFA),
                        radius = cellW * 0.36f,
                        center = Offset(left + cellW * 0.32f, top - wallHeight + cellH * 0.35f)
                    )
                    // Right fluffy drift
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = cellW * 0.28f,
                        center = Offset(left + cellW * 0.68f, top - wallHeight + cellH * 0.45f)
                    )
                    // Organic accent shading shadow
                    drawCircle(
                        color = Color(0xFFCBD5E1), // Cool shadow white soil accent
                        radius = cellW * 0.16f,
                        center = Offset(left + cellW * 0.22f, top - wallHeight + cellH * 0.66f)
                    )
                    // Visual ice cap rim on side elevation (3D look)
                    drawRect(
                        color = Color.White.copy(0.92f),
                        topLeft = Offset(left + cellW * 0.12f, top + cellH - wallHeight - 1.5f),
                        size = Size(cellW * 0.45f, 3.5f)
                    )
                }

                // Top cap borders and panels details
                when (wallType) {
                    1 -> { // Cargo box container rib ribs
                        drawRect(
                            color = theme.decorationColor.copy(0.35f),
                            topLeft = Offset(left + 2F, top - wallHeight + 2F),
                            size = Size(cellW - 4F, cellH - 4F),
                            style = Stroke(width = 2.5f)
                        )
                        for (i in 2..10 step 2) {
                            val ribY = top - wallHeight + cellH * (i.toFloat() / 12f)
                            drawLine(
                                color = Color.Black.copy(0.25f),
                                start = Offset(left + 4f, ribY),
                                end = Offset(left + cellW - 4f, ribY),
                                strokeWidth = 2f
                            )
                        }
                    }
                    2 -> { // Wooden cargo crate top braces
                        drawRect(
                            color = Color.Black.copy(0.3f),
                            topLeft = Offset(left + 4f, top - wallHeight + 4f),
                            size = Size(cellW - 8f, cellH - 8f),
                            style = Stroke(width = 3f)
                        )
                        drawLine(
                            color = Color.Black.copy(0.35f),
                            start = Offset(left + 4f, top - wallHeight + 4f),
                            end = Offset(left + cellW - 4f, top - wallHeight + cellH - 4f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color.Black.copy(0.35f),
                            start = Offset(left + cellW - 4f, top - wallHeight + 4f),
                            end = Offset(left + 4f, top - wallHeight + cellH - 4f),
                            strokeWidth = 3f
                        )
                    }
                    3 -> { // Active reactor warning circular core
                        drawCircle(
                            color = Color.Black.copy(0.45f),
                            radius = cellW * 0.3f,
                            center = Offset(left + cellW / 2f, top - wallHeight + cellH / 2f)
                        )
                        drawCircle(
                            color = theme.decorationColor,
                            radius = cellW * 0.3f,
                            center = Offset(left + cellW / 2f, top - wallHeight + cellH / 2f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // 4. DRAW COLOURED FREEZE TRAP SENSORS ON FLOOR (Cyan/Blue Snowflakes)
            level.freezeTraps.forEach { trap ->
                val left = trap.x * cellW
                val top = trap.y * cellH
                val isExploded = explodedTraps.contains(trap)

                if (!isExploded) {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(0.18f),
                        radius = cellW * 0.38f,
                        center = Offset(left + cellW / 2, top + cellH / 2)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = cellW * 0.28f,
                        center = Offset(left + cellW / 2, top + cellH / 2),
                        style = Stroke(width = 3f)
                    )
                    val pulseColor = if (System.currentTimeMillis() % 1000 < 500) Color(0xFFFF1744) else Color(0xFF00E5FF)
                    drawCircle(
                        color = pulseColor,
                        radius = 7f,
                        center = Offset(left + cellW / 2, top + cellH / 2)
                    )
                } else {
                    drawCircle(
                         color = Color(0xFF1E293B),
                         radius = cellW * 0.2f,
                         center = Offset(left + cellW / 2, top + cellH / 2),
                         style = Stroke(width = 1.5f)
                    )
                }
            }

            // 5. DRAW ACTIVE TRIPWIRE SECURITY LASER LINES
            lasers.forEach { laser ->
                if (laser.isActive) {
                    val startX = laser.cellX * cellW + cellW / 2
                    val startY = laser.cellY * cellH + cellH / 2

                    val laserWidth = 8f * laserIntensity

                    val (endX, endY) = if (laser.isVertical) {
                        startX to (laser.cellY + laser.length) * cellH
                    } else {
                        (laser.cellX + laser.length) * cellW to startY
                    }

                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(laser.cellX * cellW + 4, laser.cellY * cellH + 4),
                        size = Size(cellW - 8, cellH - 8)
                    )

                    drawLine(
                        color = Color(0xFFFF0055).copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = laserWidth + 8f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                } else {
                    drawCircle(
                        color = Color(0xFF334155),
                        radius = 8f,
                        center = Offset(laser.cellX * cellW + cellW / 2, laser.cellY * cellH + cellH / 2)
                    )
                }
            }

            // NEW: DRAW DETAILED SECURITY OFFICE LOCKABLE DOORS
            level.lockedDoors.forEach { door ->
                val left = door.x * cellW
                val top = door.y * cellH
                val isUnlocked = unlockedDoors.contains(door)

                if (isUnlocked) {
                    // Open door slider (drawn shifted/translucent with glowing green status LED)
                    drawRect(
                        color = Color(0xFF10B981).copy(0.2f),
                        topLeft = Offset(left + 2f, top + 2f),
                        size = Size(cellW * 0.25f, cellH - 4f)
                    )
                    drawRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(left + 2f, top + 2f),
                        size = Size(cellW * 0.25f, cellH - 4f),
                        style = Stroke(width = 2f)
                    )
                    // Green open status badge
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 4f,
                        center = Offset(left + cellW / 2f, top + cellH / 2f)
                    )
                } else {
                    // Sturdy solid steel sliding hatch blocking passage (glowing red warning locks)
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(left + 4f, top + 4f),
                        size = Size(cellW - 8f, cellH - 8f)
                    )
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(left + 4f, top + 4f),
                        size = Size(cellW - 8f, cellH - 8f),
                        style = Stroke(width = 3.2f)
                    )
                    // Yellow/orange diagonal security warning stripes
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(left + 8f, top + 8f),
                        end = Offset(left + cellW - 8f, top + cellH - 8f),
                        strokeWidth = 3f
                    )
                    // Red lock status diode
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 5.5f,
                        center = Offset(left + cellW / 2f, top + cellH / 2f)
                    )
                }
            }

            // NEW: DRAW STEALABLE CASH STASH BAGS / VALUABLES
            level.cashStashes.forEach { stash ->
                val containsStash = gems.any { !it.isCollected && abs(it.x - (stash.x + 0.5f)) < 0.1f && abs(it.y - (stash.y + 0.5f)) < 0.1f }
                if (containsStash) {
                    val sx = stash.x * cellW + cellW / 2
                    val sy = stash.y * cellH + cellH / 2
                    // Draw a detailed neon green cash bill shape with stack offsets
                    drawCircle(
                        color = Color(0xFF10B981).copy(0.25f),
                        radius = cellW * 0.45f,
                        center = Offset(sx, sy)
                    )
                    drawRoundRect(
                        color = Color(0xFF047857),
                        topLeft = Offset(sx - cellW * 0.35f, sy - cellH * 0.2f),
                        size = Size(cellW * 0.7f, cellH * 0.4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    drawRect(
                        color = Color(0xFF34D399),
                        topLeft = Offset(sx - cellW * 0.35f, sy - cellH * 0.2f),
                        size = Size(cellW * 0.7f, cellH * 0.4f),
                        style = Stroke(width = 2f)
                    )
                    // Bill center emblem
                    drawCircle(
                        color = Color(0xFF34D399),
                        radius = 6f,
                        center = Offset(sx, sy)
                    )
                }
            }

            // NEW: DRAW FLOATING YELLOW DISGUISE EXTRA CAMOUFLAGE BOX POWERUP
            if (yellowBoxPowerUpLocation != null) {
                val bx = yellowBoxPowerUpLocation.x * cellW + cellW / 2
                val by = yellowBoxPowerUpLocation.y * cellH + cellH / 2
                val pulseScale = 1f + 0.1f * sin(System.currentTimeMillis() * 0.008f)
                val powerupRadius = cellW * 0.42f * pulseScale

                // Golden holographic outline ring
                drawCircle(
                    color = Color(0xFFF59E0B).copy(0.25f),
                    radius = powerupRadius * 1.3f,
                    center = Offset(bx, by)
                )
                // Outer gold yellow square representing the high tech nanotech crate
                drawRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(bx - cellW * 0.28f, by - cellH * 0.28f),
                    size = Size(cellW * 0.56f, cellH * 0.56f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(bx - cellW * 0.28f, by - cellH * 0.28f),
                    size = Size(cellW * 0.56f, cellH * 0.56f),
                    style = Stroke(width = 2f)
                )
                // Detailed inner white key emblem represent nanotech
                drawCircle(
                    color = Color.White,
                    radius = 5.5f,
                    center = Offset(bx, by)
                )
            }

            // 6. DRAW LOOTABLE DIAMONDS SCATTERED GEMS
            gems.forEach { gem ->
                if (!gem.isCollected) {
                    val gx = gem.x * cellW + cellW / 2
                    val gy = gem.y * cellH + cellH / 2 + (sineFloatingOffset(gem.scaleOffset) * gemFloatAnim)

                    val gemSize = cellW * 0.38f
                    val hgSquare = gemSize / 2

                    val path = Path().apply {
                        moveTo(gx, gy - hgSquare)
                        lineTo(gx + hgSquare, gy)
                        lineTo(gx, gy + hgSquare)
                        lineTo(gx - hgSquare, gy)
                        close()
                    }

                    val gemColor = if (gem.id % 2 == 0) Color(0xFF00FFCC) else Color(0xFFFF0055)

                    drawCircle(
                        color = gemColor.copy(0.35f),
                        radius = gemSize,
                        center = Offset(gx, gy)
                    )

                    drawPath(path = path, color = gemColor)
                    drawPath(path = path, color = Color.White.copy(0.75f), style = Stroke(width = 2f))
                }
            }

            // 7. DRAW PLANNED VIRTUAL DIRECT PATHINDICATOR DOTS (HIGHLIGHTED GLOWING SLIDING CORNER CURVE)
            if (pathPoints.isNotEmpty()) {
                val flowPath = Path().apply {
                    moveTo(playerX * cellW + cellW / 2, playerY * cellH + cellH / 2)
                    pathPoints.forEach { pt ->
                        lineTo(pt.x * cellW + cellW / 2, pt.y * cellH + cellH / 2)
                    }
                }

                // Outer neon glow path line padding (ሴንተሩ እንዲደምቅ የጀርባ ፍካት መስመር)
                drawPath(
                    path = flowPath,
                    color = Color(0xFF00FFFF).copy(0.32f),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(cellW * 0.45f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Central high-highlight bright core white-cyan streak (ሴንተሩ ደመቅ ይበል)
                drawPath(
                    path = flowPath,
                    color = Color(0xFFFFFFFF),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(cellW * 0.45f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Vertices / navigation points blinking nodes overlay
                pathPoints.forEachIndexed { idx, pt ->
                    val px = pt.x * cellW + cellW / 2
                    val py = pt.y * cellH + cellH / 2
                    val pulseAlpha = 0.42f + 0.35f * sin((System.currentTimeMillis() / 70f) + idx)
                    drawCircle(
                        color = Color(0xFF00F2FF).copy(pulseAlpha.coerceIn(0.15f, 0.95f)),
                        radius = 5.5f.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // 8. DRAW ACTIVE ENEMY GUARDS (FOVs with occluding raycasts + Outlines + Spottings)
            guards.forEach { guard ->
                if (guard.status != GuardStatus.Dead) {
                    val gx = guard.x * cellW
                    val gy = guard.y * cellH

                    // Raycasted dynamic FOV Cone that stops / occlusion trims on hitting any wall coordinate
                    val fovAngleRange = 75f
                    val visionMaxCellsRange = 4.2f
                    val fovRadDistance = visionMaxCellsRange * cellW
                    val baseRayAngle = guard.directionAngle

                    // 2D grid raycasting: sample 20 radial sectors
                    val rayCount = 20
                    val fovPath = Path()
                    fovPath.moveTo(gx, gy)

                    for (r in 0..rayCount) {
                        val angleOffset = -fovAngleRange / 2f + (fovAngleRange * r.toFloat() / rayCount.toFloat())
                        val rayAngleRad = Math.toRadians((baseRayAngle + angleOffset).toDouble())
                        var finalRayLength = fovRadDistance

                        // Trace step-by-step to check if we crash inside a wall
                        val stepCount = 12
                        for (step in 1..stepCount) {
                            val offsetDist = (step.toFloat() / stepCount.toFloat()) * fovRadDistance
                            val checkWorldX = (gx + cos(rayAngleRad).toFloat() * offsetDist) / cellW
                            val checkWorldY = (gy + sin(rayAngleRad).toFloat() * offsetDist) / cellH

                            val gridX = checkWorldX.toInt()
                            val gridY = checkWorldY.toInt()
                            if (level.walls.contains(GridPoint(gridX, gridY))) {
                                finalRayLength = offsetDist
                                break
                            }
                        }

                        val pxEdge = gx + cos(rayAngleRad).toFloat() * finalRayLength
                        val pyEdge = gy + sin(rayAngleRad).toFloat() * finalRayLength
                        fovPath.lineTo(pxEdge, pyEdge)
                    }
                    fovPath.close()

                    // Glowing Searchlight Brush (daytime has soft tactical cone, night has flashlight)
                    val coneBaseColor = when (guard.status) {
                        GuardStatus.AlertShooting -> Color(0xFFFF1744)
                        GuardStatus.Investigating -> Color(0xFFFF9100)
                        GuardStatus.Frozen -> Color(0xFF00E5FF)
                        else -> if (theme.isDaytime) Color(0xFF475569) else Color(0xFFFEF08A)
                    }

                    val baseConeAlpha = if (theme.isDaytime && guard.status == GuardStatus.Patrolling) 0.08f else 0.42f
                    val targetX = gx + cos(Math.toRadians(baseRayAngle.toDouble())).toFloat() * fovRadDistance
                    val targetY = gy + sin(Math.toRadians(baseRayAngle.toDouble())).toFloat() * fovRadDistance

                    val flashlightBrush = Brush.linearGradient(
                        colors = listOf(
                            coneBaseColor.copy(alpha = baseConeAlpha),
                            coneBaseColor.copy(alpha = baseConeAlpha * 0.4f),
                            Color.Transparent
                        ),
                        start = Offset(gx, gy),
                        end = Offset(targetX, targetY)
                    )

                    drawPath(path = fovPath, brush = flashlightBrush)
                    drawPath(path = fovPath, color = coneBaseColor.copy(alpha = baseConeAlpha * 0.8f), style = Stroke(width = 2f))

                    // Draw Selection Track lock-on reticle!
                    if (trackedGuardId == guard.id) {
                        val pulseOutlineScale = 1.0f + 0.15f * sin(System.currentTimeMillis() * 0.008f)
                        drawCircle(
                            color = Color(0xFFFF0055).copy(0.2f),
                            radius = cellW * 0.75f * pulseOutlineScale,
                            center = Offset(gx, gy)
                        )
                        drawCircle(
                            color = Color(0xFFFF0055),
                            radius = cellW * 0.75f * pulseOutlineScale,
                            center = Offset(gx, gy),
                            style = Stroke(width = 3.5f)
                        )
                        // Reticle compass hairs
                        for (compassDir in 0..3) {
                            val hairAngle = (compassDir * 90f) * (PI.toFloat() / 180f)
                            drawLine(
                                color = Color(0xFFFF0055),
                                start = Offset(gx + cos(hairAngle) * cellW * 0.5f * pulseOutlineScale, gy + sin(hairAngle) * cellW * 0.5f * pulseOutlineScale),
                                end = Offset(gx + cos(hairAngle) * cellW * 0.9f * pulseOutlineScale, gy + sin(hairAngle) * cellW * 0.9f * pulseOutlineScale),
                                strokeWidth = 3f
                            )
                        }
                    }

                    // DRAW GUARD CHARACTER CONTOUR IN 2.5D WITH WALKING LEG ANIMATION
                    val angleRad = Math.toRadians(baseRayAngle.toDouble())
                    var muzzleFlashCenter = Offset(gx, gy)
                    val isSecretDrone = guard.isSecurityRobot
                    if (isSecretDrone) {
                        // Futuristic rotating security vacuum drone disk
                        val droneRadius = cellW * 0.35f
                        val pulseSec = sin(System.currentTimeMillis() * 0.01f) * 3f
                        val droneCenter = Offset(gx, gy + pulseSec)
                        muzzleFlashCenter = droneCenter
                        
                        // Main disk body
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = droneRadius,
                            center = droneCenter
                        )
                        // Outer glowing circular bezel stripe
                        drawCircle(
                            color = Color(0xFF06B6D4), // Cyan neon light
                            radius = droneRadius,
                            center = droneCenter,
                            style = Stroke(width = 4.5f)
                        )
                        // Robotic blinking core eye
                        val eyePulsate = if (System.currentTimeMillis() % 400 < 200) Color(0xFF06B6D4) else Color(0xFFE0F2FE)
                        drawCircle(
                            color = eyePulsate,
                            radius = droneRadius * 0.3f,
                            center = droneCenter
                        )
                        // Direction indicator antennas
                        drawLine(
                            color = Color(0xFF06B6D4),
                            start = droneCenter,
                            end = Offset(
                                droneCenter.x + cos(angleRad).toFloat() * droneRadius * 1.5f,
                                droneCenter.y + sin(angleRad).toFloat() * droneRadius * 1.5f
                            ),
                            strokeWidth = 3f
                        )
                    } else {
                        val bodyColor = if (guard.status == GuardStatus.Frozen) Color(0xFFB2EBF2) else Color(0xFF1E293B)
                        val trimColor = if (guard.status == GuardStatus.Frozen) Color(0xFF00B0FF) else {
                            if (guard.isElitePolice) Color(0xFF3B82F6) else Color(0xFFEF4444)
                        }

                        val animTicks = System.currentTimeMillis() / 150f
                        val walkCycle = sin(animTicks)

                        val isMoving = guard.status == GuardStatus.Investigating || guard.status == GuardStatus.Patrolling
                        val footSwingOffset = if (isMoving && guard.pauseTicksLeft <= 0) walkCycle * 0.4f else 0f
                        val handSwingOffset = if (isMoving && guard.pauseTicksLeft <= 0) -walkCycle * 0.35f else 0f

                        // Calculate guard feet offset coordinates
                        val leftFootX = gx + cos(angleRad - 1.8 + footSwingOffset).toFloat() * (cellW * 0.25f)
                        val leftFootY = gy + sin(angleRad - 1.8 + footSwingOffset).toFloat() * (cellW * 0.25f)
                        val rightFootX = gx + cos(angleRad + 1.8 - footSwingOffset).toFloat() * (cellW * 0.25f)
                        val rightFootY = gy + sin(angleRad + 1.8 - footSwingOffset).toFloat() * (cellW * 0.25f)

                        // Draw guard feet
                        drawCircle(color = Color(0xFF0F172A), radius = 8f, center = Offset(leftFootX, leftFootY))
                        drawCircle(color = Color(0xFF0F172A), radius = 8f, center = Offset(rightFootX, rightFootY))

                        // Calculate guard swinging hands offset coordinates
                        val leftHandX = gx + cos(angleRad - 1.0 + handSwingOffset).toFloat() * (cellW * 0.34f)
                        val leftHandY = gy + sin(angleRad - 1.0 + handSwingOffset).toFloat() * (cellW * 0.34f)
                        val rightHandX = gx + cos(angleRad + 1.0 - handSwingOffset).toFloat() * (cellW * 0.34f)
                        val rightHandY = gy + sin(angleRad + 1.0 - handSwingOffset).toFloat() * (cellW * 0.34f)

                        // Draw guard hands with clean border outlines (የጋርዶቹ የእጅ እንቅስቃሴ)
                        drawCircle(color = bodyColor.copy(0.9f), radius = 6f, center = Offset(leftHandX, leftHandY))
                        drawCircle(color = bodyColor.copy(0.9f), radius = 6f, center = Offset(rightHandX, rightHandY))
                        drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(leftHandX, leftHandY), style = Stroke(width = 1.8f))
                        drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(rightHandX, rightHandY), style = Stroke(width = 1.8f))

                        drawCircle(color = Color(0x60000000), radius = cellW * 0.38f, center = Offset(gx + 2f, gy + 5f))

                        val torsoCenter = Offset(gx, gy - 3f)
                        drawCircle(
                            color = bodyColor,
                            radius = cellW * 0.36f,
                            center = torsoCenter
                        )
                        drawCircle(
                            color = trimColor,
                            radius = cellW * 0.36f,
                            center = torsoCenter,
                            style = Stroke(width = 4f)
                        )

                        // If Elite Police, draw blinking red and blue shoulder siren lights
                        if (guard.isElitePolice) {
                            val isRedLight = (System.currentTimeMillis() % 300L < 150L)
                            val leftSirenColor = if (isRedLight) Color.Red else Color.Blue
                            val rightSirenColor = if (isRedLight) Color.Blue else Color.Red
                            
                            val sirenL = Offset(gx - cellW * 0.3f, gy - 3f)
                            val sirenR = Offset(gx + cellW * 0.3f, gy - 3f)
                            drawCircle(color = leftSirenColor, radius = 5.5f, center = sirenL)
                            drawCircle(color = rightSirenColor, radius = 5.5f, center = sirenR)
                        }

                        val headCenter = Offset(gx, gy - 8f)
                        drawCircle(
                            color = if (guard.status == GuardStatus.Frozen) Color(0xFF00E5FF) else Color(0xFF0F172A),
                            radius = cellW * 0.22f,
                            center = headCenter
                        )

                        val goggleX = gx + cos(angleRad).toFloat() * cellW * 0.18f
                        val goggleY = gy - 8f + sin(angleRad).toFloat() * cellW * 0.18f
                        drawCircle(
                            color = trimColor,
                            radius = 4.5f,
                            center = Offset(goggleX, goggleY)
                        )

                        val rifleBarStart = gx + cos(angleRad).toFloat() * cellW * 0.28f
                        val rifleBarStartY = gy - 3f + sin(angleRad).toFloat() * cellW * 0.28f
                        val rifleBarEnd = gx + cos(angleRad).toFloat() * cellW * 0.72f
                        val rifleBarEndY = gy - 3f + sin(angleRad).toFloat() * cellW * 0.72f
                        muzzleFlashCenter = Offset(rifleBarEnd, rifleBarEndY)

                        drawLine(
                            color = if (guard.status == GuardStatus.Frozen) Color(0xFF00B0FF) else Color(0xFF334155),
                            start = Offset(rifleBarStart, rifleBarStartY),
                            end = Offset(rifleBarEnd, rifleBarEndY),
                            strokeWidth = 6f
                        )
                    }

                    if (guard.status == GuardStatus.AlertShooting) {
                        drawCircle(
                            color = Color(0xFFFF0055),
                            radius = 4f,
                            center = muzzleFlashCenter
                        )
                    }

                    if (guard.alertLevel > 0f) {
                        drawRect(
                            color = Color.Black.copy(0.7f),
                            topLeft = Offset(gx - cellW * 0.4f, gy - cellH * 0.9f),
                            size = Size(cellW * 0.8f, 10f)
                        )
                        drawRect(
                            color = if (guard.status == GuardStatus.AlertShooting) Color(0xFFFF0055) else Color(0xFFFF9100),
                            topLeft = Offset(gx - cellW * 0.38f, gy - cellH * 0.88f),
                            size = Size((cellW * 0.76f) * guard.alertLevel, 6f)
                        )
                    }
                }
            }

            // 9. DRAW PLAYABLE HERO CHARACTER AT ACTIVE WORLD COORDINATES WITH BREATHING IDLE ANIMATION
            val px = playerX * cellW
            val py = playerY * cellH

            val isPlayerMoving = pathPoints.isNotEmpty()
            val breathingYOffset = if (!isPlayerMoving && helicopterStatus == HelicopterStatus.None) {
                sin(System.currentTimeMillis() * 0.005f) * 3f
            } else {
                0f
            }

            // Draw selection spotlight footprint aura
            drawCircle(
                color = selectedHero.accentColor.copy(0.18f),
                radius = cellW * 0.65f,
                center = Offset(px, py)
            )
            drawCircle(
                color = selectedHero.accentColor.copy(0.45f),
                radius = cellW * 0.65f,
                center = Offset(px, py),
                style = Stroke(width = 2.5f)
            )

            // Dynamic walking feet and hands swing for the Player (የእጅ እና የግር እንቅስቃሴዎች)
            val pAnimTicks = System.currentTimeMillis() / 120f
            val pWalkCycle = sin(pAnimTicks)
            val pAngleRad = Math.toRadians(playerRot.toDouble())

            val pFootSwing = if (isPlayerMoving) pWalkCycle * 0.5f else 0f
            val pHandSwing = if (isPlayerMoving) -pWalkCycle * 0.4f else 0f

            // Calculate feet offset positions
            val pLeftFootX = px + cos(pAngleRad - 1.8 + pFootSwing).toFloat() * (cellW * 0.26f)
            val pLeftFootY = py + sin(pAngleRad - 1.8 + pFootSwing).toFloat() * (cellW * 0.26f)
            val pRightFootX = px + cos(pAngleRad + 1.8 - pFootSwing).toFloat() * (cellW * 0.26f)
            val pRightFootY = py + sin(pAngleRad + 1.8 - pFootSwing).toFloat() * (cellW * 0.26f)

            // Draw player feet
            drawCircle(color = Color(0xFF0F172A), radius = 8.5f, center = Offset(pLeftFootX, pLeftFootY))
            drawCircle(color = Color(0xFF0F172A), radius = 8.5f, center = Offset(pRightFootX, pRightFootY))

            // Calculate swinging hands offset positions
            val pLeftHandX = px + cos(pAngleRad - 1.0 + pHandSwing).toFloat() * (cellW * 0.35f)
            val pLeftHandY = py + sin(pAngleRad - 1.0 + pHandSwing).toFloat() * (cellW * 0.35f)
            val pRightHandX = px + cos(pAngleRad + 1.0 - pHandSwing).toFloat() * (cellW * 0.35f)
            val pRightHandY = py + sin(pAngleRad + 1.0 - pHandSwing).toFloat() * (cellW * 0.35f)

            // Draw player hands (swinging dynamically)
            drawCircle(color = selectedHero.baseColor.copy(0.9f), radius = 6.5f, center = Offset(pLeftHandX, pLeftHandY))
            drawCircle(color = selectedHero.baseColor.copy(0.9f), radius = 6.5f, center = Offset(pRightHandX, pRightHandY))
            drawCircle(color = Color(0xFF0F172A), radius = 6.5f, center = Offset(pLeftHandX, pLeftHandY), style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF0F172A), radius = 6.5f, center = Offset(pRightHandX, pRightHandY), style = Stroke(width = 2f))

            drawCircle(color = Color(0x6F000000), radius = cellW * 0.38f, center = Offset(px + 2f, py + 5f))

            val isFullyDisguised = disguiseProgressPercentage == 100
            val isYellowBoxCamo = yellowBoxCamoTimeLeft > 0L

            if (isFullyDisguised || isYellowBoxCamo) {
                // Draw a cardboard box disguise over the player!
                val boxSize = cellW * 0.8f
                val boxColor = if (isYellowBoxCamo) Color(0xFFEAB308) else Color(0xFFD97706) // Golden/Yellow if camo, brown if normal box
                
                // Outer box
                drawRect(
                    color = boxColor,
                    topLeft = Offset(px - boxSize / 2f, py - boxSize / 2f + breathingYOffset),
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize)
                )
                // Outline
                drawRect(
                    color = Color(0xFF451A03),
                    topLeft = Offset(px - boxSize / 2f, py - boxSize / 2f + breathingYOffset),
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                    style = Stroke(width = 3f)
                )
                // Cardboard box tape decoration
                drawLine(
                    color = Color(0xFF78350F),
                    start = Offset(px - boxSize / 2f, py + breathingYOffset),
                    end = Offset(px + boxSize / 2f, py + breathingYOffset),
                    strokeWidth = 5f
                )
                // Draw dynamic warning glowing ring if Yellow Box countdown is running
                if (isYellowBoxCamo) {
                    drawCircle(
                        color = Color(0xFFEAB308).copy(0.4f),
                        radius = boxSize * 0.8f,
                        center = Offset(px, py + breathingYOffset),
                        style = Stroke(width = 2f)
                    )
                }
            } else {
                // 2.5D Torso and armor chest piece offset up (py - 3f + breathing offset)
                val pTorsoCenter = Offset(px, py - 3f + breathingYOffset)
                drawCircle(
                    color = selectedHero.baseColor,
                    radius = cellW * 0.32f,
                    center = pTorsoCenter
                )
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = cellW * 0.24f,
                    center = pTorsoCenter,
                    style = Stroke(width = 3.5f)
                )

                // 2.5D Head/hair capsule offset up (py - 8f + breathing offset)
                val pHeadCenter = Offset(px, py - 8f + breathingYOffset)
                drawCircle(
                    color = selectedHero.accentColor,
                    radius = cellW * 0.2f,
                    center = pHeadCenter
                )

                val pVisorX = px + cos(pAngleRad).toFloat() * cellW * 0.16f
                val pVisorY = py - 8f + breathingYOffset + sin(pAngleRad).toFloat() * cellW * 0.16f
                drawCircle(
                    color = Color(0xFF00F2FF),
                    radius = 4f,
                    center = Offset(pVisorX, pVisorY)
                )

                val handX = px + cos(pAngleRad + 0.45f).toFloat() * cellW * 0.32f
                val handY = py - 3f + breathingYOffset + sin(pAngleRad + 0.45f).toFloat() * cellW * 0.32f
                val bladeX = px + cos(pAngleRad + 0.5f).toFloat() * cellW * 0.72f
                val bladeY = py - 3f + breathingYOffset + sin(pAngleRad + 0.5f).toFloat() * cellW * 0.72f

                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 9.5f,
                    center = Offset(handX, handY)
                )

                drawLine(
                    color = Color(0xFF00F2FF).copy(0.6f),
                    start = Offset(handX, handY),
                    end = Offset(bladeX, bladeY),
                    strokeWidth = 8f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(handX, handY),
                    end = Offset(bladeX, bladeY),
                    strokeWidth = 3.2f
                )
            }

            // Draw disguise loading circular progress stroke overhead
            if (disguiseProgressPercentage != null && disguiseProgressPercentage < 100) {
                val progressAngle = (disguiseProgressPercentage / 100f) * 360f
                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = -90f,
                    sweepAngle = progressAngle,
                    useCenter = false,
                    topLeft = Offset(px - 16.dp.toPx(), py - 32.dp.toPx() + breathingYOffset),
                    size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 32.dp.toPx()),
                    style = Stroke(width = 5f)
                )
            }

            // 10. DRAW ADVANCED HIGH-SPEED VISUAL PARTICLE & TRACER EFFECTS
            effects.forEach { effect ->
                val ex = effect.x * cellW
                val ey = effect.y * cellH

                when (effect.type) {
                    EffectType.Slash -> {
                        val sweepRad = Math.toRadians(effect.extraAngle.toDouble())
                        val strikePath = Path()
                        val slashRadius = cellW * 0.6f

                        val arcSx = ex + cos(sweepRad - 1.2).toFloat() * slashRadius
                        val arcSy = ey + sin(sweepRad - 1.2).toFloat() * slashRadius
                        val arcEx = ex + cos(sweepRad + 1.2).toFloat() * slashRadius
                        val arcEy = ey + sin(sweepRad + 1.2).toFloat() * slashRadius

                        strikePath.moveTo(arcSx, arcSy)
                        strikePath.quadraticTo(
                            ex + cos(sweepRad).toFloat() * slashRadius * 1.4f,
                            ey + sin(sweepRad).toFloat() * slashRadius * 1.4f,
                            arcEx,
                            arcEy
                        )

                        drawPath(
                            path = strikePath,
                            color = Color(0xFF00F2FF),
                            style = Stroke(width = 5f)
                        )
                        drawPath(
                            path = strikePath,
                            color = Color.White,
                            style = Stroke(width = 2f)
                        )
                    }

                    EffectType.BloodSplatter -> {
                        for (i in 0 until 5) {
                            val degree = (i * 72f) * (PI.toFloat() / 180f)
                            val r = (cellW * 0.3f) * (1f + i % 2 * 0.3f)
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = 6f,
                                center = Offset(ex + cos(degree) * r, ey + sin(degree) * r)
                            )
                        }
                    }

                    EffectType.ShotTracer -> {
                        val actualEndX = effect.endX * cellW
                        val actualEndY = effect.endY * cellH
                        drawLine(
                            color = Color(0xFFFF007A).copy(0.6f),
                            start = Offset(ex, ey),
                            end = Offset(actualEndX, actualEndY),
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(ex, ey),
                            end = Offset(actualEndX, actualEndY),
                            strokeWidth = 1.5f
                        )
                    }

                    EffectType.AlertExclamation -> {
                        drawCircle(
                            color = Color(0xFFFF007A),
                            radius = 12f,
                            center = Offset(ex, ey)
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(ex, ey - 4),
                            end = Offset(ex, ey + 1),
                            strokeWidth = 3f
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.5f,
                            center = Offset(ex, ey + 5)
                        )
                    }

                    EffectType.FreezeWave -> {
                        val progressRatio = (System.currentTimeMillis() - effect.timestamp).toFloat() / effect.durationMs.toFloat()
                        val waveRadius = cellW * 3.5f * progressRatio.coerceIn(0f, 1f)

                        drawCircle(
                            color = Color(0xFFE0F7FA).copy(0.12f * (1f - progressRatio)),
                            radius = waveRadius,
                            center = Offset(ex, ey)
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(0.4f * (1f - progressRatio)),
                            radius = waveRadius,
                            center = Offset(ex, ey),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            // 11. DRAW DYNAMIC CINEMATIC AERIAL HELICOPTER DURING ESCAPE SEQUENCE
            if (helicopterStatus != HelicopterStatus.None) {
                val hx = helicopterX * cellW
                val hy = helicopterY * cellH

                val bladesAngle = (System.currentTimeMillis() % 360).toFloat() * (PI.toFloat() / 180f)

                withTransform({
                    this.scale(helicopterScale, helicopterScale, Offset(hx, hy))
                }) {
                    // Physical drop shadow shifts relative to scale
                    val shadowOffset = cellW * 0.35f * helicopterScale
                    drawOval(
                        color = Color.Black.copy(0.45f),
                        topLeft = Offset(hx - cellW * 1.1f + shadowOffset, hy - cellH * 0.5f + shadowOffset),
                        size = Size(cellW * 2.2f, cellH * 1.0f)
                    )

                    // 1. High-Tech Forward Tactical Searchlight Cone Beam
                    val searchlightPath = Path().apply {
                        moveTo(hx + cellW * 0.6f, hy)
                        lineTo(hx + cellW * 5.0f, hy - cellH * 2.2f)
                        lineTo(hx + cellW * 5.0f, hy + cellH * 2.2f)
                        close()
                    }
                    drawPath(
                        path = searchlightPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00FFD5).copy(0.38f),
                                Color(0xFF00E5FF).copy(0.14f),
                                Color.Transparent
                            ),
                            start = Offset(hx + cellW * 0.6f, hy),
                            end = Offset(hx + cellW * 5.0f, hy)
                        )
                    )

                    // Landing skids
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(hx - cellW * 0.8f, hy - cellH * 0.45f),
                        end = Offset(hx + cellW * 0.6f, hy - cellH * 0.45f),
                        strokeWidth = 5.5f * helicopterScale
                    )
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(hx - cellW * 0.8f, hy + cellH * 0.45f),
                        end = Offset(hx + cellW * 0.6f, hy + cellH * 0.45f),
                        strokeWidth = 5.5f * helicopterScale
                    )
                    // Skid cross-bars
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(hx - cellW * 0.3f, hy - cellH * 0.45f),
                        end = Offset(hx - cellW * 0.3f, hy + cellH * 0.45f),
                        strokeWidth = 3.5f * helicopterScale
                    )
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(hx + cellW * 0.2f, hy - cellH * 0.45f),
                        end = Offset(hx + cellW * 0.2f, hy + cellH * 0.45f),
                        strokeWidth = 3.5f * helicopterScale
                    )

                    // Tail boom assembly
                    val tailBoomEndX = hx - cellW * 1.9f
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(hx, hy),
                        end = Offset(tailBoomEndX, hy),
                        strokeWidth = 8f * helicopterScale
                    )
                    
                    // LED Glowing Flashing beacons (ብልጭልጭ መብራቶች)
                    val strobeActive = (System.currentTimeMillis() / 320) % 2 == 0L
                    drawCircle(
                        color = if (strobeActive) Color(0xFFFF0055) else Color(0x33FF0055),
                        radius = 8f * helicopterScale,
                        center = Offset(tailBoomEndX, hy)
                    )
                    drawCircle(
                        color = if (!strobeActive) Color(0xFF00FFCC) else Color(0x3300FFCC),
                        radius = 6f * helicopterScale,
                        center = Offset(hx + cellW * 0.4f, hy - cellH * 0.36f)
                    )

                    // Cabin fuselage with high-fidelity metallic gradient
                    val cabinBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF475569), // Slate Highlight
                            Color(0xFF1E293B), // Navy steel body
                            Color(0xFF090D16)  // Pitch shadows
                        ),
                        center = Offset(hx - cellW * 0.2f, hy - cellH * 0.1f),
                        radius = cellW * 1.0f
                    )
                    drawOval(
                        brush = cabinBrush,
                        topLeft = Offset(hx - cellW * 0.95f, hy - cellH * 0.45f),
                        size = Size(cellW * 1.9f, cellH * 0.9f)
                    )
                    
                    // High-tech warning decal striping
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = cellW * 0.15f,
                        center = Offset(hx - cellW * 0.2f, hy),
                        style = Stroke(width = 3.5f)
                    )

                    // Futuristic Cyan Windshield / Cockpit
                    val glassPath = Path().apply {
                        moveTo(hx + cellW * 0.5f, hy - cellH * 0.26f)
                        quadraticTo(hx + cellW * 0.92f, hy, hx + cellW * 0.5f, hy + cellH * 0.26f)
                        close()
                    }
                    drawPath(path = glassPath, color = Color(0xC000F2FF))
                    drawPath(path = glassPath, color = Color(0xFFE0F2FE), style = Stroke(width = 1.5f))

                    // Main spinning rotor blades
                    val bLen = cellW * 2.3f
                    val bx1 = hx + cos(bladesAngle) * bLen
                    val by1 = hy + sin(bladesAngle) * bLen
                    val bx2 = hx - cos(bladesAngle) * bLen
                    val by2 = hy - sin(bladesAngle) * bLen

                    drawLine(
                        color = Color.White.copy(0.9f),
                        start = Offset(hx, hy),
                        end = Offset(bx1, by1),
                        strokeWidth = 5f
                    )
                    drawLine(
                        color = Color.White.copy(0.9f),
                        start = Offset(hx, hy),
                        end = Offset(bx2, by2),
                        strokeWidth = 5f
                    )

                    val bx3 = hx + cos(bladesAngle + PI.toFloat() / 2) * bLen
                    val by3 = hy + sin(bladesAngle + PI.toFloat() / 2) * bLen
                    val bx4 = hx - cos(bladesAngle + PI.toFloat() / 2) * bLen
                    val by4 = hy - sin(bladesAngle + PI.toFloat() / 2) * bLen

                    drawLine(
                        color = Color.White.copy(0.6f),
                        start = Offset(hx, hy),
                        end = Offset(bx3, by3),
                        strokeWidth = 3.5f
                    )
                    drawLine(
                        color = Color.White.copy(0.6f),
                        start = Offset(hx, hy),
                        end = Offset(bx4, by4),
                        strokeWidth = 3.5f
                    )

                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 9f,
                        center = Offset(hx, hy)
                    )
                }
            }
        }

        val isRainActive = level.levelNumber in listOf(16, 20, 24) || customForceWeather == "Rain"
        if (isRainActive) {
            val time = System.currentTimeMillis()
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w > 0 && h > 0) {
                for (i in 0 until 60) {
                    val startX = ((i * 12345 + time * 3) % w).toFloat()
                    val startY = ((i * 54321 + time * 8) % h).toFloat()
                    drawLine(
                        color = Color(0xFF38BDF8).copy(0.45f),
                        start = Offset(startX, startY),
                        end = Offset(startX - 10f, startY + 28f),
                        strokeWidth = 2.5f
                    )
                }
            }
        }

        val isSnowActive = level.levelNumber in listOf(17, 21, 25) || customForceWeather == "Snow"
        if (isSnowActive) {
            val time = System.currentTimeMillis()
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w > 0 && h > 0) {
                for (i in 0 until 50) {
                    val startX = ((i * 23456 + time) % w).toFloat()
                    val sway = sin(time * 0.002f + i) * 15f
                    val startY = ((i * 34567 + time * 2) % h).toFloat()
                    drawCircle(
                        color = Color.White.copy(0.6f),
                        radius = 2.5f + (i % 3),
                        center = Offset(startX + sway, startY)
                    )
                }
            }
        }
    }
}

private fun sineFloatingOffset(phaseOffset: Float): Float {
    return sin(System.currentTimeMillis().toFloat() / 200f + phaseOffset * 2f * PI.toFloat())
}

@Composable
fun BottomControlsPanel(
    onPause: () -> Unit,
    activeLevel: LevelData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SECTOR SECURITY // INTRUSION",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "STATUS: ACTIVE COMBAT MAPPED",
                    color = Color(0xFFFF007A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Quick instruction help tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .clickable { onPause() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "⏸️ PAUSE MISSION",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RadarPulseCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFF00F2FF),
            radius = (size.width / 2) * scale,
            style = Stroke(width = 2f)
        )
    }
}

data class LevelTheme(
    val floorColor: Color,
    val isGridVisible: Boolean,
    val gridColor: Color,
    val wallSideColor: Color,
    val wallTopColor: Color,
    val decorationColor: Color,
    val isDaytime: Boolean = false,
    val isSnowMode: Boolean = false
)

fun getThemeForLevel(levelNumber: Int): LevelTheme {
    return when (levelNumber) {
        1 -> LevelTheme(
            floorColor = Color(0xFF0F172A),
            isGridVisible = true,
            gridColor = Color(0x0C22D3EE),
            wallSideColor = Color(0xFF334155),
            wallTopColor = Color(0xFF475569),
            decorationColor = Color(0xFF00E5FF),
            isDaytime = false
        )
        2 -> LevelTheme(
            floorColor = Color(0xFF020B09),
            isGridVisible = true,
            gridColor = Color(0x1110B981),
            wallSideColor = Color(0xFF064E3B),
            wallTopColor = Color(0xFF047857),
            decorationColor = Color(0xFF34D399),
            isDaytime = false
        )
        3 -> LevelTheme(
            floorColor = Color(0xFF06151D),
            isGridVisible = true,
            gridColor = Color(0x1838BDF8),
            wallSideColor = Color(0xFF0C4A6E),
            wallTopColor = Color(0xFF0284C7),
            decorationColor = Color(0xFF38BDF8),
            isDaytime = false
        )
        4 -> LevelTheme(
            floorColor = Color(0xFF140707),
            isGridVisible = true,
            gridColor = Color(0x11FCA5A5),
            wallSideColor = Color(0xFF7F1D1D),
            wallTopColor = Color(0xFF991B1B),
            decorationColor = Color(0xFFF87171),
            isDaytime = false
        )
        5 -> LevelTheme(
            floorColor = Color(0xFF080410),
            isGridVisible = true,
            gridColor = Color(0x14A85EEF),
            wallSideColor = Color(0xFF4C1D95),
            wallTopColor = Color(0xFF5B21B6),
            decorationColor = Color(0xFFC084FC),
            isDaytime = false
        )
        6 -> LevelTheme( // Desert Outpost - Warm Sandy Day
            floorColor = Color(0xFFFEF08A),
            isGridVisible = true,
            gridColor = Color(0x22B45309),
            wallSideColor = Color(0xFF78350F),
            wallTopColor = Color(0xFF92400E),
            decorationColor = Color(0xFFD97706),
            isDaytime = true
        )
        7 -> LevelTheme( // Sunlit Lab Hangar - High Tech Corporate Polymer Day
            floorColor = Color(0xFFF1F5F9),
            isGridVisible = true,
            gridColor = Color(0x22475569),
            wallSideColor = Color(0xFF334155),
            wallTopColor = Color(0xFF64748B),
            decorationColor = Color(0xFF3B82F6),
            isDaytime = true
        )
        8 -> LevelTheme( // Grassy Emerald Base - Vibrant Forest Camp Green Day
            floorColor = Color(0xFFDCFCE7),
            isGridVisible = true,
            gridColor = Color(0x2215803D),
            wallSideColor = Color(0xFF14532D),
            wallTopColor = Color(0xFF166534),
            decorationColor = Color(0xFF22C55E),
            isDaytime = true
        )
        9 -> LevelTheme( // Solar Station Zenith - Clean futuristic carbon/chrome
            floorColor = Color(0xFFE2E8F0),
            isGridVisible = true,
            gridColor = Color(0x220F172A),
            wallSideColor = Color(0xFF1E293B),
            wallTopColor = Color(0xFF475569),
            decorationColor = Color(0xFFF59E0B),
            isDaytime = true
        )
        10 -> LevelTheme( // Grand Final Sandbox - Glorious Sunset Bronze Orange
            floorColor = Color(0xFFFFEDD5),
            isGridVisible = true,
            gridColor = Color(0x229A3412),
            wallSideColor = Color(0xFF7C2D12),
            wallTopColor = Color(0xFF9A3412),
            decorationColor = Color(0xFFEA580C),
            isDaytime = true
        )
        11 -> LevelTheme( // Corporate Vault Siphon - Luxury Deep Sapphire Dark Theme
            floorColor = Color(0xFF020617),
            isGridVisible = true,
            gridColor = Color(0x193B82F6),
            wallSideColor = Color(0xFF1E3A8A),
            wallTopColor = Color(0xFF2563EB),
            decorationColor = Color(0xFF60A5FA),
            isDaytime = false
        )
        12 -> LevelTheme( // Robotics Hangar - Futuristic Cyber Purple Dark Theme
            floorColor = Color(0xFF03000A),
            isGridVisible = true,
            gridColor = Color(0x19A855F7),
            wallSideColor = Color(0xFF581C87),
            wallTopColor = Color(0xFF7E22CE),
            decorationColor = Color(0xFFC084FC),
            isDaytime = false
        )
        13 -> LevelTheme( // Cipher Office Maze - Sleek Tech Slate Dark Theme
            floorColor = Color(0xFF0B0F19),
            isGridVisible = true,
            gridColor = Color(0x1910B981),
            wallSideColor = Color(0xFF064E3B),
            wallTopColor = Color(0xFF059669),
            decorationColor = Color(0xFF34D399),
            isDaytime = false
        )
        14 -> LevelTheme( // Mega Vault Headquarters - Highly Polished Midnight Blue
            floorColor = Color(0xFF050B14),
            isGridVisible = true,
            gridColor = Color(0x1606B6D4),
            wallSideColor = Color(0xFF155E75),
            wallTopColor = Color(0xFF0891B2),
            decorationColor = Color(0xFF22D3EE),
            isDaytime = false
        )
        15 -> LevelTheme( // Zenith Sky Fortress - Royal Midnight Purple & Crimson
            floorColor = Color(0xFF0D0314),
            isGridVisible = true,
            gridColor = Color(0x1AEC4899),
            wallSideColor = Color(0xFF831843),
            wallTopColor = Color(0xFF9D174D),
            decorationColor = Color(0xFFF472B6),
            isDaytime = false
        )
        16 -> LevelTheme( // Rainy Docks Patrol - Storm Ocean Blue Day
            floorColor = Color(0xFF0F1B2B),
            isGridVisible = true,
            gridColor = Color(0x223B82F6),
            wallSideColor = Color(0xFF1E293B),
            wallTopColor = Color(0xFF475569),
            decorationColor = Color(0xFF38BDF8),
            isDaytime = true
        )
        17 -> LevelTheme( // Arctic Outpost Zero - Frozen Antarctic Tundra Snow Day
            floorColor = Color(0xFFF8FAFC),
            isGridVisible = true,
            gridColor = Color(0x2294A3B8),
            wallSideColor = Color(0xFF475569),
            wallTopColor = Color(0xFF64748B),
            decorationColor = Color(0xFF38BDF8),
            isDaytime = true,
            isSnowMode = true
        )
        18 -> LevelTheme( // Shadow Blackout Mainframe - Absolute Pitch Black Vault
            floorColor = Color(0xFF02040A),
            isGridVisible = true,
            gridColor = Color(0x1C10B981),
            wallSideColor = Color(0xFF111827),
            wallTopColor = Color(0xFF1F2937),
            decorationColor = Color(0xFF10B981),
            isDaytime = false
        )
        19 -> LevelTheme( // Brilliant Daylight Nexus - Solar White Energy Grid Day
            floorColor = Color(0xFFFAFAFA),
            isGridVisible = true,
            gridColor = Color(0x2A1E293B),
            wallSideColor = Color(0xFF475569),
            wallTopColor = Color(0xFF64748B),
            decorationColor = Color(0xFFF59E0B),
            isDaytime = true
        )
        20 -> LevelTheme( // Stormy Mainframe Base - Cyber Forest Dark Teal Rain Day
            floorColor = Color(0xFF040D12),
            isGridVisible = true,
            gridColor = Color(0x25185F43),
            wallSideColor = Color(0xFF134E4A),
            wallTopColor = Color(0xFF0F766E),
            decorationColor = Color(0xFF2DD4BF),
            isDaytime = true
        )
        21 -> LevelTheme( // Frostbite Cyber-Hangar - Radiant Cyber Cyan Snow Day
            floorColor = Color(0xFFECFEFF),
            isGridVisible = true,
            gridColor = Color(0x2506B6D4),
            wallSideColor = Color(0xFF164E63),
            wallTopColor = Color(0xFF0891B2),
            decorationColor = Color(0xFF22D3EE),
            isDaytime = true,
            isSnowMode = true
        )
        22 -> LevelTheme( // Pitch Black Corridor - Absolute Shadows Fortress
            floorColor = Color(0xFF010204),
            isGridVisible = true,
            gridColor = Color(0x1CF43F5E),
            wallSideColor = Color(0xFF1F2937),
            wallTopColor = Color(0xFF374151),
            decorationColor = Color(0xFFF43F5E),
            isDaytime = false
        )
        23 -> LevelTheme( // High Noon Plaza - Warm Golden Sunlit Courtyard Day
            floorColor = Color(0xFFFEF3C7),
            isGridVisible = true,
            gridColor = Color(0x2AA16207),
            wallSideColor = Color(0xFF78350F),
            wallTopColor = Color(0xFF92400E),
            decorationColor = Color(0xFFF59E0B),
            isDaytime = true
        )
        24 -> LevelTheme( // Thunder Fortress - Stormy Violet Shadow Rain Day
            floorColor = Color(0xFF120C1F),
            isGridVisible = true,
            gridColor = Color(0x256D28D9),
            wallSideColor = Color(0xFF2E1065),
            wallTopColor = Color(0xFF4C1D95),
            decorationColor = Color(0xFF8B5CF6),
            isDaytime = true
        )
        25 -> LevelTheme( // Emperor Apex Mainframe - Epic Golden Royal Tech Snow Day
            floorColor = Color(0xFF0A0A0E),
            isGridVisible = true,
            gridColor = Color(0x2BF59E0B),
            wallSideColor = Color(0xFF1C1917),
            wallTopColor = Color(0xFF292524),
            decorationColor = Color(0xFFFFD700),
            isDaytime = true,
            isSnowMode = true
        )
        else -> LevelTheme(
            floorColor = Color(0xFF0F172A),
            isGridVisible = true,
            gridColor = Color(0x0C22D3EE),
            wallSideColor = Color(0xFF334155),
            wallTopColor = Color(0xFF475569),
            decorationColor = Color(0xFF00E5FF),
            isDaytime = false
        )
    }
}

data class Footprint(
    val x: Float,
    val y: Float,
    val isPlayer: Boolean,
    val timestamp: Long,
    val angle: Float
)
