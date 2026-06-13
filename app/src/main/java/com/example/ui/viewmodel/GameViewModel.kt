package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ShadowHunterApp
import com.example.data.GuardConfig
import com.example.data.LevelData
import com.example.data.model.HeroDefinition
import com.example.util.GridPoint
import com.example.util.Pathfinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

enum class ScreenState {
    MainMenu,
    HeroSelection,
    LevelSelection,
    Gameplay,
    LevelComplete,
    GameOver
}

enum class HelicopterStatus {
    None,
    Incoming,
    Landed,
    Departing,
    Complete
}

enum class GuardStatus {
    Patrolling,
    Investigating, // Triggered by laser or trap
    AlertShooting, // Actively shooting the player
    Frozen,
    Dead
}

data class GuardState(
    val id: Int,
    val x: Float,
    val y: Float,
    val directionAngle: Float, // in degrees, 0 = right, 90 = down, 180 = left, 270 = up
    val patrolIndex: Int,
    val status: GuardStatus,
    val health: Float = 100f,
    val currentPatrolTarget: GridPoint = GridPoint(0, 0),
    val pathList: List<GridPoint> = emptyList(),
    val currentPathTargetIndex: Int = 0,
    val lastShotTime: Long = 0L,
    val freezeDurationLeftMs: Long = 0L,
    val alertLevel: Float = 0f, // 0.0f to 1.0f. Once 1.0f -> AlertShooting
    val targetInvestigation: GridPoint? = null,
    val config: GuardConfig,
    val pauseTicksLeft: Int = 0,
    val basePatrolAngle: Float = 0f,
    val isElitePolice: Boolean = false,
    val isSecurityRobot: Boolean = false
)

data class GemPickupState(
    val id: Int,
    val x: Float,
    val y: Float,
    val amount: Int,
    var isCollected: Boolean = false,
    val scaleOffset: Float = (0..100).random().toFloat() / 100f // for floating animations
)

data class LaserState(
    val id: Int,
    val cellX: Int,
    val cellY: Int,
    val isVertical: Boolean,
    val length: Int,
    var isActive: Boolean,
    var timerCounter: Int
)

enum class EffectType {
    Slash,
    ShotTracer,
    AlertExclamation,
    FreezeWave,
    BloodSplatter
}

