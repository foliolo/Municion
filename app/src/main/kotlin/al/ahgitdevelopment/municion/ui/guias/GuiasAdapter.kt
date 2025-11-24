package al.ahgitdevelopment.municion.ui.guias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.databinding.ItemGuiaBinding

/**
 * RecyclerView Adapter para Guías con DiffUtil
 *
 * FASE 4: Migración UI a Kotlin
 * - ListAdapter con DiffUtil para eficiencia
 * - ViewBinding en ViewHolder
 * - Click listeners modernos
 *
 * Reemplaza GuiaArrayAdapter y GuiaCursorAdapter
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class GuiasAdapter(
    private val onItemClick: (Guia) -> Unit
) : ListAdapter<Guia, GuiasAdapter.GuiaViewHolder>(GuiaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuiaViewHolder {
        val binding = ItemGuiaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GuiaViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GuiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GuiaViewHolder(
        private val binding: ItemGuiaBinding,
        private val onItemClick: (Guia) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(guia: Guia) {
            binding.apply {
                // Número de guía
                textNumGuia.text = guia.numGuia

                // Cupo disponible
                val disponible = guia.disponible()
                textCupo.text = root.context.getString(
                    R.string.guia_cupo_format,
                    disponible,
                    guia.cupo
                )

                // Porcentaje usado
                val porcentaje = guia.porcentajeUsado()
                progressBar.progress = porcentaje.toInt()

                // Color según disponibilidad
                when {
                    guia.cupoAgotado() -> {
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_red_dark)
                        )
                    }
                    disponible < guia.cupo * 0.2 -> {
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_orange_dark)
                        )
                    }
                    else -> {
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_green_dark)
                        )
                    }
                }

//                // Fecha validez
//                textFechaValidez.text = root.context.getString(
//                    R.string.guia_fecha_validez,
//                    guia.fechaValidez
//                )

                // Click listener
                root.setOnClickListener {
                    onItemClick(guia)
                }
            }
        }
    }

    private class GuiaDiffCallback : DiffUtil.ItemCallback<Guia>() {
        override fun areItemsTheSame(oldItem: Guia, newItem: Guia): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Guia, newItem: Guia): Boolean {
            return oldItem == newItem
        }
    }
}
