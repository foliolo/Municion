package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_item_property.view.*

class PropertyAdapter : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_property, parent, false) as ViewGroup

        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PropertyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {

        fun bindTo(item: Property) {
            itemView.item_property_nickname.text = item.nickname
            itemView.item_property_num_id.text = item.numId
            itemView.item_property_brand.text = item.brand
            itemView.item_property_model.text = item.model
            itemView.item_property_bore.text = item.bore1
            if (item.image.isNullOrBlank()) {
                itemView.item_property_image.visibility = View.GONE
            } else {
                itemView.item_property_image.visibility = View.VISIBLE
                // itemView.item_property_image.setImageResource(item.image)
            }
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Property>() {
            override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
                return oldItem == newItem
            }
        }
    }
}
