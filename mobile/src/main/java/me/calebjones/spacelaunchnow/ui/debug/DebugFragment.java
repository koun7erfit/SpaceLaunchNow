package me.calebjones.spacelaunchnow.ui.debug;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

import me.calebjones.spacelaunchnow.data.models.Constants;
import me.calebjones.spacelaunchnow.common.ui.views.SnackbarHandler;
import me.calebjones.spacelaunchnow.databinding.FragmentDebugBinding;

public class DebugFragment extends Fragment implements DebugContract.View {


    private DebugContract.Presenter debugPresenter;
    private FragmentDebugBinding binding;

    public DebugFragment() {
        // Requires empty public constructor
    }

    public static DebugFragment newInstance() {
        return new DebugFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDebugBinding.inflate(inflater, container, false);
        initializeViews();

        binding.supporterSwitch.setOnClickListener(view -> supportSwitchClicked());
        binding.nextLaunchButton.setOnClickListener(view -> nextLaunchClicked());
        binding.backgroundSyncButton.setOnClickListener(view -> backgroundSyncClicked());
        binding.vehicleSyncButton.setOnClickListener(view -> vehicleSyncClicked());
        binding.downloadFile.setOnClickListener(view -> downloadFileClicked());
        binding.deleteFile.setOnClickListener(view -> deleteFileClicked());
        binding.jobEventButton.setOnClickListener(view -> jobEventClicked());

        return binding.getRoot();
    }

    @Override
    public void setPresenter(DebugContract.Presenter presenter) {
        debugPresenter = presenter;
    }

    @Override
    public void showDebugLaunchSnackbar(boolean state) {
        SnackbarHandler.showInfoSnackbar(
                getContext(),
                binding.debugCoordinatorLayout,
                "Debug Launch " +
                "Status: " + state
        );
    }

    @Override
    public void showSupporterSnackbar(boolean state) {
        SnackbarHandler.showInfoSnackbar(
                getContext(),
                binding.debugCoordinatorLayout,
                "Supporter Status: " + state
        );
    }

    @Override
    public void setSupporterSwitch(boolean state) {
        binding.supporterSwitch.setChecked(state);
    }

    @Override
    public void showSnackbarMessage(String message) {
        SnackbarHandler.showInfoSnackbar(getContext(), binding.debugCoordinatorLayout, message);
    }

    //UI Button and Switches
//    @OnClick(R.id.supporter_switch)
    void supportSwitchClicked() {
        debugPresenter.toggleSupporterSwitch(binding.supporterSwitch.isChecked());
    }

    //    @OnClick(R.id.next_launch_button)
    void nextLaunchClicked() {
        debugPresenter.syncNextLaunchClicked(getContext());
    }

    //    @OnClick(R.id.background_sync_button)
    void backgroundSyncClicked() {
        debugPresenter.syncBackgroundSyncClicked(getContext());
    }

    //    @OnClick(R.id.vehicle_sync_button)
    void vehicleSyncClicked() {
        debugPresenter.syncVehiclesClicked(getContext());
    }

    //    @OnClick(R.id.download_file)
    void downloadFileClicked() {
        debugPresenter.downloadLogsClicked(getActivity());
    }

    //    @OnClick(R.id.delete_file)
    void deleteFileClicked() {
        debugPresenter.deleteFilesClicked(getContext());
    }

    //    @OnClick(R.id.job_event_button)
    void jobEventClicked() {
        debugPresenter.jobEventButtonClicked(getContext());
    }

    public void initializeViews() {
        binding.supporterSwitch.setChecked(debugPresenter.getSupporterStatus());
        List<String> endpoints = new ArrayList<>();

        endpoints.add(Constants.API_BASE_URL);
        endpoints.add(Constants.API_DEV_BASE_URL);
        endpoints.add(Constants.API_DEBUG_BASE_URL);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, endpoints);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.debugLaunchesSpinner.setAdapter(adapter);
        binding.debugLaunchesSpinner.setPrompt("Select Endpoint");
        binding.debugLaunchesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view,
                                       int position,
                                       long id) {
                debugPresenter.endpointSelectorClicked(adapterView
                        .getItemAtPosition(position)
                        .toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
