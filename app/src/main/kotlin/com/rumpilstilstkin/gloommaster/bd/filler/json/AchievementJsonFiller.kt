package com.rumpilstilstkin.gloommaster.bd.filler.json

import com.rumpilstilstkin.gloommaster.bd.dao.AchievementDao
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.AchievementJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.AchievementTranslationJson
import javax.inject.Inject

class AchievementJsonFiller @Inject constructor(
    private val jsonDataLoader: JsonDataLoader,
    private val achievementDao: AchievementDao,
) {
    suspend fun fill(pack: String) {
        val data =
            jsonDataLoader.loadDictionaryList<AchievementJson>("achievements.json", pack)
        val entities = data.map { it.toEntity() }
        achievementDao.insertAchievements(entities)
        jsonDataLoader.getLocalesForPack(pack).forEach { locale ->
            fillTranslations(pack, locale)
        }
    }

    suspend fun fillTranslations(
        pack: String,
        locale: String,
    ) {
        val translations =
            jsonDataLoader.loadDictionaryListOrEmpty<AchievementTranslationJson>(
                "achievements.json",
                "$pack/$locale",
            )
        val translationsEntities = translations.map { it.toEntity(locale) }
        achievementDao.insertAchievementTranslations(translationsEntities)
    }
}
