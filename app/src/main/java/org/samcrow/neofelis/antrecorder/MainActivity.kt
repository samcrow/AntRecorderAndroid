package org.samcrow.neofelis.antrecorder

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.time.Instant

class MainActivity : AppCompatActivity() {
    companion object {
        val EXTRA_EXPERIMENT_NAME: String =
            MainActivity::class.qualifiedName!! + ".EXTRA_EXPERIMENT_NAME"
    }

    private var model: AntEventVewModel? = null

    /**
     * In and out sounds
     *
     * this is non-null between onStart() and onStop()
     */
    private var sounds: ButtonSounds? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get experiment name
        val experiment = intent?.getStringExtra(EXTRA_EXPERIMENT_NAME)
            ?: throw IllegalStateException("EXTRA_EXPERIMENT_NAME required")
        title = experiment

        model = ViewModelProvider(
            this,
            AntEventViewModelFactory(experiment, application)
        )[AntEventVewModel::class.java]

        val inCountLabel = findViewById<TextView>(R.id.inCount)
        val outCountLabel = findViewById<TextView>(R.id.outCount)

        model!!.getAntInEventCount().observe(this) { count ->
            inCountLabel.text = count.toString()
        }
        model!!.getAntOutEventCount().observe(this) { count ->
            outCountLabel.text = count.toString()
        }

        // Buttons
        findViewById<Button>(R.id.inButton).let {
            it.setOnClickListener {
                model?.recordAntInEvent(Instant.now())
                sounds?.playInSound()
            }
        }
        findViewById<Button>(R.id.outButton).let {
            it.setOnClickListener {
                model?.recordAntOutEvent(Instant.now())
                sounds?.playOutSound()
            }
        }
        findViewById<Button>(R.id.undoButton).let {
            it.setOnClickListener {
                model?.deleteLastEvent()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sounds = ButtonSounds(this)
    }

    override fun onStop() {
        super.onStop()
        sounds?.release()
        sounds = null
    }
}