package me.calebjones.spacelaunchnow.ui.main.next;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.realm.RealmResults;
import jonathanfinerty.once.Once;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.BuildConfig;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.content.calendar.CalendarSyncManager;
import me.calebjones.spacelaunchnow.common.content.data.Callbacks;
import me.calebjones.spacelaunchnow.common.prefs.ListPreferences;
import me.calebjones.spacelaunchnow.common.prefs.SwitchPreferences;
import me.calebjones.spacelaunchnow.common.ui.settings.SettingsActivity;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.common.ui.views.SnackbarHandler;
import me.calebjones.spacelaunchnow.content.data.next.NextLaunchDataRepository;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.databinding.ColorRevealBinding;
import me.calebjones.spacelaunchnow.databinding.FragmentUpcomingBinding;
import me.calebjones.spacelaunchnow.ui.debug.DebugActivity;
import me.calebjones.spacelaunchnow.ui.main.MainActivity;
import me.calebjones.spacelaunchnow.common.utils.analytics.Analytics;
import me.calebjones.spacelaunchnow.utils.views.animator.FabExtensionAnimator;
import me.calebjones.spacelaunchnow.widgets.launchcard.LaunchCardCompactManager;
import me.calebjones.spacelaunchnow.widgets.launchcard.LaunchCardCompactWidgetProvider;
import me.calebjones.spacelaunchnow.widgets.launchlist.LaunchListManager;
import me.calebjones.spacelaunchnow.widgets.launchlist.LaunchListWidgetProvider;
import me.calebjones.spacelaunchnow.widgets.wordtimer.LaunchWordTimerManager;
import me.calebjones.spacelaunchnow.widgets.wordtimer.LaunchWordTimerWidgetProvider;
import timber.log.Timber;

