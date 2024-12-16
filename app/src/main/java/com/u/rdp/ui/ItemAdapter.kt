package com.u.rdp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.u.rdp.R
import com.u.rdp.model.Item
import com.u.rdp.util.Constants

class ItemAdapter
    : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    var items = ArrayList<Item>()

    //var selectedItem: String? = null // Variable que quieres acceder

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){ // V. 91 min 10
        //
        val tvBussines: TextView = itemView.findViewById(R.id.tvBussines)
        val tvItem: TextView = itemView.findViewById(R.id.tvItem )
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvPriceSale: TextView = itemView.findViewById(R.id.tvPriceSale)
        // val tvDistance: TextView = itemView.findViewById(R.id.tvDistance);


        var ivBasicImage = itemView.findViewById(R.id.ivImage) as ImageView
        var img = ""
        //var url = "http://192.168.100.14:8000/image/"
        var url = Constants.API_BASE_URL_IMG
        // public/images
        fun bind(item: Item) = with(itemView){
            tvBussines.text = "Item: ${item.name}"
            tvItem.text = "Regular price: ${item.price}"
            tvPrice.text = "Discount: ${item.discount}%"
            tvPriceSale.text =  "Price sale: ${item.sale}"
            img = "${item.image}"
            val cadena = "${url}${img}"
            // Picasso.get().load("http://i.imgur.com/DvpvklR.png").into(item.image)
            val bitmap = Picasso.get().load(cadena).into(ivBasicImage);
            //selectedItem = "Distance: ${item.distance}"
            // btn_share.visibility = View.VISIBLE  // show the buton share        28 nov 2024
        }

    }

    //inflates xml items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(  // video 93, min 5
            LayoutInflater.from(parent.context).inflate(R.layout.item_item, parent, false)
        )
    }


    // Binds data
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]    // video 93, min 2:30
        holder.bind(item);
    }

    // numero of elements
    override fun getItemCount() = items.size   // appointments.size

}