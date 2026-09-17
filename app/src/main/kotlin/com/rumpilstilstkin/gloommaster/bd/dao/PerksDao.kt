package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rumpilstilstkin.gloommaster.bd.entity.PerkBd
import com.rumpilstilstkin.gloommaster.bd.entity.PerkTranslationBd

@Dao
interface PerksDao {
    @Insert
    suspend fun insertPerks(perks: List<PerkBd>)

    @Insert
    suspend fun insert(perk: PerkBd): Long

    @Insert
    suspend fun insertTranslations(translations: List<PerkTranslationBd>)

    @Query(
        """
        SELECT * FROM PerkTranslationBd as t
            WHERE characterType = :characterType 
            AND (t.locale = :targetLocale
              OR (
                  t.locale = :defaultLocale
                  AND NOT EXISTS (
                      SELECT 1 FROM PerkTranslationBd
                      WHERE characterType = t.characterType AND locale = :targetLocale
                  )
              ))
        """,
    )
    suspend fun getPerksByCharacterClass(
        characterType: String,
        targetLocale: String,
        defaultLocale: String,
    ): List<PerkTranslationBd>
}
