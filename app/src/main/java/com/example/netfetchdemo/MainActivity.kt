package com.example.netfetchdemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var btnFetchData: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        btnFetchData = findViewById(R.id.btnFetchData)
        progressBar = findViewById(R.id.progressBar)
        tvStatus     = findViewById(R.id.tvStatus)
        recyclerView = findViewById(R.id.recyclerView)

        // Set up RecyclerView
        todoAdapter = TodoAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = todoAdapter

        btnFetchData.setOnClickListener {
            fetchTodos()
        }
    }

    private fun fetchTodos() {
        // ── Step 1: Check internet connectivity BEFORE making a request ──
        if (!isNetworkAvailable()) {
            showStatus(
                "⚠ No Internet Connection",
                "Please check your Wi-Fi or mobile data and try again.",
                isError = true
            )
            return
        }

        // ── Step 2: Show loading state ──
        showLoading(true)
        clearStatus()
        todoAdapter.updateList(emptyList())

        // ── Step 3: Launch coroutine on IO thread (never block the main UI thread) ──
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todos = RetrofitClient.instance.getTodos()

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    // ── Step 4: Handle empty data case ──
                    if (todos.isEmpty()) {
                        showStatus(
                            "○ No Data Found",
                            "The server returned an empty list. Nothing to display.",
                            isError = false,
                            isWarning = true
                        )
                    } else {
                        showStatus(
                            "✓ Success",
                            "Fetched ${todos.size} todos from the API.",
                            isError = false
                        )
                        todoAdapter.updateList(todos)
                    }
                }

            } catch (e: UnknownHostException) {
                // ── Exception 1: DNS failure / device lost internet mid-request ──
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showStatus(
                        "⚠ Connection Failed",
                        "Unable to resolve host. Check your internet connection.\n${e.message}",
                        isError = true
                    )
                }

            } catch (e: HttpException) {
                // ── Exception 2: Server responded with non-2xx HTTP status ──
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showStatus(
                        "⚠ Server Error ${e.code()}",
                        "The server returned an error: ${e.message()}",
                        isError = true
                    )
                }

            } catch (e: IOException) {
                // ── Exception 3: General network I/O failure (timeout, reset, etc.) ──
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showStatus(
                        "⚠ Network Error",
                        "A network error occurred. Please try again.\n${e.message}",
                        isError = true
                    )
                }

            } catch (e: Exception) {
                // ── Exception 4: Catch-all for any unexpected error ──
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showStatus(
                        "⚠ Unexpected Error",
                        "Something went wrong: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }

    /**
     * Checks whether the device has an active internet connection.
     * Uses ConnectivityManager + NetworkCapabilities (API 23+).
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnFetchData.isEnabled  = !isLoading
        btnFetchData.text       = if (isLoading) "Fetching..." else "Fetch Data"
    }

    private fun showStatus(title: String, message: String, isError: Boolean, isWarning: Boolean = false) {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text       = "$title\n$message"
        tvStatus.setBackgroundResource(
            when {
                isError   -> R.drawable.bg_status_error
                isWarning -> R.drawable.bg_status_warning
                else      -> R.drawable.bg_status_success
            }
        )
    }

    private fun clearStatus() {
        tvStatus.visibility = View.GONE
    }
}
