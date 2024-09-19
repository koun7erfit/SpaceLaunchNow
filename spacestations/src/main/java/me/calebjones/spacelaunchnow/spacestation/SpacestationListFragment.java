package me.calebjones.spacelaunchnow.spacestation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.kinst.jakub.view.SimpleStatefulLayout;
import io.realm.RealmResults;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.utils.EndlessRecyclerViewScrollListener;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.spacestation.data.Callbacks;
import me.calebjones.spacelaunchnow.spacestation.data.SpacestationDataRepository;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationsFragmentBinding;
import timber.log.Timber;

public class SpacestationListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private String searchTerm;
    private SpacestationDataRepository dataRepository;
    private int nextOffset = 0;
    private int stationCount = 0;
    private boolean canLoadMore;
    private boolean statefulStateContentShow = false;
    private boolean firstLaunch = true;
    private SpacestationRecyclerViewAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;
    private boolean limitReached;
    private SpacestationsFragmentBinding binding;

    public static SpacestationListFragment newInstance() {
        return new SpacestationListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new SpacestationDataRepository(getContext(), getRealm());
        setScreenName("SpaceStation List Fragment");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SpacestationsFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setHasOptionsMenu(true);

        // Set the adapter
        Context context = getContext();
        adapter = new SpacestationRecyclerViewAdapter(context);
        linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.spacestationRecyclerView.setLayoutManager(linearLayoutManager);
        binding.spacestationRecyclerView.setAdapter(adapter);
        if (firstLaunch) {
            binding.spacestationStatefulView.showProgress();
        } else {
            binding.spacestationStatefulView.showContent();
        }

        canLoadMore = true;
        limitReached = false;
        binding.spacestationStatefulView.setOfflineRetryOnClickListener(v -> onRefresh());
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (canLoadMore) {
                    boolean searchQuery = searchTerm != null;
                    fetchData(false, false, searchQuery);
                }
            }
        };
        binding.spacestationRecyclerView.addOnScrollListener(scrollListener);
        fetchData(false, firstLaunch, false);
        firstLaunch = false;
        binding.spacestationRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    private void fetchData(boolean forceRefresh, boolean firstLaunch, boolean searchQuery) {
        Timber.v("fetchData - getting astronauts");
        nextOffset = stationCount;
        int limit = 40;


        if (forceRefresh || searchQuery) {
            stationCount = 0;
            limitReached = false;
            adapter.clear();
        }

        if (!limitReached) {
            dataRepository.getSpacestations(limit, stationCount, firstLaunch, null, new Callbacks.SpacestationListCallback() {
                @Override
                public void onSpacestationsLoaded(RealmResults<Spacestation> spacestations, int next, int total) {
                    Timber.v("Offset - %s", next);
                    if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        if (spacestations.size() == total) {
                            limitReached = true;
                            canLoadMore = false;
                        } else {
                            stationCount = spacestations.size();
                            canLoadMore = true;
                        }
                        updateAdapter(spacestations);
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
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        binding.spacestationStatefulView.showOffline();
                        statefulStateContentShow = false;
                        if (throwable != null) {
                            Timber.e(throwable);
                        } else {
                            Timber.e(message);
                        }
                    }
                }
            });
        }
    }

    private void updateAdapter(List<Spacestation> spacestations) {

        if (!spacestations.isEmpty()) {
            if (!statefulStateContentShow) {
                binding.spacestationStatefulView.showContent();
                statefulStateContentShow = true;
            }
            adapter.addItems(spacestations);
        } else {
            binding.spacestationStatefulView.showEmpty();
            statefulStateContentShow = false;
            if (adapter != null) {
                adapter.clear();
            }
        }
        scrollListener.resetState();
    }

    private void showNetworkLoading(boolean refreshing) {
        binding.spacestationRefreshLayout.setRefreshing(refreshing);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onRefresh() {
        fetchData(true,  firstLaunch,  false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main_menu, menu);
    }
}
