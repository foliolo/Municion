package al.ahgitdevelopment.municion.forms;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
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

    private SharedPreferences prefs;
    private AdView mAdView;

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
        mAdView = findViewById(R.id.adView);

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
        }

        checkSegundoCalibre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutCalibre2.setVisibility(View.VISIBLE);
                } else {
                    layoutCalibre2.setVisibility(View.GONE);
                    layoutCalibre2.getEditText().setText("");
                }
            }
        });

        layoutFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        layoutFecha.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
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

        layoutPesoMunicion.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && layoutPesoMunicion.getEditText().getText().toString().equals("0"))
                    layoutPesoMunicion.getEditText().setText("");
            }
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
        // Unidades
        layoutUnidades.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutUnidades.getEditText().getText().toString().length() < 1) {
                    layoutUnidades.setError(getString(R.string.error_before_save));
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

        // Gestion de anuncios
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean(Utils.PREFS_SHOW_ADS, true)) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.setEnabled(true);
            mAdView.loadAd(Utils.getAdRequest(mAdView));
        } else {
            mAdView.setVisibility(View.GONE);
            mAdView.setEnabled(false);
        }
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
        bundle.putString("calibre1", layoutCalibre1.getEditText().getText().toString());
        if (layoutCalibre2.getEditText().getText().toString().isEmpty()) {
            layoutCalibre2.getEditText().setText(null);
        }
        bundle.putString("calibre2", layoutCalibre2.getEditText().getText().toString());

        if (layoutUnidades.getEditText().getText().toString().isEmpty()) {
            layoutUnidades.getEditText().setText("0");
        }
        bundle.putInt("unidades", Integer.parseInt(layoutUnidades.getEditText().getText().toString()));

        bundle.putDouble("precio", Double.parseDouble(layoutPrecio.getEditText().getText().toString().replace("€", "")));
        if (layoutFecha.getEditText().getText().toString().isEmpty()) {
            layoutFecha.getEditText().setText("");
        }
        bundle.putString("fecha", layoutFecha.getEditText().getText().toString());

        if (layoutTipoMunicion.getEditText().getText().toString().isEmpty()) {
            layoutTipoMunicion.getEditText().setText("");
        }
        bundle.putString("tipo", layoutTipoMunicion.getEditText().getText().toString());

        if (layoutPesoMunicion.getEditText().getText().toString().isEmpty()) {
            layoutPesoMunicion.getEditText().setText("0");
        }
        bundle.putInt("peso", Integer.parseInt(layoutPesoMunicion.getEditText().getText().toString()));

        if (layoutMarcaMunicion.getEditText().getText().toString().isEmpty()) {
            layoutMarcaMunicion.getEditText().setText(null);
        }
        bundle.putString("marca", layoutMarcaMunicion.getEditText().getText().toString());

        if (layoutTienda.getEditText().getText().toString().isEmpty()) {
            layoutTienda.getEditText().setText(null);
        }
        bundle.putString("tienda", layoutTienda.getEditText().getText().toString());

        bundle.putFloat("valoracion", valoracion.getRating());

        bundle.putString("imagePath", imagePath);

        //Paso de vuelta la posicion de la guía en el array
        // Modificacion de elemento
        if (getIntent().getExtras().get("position_guia") == null) {
            int pos = getIntent().getExtras().getInt("position", -1);
            bundle.putInt("position", pos);
            bundle.putInt("idPosGuia", FragmentMainActivity.compras.get(pos).getIdPosGuia());
        } else { // Nuevo elemento
            bundle.putInt("idPosGuia", getIntent().getExtras().getInt("position_guia"));
        }

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
        if (!retorno) {
            mensajeError.setVisibility(View.VISIBLE);
            mensajeError.setText(getString(R.string.error_mensaje_cabecera));
            mensajeError.setTextColor(Color.parseColor("#0000ff"));

            Snackbar.make(findViewById(R.id.form_scrollview_compra), getString(R.string.error_mensaje_cabecera), Snackbar.LENGTH_LONG).show();
        }
        return retorno;
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