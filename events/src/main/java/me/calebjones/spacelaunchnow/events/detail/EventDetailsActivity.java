package me.calebjones.spacelaunchnow.events.detail;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.appbar.AppBarLayout;


import jonathanfinerty.once.Amount;
import jonathanfinerty.once.Once;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.base.BaseActivityOld;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.ui.adapters.ExpeditionAdapter;
import me.calebjones.spacelaunchnow.common.ui.adapters.ListAdapter;
import me.calebjones.spacelaunchnow.common.ui.adapters.SpacestationAdapter;
import me.calebjones.spacelaunchnow.common.ui.adapters.UpdateAdapter;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.Event;
import me.calebjones.spacelaunchnow.events.R;
import me.calebjones.spacelaunchnow.events.data.Callbacks;
import me.calebjones.spacelaunchnow.events.data.EventDataRepository;
import me.calebjones.spacelaunchnow.events.databinding.ActivityEventDetailsBinding;
import timber.log.Timber;

public class EventDetailsActivity extends BaseActivityOld implements AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener {



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
    private EventDataRepository eventDataRepository;
    private EventDetailViewModel viewModel;
    private Event event;
    private int mEventId;
    private SimpleDateFormat sdf;
    private int color;
    private LinearLayoutManager linearLayoutManager;
    private ListAdapter adapter;
    private ExpeditionAdapter expeditionAdapter;
    private SpacestationAdapter spacestationAdapter;
    private UpdateAdapter updateAdapter;
    private boolean fromDeepLink = false;
    private static final String ACTION_DEEP_LINK = "deep_link";
    private ActivityEventDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.eventDetailToolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        binding.eventDetailSwipeRefresh.setOnRefreshListener(this);
        eventDataRepository = new EventDataRepository(this, getRealm());

//        appbar.addOnOffsetChangedListener(new CustomOnOffsetChangedListener(getCyanea().getPrimaryDark(), getWindow()));
//        appbar.addOnOffsetChangedListener(this);

        //Grab information from Intent
        Intent mIntent = getIntent();
        String slug = null;
        mEventId = mIntent.getIntExtra("eventId", 0);

        if (mIntent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            Bundle parameters = mIntent.getExtras();
            if (ACTION_DEEP_LINK.equals(mIntent.getAction())) {
                fromDeepLink = true;
                slug = parameters.getString("slug");
            }
        }


