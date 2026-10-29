package com.example.ai_chatbot

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Groq API Key (Get a free key from https://console.groq.com/)
    private val groqApiKey = "YOUR_GROQ_API_KEY"

    private lateinit var chatList: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var micButton: ImageButton

    private val messages = ArrayList<ChatModel>()
    private lateinit var adapter: ChatAdapter
    private val requestCodeSpeechInput = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatList = findViewById(R.id.chatList)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        micButton = findViewById(R.id.micButton)

        adapter = ChatAdapter(messages)

        chatList.layoutManager = LinearLayoutManager(this)
        chatList.adapter = adapter

        // Send Button
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addMessage(ChatModel(message, true))
            messageInput.text.clear()
            hideKeyboard()

            apiCall(message)
        }

        // Mic Button
        micButton.setOnClickListener {
            hideKeyboard()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Your AI")
            }

            try {
                startActivityForResult(intent, requestCodeSpeechInput)
            } catch (e: Exception) {
                Toast.makeText(this, "Speech recognition error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodeSpeechInput && resultCode == RESULT_OK && data != null) {
            val res: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!res.isNullOrEmpty()) {
                val spokenText = res[0]
                addMessage(ChatModel(spokenText, true))
                messageInput.text.clear()
                hideKeyboard()
                apiCall(spokenText)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { view ->
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun addMessage(message: ChatModel) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        chatList.scrollToPosition(messages.size - 1)
    }

    private fun apiCall(prompt: String) {
        val botPosition = messages.size
        messages.add(ChatModel("Thinking...", false))
        adapter.notifyItemInserted(botPosition)
        chatList.scrollToPosition(botPosition)

        lifecycleScope.launch(Dispatchers.IO) {
            var answer: String? = null

            // 1. Try Groq Llama 3 API if API Key is set
            if (groqApiKey.isNotBlank() && groqApiKey != "YOUR_GROQ_API_KEY") {
                answer = fetchGroqAiResponse(prompt)
            }

            // 2. Fallback to Free AI Engine if Groq Key is missing/fails
            if (answer.isNullOrBlank()) {
                answer = fetchFreeAiResponse(prompt)
            }

            val finalAnswer = answer ?: "Sorry, I couldn't generate a response."

            withContext(Dispatchers.Main) {
                messages[botPosition] = ChatModel(finalAnswer, false)
                adapter.notifyItemChanged(botPosition)
                chatList.scrollToPosition(botPosition)
            }
        }
    }

    private fun fetchGroqAiResponse(prompt: String): String? {
        return try {
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $groqApiKey")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val jsonBody = JSONObject().apply {
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("model", "llama3-8b-8192")
                put("temperature", 0.6)
                put("max_completion_tokens", 2048)
                put("top_p", 0.95)
                put("stream", false)
            }

            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == 200) {
                val rawResponse = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(rawResponse)
                jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFreeAiResponse(prompt: String): String? {
        try {
            val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
            val url = URL("https://text.pollinations.ai/$encodedPrompt")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/119.0")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            if (connection.responseCode == 200) {
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                if (text.isNotBlank()) return text
            }
        } catch (_: Exception) {
            // Fallthrough
        }

        try {
            val url = URL("https://text.pollinations.ai/")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/119.0")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val safePrompt = prompt.replace("\"", "\\\"").replace("\n", " ")
            val jsonBody = "{\"messages\":[{\"role\":\"user\",\"content\":\"$safePrompt\"}]}"

            connection.outputStream.use { os ->
                os.write(jsonBody.toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == 200) {
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                if (text.isNotBlank()) return text
            }
        } catch (_: Exception) {
            // Fail
        }

        return null
    }
}
