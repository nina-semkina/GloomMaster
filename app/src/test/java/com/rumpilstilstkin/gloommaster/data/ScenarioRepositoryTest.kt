package com.rumpilstilstkin.gloommaster.data

import app.cash.turbine.test
import com.rumpilstilstkin.gloommaster.bd.dao.ScenarioDao
import com.rumpilstilstkin.gloommaster.bd.dao.TeamScenarioDao
import com.rumpilstilstkin.gloommaster.bd.entity.ScenarioBd
import com.rumpilstilstkin.gloommaster.bd.entity.ScenarioWithNameBd
import com.rumpilstilstkin.gloommaster.bd.entity.TeamScenarioBd
import com.rumpilstilstkin.gloommaster.bd.entity.TeamScenarioBdDetailsBd
import com.rumpilstilstkin.gloommaster.domain.entity.ScenarioShortInfo
import com.rumpilstilstkin.gloommaster.data.AchievementRepository
import com.rumpilstilstkin.gloommaster.data.ScenarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class ScenarioRepositoryTest {
    private val scenarioDao: ScenarioDao = mockk()
    private val teamScenarioDao: TeamScenarioDao = mockk(relaxUnitFun = true)
    private val achievementRepository: AchievementRepository = mockk()
    private val sut = ScenarioRepository(scenarioDao, teamScenarioDao, achievementRepository)

    @Test
    fun `given DAO returns scenarios when getAllScenarios then dictionary is fetched exactly once and mapping is applied`() = runTest {
        // Given
        coEvery { achievementRepository.currentDictionary() } returns emptyMap()
        coEvery {
            scenarioDao.getAll(packs = listOf("MAIN"), targetLocale = "ru", defaultLocale = "en")
        } returns listOf(
            withNameFixture(scenarioNumber = 1),
            withNameFixture(scenarioNumber = 2),
        )

        // When
        val scenarios = sut.getAllScenarios(locale = "ru", packs = listOf("MAIN"))

        // Then
        expectThat(scenarios.map { it.scenarioNumber }).isEqualTo(listOf(1, 2))
        coVerify(exactly = 1) { achievementRepository.currentDictionary() }
    }

    @Test
    fun `given DAO returns join rows when getAllTeamScenarios then isCompleted is carried over from the join table`() = runTest {
        // Given
        coEvery { teamScenarioDao.getTeamScenarios(10) } returns listOf(
            joinFixture(scenarioNumber = 1, completed = true),
            joinFixture(scenarioNumber = 2, completed = false),
        )

        // When
        val list = sut.getAllTeamScenarios(10)

        // Then
        expectThat(list).hasSize(2)
        expectThat(list[0].isCompleted).isEqualTo(true)
        expectThat(list[1].isCompleted).isEqualTo(false)
    }

    @Test
    fun `given DAO Flow emits join rows when getTeamScenariosFlow is collected then mapped ScenarioShortInfo reaches the consumer`() = runTest(UnconfinedTestDispatcher()) {
        // Given
        every { teamScenarioDao.getTeamScenariosFlow(10) } returns flowOf(
            listOf(joinFixture(scenarioNumber = 1, completed = true)),
        )

        // When / Then
        sut.getTeamScenariosFlow(10).test {
            val emitted = awaitItem()
            expectThat(emitted).hasSize(1)
            expectThat(emitted[0].scenarioNumber).isEqualTo(1)
            expectThat(emitted[0].isCompleted).isEqualTo(true)
            awaitComplete()
        }
    }

    @Test
    fun `given a scenarioNumber when getScenario then DAO is called and dictionary is applied`() = runTest {
        // Given
        coEvery { achievementRepository.currentDictionary() } returns emptyMap()
        coEvery {
            scenarioDao.getScenarioWithName(scenarioNumber = 5, targetLocale = "ru", defaultLocale = "en")
        } returns withNameFixture(scenarioNumber = 5)

        // When
        val scenario = sut.getScenario(scenarioNumber = 5, locale = "ru", isCompleted = true)

        // Then
        expectThat(scenario.scenarioNumber).isEqualTo(5)
        expectThat(scenario.isCompleted).isEqualTo(true)
    }

    @Test
    fun `given two scenarios in input with one completed when getScenariosWithName then completed flags are carried over by number`() = runTest {
        // Given
        coEvery { achievementRepository.currentDictionary() } returns emptyMap()
        coEvery {
            scenarioDao.getScenariosWithNameByNumbers(
                scenarioNumbers = listOf(42, 43),
                targetLocale = "ru",
                defaultLocale = "en",
            )
        } returns listOf(
            withNameFixture(scenarioNumber = 42),
            withNameFixture(scenarioNumber = 43),
        )
        val input = listOf(
            ScenarioShortInfo.fixture(scenarioNumber = 42, isCompleted = true),
            ScenarioShortInfo.fixture(scenarioNumber = 43, isCompleted = false),
        )

        // When
        val out = sut.getScenariosWithName(scenarios = input, locale = "ru")

        // Then
        val byNumber = out.associateBy { it.scenarioNumber }
        expectThat(byNumber[42]?.isCompleted).isEqualTo(true)
        expectThat(byNumber[43]?.isCompleted).isEqualTo(false)
        coVerify(exactly = 1) { achievementRepository.currentDictionary() }
    }

    @Test
    fun `given an empty input when getScenariosWithName then DAO is not hit and dictionary is not fetched`() = runTest {
        // When
        val out = sut.getScenariosWithName(scenarios = emptyList(), locale = "ru")

        // Then
        expectThat(out).isEmpty()
        coVerify(exactly = 0) { achievementRepository.currentDictionary() }
        coVerify(exactly = 0) { scenarioDao.getScenariosWithNameByNumbers(any(), any(), any()) }
    }

    @Test
    fun `given DAO returns a scenario whose number is not in the input when getScenariosWithName then it is mapped with isCompleted false`() = runTest {
        // Given
        coEvery { achievementRepository.currentDictionary() } returns emptyMap()
        coEvery {
            scenarioDao.getScenariosWithNameByNumbers(
                scenarioNumbers = listOf(42),
                targetLocale = "ru",
                defaultLocale = "en",
            )
        } returns listOf(
            withNameFixture(scenarioNumber = 42),
            withNameFixture(scenarioNumber = 99),
        )

        // When
        val out = sut.getScenariosWithName(
            scenarios = listOf(ScenarioShortInfo.fixture(scenarioNumber = 42, isCompleted = true)),
            locale = "ru",
        )

        // Then
        val unmapped = out.first { it.scenarioNumber == 99 }
        expectThat(unmapped.isCompleted).isEqualTo(false)
    }

    @Test
    fun `given a scenarioNumber when getShortScenario then DAO is called and toInfoDomain is applied`() = runTest {
        // Given
        coEvery { scenarioDao.getScenario(scenarioNumber = 7) } returns scenarioBdFixture(scenarioNumber = 7)

        // When
        val info = sut.getShortScenario(7)

        // Then
        expectThat(info.scenarioNumber).isEqualTo(7)
    }

    @Test
    fun `given a scenarioNumber and teamId when saveTeamScenario then DAO insert receives the matching entity`() = runTest {
        // When
        sut.saveTeamScenario(scenarioNumber = 5, teamId = 10)

        // Then
        coVerify(exactly = 1) {
            teamScenarioDao.insert(
                TeamScenarioBd(teamId = 10, scenarioNumber = 5),
            )
        }
    }

    @Test
    fun `given a batch of scenarios when addTeamScenarios then DAO insertAll receives entities with the matching completed flags`() = runTest {
        // When
        sut.addTeamScenarios(
            scenarios = listOf(1 to true, 2 to false),
            teamId = 10,
        )

        // Then
        coVerify(exactly = 1) {
            teamScenarioDao.insertAll(
                listOf(
                    TeamScenarioBd(teamId = 10, scenarioNumber = 1, completed = true),
                    TeamScenarioBd(teamId = 10, scenarioNumber = 2, completed = false),
                ),
            )
        }
    }

    @Test
    fun `given DAO has the team scenario when completeTeamScenario then DAO update is invoked after the read with completed true`() = runTest {
        // Given
        val current = TeamScenarioBd(id = 1, teamId = 10, scenarioNumber = 5, completed = false)
        coEvery { teamScenarioDao.getTeamScenarioClear(10, 5) } returns current

        // When
        sut.completeTeamScenario(teamId = 10, scenarioNumber = 5)

        // Then
        coVerifyOrder {
            teamScenarioDao.getTeamScenarioClear(10, 5)
            teamScenarioDao.update(current.copy(completed = true))
        }
    }

    @Test
    fun `given DAO has the team scenario when restoreTeamScenario then DAO update is invoked with completed false`() = runTest {
        // Given
        val current = TeamScenarioBd(id = 1, teamId = 10, scenarioNumber = 5, completed = true)
        coEvery { teamScenarioDao.getTeamScenarioClear(10, 5) } returns current

        // When
        sut.restoreTeamScenario(teamId = 10, scenarioNumber = 5)

        // Then
        coVerify(exactly = 1) { teamScenarioDao.update(current.copy(completed = false)) }
    }

    @Test
    fun `given a teamId and scenarioNumber when deleteTeamScenario then DAO deleteTeamScenario is invoked with that pair`() = runTest {
        // When
        sut.deleteTeamScenario(teamId = 10, scenarioNumber = 5)

        // Then
        coVerify(exactly = 1) { teamScenarioDao.deleteTeamScenario(10, 5) }
    }

    companion object {
        fun withNameFixture(
            scenarioNumber: Int = 1,
            newScenarios: String = "",
            requirements: String = "",
            monsters: List<String> = emptyList(),
            locationName: String = "",
            pack: String = "MAIN",
            name: String = "Scenario",
        ): ScenarioWithNameBd = ScenarioWithNameBd(
            scenarioNumber = scenarioNumber,
            newScenarios = newScenarios,
            requirements = requirements,
            monsters = monsters,
            locationName = locationName,
            pack = pack,
            name = name,
        )

        fun scenarioBdFixture(
            scenarioNumber: Int = 1,
            pack: String = "MAIN",
        ): ScenarioBd = ScenarioBd(
            scenarioNumber = scenarioNumber,
            newScenarios = "",
            requirements = "",
            monsters = emptyList(),
            location = "",
            pack = pack,
        )

        fun joinFixture(
            scenarioNumber: Int = 1,
            completed: Boolean = false,
            teamId: Int = 10,
        ): TeamScenarioBdDetailsBd = TeamScenarioBdDetailsBd(
            teamScenario = TeamScenarioBd(
                teamId = teamId,
                scenarioNumber = scenarioNumber,
                completed = completed,
            ),
            scenario = ScenarioBd(
                scenarioNumber = scenarioNumber,
                newScenarios = "",
                requirements = "",
                monsters = emptyList(),
                location = "",
                pack = "MAIN",
            ),
        )
    }
}
