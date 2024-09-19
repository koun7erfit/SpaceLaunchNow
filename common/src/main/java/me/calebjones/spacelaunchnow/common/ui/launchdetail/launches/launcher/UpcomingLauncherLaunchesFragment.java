package me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.launcher;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.content.data.Callbacks;
import me.calebjones.spacelaunchnow.common.databinding.FragmentLaunchListBinding;
import me.calebjones.spacelaunchnow.common.utils.EndlessRecyclerViewScrollListener;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.common.content.data.upcoming.UpcomingDataRepository;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;
import me.calebjones.spacelaunchnow.common.ui.adapters.ListAdapter;
import timber.log.Timber;


public class UpcomingLauncherLaunchesFragment extends BaseFragment {

    private static final String SEARCH_TERM = "searchTerm";
    private static final String LSP_NAME = "lspName";
    private static final String SERIAL_NUMBER = "serialNumber";
    private static final String LAUNCHER_ID = "launcherId";

    private LinearLayoutManager linearLayoutManager;
    private ListAdapter adapter;
    private String searchTerm = null;
    private String lspName = null;
    private String serialNumber = null;
    private UpcomingDataRepository upcomingDataRepository;
    private int nextOffset = 0;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ArrayList<String> agencyList;
    private List<Agency> agencies;
    public boolean canLoadMore;
    private boolean statefulStateContentShow = false;
    private Context context;
    private Integer launcherId = null;
    private UpcomingLauncherLaunchesFragment.OnFragmentInteractionListener mListener;
    private FragmentLaunchListBinding binding;


    public UpcomingLauncherLaunchesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param searchTerm Parameter 1.
     * @param lspName    Parameter 2.
     * @return A new instance of fragment PreviousLauncherLaunchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpcomingLauncherLaunchesFragment newInstance(String searchTerm,
                                                               String lspName,
                                                               Integer launcherId,
                                                               String serialNumber) {
        UpcomingLauncherLaunchesFragment fragment = new UpcomingLauncherLaunchesFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_TERM, searchTerm);
        args.putString(LSP_NAME, lspName);
        args.putString(SERIAL_NUMBER, serialNumber);
        args.putInt(LAUNCHER_ID, launcherId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchTerm = getArguments().getString(SEARCH_TERM);
            lspName = getArguments().getString(LSP_NAME);
            launcherId = getArguments().getInt(LAUNCHER_ID);
            serialNumber = getArguments().getString(SERIAL_NUMBER);
            if (launcherId == 0) {
                launcherId = null;
            }
        }
        context = getActivity();
        upcomingDataRepository = new UpcomingDataRepository(context, getRealm());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLaunchListBinding.inflate(inflater, container, false);
        View view = inflater.inflate(R.layout.fragment_launch_list, container, false);

        adapter = new ListAdapter(context, false);
        linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        binding.recyclerView.setAdapter(adapter);
        binding.statefulView.showProgress();
        binding.statefulView.setOfflineRetryOnClickListener(v -> fetchData(true));
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (canLoadMore) {
                    fetchData(false);
                    mListener.showUpcomingLoading(true);
                }
            }
        };
        binding.recyclerView.addOnScrollListener(scrollListener);
        fetchData(true);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpcomingLauncherLaunchesFragment.OnFragmentInteractionListener) {
            mListener = (UpcomingLauncherLaunchesFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                       + " must implement OnFragmentInteractionListener");
        }
    }


    public void fetchData(boolean forceRefresh) {
        Timber.v("Sending GET_UP_LAUNCHES");
        if (forceRefresh) {
            nextOffset = 0;
            adapter.clear();
        }
        upcomingDataRepository.getUpcomingLaunches(
                nextOffset,
                searchTerm,
                lspName,
                serialNumber,
                launcherId,
                new Callbacks.ListCallbackMini() {
                    @Override
                    public void onLaunchesLoaded(List<LaunchList> launches, int next, int total) {
                        Timber.v("Offset - %s", next);
                        nextOffset = next;
                        canLoadMore = next > 0;
                        updateAdapter(launches);
                        mListener.setUpcomingBadge(total);
                    }

                    @Override
                    public void onNetworkStateChanged(boolean refreshing) {
                        mListener.showUpcomingLoading(refreshing);
                    }

                    @Override
                    public void onError(String message, @Nullable Throwable throwable) {
                        binding.statefulView.showOffline();
                        statefulStateContentShow = false;
                        if (throwable != null) {
                            Timber.e(throwable);
                        } else {
                            Timber.e(message);
                        }
                    }
                }
        );
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

    public void onRefresh(String lspName, String searchTerm, String serialNumber) {
        this.searchTerm = searchTerm;
        this.lspName = lspName;
        this.serialNumber = serialNumber;
        fetchData(true);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void showUpcomingLoading(boolean loading);

        void setUpcomingBadge(int count);

    }
}
