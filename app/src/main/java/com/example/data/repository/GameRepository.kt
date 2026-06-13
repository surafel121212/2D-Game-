package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.model.GameProgress
import com.example.data.model.HeroDefinition
import com.example.data.model.HeroState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {

    val gameProgress: Flow<GameProgress> = gameDao.getGameProgressFlow().map { progress ->
        progress ?: GameProgress(id = 1, diamonds = 100, currentLevel = 1, selectedHeroId = "std_green")
    }

    val heroStates: Flow<List<HeroState>> = gameDao.getAllHeroStatesFlow()

    suspend fun ensureInitialSetup() {
        val existingProgress = gameDao.getGameProgressSync()
        if (existingProgress == null) {
            // Setup default game progress
            gameDao.saveGameProgress(GameProgress(id = 1, diamonds = 200, currentLevel = 1, selectedHeroId = "std_green"))
        }

        // Setup base hero states (default unlocked)
        val allHeroes = HeroDefinition.HEROES
        val initialHeroStates = allHeroes.map { hero ->
            val existing = gameDao.getHeroState(hero.id)
            if (existing != null) {
                existing
            } else {
                // Free standard heroes, rest are locked
                val isFree = hero.id == "std_green" || hero.id == "std_yellow"
                HeroState(heroId = hero.id, isUnlocked = isFree)
            }
        }
        gameDao.saveHeroStates(initialHeroStates)
    }

    suspend fun addDiamonds(amount: Int) {
        val current = gameDao.getGameProgressSync() ?: GameProgress()
        gameDao.saveGameProgress(current.copy(diamonds = current.diamonds + amount))
    }

    suspend fun selectHero(heroId: String) {
        val current = gameDao.getGameProgressSync() ?: GameProgress()
        gameDao.saveGameProgress(current.copy(selectedHeroId = heroId))
    }

    suspend fun unlockHero(heroId: String, cost: Int): Boolean {
        val current = gameDao.getGameProgressSync() ?: GameProgress()
        if (current.diamonds >= cost) {
            // Deduct diamonds
            gameDao.saveGameProgress(current.copy(diamonds = current.diamonds - cost, selectedHeroId = heroId))
            // Unlock hero
            gameDao.saveHeroState(HeroState(heroId = heroId, isUnlocked = true))
            return true
        }
        return false
    }

    suspend fun advanceLevel(completedLevel: Int) {
        val current = gameDao.getGameProgressSync() ?: GameProgress()
        // If they completed the current highest level, advance to next
        if (completedLevel >= current.currentLevel) {
            gameDao.saveGameProgress(current.copy(currentLevel = completedLevel + 1))
        }
    }
}
