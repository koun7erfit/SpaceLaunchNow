package me.calebjones.spacelaunchnow.astronauts.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;

import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import jonathanfinerty.once.Amount;
import jonathanfinerty.once.Once;
import me.calebjones.spacelaunchnow.astronauts.data.AstronautDataRepository;
import me.calebjones.spacelaunchnow.astronauts.data.Callbacks;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.base.BaseActivity;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.astronaut.Astronaut;
import me.spacelaunchnow.astronauts.R;
import me.spacelaunchnow.astronauts.databinding.ActivityAstronautDetailsBinding;
import timber.log.Timber;

public class AstronautDetailsActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener {


    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;
    private int mMaxScrollSize;
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AstronautDataRepository astronautDataRepository;
    private AstronautDetailViewModel viewModel;
    private Astronaut astronaut;
    private Agency agency;
    private int astronautId;
    private ActivityAstronautDetailsBinding bindings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings = ActivityAstronautDetailsBinding.inflate(getLayoutInflater());

        bindings.astronautFabShare.setVisibility(View.GONE);
        setSupportActionBar(bindings.astronautDetailToolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        bindings.astronautDetailSwipeRefresh.setOnRefreshListener(this);
        bindings.astronautDetailViewpager.setAdapter(mSectionsPagerAdapter);
        bindings.astronautDetailViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(bindings.astronautDetailTabs));
        bindings.astronautDetailViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        bindings.astronautDetailTabs.addTab(bindings.astronautDetailTabs.newTab().setText(getString(R.string.profile)));
        bindings.astronautDetailTabs.addTab(bindings.astronautDetailTabs.newTab().setText(getString(R.string.flights)));
        bindings.astronautDetailTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(bindings.astronautDetailViewpager));
        astronautDataRepository = new AstronautDataRepository(this, getRealm());

        //Grab information from Intent
        Intent mIntent = getIntent();
        Context context = this;
        astronautId = mIntent.getIntExtra("astronautId", 0);

        fetchData(astronautId);

        if (bindings.astronautDetailToolbar != null) {
            setSupportActionBar(bindings.astronautDetailToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        viewModel = ViewModelProviders.of(this).get(AstronautDetailViewModel.class);
        // update UI
        viewModel.getAstronaut().observe(this, astronaut -> {
            if (astronaut != null) {
                updateAstronautViews(astronaut);
            }
        });
        viewModel.getAgency().observe(this, agency -> {
            if (agency != null) {
                updateAgencyViews(agency);
            }
        });

        if (!SupporterHelper.isSupporter() && Once.beenDone("appOpen",
                Amount.moreThan(3))) {
            AdRequest adRequest = new AdRequest.Builder().build();
            bindings.astronautAdView.loadAd(adRequest);
            bindings.astronautAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    bindings.astronautAdView.setVisibility(View.VISIBLE);
                }

            });
        } else {
            bindings.astronautAdView.setVisibility(View.GONE);
        }

        bindings.astronautFabShare.setOnClickListener(view -> new ShareCompat.IntentBuilder(
                AstronautDetailsActivity.this)
                .setType("text/plain")
                .setChooserTitle(astronaut.getName())
                .setText(astronaut.getUrl())
                .startChooser());
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        if (bindings.astronautDetailSwipeRefresh != null) {
            bindings.astronautDetailSwipeRefresh.setEnabled(enable);
        }
    }

    private void fetchData(int astronautId) {
        astronautDataRepository.getAstronautById(astronautId, new Callbacks.AstronautCallback() {
            @Override
            public void onAstronautLoaded(Astronaut astronaut) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    updateAstronautViewModel(astronaut);
                    astronautDataRepository.getAgencyById(astronaut.getAgency().getId(), new Callbacks.AgencyCallback() {
                        @Override
                        public void onAgencyLoaded(Agency agency) {
                            updateAgencyViewModel(agency);
                        }

                        @Override
                        public void onNetworkStateChanged(boolean refreshing) {
                            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                showNetworkLoading(refreshing);
                            }
                        }

                        @Override
                        public void onError(String message, @Nullable Throwable throwable) {
                            if (throwable != null) {
                                Timber.e(throwable);
                            } else {
                                Timber.e(message);
                            }
                        }
                    });
                }
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    showNetworkLoading(refreshing);
                }
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                if (throwable != null) {
                    Timber.e(throwable);
                } else {
                    Timber.e(message);
                }
            }
        });

    }

    private void updateAstronautViews(Astronaut astronaut) {
        this.astronaut = astronaut;
        try {
            bindings.astronautTitle.setText(astronaut.getName());
            bindings.astronautSubtitle.setText(astronaut.getNationality());

            GlideApp.with(this)
                    .load(astronaut.getProfileImage())
                    .thumbnail(GlideApp.with(this)
                            .load(astronaut.getProfileImageThumbnail()))
                    .placeholder(R.drawable.placeholder)
                    .into(bindings.astronautProfileImage);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void updateAgencyViews(Agency agency) {
        this.agency = agency;
    }

    private void updateAstronautViewModel(Astronaut astronaut) {
        viewModel.getAstronaut().setValue(astronaut);
    }

    private void updateAgencyViewModel(Agency agency){
        viewModel.getAgency().setValue(agency);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            bindings.astronautProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(300)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            bindings.astronautProfileImage.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    private void showNetworkLoading(boolean loading) {
        if (loading) {
            showLoading();
        } else {
            hideLoading();
        }
    }

    private void showLoading() {
        Timber.v("Show Loading...");
        bindings.astronautDetailSwipeRefresh.post(() -> bindings.astronautDetailSwipeRefresh.setRefreshing(true));
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        bindings.astronautDetailSwipeRefresh.post(() -> bindings.astronautDetailSwipeRefresh.setRefreshing(false));
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        fetchData(astronautId);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public AstronautProfileFragment profileFragment;
        public AstronautFlightsFragment flightsFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AstronautProfileFragment.newInstance();
                case 1:
                    return AstronautFlightsFragment.newInstance();
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    profileFragment = (AstronautProfileFragment) createdFragment;
                    break;
                case 1:
                    flightsFragment = (AstronautFlightsFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Profile";
                case 1:
                    return "Flights";
            }
            return "";
        }
    }
}
