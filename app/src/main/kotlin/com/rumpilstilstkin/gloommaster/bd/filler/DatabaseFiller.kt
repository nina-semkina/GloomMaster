package com.rumpilstilstkin.gloommaster.bd.filler

import android.content.SharedPreferences
import android.os.Trace
import androidx.core.content.edit
import androidx.room.withTransaction
import com.rumpilstilstkin.gloommaster.bd.GlHelperDatabase
import com.rumpilstilstkin.gloommaster.bd.filler.json.AchievementJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.GameLevelInfoJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.GoodJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.LocationJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.MonsterJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.PerkJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.QuestJsonFiller
import com.rumpilstilstkin.gloommaster.bd.filler.json.ScenarioJsonFiller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseFiller @Inject constructor(
    private val preferences: SharedPreferences,
    private val database: GlHelperDatabase,
    private val gameLevelInfoJsonFiller: GameLevelInfoJsonFiller,
    private val achievementJsonFiller: AchievementJsonFiller,
    private val scenarioJsonFiller: ScenarioJsonFiller,
    private val goodJsonFiller: GoodJsonFiller,
    private val perkJsonFiller: PerkJsonFiller,
    private val questJsonFiller: QuestJsonFiller,
    private val monsterJsonFiller: MonsterJsonFiller,
    private val locationJsonFiller: LocationJsonFiller,
) {
    suspend fun fillDatabase() =
        withContext(Dispatchers.IO) {
            Trace.beginAsyncSection(FILL_TRACE_SECTION, FILL_TRACE_COOKIE)
            try {
                var version = preferences.getInt(PREFS_VERSION, 0)
                database.withTransaction {
                    if (version <= 4) {
                        fillMain()
                        fillForgottenCircles()
                    } else {
                        while (version < VERSION) {
                            update(version)
                            version++
                        }
                    }
                }
                preferences.edit { putInt(PREFS_VERSION, VERSION) }
            } finally {
                Trace.endAsyncSection(FILL_TRACE_SECTION, FILL_TRACE_COOKIE)
            }
        }

    private suspend fun update(version: Int) {
        when (version) {
            6 -> {
                monsterJsonFiller.fillDecks("main")
            }

            // Example for a future language: when "de" assets are added, bump VERSION and seed
            // only its translations for the packs that have them — base data is left untouched.
            // 7 -> fillLocale(locale = "de", packs = listOf("main", "forgottenCircles"))
            else -> {
            }
        }
    }

    suspend fun fillLocale(
        locale: String,
        packs: List<String>,
    ) {
        packs.forEach { pack ->
            scenarioJsonFiller.fillTranslations(pack, locale)
            goodJsonFiller.fillTranslations(pack, locale)
            perkJsonFiller.fillTranslations(pack, locale)
            achievementJsonFiller.fillTranslations(pack, locale)
            monsterJsonFiller.fillTranslations(pack, locale)
            locationJsonFiller.fillTranslations(pack, locale)
            questJsonFiller.fillTranslations(pack, locale)
        }
    }

    private suspend fun fillMain() {
        val pack = "main"
        gameLevelInfoJsonFiller.fill(pack)
        scenarioJsonFiller.fill(pack)
        goodJsonFiller.fill(pack)
        locationJsonFiller.fill(pack)
        achievementJsonFiller.fill(pack)
        perkJsonFiller.fill(pack)
        questJsonFiller.fill(pack)
        monsterJsonFiller.fill(pack)
    }

    private suspend fun fillForgottenCircles() {
        val pack = "forgottenCircles"
        scenarioJsonFiller.fill(pack)
        goodJsonFiller.fill(pack)
        perkJsonFiller.fill(pack)
        monsterJsonFiller.fill(pack)
        achievementJsonFiller.fill(pack)
    }

    companion object {
        private const val FILL_TRACE_SECTION = "DatabaseFiller.fillDatabase"
        private const val FILL_TRACE_COOKIE = 1
        private const val VERSION = 7
        private const val PREFS_VERSION = "filler_version"
    }
}
