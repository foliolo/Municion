package al.ahgitdevelopment.municion.forms;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.datamodel.Compra;
import al.ahgitdevelopment.municion.datamodel.Guia;

/**
 * Created by ahidalgog on 11/04/2016.
 */
public class CompraFormActivity extends AppCompatActivity {
    private static TextInputLayout layoutFecha;
    private CheckBox checkSegundoCalibre;
    private RatingBar valoracion;
    //    private ImageView imagen;
    // Mensaje de error antes de guardar
    private TextView mensajeError;
    private String imagePath;
    private int posicionGuia;
    private Toolbar toolbar;
    private TextInputLayout layoutCalibre1;
    private TextInputLayout layoutCalibre2;
    private TextInputLayout layoutUnidades;
    private TextInputLayout layoutPrecio;
    private TextInputLayout layoutTipoMunicion;
    private TextInputLayout layoutPesoMunicion;
    private TextInputLayout layoutMarcaMunicion;
    private TextInputLayout layoutTienda;

    // Campos para validación de cupo
    private int cupoDisponible = Integer.MAX_VALUE;
    private int cupoTotal = 0;
    private TextView cupoInfoText;

    private SharedPreferences prefs;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_compra);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_nueva_compra);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        checkSegundoCalibre = findViewById(R.id.form_check_segundo_calibre);
        valoracion = findViewById(R.id.form_ratingBar_valoracion);
