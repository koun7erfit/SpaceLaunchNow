package me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.launcher;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.BaseActivityOld;
import me.calebjones.spacelaunchnow.common.databinding.ActivityAgencyLaunchBinding;
import me.calebjones.spacelaunchnow.common.ui.settings.SettingsActivity;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterActivity;
import timber.log.Timber;

public class LauncherLaunchActivity extends BaseActivityOld implements
        UpcomingLauncherLaunchesFragment.OnFragmentInteractionListener,
        PreviousLauncherLaunchesFragment.OnFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener
{


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private UpcomingLauncherLaunchesFragment upcomingFragment;
    private PreviousLauncherLaunchesFragment previousFragment;
    private ArrayList<String> agencyList;
    private List<Agency> agencies;
    private boolean upcomingLoading = false;
    private boolean previousLoading = false;
    private String searchTerm = null;
    private String lspName = null;
    private String launcherName = null;
    private String serialNumber = null;
    private Integer launcherId = null;
    private ActivityAgencyLaunchBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgencyLaunchBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lspName = extras.getString("lspName");
            launcherName = extras.getString("launcherName");
            launcherId = extras.getInt("launcherId");
            serialNumber = extras.getString("serialNumber");
        }
        setTitle();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        binding.container.setAdapter(mSectionsPagerAdapter);
        binding.container.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        binding.tabs.setupWithViewPager(binding.container);
        binding.swipeRefresh.setOnRefreshListener(this);

        binding.container.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(
                binding.tabs));
        binding.tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.container));
        binding.menu.setVisibility(View.GONE);
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        binding.swipeRefresh.setEnabled(enable);
    }

    @Override
    public void onResume() {
        setTitle();
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launcher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_supporter) {
            Intent intent = new Intent(this, SupporterActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showUpcomingLoading(boolean loading) {
        upcomingLoading = loading;
        showNetworkLoading();
    }

    @Override
    public void showPreviousLoading(boolean loading) {
        previousLoading = loading;
        showNetworkLoading();
    }

    @Override
    public void onRefresh() {
        lspName = null;
        searchTerm = null;
        serialNumber = null;
        refresh();
    }

    private void refresh() {
        if (upcomingFragment != null) {
            upcomingFragment.onRefresh(lspName, searchTerm, serialNumber);
        }
        if (previousFragment != null) {
            previousFragment.onRefresh(lspName, searchTerm, serialNumber);
        }
        setTitle();
    }

    private void setTitle() {
        if (launcherName != null) {
            binding.toolbar.setTitle(launcherName);
        } else if (serialNumber != null) {
            binding.toolbar.setTitle(serialNumber);
        } else {
            binding.toolbar.setTitle("Launches");
        }
    }

    private void showNetworkLoading() {
        if (upcomingLoading || previousLoading) {
            showLoading();
        } else {
            hideLoading();
        }
    }

    private void showLoading() {
        Timber.v("Show Loading...");
        binding.swipeRefresh.post(() -> binding.swipeRefresh.setRefreshing(true));
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        binding.swipeRefresh.post(() -> binding.swipeRefresh.setRefreshing(false));
    }

    @Override
    public void setUpcomingBadge(int count) {
        if (count > 0) {
            binding.tabs.with(0).badge(true).badgeCount(count).name("UPCOMING").build();
        }
    }

    @Override
    public void setPreviousBadge(int count) {
        if (count > 0) {
            binding.tabs.with(1).badge(true).badgeCount(count).name("PREVIOUS").build();
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return UpcomingLauncherLaunchesFragment.newInstance(
                            searchTerm,
                            lspName,
                            launcherId,
                            serialNumber
                    );
                case 1:
                    return PreviousLauncherLaunchesFragment.newInstance(
                            searchTerm,
                            lspName,
                            launcherId,
                            serialNumber
                    );
                default:
                    return null;
            }
        }

        // Here we can finally safely save a reference to the created
        // Fragment, no matter where it came from (either getItem() or
        // FragmentManger). Simply save the returned Fragment from
        // super.instantiateItem() into an appropriate reference depending
        // on the ViewPager position.
        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    upcomingFragment = (UpcomingLauncherLaunchesFragment) createdFragment;
                    break;
                case 1:
                    previousFragment = (PreviousLauncherLaunchesFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getApplicationContext().getString(R.string.upcoming);
                case 1:
                    return getApplicationContext().getString(R.string.previous);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
