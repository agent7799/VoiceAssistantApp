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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val TAG: String = "MainActivity"

    lateinit var requestInput: TextInputEditText

    lateinit var podsAdapter: SimpleAdapter

    lateinit var progressBar: ProgressBar

    lateinit var waEngine: WAEngine

    val pods = mutableListOf<HashMap<String, String>>()

    lateinit var textToSpeech : TextToSpeech

    var isTtsReady : Boolean = false

    val VOICE_RECOGNITION_REQUEST_CODE : Int = 777

//        HashMap<String,String>().apply {
//            put("Title", "Title 1")
//            put("Content", "Conyent 1")
//        } ,
//        HashMap<String,String>().apply {
//            put("Title", "Title 2")
//            put("Content", "Conyent 2")
//        },
//        HashMap<String,String>().apply {
//            put("Title", "Title 3")
//            put("Content", "Conyent 3")
//        },
//        HashMap<String,String>().apply {
//            put("Title", "Title 4")
//            put("Content", "Conyent 4")
//        },

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
        initWolframEngine()
        initTts()

    }

    fun initViews() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        requestInput = findViewById(R.id.text_input_edit)
        requestInput.setOnEditorActionListener { v, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                pods.clear()
                podsAdapter.notifyDataSetChanged()

                val question = requestInput.text.toString()
                askWolfram(question)
            }

            return@setOnEditorActionListener false
        }

        val podsList: ListView = findViewById(R.id.pods_list)

        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("Title", "Content"),
            intArrayOf(R.id.title, R.id.content)
        )
        podsList.adapter = podsAdapter
        podsList.setOnItemClickListener {parent, view, position, id ->
        if (isTtsReady) {
            val title = pods[position]["Title"]
            val content = pods[position]["Content"]
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, title)
            }
        }


        val voiceInputButton: FloatingActionButton = findViewById(R.id.voice_input_button)
        voiceInputButton.setOnClickListener {
            Log.d(TAG, "Voice input Button clicked")
            pods.clear()
            podsAdapter.notifyDataSetChanged()

            if (isTtsReady){
                textToSpeech.stop()
            }

            showVoiceInputDialog()
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
                requestInput.text?.clear()
                pods.clear()
                podsAdapter.notifyDataSetChanged()
            }
            R.id.action_stop -> {
                Log.d(TAG, "action stop")
                if (isTtsReady){
                    textToSpeech.stop()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun initWolframEngine() {
        waEngine = WAEngine().apply {
            appID = "WEJ2G2-KW575WQY24"
            addFormat("plaintext")
        }
    }

    fun showSnackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.ok) {
                dismiss()
            }
            show()
        }
    }

    fun askWolfram(request: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val query = waEngine.createQuery().apply { input = request }
            runCatching {
                waEngine.performQuery(query)
            }.onSuccess { result ->
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    // обработка запрос - ответ
                    if (result.isError) {
                        showSnackBar(result.errorMessage)
                        return@withContext
                    }

                    if (!result.isSuccess) {
                        requestInput.error = getString(R.string.error_do_not_understand)
                        return@withContext
                    }

                    for (pod in result.pods) {
                        if (pod.isError) continue
                        val content = StringBuilder()
                        for (subpod in pod.subpods) {
                            for (element in subpod.contents) {
                                if (element is WAPlainText) {
                                    content.append(element.text)
                                }
                            }
                        }
                        pods.add(0, HashMap<String, String>().apply {
                            put("Title", pod.title)
                            put("Content", content.toString())
                        })
                    }
                    podsAdapter.notifyDataSetChanged()
                }
            }.onFailure { t ->
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    // обработка ошибки
                    showSnackBar(t.message ?: getString(R.string.error_something_went_wrong))

                }

            }
        }
    }

    fun initTts(){
        textToSpeech  = TextToSpeech(this) { code ->
            if (code != TextToSpeech.SUCCESS){
                Log.e(TAG, "TTS Error code: $code")
                showSnackBar(getString(R.string.error_tts_not_ready))
            } else {
                isTtsReady = true
            }
        }
        textToSpeech.language = Locale.US
    }

    fun showVoiceInputDialog(){
        val intent = Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.request_hint))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        }

        kotlin.runCatching {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)

        }.onFailure {
            t -> showSnackBar(t.message ?: getString(R.string.error_voice_recognition_unavailable))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)?.let{
                question  -> requestInput.setText(question)
                askWolfram(question)
            }
        }


    }


}