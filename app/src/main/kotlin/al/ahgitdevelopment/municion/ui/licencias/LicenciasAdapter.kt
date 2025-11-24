package al.ahgitdevelopment.municion.ui.licencias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.databinding.ItemLicenciaBinding

/**
 * RecyclerView Adapter para Licencias con DiffUtil
 *
 * FASE 4: Migración UI a Kotlin
 * - ListAdapter con DiffUtil para eficiencia
 * - ViewBinding en ViewHolder
 * - Click listeners para editar y calendario
 * - Indicador visual de caducidad
 *
 * Reemplaza LicenciaArrayAdapter y LicenciaCursorAdapter
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class LicenciasAdapter(
    private val onItemClick: (Licencia) -> Unit,
    private val onCalendarClick: (Licencia) -> Unit
) : ListAdapter<Licencia, LicenciasAdapter.LicenciaViewHolder>(LicenciaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenciaViewHolder {
        val binding = ItemLicenciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LicenciaViewHolder(binding, onItemClick, onCalendarClick)
    }

    override fun onBindViewHolder(holder: LicenciaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LicenciaViewHolder(
        private val binding: ItemLicenciaBinding,
        private val onItemClick: (Licencia) -> Unit,
        private val onCalendarClick: (Licencia) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(licencia: Licencia) {
            binding.apply {
                // Número de licencia
                textNumLicencia.text = licencia.numLicencia

//                // Tipo de arma
//                textTipoArma.text = licencia.tipoArma

                // Fecha de caducidad
                textFechaCaducidad.text = root.context.getString(
                    R.string.licencia_caduca,
                    licencia.fechaCaducidad
                )

                // Días hasta caducidad con indicador de color
                val diasHastaCaducidad = licencia.diasHastaCaducidad()
                when {
                    licencia.estaCaducada() -> {
                        textDiasCaducidad.text = "¡CADUCADA!"
                        textDiasCaducidad.setTextColor(
                            ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                        )
                        cardView.strokeColor = ContextCompat.getColor(
                            root.context,
                            android.R.color.holo_red_dark
                        )
                        cardView.strokeWidth = 4
                    }
                    licencia.caducaProxima() -> {
                        textDiasCaducidad.text = "Caduca en $diasHastaCaducidad días"
                        textDiasCaducidad.setTextColor(
                            ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                        )
                        cardView.strokeColor = ContextCompat.getColor(
                            root.context,
                            android.R.color.holo_orange_dark
                        )
                        cardView.strokeWidth = 2
                    }
                    else -> {
                        textDiasCaducidad.text = "Válida ($diasHastaCaducidad días)"
                        textDiasCaducidad.setTextColor(
                            ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                        )
                        cardView.strokeColor = ContextCompat.getColor(
                            root.context,
                            android.R.color.transparent
                        )
                        cardView.strokeWidth = 0
                    }
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClick(licencia)
                }

                buttonCalendar.setOnClickListener {
                    onCalendarClick(licencia)
                }
            }
        }
    }

    private class LicenciaDiffCallback : DiffUtil.ItemCallback<Licencia>() {
        override fun areItemsTheSame(oldItem: Licencia, newItem: Licencia): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Licencia, newItem: Licencia): Boolean {
            return oldItem == newItem
        }
    }
}
