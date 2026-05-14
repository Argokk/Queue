package com.example.queues

import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.queues.databinding.ItemRvLayoutBinding
import com.example.queues.dto.EnterpriseDto

class EnterPrisesRvAdapter(var enterprises: List<EnterpriseDto>, var location: Location = Location("")): RecyclerView.Adapter<EnterPrisesRvAdapter.EnterPrisesRvAdapterViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EnterPrisesRvAdapterViewHolder {
        val binding = ItemRvLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EnterPrisesRvAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: EnterPrisesRvAdapterViewHolder,
        position: Int
    ) {
        holder.bind(enterprises[position])
    }

    override fun getItemCount() = enterprises.size
    inner class EnterPrisesRvAdapterViewHolder(val binding: ItemRvLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(enterprise: EnterpriseDto){
            Glide.with(binding.root.context).load(enterprise.url).into(binding.EntImage)
            binding.data = enterprise
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, QueueActivity::class.java).putExtra("ent_id",enterprise.id.toLong())
                binding.root.context.startActivity(intent)
            }
        }
    }
    fun updateRv(ent: List<EnterpriseDto>){
        enterprises = ent
        notifyDataSetChanged()
    }
}