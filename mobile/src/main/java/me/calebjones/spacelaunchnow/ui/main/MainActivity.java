package me.calebjones.spacelaunchnow.ui.main;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.deeplinkdispatch.DeepLink;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.michaelflisar.gdprdialog.GDPR;
import com.michaelflisar.gdprdialog.GDPRConsent;
import com.michaelflisar.gdprdialog.GDPRConsentState;
import com.michaelflisar.gdprdialog.GDPRDefinitions;
import com.michaelflisar.gdprdialog.GDPRLocation;
import com.michaelflisar.gdprdialog.GDPRSetup;
import com.michaelflisar.gdprdialog.helper.GDPRPreperationData;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.util.concurrent.TimeUnit;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import jonathanfinerty.once.Amount;
import jonathanfinerty.once.Once;
import me.calebjones.spacelaunchnow.BuildConfig;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.base.BaseActivity;
import me.calebjones.spacelaunchnow.common.ui.settings.SettingsActivity;
import me.calebjones.spacelaunchnow.databinding.ActivityMainBinding;
import me.calebjones.spacelaunchnow.events.list.EventListFragment;
import me.calebjones.spacelaunchnow.spacestation.SpacestationListFragment;
import me.calebjones.spacelaunchnow.common.ui.generate.Rate;
import me.calebjones.spacelaunchnow.common.prefs.ListPreferences;
import me.calebjones.spacelaunchnow.common.prefs.SwitchPreferences;
import me.calebjones.spacelaunchnow.news.ui.NewsViewPager;
import me.calebjones.spacelaunchnow.starship.ui.StarshipViewPager;
import me.calebjones.spacelaunchnow.ui.changelog.ChangelogActivity;
import me.calebjones.spacelaunchnow.ui.intro.OnboardingActivity;
import me.calebjones.spacelaunchnow.ui.main.launches.LaunchesViewPager;
import me.calebjones.spacelaunchnow.ui.main.next.NextLaunchFragment;
import me.calebjones.spacelaunchnow.ui.main.vehicles.VehiclesViewPager;
import me.calebjones.spacelaunchnow.ui.AboutActivity;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterActivity;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.utils.Utils;
import me.calebjones.spacelaunchnow.utils.customtab.CustomTabActivityHelper;
import me.calebjones.spacelaunchnow.astronauts.list.AstronautListFragment;
import timber.log.Timber;

@DeepLink({
        "https://spacelaunchnow.me/",
        "https://spacelaunchnow.me/launch/upcoming",
        "https://spacelaunchnow.me/launch/upcoming/",
        "https://spacelaunchnow.me/launch/previous",
        "https://spacelaunchnow.me/launch/previous/"
})
public class MainActivity extends BaseActivity implements GDPR.IGDPRCallback, NextLaunchFragment.CallBackListener {

    private static final String NAV_ITEM_ID = "navItemId";
    private static ListPreferences listPreferences;

    private LaunchesViewPager mlaunchesViewPager;
    private NextLaunchFragment mUpcomingFragment;
    private NewsViewPager mNewsViewpagerFragment;
    private VehiclesViewPager mVehicleViewPager;
    private SpacestationListFragment mSpacestationListFragment;
    private EventListFragment mEventsFragment;
    private AstronautListFragment mAstronautsListFragment;
    private StarshipViewPager mStarshipDashboardFragment;
    private Drawer drawer = null;
    private SharedPreferences sharedPref;
    private SwitchPreferences switchPreferences;
    private CustomTabActivityHelper customTabActivityHelper;
    private Context context;
    private boolean adviewEnabled = false;
    private Rate rate;

    private static final String HOME_TAG = "HOME_TAG";
    private static final String LAUNCHES_TAG = "LAUNCH_VIEWPAGER";
    private static final String NEWS_TAG = "NEWS_FRAGMENT_VIEWPAGER";
    private static final String VEHICLE_TAG = "VEHICLE_VIEWPAGER";
    private static final String ISS_TAG = "ISS_TAG";
    private static final String ASTRONAUT_TAG = "ASTRONAUT_TAG";
    private static final String STARSHIP_TAG = "STARSHIP_TAG";
    private static final String EVENTS_TAG = "EVENTS_TAG";
    private static final String ASTRONAUT_DETAIL_TAG = "ASTRONAUT_DETAIL_TAG";

