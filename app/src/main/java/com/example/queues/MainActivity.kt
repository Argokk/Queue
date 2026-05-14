package com.example.queues

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewParentCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Dao
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.queues.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val REQ_NOT_CODE = 1000
    private val REQ_LOC_CODE = 1001
    lateinit var db: DataBase
    lateinit var dao: EnterpriseDao
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window,true)

        db = Room.databaseBuilder(applicationContext, DataBase::class.java,"db").fallbackToDestructiveMigration().build()
        dao = db.getEnterpriseDao()

        notifyPermissoin(this)
        locationPermission()
        binding.pager.adapter = ViewPager(this)

        TabLayoutMediator(binding.tab,binding.pager){
            tab,position ->when(position){
                0 -> {tab.icon = ContextCompat.getDrawable(this,R.drawable.ic_main)
                    tab.text = "Главная"}
                else -> {tab.icon = ContextCompat.getDrawable(this,R.drawable.ic_settings)
                    tab.text = "Настройки"}
            }
        }.attach()

    }
    fun notifyPermissoin(activity: Activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val granted = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if(!granted){
                ActivityCompat.requestPermissions(activity,arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),REQ_NOT_CODE)
            }
        }
    }
    fun locationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val granted = ActivityCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQ_LOC_CODE
                )
            }
        }
    }
}