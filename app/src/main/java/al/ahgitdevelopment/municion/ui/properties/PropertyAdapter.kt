package al.ahgitdevelopment.municion.ui.properties

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PropertyAdapter(private val listener: PropertyAdapterListener) :
    ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_property, parent, false)
        return PropertyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PropertyViewHolder(private val view: View, private val listener: PropertyAdapterListener) :
        RecyclerView.ViewHolder(view) {

        private val nickname = view.findViewById<TextView>(R.id.item_property_nickname)
        private val image = view.findViewById<ImageView>(R.id.item_property_image)
        private val numId = view.findViewById<TextView>(R.id.item_property_num_id)
        private val brand = view.findViewById<TextView>(R.id.item_property_brand)
        private val model = view.findViewById<TextView>(R.id.item_property_model)
        private val bore = view.findViewById<TextView>(R.id.item_property_bore)

        fun bindTo(item: Property) {
            nickname.text = item.nickname
            numId.text = item.numId
            brand.text = item.brand
            model.text = item.model
            bore.text = item.bore1

            Glide.with(itemView.context)
                .load(item.image)
                .error(getRandomImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(image)

            image.setOnClickListener {
                listener.updateImage(item)
            }
        }

        private fun getRandomImage() = arrayListOf(
            ContextCompat.getDrawable(view.context, R.drawable.ic_avancarga),
            ContextCompat.getDrawable(view.context, R.drawable.ic_pistola),
            ContextCompat.getDrawable(view.context, R.drawable.ic_revolver),
            ContextCompat.getDrawable(view.context, R.drawable.ic_rifle),
            ContextCompat.getDrawable(view.context, R.drawable.ic_shotgun),
        ).random()?.current
    }

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