public class NextLaunchFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int FAB_MODE_FILTER = 1;
    private static final int FAB_MODE_CLOSE = 2;
    private static final int FAB_MODE_APPLY = 3;

    private View view;
    private CardAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private LinearLayoutManager linearLayoutManager;

    private Menu mMenu;
    private RealmResults<Launch> launchRealms;
    private ListPreferences sharedPreference;
    private SwitchPreferences switchPreferences;
    private Context context;
    private int preferredCount;
    private NextLaunchDataRepository nextLaunchDataRepository;
    private CallBackListener callBackListener;
    private boolean filterViewShowing;
    private boolean switchChanged;
    private FabExtensionAnimator fabExtensionAnimator;
    private MainActivity mainActivity;
    private FragmentUpcomingBinding binding;
    private ColorRevealBinding colorRevealBinding;

    public static NextLaunchFragment newInstance() {

        NextLaunchFragment u = new NextLaunchFragment();
        Bundle b = new Bundle();
        u.setArguments(b);

        return u;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        sharedPreference = ListPreferences.getInstance(context);
        switchPreferences = SwitchPreferences.getInstance(context);
        nextLaunchDataRepository = new NextLaunchDataRepository(context, getRealm());
        mainActivity = (MainActivity) getActivity();
        setScreenName("Next Launch Fragment");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof CallBackListener)
            callBackListener = (CallBackListener) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpcomingBinding.inflate(inflater, container, false);
        colorRevealBinding = binding.colorReveal;

        filterViewShowing = false;

        if (adapter == null) {
            adapter = new CardAdapter(context);
        }

        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);
        setUpSwitches();

        fabExtensionAnimator = new FabExtensionAnimator(binding.fab);
        fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState("Filters",
                ContextCompat.getDrawable(context, R.drawable.ic_notifications_white)),
                true);
        if (!Once.beenDone(Once.THIS_APP_INSTALL, "showFilters")) {
            Once.markDone("showFilters");
            binding.colorReveal.getRoot().setVisibility(View.VISIBLE);
            filterViewShowing = true;
            fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState("Close",
                    ContextCompat.getDrawable(context, R.drawable.ic_close)),
                    true);

            mainActivity.checkHideAd();
            binding.activityMainSwipeRefreshLayout.setEnabled(false);
        }
        binding.fab.setOnClickListener(v -> checkFilter());
        binding.fab.setVisibility(View.GONE);
        if (switchPreferences.getNextFABHidden()) {
            binding.fab.setVisibility(View.GONE);
        } else {
            binding.fab.setVisibility(View.VISIBLE);
        }

        binding.recyclerView.setHasFixedSize(true);

        //If preference is for small card, landscape tablets get three others get two.
        linearLayoutManager = new LinearLayoutManager(context.getApplicationContext(), RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerViewRoot.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int dy = Math.abs(scrollY - oldScrollY);
            if (!isFilterShown()) {
                if (scrollY > 4) {
                    setFabExtended(false);
                } else if (scrollY == 0) {
                    setFabExtended(true);
                }
            }
        });

        /*Set up Pull to refresh*/
        binding.activityMainSwipeRefreshLayout.setOnRefreshListener(this);

        //Enable no data by default
        binding.noLaunches.setVisibility(View.VISIBLE);
        binding.viewMoreLaunches.setVisibility(View.GONE);
        binding.colorReveal.actionNotificationSettings.setOnClickListener(view -> onNotificationSettingsClicked());
        colorRevealBinding.allSwitch.setOnClickListener(view -> all_switch());
        colorRevealBinding.nasaSwitch.setOnClickListener(view -> nasa_switch());
        colorRevealBinding.spacexSwitch.setOnClickListener(view -> spacex_switch());
        colorRevealBinding.roscosmosSwitch.setOnClickListener(view -> roscosmos_switch());
        colorRevealBinding.ulaSwitch.setOnClickListener(view -> ula_switch());
        colorRevealBinding.arianespaceSwitch.setOnClickListener(view -> arianespace_switch());
        colorRevealBinding.chinaSwitch.setOnClickListener(view -> china_switch());
        colorRevealBinding.indiaSwitch.setOnClickListener(view -> india_switch());
        colorRevealBinding.plesSwitch.setOnClickListener(view -> ples_switch());
        colorRevealBinding.vanSwitch.setOnClickListener(view -> van_switch());
        colorRevealBinding.KSCSwitch.setOnClickListener(view -> KSC_switch());
        colorRevealBinding.boSwitch.setOnClickListener(view -> bo_switch());
        colorRevealBinding.rlSwitch.setOnClickListener(view -> rl_switch());
        colorRevealBinding.northropSwitch.setOnClickListener(view -> northrop_switch());
        colorRevealBinding.wallopsSwitch.setOnClickListener(view -> wallops_switch());
        colorRevealBinding.newZealandSwitch.setOnClickListener(view -> new_zealand_switch());
        colorRevealBinding.fgSwitch.setOnClickListener(view -> french_guiana_switch());
        colorRevealBinding.japanSwitch.setOnClickListener(view -> japan_switch());
        colorRevealBinding.texasSwitch.setOnClickListener(view -> texas_switch());
        colorRevealBinding.kodiakSwitch.setOnClickListener(view -> kodiak_switch());

        colorRevealBinding.strictSwitch.setOnClickListener(view -> strict_switch());
        colorRevealBinding.tbdLaunch.setOnClickListener(view -> noGoSwitch());
        colorRevealBinding.miscSwitch.setOnClickListener(view -> miscSwitch());
        colorRevealBinding.persistLastLaunch.setOnClickListener(view -> setPersistLastSwitch());

        colorRevealBinding.allInfo.setOnClickListener(view -> createDialog(
                R.string.follow_all_launches,
                R.string.follow_all_launches_description
        ));
        colorRevealBinding.lastLaunchInfo.setOnClickListener(view -> createDialog(
                R.string.launch_info,
                R.string.launch_info_description
        ));
        colorRevealBinding.otherInfo.setOnClickListener(view -> createDialog(
                R.string.strict_matching_title,
                R.string.strict_matching
        ));
        colorRevealBinding.tbdInfo.setOnClickListener(view -> createDialog(
                R.string.no_go,
                R.string.no_go_description));

