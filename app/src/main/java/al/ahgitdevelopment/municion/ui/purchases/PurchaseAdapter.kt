package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Purchase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_item_purchase.view.*

class PurchaseAdapter : ListAdapter<Purchase, PurchaseAdapter.PurchaseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_purchase, parent, false) as ViewGroup

        return PurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PurchaseViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {

        fun bindTo(item: Purchase) {
            itemView.item_purchase_brand.text = item.brand
            itemView.item_purchase_bore.text = item.bore
            itemView.item_purchase_price.text = item.price.toString()
            itemView.item_purchase_date.text = item.date

            if (item.image.isNullOrBlank()) {
                itemView.item_purchase_image.visibility = View.GONE
            } else {
                itemView.item_purchase_image.visibility = View.VISIBLE
                // itemView.item_purchase_image.setImageResource(item.image)
            }
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Purchase>() {
            override fun areItemsTheSame(oldItem: Purchase, newItem: Purchase): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Purchase, newItem: Purchase): Boolean {
                return oldItem == newItem
            }
        }
    }
}
