package me.calebjones.spacelaunchnow.ui.main.launches;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import cz.kinst.jakub.view.SimpleStatefulLayout;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.ui.adapters.ListAdapter;
import me.calebjones.spacelaunchnow.common.utils.EndlessRecyclerViewScrollListener;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;

import me.calebjones.spacelaunchnow.common.content.data.Callbacks;
import me.calebjones.spacelaunchnow.common.content.data.upcoming.UpcomingDataRepository;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;
import me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.common.ui.views.SnackbarHandler;
import me.calebjones.spacelaunchnow.databinding.FragmentLaunchesBinding;
import me.spacelaunchnow.astronauts.databinding.AstronautFlightFragmentBinding;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingLaunchesFragment extends BaseFragment implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private ListAdapter adapter;
    private AdmobBannerRecyclerAdapterWrapper adapterWrapper;
    private LinearLayoutManager layoutManager;
    private Context context;
    private UpcomingDataRepository upcomingDataRepository;
    private int nextOffset = 0;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String searchTerm = null;
    private List<LaunchList> launches;
    private SearchView searchView;
    public boolean canLoadMore;
    private boolean statefulStateContentShow = false;
    private static final Field sChildFragmentManagerField;
    private FragmentLaunchesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Upcoming Launch Fragment");
        upcomingDataRepository = new UpcomingDataRepository(context, getRealm());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLaunchesBinding.inflate(inflater, container, false);
        this.context = getContext();
        canLoadMore = true;
        setHasOptionsMenu(true);
        launches = new ArrayList<>();

        if (adapter == null) {
            Timber.v("Creating new ListAdapter");
            adapter = new ListAdapter(getContext(), ThemeHelper.isDarkMode(getActivity()));
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.getInitialPrefetchItemCount();
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));

        binding.recyclerView.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (canLoadMore) {
                    fetchData(false);
                    binding.swipeRefreshLayout.setRefreshing(true);
                }
            }
        };
        binding.recyclerView.addOnScrollListener(scrollListener);
        binding.statefulView.setOfflineRetryOnClickListener(v -> fetchData(true));
        return binding.getRoot();
    }

    public static UpcomingLaunchesFragment newInstance(String text) {

        UpcomingLaunchesFragment u = new UpcomingLaunchesFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        u.setArguments(b);

        return u;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateAdapter(List<LaunchList> launches) {
        if (!launches.isEmpty()) {
            if (!statefulStateContentShow) {
                binding.statefulView.showContent();
                statefulStateContentShow = true;
            }
            adapter.addItems(launches);
            adapter.notifyDataSetChanged();

        } else {
            binding.statefulView.showEmpty();
            statefulStateContentShow = false;
            if (adapter != null) {
                adapter.clear();
            }
        }
        scrollListener.resetState();
    }

    public void fetchData(boolean forceRefresh) {
        Timber.v("Sending GET_UP_LAUNCHES");
        if (forceRefresh) {
            nextOffset = 0;
            adapter.clear();
        }
        upcomingDataRepository.getUpcomingLaunches(nextOffset, searchTerm, null, null,null, new Callbacks.ListCallbackMini() {
            @Override
            public void onLaunchesLoaded(List<LaunchList> launches, int next, int total) {
                Timber.v("Offset - %s", next);
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    nextOffset = next;
                    canLoadMore = next > 0;
                    updateAdapter(launches);
                }
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    showNetworkLoading(refreshing);
                }
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    binding.statefulView.showOffline();
                    statefulStateContentShow = false;
                    showNetworkLoading(false);
                    if (throwable != null) {
                        Timber.e(throwable);
                    } else {
                        Timber.e(message);
                        SnackbarHandler.showErrorSnackbar(context, binding.coordinatorLayout, message);
                    }
                    SnackbarHandler.showErrorSnackbar(context, binding.coordinatorLayout, message);
                }
            }
        });
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
        binding.swipeRefreshLayout.setRefreshing(true);
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        binding.swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onResume() {
        Timber.d("OnResume!");
        if (adapter.getItemCount() == 0) {
            statefulStateContentShow = false;
            binding.statefulView.showProgress();
            fetchData(false);
        }
        super.onResume();
    }

    @Override
    public void onRefresh() {
        searchTerm = null;
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
        fetchData(true);
    }

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Timber.e("Error getting mChildFragmentManager field %s", e);
        }
        sChildFragmentManagerField = f;
    }

    //Currently only used to debug
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.list_menu, menu);

        if (SupporterHelper.isSupporter()) {
            menu.removeItem(R.id.action_supporter);
        }

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            onRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchTerm = query;
        fetchData(true);
        searchView.clearFocus();
        return false;
    }
}
