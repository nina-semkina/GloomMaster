package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rumpilstilstkin.gloommaster.bd.entity.GoodBd
import com.rumpilstilstkin.gloommaster.bd.entity.GoodTranslationsBd
import com.rumpilstilstkin.gloommaster.bd.entity.GoodWithTranslation

@Dao
interface GoodsDao {
    @Query(
        """
            SELECT 
                g.*, 
                COALESCE(t1.name, t2.name, CAST(g.displayNumber AS TEXT)) AS translated_name
            FROM GoodBd g
            LEFT JOIN GoodTranslationsBd t1 ON g.displayNumber = t1.displayNumber AND t1.locale = :targetLocale
            LEFT JOIN GoodTranslationsBd t2 ON g.displayNumber = t2.displayNumber AND t2.locale = :defaultLocale
        """,
    )
    suspend fun getAll(
        targetLocale: String,
        defaultLocale: String,
    ): List<GoodWithTranslation>

    @Query(
        """
            SELECT 
                g.*, 
                COALESCE(t1.name, t2.name, CAST(g.displayNumber AS TEXT)) AS translated_name
            FROM GoodBd g
            LEFT JOIN GoodTranslationsBd t1 ON g.displayNumber = t1.displayNumber AND t1.locale = :targetLocale
            LEFT JOIN GoodTranslationsBd t2 ON g.displayNumber = t2.displayNumber AND t2.locale = :defaultLocale
            WHERE g.pack = :pack
        """,
    )
    suspend fun getAllByPack(
        pack: String,
        targetLocale: String,
        defaultLocale: String,
    ): List<GoodWithTranslation>

    @Insert
    suspend fun insertAllGoods(goods: List<GoodBd>)

    @Insert
    suspend fun insertAllGoodTranslations(translations: List<GoodTranslationsBd>)

    @Query(
        """
            SELECT 
                * 
            FROM GoodBd g
            WHERE g.displayNumber IN (:numbers)
        """,
    )
    suspend fun getGoodsByNumbers(numbers: List<Int>): List<GoodBd>

    @Query(
        """
            SELECT 
                g.*, 
                COALESCE(t1.name, t2.name, CAST(g.displayNumber AS TEXT)) AS translated_name
            FROM GoodBd g
            LEFT JOIN GoodTranslationsBd t1 ON g.displayNumber = t1.displayNumber AND t1.locale = :targetLocale
            LEFT JOIN GoodTranslationsBd t2 ON g.displayNumber = t2.displayNumber AND t2.locale = :defaultLocale
            WHERE g.goodId = :goodId
             """,
    )
    suspend fun getGoodById(
        goodId: Int,
        targetLocale: String,
        defaultLocale: String,
    ): GoodWithTranslation?
}
