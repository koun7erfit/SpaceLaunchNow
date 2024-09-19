package me.calebjones.spacelaunchnow.starship.ui.upcoming;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.data.models.main.Event;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;
import me.calebjones.spacelaunchnow.data.models.main.dashboards.Starship;
import me.calebjones.spacelaunchnow.events.list.EventRecyclerViewAdapter;
import me.calebjones.spacelaunchnow.starship.StarshipDashboardViewModel;
import me.spacelaunchnow.starship.R;
import me.spacelaunchnow.starship.databinding.FragmentStarshipDashboardUpcomingBinding;
import me.spacelaunchnow.starship.databinding.StarshipDashboardUpcomingBinding;

/**
 * A fragment representing the Starship Dashboard
 */
public class StarshipUpcomingFragment extends BaseFragment {

    private StarshipDashboardViewModel model;
    private CombinedAdapter adapter;
    private EventRecyclerViewAdapter eventRecyclerViewAdapter;
    private boolean showUpcoming = true;
    private Starship starship;
    private FragmentStarshipDashboardUpcomingBinding binding;
    private StarshipDashboardUpcomingBinding upcomingBinding;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StarshipUpcomingFragment() {
    }

    @SuppressWarnings("unused")
    public static StarshipUpcomingFragment newInstance() {
        return new StarshipUpcomingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Starship Dashboard Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStarshipDashboardUpcomingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setHasOptionsMenu(true);
        model = ViewModelProviders.of(getParentFragment()).get(StarshipDashboardViewModel.class);
        model.getStarshipDashboard().observe(this, this::viewRefreshed);
        assert binding.starshipDashboardUpcomingView != null;

        upcomingBinding.starshipDashboardLaunchCard.upcomingSwitch.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.upcomingButton) {
                    showUpcoming = true;
                    updateViews(starship, false);
                } else if (checkedId == R.id.previousButton) {
                    showUpcoming = false;
                    updateViews(starship, false);
                }
            }
        });

        return view;
    }

    private void viewRefreshed(Starship starship){
        updateViews(starship, true);
    }


    private void updateViews(Starship starship, boolean refreshed) {
        assert binding.starshipDashboardUpcomingView != null;
        binding.starshipDashboardUpcomingView.starshipDashboardLaunchCard.launchRecycler.smoothScrollToPosition(0);
        this.starship = starship;
        ArrayList<Object> combinedObjects = new ArrayList<>();


        if (showUpcoming) {
            if (!this.starship.getUpcomingObjects().getEvents().isEmpty()) {
                combinedObjects.addAll(this.starship.getUpcomingObjects().getEvents());
            }

            if (!this.starship.getUpcomingObjects().getLaunches().isEmpty()) {
                combinedObjects.addAll(this.starship.getUpcomingObjects().getLaunches());
            }

            if (!combinedObjects.isEmpty()) {
                binding.starshipDashboardUpcomingView.combinedStatefulLayout.showContent();
            } else {
                binding.starshipDashboardUpcomingView.combinedStatefulLayout.showEmpty();
            }
            combinedObjects = sortMultiClassList(combinedObjects, true);
        } else {
            if (!this.starship.getPreviousObjects().getEvents().isEmpty()) {
                combinedObjects.addAll(this.starship.getPreviousObjects().getEvents());
            }

            if (!this.starship.getPreviousObjects().getLaunches().isEmpty()) {
                combinedObjects.addAll(this.starship.getPreviousObjects().getLaunches());
            }

            if (!combinedObjects.isEmpty()) {
                binding.starshipDashboardUpcomingView.combinedStatefulLayout.showContent();
            } else {
                binding.starshipDashboardUpcomingView.combinedStatefulLayout.showEmpty();
            }

            combinedObjects = sortMultiClassList(combinedObjects, false);
        }

        if (refreshed) {
            adapter = new CombinedAdapter(getContext(), ThemeHelper.isDarkMode(getActivity()));
            if (binding.starshipDashboardUpcomingView != null) {
                binding.starshipDashboardUpcomingView.starshipDashboardLaunchCard.launchRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.starshipDashboardUpcomingView.starshipDashboardLaunchCard.launchRecycler.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
                binding.starshipDashboardUpcomingView.starshipDashboardLaunchCard.launchRecycler.setAdapter(adapter);
            }
        } else {
            adapter.clear();
        }
        adapter.addItems(combinedObjects);
    }

    public ArrayList<Object> sortMultiClassList(ArrayList<Object> yourList, boolean ascending) {

        if (ascending) {
            yourList.sort((o1, o2) -> {
                if (o1 instanceof LaunchList && o2 instanceof LaunchList) {
                    return ((LaunchList) o1).getNet().compareTo(((LaunchList) o2).getNet());
                } else if (o1 instanceof LaunchList && o2 instanceof Event) {
                    return ((LaunchList) o1).getNet().compareTo(((Event) o2).getDate());
                } else if (o1 instanceof Event && o2 instanceof LaunchList) {
                    return ((Event) o1).getDate().compareTo(((LaunchList) o2).getNet());
                } else if (o1 instanceof Event && o2 instanceof Event) {
                    return ((Event) o1).getDate().compareTo(((Event) o2).getDate());
                } else {
                    throw new IllegalArgumentException("Don't know how to compare");
                }
            });
        } else {
            yourList.sort((o1, o2) -> {
                if (o2 instanceof LaunchList && o1 instanceof LaunchList) {
                    return ((LaunchList) o2).getNet().compareTo(((LaunchList) o1).getNet());
                } else if (o2 instanceof LaunchList && o1 instanceof Event) {
                    return ((LaunchList) o2).getNet().compareTo(((Event) o1).getDate());
                } else if (o2 instanceof Event && o1 instanceof LaunchList) {
                    return ((Event) o2).getDate().compareTo(((LaunchList) o1).getNet());
                } else if (o2 instanceof Event && o1 instanceof Event) {
                    return ((Event) o2).getDate().compareTo(((Event) o1).getDate());
                } else {
                    throw new IllegalArgumentException("Don't know how to compare");
                }
            });
        }
        return yourList;
    }

    @Override
    public void onResume() {
        if (starship != null) {
            updateViews(starship, false);
        }
        super.onResume();
    }

}
