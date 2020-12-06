package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Property
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_item_property.view.*

class PropertyAdapter(private val listener: PropertyAdapterListener) :
    ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_property, parent, false) as ViewGroup

        return PropertyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PropertyViewHolder(private val parent: ViewGroup, private val listener: PropertyAdapterListener) :
        RecyclerView.ViewHolder(parent) {

        fun bindTo(item: Property) {
            itemView.item_property_nickname.text = item.nickname
            itemView.item_property_num_id.text = item.numId
            itemView.item_property_brand.text = item.brand
            itemView.item_property_model.text = item.model
            itemView.item_property_bore.text = item.bore1

            Glide.with(parent.context)
                .load(item.image)
                .error(getRandomImage())
                .into(itemView.item_property_image)

            itemView.item_property_image.setOnClickListener {
                listener.updateImage(item)
            }
        }

        private fun getRandomImage() = arrayListOf(
            ContextCompat.getDrawable(parent.context, R.drawable.ic_avancarga),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_pistola),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_revolver),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_rifle),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_shotgun)
        ).random()?.current
    }

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Property>() {
            override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
                return oldItem.equals(newItem)
            }
        }
    }
}
