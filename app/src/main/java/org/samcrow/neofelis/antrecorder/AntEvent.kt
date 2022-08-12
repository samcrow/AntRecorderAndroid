package org.samcrow.neofelis.antrecorder

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class AntEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    /**
     * Experiment name, usually including the colony ID
     */
    val experiment: String,
    /**
     * Time when the event happened, in UTC
     */
    val time: Instant,
    /**
     * Type of event
     */
    val type: EventType
)

enum class EventType {
    AntIn,
    AntOut,
}
