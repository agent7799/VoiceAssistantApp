package com.example.voiceassistant

/*
*
* APP NAME: andriodNetVA
* APPID: WEJ2G2-KW575WQY24
*
*
*
*
 */


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {

    val TAG: String = "MainActivity"

    lateinit var requestInput: TextInputEditText

    lateinit var podsAdapter: SimpleAdapter

    lateinit var progressBar : ProgressBar

    val pods = mutableListOf<HashMap<String, String>>(
        HashMap<String,String>().apply {
            put("Title", "Title 1")
            put("Content", "Conyent 1")
        } ,
        HashMap<String,String>().apply {
            put("Title", "Title 2")
            put("Content", "Conyent 2")
        },
        HashMap<String,String>().apply {
            put("Title", "Title 3")
            put("Content", "Conyent 3")
        },
        HashMap<String,String>().apply {
            put("Title", "Title 4")
            put("Content", "Conyent 4")
        },

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val name : String = "Ivan"
//        val surname : String = "Ivanov"
//        var age : Int = 37
//        val height : Double = 172.2
//        val output: TextView = findViewById(R.id.output)
//        val summary : String = "name: $name surname: $surname age: $age height: $height"
//        Log.d(TAG, summary)
//        output.text = summary

        initViews()


    }

    fun initViews(){
        val toolbar : MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        requestInput = findViewById(R.id.text_input_edit)

        val podsList : ListView = findViewById(R.id.pods_list)

        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("Title", "Content"),
            intArrayOf(R.id.title, R.id.content)
        )
        podsList.adapter = podsAdapter

        val voiceInputButton : FloatingActionButton = findViewById(R.id.voice_input_button)
        voiceInputButton.setOnClickListener {
            Log.d(TAG, "Voice input Button clicked")
        }

        progressBar = findViewById(R.id.progresBar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear -> {
                Log.d(TAG, "action clear")
            }
            R.id.action_stop -> {
                Log.d(TAG, "action stop")
            }
        }
        return super.onOptionsItemSelected(item)
    }

}