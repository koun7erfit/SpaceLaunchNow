package me.calebjones.spacelaunchnow.astronauts.list;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.realm.RealmResults;
import me.calebjones.spacelaunchnow.astronauts.data.Callbacks;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.utils.EndlessRecyclerViewScrollListener;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.astronaut.Astronaut;
import me.spacelaunchnow.astronauts.R;
import me.calebjones.spacelaunchnow.astronauts.data.AstronautDataRepository;
import me.spacelaunchnow.astronauts.databinding.FragmentAstronautListBinding;
import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class AstronautListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String ARG_STATUS_ID = "status-id";
    private String searchTerm;
    private AstronautDataRepository dataRepository;
    private List<Agency> agencies;
    private int nextOffset = 0;
    private int astronautCount = 0;
    private boolean canLoadMore;
    private boolean statefulStateContentShow = false;
    private boolean firstLaunch = true;
    private AstronautRecyclerViewAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;
    private List<Integer> statusIDs;
    private Integer[] statusIDsSelection;
    private boolean limitReached;
    private SearchView searchView;
    private FragmentAstronautListBinding binding;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AstronautListFragment() {
    }

    @SuppressWarnings("unused")
    public static AstronautListFragment newInstance() {
        return new AstronautListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new AstronautDataRepository(getContext(), getRealm());
        setScreenName("Astronaut List Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAstronautListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setHasOptionsMenu(true);

        // Set the adapter
        Context context = view.getContext();
        adapter = new AstronautRecyclerViewAdapter(context);
        linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.astronautRecyclerView.setLayoutManager(linearLayoutManager);
        binding.astronautRecyclerView.setAdapter(adapter);
        if(firstLaunch) {
            binding.astronautStatefulView.showProgress();
        } else {
            binding.astronautStatefulView.showContent();
        }

        canLoadMore = true;
        limitReached = false;
        binding.astronautStatefulView.setOfflineRetryOnClickListener(v -> onRefresh());
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (canLoadMore) {
                    boolean searchQuery = false;
                    if (searchTerm != null) {
                        searchQuery = true;
                    }
                    fetchData(false, false, searchQuery);
                }
            }
        };
        binding.astronautRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        binding.astronautRecyclerView.addOnScrollListener(scrollListener);
        fetchData(false, firstLaunch, false);
        firstLaunch = false;
        binding.astronautRefreshLayout.setOnRefreshListener(this);
        binding.astronautFilter.setOnClickListener(view1 -> filterClicked());
        return view;
    }

    private void fetchData(boolean forceRefresh, boolean firstLaunch, boolean searchQuery) {
        Timber.v("fetchData - getting astronauts");
        nextOffset = astronautCount;
        int limit = 40;


        if (forceRefresh || searchQuery) {
            astronautCount = 0;
            limitReached = false;
            adapter.clear();
        }

        if (!limitReached) {
            dataRepository.getAstronauts(limit, astronautCount, firstLaunch, forceRefresh, searchTerm, statusIDs, new Callbacks.AstronautListCallback() {
                @Override
                public void onAstronautsLoaded(RealmResults<Astronaut> astronauts, int next, int total) {
                    Timber.v("Offset - %s", next);
                    if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        if (astronauts.size() == total) {
                            limitReached = true;
                            canLoadMore = false;
                        } else {
                            astronautCount = astronauts.size();
                            canLoadMore = true;
                        }
                        updateAdapter(astronauts);
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
                        binding.astronautStatefulView.showOffline();
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

    private void searchData(String query){
        updateAdapter(dataRepository.getAstronautsFromRealm(statusIDs, query));
    }

    //Currently only used to debug
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.astronaut_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

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

    private void showNetworkLoading(boolean refreshing) {
        binding.astronautRefreshLayout.setRefreshing(refreshing);
    }

    private void updateAdapter(List<Astronaut> astronauts) {

        if (astronauts.size() > 0) {
            if (!statefulStateContentShow) {
                binding.astronautStatefulView.showContent();
                statefulStateContentShow = true;
            }
            adapter.addItems(astronauts);
        } else {
            binding.astronautStatefulView.showEmpty();
            statefulStateContentShow = false;
            if (adapter != null) {
                adapter.clear();
            }
        }
        scrollListener.resetState();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onRefresh() {
        if (searchTerm != null || statusIDs != null) {
            statusIDs = null;
            searchTerm = null;
            binding.astronautRefreshLayout.setRefreshing(false);
            fetchData(false, false, false);
        } else {
            fetchData(true, false, false);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchTerm = query;
        fetchData(false, false, true);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        searchTerm = query;
        fetchData(false, false, true);
        return false;
    }

    @Override
    public boolean onClose() {
        fetchData(false, true, false);
        return false;
    }

//    @OnClick(R2.id.astronaut_filter)
    void filterClicked(){
        new MaterialDialog.Builder(getContext())
                .title("Astronaut Status")
                .items(R.array.status_array)
                .itemsCallbackMultiChoice(statusIDsSelection, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        /**
                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected check box to actually be selected
                         * (or the newly unselected check box to be unchecked).
                         * See the limited multi choice dialog example in the sample project for details.
                         **/
                        statusIDsSelection = which;
                        statusIDs = new ArrayList<>();
                        int[] valueArray = getResources().getIntArray(R.array.status_value);
                        for (int integer: statusIDsSelection){
                            statusIDs.add(valueArray[integer]);
                        }
                        fetchData(false, false, true);
                        return true;
                    }
                })
                .positiveText("Ok")
                .show();
    }
}
