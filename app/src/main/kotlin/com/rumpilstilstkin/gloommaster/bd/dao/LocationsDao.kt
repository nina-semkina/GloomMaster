package com.rumpilstilstkin.gloommaster.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import com.rumpilstilstkin.gloommaster.bd.entity.LocationBd
import com.rumpilstilstkin.gloommaster.bd.entity.LocationTranslateBd

@Dao
interface LocationsDao {
    @Insert
    suspend fun insertAllLocation(locations: List<LocationBd>)

    @Insert
    suspend fun insertAllLocationTranslate(locations: List<LocationTranslateBd>)
}
