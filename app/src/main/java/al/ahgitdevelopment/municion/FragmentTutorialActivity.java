package al.ahgitdevelopment.municion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class FragmentTutorialActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tutorial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.content_fragment_tutorial, container, false);
            ImageView image = (ImageView) rootView.findViewById(R.id.image_tutorial);
            Button exit = (Button) rootView.findViewById(R.id.btn_exit_tutorial);

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    image.setBackgroundResource(R.drawable.tutorial_01);
                    exit.setVisibility(View.GONE);
                    break;

                case 2:
                    image.setBackgroundResource(R.drawable.tutorial_02);
                    exit.setVisibility(View.GONE);
                    break;

                case 3:
                    image.setBackgroundResource(R.drawable.tutorial_03);
                    exit.setVisibility(View.GONE);
                    break;

                case 4:
                    image.setBackgroundResource(R.drawable.tutorial_04);
                    exit.setVisibility(View.GONE);
                    break;

                case 5:
                    image.setBackgroundResource(R.drawable.tutorial_05);
                    exit.setVisibility(View.GONE);
                    break;

                case 6:
                    image.setBackgroundResource(R.drawable.tutorial_06);
                    exit.setVisibility(View.GONE);
                    break;

                case 7:
                    image.setBackgroundResource(R.drawable.tutorial_07);
                    exit.setVisibility(View.GONE);
                    break;

                case 8:
                    image.setBackgroundResource(R.drawable.tutorial_08);
                    exit.setVisibility(View.VISIBLE);
                    exit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();

                            SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                            if (!prefs.contains("show_tutorial")) {
                                prefs.edit().putBoolean("show_tutorial", false).commit();
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
