package me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.agency;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

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
import me.calebjones.spacelaunchnow.data.networking.DataClient;
import me.calebjones.spacelaunchnow.data.networking.responses.base.AgencyResponse;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AgencyLaunchActivity extends BaseActivityOld implements UpcomingAgencyLaunchesFragment.OnFragmentInteractionListener,
        PreviousAgencyLaunchesFragment.OnFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener
{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private UpcomingAgencyLaunchesFragment upcomingFragment;
    private PreviousAgencyLaunchesFragment previousFragment;
    private ArrayList<String> agencyList;
    private List<Agency> agencies;
    private boolean upcomingLoading = false;
    private boolean previousLoading = false;
    String lspName;
    String searchTerm;
    private ActivityAgencyLaunchBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAgencyLaunchBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        if (null != intent) { //Null Checking
            lspName = intent.getStringExtra("lspName");
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
        getFeaturedAgencies();

        binding.menu.setOnClickListener(v -> onViewClicked());
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        binding.swipeRefresh.setEnabled(enable);
    }

    @Override
    public void onResume() {
        setTitle();
        super.onResume();
    }

    private void getFeaturedAgencies() {
        DataClient.getInstance().getFeaturedAgencies(new Callback<AgencyResponse>() {
            @Override
            public void onResponse(Call<AgencyResponse> call, Response<AgencyResponse> response) {
                if (response.isSuccessful()) {
                    List<Agency> agencies = response.body().getAgencies();
                    agencyList = new ArrayList<>();
                    for (Agency agency : agencies) {
                        agencyList.add(agency.getName());
                    }
                    binding.menu.setVisibility(View.VISIBLE);
                } else {
                    binding.menu.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AgencyResponse> call, Throwable t) {
                binding.menu.setVisibility(View.GONE);
            }
        }, 50);
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

    private void showAgencyDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.select_launch_agency)
                .content(R.string.select_launch_agency_description)
                .items(agencyList)
                .itemsCallback((dialog, view, which, text) -> {
                    lspName = String.valueOf(text);
                    refresh();
                })
                .positiveText(R.string.filter)
                .negativeText(R.string.close)
                .show();
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
        refresh();
    }

    private void refresh() {
        if (upcomingFragment != null) {
            upcomingFragment.onRefresh(lspName, searchTerm);
        }
        if (previousFragment != null) {
            previousFragment.onRefresh(lspName, searchTerm);
        }
        setTitle();
    }

    private void setTitle() {
        if (lspName != null) {
            binding.toolbar.setTitle(lspName);
        } else if (searchTerm != null) {
            binding.toolbar.setTitle(searchTerm);
        } else {
            binding.toolbar.setTitle("Select an Agency");
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


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return UpcomingAgencyLaunchesFragment.newInstance(searchTerm, lspName);
                case 1:
                    return PreviousAgencyLaunchesFragment.newInstance(searchTerm, lspName);
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
                    upcomingFragment = (UpcomingAgencyLaunchesFragment) createdFragment;
                    break;
                case 1:
                    previousFragment = (PreviousAgencyLaunchesFragment) createdFragment;
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

    public void onViewClicked() {
        showAgencyDialog();
    }
}
