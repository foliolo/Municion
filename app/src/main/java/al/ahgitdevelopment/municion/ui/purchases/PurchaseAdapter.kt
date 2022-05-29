package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.AdapterItemPurchaseBinding
import al.ahgitdevelopment.municion.datamodel.Purchase
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PurchaseAdapter(private val listener: PurchaseAdapterListener) :
    ListAdapter<Purchase, PurchaseAdapter.PurchaseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = AdapterItemPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PurchaseViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class PurchaseViewHolder(
        private val binding: AdapterItemPurchaseBinding,
        private val listener: PurchaseAdapterListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: Purchase) {
            binding.itemPurchaseBrand.text = item.brand
            binding.itemPurchaseBore.text = item.bore
            binding.itemPurchasePrice.text = item.price.toString()
            binding.itemPurchaseDate.text = item.date

            Glide.with(binding.root.context)
                .load(item.image)
                .error(getRandomImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.itemPurchaseImage)

            binding.itemPurchaseImage.setOnClickListener {
                listener.updateImage(item)
            }
        }

        private fun getRandomImage() = arrayListOf(
            ContextCompat.getDrawable(binding.root.context, R.drawable.ic_balas),
            ContextCompat.getDrawable(binding.root.context, R.drawable.ic_balas_rifle),
            ContextCompat.getDrawable(binding.root.context, R.drawable.ic_cartuchos)
        ).random()?.current
    }

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
