package me.calebjones.spacelaunchnow.astronauts.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.ui.adapters.ListAdapter;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.data.models.main.astronaut.Astronaut;
import me.spacelaunchnow.astronauts.databinding.AstronautFlightFragmentBinding;

public class AstronautFlightsFragment extends BaseFragment {

    private AstronautDetailViewModel mViewModel;
    private LinearLayoutManager linearLayoutManager;
    private ListAdapter adapter;
    private Context context;
    private AstronautFlightFragmentBinding binding;

    public static AstronautFlightsFragment newInstance() {
        return new AstronautFlightsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AstronautFlightFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        linearLayoutManager = new LinearLayoutManager(context);

        adapter = new ListAdapter(context, ThemeHelper.isDarkMode(getActivity()));
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        binding.recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(AstronautDetailViewModel.class);
        // update UI
        mViewModel.getAstronaut().observe(this, this::setAstronaut);
    }

    private void setAstronaut(Astronaut astronaut) {
        if (astronaut != null && astronaut.getFlights() != null){
            adapter.clear();
            adapter.addItems(astronaut.getFlights());
        }
    }
}
