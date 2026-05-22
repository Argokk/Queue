package com.example.queues

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queues.api.ApiFactory
import com.example.queues.dto.EnterpriseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnterprisesViewModel: ViewModel() {
    var enterprises = MutableLiveData<List<EnterpriseDto>>(mutableListOf())
    var nearEnt = MutableLiveData<List<EnterpriseDto>>(mutableListOf())
    val enterprise = MutableLiveData<EnterpriseDto>()

    fun loadEnterprises(){
            viewModelScope.launch {
                try {
                    enterprises.value = ApiFactory.enterpriseApi.getAllEnt()
                    Log.d("my_log",ApiFactory.enterpriseApi.getAllEnt().toString())
                }
                catch(e: Exception){
                    e.printStackTrace()
                }
            }
    }
    fun loadEnterpriseById(id:Long){
        viewModelScope.launch {
            if(id != -1L){
                enterprise.value = ApiFactory.enterpriseApi.getEntById(id).body()
                //Log.d("myyy",enterprise.value.toString())
            }
            else{
                //Log.d("my_log","")
            }
        }
    }
    fun sortEntByLoc(new_location: Location){
        nearEnt.value = enterprises.value?.filter { enterprise ->
            val entLoc = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = enterprise.locationX
                longitude = enterprise.locationY
            }
            new_location.distanceTo(entLoc) <= 50f
        }
        Log.d("my_log",enterprises.value.toString())
    }

}