package com.rumpilstilstkin.gloommaster.bd.filler.json

import com.rumpilstilstkin.gloommaster.bd.dao.GoodsDao
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.GoodJson
import com.rumpilstilstkin.gloommaster.bd.filler.json.models.GoodTranslationJson
import javax.inject.Inject

class GoodJsonFiller @Inject constructor(
    private val jsonDataLoader: JsonDataLoader,
    private val goodsDao: GoodsDao,
) {
    suspend fun fill(pack: String) {
        val data = jsonDataLoader.loadDictionaryList<GoodJson>("goods.json", pack)
        val entities =
            data.flatMap { good ->
                List(good.count) { good.toEntity() }
            }
        goodsDao.insertAllGoods(entities)
        jsonDataLoader.getLocalesForPack(pack).forEach { locale ->
            fillTranslations(pack, locale)
        }
    }

    suspend fun fillTranslations(
        pack: String,
        locale: String,
    ) {
        val translations =
            jsonDataLoader.loadDictionaryListOrEmpty<GoodTranslationJson>(
                "goods.json",
                "$pack/$locale",
            )
        val translationsEntities = translations.map { it.toEntity(locale) }
        goodsDao.insertAllGoodTranslations(translationsEntities)
    }
}
