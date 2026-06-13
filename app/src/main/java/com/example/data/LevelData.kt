package com.example.data

import com.example.util.GridPoint

data class GuardConfig(
    val id: Int,
    val initialX: Int,
    val initialY: Int,
    val patrolRoute: List<GridPoint>,
    val lookIntervalMs: Long = 3000L
)

data class LaserConfig(
    val id: Int,
    val cellX: Int,
    val cellY: Int,
    val isVertical: Boolean,
    val length: Int,
    val togglePeriod: Int = 4 // cycles every 4 ticks (e.g. 2s on, 2s off)
)

data class LevelData(
    val levelNumber: Int,
    val name: String,
    val width: Int = 12,
    val height: Int = 18,
    val walls: Set<GridPoint>, // 1 is container/crate, 2 is metal barrier, etc.
    val wallTypes: Map<GridPoint, Int>, // maps coordinate to type: 1 = red box, 2 = green container, 3 = grey barrier
    val guards: List<GuardConfig>,
    val freezeTraps: List<GridPoint> = emptyList(),
    val lasers: List<LaserConfig> = emptyList(),
    val helipad: GridPoint, // Exit portal
    val lockedDoors: Set<GridPoint> = emptySet(),
    val cashStashes: Set<GridPoint> = emptySet(),
    val robotRoute: List<GridPoint> = emptyList()
) {
    companion object {
        val LEVELS = listOf(
            // LEVEL 1: Cargo Crossing (Simple introductions, 2 guards, straightforward pathing)
            LevelData(
                levelNumber = 1,
                name = "Cargo Crossing",
                helipad = GridPoint(6, 1),
                walls = setOf(
                    // outer boundaries - handled by game grid boundaries, let's place crates in center
                    GridPoint(1, 4), GridPoint(2, 4), GridPoint(3, 4),
                    GridPoint(8, 4), GridPoint(9, 4), GridPoint(10, 4),
                    GridPoint(4, 9), GridPoint(5, 9), GridPoint(6, 9), GridPoint(7, 9),
                    GridPoint(2, 13), GridPoint(3, 13), GridPoint(8, 13), GridPoint(9, 13)
                ),
                wallTypes = mapOf(
                    GridPoint(1, 4) to 1, GridPoint(2, 4) to 1, GridPoint(3, 4) to 1,
                    GridPoint(8, 4) to 2, GridPoint(9, 4) to 2, GridPoint(10, 4) to 2,
                    GridPoint(4, 9) to 1, GridPoint(5, 9) to 1, GridPoint(6, 9) to 1, GridPoint(7, 9) to 2,
                    GridPoint(2, 13) to 2, GridPoint(3, 13) to 2, GridPoint(8, 13) to 1, GridPoint(9, 13) to 1
                ),
                guards = listOf(
                    GuardConfig(
                        id = 1,
                        initialX = 1,
                        initialY = 7,
                        patrolRoute = listOf(GridPoint(1, 7), GridPoint(10, 7), GridPoint(1, 7))
                    ),
                    GuardConfig(
                        id = 2,
                        initialX = 10,
                        initialY = 11,
                        patrolRoute = listOf(GridPoint(10, 11), GridPoint(1, 11), GridPoint(10, 11))
                    )
                ),
                freezeTraps = listOf(GridPoint(5, 15))
            ),

            // LEVEL 2: The Laser Grid (Lasers that activate every 2 seconds, 3 guards)
            LevelData(
                levelNumber = 2,
                name = "The Laser Grid",
                helipad = GridPoint(5, 0),
                walls = setOf(
                    GridPoint(0, 3), GridPoint(1, 3), GridPoint(2, 3), GridPoint(3, 3), GridPoint(4, 3),
                    GridPoint(7, 3), GridPoint(8, 3), GridPoint(9, 3), GridPoint(10, 3), GridPoint(11, 3),
                    GridPoint(3, 7), GridPoint(4, 7), GridPoint(7, 7), GridPoint(8, 7),
                    GridPoint(0, 11), GridPoint(1, 11), GridPoint(5, 11), GridPoint(6, 11), GridPoint(10, 11), GridPoint(11, 11),
                    GridPoint(3, 14), GridPoint(8, 14)
                ),
                wallTypes = mapOf(
                    GridPoint(0, 3) to 2, GridPoint(1, 3) to 2, GridPoint(2, 3) to 2, GridPoint(3, 3) to 2, GridPoint(4, 3) to 1,
                    GridPoint(7, 3) to 1, GridPoint(8, 3) to 2, GridPoint(9, 3) to 2, GridPoint(10, 3) to 2, GridPoint(11, 3) to 2,
                    GridPoint(3, 7) to 3, GridPoint(4, 7) to 3, GridPoint(7, 7) to 3, GridPoint(8, 7) to 3,
                    GridPoint(0, 11) to 1, GridPoint(1, 11) to 1, GridPoint(5, 11) to 2, GridPoint(6, 11) to 2, GridPoint(10, 11) to 1, GridPoint(11, 11) to 1,
                    GridPoint(3, 14) to 3, GridPoint(8, 14) to 3
                ),
                guards = listOf(
                    GuardConfig(
                        id = 1,
                        initialX = 5,
                        initialY = 5,
                        patrolRoute = listOf(GridPoint(5, 5), GridPoint(5, 1), GridPoint(5, 5))
                    ),
                    GuardConfig(
                        id = 2,
                        initialX = 1,
                        initialY = 9,
                        patrolRoute = listOf(GridPoint(1, 9), GridPoint(10, 9), GridPoint(1, 9))
                    ),
                    GuardConfig(
                        id = 3,
                        initialX = 5,
                        initialY = 13,
                        patrolRoute = listOf(GridPoint(5, 13), GridPoint(1, 13), GridPoint(10, 13), GridPoint(5, 13))
                    )
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 5, cellY = 3, isVertical = false, length = 2, togglePeriod = 4), // laser in the central gate
                    LaserConfig(id = 2, cellX = 2, cellY = 11, isVertical = false, length = 3, togglePeriod = 4)
                ),
                freezeTraps = listOf(GridPoint(10, 15))
            ),

            // LEVEL 3: Freeze Chamber (Lots of freeze traps! 4 guards to hunt)
            LevelData(
                levelNumber = 3,
                name = "Freeze Chamber",
                helipad = GridPoint(2, 1),
                walls = setOf(
                    GridPoint(2, 3), GridPoint(3, 3), GridPoint(4, 3), GridPoint(5, 3), GridPoint(6, 3), GridPoint(7, 3), GridPoint(8, 3), GridPoint(9, 3),
                    GridPoint(2, 7), GridPoint(3, 7), GridPoint(8, 7), GridPoint(9, 7),
                    GridPoint(0, 11), GridPoint(1, 11), GridPoint(2, 11), GridPoint(3, 11), GridPoint(8, 11), GridPoint(9, 11), GridPoint(10, 11), GridPoint(11, 11),
                    GridPoint(4, 14), GridPoint(5, 14), GridPoint(6, 14), GridPoint(7, 14)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 3) to 1, GridPoint(3, 3) to 1, GridPoint(4, 3) to 3, GridPoint(5, 3) to 3, GridPoint(6, 3) to 3, GridPoint(7, 3) to 1, GridPoint(8, 3) to 1, GridPoint(9, 3) to 1,
                    GridPoint(2, 7) to 2, GridPoint(3, 7) to 2, GridPoint(8, 7) to 2, GridPoint(9, 7) to 2,
                    GridPoint(0, 11) to 1, GridPoint(1, 11) to 1, GridPoint(2, 11) to 1, GridPoint(3, 11) to 1, GridPoint(8, 11) to 1, GridPoint(9, 11) to 1, GridPoint(10, 11) to 1, GridPoint(11, 11) to 1,
                    GridPoint(4, 14) to 2, GridPoint(5, 14) to 2, GridPoint(6, 14) to 2, GridPoint(7, 14) to 2
                ),
                guards = listOf(
                    GuardConfig(
                        id = 1,
                        initialX = 1,
                        initialY = 5,
                        patrolRoute = listOf(GridPoint(1, 5), GridPoint(10, 5), GridPoint(1, 5))
                    ),
                    GuardConfig(
                        id = 2,
                        initialX = 10,
                        initialY = 9,
                        patrolRoute = listOf(GridPoint(10, 9), GridPoint(1, 9), GridPoint(10, 9))
                    ),
                    GuardConfig(
                        id = 3,
                        initialX = 5,
                        initialY = 12,
                        patrolRoute = listOf(GridPoint(5, 12), GridPoint(5, 16), GridPoint(5, 12))
                    ),
                    GuardConfig(
                        id = 4,
                        initialX = 1,
                        initialY = 15,
                        patrolRoute = listOf(GridPoint(1, 15), GridPoint(10, 15), GridPoint(1, 15))
                    )
                ),
                freezeTraps = listOf(
                    GridPoint(5, 8), // Central freeze trap
                    GridPoint(1, 12),
                    GridPoint(10, 12)
                )
            ),

            // LEVEL 4: Helipad Warehouse (Heavy security Warehouse maze around center helipad)
            LevelData(
                levelNumber = 4,
                name = "Helipad Warehouse",
                helipad = GridPoint(5, 8), // Exit pad is in center of warehouse!
                walls = setOf(
                    GridPoint(4, 4), GridPoint(5, 4), GridPoint(6, 4), GridPoint(7, 4),
                    GridPoint(4, 5), GridPoint(7, 5),
                    GridPoint(4, 6), GridPoint(7, 6),
                    GridPoint(4, 10), GridPoint(7, 10),
                    GridPoint(4, 11), GridPoint(5, 11), GridPoint(6, 11), GridPoint(7, 11),
                    GridPoint(1, 2), GridPoint(2, 2), GridPoint(9, 2), GridPoint(10, 2),
                    GridPoint(1, 15), GridPoint(2, 15), GridPoint(9, 15), GridPoint(10, 15)
                ),
                wallTypes = mapOf(
                    GridPoint(4, 4) to 3, GridPoint(5, 4) to 3, GridPoint(6, 4) to 3, GridPoint(7, 4) to 3,
                    GridPoint(4, 5) to 3, GridPoint(7, 5) to 3,
                    GridPoint(4, 6) to 3, GridPoint(7, 6) to 3,
                    GridPoint(4, 10) to 3, GridPoint(7, 10) to 3,
                    GridPoint(4, 11) to 3, GridPoint(5, 11) to 3, GridPoint(6, 11) to 3, GridPoint(7, 11) to 3,
                    GridPoint(1, 2) to 1, GridPoint(2, 2) to 1, GridPoint(9, 2) to 2, GridPoint(10, 2) to 2,
                    GridPoint(1, 15) to 2, GridPoint(2, 15) to 2, GridPoint(9, 15) to 1, GridPoint(10, 15) to 1
                ),
                guards = listOf(
                    GuardConfig(
                        id = 1,
                        initialX = 1,
                        initialY = 5,
                        patrolRoute = listOf(GridPoint(1, 5), GridPoint(1, 1), GridPoint(10, 1), GridPoint(1, 5))
                    ),
                    GuardConfig(
                        id = 2,
                        initialX = 10,
                        initialY = 5,
                        patrolRoute = listOf(GridPoint(10, 5), GridPoint(10, 1), GridPoint(1, 1), GridPoint(10, 5))
                    ),
                    GuardConfig(
                        id = 3,
                        initialX = 1,
                        initialY = 13,
                        patrolRoute = listOf(GridPoint(1, 13), GridPoint(10, 13), GridPoint(1, 13))
                    ),
                    GuardConfig(
                        id = 4,
                        initialX = 5,
                        initialY = 15,
                        patrolRoute = listOf(GridPoint(5, 15), GridPoint(5, 12), GridPoint(5, 15))
                    ),
                    GuardConfig(
                        id = 5,
                        initialX = 10,
                        initialY = 10,
                        patrolRoute = listOf(GridPoint(10, 10), GridPoint(1, 10), GridPoint(10, 10))
                    )
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 3, cellY = 8, isVertical = true, length = 3, togglePeriod = 4),
                    LaserConfig(id = 2, cellX = 7, cellY = 8, isVertical = true, length = 3, togglePeriod = 4)
                ),
                freezeTraps = listOf(GridPoint(1, 8), GridPoint(10, 8))
            ),

            // LEVEL 5: Shadow's Gauntlet (The ultimate combination of ALL traps + 6 fast guards)
            LevelData(
                levelNumber = 5,
                name = "Shadow's Gauntlet",
                helipad = GridPoint(6, 0),
                walls = setOf(
                    GridPoint(0, 3), GridPoint(1, 3), GridPoint(4, 3), GridPoint(5, 3), GridPoint(8, 3), GridPoint(9, 3),
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(6, 6), GridPoint(7, 6), GridPoint(10, 6),
                    GridPoint(1, 9), GridPoint(4, 9), GridPoint(5, 9), GridPoint(8, 9), GridPoint(11, 9),
                    GridPoint(2, 12), GridPoint(3, 12), GridPoint(6, 12), GridPoint(7, 12), GridPoint(10, 12),
                    GridPoint(0, 15), GridPoint(1, 15), GridPoint(4, 15), GridPoint(5, 15), GridPoint(8, 15), GridPoint(9, 15)
                ),
                wallTypes = mapOf(
                    GridPoint(0, 3) to 1, GridPoint(1, 3) to 1, GridPoint(4, 3) to 2, GridPoint(5, 3) to 2, GridPoint(8, 3) to 1, GridPoint(9, 3) to 1,
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(6, 6) to 3, GridPoint(7, 6) to 3, GridPoint(10, 6) to 3,
                    GridPoint(1, 9) to 2, GridPoint(4, 9) to 2, GridPoint(5, 9) to 2, GridPoint(8, 9) to 2, GridPoint(11, 9) to 2,
                    GridPoint(2, 12) to 3, GridPoint(3, 12) to 3, GridPoint(6, 12) to 3, GridPoint(7, 12) to 3, GridPoint(10, 12) to 3,
                    GridPoint(0, 15) to 1, GridPoint(1, 15) to 1, GridPoint(4, 15) to 2, GridPoint(5, 15) to 2, GridPoint(8, 15) to 1, GridPoint(9, 15) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 1, patrolRoute = listOf(GridPoint(1, 1), GridPoint(10, 1), GridPoint(1, 1))),
                    GuardConfig(id = 2, initialX = 10, initialY = 4, patrolRoute = listOf(GridPoint(10, 4), GridPoint(1, 4), GridPoint(10, 4))),
                    GuardConfig(id = 3, initialX = 1, initialY = 7, patrolRoute = listOf(GridPoint(1, 7), GridPoint(10, 7), GridPoint(1, 7))),
                    GuardConfig(id = 4, initialX = 10, initialY = 10, patrolRoute = listOf(GridPoint(10, 10), GridPoint(1, 10), GridPoint(10, 10))),
                    GuardConfig(id = 5, initialX = 1, initialY = 13, patrolRoute = listOf(GridPoint(1, 13), GridPoint(10, 13), GridPoint(1, 13))),
                    GuardConfig(id = 6, initialX = 10, initialY = 16, patrolRoute = listOf(GridPoint(10, 16), GridPoint(1, 16), GridPoint(10, 16)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 6, cellY = 3, isVertical = false, length = 2, togglePeriod = 4),
                    LaserConfig(id = 2, cellX = 3, cellY = 9, isVertical = false, length = 1, togglePeriod = 4),
                    LaserConfig(id = 3, cellX = 7, cellY = 9, isVertical = false, length = 1, togglePeriod = 4),
                    LaserConfig(id = 4, cellX = 6, cellY = 15, isVertical = false, length = 2, togglePeriod = 4)
                ),
                freezeTraps = listOf(
                    GridPoint(2, 1),
                    GridPoint(9, 7),
                    GridPoint(2, 14),
                    GridPoint(9, 14)
                )
            ),

            // LEVEL 6: Desert Outpost (Spacious daytime military complex, sand ground, no flashlights needed)
            LevelData(
                levelNumber = 6,
                name = "Desert Outpost",
                width = 16,
                height = 16,
                helipad = GridPoint(8, 1),
                walls = setOf(
                    GridPoint(3, 4), GridPoint(4, 4), GridPoint(5, 4), GridPoint(6, 4),
                    GridPoint(10, 4), GridPoint(11, 4), GridPoint(12, 4),
                    GridPoint(2, 8), GridPoint(3, 8), GridPoint(7, 8), GridPoint(8, 8), GridPoint(9, 8), GridPoint(13, 8),
                    GridPoint(4, 12), GridPoint(5, 12), GridPoint(10, 12), GridPoint(11, 12)
                ),
                wallTypes = mapOf(
                    GridPoint(3, 4) to 1, GridPoint(4, 4) to 1, GridPoint(5, 4) to 3, GridPoint(6, 4) to 3,
                    GridPoint(10, 4) to 2, GridPoint(11, 4) to 2, GridPoint(12, 4) to 1,
                    GridPoint(2, 8) to 2, GridPoint(3, 8) to 2, GridPoint(7, 8) to 3, GridPoint(8, 8) to 3, GridPoint(9, 8) to 3, GridPoint(13, 8) to 1,
                    GridPoint(4, 12) to 1, GridPoint(5, 12) to 1, GridPoint(10, 12) to 2, GridPoint(11, 12) to 2
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(14, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 14, initialY = 6, patrolRoute = listOf(GridPoint(14, 6), GridPoint(1, 6), GridPoint(14, 6))),
                    GuardConfig(id = 3, initialX = 2, initialY = 10, patrolRoute = listOf(GridPoint(2, 10), GridPoint(13, 10), GridPoint(2, 10))),
                    GuardConfig(id = 4, initialX = 13, initialY = 13, patrolRoute = listOf(GridPoint(13, 13), GridPoint(2, 13), GridPoint(13, 13)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 7, cellY = 4, isVertical = false, length = 3, togglePeriod = 6)
                ),
                freezeTraps = listOf(GridPoint(8, 14), GridPoint(7, 6))
            ),

            // LEVEL 7: Sunlit Lab Hangar (Expansive daytime laboratory warehouse layout)
            LevelData(
                levelNumber = 7,
                name = "Sunlit Lab Hangar",
                width = 16,
                height = 16,
                helipad = GridPoint(7, 2),
                walls = setOf(
                    GridPoint(1, 4), GridPoint(2, 4), GridPoint(5, 4), GridPoint(6, 4), GridPoint(9, 4), GridPoint(10, 4), GridPoint(13, 4),
                    GridPoint(3, 8), GridPoint(4, 8), GridPoint(7, 8), GridPoint(8, 8), GridPoint(11, 8), GridPoint(12, 8),
                    GridPoint(2, 12), GridPoint(6, 12), GridPoint(9, 12), GridPoint(13, 12)
                ),
                wallTypes = mapOf(
                    GridPoint(1, 4) to 3, GridPoint(2, 4) to 3, GridPoint(5, 4) to 2, GridPoint(6, 4) to 2, GridPoint(9, 4) to 1, GridPoint(10, 4) to 1, GridPoint(13, 4) to 3,
                    GridPoint(3, 8) to 1, GridPoint(4, 8) to 1, GridPoint(7, 8) to 3, GridPoint(8, 8) to 3, GridPoint(11, 8) to 2, GridPoint(12, 8) to 2,
                    GridPoint(2, 12) to 2, GridPoint(6, 12) to 3, GridPoint(9, 12) to 3, GridPoint(13, 12) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 2, patrolRoute = listOf(GridPoint(1, 2), GridPoint(14, 2), GridPoint(1, 2))),
                    GuardConfig(id = 2, initialX = 14, initialY = 6, patrolRoute = listOf(GridPoint(14, 6), GridPoint(1, 6), GridPoint(14, 6))),
                    GuardConfig(id = 3, initialX = 1, initialY = 10, patrolRoute = listOf(GridPoint(1, 10), GridPoint(14, 10), GridPoint(1, 10))),
                    GuardConfig(id = 4, initialX = 14, initialY = 14, patrolRoute = listOf(GridPoint(14, 14), GridPoint(1, 14), GridPoint(14, 14)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 3, cellY = 4, isVertical = true, length = 3, togglePeriod = 4),
                    LaserConfig(id = 2, cellX = 11, cellY = 4, isVertical = true, length = 3, togglePeriod = 4)
                ),
                freezeTraps = listOf(GridPoint(4, 14), GridPoint(11, 14))
            ),

            // LEVEL 8: Emerald Training Base (Bigger training facility with beautiful grassy green day theme)
            LevelData(
                levelNumber = 8,
                name = "Emerald Training Base",
                width = 18,
                height = 18,
                helipad = GridPoint(9, 2),
                walls = setOf(
                    GridPoint(4, 4), GridPoint(5, 4), GridPoint(6, 4), GridPoint(11, 4), GridPoint(12, 4), GridPoint(13, 4),
                    GridPoint(2, 8), GridPoint(3, 8), GridPoint(8, 8), GridPoint(9, 8), GridPoint(14, 8), GridPoint(15, 8),
                    GridPoint(4, 12), GridPoint(5, 12), GridPoint(12, 12), GridPoint(13, 12),
                    GridPoint(8, 15), GridPoint(9, 15)
                ),
                wallTypes = mapOf(
                    GridPoint(4, 4) to 2, GridPoint(5, 4) to 2, GridPoint(6, 4) to 2, GridPoint(11, 4) to 1, GridPoint(12, 4) to 1, GridPoint(13, 4) to 1,
                    GridPoint(2, 8) to 1, GridPoint(3, 8) to 1, GridPoint(8, 8) to 3, GridPoint(9, 8) to 3, GridPoint(14, 8) to 2, GridPoint(15, 8) to 2,
                    GridPoint(4, 12) to 3, GridPoint(5, 12) to 3, GridPoint(12, 12) to 3, GridPoint(13, 12) to 3,
                    GridPoint(8, 15) to 1, GridPoint(9, 15) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(16, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 16, initialY = 6, patrolRoute = listOf(GridPoint(16, 6), GridPoint(1, 6), GridPoint(16, 6))),
                    GuardConfig(id = 3, initialX = 1, initialY = 10, patrolRoute = listOf(GridPoint(1, 10), GridPoint(16, 10), GridPoint(1, 10))),
                    GuardConfig(id = 4, initialX = 16, initialY = 13, patrolRoute = listOf(GridPoint(16, 13), GridPoint(1, 13), GridPoint(16, 13))),
                    GuardConfig(id = 5, initialX = 3, initialY = 16, patrolRoute = listOf(GridPoint(3, 16), GridPoint(14, 16), GridPoint(3, 16)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 8, cellY = 4, isVertical = false, length = 2, togglePeriod = 4)
                ),
                freezeTraps = listOf(GridPoint(5, 6), GridPoint(12, 6))
            ),

            // LEVEL 9: Solar Station Zenith (Clean, silver slate solar grid facility)
            LevelData(
                levelNumber = 9,
                name = "Solar Hangar Zenith",
                width = 18,
                height = 18,
                helipad = GridPoint(9, 1),
                walls = setOf(
                    GridPoint(2, 5), GridPoint(3, 5), GridPoint(4, 5), GridPoint(7, 5), GridPoint(10, 5), GridPoint(13, 5), GridPoint(14, 5), GridPoint(15, 5),
                    GridPoint(5, 9), GridPoint(6, 9), GridPoint(11, 9), GridPoint(12, 9),
                    GridPoint(2, 13), GridPoint(3, 13), GridPoint(14, 13), GridPoint(15, 13)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 5) to 3, GridPoint(3, 5) to 3, GridPoint(4, 5) to 3, GridPoint(7, 5) to 2, GridPoint(10, 5) to 2, GridPoint(13, 5) to 1, GridPoint(14, 5) to 1, GridPoint(15, 5) to 1,
                    GridPoint(5, 9) to 1, GridPoint(6, 9) to 2, GridPoint(11, 9) to 2, GridPoint(12, 9) to 1,
                    GridPoint(2, 13) to 2, GridPoint(3, 13) to 2, GridPoint(14, 13) to 3, GridPoint(15, 13) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(16, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 16, initialY = 7, patrolRoute = listOf(GridPoint(16, 7), GridPoint(1, 7), GridPoint(16, 7))),
                    GuardConfig(id = 3, initialX = 1, initialY = 11, patrolRoute = listOf(GridPoint(1, 11), GridPoint(16, 11), GridPoint(1, 11))),
                    GuardConfig(id = 4, initialX = 16, initialY = 15, patrolRoute = listOf(GridPoint(16, 15), GridPoint(1, 15), GridPoint(16, 15)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 5, cellY = 5, isVertical = false, length = 2, togglePeriod = 4),
                    LaserConfig(id = 2, cellX = 11, cellY = 5, isVertical = false, length = 2, togglePeriod = 4)
                ),
                freezeTraps = listOf(GridPoint(9, 13))
            ),

            // LEVEL 10: Grand Final Sandbox (The ultimate final challenge with 6 alert guards & complete tactical security maze)
            LevelData(
                levelNumber = 10,
                name = "Grand Final Sandbox",
                width = 20,
                height = 20,
                helipad = GridPoint(10, 1),
                walls = setOf(
                    GridPoint(0, 4), GridPoint(1, 4), GridPoint(4, 4), GridPoint(5, 4), GridPoint(8, 4), GridPoint(11, 4), GridPoint(14, 4), GridPoint(15, 4), GridPoint(18, 4), GridPoint(19, 4),
                    GridPoint(3, 8), GridPoint(4, 8), GridPoint(7, 8), GridPoint(12, 8), GridPoint(15, 8), GridPoint(16, 8),
                    GridPoint(0, 12), GridPoint(1, 12), GridPoint(5, 12), GridPoint(6, 12), GridPoint(13, 12), GridPoint(14, 12), GridPoint(18, 12), GridPoint(19, 12),
                    GridPoint(4, 16), GridPoint(5, 16), GridPoint(14, 16), GridPoint(15, 16)
                ),
                wallTypes = mapOf(
                    GridPoint(0, 4) to 3, GridPoint(1, 4) to 3, GridPoint(4, 4) to 1, GridPoint(5, 4) to 1, GridPoint(8, 4) to 2, GridPoint(11, 4) to 2, GridPoint(14, 4) to 1, GridPoint(15, 4) to 1, GridPoint(18, 4) to 3, GridPoint(19, 4) to 3,
                    GridPoint(3, 8) to 1, GridPoint(4, 8) to 1, GridPoint(7, 8) to 3, GridPoint(12, 8) to 3, GridPoint(15, 8) to 1, GridPoint(16, 8) to 1,
                    GridPoint(0, 12) to 2, GridPoint(1, 12) to 2, GridPoint(5, 12) to 3, GridPoint(6, 12) to 3, GridPoint(13, 12) to 3, GridPoint(14, 12) to 3, GridPoint(18, 12) to 2, GridPoint(19, 12) to 2,
                    GridPoint(4, 16) to 1, GridPoint(5, 16) to 1, GridPoint(14, 16) to 3, GridPoint(15, 16) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 2, patrolRoute = listOf(GridPoint(1, 2), GridPoint(18, 2), GridPoint(1, 2))),
                    GuardConfig(id = 2, initialX = 18, initialY = 6, patrolRoute = listOf(GridPoint(18, 6), GridPoint(1, 6), GridPoint(18, 6))),
                    GuardConfig(id = 3, initialX = 1, initialY = 10, patrolRoute = listOf(GridPoint(1, 10), GridPoint(18, 10), GridPoint(1, 10))),
                    GuardConfig(id = 4, initialX = 18, initialY = 14, patrolRoute = listOf(GridPoint(18, 14), GridPoint(1, 14), GridPoint(18, 14))),
                    GuardConfig(id = 5, initialX = 1, initialY = 17, patrolRoute = listOf(GridPoint(1, 17), GridPoint(18, 17), GridPoint(1, 17))),
                    GuardConfig(id = 6, initialX = 10, initialY = 11, patrolRoute = listOf(GridPoint(10, 11), GridPoint(10, 15), GridPoint(10, 11)))
                ),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 2, cellY = 4, isVertical = false, length = 2, togglePeriod = 4),
                    LaserConfig(id = 2, cellX = 16, cellY = 4, isVertical = false, length = 2, togglePeriod = 4),
                    LaserConfig(id = 3, cellX = 9, cellY = 8, isVertical = false, length = 3, togglePeriod = 4),
                    LaserConfig(id = 4, cellX = 2, cellY = 12, isVertical = false, length = 3, togglePeriod = 4),
                    LaserConfig(id = 5, cellX = 15, cellY = 12, isVertical = false, length = 3, togglePeriod = 4)
                ),
                freezeTraps = listOf(
                    GridPoint(4, 2),
                    GridPoint(15, 2),
                    GridPoint(5, 10),
                    GridPoint(14, 10)
                )
            ),

            // LEVEL 11: Corporate Vault Siphon (Locked corporate offices, stashed money bills, patrol robot grid)
            LevelData(
                levelNumber = 11,
                name = "Corporate Vault Siphon",
                width = 18,
                height = 18,
                helipad = GridPoint(9, 1),
                walls = setOf(
                    GridPoint(2, 4), GridPoint(3, 4), GridPoint(4, 4), GridPoint(5, 4),
                    GridPoint(12, 4), GridPoint(13, 4), GridPoint(14, 4), GridPoint(15, 4),
                    GridPoint(2, 9), GridPoint(3, 9), GridPoint(4, 9), GridPoint(5, 9),
                    GridPoint(12, 9), GridPoint(13, 9), GridPoint(14, 9), GridPoint(15, 9)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 4) to 1, GridPoint(3, 4) to 1, GridPoint(4, 4) to 2, GridPoint(5, 4) to 2,
                    GridPoint(12, 4) to 3, GridPoint(13, 4) to 3, GridPoint(14, 4) to 1, GridPoint(15, 4) to 1,
                    GridPoint(2, 9) to 2, GridPoint(3, 9) to 2, GridPoint(4, 9) to 2, GridPoint(5, 9) to 3,
                    GridPoint(12, 9) to 3, GridPoint(13, 9) to 3, GridPoint(14, 9) to 3, GridPoint(15, 9) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(8, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 16, initialY = 6, patrolRoute = listOf(GridPoint(16, 6), GridPoint(10, 6), GridPoint(16, 6))),
                    GuardConfig(id = 3, initialX = 1, initialY = 12, patrolRoute = listOf(GridPoint(1, 12), GridPoint(8, 12), GridPoint(1, 12)))
                ),
                lockedDoors = setOf(GridPoint(6, 4), GridPoint(11, 4)),
                cashStashes = setOf(GridPoint(3, 2), GridPoint(14, 2), GridPoint(3, 7), GridPoint(14, 7)),
                robotRoute = listOf(GridPoint(2, 6), GridPoint(8, 6), GridPoint(14, 6), GridPoint(8, 6))
            ),

            // LEVEL 12: The Robotics Hangar (20x20 Warehouse with Security bot patrolling and locked vaults)
            LevelData(
                levelNumber = 12,
                name = "The Robotics Hangar",
                width = 20,
                height = 20,
                helipad = GridPoint(10, 1),
                walls = setOf(
                    GridPoint(1, 5), GridPoint(2, 5), GridPoint(3, 5), GridPoint(4, 5), GridPoint(5, 5),
                    GridPoint(14, 5), GridPoint(15, 5), GridPoint(16, 5), GridPoint(17, 5), GridPoint(18, 5),
                    GridPoint(3, 10), GridPoint(4, 10), GridPoint(5, 10), GridPoint(6, 10),
                    GridPoint(13, 10), GridPoint(14, 10), GridPoint(15, 10), GridPoint(16, 10)
                ),
                wallTypes = mapOf(
                    GridPoint(1, 5) to 1, GridPoint(2, 5) to 1, GridPoint(3, 5) to 2, GridPoint(4, 5) to 2, GridPoint(5, 5) to 2,
                    GridPoint(14, 5) to 3, GridPoint(15, 5) to 3, GridPoint(16, 5) to 1, GridPoint(17, 5) to 1, GridPoint(18, 5) to 1,
                    GridPoint(3, 10) to 2, GridPoint(4, 10) to 2, GridPoint(5, 10) to 2, GridPoint(6, 10) to 3,
                    GridPoint(13, 10) to 3, GridPoint(14, 10) to 2, GridPoint(15, 10) to 2, GridPoint(16, 10) to 2
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(17, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 17, initialY = 8, patrolRoute = listOf(GridPoint(17, 8), GridPoint(2, 8), GridPoint(17, 8))),
                    GuardConfig(id = 3, initialX = 2, initialY = 13, patrolRoute = listOf(GridPoint(2, 13), GridPoint(17, 13), GridPoint(2, 13))),
                    GuardConfig(id = 4, initialX = 10, initialY = 16, patrolRoute = listOf(GridPoint(10, 16), GridPoint(10, 19), GridPoint(10, 16)))
                ),
                lockedDoors = setOf(GridPoint(6, 5), GridPoint(13, 5), GridPoint(7, 10), GridPoint(12, 10)),
                cashStashes = setOf(GridPoint(3, 3), GridPoint(16, 3), GridPoint(2, 11), GridPoint(17, 11)),
                robotRoute = listOf(GridPoint(3, 7), GridPoint(10, 7), GridPoint(16, 7), GridPoint(10, 7))
            ),

            // LEVEL 13: Cipher Office Maze (22x22 Expanded board with 4 separate office boxes, 4 security robot nodes)
            LevelData(
                levelNumber = 13,
                name = "Cipher Office Maze",
                width = 22,
                height = 22,
                helipad = GridPoint(11, 1),
                walls = setOf(
                    GridPoint(2, 4), GridPoint(3, 4), GridPoint(4, 4), GridPoint(5, 4),
                    GridPoint(16, 4), GridPoint(17, 4), GridPoint(18, 4), GridPoint(19, 4),
                    GridPoint(2, 11), GridPoint(3, 11), GridPoint(4, 11), GridPoint(5, 11),
                    GridPoint(16, 11), GridPoint(17, 11), GridPoint(18, 11), GridPoint(19, 11),
                    GridPoint(2, 17), GridPoint(3, 17), GridPoint(4, 17), GridPoint(5, 17),
                    GridPoint(16, 17), GridPoint(17, 17), GridPoint(18, 17), GridPoint(19, 17)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 4) to 3, GridPoint(3, 4) to 3, GridPoint(4, 4) to 3, GridPoint(5, 4) to 3,
                    GridPoint(16, 4) to 1, GridPoint(17, 4) to 1, GridPoint(18, 4) to 1, GridPoint(19, 4) to 1,
                    GridPoint(2, 11) to 2, GridPoint(3, 11) to 2, GridPoint(4, 11) to 2, GridPoint(5, 11) to 2,
                    GridPoint(16, 11) to 2, GridPoint(17, 11) to 2, GridPoint(18, 11) to 2, GridPoint(19, 11) to 2,
                    GridPoint(2, 17) to 1, GridPoint(3, 17) to 1, GridPoint(4, 17) to 1, GridPoint(5, 17) to 1,
                    GridPoint(16, 17) to 3, GridPoint(17, 17) to 3, GridPoint(18, 17) to 3, GridPoint(19, 17) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(20, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 20, initialY = 8, patrolRoute = listOf(GridPoint(20, 8), GridPoint(1, 8), GridPoint(20, 8))),
                    GuardConfig(id = 3, initialX = 1, initialY = 13, patrolRoute = listOf(GridPoint(1, 13), GridPoint(20, 13), GridPoint(1, 13))),
                    GuardConfig(id = 4, initialX = 20, initialY = 18, patrolRoute = listOf(GridPoint(20, 18), GridPoint(1, 18), GridPoint(20, 18)))
                ),
                lockedDoors = setOf(GridPoint(6, 4), GridPoint(15, 4), GridPoint(6, 11), GridPoint(15, 11), GridPoint(6, 17), GridPoint(15, 17)),
                cashStashes = setOf(GridPoint(3, 2), GridPoint(18, 2), GridPoint(3, 10), GridPoint(18, 10), GridPoint(3, 16), GridPoint(18, 16)),
                robotRoute = listOf(GridPoint(3, 6), GridPoint(10, 6), GridPoint(18, 6), GridPoint(10, 6))
            ),

            // LEVEL 14: Mega Vault Headquarters (24x24 security mainframe center)
            LevelData(
                levelNumber = 14,
                name = "Mega Vault Headquarters",
                width = 24,
                height = 24,
                helipad = GridPoint(12, 1),
                walls = setOf(
                    GridPoint(2, 5), GridPoint(3, 5), GridPoint(4, 5), GridPoint(5, 5), GridPoint(6, 5),
                    GridPoint(17, 5), GridPoint(18, 5), GridPoint(19, 5), GridPoint(20, 5), GridPoint(21, 5),
                    GridPoint(2, 12), GridPoint(3, 12), GridPoint(4, 12), GridPoint(5, 12), GridPoint(6, 12),
                    GridPoint(17, 12), GridPoint(18, 12), GridPoint(19, 12), GridPoint(20, 12), GridPoint(21, 12),
                    GridPoint(2, 18), GridPoint(3, 18), GridPoint(4, 18), GridPoint(5, 18), GridPoint(6, 18),
                    GridPoint(17, 18), GridPoint(18, 18), GridPoint(19, 18), GridPoint(20, 18), GridPoint(21, 18)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 5) to 1, GridPoint(3, 5) to 2, GridPoint(4, 5) to 2, GridPoint(5, 5) to 2, GridPoint(6, 5) to 1,
                    GridPoint(17, 5) to 3, GridPoint(18, 5) to 3, GridPoint(19, 5) to 3, GridPoint(20, 5) to 1, GridPoint(21, 5) to 1,
                    GridPoint(2, 12) to 2, GridPoint(3, 12) to 2, GridPoint(4, 12) to 2, GridPoint(5, 12) to 2, GridPoint(6, 12) to 2,
                    GridPoint(17, 12) to 2, GridPoint(18, 12) to 2, GridPoint(19, 12) to 3, GridPoint(20, 12) to 3, GridPoint(21, 12) to 3,
                    GridPoint(2, 18) to 1, GridPoint(3, 18) to 1, GridPoint(4, 18) to 1, GridPoint(5, 18) to 2, GridPoint(6, 18) to 2,
                    GridPoint(17, 18) to 2, GridPoint(18, 18) to 2, GridPoint(19, 18) to 1, GridPoint(20, 18) to 1, GridPoint(21, 18) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(21, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 21, initialY = 9, patrolRoute = listOf(GridPoint(21, 9), GridPoint(2, 9), GridPoint(21, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 15, patrolRoute = listOf(GridPoint(2, 15), GridPoint(21, 15), GridPoint(2, 15))),
                    GuardConfig(id = 4, initialX = 21, initialY = 21, patrolRoute = listOf(GridPoint(21, 21), GridPoint(2, 21), GridPoint(21, 21)))
                ),
                lockedDoors = setOf(GridPoint(7, 5), GridPoint(16, 5), GridPoint(7, 12), GridPoint(16, 12), GridPoint(7, 18), GridPoint(16, 18)),
                cashStashes = setOf(GridPoint(4, 3), GridPoint(19, 3), GridPoint(4, 10), GridPoint(19, 10), GridPoint(4, 16), GridPoint(19, 16)),
                robotRoute = listOf(GridPoint(4, 7), GridPoint(12, 7), GridPoint(19, 7), GridPoint(12, 7))
            ),

            // LEVEL 15: Zenith Fortress (26x26 Grand finale vault complex with high alarms and full stealth operations)
            LevelData(
                levelNumber = 15,
                name = "Zenith Sky Fortress",
                width = 26,
                height = 26,
                helipad = GridPoint(13, 1),
                walls = setOf(
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(4, 6), GridPoint(5, 6), GridPoint(6, 6), GridPoint(7, 6),
                    GridPoint(18, 6), GridPoint(19, 6), GridPoint(20, 6), GridPoint(21, 6), GridPoint(22, 6), GridPoint(23, 6),
                    GridPoint(2, 13), GridPoint(3, 13), GridPoint(4, 13), GridPoint(5, 13), GridPoint(6, 13), GridPoint(7, 13),
                    GridPoint(18, 13), GridPoint(19, 13), GridPoint(20, 13), GridPoint(21, 13), GridPoint(22, 13), GridPoint(23, 13),
                    GridPoint(2, 20), GridPoint(3, 20), GridPoint(4, 20), GridPoint(5, 20), GridPoint(6, 20), GridPoint(7, 20),
                    GridPoint(18, 20), GridPoint(19, 20), GridPoint(20, 20), GridPoint(21, 20), GridPoint(22, 20), GridPoint(23, 20)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(4, 6) to 2, GridPoint(5, 6) to 2, GridPoint(6, 6) to 2, GridPoint(7, 6) to 1,
                    GridPoint(18, 6) to 1, GridPoint(19, 6) to 3, GridPoint(20, 6) to 3, GridPoint(21, 6) to 3, GridPoint(22, 6) to 2, GridPoint(23, 6) to 2,
                    GridPoint(2, 13) to 2, GridPoint(3, 13) to 2, GridPoint(4, 13) to 2, GridPoint(5, 13) to 2, GridPoint(6, 13) to 2, GridPoint(7, 13) to 2,
                    GridPoint(18, 13) to 2, GridPoint(19, 13) to 2, GridPoint(20, 13) to 2, GridPoint(21, 13) to 2, GridPoint(22, 13) to 3, GridPoint(23, 13) to 3,
                    GridPoint(2, 20) to 1, GridPoint(3, 20) to 1, GridPoint(4, 20) to 1, GridPoint(5, 20) to 1, GridPoint(6, 20) to 2, GridPoint(7, 20) to 2,
                    GridPoint(18, 20) to 2, GridPoint(19, 20) to 2, GridPoint(20, 20) to 1, GridPoint(21, 20) to 1, GridPoint(22, 20) to 1, GridPoint(23, 20) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(23, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 23, initialY = 9, patrolRoute = listOf(GridPoint(23, 9), GridPoint(2, 9), GridPoint(23, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 16, patrolRoute = listOf(GridPoint(2, 16), GridPoint(23, 16), GridPoint(2, 16))),
                    GuardConfig(id = 4, initialX = 23, initialY = 23, patrolRoute = listOf(GridPoint(23, 23), GridPoint(2, 23), GridPoint(23, 23)))
                ),
                lockedDoors = setOf(GridPoint(8, 6), GridPoint(17, 6), GridPoint(8, 13), GridPoint(17, 13), GridPoint(8, 20), GridPoint(17, 20)),
                cashStashes = setOf(GridPoint(4, 4), GridPoint(21, 4), GridPoint(4, 11), GridPoint(21, 11), GridPoint(4, 18), GridPoint(21, 18)),
                robotRoute = listOf(GridPoint(4, 8), GridPoint(13, 8), GridPoint(21, 8), GridPoint(13, 8))
            ),

            // LEVEL 16: Rainy Docks Patrol (Rain theme, size 18x18, helipad at 9,1)
            LevelData(
                levelNumber = 16,
                name = "Rainy Docks Patrol",
                width = 18,
                height = 18,
                helipad = GridPoint(9, 1),
                walls = setOf(
                    GridPoint(3, 4), GridPoint(4, 4), GridPoint(5, 4), GridPoint(12, 4), GridPoint(13, 4), GridPoint(14, 4),
                    GridPoint(1, 8), GridPoint(2, 8), GridPoint(8, 8), GridPoint(9, 8), GridPoint(15, 8), GridPoint(16, 8),
                    GridPoint(4, 13), GridPoint(5, 13), GridPoint(12, 13), GridPoint(13, 13)
                ),
                wallTypes = mapOf(
                    GridPoint(3, 4) to 1, GridPoint(4, 4) to 1, GridPoint(5, 4) to 1, GridPoint(12, 4) to 2, GridPoint(13, 4) to 2, GridPoint(14, 4) to 2,
                    GridPoint(1, 8) to 2, GridPoint(2, 8) to 2, GridPoint(8, 8) to 3, GridPoint(9, 8) to 3, GridPoint(15, 8) to 1, GridPoint(16, 8) to 1,
                    GridPoint(4, 13) to 3, GridPoint(5, 13) to 3, GridPoint(12, 13) to 1, GridPoint(13, 13) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(16, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 16, initialY = 6, patrolRoute = listOf(GridPoint(16, 6), GridPoint(1, 6), GridPoint(16, 6))),
                    GuardConfig(id = 3, initialX = 1, initialY = 11, patrolRoute = listOf(GridPoint(1, 11), GridPoint(16, 11), GridPoint(1, 11)))
                ),
                lockedDoors = setOf(GridPoint(6, 4), GridPoint(11, 4)),
                cashStashes = setOf(GridPoint(4, 2), GridPoint(13, 2), GridPoint(5, 10), GridPoint(12, 10)),
                robotRoute = listOf(GridPoint(2, 6), GridPoint(9, 6), GridPoint(16, 6), GridPoint(9, 6))
            ),

            // LEVEL 17: Arctic Outpost Zero (Snow theme, size 18x18, helipad at 9,1)
            LevelData(
                levelNumber = 17,
                name = "Arctic Outpost Zero",
                width = 18,
                height = 18,
                helipad = GridPoint(9, 1),
                walls = setOf(
                    GridPoint(2, 4), GridPoint(3, 4), GridPoint(4, 4), GridPoint(13, 4), GridPoint(14, 4), GridPoint(15, 4),
                    GridPoint(4, 9), GridPoint(5, 9), GridPoint(12, 9), GridPoint(13, 9),
                    GridPoint(2, 13), GridPoint(3, 13), GridPoint(14, 13), GridPoint(15, 13)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 4) to 1, GridPoint(3, 4) to 1, GridPoint(4, 4) to 1, GridPoint(13, 4) to 1, GridPoint(14, 4) to 1, GridPoint(15, 4) to 1,
                    GridPoint(4, 9) to 2, GridPoint(5, 9) to 2, GridPoint(12, 9) to 2, GridPoint(13, 9) to 2,
                    GridPoint(2, 13) to 3, GridPoint(3, 13) to 3, GridPoint(14, 13) to 3, GridPoint(15, 13) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 1, initialY = 3, patrolRoute = listOf(GridPoint(1, 3), GridPoint(16, 3), GridPoint(1, 3))),
                    GuardConfig(id = 2, initialX = 16, initialY = 7, patrolRoute = listOf(GridPoint(16, 7), GridPoint(1, 7), GridPoint(16, 7))),
                    GuardConfig(id = 3, initialX = 1, initialY = 11, patrolRoute = listOf(GridPoint(1, 11), GridPoint(16, 11), GridPoint(1, 11)))
                ),
                cashStashes = setOf(GridPoint(3, 2), GridPoint(14, 2), GridPoint(4, 8), GridPoint(13, 8)),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 8, cellY = 4, isVertical = false, length = 2, togglePeriod = 4)
                )
            ),

            // LEVEL 18: Shadow Blackout Mainframe (Extreme Dark theme, size 20x20, helipad at 10,1)
            LevelData(
                levelNumber = 18,
                name = "Shadow Blackout Mainframe",
                width = 20,
                height = 20,
                helipad = GridPoint(10, 1),
                walls = setOf(
                    GridPoint(2, 5), GridPoint(3, 5), GridPoint(4, 5), GridPoint(15, 5), GridPoint(16, 5), GridPoint(17, 5),
                    GridPoint(5, 10), GridPoint(6, 10), GridPoint(13, 10), GridPoint(14, 10),
                    GridPoint(2, 15), GridPoint(3, 15), GridPoint(16, 15), GridPoint(17, 15)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 5) to 3, GridPoint(3, 5) to 3, GridPoint(4, 5) to 3, GridPoint(15, 5) to 3, GridPoint(16, 5) to 3, GridPoint(17, 5) to 3,
                    GridPoint(5, 10) to 2, GridPoint(6, 10) to 2, GridPoint(13, 10) to 2, GridPoint(14, 10) to 2,
                    GridPoint(2, 15) to 1, GridPoint(3, 15) to 1, GridPoint(16, 15) to 1, GridPoint(17, 15) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(17, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 17, initialY = 8, patrolRoute = listOf(GridPoint(17, 8), GridPoint(2, 8), GridPoint(17, 8))),
                    GuardConfig(id = 3, initialX = 2, initialY = 13, patrolRoute = listOf(GridPoint(2, 13), GridPoint(17, 13), GridPoint(2, 13)))
                ),
                lockedDoors = setOf(GridPoint(7, 5), GridPoint(12, 5)),
                cashStashes = setOf(GridPoint(3, 3), GridPoint(16, 3), GridPoint(4, 9), GridPoint(15, 9)),
                robotRoute = listOf(GridPoint(3, 7), GridPoint(10, 7), GridPoint(16, 7), GridPoint(10, 7))
            ),

            // LEVEL 19: Brilliant Daylight Nexus (Day theme, size 20x20, helipad at 10,1)
            LevelData(
                levelNumber = 19,
                name = "Brilliant Daylight Nexus",
                width = 20,
                height = 20,
                helipad = GridPoint(10, 1),
                walls = setOf(
                    GridPoint(3, 5), GridPoint(4, 5), GridPoint(5, 5), GridPoint(14, 5), GridPoint(15, 5), GridPoint(16, 5),
                    GridPoint(5, 10), GridPoint(6, 10), GridPoint(13, 10), GridPoint(14, 10),
                    GridPoint(3, 15), GridPoint(4, 15), GridPoint(15, 15), GridPoint(16, 15)
                ),
                wallTypes = mapOf(
                    GridPoint(3, 5) to 2, GridPoint(4, 5) to 2, GridPoint(5, 5) to 2, GridPoint(14, 5) to 2, GridPoint(15, 5) to 2, GridPoint(16, 5) to 2,
                    GridPoint(5, 10) to 1, GridPoint(6, 10) to 1, GridPoint(13, 10) to 1, GridPoint(14, 10) to 1,
                    GridPoint(3, 15) to 3, GridPoint(4, 15) to 3, GridPoint(15, 15) to 3, GridPoint(16, 15) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(17, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 17, initialY = 7, patrolRoute = listOf(GridPoint(17, 7), GridPoint(2, 7), GridPoint(17, 7))),
                    GuardConfig(id = 3, initialX = 2, initialY = 12, patrolRoute = listOf(GridPoint(2, 12), GridPoint(17, 12), GridPoint(2, 12)))
                ),
                cashStashes = setOf(GridPoint(3, 2), GridPoint(16, 2), GridPoint(4, 8), GridPoint(15, 8)),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 9, cellY = 5, isVertical = false, length = 2, togglePeriod = 4)
                )
            ),

            // LEVEL 20: Stormy Mainframe Base (Heavy rain theme, size 22x22, helipad at 11,1)
            LevelData(
                levelNumber = 20,
                name = "Stormy Mainframe Base",
                width = 22,
                height = 22,
                helipad = GridPoint(11, 1),
                walls = setOf(
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(4, 6), GridPoint(5, 6),
                    GridPoint(16, 6), GridPoint(17, 6), GridPoint(18, 6), GridPoint(19, 6),
                    GridPoint(4, 12), GridPoint(5, 12), GridPoint(14, 12), GridPoint(15, 12),
                    GridPoint(2, 17), GridPoint(3, 17), GridPoint(18, 17), GridPoint(19, 17)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(4, 6) to 3, GridPoint(5, 6) to 3,
                    GridPoint(16, 6) to 1, GridPoint(17, 6) to 1, GridPoint(18, 6) to 1, GridPoint(19, 6) to 1,
                    GridPoint(4, 12) to 2, GridPoint(5, 12) to 2, GridPoint(14, 12) to 2, GridPoint(15, 12) to 2,
                    GridPoint(2, 17) to 1, GridPoint(3, 17) to 1, GridPoint(18, 17) to 1, GridPoint(19, 17) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(19, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 19, initialY = 9, patrolRoute = listOf(GridPoint(19, 9), GridPoint(2, 9), GridPoint(19, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 15, patrolRoute = listOf(GridPoint(2, 15), GridPoint(19, 15), GridPoint(2, 15)))
                ),
                lockedDoors = setOf(GridPoint(7, 6), GridPoint(14, 6)),
                cashStashes = setOf(GridPoint(4, 3), GridPoint(17, 3), GridPoint(5, 10), GridPoint(16, 10)),
                robotRoute = listOf(GridPoint(4, 8), GridPoint(11, 8), GridPoint(17, 8), GridPoint(11, 8))
            ),

            // LEVEL 21: Frostbite Cyber-Hangar (Snow theme, size 22x22, helipad at 11,2)
            LevelData(
                levelNumber = 21,
                name = "Frostbite Cyber-Hangar",
                width = 22,
                height = 22,
                helipad = GridPoint(11, 2),
                walls = setOf(
                    GridPoint(3, 5), GridPoint(4, 5), GridPoint(5, 5), GridPoint(16, 5), GridPoint(17, 5), GridPoint(18, 5),
                    GridPoint(2, 11), GridPoint(3, 11), GridPoint(18, 11), GridPoint(19, 11),
                    GridPoint(3, 17), GridPoint(4, 17), GridPoint(17, 17), GridPoint(18, 17)
                ),
                wallTypes = mapOf(
                    GridPoint(3, 5) to 1, GridPoint(4, 5) to 1, GridPoint(5, 5) to 1, GridPoint(16, 5) to 1, GridPoint(17, 5) to 1, GridPoint(18, 5) to 1,
                    GridPoint(2, 11) to 3, GridPoint(3, 11) to 3, GridPoint(18, 11) to 3, GridPoint(19, 11) to 3,
                    GridPoint(3, 17) to 2, GridPoint(4, 17) to 2, GridPoint(17, 17) to 2, GridPoint(18, 17) to 2
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(19, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 19, initialY = 8, patrolRoute = listOf(GridPoint(19, 8), GridPoint(2, 8), GridPoint(19, 8))),
                    GuardConfig(id = 3, initialX = 2, initialY = 14, patrolRoute = listOf(GridPoint(2, 14), GridPoint(19, 14), GridPoint(2, 14)))
                ),
                cashStashes = setOf(GridPoint(4, 2), GridPoint(17, 2), GridPoint(3, 9), GridPoint(18, 9)),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 10, cellY = 5, isVertical = false, length = 2, togglePeriod = 4)
                )
            ),

            // LEVEL 22: Pitch Black Corridor (Extreme Dark theme, size 24x24, helipad at 12,1)
            LevelData(
                levelNumber = 22,
                name = "Pitch Black Corridor",
                width = 24,
                height = 24,
                helipad = GridPoint(12, 1),
                walls = setOf(
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(4, 6), GridPoint(19, 6), GridPoint(20, 6), GridPoint(21, 6),
                    GridPoint(5, 12), GridPoint(6, 12), GridPoint(17, 12), GridPoint(18, 12),
                    GridPoint(2, 18), GridPoint(3, 18), GridPoint(20, 18), GridPoint(21, 18)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(4, 6) to 3, GridPoint(19, 6) to 3, GridPoint(20, 6) to 3, GridPoint(21, 6) to 3,
                    GridPoint(5, 12) to 2, GridPoint(6, 12) to 2, GridPoint(17, 12) to 2, GridPoint(18, 12) to 2,
                    GridPoint(2, 18) to 1, GridPoint(3, 18) to 1, GridPoint(20, 18) to 1, GridPoint(21, 18) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(21, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 21, initialY = 9, patrolRoute = listOf(GridPoint(21, 9), GridPoint(2, 9), GridPoint(21, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 15, patrolRoute = listOf(GridPoint(2, 15), GridPoint(21, 15), GridPoint(2, 15)))
                ),
                lockedDoors = setOf(GridPoint(8, 6), GridPoint(15, 6)),
                cashStashes = setOf(GridPoint(4, 2), GridPoint(19, 2), GridPoint(5, 10), GridPoint(18, 10)),
                robotRoute = listOf(GridPoint(3, 8), GridPoint(12, 8), GridPoint(20, 8), GridPoint(12, 8))
            ),

            // LEVEL 23: High Noon Plaza (Bright day theme, size 24x24, helipad at 12,2)
            LevelData(
                levelNumber = 23,
                name = "High Noon Plaza",
                width = 24,
                height = 24,
                helipad = GridPoint(12, 2),
                walls = setOf(
                    GridPoint(3, 6), GridPoint(4, 6), GridPoint(5, 6), GridPoint(18, 6), GridPoint(19, 6), GridPoint(20, 6),
                    GridPoint(5, 11), GridPoint(6, 11), GridPoint(17, 11), GridPoint(18, 11),
                    GridPoint(3, 17), GridPoint(4, 17), GridPoint(19, 17), GridPoint(20, 17)
                ),
                wallTypes = mapOf(
                    GridPoint(3, 6) to 2, GridPoint(4, 6) to 2, GridPoint(5, 6) to 2, GridPoint(18, 6) to 2, GridPoint(19, 6) to 2, GridPoint(20, 6) to 2,
                    GridPoint(5, 11) to 1, GridPoint(6, 11) to 1, GridPoint(17, 11) to 1, GridPoint(18, 11) to 1,
                    GridPoint(3, 17) to 3, GridPoint(4, 17) to 3, GridPoint(19, 17) to 3, GridPoint(20, 17) to 3
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(21, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 21, initialY = 8, patrolRoute = listOf(GridPoint(21, 8), GridPoint(2, 8), GridPoint(21, 8))),
                    GuardConfig(id = 3, initialX = 2, initialY = 14, patrolRoute = listOf(GridPoint(2, 14), GridPoint(21, 14), GridPoint(2, 14)))
                ),
                cashStashes = setOf(GridPoint(4, 3), GridPoint(19, 3), GridPoint(4, 9), GridPoint(19, 9)),
                lasers = listOf(
                    LaserConfig(id = 1, cellX = 11, cellY = 6, isVertical = false, length = 2, togglePeriod = 4)
                )
            ),

            // LEVEL 24: Thunder Fortress (Stormy rain theme, size 26x26, helipad at 13,1)
            LevelData(
                levelNumber = 24,
                name = "Thunder Fortress",
                width = 26,
                height = 26,
                helipad = GridPoint(13, 1),
                walls = setOf(
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(4, 6), GridPoint(5, 6), GridPoint(20, 6), GridPoint(21, 6), GridPoint(22, 6), GridPoint(23, 6),
                    GridPoint(6, 13), GridPoint(7, 13), GridPoint(18, 13), GridPoint(19, 13),
                    GridPoint(2, 20), GridPoint(3, 20), GridPoint(22, 20), GridPoint(23, 20)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(4, 6) to 3, GridPoint(5, 6) to 3, GridPoint(20, 6) to 3, GridPoint(21, 6) to 3, GridPoint(22, 6) to 3, GridPoint(23, 6) to 3,
                    GridPoint(6, 13) to 2, GridPoint(7, 13) to 2, GridPoint(18, 13) to 2, GridPoint(19, 13) to 2,
                    GridPoint(2, 20) to 1, GridPoint(3, 20) to 1, GridPoint(22, 20) to 1, GridPoint(23, 20) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(23, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 23, initialY = 9, patrolRoute = listOf(GridPoint(23, 9), GridPoint(2, 9), GridPoint(23, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 16, patrolRoute = listOf(GridPoint(2, 16), GridPoint(23, 16), GridPoint(2, 16)))
                ),
                lockedDoors = setOf(GridPoint(8, 6), GridPoint(17, 6)),
                cashStashes = setOf(GridPoint(4, 2), GridPoint(21, 2), GridPoint(5, 11), GridPoint(20, 11)),
                robotRoute = listOf(GridPoint(3, 8), GridPoint(13, 8), GridPoint(22, 8), GridPoint(13, 8))
            ),

            // LEVEL 25: Mega Emperor Apex Mainframe (The ultimate final level! Snow Storm theme, size 28x28, helipad at 14,2)
            LevelData(
                levelNumber = 25,
                name = "Emperor Apex Mainframe",
                width = 28,
                height = 28,
                helipad = GridPoint(14, 2),
                walls = setOf(
                    GridPoint(2, 6), GridPoint(3, 6), GridPoint(4, 6), GridPoint(5, 6), GridPoint(6, 6), GridPoint(21, 6), GridPoint(22, 6), GridPoint(23, 6), GridPoint(24, 6), GridPoint(25, 6),
                    GridPoint(2, 13), GridPoint(3, 13), GridPoint(4, 13), GridPoint(23, 13), GridPoint(24, 13), GridPoint(25, 13),
                    GridPoint(5, 20), GridPoint(6, 20), GridPoint(7, 20), GridPoint(20, 20), GridPoint(21, 20), GridPoint(22, 20)
                ),
                wallTypes = mapOf(
                    GridPoint(2, 6) to 3, GridPoint(3, 6) to 3, GridPoint(4, 6) to 3, GridPoint(5, 6) to 3, GridPoint(6, 6) to 3, GridPoint(21, 6) to 3, GridPoint(22, 6) to 3, GridPoint(23, 6) to 3, GridPoint(24, 6) to 3, GridPoint(25, 6) to 3,
                    GridPoint(2, 13) to 2, GridPoint(3, 13) to 2, GridPoint(4, 13) to 2, GridPoint(23, 13) to 2, GridPoint(24, 13) to 2, GridPoint(25, 13) to 2,
                    GridPoint(5, 20) to 1, GridPoint(6, 20) to 1, GridPoint(7, 20) to 1, GridPoint(20, 20) to 1, GridPoint(21, 20) to 1, GridPoint(22, 20) to 1
                ),
                guards = listOf(
                    GuardConfig(id = 1, initialX = 2, initialY = 3, patrolRoute = listOf(GridPoint(2, 3), GridPoint(25, 3), GridPoint(2, 3))),
                    GuardConfig(id = 2, initialX = 25, initialY = 9, patrolRoute = listOf(GridPoint(25, 9), GridPoint(2, 9), GridPoint(25, 9))),
                    GuardConfig(id = 3, initialX = 2, initialY = 16, patrolRoute = listOf(GridPoint(2, 16), GridPoint(25, 16), GridPoint(2, 16))),
                    GuardConfig(id = 4, initialX = 25, initialY = 23, patrolRoute = listOf(GridPoint(25, 23), GridPoint(2, 23), GridPoint(25, 23)))
                ),
                lockedDoors = setOf(GridPoint(9, 6), GridPoint(18, 6), GridPoint(10, 13), GridPoint(17, 13)),
                cashStashes = setOf(GridPoint(4, 2), GridPoint(23, 2), GridPoint(4, 11), GridPoint(23, 11), GridPoint(4, 18), GridPoint(23, 18)),
                robotRoute = listOf(GridPoint(3, 8), GridPoint(14, 8), GridPoint(24, 8), GridPoint(14, 8))
            )
        )
    }
}
