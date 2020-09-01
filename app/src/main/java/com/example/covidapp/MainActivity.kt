package com.example.covidapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.covidapp.adapter.CountryAdapter
import com.example.covidapp.model.AllNegara
import com.example.covidapp.model.Negara
import com.example.covidapp.network.ApiService
import com.example.covidapp.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private var progressBar: ProgressBar? = null

    private var ascending = true

    companion object {
        lateinit var adapters: CountryAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progress_bar)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapters.filter.filter(newText)
                return false
            }


        })
        swipe_refresh.setOnRefreshListener {
            getcountry()
            swipe_refresh.isRefreshing = false
        }

        getcountry()
        initializeViews()
    }

    private fun initializeViews() {
        sequence.setOnClickListener {
            sequenceWithoutInternet(ascending)
            ascending = !ascending
        }
    }

    private fun sequenceWithoutInternet(asc: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (asc) {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).reverseLayout = true
                Toast.makeText(this@MainActivity, "A-Z", Toast.LENGTH_LONG).show()
            }
            adapter = adapters
        }
    }

    private fun getcountry() {
        val api = retrofit.create(ApiService::class.java)
        api.getAllNegara().enqueue(object : Callback<AllNegara> {
            override fun onResponse(call: Call<AllNegara>, response: Response<AllNegara>) {
                if (response.isSuccessful) {
                    val getListDataCorona = response.body()!!.Global
                    val formatter: NumberFormat = DecimalFormat("#,###")
                    txt_confirmed_globe.text =
                        formatter.format(getListDataCorona?.TotalConfirmed?.toDouble())
                    txt_recovered_globe.text =
                        formatter.format(getListDataCorona?.TotalRecovered?.toDouble())
                    deaths_globe.text =
                        formatter.format(getListDataCorona?.TotalDeaths?.toDouble())
                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progressBar?.visibility = View.GONE
                        adapters = CountryAdapter(response.body()!!.Countries as ArrayList<Negara>)
                        { negara -> itemClicked(negara) }

                        adapter = adapters
                    }

                } else {
                    progressBar?.visibility = View.GONE
                    errorLoading(this@MainActivity)
                }
            }

            override fun onFailure(call: Call<AllNegara>, t: Throwable) {
                progressBar?.visibility = View.GONE
                errorLoading(this@MainActivity)
            }

        })
    }


    private fun itemClicked(negara: Negara) {
        val moveWithData = Intent(this, ChartCountryActivity::class.java)
        moveWithData.putExtra(ChartCountryActivity.EXTRA_COUNTRY, negara)
        startActivity(moveWithData)
    }


    private fun errorLoading(context: Context) {
        val builder = AlertDialog.Builder(context)
        with(builder) {
            setTitle("Negara Error")
            setCancelable(false)
            setPositiveButton("REFRESH") { _, _ ->
                super.onRestart()
                val refresh = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(refresh)
                finish()
            }

            setNegativeButton("EXIT") { _, _ ->
                finish()
            }
            create()
            show()
            show()
        }
    }

}