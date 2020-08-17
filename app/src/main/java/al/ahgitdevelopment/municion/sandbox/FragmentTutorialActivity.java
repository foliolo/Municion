package al.ahgitdevelopment.municion.sandbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.Locale;

import al.ahgitdevelopment.municion.R;

public class FragmentTutorialActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tutorial);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (!prefs.contains("show_tutorial")) {
            prefs.edit().putBoolean("show_tutorial", false).commit();
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

        public PlaceholderFragment() {
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
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.content_fragment_tutorial, container, false);
            ImageView image = rootView.findViewById(R.id.image_tutorial);
            final ImageView continuar = rootView.findViewById(R.id.btn_exit_tutorial);

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    image.setBackgroundResource(R.drawable.tutorial_01);
                    continuar.setVisibility(View.GONE);
                    break;

                case 2:
                    image.setBackgroundResource(R.drawable.tutorial_02);
                    continuar.setVisibility(View.GONE);
                    break;

                case 3:
                    image.setBackgroundResource(R.drawable.tutorial_03);
                    continuar.setVisibility(View.GONE);
                    break;

                case 4:
                    image.setBackgroundResource(R.drawable.tutorial_04);
                    continuar.setVisibility(View.GONE);
                    break;

                case 5:
                    image.setBackgroundResource(R.drawable.tutorial_05);
                    continuar.setVisibility(View.GONE);
                    break;

                case 6:
                    image.setBackgroundResource(R.drawable.tutorial_06);
                    continuar.setVisibility(View.GONE);
                    break;

                case 7:
                    image.setBackgroundResource(R.drawable.tutorial_07);
                    continuar.setVisibility(View.GONE);
                    break;

                case 8:
                    image.setBackgroundResource(R.drawable.tutorial_08);

                    //Cambio del boton en funcion del idioma
                    if (Locale.getDefault().getLanguage().toLowerCase().contains("es")) {
                        continuar.setImageResource(R.drawable.ic_empieza);
                    } else {
                        continuar.setImageResource(R.drawable.ic_go);
                    }

                    continuar.setVisibility(View.VISIBLE);
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                            SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                            if (!prefs.contains("show_tutorial")) {
                                prefs.edit().putBoolean("show_tutorial", false).apply();
                            }
                        }
                    });
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 8;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "SECTION 1";
//                case 1:
//                    return "SECTION 2";
//                case 2:
//                    return "SECTION 3";
//            }
//            return null;
//        }
    }
}
