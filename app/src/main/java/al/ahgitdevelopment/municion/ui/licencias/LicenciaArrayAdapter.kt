package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.License
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

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
        private val licenseNumber: TextView
        private val issueDate: TextView
        private val expiryDate: TextView
        private val insuranceNumber: TextView

        init {
            LayoutInflater.from(parent.context).inflate(R.layout.licencia_item, parent, false)
            licenseNumber = itemView.findViewById(R.id.item_license_number)
            issueDate = itemView.findViewById(R.id.item_license_issue_date)
            expiryDate = itemView.findViewById(R.id.item_license_expiry_date)
            insuranceNumber = itemView.findViewById(R.id.item_license_insurance_number)
        }

        fun bindTo(item: License) {
            licenseNumber.text = item.licenseNumber
            issueDate.text = item.issueDate
            expiryDate.text = item.expiryDate
            insuranceNumber.text = item.insuranceNumber
        }
    }

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