data class VisualEffect(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: EffectType,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 400L,
    val extraAngle: Float = 0f,
    val endX: Float = 0f, // only for shot tracer lines
    val endY: Float = 0f
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShadowHunterApp.repository

    // UI state flows mapped from Room
    val gameProgress = repository.gameProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val heroStates = repository.heroStates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Core screen navigation
    private val _screenState = MutableStateFlow(ScreenState.MainMenu)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    // Active playing level information
    private val _currentLevelData = MutableStateFlow<LevelData?>(null)
    val currentLevelData: StateFlow<LevelData?> = _currentLevelData.asStateFlow()

    // Playback state variables
    private val _playerX = MutableStateFlow(0f)
    val playerX: StateFlow<Float> = _playerX.asStateFlow()

    private val _playerY = MutableStateFlow(0f)
    val playerY: StateFlow<Float> = _playerY.asStateFlow()

    private val _playerRotation = MutableStateFlow(0f)
    val playerRotation: StateFlow<Float> = _playerRotation.asStateFlow()

    private val _playerHP = MutableStateFlow(100f)
    val playerHP: StateFlow<Float> = _playerHP.asStateFlow()

    private val _maxPlayerHP = MutableStateFlow(100f)
    val maxPlayerHP: StateFlow<Float> = _maxPlayerHP.asStateFlow()

    private val _levelGuards = MutableStateFlow<List<GuardState>>(emptyList())
    val levelGuards: StateFlow<List<GuardState>> = _levelGuards.asStateFlow()

    private val _levelGems = MutableStateFlow<List<GemPickupState>>(emptyList())
    val levelGems: StateFlow<List<GemPickupState>> = _levelGems.asStateFlow()

    private val _levelLasers = MutableStateFlow<List<LaserState>>(emptyList())
    val levelLasers: StateFlow<List<LaserState>> = _levelLasers.asStateFlow()

    private val _activeFreezeTrapsState = MutableStateFlow<Set<GridPoint>>(emptySet()) // exploded/unusable traps
    val activeFreezeTrapsState: StateFlow<Set<GridPoint>> = _activeFreezeTrapsState.asStateFlow()

    private val _visualEffects = MutableStateFlow<List<VisualEffect>>(emptyList())
    val visualEffects: StateFlow<List<VisualEffect>> = _visualEffects.asStateFlow()

    private val _playerPath = MutableStateFlow<List<GridPoint>>(emptyList())
    val playerPath: StateFlow<List<GridPoint>> = _playerPath.asStateFlow()

    private val _diamondsCollectedThisLevel = MutableStateFlow(0)
    val diamondsCollectedThisLevel: StateFlow<Int> = _diamondsCollectedThisLevel.asStateFlow()

    private val _gameWinMessage = MutableStateFlow(false)
    val gameWinMessage: StateFlow<Boolean> = _gameWinMessage.asStateFlow()

    private val _helicopterStatus = MutableStateFlow(HelicopterStatus.None)
    val helicopterStatus: StateFlow<HelicopterStatus> = _helicopterStatus.asStateFlow()

    private val _helicopterX = MutableStateFlow(0f)
    val helicopterX: StateFlow<Float> = _helicopterX.asStateFlow()

    private val _helicopterY = MutableStateFlow(0f)
    val helicopterY: StateFlow<Float> = _helicopterY.asStateFlow()

    private val _helicopterScale = MutableStateFlow(1f)
    val helicopterScale: StateFlow<Float> = _helicopterScale.asStateFlow()

    private val _trackedGuardId = MutableStateFlow<Int?>(null)
    val trackedGuardId: StateFlow<Int?> = _trackedGuardId.asStateFlow()

    // NEW GAME SYSTEM STATEFLOWS (RECONNAISSANCE, OFFICES, MELEE, DISGUISES, ELITE POLICE, SHOP UPGRADES)
    private val _unlockedDoors = MutableStateFlow<Set<GridPoint>>(emptySet())
    val unlockedDoors: StateFlow<Set<GridPoint>> = _unlockedDoors.asStateFlow()

    private val _guardsKilled = MutableStateFlow(0)
    val guardsKilled: StateFlow<Int> = _guardsKilled.asStateFlow()

    private val _policeSpawned = MutableStateFlow(false)
    val policeSpawned: StateFlow<Boolean> = _policeSpawned.asStateFlow()

    private val _disguiseProgressPercentage = MutableStateFlow<Int?>(null) // null = not disguised, 0 to 100 = disguised
    val disguiseProgressPercentage: StateFlow<Int?> = _disguiseProgressPercentage.asStateFlow()

    private val _disguiseTimeRemainingMs = MutableStateFlow(0L)
    val disguiseTimeRemainingMs: StateFlow<Long> = _disguiseTimeRemainingMs.asStateFlow()

    private val _disguiseUsageCount = MutableStateFlow(0)
    val disguiseUsageCount: StateFlow<Int> = _disguiseUsageCount.asStateFlow()

    private val _yellowBoxCamoTimeLeft = MutableStateFlow(0L) // Remaining time in yellow cardboard box mode (ms)
    val yellowBoxCamoTimeLeft: StateFlow<Long> = _yellowBoxCamoTimeLeft.asStateFlow()

    private val _yellowBoxPowerUpLocation = MutableStateFlow<GridPoint?>(null)
    val yellowBoxPowerUpLocation: StateFlow<GridPoint?> = _yellowBoxPowerUpLocation.asStateFlow()

    private val _speedUpgradeLevel = MutableStateFlow(0)
    val speedUpgradeLevel: StateFlow<Int> = _speedUpgradeLevel.asStateFlow()

    private val _armorUpgradeLevel = MutableStateFlow(0)
    val armorUpgradeLevel: StateFlow<Int> = _armorUpgradeLevel.asStateFlow()

    private val _stealthUpgradeLevel = MutableStateFlow(0)
    val stealthUpgradeLevel: StateFlow<Int> = _stealthUpgradeLevel.asStateFlow()

    private val _punchUpgradeLevel = MutableStateFlow(0)
    val punchUpgradeLevel: StateFlow<Int> = _punchUpgradeLevel.asStateFlow()

    private val _isTacticalMapOpen = MutableStateFlow(false)
    val isTacticalMapOpen: StateFlow<Boolean> = _isTacticalMapOpen.asStateFlow()

    private val _isGamePaused = MutableStateFlow(false)
    val isGamePaused: StateFlow<Boolean> = _isGamePaused.asStateFlow()

    // CAMERA VIEWPORT CONFIGURATIONS AND CUSTOM WEATHER OVERRIDES
    private val _cameraZoom = MutableStateFlow(1.0f)
    val cameraZoom: StateFlow<Float> = _cameraZoom.asStateFlow()

    private val _cameraAngle = MutableStateFlow(0f)
    val cameraAngle: StateFlow<Float> = _cameraAngle.asStateFlow()

    private val _customForceWeather = MutableStateFlow("Default")
    val customForceWeather: StateFlow<String> = _customForceWeather.asStateFlow()

    fun updateCameraZoom(zoom: Float) {
        _cameraZoom.value = zoom
    }

    fun updateCameraAngle(angle: Float) {
        _cameraAngle.value = angle
    }

    fun updateCustomForceWeather(weather: String) {
        _customForceWeather.value = weather
    }

    private val sharedPrefs = application.getSharedPreferences("ShadowHunterSettings", android.content.Context.MODE_PRIVATE)

    fun loadUpgrades() {
        _speedUpgradeLevel.value = sharedPrefs.getInt("speed", 0)
        _armorUpgradeLevel.value = sharedPrefs.getInt("armor", 0)
        _stealthUpgradeLevel.value = sharedPrefs.getInt("stealth", 0)
        _punchUpgradeLevel.value = sharedPrefs.getInt("punch", 0)
    }

    fun upgradeStat(statName: String, cost: Int): Boolean {
        val currentDiamonds = gameProgress.value?.diamonds ?: 0
        if (currentDiamonds >= cost) {
            viewModelScope.launch {
                repository.addDiamonds(-cost)
                val currentLevel = sharedPrefs.getInt(statName, 0)
                if (currentLevel < 5) {
                    val nextLevel = currentLevel + 1
                    sharedPrefs.edit().putInt(statName, nextLevel).apply()
                    loadUpgrades()
                    
                    if (statName == "armor") {
                        val selectedId = gameProgress.value?.selectedHeroId ?: "std_green"
                        val selectedHero = HeroDefinition.HEROES.find { it.id == selectedId } ?: HeroDefinition.HEROES[0]
                        val maxHP = 100f * (1f + selectedHero.healthBonus) + nextLevel * 15f
                        _maxPlayerHP.value = maxHP
                        _playerHP.value = maxHP
                    }
                }
            }
            return true
        }
        return false
    }

    fun toggleTacticalBlueprintMap(isOpen: Boolean) {
        _isTacticalMapOpen.value = isOpen
        _isGamePaused.value = isOpen
    }

    fun toggleGamePaused(isPaused: Boolean) {
        _isGamePaused.value = isPaused
    }

    private val effectIdGenerator = AtomicInteger(1)
    private val gemIdGenerator = AtomicInteger(1)

    private var gameLoopJob: Job? = null
    private var pathfinder: Pathfinder? = null

    init {
        loadUpgrades()
        // Run database initializer inside a safe background scope
        viewModelScope.launch {
            repository.ensureInitialSetup()
        }
    }

    fun navigateTo(state: ScreenState) {
        // Clean game loop if moving away from gameplay
        if (state != ScreenState.Gameplay) {
            stopGameLoop()
        }
        _screenState.value = state
    }

    fun advanceToNextLevel() {
        val nextLevelNumber = (_currentLevelData.value?.levelNumber ?: 1) + 1
        val nextLevel = LevelData.LEVELS.find { it.levelNumber == nextLevelNumber }
        if (nextLevel != null) {
            startLevel(nextLevel)
        } else {
            navigateTo(ScreenState.MainMenu)
        }
    }

    fun startLevel(level: LevelData) {
        viewModelScope.launch {
            _currentLevelData.value = level
            _diamondsCollectedThisLevel.value = 0
            _gameWinMessage.value = false
            _playerPath.value = emptyList()
            _activeFreezeTrapsState.value = emptySet()
            _visualEffects.value = emptyList()
            _helicopterStatus.value = HelicopterStatus.None
            _helicopterX.value = 0f
            _helicopterY.value = 0f
            _helicopterScale.value = 1f
            _trackedGuardId.value = null

            // Resolve player health modification based on selected hero
            val selectedId = gameProgress.value?.selectedHeroId ?: "std_green"
            val selectedHero = HeroDefinition.HEROES.find { it.id == selectedId } ?: HeroDefinition.HEROES[0]
            val actualArmorLevel = sharedPrefs.getInt("armor", 0)
            val maxHP = 100f * (1f + selectedHero.healthBonus) + actualArmorLevel * 15f
            _maxPlayerHP.value = maxHP
            _playerHP.value = maxHP

            // RESET ALL CUSTOM GAME STATES FOR NEW LEVEL
            _unlockedDoors.value = emptySet()
            _guardsKilled.value = 0
            _policeSpawned.value = false
            _disguiseProgressPercentage.value = null
            _disguiseTimeRemainingMs.value = 0L
            _disguiseUsageCount.value = 0
            _yellowBoxCamoTimeLeft.value = 0L
            _isTacticalMapOpen.value = false
            _isGamePaused.value = false

            // Find an empty start point (e.g., opposite side of Helipad or default 1, height-1)
            var startX = 1
            var startY = level.height - 1
            // Ensure starting area is walkable
            if (level.walls.contains(GridPoint(startX, startY)) || startY >= level.height) {
                for (x in 1 until level.width) {
                    for (y in level.height - 3 until level.height) {
                        if (!level.walls.contains(GridPoint(x, y))) {
                            startX = x
                            startY = y
                            break
                        }
                    }
                }
            }

            _playerX.value = startX.toFloat() + 0.5f
            _playerY.value = startY.toFloat() + 0.5f
            _playerRotation.value = 270f // facing upper direction standard

            // Initialize Pathfinder with support for openable office doors
            pathfinder = Pathfinder(level.width, level.height) { cellX, cellY ->
                val pt = GridPoint(cellX, cellY)
                val isWall = level.walls.contains(pt)
                val isDoor = level.lockedDoors.contains(pt)
                val isOpened = _unlockedDoors.value.contains(pt)
                if (isDoor) {
                    isOpened // Door is walkable only once unlocked!
                } else {
                    !isWall
                }
            }

            // Map and load level guards (including Security Drone and initial enforcers)
            val guardsList = level.guards.map { config ->
                GuardState(
                    id = config.id,
                    x = config.initialX.toFloat() + 0.5f,
                    y = config.initialY.toFloat() + 0.5f,
                    directionAngle = 0f,
                    patrolIndex = 0,
                    status = GuardStatus.Patrolling,
                    currentPatrolTarget = config.patrolRoute.first(),
                    config = config
                )
            }.toMutableList()

            // Spawn Security Patrol bot if level has a robot route
            if (level.robotRoute.isNotEmpty()) {
                val startDrone = level.robotRoute.first()
                guardsList.add(
                    GuardState(
                        id = 9999, // Unique id for security drone
                        x = startDrone.x.toFloat() + 0.5f,
                        y = startDrone.y.toFloat() + 0.5f,
                        directionAngle = 0f,
                        patrolIndex = 0,
                        status = GuardStatus.Patrolling,
                        currentPatrolTarget = if (level.robotRoute.size > 1) level.robotRoute[1] else startDrone,
                        config = GuardConfig(
                            id = 9999,
                            initialX = startDrone.x,
                            initialY = startDrone.y,
                            patrolRoute = level.robotRoute
                        ),
                        isSecurityRobot = true
                    )
                )
            }
            _levelGuards.value = guardsList

            // Setup glowing yellow box powerup location randomly in the level
            var powerUpPoint: GridPoint? = null
            for (attempt in 0..100) {
                val rx = (1 until level.width - 1).random()
                val ry = (1 until level.height - 1).random()
                val gp = GridPoint(rx, ry)
                if (!level.walls.contains(gp) && level.helipad != gp && !level.lockedDoors.contains(gp)) {
                    powerUpPoint = gp
                    break
                }
            }
            _yellowBoxPowerUpLocation.value = powerUpPoint

            // Populate Gems list (and add cash stashes as special green rich diamond gems)
            val levelGemsList = mutableListOf<GemPickupState>()
            level.cashStashes.forEach { officeLoc ->
                levelGemsList.add(
                    GemPickupState(
                        id = gemIdGenerator.incrementAndGet(),
                        x = officeLoc.x.toFloat() + 0.5f,
                        y = officeLoc.y.toFloat() + 0.5f,
                        amount = 150 // Worth substantial cash/diamonds!
                    )
                )
            }
            _levelGems.value = levelGemsList

            // Set up Lasers state
            val lasersList = level.lasers.map { config ->
                LaserState(
                    id = config.id,
                    cellX = config.cellX,
                    cellY = config.cellY,
                    isVertical = config.isVertical,
                    length = config.length,
                    isActive = true,
                    timerCounter = 0
                )
            }
            _levelLasers.value = lasersList

            // Set screen state
            _screenState.value = ScreenState.Gameplay

            // Start processing visual physics game loop
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            val tickRateMs = 16L // ~60 FPS update frequency
            var totalTickCount = 0L

            while (isActive) {
                if (_isGamePaused.value) {
                    delay(50)
                    continue
                }
                val cycleStartTime = System.currentTimeMillis()

                // Tick custom powerup timers
                val currentCamo = _yellowBoxCamoTimeLeft.value
                if (currentCamo > 0L) {
                    _yellowBoxCamoTimeLeft.value = (currentCamo - 16L).coerceAtLeast(0L)
                }

                // Tick disguise progress and active camouflage timer bounds (ለ5 ሰከንድ ብቻ ይሁን)
                val currentDisguisePerc = _disguiseProgressPercentage.value
                if (currentDisguisePerc != null) {
                    if (currentDisguisePerc < 100) {
                        _disguiseProgressPercentage.value = (currentDisguisePerc + 2).coerceAtMost(100) // fast blend
                    } else {
                        // Countdown the active timer once fully camouflaged (5 seconds limit)
                        val rem = _disguiseTimeRemainingMs.value
                        if (rem > 0L) {
                            val nextRem = (rem - 16L).coerceAtLeast(0L)
                            _disguiseTimeRemainingMs.value = nextRem
                            if (nextRem == 0L) {
                                _disguiseProgressPercentage.value = null // Cancelled / Expired after 5 seconds!
                            }
                        }
                    }
                }

                // Check proximity to yellow box powerup
                val boxLoc = _yellowBoxPowerUpLocation.value
                if (boxLoc != null) {
                    val dx = _playerX.value - (boxLoc.x + 0.5f)
                    val dy = _playerY.value - (boxLoc.y + 0.5f)
                    if (sqrt(dx * dx + dy * dy) < 0.7f) {
                        _yellowBoxPowerUpLocation.value = null // Picked up!
                        _yellowBoxCamoTimeLeft.value = 7000L // 7 seconds!
                        addVisualEffect(_playerX.value, _playerY.value, EffectType.FreezeWave, durationMs = 500L)
                    }
                }

                // Execute core gameplay states inside high-frequency loop update
                updatePlayerPhysics()
                updateGuardPhysics(totalTickCount)
                updateLaserPhysics(totalTickCount)
                updateEffectsPhysics()

                // Check terminal results
                if (_playerHP.value <= 0f) {
                    withContext(Dispatchers.Main) {
                        _screenState.value = ScreenState.GameOver
                    }
                    break
                }

                // Level win conditions: all guards eliminated & player reached Helipad
                val liveGuardsCount = _levelGuards.value.count { it.status != GuardStatus.Dead && !it.isSecurityRobot }
                if (liveGuardsCount == 0 && !_gameWinMessage.value) {
                    _gameWinMessage.value = true
                }

                if (liveGuardsCount == 0) {
                    // Check distance to helipad
                    val level = _currentLevelData.value
                    if (level != null && _helicopterStatus.value == HelicopterStatus.None) {
                        val dx = _playerX.value - (level.helipad.x + 0.5f)
                        val dy = _playerY.value - (level.helipad.y + 0.5f)
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 0.8f) {
                            // LEVEL ESCAPED SUCCESSFULLY VIA CINEMATIC HELICOPTER RESCUE!
                            startHelicopterCinematic(level)
                            break
                        }
                    }
                }

                totalTickCount++
                val workTime = System.currentTimeMillis() - cycleStartTime
                val delayTime = max(2L, tickRateMs - workTime)
                delay(delayTime)
            }
        }
    }

    private fun startHelicopterCinematic(level: LevelData) {
        viewModelScope.launch {
            _helicopterStatus.value = HelicopterStatus.Incoming
            _trackedGuardId.value = null
            _playerPath.value = emptyList()

            val destX = level.helipad.x + 0.5f
            val destY = level.helipad.y + 0.5f

            // Start offscreen top right
            val startX = destX + 11f
            val startY = destY - 11f
            _helicopterX.value = startX
            _helicopterY.value = startY
            _helicopterScale.value = 2.5f

            // 1. Fly from offscreen and land on helipad. Smoothly interpolate inside 120 ticks
            val ticks = 120
            for (step in 1..ticks) {
                val ratio = step.toFloat() / ticks.toFloat()
                _helicopterX.value = startX + (destX - startX) * ratio
                _helicopterY.value = startY + (destY - startY) * ratio
                _helicopterScale.value = 2.5f - 1.5f * ratio
                
                // Snap player coordinates exactly to helipad center
                _playerX.value = destX
                _playerY.value = destY
                delay(16)
            }

            _helicopterStatus.value = HelicopterStatus.Landed
            delay(1500) // Stay on floor to pick up player

            // 2. Take off and fly away departed top-left
            _helicopterStatus.value = HelicopterStatus.Departing
            val finalX = destX - 13f
            val finalY = destY - 13f

            for (step in 1..ticks) {
                val ratio = step.toFloat() / ticks.toFloat()
                _helicopterX.value = destX + (finalX - destX) * ratio
                _helicopterY.value = destY + (finalY - destY) * ratio
                _helicopterScale.value = 1.0f + 1.5f * ratio
                
                // Hide player under the helicopter
                _playerX.value = -100f
                _playerY.value = -100f
                delay(16)
            }

            _helicopterStatus.value = HelicopterStatus.Complete
            handleLevelEscapeSuccess(level)
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun updatePlayerPhysics() {
        if (_helicopterStatus.value != HelicopterStatus.None) {
            return
        }

        // Active guard chase path tracking
        val trackedId = _trackedGuardId.value
        if (trackedId != null) {
            val targetGuard = _levelGuards.value.find { it.id == trackedId }
            if (targetGuard == null || targetGuard.status == GuardStatus.Dead) {
                _trackedGuardId.value = null
            } else if (_playerPath.value.isEmpty() || System.currentTimeMillis() % 400 < 30) {
                val startPt = GridPoint(_playerX.value.toInt(), _playerY.value.toInt())
                val endPt = GridPoint(targetGuard.x.toInt(), targetGuard.y.toInt())
                val resolved = pathfinder?.findPath(startPt, endPt) ?: emptyList()
                if (resolved.isNotEmpty()) {
                    _playerPath.value = resolved
                }
            }
        }

        // Selected Character speed multipliers
        val selectedId = gameProgress.value?.selectedHeroId ?: "std_green"
        val selectedHero = HeroDefinition.HEROES.find { it.id == selectedId } ?: HeroDefinition.HEROES[0]
        val heroSpeedBonus = selectedHero.speedBonus
        val bootsModifier = _speedUpgradeLevel.value * 0.07f

        val baseSpeedPerTick = 0.08f // speed cells per tick
        val actualSpeed = baseSpeedPerTick * (1f + heroSpeedBonus + bootsModifier)

        val path = _playerPath.value
        if (path.isNotEmpty()) {
            // Cancel active disguise immediately upon initiating movement!
            if (_disguiseProgressPercentage.value != null) {
                _disguiseProgressPercentage.value = null
                _disguiseTimeRemainingMs.value = 0L
            }
            val nextHop = path.first()
            val targetX = nextHop.x + 0.5f
            val targetY = nextHop.y + 0.5f

            val dx = targetX - _playerX.value
            val dy = targetY - _playerY.value
            val distanceToHop = sqrt(dx * dx + dy * dy)

            if (distanceToHop < actualSpeed) {
                // Snapped into grid center cell
                _playerX.value = targetX
                _playerY.value = targetY
                _playerPath.value = path.drop(1)
                com.example.util.SoundEngine.playFootstep()
            } else {
                // Interpolate heading towards next target cell
                val angle = atan2(dy, dx)
                _playerX.value += cos(angle) * actualSpeed
                _playerY.value += sin(angle) * actualSpeed

                // Rotate player head rotation angle
                val deg = Math.toDegrees(angle.toDouble()).toFloat()
                _playerRotation.value = (deg + 360) % 360
            }
        }

        // Auto-collect loose gems within range of player
        val gems = _levelGems.value
        val updatedGems = gems.map { gem ->
            if (!gem.isCollected) {
                val dx = gem.x - _playerX.value
                val dy = gem.y - _playerY.value
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 0.7f) {
                    gem.copy(isCollected = true).also {
                        // Increase collected diamonds count
                        _diamondsCollectedThisLevel.value += gem.amount
                        com.example.util.SoundEngine.playGem()
                    }
                } else {
                    gem
                }
            } else {
                gem
            }
        }
        if (updatedGems != gems) {
            _levelGems.value = updatedGems
        }

        // Check proximity to guards for auto check tap attack or visual knife strike overlap
        val guards = _levelGuards.value
        var guardToExecute: GuardState? = null
        for (g in guards) {
            if (g.status != GuardStatus.Dead) {
                val dx = g.x - _playerX.value
                val dy = g.y - _playerY.value
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 0.85f) {
                    guardToExecute = g
                    break
                }
            }
        }

        if (guardToExecute != null) {
            val executedGuardModel = executeSlashOnGuard(guardToExecute)
            _guardsKilled.value = _guardsKilled.value + 1
            
            // Spawn Elite Police Sheriff if first time reaching 3 guard kills
            val level = _currentLevelData.value
            if (level != null && _guardsKilled.value >= 3 && !_policeSpawned.value) {
                spawnElitePoliceSheriff(level)
            }

            val deathPoint = GridPoint(guardToExecute.x.toInt(), guardToExecute.y.toInt())
            val alertRadius = 6.0f
            
            val finalGuards = guards.map { g ->
                if (g.id == guardToExecute.id) {
                    executedGuardModel
                } else if (g.status != GuardStatus.Dead && g.status != GuardStatus.Frozen) {
                    val dist = sqrt((g.x - guardToExecute.x)*(g.x - guardToExecute.x) + (g.y - guardToExecute.y)*(g.y - guardToExecute.y))
                    if (dist < alertRadius) {
                        addVisualEffect(g.x, g.y - 0.5f, EffectType.AlertExclamation, durationMs = 800L)
                        g.copy(
                            status = GuardStatus.Investigating,
                            targetInvestigation = deathPoint,
                            pathList = emptyList(),
                            currentPathTargetIndex = 0,
                            alertLevel = min(1.0f, g.alertLevel + 0.35f)
                        )
                    } else {
                        g
                    }
                } else {
                    g
                }
            }
            _levelGuards.value = finalGuards
        }
    }

    private fun executeSlashOnGuard(guard: GuardState): GuardState {
        // Trigger blood/kill visual slice particle explosion
        addVisualEffect(guard.x, guard.y, EffectType.Slash, durationMs = 300L, angle = _playerRotation.value)
        addVisualEffect(guard.x, guard.y, EffectType.BloodSplatter, durationMs = 600L)
        com.example.util.SoundEngine.playSlash()

        val updated = guard.copy(
            status = GuardStatus.Dead,
            health = 0f
        )

        // Drop multiple glowing diamond gems scatter reward
        val dropCount = (3..5).random()
        val currentGemsList = _levelGems.value.toMutableList()
        for (i in 0 until dropCount) {
            val angle = (0..359).random().toFloat() * (PI.toFloat() / 180f)
            val burstRadius = (3..7).random().toFloat() / 10f
            val scatterX = guard.x + cos(angle) * burstRadius
            val scatterY = guard.y + sin(angle) * burstRadius
            currentGemsList.add(
                GemPickupState(
                    id = gemIdGenerator.incrementAndGet(),
                    x = scatterX,
                    y = scatterY,
                    amount = (5..15).random()
                )
            )
        }
        _levelGems.value = currentGemsList

        return updated
    }

    private fun updateGuardPhysics(tickCount: Long) {
        val level = _currentLevelData.value ?: return
        val currentGuards = _levelGuards.value
        val px = _playerX.value
        val py = _playerY.value

        // Guards become significantly more intelligent and faster in higher levels
        val levelFactor = level.levelNumber - 1
        val speedMultiplier = 1.0f + (levelFactor * 0.15f)
        val alertIncrMultiplier = 1.0f + (levelFactor * 0.25f)
        val fireRateInterval = (400L - (levelFactor * 60L)).coerceAtLeast(180L)

        // Stealth mesh upgrades logic
        val stealthUpgrade = _stealthUpgradeLevel.value
        val alertProgressionModifier = (1.0f - stealthUpgrade * 0.12f).coerceAtLeast(0.40f)

        val updatedGuards = currentGuards.map { guard ->
            if (guard.status == GuardStatus.Dead) return@map guard

            // Reduce freeze duration if frozen
            if (guard.status == GuardStatus.Frozen) {
                val remainingMs = guard.freezeDurationLeftMs - 16L
                if (remainingMs <= 0) {
                    guard.copy(
                        status = GuardStatus.Patrolling,
                        freezeDurationLeftMs = 0L
                    )
                } else {
                    guard.copy(freezeDurationLeftMs = remainingMs)
                }
            } else {
                // Line of Sight (LOS) calculation to check if guard spots player
                val canSeePlayer = calculateLineOfSight(guard, px, py, level)

                var newStatus = guard.status
                var alertLevel = guard.alertLevel
                var path = guard.pathList
                var pathIndex = guard.currentPathTargetIndex
                var targetInvestigation = guard.targetInvestigation
                var lastShot = guard.lastShotTime
                var directionAngle = guard.directionAngle
                var patrolIndex = guard.patrolIndex
                var currentPatrolTarget = guard.currentPatrolTarget
                var pauseTicksLeft = guard.pauseTicksLeft
                var basePatrolAngle = guard.basePatrolAngle

                if (canSeePlayer) {
                    if (guard.isSecurityRobot) {
                        // Drone Bot doesn't shoot or have normal alert progression. It triggers instantly!
                        val now = System.currentTimeMillis()
                        if (now - lastShot > 4000L) {
                            lastShot = now
                            // Spawn 2 rapid enforcers who storm the player location!
                            spawnElitePoliceEnforcers(level)
                        }
                    } else {
                        // Standard human guard alert progression with Stealth Suit modifier
                        val oldAlert = alertLevel
                        alertLevel = min(1.0f, alertLevel + (0.12f * alertIncrMultiplier * alertProgressionModifier))
                        if (alertLevel >= 0.9f) {
                            if (oldAlert < 0.9f) {
                                com.example.util.SoundEngine.playAlarm()
                            }
                            newStatus = GuardStatus.AlertShooting
                            // Turn to face player
                            val dx = px - guard.x
                            val dy = py - guard.y
                            directionAngle = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360) % 360).toFloat()

                            // Rate of fire logic
                            val now = System.currentTimeMillis()
                            if (now - lastShot > fireRateInterval) {
                                lastShot = now
                                shootPlayer()
                                addVisualEffect(
                                    guard.x,
                                    guard.y,
                                    EffectType.ShotTracer,
                                    durationMs = 150L,
                                    endX = px,
                                    endY = py
                                )
                            }
                        }
                    }
                } else {
                    // Decay alert level when player leaves LOD field
                    alertLevel = max(0.0f, alertLevel - 0.03f)
                    if (alertLevel <= 0.1f && newStatus == GuardStatus.AlertShooting) {
                        newStatus = GuardStatus.Investigating
                        targetInvestigation = GridPoint(px.toInt(), py.toInt())
                        path = emptyList() // reload route
                    }
                }

                // AI Movement Speed factors
                var currentX = guard.x
                var currentY = guard.y
                val rawSpeed = when (newStatus) {
                    GuardStatus.AlertShooting -> 0.055f
                    GuardStatus.Investigating -> 0.038f
                    else -> 0.024f
                }
                
                // Security vacuum robots crawl slightly slower; elite police are exceptionally swift
                val baseSpeed = if (guard.isSecurityRobot) {
                    0.020f
                } else if (guard.isElitePolice) {
                    0.065f // rapid elite pursue speed!
                } else {
                    rawSpeed
                }
                val guardSpeed = baseSpeed * speedMultiplier

                if (newStatus == GuardStatus.AlertShooting) {
                    guard.copy(
                        alertLevel = alertLevel,
                        status = newStatus,
                        lastShotTime = lastShot,
                        directionAngle = directionAngle
                    )
                } else if (newStatus == GuardStatus.Investigating && targetInvestigation != null) {
                    if (path.isEmpty()) {
                        val startPt = GridPoint(currentX.toInt(), currentY.toInt())
                        path = pathfinder?.findPath(startPt, targetInvestigation) ?: emptyList()
                        pathIndex = 0
                    }

                    if (path.isNotEmpty() && pathIndex < path.size) {
                        val targetNode = path[pathIndex]
                        val tgtX = targetNode.x + 0.5f
                        val tgtY = targetNode.y + 0.5f

                        val dx = tgtX - currentX
                        val dy = tgtY - currentY
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist < guardSpeed) {
                            currentX = tgtX
                            currentY = tgtY
                            pathIndex++
                            if (pathIndex >= path.size) {
                                newStatus = GuardStatus.Patrolling
                                targetInvestigation = null
                                path = emptyList()
                            }
                        } else {
                            val angle = atan2(dy, dx)
                            currentX += cos(angle) * guardSpeed
                            currentY += sin(angle) * guardSpeed
                            directionAngle = ((Math.toDegrees(angle.toDouble()) + 360) % 360).toFloat()
                        }
                    } else {
                        newStatus = GuardStatus.Patrolling
                        targetInvestigation = null
                        path = emptyList()
                    }

                    val resolvedPos = resolveWallCollisions(currentX, currentY, 0.28f, level)
                    currentX = resolvedPos.first
                    currentY = resolvedPos.second

                    guard.copy(
                        x = currentX,
                        y = currentY,
                        status = newStatus,
                        alertLevel = alertLevel,
                        pathList = path,
                        currentPathTargetIndex = pathIndex,
                        targetInvestigation = targetInvestigation,
                        directionAngle = directionAngle,
                        lastShotTime = lastShot
                    )
                } else {
                    // Regular Patrolling Loop
                    if (pauseTicksLeft > 0) {
                        pauseTicksLeft--
                        val scanOffset = sin(tickCount * 0.06f) * 50f
                        directionAngle = ((basePatrolAngle + scanOffset + 360f) % 360f)
                    } else {
                        val targetNode = currentPatrolTarget
                        val tgtX = targetNode.x + 0.5f
                        val tgtY = targetNode.y + 0.5f

                        val dx = tgtX - currentX
                        val dy = tgtY - currentY
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist < guardSpeed) {
                            currentX = tgtX
                            currentY = tgtY
                            val route = guard.config.patrolRoute
                            if (route.isNotEmpty()) {
                                val currentIndexInRoute = route.indexOf(targetNode)
                                val nextIndexInRoute = (currentIndexInRoute + 1) % route.size
                                currentPatrolTarget = route[nextIndexInRoute]
                            }

                            // Pausing looking around scans
                            pauseTicksLeft = (60..110).random()
                            basePatrolAngle = directionAngle
                        } else {
                            val angle = atan2(dy, dx)
                            currentX += cos(angle) * guardSpeed
                            currentY += sin(angle) * guardSpeed
                            directionAngle = ((Math.toDegrees(angle.toDouble()) + 360) % 360).toFloat()
                        }
                    }

                    val resolvedPos = resolveWallCollisions(currentX, currentY, 0.28f, level)
                    currentX = resolvedPos.first
                    currentY = resolvedPos.second

                    guard.copy(
                        x = currentX,
                        y = currentY,
                        status = GuardStatus.Patrolling,
                        alertLevel = alertLevel,
                        pauseTicksLeft = pauseTicksLeft,
                        directionAngle = directionAngle,
                        basePatrolAngle = basePatrolAngle,
                        currentPatrolTarget = currentPatrolTarget,
                        lastShotTime = lastShot
                    )
                }
            }
        }

        _levelGuards.value = updatedGuards
    }

    fun resolveWallCollisions(x: Float, y: Float, radius: Float, level: LevelData): Pair<Float, Float> {
        var resolvedX = x
        var resolvedY = y
        val r = radius
        val minCellX = (resolvedX - r).toInt().coerceAtLeast(0)
        val maxCellX = (resolvedX + r).toInt().coerceAtMost(level.width - 1)
        val minCellY = (resolvedY - r).toInt().coerceAtLeast(0)
        val maxCellY = (resolvedY + r).toInt().coerceAtMost(level.height - 1)

        for (cx in minCellX..maxCellX) {
            for (cy in minCellY..maxCellY) {
                val pt = GridPoint(cx, cy)
                val isSolid = level.walls.contains(pt) || (level.lockedDoors.contains(pt) && !_unlockedDoors.value.contains(pt))
                if (isSolid) {
                    val closestX = resolvedX.coerceIn(cx.toFloat(), cx.toFloat() + 1f)
                    val closestY = resolvedY.coerceIn(cy.toFloat(), cy.toFloat() + 1f)
                    val dx = resolvedX - closestX
                    val dy = resolvedY - closestY
                    val distance = sqrt(dx * dx + dy * dy)
                    if (distance in 0.001f..r) {
                        val overlap = r - distance
                        resolvedX += (dx / distance) * overlap
                        resolvedY += (dy / distance) * overlap
                    } else if (distance == 0f) {
                        val distToLeft = resolvedX - cx
                        val distToRight = (cx + 1) - resolvedX
                        val distToTop = resolvedY - cy
                        val distToBottom = (cy + 1) - resolvedY
                        val minDist = minOf(distToLeft, distToRight, distToTop, distToBottom)
                        when (minDist) {
                            distToLeft -> resolvedX -= (r + distToLeft)
                            distToRight -> resolvedX += (r + distToRight)
                            distToTop -> resolvedY -= (r + distToTop)
                            distToBottom -> resolvedY += (r + distToBottom)
                        }
                    }
                }
            }
        }
        return Pair(resolvedX, resolvedY)
    }

    private fun calculateLineOfSight(guard: GuardState, px: Float, py: Float, level: LevelData): Boolean {
        // Invisibility check: disguised or wearing yellow box camouflage
        if (_disguiseProgressPercentage.value == 100 || _yellowBoxCamoTimeLeft.value > 0L) {
            return false
        }

        val dx = px - guard.x
        val dy = py - guard.y
        val dist = sqrt(dx * dx + dy * dy)

        val maxVisionRange = 4.2f
        if (dist > maxVisionRange) return false

        // Angle checks
        val angleToPlayer = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360) % 360).toFloat()
        val diffAngle = abs(angleToPlayer - guard.directionAngle)
        val viewConeAngle = 75f // guard field of view index angle bounds

        val inViewArc = diffAngle <= viewConeAngle / 2f || diffAngle >= (360f - viewConeAngle / 2f)
        if (!inViewArc) return false

        // Straight ray collision block checks on the grid (including locked opaque doors)
        val stepCount = (dist * 3f).toInt().coerceAtLeast(3)
        for (i in 1 until stepCount) {
            val ratio = i.toFloat() / stepCount.toFloat()
            val rayX = guard.x + dx * ratio
            val rayY = guard.y + dy * ratio
            val checkPoint = GridPoint(rayX.toInt(), rayY.toInt())
            
            val isWall = level.walls.contains(checkPoint)
            val isDoor = level.lockedDoors.contains(checkPoint)
            val isUnlocked = _unlockedDoors.value.contains(checkPoint)
            
            if (isWall || (isDoor && !isUnlocked)) {
                return false // Obstacle blockades line of sight! Safe.
            }
        }
        return true
    }

    private fun shootPlayer() {
        // Safe thread updates
        val currentHP = _playerHP.value
        val damage = 7f // Guard weapon damage
        _playerHP.value = max(0f, currentHP - damage)
        com.example.util.SoundEngine.playShoot()
    }

    private fun updateLaserPhysics(tickCount: Long) {
        val level = _currentLevelData.value ?: return
        val currentLasers = _levelLasers.value

        val toggleInterval = 120 // 120 ticks ~= 2 seconds cycle
        val updatedLasers = currentLasers.map { laser ->
            val nextCounter = (laser.timerCounter + 1) % toggleInterval
            val shouldBeActive = nextCounter < 60 // On for half period, Off for other half

            laser.copy(isActive = shouldBeActive, timerCounter = nextCounter).also {
                // If player touches laser while active -> trigger alarms immediately
                if (shouldBeActive) {
                    val px = _playerX.value.toInt()
                    val py = _playerY.value.toInt()
                    var isInsideLaser = false

                    if (laser.isVertical) {
                        if (px == laser.cellX && py in laser.cellY until (laser.cellY + laser.length)) {
                            isInsideLaser = true
                        }
                    } else {
                        if (py == laser.cellY && px in laser.cellX until (laser.cellX + laser.length)) {
                            isInsideLaser = true
                        }
                    }

                    if (isInsideLaser) {
                        // Trigger immediate alarms and guard pursuit of player position
                        triggerLaserAlarm(GridPoint(px, py))
                    }
                }
            }
        }
        _levelLasers.value = updatedLasers
    }

    private fun triggerLaserAlarm(alertPoint: GridPoint) {
        com.example.util.SoundEngine.playAlarm()
        val guards = _levelGuards.value
        val updated = guards.map { guard ->
            if (guard.status != GuardStatus.Dead && guard.status != GuardStatus.Frozen) {
                // Show floating warning exclamation mark
                addVisualEffect(guard.x, guard.y - 0.4f, EffectType.AlertExclamation, durationMs = 800L)
                guard.copy(
                    status = GuardStatus.Investigating,
                    targetInvestigation = alertPoint,
                    pathList = emptyList() // force pathfinding reload
                )
            } else {
                guard
            }
        }
        _levelGuards.value = updated
    }

    private fun updateEffectsPhysics() {
        val now = System.currentTimeMillis()
        val current = _visualEffects.value
        val filtered = current.filter { effect ->
            now - effect.timestamp < effect.durationMs
        }
        if (filtered.size != current.size) {
            _visualEffects.value = filtered
        }
    }

    private fun addVisualEffect(
        x: Float,
        y: Float,
        type: EffectType,
        durationMs: Long = 400L,
        angle: Float = 0f,
        endX: Float = 0f,
        endY: Float = 0f
    ) {
        val effect = VisualEffect(
            id = effectIdGenerator.incrementAndGet(),
            x = x,
            y = y,
            type = type,
            durationMs = durationMs,
            extraAngle = angle,
            endX = endX,
            endY = endY
        )
        _visualEffects.value = _visualEffects.value + effect
    }

    // Interactive taps on the level field (movement paths or action hooks)
    fun handleLevelTap(worldX: Float, worldY: Float) {
        val cellX = worldX.toInt()
        val cellY = worldY.toInt()
        val level = _currentLevelData.value ?: return

        // Check if player clicked directly on a guard to track and eliminate them!
        val clickedGuard = _levelGuards.value.find { g ->
            g.status != GuardStatus.Dead && abs(g.x - worldX) < 1.2f && abs(g.y - worldY) < 1.2f
        }
        if (clickedGuard != null) {
            _trackedGuardId.value = clickedGuard.id
            addVisualEffect(clickedGuard.x, clickedGuard.y, EffectType.AlertExclamation, durationMs = 400L)
            
            val startPt = GridPoint(_playerX.value.toInt(), _playerY.value.toInt())
            val endPt = GridPoint(clickedGuard.x.toInt(), clickedGuard.y.toInt())
            val resolved = pathfinder?.findPath(startPt, endPt) ?: emptyList()
            if (resolved.isNotEmpty()) {
                _playerPath.value = resolved
            }
            return
        } else {
            _trackedGuardId.value = null
        }

        // Check if player clicked directly on an unexploded freeze trap to remote trigger it!
        val clickedTrap = level.freezeTraps.find { it.x == cellX && it.y == cellY }
        if (clickedTrap != null && !_activeFreezeTrapsState.value.contains(clickedTrap)) {
            triggerFreezeTrap(clickedTrap)
            return
        }

        // Check pathfind route mapping to node click
        val startPt = GridPoint(_playerX.value.toInt(), _playerY.value.toInt())
        val endPt = GridPoint(cellX, cellY)

        val resolvedPath = pathfinder?.findPath(startPt, endPt) ?: emptyList()
        if (resolvedPath.isNotEmpty()) {
            _playerPath.value = resolvedPath
        } else {
            // Play wall collision / bad path tap feedback thud!
            val isWall = level.walls.contains(endPt) || (level.lockedDoors.contains(endPt) && !_unlockedDoors.value.contains(endPt))
            if (isWall) {
                com.example.util.SoundEngine.playCollision()
            }
        }
    }

    private fun triggerFreezeTrap(trapPt: GridPoint) {
        // Mark trap exploded
        _activeFreezeTrapsState.value = _activeFreezeTrapsState.value + trapPt

        // Spawn visual freeze trap wave ring expansion
        val centerGridX = trapPt.x + 0.5f
        val centerGridY = trapPt.y + 0.5f
        addVisualEffect(centerGridX, centerGridY, EffectType.FreezeWave, durationMs = 500L)

        // Freeze all active guards in a 3.5 cell radius!
        val guards = _levelGuards.value
        val updated = guards.map { guard ->
            if (guard.status != GuardStatus.Dead) {
                val dx = guard.x - centerGridX
                val dy = guard.y - centerGridY
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 3.5f) {
                    addVisualEffect(guard.x, guard.y, EffectType.FreezeWave, durationMs = 400L)
                    guard.copy(
                        status = GuardStatus.Frozen,
                        freezeDurationLeftMs = 6000L // frozen solid for 6 seconds!
                    )
                } else {
                    guard
                }
            } else {
                guard
            }
        }
        _levelGuards.value = updated
    }

    private fun handleLevelEscapeSuccess(level: LevelData) {
        stopGameLoop()
        viewModelScope.launch {
            // Save earned rewards list inside DB
            val rewardMoney = _diamondsCollectedThisLevel.value
            repository.addDiamonds(rewardMoney)
            // Advance level progress
            repository.advanceLevel(level.levelNumber)

            withContext(Dispatchers.Main) {
                _screenState.value = ScreenState.LevelComplete
            }
        }
    }

    fun unlockOfficeDoor(doorPt: GridPoint) {
        if (_unlockedDoors.value.contains(doorPt)) return
        _unlockedDoors.value = _unlockedDoors.value + doorPt
        addVisualEffect(doorPt.x + 0.5f, doorPt.y + 0.5f, EffectType.FreezeWave, durationMs = 300L)
        // Refresh pathfinder with the unlocked door
        val level = _currentLevelData.value ?: return
        pathfinder = Pathfinder(level.width, level.height) { cellX, cellY ->
            val pt = GridPoint(cellX, cellY)
            val isWall = level.walls.contains(pt)
            val isDoor = level.lockedDoors.contains(pt)
            val isOpened = _unlockedDoors.value.contains(pt)
            if (isDoor) isOpened else !isWall
        }
    }

    fun startProximityDisguise() {
        if (_disguiseProgressPercentage.value != null) return
        if (_disguiseUsageCount.value >= 2) {
            // Cannot use more than twice per game level (በአንድ ጌም ከሁለት ጊዜ በላይ አይቻልም)
            addVisualEffect(_playerX.value, _playerY.value, EffectType.ShotTracer, durationMs = 300L)
            return
        }

        // Anchors the player and starts the blending sequence
        _disguiseProgressPercentage.value = 0
        _disguiseTimeRemainingMs.value = 5000L // 5 seconds active camouflage duration limit
        _disguiseUsageCount.value += 1
        _playerPath.value = emptyList() // Abort movement
        addVisualEffect(_playerX.value, _playerY.value, EffectType.FreezeWave, durationMs = 600L)
    }

    fun spawnElitePoliceEnforcers(level: LevelData) {
        val currentGuards = _levelGuards.value.toMutableList()
        val pX = _playerX.value.toInt()
        val pY = _playerY.value.toInt()
        
        // Spawn 2 rapid enforcers who investigate player location
        val startLoc1 = level.helipad
        currentGuards.add(
            GuardState(
                id = 20000 + (1000..9000).random(),
                x = startLoc1.x.toFloat() + 0.5f,
                y = startLoc1.y.toFloat() + 0.5f,
                directionAngle = 180f,
                patrolIndex = 0,
                status = GuardStatus.Investigating,
                targetInvestigation = GridPoint(pX, pY),
                config = GuardConfig(
                    id = 101,
                    initialX = startLoc1.x,
                    initialY = startLoc1.y,
                    patrolRoute = listOf(startLoc1)
                ),
                isElitePolice = true
            )
        )
        currentGuards.add(
            GuardState(
                id = 30000 + (1000..9000).random(),
                x = startLoc1.x.toFloat() + 0.5f,
                y = startLoc1.y.toFloat() + 0.5f,
                directionAngle = 180f,
                patrolIndex = 0,
                status = GuardStatus.Investigating,
                targetInvestigation = GridPoint(pX, pY),
                config = GuardConfig(
                    id = 102,
                    initialX = startLoc1.x,
                    initialY = startLoc1.y,
                    patrolRoute = listOf(startLoc1)
                ),
                isElitePolice = true
            )
        )
        _levelGuards.value = currentGuards
        addVisualEffect(startLoc1.x.toFloat() + 0.5f, startLoc1.y.toFloat() + 0.5f, EffectType.AlertExclamation, durationMs = 1200L)
    }

    fun spawnElitePoliceSheriff(level: LevelData) {
        val currentGuards = _levelGuards.value.toMutableList()
        val pX = _playerX.value.toInt()
        val pY = _playerY.value.toInt()
        
        // Spawn 1 super rapid boss-level Sheriff who pursues players
        val startLoc = level.helipad
        currentGuards.add(
            GuardState(
                id = 50000,
                x = startLoc.x.toFloat() + 0.5f,
                y = startLoc.y.toFloat() + 0.5f,
                directionAngle = 180f,
                patrolIndex = 0,
                status = GuardStatus.Investigating,
                targetInvestigation = GridPoint(pX, pY),
                config = GuardConfig(
                    id = 103,
                    initialX = startLoc.x,
                    initialY = startLoc.y,
                    patrolRoute = listOf(startLoc)
                ),
                isElitePolice = true
            )
        )
        _levelGuards.value = currentGuards
        _policeSpawned.value = true
        addVisualEffect(startLoc.x.toFloat() + 0.5f, startLoc.y.toFloat() + 0.5f, EffectType.AlertExclamation, durationMs = 1500L)
    }

    fun performMeleePunch() {
        val px = _playerX.value
        val py = _playerY.value
        val punchStunDuration = 4000L + _punchUpgradeLevel.value * 1000L

        // Trigger visual swipe punch effect
        addVisualEffect(px, py - 0.2f, EffectType.Slash, durationMs = 250L, angle = _playerRotation.value)

        val guards = _levelGuards.value
        val updated = guards.map { guard ->
            if (guard.status != GuardStatus.Dead) {
                val dx = guard.x - px
                val dy = guard.y - py
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 1.2f) {
                    addVisualEffect(guard.x, guard.y, EffectType.FreezeWave, durationMs = 450L)
                    guard.copy(
                        status = GuardStatus.Frozen,
                        freezeDurationLeftMs = punchStunDuration
                    )
                } else {
                    guard
                }
            } else {
                guard
            }
        }
        _levelGuards.value = updated
    }

    // Hero Custom Store utilities
    fun selectHero(hero: HeroDefinition) {
        viewModelScope.launch {
            repository.selectHero(hero.id)
        }
    }

    fun unlockHero(hero: HeroDefinition) {
        viewModelScope.launch {
            repository.unlockHero(hero.id, hero.cost)
        }
    }
}
