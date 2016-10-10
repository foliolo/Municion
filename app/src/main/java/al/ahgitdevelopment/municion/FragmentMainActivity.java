package al.ahgitdevelopment.municion;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import al.ahgitdevelopment.municion.Adapters.CompraArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.GuiaArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.LicenciaArrayAdapter;
import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;

public class FragmentMainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 100;
    public static final int GUIA_COMPLETED = 1;
    public static final int COMPRA_COMPLETED = 2;
    public static File fileImagePath = null;
    public static View auxView = null;
    public static ActionMode mActionMode = null;
    public static ActionMode.Callback mActionModeCallback = null;
    public static int imagePosition;
    public static ArrayList<Guia> guias;
    public static ArrayList<Compra> compras;
    public static ArrayList<Licencia> licencias;
    private static DataBaseSQLiteHelper dbSqlHelper;
    private final int LICENCIA_COMPLETED = 3;
    private final int GUIA_UPDATED = 4;
    private final int COMPRA_UPDATED = 5;
    private final int LICENCIA_UPDATED = 6;
    public Toolbar toolbar;
    private TextView textEmptyList = null;

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);

        mActionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_cab, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.item_menu_modify:
//                        Toast.makeText(FragmentMainActivity.this, "Modify item: " + (int) mActionMode.getTag(), Toast.LENGTH_SHORT).show();
                        openForm((int) mActionMode.getTag());
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.item_menu_delete:
//                        Toast.makeText(FragmentMainActivity.this, "Delete item" + (int) mActionMode.getTag(), Toast.LENGTH_SHORT).show();
                        deleteSelectedItems((int) mActionMode.getTag());
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (auxView != null)
                    auxView.setSelected(false);

                mActionMode = null;
            }
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        textEmptyList = (TextView) findViewById(R.id.textEmptyList);

        // Instanciamos la base de datos
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());

        // Obtenemos las estructuras de datos
        if (guias == null)
            guias = getIntent().getParcelableArrayListExtra("guias");
        if (compras == null)
            compras = getIntent().getParcelableArrayListExtra("compras");
        if (licencias == null)
            licencias = getIntent().getParcelableArrayListExtra("licencias");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null && mSectionsPagerAdapter != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mActionMode != null)
                    mActionMode.finish();
                mActionMode = null;

                ShowTextEmptyList();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mViewPager != null)
            tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)

        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //                        .setAction("Action", null).show();
                    Intent form;
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            if (Utils.getLicenseName(FragmentMainActivity.this).length > 0) {
                                // Seleccion de licencia a la que asociar la guia
                                DialogFragment dialog = new GuiaDialogFragment();
                                dialog.show(getSupportFragmentManager(), "NewGuiaDialogFragment");
                            } else {
                                Snackbar.make(view, R.string.dialog_guia_fail, Snackbar.LENGTH_LONG)
//                                        .setAction(android.R.string.ok, new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                            }
//                                        })
                                        .setAction(android.R.string.ok, null)
                                        .show();
                            }

                            break;
                        case 1:
                            if (guias.size() > 0) {
//                                form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
//                                startActivityForResult(form, COMPRA_COMPLETED);
                                DialogFragment dialog = new CompraDialogFragment();
                                dialog.show(getSupportFragmentManager(), "NewCompraDialogFragment");
                            } else {
                                Snackbar.make(view, "Debe introducir una guia primero", Snackbar.LENGTH_LONG)
//                                        .setAction(android.R.string.ok, new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                            }
//                                        })
                                        .setAction(android.R.string.ok, null)
                                        .show();
                            }

                            break;
                        case 2:
                            form = new Intent(FragmentMainActivity.this, LicenciaFormActivity.class);
                            startActivityForResult(form, LICENCIA_COMPLETED);
                            break;
                    }

                    mActionModeCallback.onDestroyActionMode(mActionMode);
                }
            });
        }

        //Admob - TODO
        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(Utils.getAdRequest(mAdView));
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onStart() {
        super.onStart();

        ShowTextEmptyList();
    }

    /**
     *
     */
    private void ShowTextEmptyList() {
        textEmptyList.setVisibility(View.GONE);

        //En caso de estar la lista vacía, indicamos un texto por defecto.
        switch (mViewPager.getCurrentItem()) {
            case 0:
                if (guias.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.guia_empty_list);
                }
                break;

            case 1:
                if (compras.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.compra_empty_list);
                }
                break;

            case 2:
                if (licencias.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.licencia_empty_list);
                }
                break;

            default:
                textEmptyList.setVisibility(View.GONE);
        }
    }

    private void openForm(int position) {
        Intent form;

        switch (mViewPager.getCurrentItem()) {
            case 0:
                form = new Intent(FragmentMainActivity.this, GuiaFormActivity.class);
                form.putExtra("modify_guia", guias.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, GUIA_UPDATED);
                break;
            case 1:
                form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
                form.putExtra("modify_compra", compras.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, COMPRA_UPDATED);
                break;
            case 2:
                form = new Intent(FragmentMainActivity.this, LicenciaFormActivity.class);
                form.putExtra("modify_licencia", licencias.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, LICENCIA_UPDATED);
                break;
        }
    }

    private void deleteSelectedItems(int position) {
        switch (mViewPager.getCurrentItem()) {
            case 0:
                guias.remove(position);
                PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
                break;
            case 1:
                //FIXME: Falla en la posicion de las guias por eso no guarda las imagenes a veces y no borra lo que debe!
                try {
                    //Actualizar cupo de la guia correspondiente
                    Guia guia = guias.get(compras.get(position).getIdPosGuia());
                    int unidadesComprada = compras.get(position).getUnidades();
                    guia.setGastado(guia.getGastado() - unidadesComprada);

                    //Borrado de la compra
                    compras.remove(position);
                    PlaceholderFragment.compraArrayAdapter.notifyDataSetChanged();
                    PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
                } catch (IndexOutOfBoundsException ex) {
                    Log.e(getPackageName(), "Fallo con los index al borrar una compra", ex);
                }
                break;
            case 2:
                //Si existe alguna conexion, no se podra eliminar la licencia
                if (Utils.licenseCanBeDeleted(position)) {
                    Toast.makeText(FragmentMainActivity.this, R.string.delete_license_fail, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        Utils.removeNotificationFromSharedPreference(this, licencias.get(position).getNumLicencia());
                    } catch (Exception ex) {
                        Log.wtf(getPackageName(), "Fallo al listar las notificaciones", ex);
                    }
                    licencias.remove(position);
                    PlaceholderFragment.licenciaArrayAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fragment_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(FragmentMainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Recepción de los datos del formulario
     *
     * @param requestCode Código de peticion
     * @param resultCode  Código de resultado de la operacion
     * @param data        Intent con los datos de respuesta
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap;
        // Check which request we're responding to
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        imageBitmap = (Bitmap) data.getExtras().get("data");
                        updateImage(imageBitmap);
                    } else
                        Log.i(getPackageName(), "Intent sin informacion");
                    break;

                case GUIA_COMPLETED:
                    guias.add(new Guia(data.getExtras()));
                    PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
                    break;

                case COMPRA_COMPLETED:
                    Compra newCompra = new Compra(data.getExtras());
                    compras.add(newCompra);
                    PlaceholderFragment.compraArrayAdapter.notifyDataSetChanged();

                    //Actualizamos el cupo de la guia a la que pertence la compra
                    int posGuia = newCompra.getIdPosGuia();
                    if (posGuia != -1) {
                        int gastoActual = guias.get(posGuia).getGastado();
                        guias.get(posGuia).setGastado(gastoActual + newCompra.getUnidades());
                    }
                    PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
                    break;

                case LICENCIA_COMPLETED:
                    licencias.add(new Licencia((Licencia) data.getExtras().getParcelable("modify_licencia")));
                    PlaceholderFragment.licenciaArrayAdapter.notifyDataSetChanged();
                    break;

                case GUIA_UPDATED:
                    updateGuia(data);
                    break;
                case COMPRA_UPDATED:
                    updateCompra(data);
                    break;
                case LICENCIA_UPDATED:
                    updateLicencia(data);
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.e(getPackageName(), "Resultado de la camara cancelada");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbSqlHelper.saveListGuias(null, guias);
        dbSqlHelper.saveListCompras(null, compras);
        dbSqlHelper.saveListLicencias(null, licencias);
        dbSqlHelper.close();

//        Toast.makeText(FragmentMainActivity.this, R.string.guardadoBBDD, Toast.LENGTH_SHORT).show();
    }

    private void updateImage(Bitmap imageBitmap) {
        //Guardado en disco de la imagen tomada con la foto
        saveBitmapToFile(imageBitmap);

        //Actualizacion de las listas para mostrar las nuevas imagenes
        if (imageBitmap != null) {
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    guias.get(imagePosition).setImagePath(fileImagePath.getAbsolutePath());
                    PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    compras.get(imagePosition).setImagePath(fileImagePath.getAbsolutePath());
                    PlaceholderFragment.compraArrayAdapter.notifyDataSetChanged();
                    break;
            }
        } else
            Log.e(getPackageName(), "Error en la devolucion de la imagens");
    }

    private void saveBitmapToFile(Bitmap imageBitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileImagePath);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // imageBitmap is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGuia(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Guia guia = guias.get(position);

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Guia y no los campos individualizados.
//            guia.setIdCompra(data.getExtras().getInt(""));
            guia.setTipoLicencia(data.getExtras().getInt("tipoLicencia"));
            guia.setMarca(data.getExtras().getString("marca"));
            guia.setModelo(data.getExtras().getString("modelo"));
            guia.setApodo(data.getExtras().getString("apodo"));
            guia.setTipoArma(data.getExtras().getInt("tipoArma"));
            guia.setCalibre1(data.getExtras().getString("calibre1"));
            guia.setCalibre2(data.getExtras().getString("calibre2"));
            guia.setNumGuia(data.getExtras().getInt("numGuia"));
            guia.setNumArma(data.getExtras().getInt("numArma"));
            guia.setImagePath(data.getExtras().getString("imagePath"));
            guia.setCupo(data.getExtras().getInt("cupo"));
            guia.setGastado(data.getExtras().getInt("gastado"));

            PlaceholderFragment.guiaArrayAdapter.notifyDataSetChanged();
            PlaceholderFragment.compraArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateCompra(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Compra compra = compras.get(position);

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Compra y no los campos individualizados.
            compra.setIdPosGuia(data.getExtras().getInt("idPosGuia"));
            compra.setCalibre1(data.getExtras().getString("calibre1"));
            compra.setCalibre2(data.getExtras().getString("calibre2"));
            compra.setUnidades(data.getExtras().getInt("unidades"));
            compra.setPrecio(data.getExtras().getDouble("precio"));
            compra.setFecha(data.getExtras().getString("fecha"));
            compra.setTipo(data.getExtras().getString("tipo"));
            compra.setPeso(data.getExtras().getInt("peso"));
            compra.setMarca(data.getExtras().getString("marca"));
            compra.setTienda(data.getExtras().getString("tienda"));
            compra.setValoracion(data.getExtras().getFloat("valoracion"));
            compra.setImagePath(data.getExtras().getString("imagePath"));

            PlaceholderFragment.compraArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateLicencia(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);

            Licencia licencia = new Licencia((Licencia) data.getExtras().get("modify_licencia"));
            licencias.set(position, licencia);

            PlaceholderFragment.licenciaArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static GuiaArrayAdapter guiaArrayAdapter = null;
        public static CompraArrayAdapter compraArrayAdapter = null;
        public static LicenciaArrayAdapter licenciaArrayAdapter = null;
        private static ListView listView = null;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_view_pager, container, false);


            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0: // Lista de las guias
                    //Abrimos la base de datos 'DBUMunicion' en modo escritura
                    guiaArrayAdapter = new GuiaArrayAdapter(getActivity(), R.layout.guia_item, guias);
                    listView = (ListView) rootView.findViewById(R.id.ListView);
                    listView.setAdapter(guiaArrayAdapter);
                    break;

                case 1: // Lista de las compras
                    compraArrayAdapter = new CompraArrayAdapter(getActivity(), R.layout.compra_item, compras);
                    listView = (ListView) rootView.findViewById(R.id.ListView);
                    listView.setAdapter(compraArrayAdapter);
                    break;

                case 2: // Lista de las licencias
                    licenciaArrayAdapter = new LicenciaArrayAdapter(getActivity(), R.layout.licencia_item, licencias);
                    listView = (ListView) rootView.findViewById(R.id.ListView);
                    listView.setAdapter(licenciaArrayAdapter);
                    break;
            }

            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mActionMode != null) {
                        return false;
                    }

                    view.setSelected(true);
                    auxView = view;

                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    assert mActionMode != null;
                    mActionMode.setTitle(R.string.menu_cab_title);
                    mActionMode.setTag(position);
                    return true;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mActionMode != null)
                        mActionMode.finish();
                    mActionMode = null;
                }
            });

            return rootView;
        }
    }

    /**
     * Dialog para la seleccion de la licencia
     */
    public static class GuiaDialogFragment extends DialogFragment {
        //https://developer.android.com/guide/topics/ui/dialogs.html

        private int selectedLicense;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set title
            builder.setTitle(R.string.dialog_licencia_title)
                    // Set items
                    .setSingleChoiceItems(Utils.getLicenseName(getActivity()), 0, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Toast.makeText(getActivity(), "Seleccionado: " + (String) getGuiaName()[i], Toast.LENGTH_SHORT).show();
                            selectedLicense = i;
                        }
                    })
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            String tipoLicencia = (String) Utils.getLicenseName(getActivity())[selectedLicense];
                            if (tipoLicencia.equals("F - Tiro olimpico")) {
                                if (Utils.isLicenciaFederativa(getActivity())) {
                                    Intent form = new Intent(getActivity(), GuiaFormActivity.class);
                                    form.putExtra("tipo_licencia", (String) Utils.getLicenseName(getActivity())[selectedLicense]);
                                    getActivity().startActivityForResult(form, FragmentMainActivity.GUIA_COMPLETED);
                                } else {
                                    Toast.makeText(getActivity(), R.string.dialog_guia_licencia_federativa, Toast.LENGTH_LONG).show();
                                    GuiaDialogFragment.this.getDialog().dismiss();
                                }
                            } else {
                                Intent form = new Intent(getActivity(), GuiaFormActivity.class);
                                form.putExtra("tipo_licencia", (String) Utils.getLicenseName(getActivity())[selectedLicense]);
                                getActivity().startActivityForResult(form, FragmentMainActivity.GUIA_COMPLETED);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GuiaDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

    }

    /**
     * Dialog para la seleccion de la licencia qu
     */
    public static class CompraDialogFragment extends DialogFragment {
        //https://developer.android.com/guide/topics/ui/dialogs.html

        private int selectedGuia;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set title
            builder.setTitle(R.string.dialog_guia_title)
                    // Set items
                    .setSingleChoiceItems(getGuiaName(), 0, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {
                            selectedGuia = pos;
                        }
                    })
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos >= 0)
                                Toast.makeText(getActivity(), "Seleccionado: " + getGuiaName()[pos].toString(), Toast.LENGTH_SHORT).show();

                            Intent form = new Intent(getActivity(), CompraFormActivity.class);
                            form.putExtra("position_guia", selectedGuia);
                            form.putExtra("guia", guias.get(selectedGuia));
                            getActivity().startActivityForResult(form, FragmentMainActivity.COMPRA_COMPLETED);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CompraDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }


        private CharSequence[] getGuiaName() {
            ArrayList<String> list = new ArrayList<>();
            for (Guia guia : guias) {
                list.add(guia.getApodo());
            }

            return list.toArray(new CharSequence[list.size()]);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.section_guias_title);
                case 1:
                    return getString(R.string.section_compras_title);
                case 2:
                    return getString(R.string.section_licencias_title);
            }
            return null;
        }
    }
}

//http://stackoverflow.com/questions/17207366/creating-a-menu-after-a-long-click-event-on-a-list-view
//http://stackoverflow.com/questions/18204386/contextual-action-mode-in-fragment-close-if-not-focused