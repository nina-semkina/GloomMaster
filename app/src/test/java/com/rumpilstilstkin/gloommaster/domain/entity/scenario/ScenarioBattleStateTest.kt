package com.rumpilstilstkin.gloommaster.domain.entity.scenario

import com.rumpilstilstkin.gloommaster.domain.entity.scenario.MagicChargeState
import com.rumpilstilstkin.gloommaster.domain.entity.scenario.MonsterDeckState
import com.rumpilstilstkin.gloommaster.domain.entity.scenario.MonsterItem
import com.rumpilstilstkin.gloommaster.domain.entity.scenario.MonsterUnit
import com.rumpilstilstkin.gloommaster.domain.entity.scenario.ScenarioBattleState
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ScenarioBattleStateTest {

    private val monsterSlug = "oozy"
    private val unitNumber = 1

    @Test
    fun `given matching slug and unit number when updateUnit then unit is transformed`() {
        // Given
        val state = state(
            activeMonsters = mapOf(
                monsterSlug to
                monsterItem(
                    monsterSlug,
                    units = mapOf(unitNumber to unit(number = unitNumber, currentLife = 5))
                ),
            ),
        )

        // When
        val updated = state.updateUnit(slug = monsterSlug, number = unitNumber) { copy(currentLife = 1) }

        // Then
        expectThat(updated.activeMonsters[monsterSlug]?.units[unitNumber]?.currentLife).isEqualTo(1)
    }

    @Test
    fun `given missing slug when updateUnit then state is unchanged`() {
        // Given
        val state = state(
            activeMonsters = mapOf(
                monsterSlug to
                        monsterItem(
                            monsterSlug,
                            units = mapOf(unitNumber to unit(number = unitNumber, currentLife = 5))
                        ),
            ),
        )

        // When
        val updated = state.updateUnit(slug = "unknown", number = 1) { copy(currentLife = 1) }

        // Then
        expectThat(updated.activeMonsters[monsterSlug]?.units[unitNumber]?.currentLife).isEqualTo(5)
        expectThat(updated.activeMonsters["unknown"]).isNull()
    }

    private fun state(
        activeMonsters: Map<String, MonsterItem>,
    ) = ScenarioBattleState(
        generalLevel = 0,
        scenarioNumber = 1,
        name = "Scenario",
        monsters = emptyMap(),
        golds = 0,
        exp = 0,
        trapDamage = 0,
        gamersCount = 2,
        monsterLevel = 0,
        deck = MonsterDeckState.create(emptyList()),
        activeMonsters = activeMonsters,
        round = 0,
        magicState = MagicChargeState.initial(),
        availableEffects = emptySet(),
    )

    private fun monsterItem(
        slug: String,
        units: Map<Int, MonsterUnit>,
    ) = MonsterItem(
        slug = slug,
        name = slug,
        isFly = false,
        deck = "deck",
        currentCard = null,
        units = units,
        isBoss = false,
    )

    private fun unit(
        number: Int,
        currentLife: Int,
    ) = MonsterUnit.fixture(
        number = number,
        currentLife = currentLife,
        maxLife = 10,
    )
}
