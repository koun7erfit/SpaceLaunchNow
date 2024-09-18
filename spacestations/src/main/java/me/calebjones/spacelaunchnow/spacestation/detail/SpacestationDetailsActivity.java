package me.calebjones.spacelaunchnow.spacestation.detail;

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
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.common.base.BaseActivityOld;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.spacestation.R;
import me.calebjones.spacelaunchnow.spacestation.data.Callbacks;
import me.calebjones.spacelaunchnow.spacestation.data.SpacestationDataRepository;
import me.calebjones.spacelaunchnow.spacestation.databinding.ActivitySpacestationDetailsBinding;
import me.calebjones.spacelaunchnow.spacestation.detail.fragments.SpacestationDockedVehiclesFragment;
import me.calebjones.spacelaunchnow.spacestation.detail.fragments.expeditions.SpacestationExpeditionFragment;
import me.calebjones.spacelaunchnow.spacestation.detail.fragments.detail.SpacestationDetailFragment;
import timber.log.Timber;

public class SpacestationDetailsActivity extends BaseActivityOld implements AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener {

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
    private SpacestationDataRepository spacestationDataRepository;
    private SpacestationDetailViewModel viewModel;
    private int spacestationId;
    private ActivitySpacestationDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spacestation_details);
        binding = ActivitySpacestationDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.spacestationDetailToolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        binding.spacestationDetailSwipeRefresh.setOnRefreshListener(this);
        binding.spacestationDetailViewpager.setAdapter(mSectionsPagerAdapter);
        binding.spacestationDetailViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.spacestationDetailTabs));
        binding.spacestationDetailViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled( int position, float v, int i1 ) {
            }

            @Override
            public void onPageSelected( int position ) {
            }

            @Override
            public void onPageScrollStateChanged( int state ) {
                enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
            }
        } );
        binding.spacestationDetailTabs.addTab(binding.spacestationDetailTabs.newTab().setText(getString(R.string.details)));
        binding.spacestationDetailTabs.addTab(binding.spacestationDetailTabs.newTab().setText(getString(R.string.expeditions)));
        binding.spacestationDetailTabs.addTab(binding.spacestationDetailTabs.newTab().setText(getString(R.string.docked_alt)));
        binding.spacestationDetailTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.spacestationDetailViewpager));
//        tabs.setTabTextColors(Utils.getSecondaryTitleTextColor(getCyanea().getPrimary()),
//                Utils.getTitleTextColor(getCyanea().getPrimary()));
//        tabs.setBackgroundColor(getCyanea().getPrimary());
        spacestationDataRepository = new SpacestationDataRepository(this, getRealm());

//        appbar.addOnOffsetChangedListener(new CustomOnOffsetChangedListener(getCyanea().getPrimaryDark(), getWindow()));
//        appbar.addOnOffsetChangedListener(this);

        //Grab information from Intent
        Intent mIntent = getIntent();
        spacestationId = mIntent.getIntExtra("spacestationId", 0);

        fetchData(spacestationId);

        setSupportActionBar(binding.spacestationDetailToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        viewModel = ViewModelProviders.of(this).get(SpacestationDetailViewModel.class);
        // update UI
        viewModel.getSpacestation().observe(this, spacestation -> {
            if (spacestation != null) {
                updateViews(spacestation);
            }
        });

        if (!SupporterHelper.isSupporter() && Once.beenDone("appOpen",
                Amount.moreThan(3))) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.spacestationAdView.loadAd(adRequest);
            binding.spacestationAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    binding.spacestationAdView.setVisibility(View.VISIBLE);
                }

            });
        } else {
            binding.spacestationAdView.setVisibility(View.GONE);
        }
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        binding.spacestationDetailSwipeRefresh.setEnabled(enable);
    }

    private void fetchData(int spacestationId) {
        spacestationDataRepository.getSpacestationById(spacestationId, new Callbacks.SpacestationCallback() {
            @Override
            public void onSpacestationLoaded(Spacestation spacestation) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    updateViewModel(spacestation);
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

    private void updateViews(Spacestation spacestation) {
        try {
            binding.spacestationTitle.setText(spacestation.getName());
            binding.spacestationSubtitle.setText(spacestation.getType().getName());
            GlideApp.with(this)
                    .load(spacestation.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(binding.spacestationProfileImage);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    private void updateViewModel(Spacestation spacestation) {

        viewModel.getSpacestation().setValue(spacestation);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            binding.spacestationProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(300)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            binding.spacestationProfileImage.animate()
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
        binding.spacestationDetailSwipeRefresh.post(() -> binding.spacestationDetailSwipeRefresh.setRefreshing(true));
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        binding.spacestationDetailSwipeRefresh.post(() -> binding.spacestationDetailSwipeRefresh.setRefreshing(false));
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
        fetchData(spacestationId);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SpacestationDetailFragment detailFragment;
        public SpacestationExpeditionFragment expeditionFragment;
        public SpacestationDockedVehiclesFragment dockingEventFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SpacestationDetailFragment.newInstance();
                case 1:
                    return SpacestationExpeditionFragment.newInstance();
                case 2:
                    return SpacestationDockedVehiclesFragment.newInstance();
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    detailFragment = (SpacestationDetailFragment) createdFragment;
                    break;
                case 1:
                    expeditionFragment = (SpacestationExpeditionFragment) createdFragment;
                    break;
                case 2:
                    dockingEventFragment = (SpacestationDockedVehiclesFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Details";
                case 1:
                    return "Expeditions";
                case 2:
                    return "Docked Vehicles";
            }
            return "";
        }
    }
}
