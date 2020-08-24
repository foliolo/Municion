package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.di.AppComponent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.form_licencia.*
import javax.inject.Inject

/**
 * Created by Alberto on 24/05/2016.
 */
class LicenciaFormFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // Creo este flag para comprabar en el Calendario si es un guardado o modificacion de licencia
    private var isModify = false
    // private var prefs: SharedPreferences
    // private var mAdView: AdView

    private val viewModel: LicenciasFormViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AppComponent.create(requireContext()).inject(this)
    }

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.form_licencia, container, false)
    }

    private fun callDatePickerFragment() {
        // DatePickerFragment().show(parentFragmentManager, "datePicker")
    }

    fun fabSaveOnClick(view: View?) {
    }

    /**
     * Método para modificar la visibilidad de los campos en función del tipo de licencia seleccionado
     *
     * @param tipoLicencia Licencia seleccionada
     */
    private fun setVisibilityFields(tipoLicencia: Int) {
        //La licencia A no tiene fecha de caducidad
        if (tipoLicencia == 0) {
            layout_form_fecha_caducidad.visibility = View.GONE
            layout_escala.visibility = View.VISIBLE
        } else {
            layout_form_fecha_caducidad.visibility = View.VISIBLE
            layout_escala.visibility = View.GONE
        }
        when (tipoLicencia) {
            0, 1, 2, 3, 4, 5, 6, 7, 8 -> {
                text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
                (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    0,
                    0,
                    0
                )
                layout_form_num_abonado.visibility = View.GONE
                layout_form_num_poliza.visibility = View.GONE
                layout_ccaa.visibility = View.GONE
                layout_permiso_conducir.visibility = View.GONE
                text_input_layout_edad.visibility = View.GONE
                layout_categoria.visibility = View.GONE
            }
            9, 10 -> {
                text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
                (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    0,
                    0,
                    0
                )
                layout_form_num_abonado.visibility = View.VISIBLE
                layout_form_num_poliza.visibility = View.VISIBLE
                layout_ccaa.visibility = View.VISIBLE
                layout_permiso_conducir.visibility = View.GONE
                text_input_layout_edad.visibility = View.GONE
                layout_categoria.visibility = View.GONE
            }
            11 -> {
                text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
                (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    10,
                    0,
                    0
                )
                layout_form_num_abonado.visibility = View.VISIBLE
                layout_form_num_poliza.visibility = View.GONE
                layout_ccaa.visibility = View.VISIBLE
                layout_permiso_conducir.visibility = View.GONE
                text_input_layout_edad.visibility = View.GONE
                layout_categoria.visibility = View.VISIBLE
            }
            12 -> {
                text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_dni)
                (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    10,
                    0,
                    0
                )
                layout_form_num_abonado.visibility = View.GONE
                layout_form_num_poliza.visibility = View.GONE
                layout_ccaa.visibility = View.GONE
                layout_permiso_conducir.visibility = View.VISIBLE
                text_input_layout_edad.visibility = View.VISIBLE
                layout_categoria.visibility = View.GONE
            }
        }
    }

    /**
     * DatePickerFragment para seleccionar la fecha de expedicion
     */
    // class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    //     override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    //         var year = 0
    //         var month = 0
    //         var day = 0
    //         val c = Calendar.getInstance()
    //         if (layout_form_fecha_expedicion.editText?.text.toString() == "") {
    //             // Use the current date as the default date in the picker
    //             year = c[Calendar.YEAR]
    //             month = c[Calendar.MONTH]
    //             day = c[Calendar.DAY_OF_MONTH]
    //         } else {
    //             try {
    //                 c.time = SimpleDateFormat("dd/MM/yyyy").parse(
    //                     layout_form_fecha_caducidad.editText?.text.toString()
    //                 )
    //                 year = c[Calendar.YEAR]
    //                 month = c[Calendar.MONTH]
    //                 day = c[Calendar.DAY_OF_MONTH]
    //             } catch (e: ParseException) {
    //                 e.printStackTrace()
    //             }
    //         }
    //         // Create a new instance of DatePickerDialog and return it
    //         return DatePickerDialog(activity, this, year, month, day)
    //     }
    //
    //     override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
    //         // Do something with the date chosen by the user
    //         val cal = Calendar.getInstance()
    //         cal[Calendar.YEAR] = year
    //         cal[Calendar.MONTH] = month
    //         cal[Calendar.DAY_OF_MONTH] = day
    //         val date = cal.time
    //         val formatter: String =  DateTimeFormatter.ofPattern("dd/MM/yyyy")
    //         layout_form_fecha_caducidad.editText?.setText(fecha)
    //     }
    // }
}