        if (binding.eventDetailToolbar != null) {
            setSupportActionBar(binding.eventDetailToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("local_time", true)) {
            sdf = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy");
        } else {
            sdf = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy zzz");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        linearLayoutManager = new LinearLayoutManager(this);
        adapter = new ListAdapter(this, ThemeHelper.isDarkMode(this));
        binding.launchRecyclerView.setLayoutManager(linearLayoutManager);
        binding.launchRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        binding.launchRecyclerView.setAdapter(adapter);

        expeditionAdapter = new ExpeditionAdapter(this, false);
        binding.expeditionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.expeditionRecyclerView.setAdapter(expeditionAdapter);

        spacestationAdapter = new SpacestationAdapter(this);
        binding.spacestationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.spacestationRecyclerView.setAdapter(spacestationAdapter);

        viewModel = ViewModelProviders.of(this).get(EventDetailViewModel.class);
        // update UI
        viewModel.getEvent().observe(this, event -> {
            if (event != null) {
                updateViews(event);
            }
        });
        fetchData(mEventId, slug);

        if (!SupporterHelper.isSupporter() && Once.beenDone("appOpen",
                Amount.moreThan(3))) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.eventAdView.loadAd(adRequest);
            binding.eventAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    binding.eventAdView.setVisibility(View.VISIBLE);
                }

            });
        } else {
            binding.eventAdView.setVisibility(View.GONE);
        }
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        binding.eventDetailSwipeRefresh.setEnabled(enable);
    }

    private void fetchData(int eventId, String slug) {

        eventDataRepository.getEventByIdOrSlug(eventId, slug, new Callbacks.EventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    if (slug != null){
                        mEventId = event.getId();
                    }
                    updateViewModel(event);
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

    private void updateViews(Event event) {
        this.event = event;
        binding.eventTitle.setText(event.getName());

        if (event.getLocation() != null) {
            binding.eventSubtitle.setText(event.getLocation());
        }

        binding.eventCardTitle.setText("Overview");
        binding.eventType.setText(event.getType().getName());
        SimpleDateFormat df = Utils.getSimpleDateTimeFormatForUIWithPrecision(this, event.getDatePrecision());
        df.toLocalizedPattern();
        binding.eventDate.setText(df.format(event.getDate()));
        binding.eventDescription.setText(event.getDescription());
        GlideApp.with(this)
                .load(event.getFeatureImage())
                .placeholder(R.drawable.placeholder)
                .into(binding.eventProfileImage);

        if (event.getLaunches() != null && !event.getLaunches().isEmpty()){
            binding.launchCardRootView.setVisibility(View.VISIBLE);
            adapter.addItems(event.getLaunches());
        } else {
            binding.launchCardRootView.setVisibility(View.GONE);
        }

        if (event.getNewsUrl() != null){
            binding.eventWebButton.setVisibility(View.VISIBLE);
        } else {
            binding.eventWebButton.setVisibility(View.GONE);
        }

        if (event.getVideoUrl() != null){
            binding.eventWatchButton.setVisibility(View.VISIBLE);
        } else {
            binding.eventWatchButton.setVisibility(View.GONE);
        }

        if (event.getExpeditions() != null && !event.getExpeditions().isEmpty()){
            binding.expeditionCardRootView.setVisibility(View.VISIBLE);
            expeditionAdapter.addItems(event.getExpeditions());
        } else {
            binding.expeditionCardRootView.setVisibility(View.GONE);
        }

        if (event.getSpacestations() != null && !event.getSpacestations().isEmpty()){
            binding.spacestationCardRootView.setVisibility(View.VISIBLE);
            spacestationAdapter.addItems(event.getSpacestations());
        } else {
            binding.spacestationCardRootView.setVisibility(View.GONE);
        }

        if (event.getUpdates() != null && !event.getUpdates().isEmpty()){
            updateAdapter = new UpdateAdapter(this);
            binding.updateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.updateRecyclerView.setAdapter(updateAdapter);
            updateAdapter.addItems(event.getUpdates());
            binding.updateCard.setVisibility(View.VISIBLE);
        } else {
            binding.updateCard.setVisibility(View.GONE);
        }

        binding.eventFabShare.setOnClickListener(view -> fabClicked());
        binding.eventWatchButton.setOnClickListener(view -> watchClicked());
        binding.eventWebButton.setOnClickListener(view -> webClicked());
    }

    private void updateViewModel(Event event) {
        adapter.clear();
        expeditionAdapter.clear();
        spacestationAdapter.clear();
        viewModel.getEvent().setValue(event);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            binding.eventProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(300)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            binding.eventProfileImage.animate()
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
        binding.eventDetailSwipeRefresh.post(() -> binding.eventDetailSwipeRefresh.setRefreshing(true));
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        binding.eventDetailSwipeRefresh.post(() -> binding.eventDetailSwipeRefresh.setRefreshing(false));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void fabClicked(){
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle(event.getName())
                .setText("https://spacelaunchnow.me/event/" + event.getId())
                .startChooser();
    }

    void watchClicked(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(event.getVideoUrl()));
        startActivity(i);
    }

    void webClicked(){
        openCustomTab(this, getApplicationContext(), event.getNewsUrl());
    }

    @Override
    public void onRefresh() {
        fetchData(mEventId, null);
    }

    @DeepLink({"https://spacelaunchnow.me/event/{slug}/", "https://spacelaunchnow.me/event/{slug}"})
    public static TaskStackBuilder intentForTaskStackBuilderMethods(Context context) throws ClassNotFoundException {
        Intent detailsIntent = new Intent(context, EventDetailsActivity.class).setAction(ACTION_DEEP_LINK);
        TaskStackBuilder  taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(detailsIntent);
        return taskStackBuilder;
    }
}
