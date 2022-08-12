package org.samcrow.neofelis.antrecorder

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.PrintStream
import java.time.format.DateTimeFormatterBuilder

class SelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val db: EventDatabase =
        Room.databaseBuilder(application, EventDatabase::class.java, "events").build()

    private val experimentNames: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>().also { viewModelScope.launch { loadExperimentNames() } }
    }

    /**
     * Returns LiveData containing a list of experiment names, with the most recently modified
     * experiments first
     */
    fun experimentNames(): LiveData<List<String>> {
        return experimentNames
    }

    private suspend fun loadExperimentNames() {
        val names = db.eventDao().getRecentExperiments()
        experimentNames.postValue(names)
    }

    fun refreshExperimentNames() {
        viewModelScope.launch { loadExperimentNames() }
    }

    /**
     * Writes all events with the provided experiment ID to a file at the provided URI
     */
    fun exportEvents(name: String, uri: Uri) {
        viewModelScope.launch { exportEventsInner(name, uri) }
    }

    private suspend fun exportEventsInner(name: String, uri: Uri) {
         // A formatter that produces ISO 8601 values like 2022-08-09T19:40:37.740Z
        val formatter = DateTimeFormatterBuilder().appendInstant(3).toFormatter()
        val events = db.eventDao().getAllExperimentEvents(name)
        val resolver = getApplication<Application>().contentResolver

        withContext(Dispatchers.IO) {
            PrintStream(BufferedOutputStream(resolver.openOutputStream(uri))).use { stream ->
                // Headers
                stream.println("Time,Event")
                for (event in events) {
                    stream.print(formatter.format(event.time))
                    stream.print(',')
                    stream.print(event.type.toString())
                    stream.println()
                }
            }
        }
        // TODO: Indicate success?
    }
}