package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Purchase
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.adapter_item_purchase.view.*

class PurchaseAdapter(private val listener: PurchaseAdapterListener) :
    ListAdapter<Purchase, PurchaseAdapter.PurchaseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_purchase, parent, false) as ViewGroup

        return PurchaseViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PurchaseViewHolder(private val parent: ViewGroup, private val listener: PurchaseAdapterListener) :
        RecyclerView.ViewHolder(parent) {

        fun bindTo(item: Purchase) {
            itemView.item_purchase_brand.text = item.brand
            itemView.item_purchase_bore.text = item.bore
            itemView.item_purchase_price.text = item.price.toString()
            itemView.item_purchase_date.text = item.date

            Glide.with(parent.context)
                .load(item.image)
                .error(getRandomImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(itemView.item_purchase_image)

            itemView.item_purchase_image.setOnClickListener {
                listener.updateImage(item)
            }
        }

        private fun getRandomImage() = arrayListOf(
            ContextCompat.getDrawable(parent.context, R.drawable.ic_balas),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_balas_rifle),
            ContextCompat.getDrawable(parent.context, R.drawable.ic_cartuchos)
        ).random()?.current
    }

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Purchase>() {
            override fun areItemsTheSame(oldItem: Purchase, newItem: Purchase): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Purchase, newItem: Purchase): Boolean {
                return oldItem.equals(newItem)
            }
        }
    }
}
