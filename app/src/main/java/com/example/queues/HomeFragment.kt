package com.example.queues

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queues.api.ApiFactory
import com.example.queues.api.EnterpriseApi
import com.example.queues.databinding.FragmentHomeBinding
import com.example.queues.dto.EnterpriseDto
import com.example.queues.viewmodel.EnterprisesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Queue

class HomeFragment : Fragment(), LocationListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var lm: LocationManager
    var enterprises = listOf<EnterpriseDto>()
    private val viewmodel: EnterprisesViewModel by viewModels()
    lateinit var adapter: EnterPrisesRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        viewmodel.loadEnterprises()
        viewmodel.nearEnt.observe(viewLifecycleOwner){
            enterprises = it
            adapter.updateRv(enterprises)
        }

        adapter = EnterPrisesRvAdapter(enterprises)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)

        onLocationSetup()
        binding.searchEditText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val filtered = enterprises.filter { ent ->
                        ent.name.contains(it, ignoreCase = true)
                    }
                    adapter.updateRv(filtered)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = enterprises.filter { ent ->
                    newText?.let { it1 -> ent.name.contains(it1, ignoreCase = true) } ?: true
                }
                adapter.updateRv(filtered)
                return true
            }
        })
        return binding.root
    }
    fun onLocationSetup(){
        lm = requireActivity().getSystemService(LocationManager::class.java)
        val granted = ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                5f,
                this
            )
        }
    }
    override fun onLocationChanged(location: Location) {
        viewmodel.sortEntByLoc(location)
    }
}