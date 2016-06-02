package al.ahgitdevelopment.municion;

import android.content.Intent;
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

import java.util.ArrayList;

import al.ahgitdevelopment.municion.Adapters.CompraArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.GuiaArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.LicenciaArrayAdapter;
import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;

public class FragmentMainActivity extends AppCompatActivity {

    private static DataBaseSQLiteHelper dbSqlHelper;
    private static ArrayList<Guia> guias;
    private static ArrayList<Compra> compras;
    private static ArrayList<Licencia> licencias;

    private final int GUIA_COMPLETED = 1;
    private final int COMPRA_COMPLETED = 2;
    private final int LICENCIA_COMPLETED = 3;

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
//    public Toolbar toolbar;

    public static ActionMode mActionMode = null;
    public static ActionMode.Callback mActionModeCallback = null;

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
//                            openForm();
                        Toast.makeText(FragmentMainActivity.this, "Modify item: " + (int) mActionMode.getTag(), Toast.LENGTH_SHORT).show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.item_menu_delete:
//                            deleteSelectedItems();
                        Toast.makeText(FragmentMainActivity.this, "Delete item" + (int) mActionMode.getTag(), Toast.LENGTH_SHORT).show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
//                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).getListView()
//                        .getSelectedView().setSelected(false);
                mActionMode = null;
            }
        };

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(R.string.title_activity_fragment_main);
//        toolbar.setCollapsible(false);
//        setSupportActionBar(toolbar); //con esto sale el "puto" action bar de arriba

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
                if (mActionMode != null)
                    mActionMode.finish();
                mActionMode = null;
            }

            @Override
            public void onPageSelected(int position) {

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
                }
            });
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
        // Check which request we're responding to
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
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
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbSqlHelper.saveListGuias(guias);
        dbSqlHelper.saveListCompras(compras);
        dbSqlHelper.saveListLicencias(licencias);
        dbSqlHelper.close();

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

        public ListView getListView() {
            return listView;
        }

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
                    Toast.makeText(getActivity(), "Elemento pulsado", Toast.LENGTH_SHORT).show();

                    if (mActionMode != null) {
                        return false;
                    }

                    view.setSelected(true);
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    mActionMode.setTag(position);
                    return true;
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