//        @OnClick({R.id.view_more_launches, R.id.view_more_launches2})
        binding.viewMoreLaunches.setOnClickListener(view -> onViewClicked());
        binding.viewMoreLaunches2.setOnClickListener(view -> onViewClicked());

        return binding.getRoot();
    }

    private void setFabExtended(boolean extended) {
        fabExtensionAnimator.setExtended(extended);
    }

    public boolean isFilterShown() {
        return filterViewShowing;
    }

    @Override
    public void onStart() {
        Timber.v("onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setLayoutManager(int size) {
        if (!isDetached() && isAdded()) {
            linearLayoutManager = new LinearLayoutManager(context.getApplicationContext(),
                    RecyclerView.VERTICAL, false);
            binding.recyclerView.setLayoutManager(linearLayoutManager);
            binding.recyclerView.setAdapter(adapter);
        } else if (isDetached()) {
            Timber.v("View is detached.");
        }
    }

    private void setUpSwitches() {
        colorRevealBinding.allSwitch.setChecked(switchPreferences.getAllSwitch());
        colorRevealBinding.nasaSwitch.setChecked(switchPreferences.getSwitchNasa());
        colorRevealBinding.spacexSwitch.setChecked(switchPreferences.getSwitchSpaceX());
        colorRevealBinding.roscosmosSwitch.setChecked(switchPreferences.getSwitchRoscosmos());
        colorRevealBinding.ulaSwitch.setChecked(switchPreferences.getSwitchULA());
        colorRevealBinding.arianespaceSwitch.setChecked(switchPreferences.getSwitchArianespace());
        colorRevealBinding.chinaSwitch.setChecked(switchPreferences.getSwitchCASC());
        colorRevealBinding.indiaSwitch.setChecked(switchPreferences.getSwitchISRO());
        colorRevealBinding.plesSwitch.setChecked(switchPreferences.getSwitchRussia());
        colorRevealBinding.vanSwitch.setChecked(switchPreferences.getSwitchVan());
        colorRevealBinding.KSCSwitch.setChecked(switchPreferences.getSwitchKSC());
        colorRevealBinding.boSwitch.setChecked(switchPreferences.getSwitchBO());
        colorRevealBinding.rlSwitch.setChecked(switchPreferences.getSwitchRL());
        colorRevealBinding.tbdLaunch.setChecked(switchPreferences.getTBDSwitch());
        colorRevealBinding.northropSwitch.setChecked(switchPreferences.getSwitchNorthrop());
        colorRevealBinding.wallopsSwitch.setChecked(switchPreferences.getSwitchWallops());
        colorRevealBinding.newZealandSwitch.setChecked(switchPreferences.getSwitchNZ());
        colorRevealBinding.fgSwitch.setChecked(switchPreferences.getSwitchFG());
        colorRevealBinding.japanSwitch.setChecked(switchPreferences.getSwitchJapan());
        colorRevealBinding.persistLastLaunch.setChecked(switchPreferences.getPersistSwitch());
        colorRevealBinding.texasSwitch.setChecked(switchPreferences.getSwitchTexas());
        colorRevealBinding.kodiakSwitch.setChecked(switchPreferences.getSwitchKodiak());
        colorRevealBinding.miscSwitch.setChecked(switchPreferences.getSwitchOtherLocations());
        colorRevealBinding.strictSwitch.setChecked(switchPreferences.getSwitchStrictMatching());
    }

    private void hideView() {
        try {
            // get the center for the clipping circle
            int x = (int) (binding.fab.getX() + binding.fab.getWidth() / 2);
            int y = (int) (binding.fab.getY() + binding.fab.getHeight() / 2);

            // get the initial radius for the clipping circle
            int initialRadius = Math.max(
                    binding.colorReveal.getRoot().getWidth(),
                    binding.colorReveal.getRoot().getHeight());

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(binding.colorReveal.getRoot(), x, y, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    binding.colorReveal.getRoot();
                    binding.colorReveal.getRoot().setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();


            mainActivity.checkShowAd();
            binding.activityMainSwipeRefreshLayout.setEnabled(true);
        } catch (IllegalStateException exception) {
            Timber.e(exception);
        }
    }

    private void showView() {
        try {
            // get the center for the clipping circle
            int x = (int) (binding.fab.getX() + binding.fab.getWidth() / 2);
            int y = (int) (binding.fab.getY() + binding.fab.getHeight() / 2);

            // get the final radius for the clipping circle
            int finalRadius = Math.max(binding.colorReveal.getRoot().getWidth(), binding.colorReveal.getRoot().getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(binding.colorReveal.getRoot(), x, y, 0, finalRadius);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    super.onAnimationEnd(animation);
                }
            });

            binding.colorReveal.getRoot().setVisibility(View.VISIBLE);
            anim.start();

            mainActivity.checkHideAd();
            binding.activityMainSwipeRefreshLayout.setEnabled(false);
        } catch (IllegalStateException exception) {
            Timber.e(exception);
        }
    }

    public void fetchData(boolean forceRefresh) {
        Timber.v("Sending GET_UP_LAUNCHES");
        preferredCount = 20;
        nextLaunchDataRepository.getNextUpcomingLaunches(preferredCount, forceRefresh, new Callbacks.NextLaunchesCallback() {
            @Override
            public void onLaunchesLoaded(RealmResults<Launch> launches) {
                try {
                    updateAdapter(launches);
                    if (switchPreferences.getCalendarStatus()) {
                        CalendarSyncManager calendarSyncManager = new CalendarSyncManager(context);
                        calendarSyncManager.resyncAllEvents();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
                showNetworkLoading(refreshing);
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                if (throwable != null) {
                    Timber.e(throwable);
                } else {
                    Timber.e(message);
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        SnackbarHandler.showErrorSnackbar(context, binding.coordinatorLayout, message);
                    }
                }
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    SnackbarHandler.showErrorSnackbar(context, binding.coordinatorLayout, message);
                }
            }
        });
    }

    private void updateAdapter(RealmResults<Launch> launches) {
        adapter.clear();
        preferredCount = 5;
        if (launches.size() >= preferredCount) {
            binding.noLaunches.setVisibility(View.GONE);
            binding.viewMoreLaunches.setVisibility(View.VISIBLE);
            setLayoutManager(preferredCount);
            adapter.addItems(launches.subList(0, preferredCount));
            adapter.notifyDataSetChanged();

        } else if (!launches.isEmpty()) {
            binding.noLaunches.setVisibility(View.GONE);
            binding.viewMoreLaunches.setVisibility(View.VISIBLE);
            setLayoutManager(preferredCount);
            adapter.addItems(launches);
            adapter.notifyDataSetChanged();

        } else {
            binding.noLaunches.setVisibility(View.VISIBLE);
            binding.viewMoreLaunches.setVisibility(View.GONE);
            if (adapter != null) {
                adapter.clear();
            }
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
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            binding.activityMainSwipeRefreshLayout.post(() -> {
                binding.activityMainSwipeRefreshLayout.setRefreshing(true);
            });
        }
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            binding.activityMainSwipeRefreshLayout.post(() -> {
                binding.activityMainSwipeRefreshLayout.setRefreshing(false);
            });
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.v("onResume");
        setTitle();
        if (adapter.getItemCount() == 0) {
            fetchData(false);
        } else {
            binding.noLaunches.setVisibility(View.GONE);
            binding.viewMoreLaunches.setVisibility(View.VISIBLE);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("SHOW_FILTERS")) {
                if (!filterViewShowing) {
                    new Handler().postDelayed(this::checkFilter, 500);
                }
                bundle.clear();
            }
        }

        if (filterViewShowing) {
            mainActivity.checkHideAd();
        }
    }

    @Override
    public void onRefresh() {
        fetchData(true);
    }

    private void setTitle() {
        mainActivity.setActionBarTitle("Space Launch Now");
    }

    //Currently only used to debug
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) {
            menu.clear();
            inflater.inflate(R.menu.debug_menu, menu);
            mMenu = menu;
        } else {
            menu.clear();
            inflater.inflate(R.menu.next_menu, menu);
            mMenu = menu;
        }

        if (switchPreferences.getNextFABHidden()) {
            MenuItem item = menu.findItem(R.id.action_FAB);
            item.setTitle("Show FAB");
        }

        if (SupporterHelper.isSupporter()) {
            mMenu.removeItem(R.id.action_supporter);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.debug_menu) {
            Intent debugIntent = new Intent(getActivity(), DebugActivity.class);
            startActivity(debugIntent);

        } else if (id == R.id.action_alert) {
            checkFilter();
        } else if (id == R.id.action_FAB) {
            switchPreferences.setNextFABHidden(!switchPreferences.getNextFABHidden());
            if (switchPreferences.getNextFABHidden()) {
                item.setTitle("Show FAB");
                if (switchPreferences.getNextFABHidden()) {
                    binding.fab.setVisibility(View.GONE);
                }
            } else {
                item.setTitle("Hide FAB");
                if (!switchPreferences.getNextFABHidden()) {
                    binding.fab.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    public void checkFilter() {

        if (!filterViewShowing) {
            Analytics.getInstance().sendButtonClicked("Show Launch filters.");
            switchChanged = false;
            filterViewShowing = true;
            binding.fab.setVisibility(View.VISIBLE);
            binding.activityMainSwipeRefreshLayout.setEnabled(false);
            fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState("Close", ContextCompat.getDrawable(context, R.drawable.ic_close)), true);
            showView();
        } else {
            Analytics.getInstance().sendButtonClicked("Hide Launch filters.");
            filterViewShowing = false;
            if (switchPreferences.getNextFABHidden()) {
                binding.fab.setVisibility(View.GONE);
            }
            fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState("Filters", ContextCompat.getDrawable(context, R.drawable.ic_notifications_white)), true);

            hideView();
            if (switchChanged) {
                LaunchCardCompactManager launchCardCompactManager = new LaunchCardCompactManager(context);
                LaunchWordTimerManager launchWordTimerManager = new LaunchWordTimerManager(context);
                LaunchListManager launchListManager = new LaunchListManager(context);

                int cardIds[] = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(new ComponentName(context,
                                LaunchCardCompactWidgetProvider.class));

                for (int id : cardIds) {
                    launchCardCompactManager.updateAppWidget(id);
                }

                int timerIds[] = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(new ComponentName(context,
                                LaunchWordTimerWidgetProvider.class));

                for (int id : timerIds) {
                    launchWordTimerManager.updateAppWidget(id);
                }

                int listIds[] = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(new ComponentName(context,
                                LaunchListWidgetProvider.class));

                for (int id : listIds) {
                    launchListManager.updateAppWidget(id);
                }
                binding.activityMainSwipeRefreshLayout.setEnabled(true);
                fetchData(true);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.v("onDestroyView");
        callBackListener = null;
    }


    public void nasa_switch() {
        confirm();
        switchPreferences.setSwitchNasa(!switchPreferences.getSwitchNasa());
        checkAll();
    }

    public void spacex_switch() {
        confirm();
        switchPreferences.setSwitchSpaceX(!switchPreferences.getSwitchSpaceX());
        checkAll();
    }

    public void roscosmos_switch() {
        confirm();
        switchPreferences.setSwitchRoscosmos(!switchPreferences.getSwitchRoscosmos());
        checkAll();
    }

    public void wallops_switch() {
        confirm();
        switchPreferences.setSwitchWallops(!switchPreferences.getSwitchWallops());
        checkAll();
    }

    public void new_zealand_switch() {
        confirm();
        switchPreferences.setSwitchNZ(!switchPreferences.getSwitchNZ());
        checkAll();
    }

    public void french_guiana_switch() {
        confirm();
        switchPreferences.setSwitchFG(!switchPreferences.getSwitchFG());
        checkAll();
    }


    public void ula_switch() {
        confirm();
        switchPreferences.setSwitchULA(!switchPreferences.getSwitchULA());
        checkAll();
    }


    public void arianespace_switch() {
        confirm();
        switchPreferences.setSwitchArianespace(!switchPreferences.getSwitchArianespace());
        checkAll();
    }


    public void china_switch() {
        confirm();
        switchPreferences.setSwitchCASC(!switchPreferences.getSwitchCASC());
        checkAll();
    }


    public void india_switch() {
        confirm();
        switchPreferences.setSwitchISRO(!switchPreferences.getSwitchISRO());
        checkAll();
    }


    public void KSC_switch() {
        confirm();
        switchPreferences.setSwitchKSC(!switchPreferences.getSwitchKSC());
        checkAll();
    }


    public void ples_switch() {
        confirm();
        switchPreferences.setSwitchRussia(binding.colorReveal.plesSwitch.isChecked());
        checkAll();
    }


    public void van_switch() {
        confirm();
        switchPreferences.setSwitchVan(!switchPreferences.getSwitchVan());
        checkAll();
    }


    public void bo_switch() {
        confirm();
        switchPreferences.setSwitchBO(!switchPreferences.getSwitchBO());
        checkAll();
    }


    public void rl_switch() {
        confirm();
        switchPreferences.setSwitchRL(!switchPreferences.getSwitchRL());
        checkAll();
    }


    public void northrop_switch() {
        confirm();
        switchPreferences.setSwitchNorthrop(!switchPreferences.getSwitchNorthrop());
        checkAll();
    }


    public void japan_switch() {
        confirm();
        switchPreferences.setSwitchJapan(!switchPreferences.getSwitchJapan());
        checkAll();
    }


    public void kodiak_switch() {
        confirm();
        switchPreferences.setSwitchKodiak(!switchPreferences.getSwitchKodiak());
        checkAll();
    }
    

    public void texas_switch() {
        confirm();
        switchPreferences.setSwitchTexas(!switchPreferences.getSwitchTexas());
        checkAll();
    }


    public void miscSwitch() {
        confirm();
        switchPreferences.setSwitchOtherLocation(!switchPreferences.getSwitchOtherLocations());
        checkAll();
    }


    public void strict_switch() {
        confirm();
        switchPreferences.setSwitchStrictMatching(!switchPreferences.getSwitchStrictMatching());
        setUpSwitches();
    }


    public void all_switch() {
        confirm();
        switchPreferences.setAllSwitch(!switchPreferences.getAllSwitch());
        setUpSwitches();
    }


    public void noGoSwitch() {
        confirm();
        switchPreferences.setNoGoSwitch(binding.colorReveal.tbdLaunch.isChecked());
    }


    public void setPersistLastSwitch() {
        confirm();
        switchPreferences.setPersistLastSwitch(binding.colorReveal.persistLastLaunch.isChecked());
    }


    public void onNotificationSettingsClicked() {
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
    }

    public void createDialog(int titleRes, int contentRes) {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
        dialog.positiveText("Ok");
        dialog.title(titleRes).content(contentRes).show();
    }

    private void confirm() {
        if (!switchChanged) {
            fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState("Apply", ContextCompat.getDrawable(context, R.drawable.ic_check)), true);
            fabExtensionAnimator.setExtended(true);
        }
        switchChanged = true;
    }

    private void checkAll() {
        if (switchPreferences.getAllSwitch()) {
            switchPreferences.setAllSwitch(false);
            binding.colorReveal.allSwitch.setChecked(false);
        }
    }

    public void onViewClicked() {
        callBackListener.onNavigateToLaunches();
    }

    public interface CallBackListener {
        void onNavigateToLaunches();// pass any parameter in your onCallBack which you want to return
    }
}


