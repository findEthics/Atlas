package com.example.chatty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var queryInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var currentModel = AIModel.PERPLEXITY
    private lateinit var aiClient: AIClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aiClient = AIClient()
        initializeViews()
        setupRecyclerView()

        // Setup navigation drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupClickListeners()

        setupModelSwitch()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.messagesRecycler)
        queryInput = findViewById(R.id.queryInput)
        sendButton = findViewById(R.id.sendButton)
        resetButton = findViewById(R.id.resetButton)
        titleText = findViewById(R.id.titleText)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val query = queryInput.text.toString().trim()
            if (query.isNotEmpty()) {
                handleQuery(query)
            }
        }

        resetButton.setOnClickListener {
            resetSession()
        }

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_api_key -> {
                    showApiKeyDialog(currentModel)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupModelSwitch() {
        val modelSwitch = findViewById<Button>(R.id.modelSwitch)
        modelSwitch.setOnClickListener {
            currentModel = if (currentModel == AIModel.PERPLEXITY) {
                AIModel.GROK
            } else {
                AIModel.PERPLEXITY
            }
            modelSwitch.text = currentModel.name
        }
    }

    private fun handleQuery(query: String) {
        loadingProgressBar.visibility = View.VISIBLE
        sendButton.isEnabled = false
        val apiKey = when(currentModel) {
            AIModel.PERPLEXITY -> aiClient.loadApiKey(this, AIModel.PERPLEXITY)
            AIModel.GROK -> aiClient.loadApiKey(this, AIModel.GROK)
        }
        if (apiKey.isEmpty()) {
            showApiKeyDialog(currentModel)
            loadingProgressBar.visibility = View.GONE
            sendButton.isEnabled = true
        } else {
            sendQueryToApi(query, apiKey)
        }
    }

    private fun sendQueryToApi(query: String, apiKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the current model selection
                val model = currentModel
                // Execute the API call for the selected model
                val response = when (model) {
                    AIModel.PERPLEXITY -> aiClient.queryPerplexity(query, apiKey)
                    AIModel.GROK -> aiClient.queryGrok(query, apiKey)
                }
                withContext(Dispatchers.Main) {
                    updateUIWithResponse(query, response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = when (e) {
                        is IllegalArgumentException -> "Invalid API key"
                        else -> "API error: ${e.localizedMessage}"
                    }
                    println("sendQueryToApi ${e.message}")
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    loadingProgressBar.visibility = View.GONE
                    sendButton.isEnabled = true
                }
            }
        }
    }

    private fun updateUIWithResponse(query: String, response: String) {
        loadingProgressBar.visibility = View.GONE
        sendButton.isEnabled = true
        titleText.visibility = View.GONE
        messages.add(Message(query, response))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
        queryInput.text.clear()
        resetButton.visibility = View.VISIBLE
    }

    private fun showApiKeyDialog(model: AIModel) {
        loadingProgressBar.visibility = View.GONE
        sendButton.isEnabled = true
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("${model.name} API Key")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val key = input.text.toString().trim()
                if (key.isNotEmpty()) {
                    aiClient.saveApiKey(this, key, model)
                }
            }
            .show()
    }

    private fun resetSession() {
        messages.clear()
        adapter.notifyDataSetChanged()
        titleText.visibility = View.VISIBLE
        resetButton.visibility = View.GONE
        loadingProgressBar.visibility = View.GONE
        sendButton.isEnabled = true
    }

    inner class MessageAdapter(private val messages: List<Message>) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val queryText: TextView = itemView.findViewById(R.id.queryText)
            val responseText: TextView = itemView.findViewById(R.id.responseText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.queryText.text = message.query
            holder.responseText.text = message.response
        }

        override fun getItemCount() = messages.size
    }
}


