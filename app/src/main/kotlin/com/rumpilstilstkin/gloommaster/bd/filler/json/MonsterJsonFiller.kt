package com.rumpilstilstkin.gloommaster.bd.filler.json

import com.rumpilstilstkin.gloommaster.bd.dao.MonsterDao
import com.rumpilstilstkin.gloommaster.bd.entity.MonsterAbilityCardTranslationBd
import com.rumpilstilstkin.gloommaster.bd.entity.MonsterStatsSeedBd
import com.rumpilstilstkin.gloommaster.bd.entity.MonsterTextStatsBd
import com.rumpilstilstkin.gloommaster.bd.entity.MonsterTranslationsBd
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.DeckJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.DeckTranslationJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.MonsterJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.MonsterStatsJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.MonsterTranslationJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.MonsterTranslationStatsJson
import com.rumpilstilstkin.gloommaster.domain.entity.monster.MonsterAction
import javax.inject.Inject

class MonsterJsonFiller @Inject constructor(
    private val jsonDataLoader: JsonDataLoader,
    private val monsterDao: MonsterDao,
) {
    suspend fun fill(pack: String) {
        fillDecks(pack)
        fillMonsters(pack)
        fillStats(pack)
    }

    suspend fun fillDecks(pack: String) {
        val decks = jsonDataLoader.loadDictionaryList<DeckJson>("ability_decks.json", pack)
        val entities = decks.flatMap(DeckJson::toEntity)
        monsterDao.insertCards(entities)
        val translations =
            jsonDataLoader.getLocalesForPack(pack).flatMap { locale ->
                loadDeckTranslations(pack, locale)
            }
        monsterDao.insertCardTranslations(translations)
    }

    private suspend fun fillMonsters(pack: String) {
        val file = "monsters.json"
        val monsters = jsonDataLoader.loadDictionaryList<MonsterJson>(file, pack)
        val entities = monsters.map { it.toEntity() }
        monsterDao.insertMonsters(entities)

        val translations =
            jsonDataLoader.getLocalesForPack(pack).flatMap { locale ->
                loadMonsterTranslations(pack, locale)
            }
        monsterDao.insertMonsterTranslations(translations)
    }

    private suspend fun fillStats(pack: String) {
        val stats =
            STATS_FILES.flatMap { fileName ->
                loadStats(fileName, pack)
            }
        monsterDao.insertAllStats(stats)

        val translations =
            jsonDataLoader.getLocalesForPack(pack).flatMap { locale ->
                STATS_FILES.flatMap { fileName ->
                    loadStatsTranslations(fileName, pack, locale)
                }
            }
        monsterDao.insertAllTextStats(translations)
    }

    /** Loads only the per-locale text (names + stat text) for an additional language. */
    suspend fun fillTranslations(
        pack: String,
        locale: String,
    ) {
        monsterDao.insertMonsterTranslations(loadMonsterTranslations(pack, locale))
        monsterDao.insertCardTranslations(loadDeckTranslations(pack, locale))
        val stats =
            STATS_FILES.flatMap { fileName ->
                loadStatsTranslations(fileName, pack, locale)
            }
        monsterDao.insertAllTextStats(stats)
    }

    private fun loadDeckTranslations(
        pack: String,
        locale: String,
    ): List<MonsterAbilityCardTranslationBd> {
        val decks =
            jsonDataLoader.loadDictionaryListOrEmpty<DeckTranslationJson>("ability_decks.json", "$pack/$locale")
        return decks.flatMap { it.toEntity(locale) }
    }

    private fun loadMonsterTranslations(
        pack: String,
        locale: String,
    ): List<MonsterTranslationsBd> {
        val translations =
            jsonDataLoader.loadDictionaryListOrEmpty<MonsterTranslationJson>("monsters.json", "$pack/$locale")
        return translations.map { it.toEntity(locale) }
    }

    private fun loadStats(
        fileName: String,
        pack: String,
    ): List<MonsterStatsSeedBd> {
        val data = jsonDataLoader.loadDictionaryList<MonsterStatsJson>(fileName, pack)
        return data.flatMap { monsterStat ->
            monsterStat.stats.map { levelStat ->
                MonsterStatsSeedBd(
                    monsterSlug = monsterStat.monsterSlug,
                    scenarioLevel = levelStat.level,
                    isElite = levelStat.isElite,
                    life = levelStat.life,
                    stats = levelStat.stats.toString(),
                )
            }
        }
    }

    private fun loadStatsTranslations(
        fileName: String,
        pack: String,
        locale: String,
    ): List<MonsterTextStatsBd> {
        val translationFile = "text_$fileName"
        val translations =
            jsonDataLoader.loadDictionaryListOrEmpty<MonsterTranslationStatsJson>(
                translationFile,
                "$pack/$locale",
            )
        return translations.flatMap { monsterStat ->
            monsterStat.stats.map { levelStat ->
                MonsterTextStatsBd(
                    locale = locale,
                    monsterSlug = monsterStat.monsterSlug,
                    scenarioLevel = levelStat.level,
                    isElite = levelStat.isElite,
                    stats = levelStat.textStats.map { MonsterAction.Text(content = it) },
                )
            }
        }
    }

    companion object {
        private val STATS_FILES = listOf("boss_stats.json", "base_stats.json")
    }
}
