package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rumpilstilstkin.gloommaster.bd.entity.ScenarioBd
import com.rumpilstilstkin.gloommaster.bd.entity.ScenarioTranslationsBd
import com.rumpilstilstkin.gloommaster.bd.entity.ScenarioWithNameBd

@Dao
interface ScenarioDao {
    @Query(
        """
        SELECT 
            a.scenarioNumber, 
            a.newScenarios, 
            a.requirements, 
            a.monsters, 
            COALESCE(l1.name, l2.name, a.location) AS locationName ,
            a.pack,
            COALESCE(t1.name, t2.name, CAST(a.scenarioNumber AS TEXT)) AS name
        FROM ScenarioBd a
        LEFT JOIN ScenarioTranslationsBd t1 ON a.scenarioNumber = t1.scenarioNumber AND t1.locale = :targetLocale
        LEFT JOIN ScenarioTranslationsBd t2 ON a.scenarioNumber = t2.scenarioNumber AND t2.locale = :defaultLocale
        LEFT JOIN LocationTranslateBd l1 ON a.location =  l1.slug AND l1.locale = :targetLocale
        LEFT JOIN LocationTranslateBd l2 ON a.location = l2.slug AND l2.locale = :defaultLocale
        WHERE a.pack in (:packs)
    """,
    )
    suspend fun getAll(
        packs: List<String>,
        targetLocale: String,
        defaultLocale: String,
    ): List<ScenarioWithNameBd>

    @Query("SELECT * FROM ScenarioBd WHERE scenarioNumber = :scenarioNumber LIMIT 1")
    suspend fun getScenario(scenarioNumber: Int): ScenarioBd

    @Query(
        """
        SELECT 
            a.scenarioNumber, 
            a.newScenarios, 
            a.requirements, 
            a.monsters, 
            COALESCE(l1.name, l2.name, a.location) AS locationName ,
            a.pack,
            COALESCE(t1.name, t2.name, CAST(a.scenarioNumber AS TEXT)) AS name
        FROM ScenarioBd a
        LEFT JOIN ScenarioTranslationsBd t1 ON a.scenarioNumber = t1.scenarioNumber AND t1.locale = :targetLocale
        LEFT JOIN ScenarioTranslationsBd t2 ON a.scenarioNumber = t2.scenarioNumber AND t2.locale = :defaultLocale
        LEFT JOIN LocationTranslateBd l1 ON a.location =  l1.slug AND l1.locale = :targetLocale
        LEFT JOIN LocationTranslateBd l2 ON a.location = l2.slug AND l2.locale = :defaultLocale
        WHERE a.scenarioNumber = :scenarioNumber LIMIT 1
    """,
    )
    suspend fun getScenarioWithName(
        scenarioNumber: Int,
        targetLocale: String,
        defaultLocale: String,
    ): ScenarioWithNameBd

    @Query(
        """
        SELECT
            a.scenarioNumber,
            a.newScenarios,
            a.requirements,
            a.monsters,
            COALESCE(l1.name, l2.name, a.location) AS locationName ,
            a.pack,
            COALESCE(t1.name, t2.name, CAST(a.scenarioNumber AS TEXT)) AS name
        FROM ScenarioBd a
        LEFT JOIN ScenarioTranslationsBd t1 ON a.scenarioNumber = t1.scenarioNumber AND t1.locale = :targetLocale
        LEFT JOIN ScenarioTranslationsBd t2 ON a.scenarioNumber = t2.scenarioNumber AND t2.locale = :defaultLocale
        LEFT JOIN LocationTranslateBd l1 ON a.location =  l1.slug AND l1.locale = :targetLocale
        LEFT JOIN LocationTranslateBd l2 ON a.location = l2.slug AND l2.locale = :defaultLocale
        WHERE a.scenarioNumber IN (:scenarioNumbers)
    """,
    )
    suspend fun getScenariosWithNameByNumbers(
        scenarioNumbers: List<Int>,
        targetLocale: String,
        defaultLocale: String,
    ): List<ScenarioWithNameBd>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllScenario(scenarios: List<ScenarioBd>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllScenarioTranslations(translations: List<ScenarioTranslationsBd>)
}
