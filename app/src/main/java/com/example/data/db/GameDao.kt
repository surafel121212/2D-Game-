package com.example.data.db

import androidx.room.*
import com.example.data.model.GameProgress
import com.example.data.model.HeroState
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    fun getGameProgressFlow(): Flow<GameProgress?>

    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    suspend fun getGameProgressSync(): GameProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameProgress(progress: GameProgress)

    @Query("SELECT * FROM hero_state")
    fun getAllHeroStatesFlow(): Flow<List<HeroState>>

    @Query("SELECT * FROM hero_state WHERE heroId = :heroId LIMIT 1")
    suspend fun getHeroState(heroId: String): HeroState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHeroState(state: HeroState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHeroStates(states: List<HeroState>)
}
