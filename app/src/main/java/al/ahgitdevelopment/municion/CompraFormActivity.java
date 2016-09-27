package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;

/**
 * Created by ahidalgog on 11/04/2016.
 */
public class CompraFormActivity extends AppCompatActivity {
    public static EditText fecha;
    private TextInputLayout layoutFecha;
    private EditText calibre1;
    private EditText calibre2;
    private CheckBox checkSegundoCalibre;
    private EditText unidades;
    private EditText precio;
    private EditText tipoMunicion;
    private EditText pesoMunicion;
    private EditText marcaMunicion;
    private EditText tienda;
    private RatingBar valoracion;
    //    private ImageView imagen;
    // Mensaje de error antes de guardar
    private TextView mensajeError;
    private String imagePath;

    private int posicionGuia;
    private Toolbar toolbar;


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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_nueva_compra);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        calibre1 = (EditText) findViewById(R.id.form_calibre1);
        checkSegundoCalibre = (CheckBox) findViewById(R.id.form_check_segundo_calibre);
        calibre2 = (EditText) findViewById(R.id.form_calibre2);
        unidades = (EditText) findViewById(R.id.form_unidades);
        precio = (EditText) findViewById(R.id.form_precio);
        layoutFecha = (TextInputLayout) findViewById(R.id.layout_form_fecha_compra);
        fecha = (EditText) findViewById(R.id.form_fecha);
        tipoMunicion = (EditText) findViewById(R.id.form_tipo_municion);
        pesoMunicion = (EditText) findViewById(R.id.form_peso_municion);
        marcaMunicion = (EditText) findViewById(R.id.form_marca_municion);
        tienda = (EditText) findViewById(R.id.form_tienda);
        valoracion = (RatingBar) findViewById(R.id.form_ratingBar_valoracion);
//        imagen = (ImageView) findViewById(R.id.imagen);
        mensajeError = (TextView) findViewById(R.id.form_mensaje_compra);
        imagePath = null;

        if (getIntent().getExtras() != null) {
            //Carga de datos (en caso de modificacion)
            if (getIntent().getExtras().get("position_guia") == null) {
                try {
                    Compra compra = getIntent().getExtras().getParcelable("modify_compra");

                    calibre1.setText(compra.getCalibre1());
                    if (compra.getCalibre2() != null) {
                        if (!"".equals(compra.getCalibre2().toString())) {
                            checkSegundoCalibre.setChecked(true);
                            calibre2.setVisibility(View.VISIBLE);
                        } else {
                            checkSegundoCalibre.setChecked(false);
                            calibre2.setVisibility(View.GONE);
                        }
                        calibre2.setText(compra.getCalibre2().toString());
                    }
                    unidades.setText(String.valueOf(compra.getUnidades()));
                    precio.setText(String.valueOf(compra.getPrecio() + "€"));
                    fecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(compra.getFecha().getTime()));
                    tipoMunicion.setText(compra.getTipo());
                    pesoMunicion.setText(String.valueOf(compra.getPeso()));
                    marcaMunicion.setText(compra.getMarca());
                    tienda.setText(compra.getTienda());
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
                    calibre1.setText(guia.getCalibre1());
                    if (guia.getCalibre2() != null && !guia.getCalibre2().equals("")) {
                        checkSegundoCalibre.setChecked(true);
                        calibre2.setVisibility(View.VISIBLE);
                        calibre2.setText(guia.getCalibre2());
                    }
                }
            }
        }

        checkSegundoCalibre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibre2.setVisibility(View.VISIBLE);
                } else {
                    calibre2.setVisibility(View.GONE);
                    calibre2.setText("");
                }
            }
        });

        layoutFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        fecha.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });

        // Evento que saca el calendario al recibir el foco en el campo fecha
        fecha.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    callDatePickerFragment();
            }
        });

        pesoMunicion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && pesoMunicion.getText().toString().equals("0"))
                    pesoMunicion.setText("");
            }
        });

        // Validaciones de campos obligatorios antes de guardar
        // Calibre
        calibre1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (calibre1.getText().toString().length() < 1) {
                    calibre1.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Unidades
        unidades.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (unidades.getText().toString().length() < 1) {
                    unidades.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Precio
        precio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (precio.getText().toString().length() < 1) {
                    precio.setError(getString(R.string.error_before_save));
                }
            }
        });

        //Admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(Utils.getAdRequest(mAdView));
    }

    private void callDatePickerFragment() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // Create intent to deliver some kind of result data
            Intent result = new Intent(this, FragmentMainActivity.class);

            // Validación formulario
            if (!validateForm()) {
                return false;
            }

            Bundle bundle = new Bundle();
            bundle.putString("calibre1", calibre1.getText().toString());
            if (calibre2.getText().toString().isEmpty()) {
                calibre2.setText(null);
            }
            bundle.putString("calibre2", calibre2.getText().toString());

            if (unidades.getText().toString().isEmpty()) {
                unidades.setText("0");
            }
            bundle.putInt("unidades", Integer.parseInt(unidades.getText().toString()));

            bundle.putDouble("precio", Double.parseDouble(precio.getText().toString().replace("€", "")));
            if (fecha.getText().toString().isEmpty()) {
                fecha.setText("");
            }
            bundle.putString("fecha", fecha.getText().toString());

            if (tipoMunicion.getText().toString().isEmpty()) {
                tipoMunicion.setText("");
            }
            bundle.putString("tipo", tipoMunicion.getText().toString());

            if (pesoMunicion.getText().toString().isEmpty()) {
                pesoMunicion.setText("0");
            }
            bundle.putInt("peso", Integer.parseInt(pesoMunicion.getText().toString()));

            if (marcaMunicion.getText().toString().isEmpty()) {
                marcaMunicion.setText(null);
            }
            bundle.putString("marca", marcaMunicion.getText().toString());

            if (tienda.getText().toString().isEmpty()) {
                tienda.setText(null);
            }
            bundle.putString("tienda", tienda.getText().toString());

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

        return super.onOptionsItemSelected(item);
    }

    private boolean validateForm() {
        boolean retorno = true;
        // Validaciones campos formularios
        // Calibre1
        if (calibre1.getText().toString().length() < 1) {
            calibre1.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Unidades
        if (unidades.getText().toString().length() < 1) {
            unidades.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Precio
        if (precio.getText().toString().length() < 1) {
            precio.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        if (!retorno) {
            mensajeError.setVisibility(View.VISIBLE);
            mensajeError.setText(getString(R.string.error_mensaje_cabecera));
            mensajeError.setTextColor(Color.parseColor("#0000ff"));
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
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

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
            fecha.setText(f);
        }
    }
}
