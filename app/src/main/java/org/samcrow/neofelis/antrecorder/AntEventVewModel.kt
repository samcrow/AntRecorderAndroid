package org.samcrow.neofelis.antrecorder

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * A ViewModel with a database connection that can record ant in/out events and supply
 * the number of events that have happened
 */
class AntEventVewModel(private val experiment: String, application: Application) :
    AndroidViewModel(application) {
    private val db: EventDatabase =
        Room.databaseBuilder(application, EventDatabase::class.java, "events").build()

    private val antInEventCount: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also { viewModelScope.launch { loadAntInEvents() } }
    }
    private val antOutEventCount: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also { viewModelScope.launch { loadAntOutEvents() } }
    }

    fun getAntInEventCount(): LiveData<Long> {
        return antInEventCount
    }

    fun getAntOutEventCount(): LiveData<Long> {
        return antOutEventCount
    }

    fun recordAntInEvent(time: Instant) {
        val event = AntEvent(0, experiment, time, EventType.AntIn)
        viewModelScope.launch {
            db.eventDao().insert(event)
            antInEventCount.postValue(antInEventCount.value!! + 1)
        }
    }

    fun recordAntOutEvent(time: Instant) {
        val event = AntEvent(0, experiment, time, EventType.AntOut)
        viewModelScope.launch {
            db.eventDao().insert(event)
            antOutEventCount.postValue(antOutEventCount.value!! + 1)
        }
    }

    private suspend fun loadAntInEvents() {
        val events = db.eventDao().countAntInEvents(experiment)
        antInEventCount.postValue(events)
    }

    private suspend fun loadAntOutEvents() {
        val events = db.eventDao().countAntOutEvents(experiment)
        antOutEventCount.postValue(events)
    }

    override fun onCleared() {
        super.onCleared()
        db.close()
    }

    fun deleteLastEvent() {
        viewModelScope.launch {
            db.eventDao().deleteLastEvent(experiment)
            // Count events again to check that they're accurate
            loadAntInEvents()
            loadAntOutEvents()
        }
    }
}

class AntEventViewModelFactory(
    private val experiment: String,
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        assert(modelClass == AntEventVewModel::class.java)
        @Suppress("UNCHECKED_CAST")
        return AntEventVewModel(experiment, application) as T
    }
}