package com.rumpilstilstkin.gloommaster.bd.filler.json

import com.rumpilstilstkin.gloommaster.bd.dao.PerksDao
import com.rumpilstilstkin.gloommaster.bd.entity.PerkBd
import com.rumpilstilstkin.gloommaster.bd.entity.PerkTranslationBd
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.CharacterPerksJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.PerkTranslationGroupJson
import javax.inject.Inject

class PerkJsonFiller @Inject constructor(
    private val jsonDataLoader: JsonDataLoader,
    private val perksDao: PerksDao,
) {
    suspend fun fill(pack: String) {
        val data =
            jsonDataLoader
                .loadDictionaryList<CharacterPerksJson>("perks.json", pack)
                .map { PerkBd(it.perksCount, it.characterType) }
        perksDao.insertPerks(data)

        jsonDataLoader.getLocalesForPack(pack).forEach { locale ->
            fillTranslations(pack, locale)
        }
    }

    suspend fun fillTranslations(
        pack: String,
        locale: String,
    ) {
        val translationGroups =
            jsonDataLoader.loadDictionaryListOrEmpty<PerkTranslationGroupJson>("perks.json", "$pack/$locale")
        val entities =
            translationGroups.flatMap { group ->
                group.perks.map {
                    PerkTranslationBd(
                        perkId = it.id,
                        locale = locale,
                        text = it.text,
                        characterType = group.characterType,
                    )
                }
            }
        perksDao.insertTranslations(entities)
    }
}
