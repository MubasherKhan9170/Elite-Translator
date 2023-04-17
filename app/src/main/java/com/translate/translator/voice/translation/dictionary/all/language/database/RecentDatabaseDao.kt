package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface RecentDatabaseDao {
    @Insert
    suspend fun insert(item: RecentItem)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
    suspend fun update(item: RecentItem)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from recent_language_table WHERE id = :key")
    suspend fun get(key: Long): RecentItem?

    /**
     * Deletes the selected Item values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM recent_language_table WHERE id = :key")
    suspend fun delete(key: Long)



    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM recent_language_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM recent_language_table ORDER BY id DESC")
    fun getAllRecent(): LiveData<List<RecentItem>>


    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM recent_language_table ORDER BY id DESC LIMIT 1")
    suspend fun getToRecent(): RecentItem?


}
