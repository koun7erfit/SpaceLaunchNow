package me.calebjones.spacelaunchnow.spacestation.detail.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import me.calebjones.spacelaunchnow.data.models.main.spacestation.DockingLocation;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationDockingFragmentBinding;
import me.calebjones.spacelaunchnow.spacestation.detail.SpacestationDetailViewModel;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.DockingLocationItem;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.ListItem;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.SpacestationAdapter;

public class SpacestationDockedVehiclesFragment extends BaseFragment {

    private SpacestationDetailViewModel mViewModel;
    private LinearLayoutManager linearLayoutManager;
    private SpacestationAdapter adapter;
    private Context context;
    private SpacestationDockingFragmentBinding binding;

    public static SpacestationDockedVehiclesFragment newInstance() {
        return new SpacestationDockedVehiclesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SpacestationDockingFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        linearLayoutManager = new LinearLayoutManager(context);
        adapter = new SpacestationAdapter(context);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.statefulView.showProgress();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(SpacestationDetailViewModel.class);
        // update UI
        mViewModel.getSpacestation().observe(this, this::setSpacestation);
    }

    private void setSpacestation(Spacestation spacestation) {
        if (spacestation != null && spacestation.getDockingLocations() != null) {
            List<ListItem> items = new ArrayList<>();
            for (DockingLocation dockingLocation : spacestation.getDockingLocations()) {
                if (dockingLocation.getDocked() != null) {
                    DockingLocationItem item = new DockingLocationItem(dockingLocation);
                    items.add(item);
                }
            }
            adapter.clear();
            adapter.addItems(items);
        }
        if (adapter.getItemCount() > 0) {
            binding.statefulView.showContent();
        } else {
            binding.statefulView.showEmpty();
        }
    }
}
