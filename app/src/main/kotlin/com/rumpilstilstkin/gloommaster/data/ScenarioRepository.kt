package com.rumpilstilstkin.gloommaster.data

import com.rumpilstilstkin.gloommaster.bd.dao.ScenarioDao
import com.rumpilstilstkin.gloommaster.bd.dao.TeamScenarioDao
import com.rumpilstilstkin.gloommaster.bd.entity.TeamScenarioBd
import com.rumpilstilstkin.gloommaster.data.mappers.toDomain
import com.rumpilstilstkin.gloommaster.data.mappers.toInfoDomain
import com.rumpilstilstkin.gloommaster.data.mappers.toShortDomain
import com.rumpilstilstkin.gloommaster.domain.entity.ScenarioInfo
import com.rumpilstilstkin.gloommaster.domain.entity.ScenarioInfoWithName
import com.rumpilstilstkin.gloommaster.domain.entity.ScenarioShortInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScenarioRepository @Inject constructor(
    private val scenarioDao: ScenarioDao,
    private val teamScenarioDao: TeamScenarioDao,
    private val achievementRepository: AchievementRepository,
) {
    suspend fun getAllScenarios(
        locale: String,
        packs: List<String>,
    ): List<ScenarioInfoWithName> {
        val dictionary = achievementRepository.currentDictionary()
        return scenarioDao
            .getAll(
                packs = packs,
                targetLocale = locale,
                defaultLocale = LocaleRepository.DEFAULT_LOCALE,
            ).map {
                it.toDomain(
                    isCompleted = false,
                    dictionary = dictionary,
                )
            }
    }

    suspend fun getAllTeamScenarios(teamId: Int): List<ScenarioShortInfo> =
        teamScenarioDao.getTeamScenarios(teamId).map {
            it.scenario.toShortDomain(
                isCompleted = it.teamScenario.completed,
            )
        }

    fun getTeamScenariosFlow(teamId: Int): Flow<List<ScenarioShortInfo>> =
        teamScenarioDao
            .getTeamScenariosFlow(teamId)
            .map { scenarioBds ->
                scenarioBds.map {
                    it.scenario.toShortDomain(
                        isCompleted = it.teamScenario.completed,
                    )
                }
            }

    suspend fun getScenario(
        scenarioNumber: Int,
        locale: String,
        isCompleted: Boolean = false,
    ): ScenarioInfoWithName =
        scenarioDao
            .getScenarioWithName(
                scenarioNumber = scenarioNumber,
                targetLocale = locale,
                defaultLocale = LocaleRepository.DEFAULT_LOCALE,
            ).toDomain(
                isCompleted = isCompleted,
                dictionary = achievementRepository.currentDictionary(),
            )

    suspend fun getScenariosWithName(
        scenarios: List<ScenarioShortInfo>,
        locale: String,
    ): List<ScenarioInfoWithName> {
        if (scenarios.isEmpty()) return emptyList()
        val dictionary = achievementRepository.currentDictionary()
        val completedByNumber = scenarios.associate { it.scenarioNumber to it.isCompleted }
        return scenarioDao
            .getScenariosWithNameByNumbers(
                scenarioNumbers = scenarios.map { it.scenarioNumber },
                targetLocale = locale,
                defaultLocale = LocaleRepository.DEFAULT_LOCALE,
            ).map { scenario ->
                scenario.toDomain(
                    isCompleted = completedByNumber[scenario.scenarioNumber] ?: false,
                    dictionary = dictionary,
                )
            }
    }

    suspend fun getShortScenario(scenarioNumber: Int): ScenarioInfo =
        scenarioDao
            .getScenario(
                scenarioNumber = scenarioNumber,
            ).toInfoDomain()

    suspend fun saveTeamScenario(
        scenarioNumber: Int,
        teamId: Int,
    ) {
        teamScenarioDao.insert(
            TeamScenarioBd(
                teamId = teamId,
                scenarioNumber = scenarioNumber,
            ),
        )
    }

    suspend fun addTeamScenarios(
        scenarios: List<Pair<Int, Boolean>>,
        teamId: Int,
    ) {
        teamScenarioDao.insertAll(
            scenarios
                .map { (number, completed) ->
                    TeamScenarioBd(
                        teamId = teamId,
                        scenarioNumber = number,
                        completed = completed,
                    )
                },
        )
    }

    suspend fun completeTeamScenario(
        teamId: Int,
        scenarioNumber: Int,
    ) {
        teamScenarioDao.getTeamScenarioClear(teamId, scenarioNumber).let {
            teamScenarioDao.update(it.copy(completed = true))
        }
    }

    suspend fun restoreTeamScenario(
        teamId: Int,
        scenarioNumber: Int,
    ) {
        teamScenarioDao.getTeamScenarioClear(teamId, scenarioNumber).let {
            teamScenarioDao.update(it.copy(completed = false))
        }
    }

    suspend fun deleteTeamScenario(
        teamId: Int,
        scenarioNumber: Int,
    ) {
        teamScenarioDao.deleteTeamScenario(teamId, scenarioNumber)
    }
}