    static final int SHOW_INTRO = 1;

    private int mNavItemId;
    private Snackbar snackbar;
    private String action;
    private boolean showFilter = false;
    private String newsUrl;
    private boolean allowsPersonalAds = false;
    private boolean allowAds = false;
    private int adShowCount = 0;
    private ActivityMainBinding binding;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate");
        setTheme(R.style.BaseAppTheme);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (!Once.beenDone(Once.THIS_APP_INSTALL, "showTutorial")) {
            showFilter = true;
            startActivityForResult(new Intent(this, OnboardingActivity.class), SHOW_INTRO);
        } else if (!Once.beenDone("show2024dialog") && Once.beenDone("appOpen", Amount.moreThan(5))) {
            Once.markDone("show2024dialog");
            if (!SupporterHelper.is2024Supporter()) {
                becomeSupporter();
            }
        } else if (!Once.beenDone("showNotificationPermission")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                int permissionState = ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.POST_NOTIFICATIONS);
                // If the permission is not granted, request it.
                if (permissionState == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            }
            Once.markDone("showNotificationPermission");
        }

        // Get intent, action and MIME type
        Intent intent = getIntent();
        action = intent.getAction();

        Timber.d("Creating Preference instances.");
        listPreferences = ListPreferences.getInstance(this.context);
        switchPreferences = SwitchPreferences.getInstance(this.context);


        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.context = getApplicationContext();
        customTabActivityHelper = new CustomTabActivityHelper();


        Timber.d("Binding views.");


        adviewEnabled = false;
        hideAd();

        setupWindowAnimations();

        setSupportActionBar(binding.mainToolbar);
        // load saved navigation state if present

        Timber.d("Building account header.");
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(false)
                .withAccountHeader(R.layout.header_view)
                .withSavedInstance(savedInstanceState)
                .build();

        Timber.d("Building rate builder.");
        rate = new Rate.Builder(context)
                .setTriggerCount(10)
                .setMinimumInstallTime(TimeUnit.DAYS.toMillis(3))
                .setMessage(R.string.please_rate_short)
                .setFeedbackAction(() -> showFeedback())
                .setSnackBarParent(binding.coordinatorLayout)
                .build();

        Timber.d("Building DrawerBuilder");
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(binding.mainToolbar)
                .withHasStableIds(true)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.home)
                                .withIcon(GoogleMaterial.Icon.gmd_home)
                                .withIdentifier(R.id.menu_home)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withName(R.string.launches)
                                .withIcon(getResources().getDrawable(R.drawable.ic_satellite_white))
                                .withIdentifier(R.id.menu_launches)
                                .withIconTintingEnabled(true)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withName(R.string.news_and_events)
                                .withIcon(GoogleMaterial.Icon.gmd_assignment)
                                .withIdentifier(R.id.menu_news)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withName(R.string.vehicles)
                                .withIcon(getResources().getDrawable(R.drawable.ic_rocket))
                                .withIconTintingEnabled(true)
                                .withIdentifier(R.id.menu_vehicle)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_spacestation_logo, null))
                                .withName(getString(R.string.spacestations))
                                .withIdentifier(R.id.menu_iss)
                                .withIconTintingEnabled(true)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withIcon(FontAwesome.Icon.faw_user_astronaut)
                                .withName(getString(R.string.astronauts))
                                .withIdentifier(R.id.menu_astronauts)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withIcon(this.getDrawable(R.drawable.ic_starship))
                                .withName(getString(R.string.starship))
                                .withIdentifier(R.id.menu_starship)
                                .withSelectable(true),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.about)
                                .withIcon(GoogleMaterial.Icon.gmd_info)
                                .withIdentifier(R.id.about)
                                .withSelectable(false),
                        new PrimaryDrawerItem()
                                .withIcon(FontAwesome.Icon.faw_patreon)
                                .withName("Patreon")
                                .withIdentifier(R.id.menu_patreon)
                                .withSelectable(false),
                        new PrimaryDrawerItem()
                                .withIcon(CommunityMaterial.Icon.cmd_discord)
                                .withName(R.string.discord)
                                .withIdentifier(R.id.menu_discord)
                                .withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                                .withIdentifier(R.id.menu_settings)
                                .withSelectable(false)
                ).withOnDrawerItemClickListener((v, position, drawerItem) -> {
                    if (drawerItem != null) {
                        if (mNavItemId != (int) drawerItem.getIdentifier()) {
                            navigate((int) drawerItem.getIdentifier());
                        }
                    }
                    return false;
                }).build();

        Timber.d("If not Supporter add footer else thank the user!");
        if (!SupporterHelper.isSupporter()) {
            FirebaseAnalytics.getInstance(this).setUserProperty("supporter", "false");
            Timber.d("Adding footer.");
            drawer.addStickyFooterItem(
                    new PrimaryDrawerItem().withName(R.string.supporter_title)
                            .withDescription(R.string.supporter_main)
                            .withIcon(FontAwesome.Icon.faw_dollar_sign)
                            .withIdentifier(R.id.menu_support)
                            .withSelectable(false));
        } else if (SupporterHelper.is2022Supporter()) {
            FirebaseAnalytics.getInstance(this).setUserProperty("supporter", "true");
            Timber.d("Show thanks for support.");
            drawer.addStickyFooterItem(
                    new PrimaryDrawerItem().withName(R.string.thank_you_for_support)
                            .withIcon(GoogleMaterial.Icon.gmd_mood)
                            .withIdentifier(R.id.menu_support)
                            .withSelectable(false));
        } else if (SupporterHelper.isSupporter()) {
            FirebaseAnalytics.getInstance(this).setUserProperty("supporter", "true");
            Timber.d("Show thanks for support.");
            drawer.addStickyFooterItem(
                    new PrimaryDrawerItem().withName(R.string.thank_you_for_support)
                            .withDescription(R.string.supporter_again_footer)
                            .withIcon(GoogleMaterial.Icon.gmd_mood)
                            .withIdentifier(R.id.menu_support)
                            .withSelectable(false));
        }

        binding.navigationView
                .addItem(new BottomNavigationItem(R.drawable.ic_favorite, getString(R.string.favorites))
                        .setActiveColorResource(R.color.md_red_700))
                .addItem(new BottomNavigationItem(R.drawable.ic_satellite_white, getString(R.string.launches))
                        .setActiveColorResource(R.color.primary))
                .addItem(new BottomNavigationItem(R.drawable.ic_assignment_white, getString(R.string.news))
                        .setActiveColorResource(R.color.material_color_deep_orange_500))
                .addItem(new BottomNavigationItem(R.drawable.ic_rocket, getString(R.string.vehicles))
                        .setActiveColorResource(R.color.material_color_blue_grey_500))
                .setFirstSelectedPosition(0)
                .initialise();

        if (null == savedInstanceState) {
            mNavItemId = R.id.menu_home;
            navigate(mNavItemId);
        } else if (intent.getExtras() != null && intent.getExtras().containsKey("newsUrl")) {
            newsUrl = intent.getExtras().getString("newsUrl");
            mNavItemId = R.id.menu_news;
            navigate(mNavItemId);
            intent.getExtras().clear();
        } else if ("SHOW_FILTERS".equals(action)) {
            getIntent().setAction("");
            showFilter = true;
            mNavItemId = R.id.menu_home;
        } else if (getIntent().getBooleanExtra("SHOW_EVENTS", false)) {
            mNavItemId = R.id.menu_news;
            navigate(mNavItemId);
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }
    }

    public void removeAppBarElevation() {
        getSupportActionBar().setElevation(0);
        binding.appBarLayout.setElevation(0);
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(binding.appBarLayout, "elevation", 0));
        binding.appBarLayout.setStateListAnimator(stateListAnimator);
    }

    public void addAppBarElevation() {
        getSupportActionBar().setElevation(8);
        binding.appBarLayout.setElevation(8);
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(binding.appBarLayout, "elevation", 8));
        binding.appBarLayout.setStateListAnimator(stateListAnimator);
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setEnterTransition(slide);
        getWindow().setReturnTransition(slide);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("MainActivity onStart!");
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    public void onStop() {
        Timber.v("MainActivity onStop!");
        customTabActivityHelper.unbindCustomTabsService(this);
        super.onStop();
    }

    public void showWhatsNew() {
        Intent whatsNew = new Intent(this, ChangelogActivity.class);
        startActivity(whatsNew);
    }

    public void onResume() {
        super.onResume();
        Timber.v("onResume");

        if ("SHOW_FILTERS".equals(action)) {
            getIntent().setAction("");
            showFilter = true;
            mNavItemId = R.id.menu_home;
            navigate(R.id.menu_home);
        } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("newsUrl")) {
            newsUrl = getIntent().getExtras().getString("newsUrl");
            mNavItemId = R.id.menu_news;
            navigate(R.id.menu_news);
            getIntent().getExtras().clear();
        } else if (getIntent().getBooleanExtra("SHOW_EVENTS", false)) {
            navigate(R.id.menu_events);
        } else {
            Timber.d("Navigate to initial fragment.");
            navigate(mNavItemId);
        }

        if (rate != null) {
            rate.count();
        }

        if (BuildConfig.DEBUG) {
            showRemainingCount();
        }

        if (!rate.isShown()) {
            if (!Once.beenDone(Once.THIS_APP_VERSION, "showChangelog")
                    && Once.beenDone("appOpen", Amount.moreThan(2))) {
                Once.markDone("showChangelog");
                final Handler handler = new Handler();
                handler.postDelayed(() -> showChangelogSnackBar(), 1000);

            } else if (!SupporterHelper.isSupporter()) {
                if (!Once.beenDone("userCheckedSupporter")) {
                    if (Once.beenDone("appOpen", Amount.exactly(3))) {
                        if (!Once.beenDone("showRemoveAdThree") && !SupporterHelper.isSupporter()) {
                            Once.markDone("showRemoveAdThree");
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> showRemoveAd(), 5000);
                        }
                    } else if (Once.beenDone("appOpen", Amount.moreThan(10))) {
                        if (!Once.beenDone("showDiscord") && !Once.beenDone("discordResponse")) {
                            Once.markDone("showDiscord");
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> showDiscord(), 1000);
                        }
                    } else if (Once.beenDone("appOpen", Amount.moreThan(7))
                            && Once.beenDone("appOpen", Amount.lessThan(13))) {
                        if (!Once.beenDone("showRemoveAdSeven") && !SupporterHelper.isSupporter()) {
                            Once.markDone("showRemoveAdSeven");
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> showRemoveAd(), 5000);
                        }
                    } else if (Once.beenDone("appOpen", Amount.exactly(14))) {
                        if (!Once.beenDone("showRemoveAd14") && !SupporterHelper.isSupporter()) {
                            Once.markDone("showRemoveAd14");
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> showRemoveAd(), 5000);
                        }
                    }
                }
            }
        }
        if (getSharedPreferences("theme_changed", 0).getBoolean("recreate", false)) {
            SharedPreferences.Editor editor = getSharedPreferences("theme_changed", 0).edit();
            editor.putBoolean("recreate", false);
            editor.apply();
            recreate();
        }

        // show GDPR Dialog if necessary, the library takes care about if and how to show it
        GDPR.getInstance().checkIfNeedsToBeShown(this, getGDPRSetup());
        configureAdState(GDPR.getInstance().getConsentState());

        if (Once.beenDone(Once.THIS_APP_INSTALL, "showTutorial")) {
            if (!Once.beenDone("showNotificationPermission")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    int permissionState = ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.POST_NOTIFICATIONS);
                    // If the permission is not granted, request it.
                    if (permissionState == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                    }
                }
                Once.markDone("showNotificationPermission");
            }
        }
    }

    private void showRemoveAd() {
        snackbar = Snackbar
                .make(binding.coordinatorLayout, R.string.upgrade_pro, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setAction(getString(R.string.show_me), view -> {
                    Once.markDone("userCheckedSupporter");
                    startActivity(new Intent(context, SupporterActivity.class));
                });
        snackbar.show();
    }

    private void showDiscord() {
        new MaterialDialog.Builder(this)
                .title("Official Discord Server")
                .icon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_discord)
                        .color(Color.rgb(114, 137, 218))
                        .sizeDp(24))
                .content(R.string.join_discord)
                .negativeText(R.string.button_no)
                .positiveText(R.string.ok)
                .onNegative((dialog, which) -> {
                    Once.markDone("discordResponse");
                    dialog.dismiss();
                })
                .onPositive((dialog, which) -> {
                    Once.markDone("discordResponse");
                    String discordUrl = getString(R.string.discord_url);
                    Intent discordIntent = new Intent(Intent.ACTION_VIEW);
                    discordIntent.setData(Uri.parse(discordUrl));
                    startActivity(discordIntent);
                })
                .show();
    }

    private void showChangelogSnackBar() {
        snackbar = Snackbar
                .make(binding.coordinatorLayout, getString(R.string.updated_version) + " " + Utils.getVersionName(context), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAction("Changelog", view -> showWhatsNew());
        snackbar.show();

    }

    private void becomeSupporter() {
        Intent becomeSupporter = new Intent(this, SupporterActivity.class);
        startActivity(becomeSupporter);
    }

    @SuppressLint("ShowToast")
    private synchronized void showRemainingCount() {
        int count = (int) rate.getRemainingCount();
        String message = String.format("%s more times until rate pop-up", count);
        Toast mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.setText(message);
        if (BuildConfig.DEBUG) {
            mToast.show();
        }
    }

    public void setActionBarTitle(String title) {
        binding.mainToolbar.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (mNavItemId != R.id.menu_home
                && mNavItemId != R.id.menu_vehicle
                && mNavItemId != R.id.menu_launches
                && mNavItemId != R.id.menu_news) {
            mNavItemId = R.id.menu_home;
            drawer.setSelection(R.id.menu_home, false);
            navigate(mNavItemId);
        } else if (mNavItemId != R.id.menu_home) {
            mNavItemId = R.id.menu_home;
            navigate(mNavItemId);
        } else {
            checkExitApp();
        }
    }

    private void checkExitApp() {
        if (sharedPref.getBoolean("confirm_exit", false)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.confirm_exit)
                    .negativeText(R.string.cancel)
                    .positiveText(R.string.exit)
                    .onPositive((dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.debug_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        if (SupporterHelper.isSupporter()) {
            menu.findItem(R.id.action_supporter).setVisible(false);
            menu.removeItem(R.id.action_supporter);
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_consent) {
            GDPR.getInstance().resetConsent();
            showGDPRIfNecessary();
        }

        if (id == R.id.action_supporter) {
            Intent intent = new Intent(this, SupporterActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void navigate(final int itemId) {
        Timber.v("Navigate to %s", itemId);
        // perform the actual navigation logic, updating the main_menu content fragment etc
        switch (itemId) {
            case R.id.menu_home:
                setActionBarTitle("Space Launch Now");
                if (mUpcomingFragment == null) {
                    mUpcomingFragment = NextLaunchFragment.newInstance();
                    if (showFilter) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("SHOW_FILTERS", true);
                        mUpcomingFragment.setArguments(bundle);
                        showFilter = false;
                    }
                }
                mNavItemId = R.id.menu_home;
                drawer.setSelection(mNavItemId, false);
                navigateToFragment(mUpcomingFragment, HOME_TAG);
                addAppBarElevation();
                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_launches:
                setActionBarTitle("Space Launch Now");
                removeAppBarElevation();
                mNavItemId = R.id.menu_launches;
                if (mlaunchesViewPager == null) {
                    mlaunchesViewPager = LaunchesViewPager.newInstance();
                }
                navigateToFragment(mlaunchesViewPager, LAUNCHES_TAG);
                if (rate != null) {
                    rate.showRequest();
                }
                checkHideAd();
                break;
            case R.id.menu_news:
                setActionBarTitle(getString(R.string.space_launch_news));
                removeAppBarElevation();
                mNavItemId = R.id.menu_news;
                drawer.setSelection(mNavItemId, false);
                if (mNewsViewpagerFragment == null) {
                    mNewsViewpagerFragment = NewsViewPager.newInstance();
                    if (newsUrl != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("newsUrl", newsUrl);
                        mNewsViewpagerFragment.setArguments(bundle);
                        newsUrl = null;
                    }
                } else if (newsUrl != null) {
                    mNewsViewpagerFragment = NewsViewPager.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString("newsUrl", newsUrl);
                    mNewsViewpagerFragment.setArguments(bundle);
                    newsUrl = null;
                }
                navigateToFragment(mNewsViewpagerFragment, NEWS_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_iss:
                setActionBarTitle(getString(R.string.spacestations));
                mNavItemId = R.id.menu_iss;
                drawer.setSelection(mNavItemId, false);
                addAppBarElevation();
                if (mSpacestationListFragment == null)
                    mSpacestationListFragment = SpacestationListFragment.newInstance();
                navigateToFragment(mSpacestationListFragment, ISS_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_astronauts:
                setActionBarTitle(getString(R.string.astronauts));
                mNavItemId = R.id.menu_astronauts;
                drawer.setSelection(mNavItemId, false);
                removeAppBarElevation();
                if (mAstronautsListFragment == null)
                    mAstronautsListFragment = AstronautListFragment.newInstance();
                navigateToFragment(mAstronautsListFragment, ASTRONAUT_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_starship:
                setActionBarTitle("Starship Development");
                mNavItemId = R.id.menu_starship;
                drawer.setSelection(mNavItemId, false);
                removeAppBarElevation();
                if (mStarshipDashboardFragment == null)
                    mStarshipDashboardFragment = StarshipViewPager.newInstance();
                navigateToFragment(mStarshipDashboardFragment, STARSHIP_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_events:
                setActionBarTitle(getString(R.string.events));
                mNavItemId = R.id.menu_events;
                drawer.setSelection(mNavItemId, false);
                addAppBarElevation();
                if (mEventsFragment == null)
                    mEventsFragment = EventListFragment.newInstance();
                navigateToFragment(mEventsFragment, EVENTS_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_vehicle:
                setActionBarTitle(getString(R.string.vehicles));
                removeAppBarElevation();
                mNavItemId = R.id.menu_vehicle;
                drawer.setSelection(mNavItemId, false);
                if (mVehicleViewPager == null) mVehicleViewPager = VehiclesViewPager.newInstance();
                navigateToFragment(mVehicleViewPager, VEHICLE_TAG);

                if (rate != null) {
                    rate.showRequest();
                }
                checkRefreshAd();
                break;
            case R.id.menu_launch:
                openCustomTab(this, getApplicationContext(), "https://launchlibrary.net/");
                break;
            case R.id.menu_patreon:
                openCustomTab(this, getApplicationContext(), "https://www.patreon.com/spacelaunchnow");
                break;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(settingsIntent);
                break;
            case R.id.menu_new:
                showWhatsNew();
                break;
            case R.id.menu_support:
                Intent supportIntent = new Intent(this, SupporterActivity.class);
                startActivity(supportIntent);
                break;
            case R.id.menu_feedback:
                showFeedback();
                break;
            case R.id.menu_twitter:
                String url = getString(R.string.twitter_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.menu_discord:
                String discordUrl = getString(R.string.discord_url);
                Intent discordIntent = new Intent(Intent.ACTION_VIEW);
                discordIntent.setData(Uri.parse(discordUrl));
                startActivity(discordIntent);
                break;
            case R.id.menu_facebook:
                String facebookUrl = getString(R.string.facebook_url);
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
                break;
            case R.id.menu_website:
                String websiteUrl = getString(R.string.spacelaunchnow_web);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                websiteIntent.setData(Uri.parse(websiteUrl));
                startActivity(websiteIntent);
                break;
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            default:
                // ignore
                break;
        }
    }

    private void navigateToFragment(Fragment fragment, String TAG) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        Fragment inputFragment = supportFragmentManager.findFragmentByTag(TAG);

        if (inputFragment == null) {
            fragmentTransaction.replace(R.id.flContent, fragment, TAG)
                    .addToBackStack(null).commitAllowingStateLoss();
        } else {
            fragmentTransaction.replace(R.id.flContent, inputFragment, TAG)
                    .addToBackStack(null).commitAllowingStateLoss();
        }
    }

    private void showFeedback() {
        new MaterialDialog.Builder(this)
                .title(R.string.feedback_title)
                .autoDismiss(true)
                .content(R.string.feedback_description)
//                .negativeColor(getCyanea().getAccent())
                .negativeText(R.string.launch_data)

                .onNegative((dialog, which) -> {
                    String url = getString(R.string.launch_library_reddit);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                })
//                .positiveColor(getCyanea().getPrimary())
                .positiveText(R.string.app_feedback)
                .onPositive((dialog, which) -> dialog.getBuilder()
                        .title(R.string.need_support)
                        .content(R.string.need_support_description)
                        .neutralText(R.string.email)
                        .negativeText(R.string.cancel)
                        .positiveText(R.string.discord)
                        .onNeutral((dialog1, which1) -> {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@spacelaunchnow.me"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Space Launch Now - Feedback");

                            startActivity(Intent.createChooser(intent, "Email via..."));
                        })
                        .onPositive((dialog12, which12) -> {
                            String url = getString(R.string.discord_url);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        })
                        .onNegative((dialog13, which13) -> dialog13.dismiss())
                        .show())
                .show();
    }

    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }

    private void hideAd() {
        binding.adView.setVisibility(View.GONE);
    }

    private void showAd() {
        Timber.v("Showing Ad!");
        if ((adviewEnabled && binding.adView.getVisibility() == View.GONE) || (adviewEnabled && binding.adView.getVisibility() == View.INVISIBLE)) {
            binding.adView.setVisibility(View.VISIBLE);
        }
    }


    public void checkHideAd() {
        hideAd();
    }

    public void checkShowAd() {
        Timber.v("Checking if Ads are showing!");
        if (adviewEnabled && mNavItemId != R.id.menu_launches) {
            showAd();
        }
    }

    public void checkRefreshAd() {
        adShowCount += 1;
        if (adviewEnabled) {
            checkShowAd();
            Timber.v("Ad show count - %s", adShowCount);
            if (adShowCount % 9 == 0 && adShowCount != 0) {
                if (mNavItemId != R.id.menu_launches) {
                    loadAd();
                }
            }
        }
    }

    public void hideAdIfEnabled() {
        if (adviewEnabled) {
            hideAd();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int m_theme = R.style.BaseAppTheme;

        setTheme(m_theme);
        // Check which request we're responding to
        if (requestCode == SHOW_INTRO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Once.markDone("showTutorial");
                showFilter = true;
                navigate(mNavItemId);
            }
        }
    }

    private void showGDPRIfNecessary(boolean forceShow, GDPRPreperationData data) {
        if (forceShow || data.getLocation() == GDPRLocation.IN_EAA_OR_UNKNOWN) {
            try {
                GDPR.getInstance().showDialog(this, getGDPRSetup(), data.getLocation());
            } catch (IllegalStateException e) {
                Timber.e(e);
            }
        }
    }

    private void showGDPRIfNecessary() {
        GDPRSetup setup = getGDPRSetup();
        GDPR.getInstance().checkIfNeedsToBeShown(this, setup);
    }

    private GDPRSetup getGDPRSetup() {

        return new GDPRSetup(GDPRDefinitions.ADMOB,
                GDPRDefinitions.FIREBASE_CRASH,
                GDPRDefinitions.FABRIC_CRASHLYTICS,
                GDPRDefinitions.FABRIC_ANSWERS,
                GDPRDefinitions.FIREBASE_ANALYTICS)
                .withPrivacyPolicy("https://spacelaunchnow.me/app/privacy")
                .withAllowNoConsent(false)
                .withBottomSheet(true)
                .withForceSelection(true);
    }


    @Override
    public void onConsentNeedsToBeRequested(GDPRPreperationData gdprPreperationData) {
        showGDPRIfNecessary(false, gdprPreperationData);
    }

    @Override
    public void onConsentInfoUpdate(GDPRConsentState consentState, boolean isNewState) {
        GDPRConsent consent = consentState.getConsent();
        if (isNewState) {
            // user just selected this consent, do whatever you want...
            switch (consent) {
                case UNKNOWN:
                    // never happens!
                    break;
                case NO_CONSENT:
                    break;
                case NON_PERSONAL_CONSENT_ONLY:
                    break;
                case PERSONAL_CONSENT:
                    break;
            }
        } else {
            switch (consent) {
                case UNKNOWN:
                    // never happens!
                    break;
                case NO_CONSENT:
                    break;
                case NON_PERSONAL_CONSENT_ONLY:
                    break;
                case PERSONAL_CONSENT:
                    // user restarted activity and consent was already given...
                    break;
            }
        }
        configureAdState(consentState);
    }

    private void configureAdState(GDPRConsentState consentState) {
        allowsPersonalAds = true;
        if (Once.beenDone("appOpen", Amount.moreThan(3))) {
            allowAds = true;
            GDPRConsent consent = consentState.getConsent();

            if (consentState.getLocation() == GDPRLocation.IN_EAA_OR_UNKNOWN
                    && consent == GDPRConsent.UNKNOWN) {
                allowAds = false;
            } else if (consentState.getLocation() == GDPRLocation.IN_EAA_OR_UNKNOWN
                    && consent == GDPRConsent.NO_CONSENT
                    || consent == GDPRConsent.NON_PERSONAL_CONSENT_ONLY) {
                allowsPersonalAds = false;
            } else if (consentState.getLocation() == GDPRLocation.NOT_IN_EAA) {
                GDPR.getInstance().resetConsent();
            }

            if (mNavItemId != R.id.menu_launches) {
                loadAd();
            } else {
                checkRefreshAd();
            }
        }
    }

    private void loadAd() {
        Timber.v("Checking supporter or not.");
        if (!SupporterHelper.isSupporter() && allowAds) {
            Timber.d("Loading ads.");
            if (allowsPersonalAds) {
                binding.adView.loadAd(new AdRequest.Builder().build());
            } else {
                Bundle extras = new Bundle();
                extras.putString("npa", "1");

                binding.adView.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build());
            }
            binding.adView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    Timber.d("Ad loaded successfully.");
                    adviewEnabled = true;
                    if (mUpcomingFragment != null) {
                        if (!mUpcomingFragment.isFilterShown()) {
                            showAd();
                        }
                    } else {
                        showAd();
                    }
                    super.onAdLoaded();
                }
            });
        } else {
            Timber.d("Hiding ads.");
            adviewEnabled = false;
            hideAd();
        }
    }

    @Override
    public void onNavigateToLaunches() {
        drawer.setSelection(R.id.menu_launches, false);
        navigate(R.id.menu_launches);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("newsUrl")) {
                newsUrl = extras.getString("newsUrl");
                mNavItemId = R.id.menu_news;
                drawer.setSelection(mNavItemId, false);
                navigate(R.id.menu_news);
                intent.getExtras().clear();
            }
        }
    }
}
