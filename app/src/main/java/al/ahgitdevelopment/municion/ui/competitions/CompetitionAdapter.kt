package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Competition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CompetitionAdapter() : ListAdapter<Competition, CompetitionAdapter.CompetitionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompetitionViewHolder {
        // val view = LayoutInflater.from(parent.context)
        //     .inflate(R.layout.adapter_item_competition, parent, false) as ViewGroup
        //
        // return CompetitionViewHolder(view)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_competition, parent, false)
        return CompetitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompetitionViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class CompetitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val description: TextView = view.findViewById(R.id.item_competition_description)
        private val date: TextView = view.findViewById(R.id.item_competition_description)
        private val ranking: TextView = view.findViewById(R.id.item_competition_description)
        private val points: TextView = view.findViewById(R.id.item_competition_description)
        private val place: TextView = view.findViewById(R.id.item_competition_description)

        fun bindTo(item: Competition) {
            description.text = item.description
            date.text = item.date
            ranking.text = item.ranking
            points.text = item.points.toString()
            place.text = item.place
        }
    }

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Competition>() {
            override fun areItemsTheSame(oldItem: Competition, newItem: Competition): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Competition, newItem: Competition): Boolean {
                return oldItem == newItem
            }
        }
    }
}
