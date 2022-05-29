package al.ahgitdevelopment.municion.ui.licenses

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.AdapterItemLicenseBinding
import al.ahgitdevelopment.municion.datamodel.License
import android.view.LayoutInflater
import android.view.View
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
        val view = AdapterItemLicenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LicenseViewHolder(view.root)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class LicenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_license_name)
        private val number: TextView = view.findViewById(R.id.item_license_number)
        private val issueDate: TextView = view.findViewById(R.id.item_license_issue_date)
        private val expiryDate: TextView = view.findViewById(R.id.item_license_expiry_date)
        private val insuranceNumber: TextView = view.findViewById(R.id.item_license_insurance_number)

        fun bindTo(item: License) {
            name.text = item.licenseName
            number.text = item.licenseNumber
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
                return oldItem == newItem
            }
        }
    }
}
