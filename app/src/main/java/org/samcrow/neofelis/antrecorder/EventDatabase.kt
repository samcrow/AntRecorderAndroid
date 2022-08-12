package org.samcrow.neofelis.antrecorder

import androidx.room.*
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder

@Dao
interface AntEventDao {
    @Query("SELECT * FROM AntEvent")
    @Transaction
    suspend fun getAllEvents(): List<AntEvent>

    /**
     * Returns a list of all events for the provided experiment, sorted from oldest to newest
     */
    @Query("SELECT * FROM AntEvent WHERE experiment = :experiment ORDER BY time ASC")
    @Transaction
    suspend fun getAllExperimentEvents(experiment: String): List<AntEvent>

    @Insert
    suspend fun insert(event: AntEvent): Long

    @Delete
    suspend fun delete(event: AntEvent)

    @Query("SELECT COUNT(*) FROM AntEvent WHERE type = 'AntIn' AND experiment = :experiment")
    suspend fun countAntInEvents(experiment: String): Long
    @Query("SELECT COUNT(*) FROM AntEvent WHERE type = 'AntOut' AND experiment = :experiment")
    suspend fun countAntOutEvents(experiment: String): Long

    /**
     * Deletes the newest (most recent) event associated with the provided experiment
     */
    @Query("DELETE FROM AntEvent WHERE id = (SELECT id FROM AntEvent WHERE experiment = :experiment ORDER BY time DESC LIMIT 1)")
    suspend fun deleteLastEvent(experiment: String)

    /**
     * Returns a list of experiment names, with the most recently modified experiment first
     */
    @Query("SELECT experiment FROM AntEvent GROUP BY experiment ORDER BY max(time) DESC")
    suspend fun getRecentExperiments(): List<String>
}

@Database(entities = [AntEvent::class], version = 1)
@TypeConverters(Converters::class)
abstract class EventDatabase: RoomDatabase() {
    abstract fun eventDao(): AntEventDao
}

class Converters {
    /**
     * A formatter that uses ISO 8601 in UTC with millisecond precision, always with 3 decimal
     * digits
     * (so the output has a fixed length, for correct sorting in the database)
     *
     * This produces values like 2022-08-09T19:40:37.740Z
     */
    private val formatter = DateTimeFormatterBuilder().appendInstant(3).toFormatter()

    @TypeConverter
    fun parseTime(value: String?): Instant? {
        return value?.let { formatter.parse(it, Instant::from) }
    }

    @TypeConverter
    fun encodeTime(value: Instant?): String? {
        return value?.let { formatter.format(it) }
    }
}
