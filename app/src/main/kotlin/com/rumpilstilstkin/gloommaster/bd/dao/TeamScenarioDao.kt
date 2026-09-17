package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rumpilstilstkin.gloommaster.bd.entity.TeamScenarioBd
import com.rumpilstilstkin.gloommaster.bd.entity.TeamScenarioBdDetailsBd
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamScenarioDao {
    @Insert
    suspend fun insertAll(scenarios: List<TeamScenarioBd>)

    @Insert
    suspend fun insert(scenario: TeamScenarioBd)

    @Update
    suspend fun update(team: TeamScenarioBd)

    @Transaction
    @Query("SELECT * FROM TeamScenarioBd WHERE teamId = :teamId AND scenarioNumber = :scenarioNumber LIMIT 1")
    suspend fun getTeamScenario(
        teamId: Int,
        scenarioNumber: Int,
    ): TeamScenarioBdDetailsBd

    @Query("SELECT * FROM TeamScenarioBd WHERE teamId = :teamId AND scenarioNumber = :scenarioNumber LIMIT 1")
    suspend fun getTeamScenarioClear(
        teamId: Int,
        scenarioNumber: Int,
    ): TeamScenarioBd

    @Transaction
    @Query("SELECT * FROM TeamScenarioBd WHERE teamId = :teamId")
    suspend fun getTeamScenarios(teamId: Int): List<TeamScenarioBdDetailsBd>

    @Transaction
    @Query("SELECT * FROM TeamScenarioBd WHERE teamId = :teamId")
    fun getTeamScenariosFlow(teamId: Int): Flow<List<TeamScenarioBdDetailsBd>>

    @Query("DELETE FROM TeamScenarioBd WHERE teamId = :teamId AND scenarioNumber = :scenarioNumber")
    suspend fun deleteTeamScenario(
        teamId: Int,
        scenarioNumber: Int,
    )
}
