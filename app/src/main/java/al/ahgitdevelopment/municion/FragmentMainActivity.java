package al.ahgitdevelopment.municion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.Toast;

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
    public static File fileImagePath = null;
    public static View auxView = null;
    public static ActionMode mActionMode = null;
    public static ActionMode.Callback mActionModeCallback = null;
    public static int imagePosition;
    private static DataBaseSQLiteHelper dbSqlHelper;
    private static ArrayList<Guia> guias;
    private static ArrayList<Compra> compras;
    private static ArrayList<Licencia> licencias;
    private final int GUIA_COMPLETED = 1;
    private final int COMPRA_COMPLETED = 2;
    private final int LICENCIA_COMPLETED = 3;
    private final int GUIA_UPDATED = 4;
    private final int COMPRA_UPDATED = 5;
    private final int LICENCIA_UPDATED = 6;
    public Toolbar toolbar;
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
        toolbar.setTitle(R.string.title_activity_fragment_main);
        toolbar.setCollapsible(false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_4_transparent);

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
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mViewPager != null)
            tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //                        .setAction("Action", null).show();
                    Intent form = null;
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
//                            Snackbar.make(view, "Introduce una guía", Snackbar.LENGTH_LONG)
//                                    .setAction("Action", null).show();
                            form = new Intent(FragmentMainActivity.this, GuiaFormActivity.class);
                            startActivityForResult(form, GUIA_COMPLETED);
                            break;
                        case 1:
//                            Snackbar.make(view, "Introduce una compra", Snackbar.LENGTH_LONG)
//                                    .setAction("Action", null).show();
                            form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
                            startActivityForResult(form, COMPRA_COMPLETED);

                            break;
                        case 2:
//                            Snackbar.make(view, "Introduce una licencia", Snackbar.LENGTH_LONG)
//                                    .setAction("Action", null).show();
                            form = new Intent(FragmentMainActivity.this, LicenciaFormActivity.class);
                            startActivityForResult(form, LICENCIA_COMPLETED);
                            break;
                    }

                    mActionModeCallback.onDestroyActionMode(mActionMode);
                }
            });
        }
    }

    private void openForm(int position) {
        Intent form = null;
        Bundle data = new Bundle();
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
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).guiaArrayAdapter.notifyDataSetChanged();
                break;
            case 1:
                compras.remove(position);
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).compraArrayAdapter.notifyDataSetChanged();
                break;
            case 2:
                licencias.remove(position);
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).licenciaArrayAdapter.notifyDataSetChanged();
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
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        // Check which request we're responding to
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
//                    if (data != null) {
//                        Bundle extras = data.getExtras();
//                        imageBitmap = (Bitmap) extras.get("data");
//                        updateImage(imageBitmap, extras.getParcelable(MediaStore.EXTRA_OUTPUT));
//                    } else
//                        Log.i(getPackageName(), "Intent sin informacion");


                    if (data != null) {
                        imageBitmap = (Bitmap) data.getExtras().get("data");
                        updateImage(imageBitmap);
                    } else
                        Log.i(getPackageName(), "Intent sin informacion");
                    break;
                case GUIA_COMPLETED:
                    guias.add(new Guia(data.getExtras()));
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).guiaArrayAdapter.notifyDataSetChanged();
                    break;
                case COMPRA_COMPLETED:
                    compras.add(new Compra(data.getExtras()));
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).compraArrayAdapter.notifyDataSetChanged();
                    break;
                case LICENCIA_COMPLETED:
                    licencias.add(new Licencia(data.getExtras()));
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).licenciaArrayAdapter.notifyDataSetChanged();
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

    private void updateImage(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            switch (mViewPager.getCurrentItem()) {
                case 0:
//                    guias.get(imagePosition).setImagen(getImageFromUri(imageBitmap.toString()));
//                    guias.get(imagePosition).setImagen(imageBitmap);
                    saveBitmapToFile(imageBitmap);
                    guias.get(imagePosition).setImagePath(fileImagePath.getAbsolutePath());
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).guiaArrayAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    compras.get(imagePosition).setImagen(imageBitmap);
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).compraArrayAdapter.notifyDataSetChanged();
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

//            guia.setIdCompra(data.getExtras().getInt(""));
//            guia.setIdLicencia(data.getExtras().getInt(""));
            guia.setMarca(data.getExtras().getString("marca"));
            guia.setModelo(data.getExtras().getString("modelo"));
            guia.setApodo(data.getExtras().getString("apodo"));
            guia.setTipoArma(data.getExtras().getInt("tipoArma"));
            guia.setCalibre1(data.getExtras().getString("calibre1"));
            guia.setCalibre2(data.getExtras().getString("calibre2"));
            guia.setNumGuia(data.getExtras().getInt("numGuia"));
            guia.setNumArma(data.getExtras().getInt("numArma"));
//            guia.setImagen(data.getExtras().getString("imagen"));
            guia.setCupo(data.getExtras().getInt("cupo"));
            guia.setGastado(data.getExtras().getInt("gastado"));

            ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).guiaArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateCompra(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Compra compra = compras.get(position);

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
//            compra.setImagen(data.getExtras().getString("imagen"));

            ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).compraArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateLicencia(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Licencia licencia = licencias.get(position);

            licencia.setTipo(data.getExtras().getInt("tipo"));
            licencia.setNumLicencia(data.getExtras().getInt("num_licencia"));
            licencia.setFechaExpedicion(data.getExtras().getString("fecha_expedicion"));
            licencia.setFechaCaducidad(data.getExtras().getString("fecha_caducidad"));

            ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).licenciaArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbSqlHelper.saveListGuias(null, guias);
        dbSqlHelper.saveListCompras(null, compras);
        dbSqlHelper.saveListLicencias(null, licencias);
        dbSqlHelper.close();

        Toast.makeText(FragmentMainActivity.this, R.string.guardadoBBDD, Toast.LENGTH_SHORT).show();
    }

    private Bitmap getImageFromUri(String imageUri) {
        if (!imageUri.equals("null"))
            return BitmapFactory.decodeFile(imageUri);
        else
            return BitmapFactory.decodeResource(getResources(), R.drawable.pistola);
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

        public ListView getListView() {
            return listView;
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
                    return getResources().getString(R.string.section_guias_title);
                case 1:
                    return getResources().getString(R.string.section_compras_title);
                case 2:
                    return getResources().getString(R.string.section_licencias_title);
            }
            return null;
        }
    }
}

//http://stackoverflow.com/questions/17207366/creating-a-menu-after-a-long-click-event-on-a-list-view
//http://stackoverflow.com/questions/18204386/contextual-action-mode-in-fragment-close-if-not-focused