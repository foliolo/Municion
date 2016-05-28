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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setCollapsible(false);

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
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).guiaArrayAdapter.notifyDataSetChanged();
                    break;
                case COMPRA_COMPLETED:
                    compras.add(new Compra(data.getExtras()));
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(1)).compraArrayAdapter.notifyDataSetChanged();
                    break;
                case LICENCIA_COMPLETED:
                    licencias.add(new Licencia(data.getExtras()));
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(2)).licenciaArrayAdapter.notifyDataSetChanged();
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

        public static ListView getListView() {
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
            View rootView = inflater.inflate(R.layout.fragment_fragment_main, container, false);

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