package al.ahgitdevelopment.municion;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentMainActivity extends AppCompatActivity {

    private final int GUIA_COMPLETED = 1;
    private final int COMPRA_COMPLETED = 2;
    //    private static HashMap<String, ArrayList<Guia>> grupoGuias = new HashMap<String, ArrayList<Guia>>();
    private DataBaseSQLiteHelper dbSqlHelper;
    private SQLiteDatabase db;

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
//        final View layout = getLayoutInflater().inflate(R.layout.activity_fragment_main,null);
//        setContentView(layout);
        setContentView(R.layout.activity_fragment_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setCollapsible(false);

        //Abrimos la base de datos 'DBUMunicion' en modo escritura
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        db = dbSqlHelper.getWritableDatabase();

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
                    if (mViewPager.getCurrentItem() == 0) {
                        Intent form = new Intent(FragmentMainActivity.this, GuiaFormActivity.class);
                        startActivityForResult(form, GUIA_COMPLETED);
                    } else {
                        Snackbar.make(view, "Mostrar Fragment Dialog para seleccionar una guia", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //                    Intent form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
                        //                    startActivityForResult(form, COMPRA_COMPLETED);
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
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == GUIA_COMPLETED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0))
//                        .getGuias().add(new Guia(data.getExtras()));
//                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).myGuiaAdapter.notifyDataSetChanged();

                insertGuiaToBBDD(data.getExtras());
//                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).getMyGuiaCursorAdapter().notifyDataSetChanged();
            }
        }

        if (requestCode == COMPRA_COMPLETED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(1))
                        .getCompras().add(new Compra(data.getExtras()));
//                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(1)).getView().invalidate();
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(1)).myCompraAdapter.notifyDataSetChanged();
                insertCompraToBBDD(data.getExtras());
            }
        }
    }

    private void insertGuiaToBBDD(Bundle data) {
        ContentValues values = new ContentValues();
        values.put(DataBaseSQLiteHelper.KEY_GUIA_NOMBRE, data.getString("nombreArma", ""));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_MARCA, data.getString("marca", ""));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_MODELO, data.getString("modelo", ""));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_NUM_GUIA, data.getInt("numGuia", 0));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_CALIBRE, data.getString("calibre", ""));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_TIPO_ARMA, data.getString("tipoArma", ""));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_GASTADOS, data.getInt("cartuchosGastados", 0));
        values.put(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_TOTALES, data.getInt("cartuchosTotales", 0));

        long newRowId = db.insert(DataBaseSQLiteHelper.TABLE_GUIAS, null, values);
        Toast.makeText(FragmentMainActivity.this, "Row id: " + newRowId, Toast.LENGTH_LONG).show();
    }

    private void insertCompraToBBDD(Bundle data) {
        ContentValues values = new ContentValues();
        values.put(DataBaseSQLiteHelper.KEY_COMPRA_PRECIO, data.getFloat("precio", 0));
        values.put(DataBaseSQLiteHelper.KEY_COMPRA_CARTUCHOS_COMPRADOS, data.getInt("cartuchosComprados", 0));

        db.insert(DataBaseSQLiteHelper.TABLE_COMPRAS, null, values);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //        private MyExpandableGuias expListAdapter;
        private static GuiaAdapter myGuiaAdapter;
        private static GuiaCursorAdapter myGuiaCursorAdapter;
        private static CompraAdapter myCompraAdapter;
        //        private static CompraCursorAdapter myCompraCursorAdapter;
        private static ArrayList<Guia> guias = new ArrayList<Guia>();
        private static Cursor cursorGuias;
        private static ArrayList<Compra> compras = new ArrayList<Compra>();
        private static DataBaseSQLiteHelper dbSqlHelper;
        private static SQLiteDatabase db;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            try {
//                myGuiaAdapter = new GuiaAdapter(fragment.getActivity(), guias);

                //Abrimos la base de datos 'DBUMunicion' en modo escritura
                dbSqlHelper = new DataBaseSQLiteHelper(fragment.getActivity());

                db = dbSqlHelper.getWritableDatabase();
                cursorGuias = db.query(
                        DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                        null,  //Lista de Columnas a consultar
                        null,  //Columnas para la clausula WHERE
                        null,  //Valores a comparar con las columnas del WHERE
                        null,  //Agrupar con GROUP BY
                        null,  //Condición HAVING para GROUP BY
                        null  //Clausula ORDER BY
                );

                myGuiaCursorAdapter = new GuiaCursorAdapter(fragment.getActivity(), cursorGuias, CursorAdapter.FLAG_AUTO_REQUERY);

                myCompraAdapter = new CompraAdapter(fragment.getActivity(), compras);
            } catch (Exception ex) {
                Log.e("TAG", "Error en la instaciacion de los adapter", ex);
            }

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//          Example: getArguments().getInt(ARG_SECTION_NUMBER)));

//            View rootView = inflater.inflate(R.layout.fragment_fragment_main, container, false);
//            ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
//            expListAdapter = new MyExpandableGuias(this.getActivity(), grupoGuias);
//            expandableListView.setAdapter(expListAdapter);

            db = dbSqlHelper.getWritableDatabase();
            if (db != null) {
                cursorGuias = db.query(
                        DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                        null,  //Lista de Columnas a consultar
                        null,  //Columnas para la clausula WHERE
                        null,  //Valores a comparar con las columnas del WHERE
                        null,  //Agrupar con GROUP BY
                        null,  //Condición HAVING para GROUP BY
                        null  //Clausula ORDER BY
                );
            }
            myGuiaCursorAdapter = new GuiaCursorAdapter(getActivity(), cursorGuias, CursorAdapter.FLAG_AUTO_REQUERY);

            View rootView = inflater.inflate(android.R.layout.list_content, container, false);
            ListView listView = (ListView) rootView.findViewById(android.R.id.list);

            // Lista de guias
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                //Utilizando un adapter
//                listView.setAdapter(myGuiaAdapter);
                //Utilizando un cursorAdapter
                listView.setAdapter(myGuiaCursorAdapter);
            }
            // Lista de compras
            else {
                myCompraAdapter = new CompraAdapter(getActivity(), compras);
                listView.setAdapter(myCompraAdapter);
            }

            return rootView;
        }

//        public MyExpandableGuias getExpListAdapter() {
//            if (expListAdapter == null)
//                expListAdapter = new MyExpandableGuias(this.getActivity(), grupoGuias);
//
//            expListAdapter.notifyDataSetChanged();
//
//            return expListAdapter;
//        }

        public ArrayList<Guia> getGuias() {
            return guias;
        }

        public ArrayList<Compra> getCompras() {
            return compras;
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
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.section_guias_title);
                case 1:
                    return getResources().getString(R.string.section_compras_title);
            }
            return null;
        }
    }
}
