package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rumpilstilstkin.gloommaster.bd.entity.PersonalQuestBd
import com.rumpilstilstkin.gloommaster.bd.entity.PersonalQuestTaskTranslationsBd
import com.rumpilstilstkin.gloommaster.bd.entity.PersonalQuestTranslationsBd

@Dao
interface PersonalQuestDao {
    @Query("SELECT * FROM PersonalQuestBd")
    suspend fun getQuests(): List<PersonalQuestBd>

    @Query(
        """
        SELECT * FROM PersonalQuestTranslationsBd as t
            WHERE questId = :questId
            AND (t.locale = :targetLocale
              OR (
                  t.locale = :defaultLocale
                  AND NOT EXISTS (
                      SELECT 1 FROM PersonalQuestTranslationsBd
                      WHERE questId = t.questId AND locale = :targetLocale
                  )
              ))
              LIMIT 1
        """,
    )
    suspend fun getQuestTranslation(
        questId: String,
        targetLocale: String,
        defaultLocale: String,
    ): PersonalQuestTranslationsBd

    @Query(
        """
        SELECT * FROM PersonalQuestTaskTranslationsBd as t
            WHERE questId = :questId
            AND (t.locale = :targetLocale
              OR (
                  t.locale = :defaultLocale
                  AND NOT EXISTS (
                      SELECT 1 FROM PersonalQuestTaskTranslationsBd
                      WHERE questId = t.questId AND locale = :targetLocale
                  )
              ))
        """,
    )
    suspend fun getTasksTranslation(
        questId: String,
        targetLocale: String,
        defaultLocale: String,
    ): List<PersonalQuestTaskTranslationsBd>

    @Query("SELECT * FROM PersonalQuestBd WHERE questId = :questId LIMIT 1")
    suspend fun getQuest(questId: String): PersonalQuestBd

    @Insert
    suspend fun insertAllPersonalQuest(quests: List<PersonalQuestBd>)

    @Insert
    suspend fun insertTranslations(translations: List<PersonalQuestTranslationsBd>)

    @Insert
    suspend fun insertTaskTranslations(translations: List<PersonalQuestTaskTranslationsBd>)
}
