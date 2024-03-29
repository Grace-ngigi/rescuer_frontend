package com.example.rappeler

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdoptAdapter(private val rescues: List<Rescue>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AdoptAdapter.RescueViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(rescue: Rescue)
    }
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

        val decodedBytes = Base64.decode(animal.imageString, Base64.DEFAULT)
        val decodedBitmap = decodedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

        Glide.with(holder.itemView.context)
            .load(decodedBitmap)
            .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image while loading
//            .error(R.drawable.error_image) // Error image if loading fails
            .into(holder.image)

        holder.adopt.setOnClickListener {
            listener.onItemClick(animal)
        }
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
        val adopt: TextView = itemView.findViewById(R.id.tvAdopt)
        val vetRemarks: TextView = itemView.findViewById(R.id.vetRemarks)
    }
}
