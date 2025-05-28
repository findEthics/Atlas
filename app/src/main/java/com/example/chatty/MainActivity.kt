package com.example.chatty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var queryInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var resetFab: FloatingActionButton
    private lateinit var titleText: TextView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var perplexityClient: PerplexityClient

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        perplexityClient = PerplexityClient()
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

    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.messagesRecycler)
        queryInput = findViewById(R.id.queryInput)
        sendButton = findViewById(R.id.sendButton)
        resetFab = findViewById(R.id.resetFab)
        titleText = findViewById(R.id.titleText)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
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

        resetFab.setOnClickListener {
            resetSession()
        }

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_api_key -> {
                    showApiKeyDialog("")
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun handleQuery(query: String) {
        val apiKey = perplexityClient.loadApiKey(this)
        if (apiKey.isEmpty()) {
            showApiKeyDialog(query)
        } else {
            sendQueryToApi(query, apiKey)
        }
    }

    private fun sendQueryToApi(query: String, apiKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = perplexityClient.query(query, apiKey)
                withContext(Dispatchers.Main) {
                    updateUIWithResponse(query, response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("sendQueryToApi ${e.message}")
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUIWithResponse(query: String, response: String) {
        messages.add(Message(query, response))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
        queryInput.text.clear()
        titleText.visibility = View.GONE
        resetFab.visibility = View.VISIBLE
    }

    private fun showApiKeyDialog(pendingQuery: String) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("API Key Required")
            .setMessage("Enter your Perplexity API key:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val apiKey = input.text.toString().trim()
                if (apiKey.isNotEmpty()) {
                    perplexityClient.saveApiKey(this, apiKey)
                    Toast.makeText(this, "API Key Saved", Toast.LENGTH_SHORT).show()
                    if(pendingQuery.isNotEmpty()) {
                        sendQueryToApi(pendingQuery, apiKey)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetSession() {
        messages.clear()
        adapter.notifyDataSetChanged()
        titleText.visibility = View.VISIBLE
        resetFab.visibility = View.GONE
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


