package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.License
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.licencia_item.view.*

/**
 * Created by Alberto on 28/05/2016.
 */
class LicenseAdapter : ListAdapter<License, LicenseAdapter.LicenseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.licencia_item, parent, false) as ViewGroup

        return LicenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class LicenseViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {

        fun bindTo(item: License) {
            itemView.item_license_number.text = item.licenseNumber
            itemView.item_license_issue_date.text = item.issueDate
            itemView.item_license_expiry_date.text = item.expiryDate
            itemView.item_license_insurance_number.text = item.insuranceNumber
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<License>() {
            override fun areItemsTheSame(oldItem: License, newItem: License): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: License, newItem: License): Boolean {
                return oldItem.equals(newItem)
            }
        }
    }
}