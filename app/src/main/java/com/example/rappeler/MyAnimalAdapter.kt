package com.example.rappeler

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAnimalAdapter(private var rescues: List<Rescue>) :
RecyclerView.Adapter<MyAnimalAdapter.RescueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RescueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rescue_item, parent, false)
        return RescueViewHolder(view)
    }

    override fun onBindViewHolder(holder: RescueViewHolder, position: Int) {
        val animal = rescues[position]
        holder.species.text = buildString {
            append("Species: ")
            append(animal.species)
        }
        holder.age.text = buildString {
            append("Age: ")
            append(animal.age.toString())
        }
        holder.color.text = buildString {
            append("Color: ")
            append(animal.color)
        }
        holder.gender.text = buildString {
            append("Gender: ")
            append(animal.gender)
        }
        holder.vetRemarks.text = buildString {
            append("Vet Remarks: ")
            append(animal.vetEvaluation)
        }
        holder.status.visibility = View.VISIBLE
        holder.status.text = buildString {
            append("Status: ")
            append(animal.status)
        }
        holder.adopt.visibility = View.GONE

        val decodedBytes = Base64.decode(animal.imageString, Base64.DEFAULT)
        val decodedBitmap = decodedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

        Glide.with(holder.itemView.context)
            .load(decodedBitmap)
            .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image while loading
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return rescues.size
    }

    inner class RescueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val species: TextView = itemView.findViewById(R.id.textSpecies)
        val age: TextView = itemView.findViewById(R.id.textAge)
        val image: ImageView = itemView.findViewById(R.id.imageAnimal)
        val gender: TextView = itemView.findViewById(R.id.textGender)
        val color: TextView = itemView.findViewById(R.id.textColor)
        val vetRemarks: TextView = itemView.findViewById(R.id.vetRemarks)
        val status : TextView = itemView.findViewById(R.id.status)
        val adopt: TextView = itemView.findViewById(R.id.tvAdopt)
    }
    fun updateList(newList: List<Rescue>) {
        rescues = newList
        notifyDataSetChanged()
    }
}