//        imagen = (ImageView) findViewById(R.id.imagen);
        mensajeError = findViewById(R.id.form_mensaje_compra);
        imagePath = null;
        layoutFecha = findViewById(R.id.layout_form_fecha_compra);
        layoutCalibre1 = findViewById(R.id.text_input_layout_calibre1);
        layoutCalibre2 = findViewById(R.id.text_input_layout_calibre2);
        layoutUnidades = findViewById(R.id.text_input_layout_unidades);
        layoutPrecio = findViewById(R.id.text_input_layout_precio);
        layoutTipoMunicion = findViewById(R.id.text_input_layout_tipo_municion);
        layoutPesoMunicion = findViewById(R.id.text_input_layout_peso_municion);
        layoutMarcaMunicion = findViewById(R.id.text_input_layout_marca_municion);
        layoutTienda = findViewById(R.id.text_input_layout_tienda);

        if (getIntent().getExtras() != null) {
            //Carga de datos (en caso de modificacion)
            if (getIntent().getExtras().get("position_guia") == null) {
                try {
                    Compra compra = getIntent().getExtras().getParcelable("modify_compra");
                    // Para la fecha de compra
                    layoutCalibre1.getEditText().setText(compra.getCalibre1());
                    if (compra.getCalibre2() != null && !"null".equals(compra.getCalibre2())) {
                        if (!"".equals(compra.getCalibre2())) {
                            checkSegundoCalibre.setChecked(true);
                            layoutCalibre2.setVisibility(View.VISIBLE);
                            layoutCalibre2.getEditText().setText(compra.getCalibre2());
                        } else {
                            checkSegundoCalibre.setChecked(false);
                            layoutCalibre2.setVisibility(View.GONE);
                            layoutCalibre2.getEditText().setText("");
                        }
                    }
                    layoutUnidades.getEditText().setText(String.valueOf(compra.getUnidades()));
                    layoutPrecio.getEditText().setText(compra.getPrecio() + "€");
                    layoutFecha.getEditText().setText(compra.getFecha());
                    layoutTipoMunicion.getEditText().setText(compra.getTipo());
                    layoutPesoMunicion.getEditText().setText(String.valueOf(compra.getPeso()));
                    layoutMarcaMunicion.getEditText().setText(compra.getMarca());
                    layoutTienda.getEditText().setText(compra.getTienda());
                    valoracion.setRating(compra.getValoracion());
//                imagen.setImageBitmap(BitmapFactory.decodeFile(compra.getImagePath()));
                    imagePath = compra.getImagePath();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            //Carga de datos de la guia seleccionada
            else {
                posicionGuia = getIntent().getExtras().getInt("position_guia", -1);
                if (posicionGuia != -1) {
                    Guia guia = getIntent().getExtras().getParcelable("guia");
                    layoutCalibre1.getEditText().setText(guia.getCalibre1());
                    if (guia.getCalibre2() != null && !guia.getCalibre2().equals("") && !"null".equals(guia.getCalibre2())) {
                        checkSegundoCalibre.setChecked(true);
                        layoutCalibre2.setVisibility(View.VISIBLE);
                        layoutCalibre2.getEditText().setText(guia.getCalibre2());
                    } else {
                        checkSegundoCalibre.setChecked(false);
                        layoutCalibre2.setVisibility(View.GONE);
                        layoutCalibre2.getEditText().setText("");
                    }
                }
            }

            // Leer información de cupo para validación client-side
            cupoDisponible = getIntent().getExtras().getInt("cupo_disponible", Integer.MAX_VALUE);
            cupoTotal = getIntent().getExtras().getInt("cupo_total", 0);
        }

        // Inicializar y mostrar información de cupo disponible
        cupoInfoText = findViewById(R.id.form_cupo_info);
        if (cupoDisponible < Integer.MAX_VALUE && cupoInfoText != null) {
            cupoInfoText.setVisibility(View.VISIBLE);
            cupoInfoText.setText("Cupo disponible: " + cupoDisponible + " / " + cupoTotal + " unidades");
        }

        checkSegundoCalibre.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutCalibre2.setVisibility(View.VISIBLE);
            } else {
                layoutCalibre2.setVisibility(View.GONE);
                layoutCalibre2.getEditText().setText("");
            }
        });

        layoutFecha.setOnClickListener(v -> callDatePickerFragment());

        layoutFecha.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });

        // Evento que saca el calendario al recibir el foco en el campo fecha
        layoutFecha.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    callDatePickerFragment();
            }
        });

        layoutPesoMunicion.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && layoutPesoMunicion.getEditText().getText().toString().equals("0"))
                layoutPesoMunicion.getEditText().setText("");
        });

        // Validaciones de campos obligatorios antes de guardar
        // Calibre
        layoutCalibre1.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutCalibre1.getEditText().getText().toString().length() < 1) {
                    layoutCalibre1.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Unidades - con validación de cupo en tiempo real
        layoutUnidades.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    layoutUnidades.setError(getString(R.string.error_before_save));
                    return;
                }
                try {
                    int unidades = Integer.parseInt(text);
                    if (unidades > cupoDisponible) {
                        layoutUnidades.setError("Excede cupo disponible (" + cupoDisponible + ")");
                    } else {
                        layoutUnidades.setError(null); // Limpiar error
                    }
                } catch (NumberFormatException e) {
                    layoutUnidades.setError("Número inválido");
                }
            }
        });
        // Precio
        layoutPrecio.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutPrecio.getEditText().getText().toString().length() < 1) {
                    layoutPrecio.setError(getString(R.string.error_before_save));
                }
            }
        });
    }

    private void callDatePickerFragment() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public void fabSaveOnClick(View view) {
        // Create intent to deliver some kind of result data
        Intent result = new Intent(this, FragmentMainActivity.class);

        // Validación formulario
        if (!validateForm()) {
            return;
        }

        Bundle bundle = new Bundle();

        // Obtener idPosGuia antes de crear el objeto Compra
        int idPosGuia;
        // Modificacion de elemento
        if (getIntent().getExtras().get("position_guia") == null) {
            int pos = getIntent().getExtras().getInt("position", -1);
            bundle.putInt("position", pos);
            idPosGuia = FragmentMainActivity.compras.get(pos).getIdPosGuia();
        } else { // Nuevo elemento
            idPosGuia = getIntent().getExtras().getInt("position_guia");
        }

        // Crear objeto Compra con todos los campos del formulario
        Compra compra = getCurrentCompra(idPosGuia);
        if (compra == null) {
            return; // Error de validación de números
        }
        bundle.putParcelable("modify_compra", compra);

        result.putExtras(bundle);

        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private boolean validateForm() {
        boolean retorno = true;
        // Validaciones campos formularios
        // Calibre1
        if (layoutCalibre1.getEditText().getText().toString().length() < 1) {
            layoutCalibre1.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Unidades compradas
        if (layoutUnidades.getEditText().getText().toString().length() < 1) {
            layoutUnidades.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Precio
        if (layoutPrecio.getEditText().getText().toString().length() < 1) {
            layoutPrecio.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Fecha
        if (layoutFecha.getEditText().getText().toString().length() < 1) {
            layoutFecha.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Validación de cupo - evita que el formulario cierre si excede el cupo
        try {
            String unidadesText = layoutUnidades.getEditText().getText().toString().trim();
            int unidades = unidadesText.isEmpty() ? 0 : Integer.parseInt(unidadesText);
            if (unidades > cupoDisponible) {
                layoutUnidades.setError("Cupo insuficiente. Disponible: " + cupoDisponible);
                retorno = false;
            }
        } catch (NumberFormatException e) {
            layoutUnidades.setError("Número inválido");
            retorno = false;
        }
        if (!retorno) {
            mensajeError.setVisibility(View.VISIBLE);
            mensajeError.setText(getString(R.string.error_mensaje_cabecera));
            mensajeError.setTextColor(Color.parseColor("#0000ff"));

            Snackbar.make(findViewById(R.id.form_scrollview_compra), getString(R.string.error_mensaje_cabecera), Snackbar.LENGTH_LONG).show();
        }
        return retorno;
    }

    /**
     * Recoge todos los campos del formulario y crea un objeto Compra.
     * Similar al patrón usado en LicenciaFormActivity.getCurrenteLicense()
     *
     * @param idPosGuia ID de posición de la guía asociada
     * @return Compra con los datos del formulario, o null si hay error de parsing
     */
    private Compra getCurrentCompra(int idPosGuia) {
        Compra compra = new Compra();
        compra.setIdPosGuia(idPosGuia);
        compra.setCalibre1(layoutCalibre1.getEditText().getText().toString());

        // Segundo calibre
        String cal2 = layoutCalibre2.getEditText().getText().toString();
        compra.setCalibre2(cal2.isEmpty() ? "" : cal2);

        // Parse unidades con manejo de errores
        try {
            String unidadesText = layoutUnidades.getEditText().getText().toString().trim();
            int unidades = unidadesText.isEmpty() ? 0 : Integer.parseInt(unidadesText);
            compra.setUnidades(unidades);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Unidades debe ser un número válido", Toast.LENGTH_LONG).show();
            layoutUnidades.setError("Número inválido");
            return null;
        }

        // Parse precio con manejo de errores y soporte de locale español
        try {
            String precioText = layoutPrecio.getEditText().getText().toString()
                    .replace("€", "")
                    .replace(",", ".")  // Handle Spanish decimal separator
                    .trim();
            double precio = precioText.isEmpty() ? 0.0 : Double.parseDouble(precioText);
            compra.setPrecio(precio);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Precio debe ser un número válido (use . o , para decimales)", Toast.LENGTH_LONG).show();
            layoutPrecio.setError("Número inválido");
            return null;
        }

        // Fecha
        String fecha = layoutFecha.getEditText().getText().toString();
        compra.setFecha(fecha.isEmpty() ? "" : fecha);

        // Tipo munición
        String tipo = layoutTipoMunicion.getEditText().getText().toString();
        compra.setTipo(tipo.isEmpty() ? "" : tipo);

        // Parse peso con manejo de errores
        try {
            String pesoText = layoutPesoMunicion.getEditText().getText().toString().trim();
            int peso = pesoText.isEmpty() ? 0 : Integer.parseInt(pesoText);
            compra.setPeso(peso);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Peso debe ser un número válido", Toast.LENGTH_LONG).show();
            layoutPesoMunicion.setError("Número inválido");
            return null;
        }

        // Marca munición
        String marca = layoutMarcaMunicion.getEditText().getText().toString();
        compra.setMarca(marca.isEmpty() ? "" : marca);

        // Tienda
        String tienda = layoutTienda.getEditText().getText().toString();
        compra.setTienda(tienda.isEmpty() ? "" : tienda);

        compra.setValoracion(valoracion.getRating());
        compra.setImagePath(imagePath);

        return compra;
    }

    /**
     * Created by ahidalgog
     * DatePickerFragment para seleccionar la fecha de compra
     */
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = 0, month = 0, day = 0;
            final Calendar c = Calendar.getInstance();

            if (layoutFecha.getEditText().getText().toString().equals("")) {
                // Use the current date as the default date in the picker
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                try {
                    c.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFecha.getEditText().getText().toString()));
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            Date date = cal.getTime();

            String f = new DateFormat().format("dd/MM/yyyy", date).toString();
            layoutFecha.getEditText().setText(f);
        }
    }
}
