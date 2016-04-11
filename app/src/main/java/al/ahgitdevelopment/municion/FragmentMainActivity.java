package al.ahgitdevelopment.municion;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import java.util.HashMap;

public class FragmentMainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private final int FORM_COMPLETED = 1;
    private static HashMap<String, ArrayList<Guia>> grupoGuias = new HashMap<String, ArrayList<Guia>>();
    private static ArrayList<Guia> guias = new ArrayList<Guia>();
    private static ArrayList<Compra> compras = new ArrayList<Compra>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setCollapsible(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent form = new Intent(FragmentMainActivity.this, GuiaFormActivity.class);
                startActivityForResult(form, FORM_COMPLETED);
                if(((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).myGuiaAdapter != null)
                    ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).myGuiaAdapter.notifyDataSetChanged();

                if (((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).myCompraAdapter != null)
                ((PlaceholderFragment) mSectionsPagerAdapter.getItem(0)).myCompraAdapter.notifyDataSetChanged();
            }
        });
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
        if (requestCode == FORM_COMPLETED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                addNewGuia(data);
            }
        }
    }

    // Version 1
//    private void addNewGuia(Intent data) {
//        Bundle bundle = data.getExtras();
//        String tipoArma = bundle.getString("tipoArma");
//
//        ArrayList<Guia> itemsList = grupoGuias.get(tipoArma);
//
//        // if list does not exist create it
//        if (itemsList == null) {
//            itemsList = new ArrayList<Guia>();
//            itemsList.add(new Guia(bundle));
//            grupoGuias.put(tipoArma, itemsList);
//        } else {
//            // add if item is not already in list
//            itemsList.add(new Guia(bundle));
//        }
//    }

    // Version 2
    private void addNewGuia(Intent data) {
        Bundle bundle = data.getExtras();

        // if list does not exist create it
        if (guias == null) {
            guias = new ArrayList<Guia>();
            guias.add(new Guia(bundle));
        } else {
            // add if item is not already in list
            guias.add(new Guia(bundle));
        }
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
        public MyExpandableGuias expListAdapter;
        public GuiaAdapter myGuiaAdapter;
        public CompraAdapter myCompraAdapter;


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
//          getArguments().getInt(ARG_SECTION_NUMBER)));

//            View rootView = inflater.inflate(R.layout.fragment_fragment_main, container, false);
//            ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
//            expListAdapter = new MyExpandableGuias(this.getActivity(), grupoGuias);
//            expandableListView.setAdapter(expListAdapter);

            View rootView = null;

            // Lista de guias
            if(getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                rootView = inflater.inflate(android.R.layout.list_content, container, false);
                ListView listView = (ListView) rootView.findViewById(android.R.id.list);
                myGuiaAdapter = new GuiaAdapter(getActivity(), guias);
                listView.setAdapter(myGuiaAdapter);
            }
            // Lista de compras
            else{
                rootView = inflater.inflate(android.R.layout.list_content, container, false);
                ListView listView = (ListView) rootView.findViewById(android.R.id.list);
                myCompraAdapter = new CompraAdapter(getActivity(), compras);
                listView.setAdapter(myCompraAdapter);
            }

            return rootView;
        }

        public MyExpandableGuias getExpListAdapter() {
            if (expListAdapter == null)
                expListAdapter = new MyExpandableGuias(this.getActivity(), grupoGuias);

            expListAdapter.notifyDataSetChanged();

            return expListAdapter;
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
            return PlaceholderFragment.newInstance(position + 1);
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
