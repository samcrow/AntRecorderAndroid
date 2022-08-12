package org.samcrow.neofelis.antrecorder

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SelectionActivity : AppCompatActivity() {

    private val saveFileResult = registerForActivityResult(CreateCsv()) { uri ->
        if (uri != null) {
            Log.i("SelectionActivity", "Exporting to $uri")
            // exportName was set in startExport
            val name = exportName!!
            model!!.exportEvents(name, uri)
            exportName = null
        }
    }

    private var nameField: EditText? = null
    private var model: SelectionViewModel? = null

    /**
     * The name of the experiment being exported
     *
     * This should be non-null only while choosing a file name to export.
     */
    private var exportName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)
        model = ViewModelProvider(this)[SelectionViewModel::class.java]

        nameField = findViewById(R.id.experimentNameField)

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            startNewExperiment(it)
        }

        // Configure enter button on keyboard to be equivalent to start button
        nameField!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                startNewExperiment(startButton)
                true
            } else {
                false
            }
        }

        // List of recent experiments
        val experimentList = findViewById<RecyclerView>(R.id.recentExperimentList)
        experimentList.layoutManager = LinearLayoutManager(this)

        val adapter = ExperimentNameAdapter(this)
        experimentList.adapter = adapter
        model!!.experimentNames().observe(this) { names ->
            adapter.submitList(names)
        }
    }

    private fun startNewExperiment(button: View) {
        val text = nameField!!.text.toString()
        if (text.isNotEmpty()) {
            startExperiment(this, text, button)
        }
    }

    fun startExport(name: String) {
        exportName = name
        saveFileResult.launch(name)
    }

    override fun onResume() {
        super.onResume()
        // Update the list of experiments, which might have been changed by another activity
        model?.refreshExperimentNames()
    }
}

fun startExperiment(ctx: Context, name: String, button: View) {
    // Fancy animation
    val options = ActivityOptions.makeScaleUpAnimation(
        button,
        0,
        0,
        button.width,
        button.height
    )

    val intent = Intent(ctx, MainActivity::class.java)
    intent.putExtra(MainActivity.EXTRA_EXPERIMENT_NAME, name)
    ctx.startActivity(intent, options.toBundle())
}

class ExperimentNameAdapter(private val activity: SelectionActivity) :
    ListAdapter<String, ExperimentNameHolder>(StringItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentNameHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.experiment_list_item, parent, false)
        return ExperimentNameHolder(view)
    }

    override fun onBindViewHolder(holder: ExperimentNameHolder, position: Int) {
        val name = getItem(position)
        holder.name.text = name
        holder.openButton.setOnClickListener {
            startExperiment(holder.itemView.context, name, it)
        }
        holder.exportButton.setOnClickListener {
            activity.startExport(name)
        }
    }
}

class StringItemCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

}

class ExperimentNameHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.nameLabel)
    val openButton: Button = view.findViewById(R.id.openButton)
    val exportButton: Button = view.findViewById(R.id.exportButton)
}

/**
 * An activity contract that creates a CSV file
 */
class CreateCsv : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        intent.type = "text/csv"
        // Add .csv to the end of the file name so the saved file will always have the correct
        // extension
        val title = intent.getStringExtra(Intent.EXTRA_TITLE)
        intent.putExtra(Intent.EXTRA_TITLE, "$title.csv")
        return intent
    }
}
