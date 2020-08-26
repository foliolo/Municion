package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.ui.licencias.types.LicenseType
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.form_licencia.*
import javax.inject.Inject

/**
 * Created by Alberto on 24/05/2016.
 */
class LicenseFormFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LicenseFormViewModel by viewModels {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.licenseType.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), "Type: ${it.name}", Toast.LENGTH_LONG).show()
            setVisibilityFields(it)
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        form_tipo_licencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.changeLicenseType(LicenseType.valueOf(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.changeLicenseType(LicenseType.A_profesionales)
            }
        }
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
    private fun setVisibilityFields(licenseType: LicenseType) {

        when (licenseType) {
            LicenseType.A_profesionales -> {
                visibilityDateExpiry(true)
                visibilityWeapons()
            }
            LicenseType.B_defensa -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.C_viginlantes_seguridad -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.D_caza_mayor -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.E_escopeta -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.F_tiro_olimpico -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.AE_autorizacion_especial -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.AER_autorizacion_replica -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.Libro_coleccionista -> {
                visibilityDateExpiry(false)
                visibilityWeapons()
            }
            LicenseType.Autonomica_caza -> {
                visibilityDateExpiry(false)
                visibilityAutonomicHunterFishing()
            }
            LicenseType.Autonomica_pesca -> {
                visibilityDateExpiry(false)
                visibilityAutonomicHunterFishing()
            }
            LicenseType.Federativa_tiro -> {
                visibilityDateExpiry(false)
                visibilityFederateShooter()
            }
            LicenseType.Permiso_conducir -> {
                visibilityDateExpiry(false)
                visibilityDrivingLicense()
            }
        }
    }

    private fun visibilityDateExpiry(visible: Boolean) {
        if (visible) {
            layout_form_fecha_caducidad.visibility = View.GONE
            layout_escala.visibility = View.VISIBLE
        } else {
            layout_form_fecha_caducidad.visibility = View.VISIBLE
            layout_escala.visibility = View.GONE
        }
    }

    private fun visibilityWeapons() {
        text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
        // (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
        //     0,
        //     0,
        //     0,
        //     0
        // )
        layout_form_num_abonado.visibility = View.GONE
        layout_form_num_poliza.visibility = View.GONE
        layout_ccaa.visibility = View.GONE
        layout_permiso_conducir.visibility = View.GONE
        text_input_layout_edad.visibility = View.GONE
        layout_categoria.visibility = View.GONE
    }

    private fun visibilityAutonomicHunterFishing() {
        text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
        // (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
        //     0,
        //     0,
        //     0,
        //     0
        // )
        layout_form_num_abonado.visibility = View.VISIBLE
        layout_form_num_poliza.visibility = View.VISIBLE
        layout_ccaa.visibility = View.VISIBLE
        layout_permiso_conducir.visibility = View.GONE
        text_input_layout_edad.visibility = View.GONE
        layout_categoria.visibility = View.GONE
    }

    private fun visibilityFederateShooter() {
        text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_licencia)
        // (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
        //     0,
        //     10,
        //     0,
        //     0
        // )
        layout_form_num_abonado.visibility = View.VISIBLE
        layout_form_num_poliza.visibility = View.GONE
        layout_ccaa.visibility = View.VISIBLE
        layout_permiso_conducir.visibility = View.GONE
        text_input_layout_edad.visibility = View.GONE
        layout_categoria.visibility = View.VISIBLE
    }

    private fun visibilityDrivingLicense() {
        text_input_layout_licencia.hint = resources.getString(R.string.lbl_num_dni)
        // (text_input_layout_licencia.layoutParams as LinearLayout.LayoutParams).setMargins(
        //     0,
        //     10,
        //     0,
        //     0
        // )
        layout_form_num_abonado.visibility = View.GONE
        layout_form_num_poliza.visibility = View.GONE
        layout_ccaa.visibility = View.GONE
        layout_permiso_conducir.visibility = View.VISIBLE
        text_input_layout_edad.visibility = View.VISIBLE
        layout_categoria.visibility = View.GONE
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