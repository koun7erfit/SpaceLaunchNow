package me.calebjones.spacelaunchnow.common.ui.launchdetail.fragments.mission;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;


import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.RetroFitFragment;
import me.calebjones.spacelaunchnow.common.databinding.DetailLaunchPayloadBinding;
import me.calebjones.spacelaunchnow.common.ui.adapters.CrewAdapter;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.DetailsViewModel;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.launcher.LauncherLaunchActivity;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.Mission;
import me.calebjones.spacelaunchnow.data.models.main.launcher.LauncherConfig;
import me.calebjones.spacelaunchnow.data.models.main.spacecraft.SpacecraftStage;
import timber.log.Timber;

public class MissionDetailFragment extends RetroFitFragment {


    private Context context;
    public Launch detailLaunch;
    private DetailsViewModel model;
    private DetailLaunchPayloadBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Mission Detail Fragment");
        // retain this fragment
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        binding = DetailLaunchPayloadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void setLaunch(Launch launch) {
        Timber.v("Launch update received: %s", launch.getName());
        detailLaunch = launch;
        setUpViews(launch);
    }

    private void setUpViews(Launch launch) {
        try {
            detailLaunch = launch;

            if (detailLaunch.getMission() != null) {
                final Mission mission = detailLaunch.getMission();

                binding.payloadStatus.setText(mission.getName());
                binding.payloadDescription.setText(mission.getDescription());
                binding.payloadType.setText(mission.getTypeName());
                if (mission.getOrbit() != null && mission.getOrbit().getAbbrev() != null) {
                    binding.orbit.setVisibility(View.VISIBLE);
                    binding.orbit.setText(String.format("%s (%s)", mission.getOrbit().getName(), mission.getOrbit().getAbbrev()));
                } else {
                    binding.orbit.setVisibility(View.GONE);
                }
                binding.payloadInfoButton.setVisibility(View.GONE);
                binding.payloadWikiButton.setVisibility(View.GONE);

            } else {
                binding.payloadStatus.setText(R.string.unknown_mission_or_payload);

                binding.payloadInfoButton.setVisibility(View.GONE);
                binding.payloadWikiButton.setVisibility(View.GONE);
            }

            if(detailLaunch.getFlightclub() != null){
                binding.flightclubInfoButton.setVisibility(View.VISIBLE);
                binding.flightclubInfoButton.setOnClickListener(v -> {
                    Activity activity = (Activity) context;
                    openCustomTab(activity, context, detailLaunch.getFlightclub());
                });
            } else {
                binding.flightclubInfoButton.setVisibility(View.GONE);
            }


            binding.launchVehicle.setText(detailLaunch.getRocket().getConfiguration().getFullName());
            binding.launchConfiguration.setText(detailLaunch.getRocket().getConfiguration().getVariant());
            binding.launchFamily.setText(detailLaunch.getRocket().getConfiguration().getFamily());
            if (detailLaunch.getRocket().getConfiguration().getInfoUrl() != null &&
                !detailLaunch.getRocket().getConfiguration().getInfoUrl().isEmpty()) {
                binding.vehicleInfoButton.setOnClickListener(view -> {
                    Activity activity = (Activity) context;
                    openCustomTab(activity, context, detailLaunch.getRocket().getConfiguration().getInfoUrl());
                });
            } else {
                binding.vehicleInfoButton.setVisibility(View.GONE);
            }

            if (detailLaunch.getPatches() != null && detailLaunch.getPatches().size() > 0){
                binding.missionPatch.setVisibility(View.VISIBLE);
                GlideApp.with(context)
                        .load(detailLaunch.getPatches().get(0).getImageUrl())
                        .centerInside()
                        .into(binding.missionPatch);
            } else {
                binding.missionPatch.setVisibility(View.GONE);
            }

            if (detailLaunch.getRocket().getConfiguration().getWikiUrl() != null && detailLaunch.getRocket().getConfiguration().getWikiUrl().length() > 0) {
                binding.vehicleWikiButton.setOnClickListener(view -> {
                    Activity activity = (Activity) context;
                    openCustomTab(activity, context, detailLaunch.getRocket().getConfiguration().getWikiUrl());
                });
            } else {
                binding.vehicleWikiButton.setVisibility(View.GONE);
            }
            configureLaunchVehicle(launch.getRocket().getConfiguration());

            if (launch.getRocket().getLauncherStage() != null && launch.getRocket().getLauncherStage().size() > 0) {
                binding.coreRecyclerView.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                binding.coreRecyclerView.setLayoutManager(layoutManager);
                StageInformationAdapter stageInformationAdapter = new StageInformationAdapter(launch, context);
                binding.coreRecyclerView.setAdapter(stageInformationAdapter);
                binding.coreRecyclerView.setHasFixedSize(true);
            } else {
                binding.coreRecyclerView.setVisibility(View.GONE);
            }

            if (launch.getRocket().getSpacecraftStage() != null) {
                binding.spacecraftCard.setVisibility(View.VISIBLE);
                SpacecraftStage stage = launch.getRocket().getSpacecraftStage();
                GlideApp.with(context)
                        .load(stage.getSpacecraft().getConfiguration().getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .centerCrop()
                        .into(binding.spacecraftImage);
                binding.spacecraftTitle.setText(stage.getSpacecraft().getConfiguration().getName());
                binding.spacecraftSubTitle.setText(stage.getSpacecraft().getConfiguration().getAgency().getName());
                binding.destinationText.setText(stage.getDestination());
                binding.serialNumberText.setText(stage.getSpacecraft().getSerialNumber());
                binding.statusText.setText(stage.getSpacecraft().getStatus().getName());
                binding.description.setText(stage.getSpacecraft().getDescription());
                if (launch.getRocket().getSpacecraftStage().getLaunchCrew() != null
                    && !launch.getRocket().getSpacecraftStage().getLaunchCrew().isEmpty()) {
                    binding.crewRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    binding.crewRecyclerView.setAdapter(new CrewAdapter(context,
                            launch.getRocket().getSpacecraftStage().getLaunchCrew()));
                }
            } else {
                binding.spacecraftCard.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    private void configureLaunchVehicle(LauncherConfig launchVehicle) {
        if (launchVehicle != null) {
            try {
                if (launchVehicle.getLength() != null) {
                    binding.launchVehicleSpecsHeight.setText(String.format(context.getString(R.string.height_value), launchVehicle.getLength()));
                } else {
                    binding.launchVehicleSpecsHeight.setText(" - ");
                }

                if (launchVehicle.getDiameter() != null) {
                    binding.launchVehicleSpecsDiameter.setText(String.format(context.getString(R.string.diameter_value), launchVehicle.getDiameter()));
                } else {
                    binding.launchVehicleSpecsDiameter.setText(" - ");
                }

                if (launchVehicle.getMaxStage() != null) {
                    binding.launchVehicleSpecsStages.setText(String.format(context.getString(R.string.stage_value), launchVehicle.getMaxStage()));
                } else {
                    binding.launchVehicleSpecsStages.setText(" - ");
                }

                if (launchVehicle.getLeoCapacity() != null) {
                    binding.launchVehicleSpecsLeo.setText(String.format(context.getString(R.string.mass_leo_value), launchVehicle.getLeoCapacity()));
                } else {
                    binding.launchVehicleSpecsLeo.setText(" - ");
                }

                if (launchVehicle.getGtoCapacity() != null) {
                    binding.launchVehicleSpecsGto.setText(String.format(context.getString(R.string.mass_gto_value), launchVehicle.getGtoCapacity()));
                } else {
                    binding.launchVehicleSpecsGto.setText(" - ");
                }

                if (launchVehicle.getLaunchMass() != null) {
                    binding.launchVehicleSpecsLaunchMass.setText(String.format(context.getString(R.string.mass_launch_value), launchVehicle.getLaunchMass()));
                } else {
                    binding.launchVehicleSpecsLaunchMass.setText(" - ");
                }

                if (launchVehicle.getToThrust() != null) {
                    binding.launchVehicleSpecsThrust.setText(String.format(context.getString(R.string.thrust_value), launchVehicle.getToThrust()));
                } else {
                    binding.launchVehicleSpecsThrust.setText(" - ");
                }

                if (launchVehicle.getConsecutiveSuccessfulLaunches() != null) {
                    binding.consecutiveSuccessValue.setText(String.valueOf(launchVehicle.getConsecutiveSuccessfulLaunches()));
                } else {
                    binding.consecutiveSuccessValue.setText(" - ");
                }

                if (launchVehicle.getSuccessfulLaunches() != null) {
                    binding.launchSuccessValue.setText(String.valueOf(launchVehicle.getSuccessfulLaunches()));
                } else {
                    binding.launchSuccessValue.setText(" - ");
                }

                if (launchVehicle.getTotalLaunchCount() != null) {
                    binding.launchTotalValue.setText(String.valueOf(launchVehicle.getTotalLaunchCount()));
                } else {
                    binding.launchTotalValue.setText(" - ");
                }

                if (launchVehicle.getFailedLaunches() != null) {
                    binding.launchFailureValue.setText(String.valueOf(launchVehicle.getFailedLaunches()));
                } else {
                    binding.launchFailureValue.setText(" - ");
                }

                if (launchVehicle.getMaidenFlight() != null){
                    //Setup SimpleDateFormat to parse out getNet launchDate.
                    SimpleDateFormat output = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy");
                    binding.maidenValue.setText(output.format(launchVehicle.getMaidenFlight()));
                }

                if (launchVehicle.getDescription() != null && !launchVehicle.getDescription().isEmpty()) {
                    binding.launchVehicleDescription.setText(launchVehicle.getDescription());
                    binding.launchVehicleDescription.setVisibility(View.VISIBLE);
                } else {
                    binding.launchVehicleDescription.setVisibility(View.GONE);
                }

                binding.launcherLaunches.setText(String.format(getString(R.string.view_rocket_launches), launchVehicle.getName()));
                binding.launcherLaunches.setOnClickListener(v -> {
                    Intent launches = new Intent(context, LauncherLaunchActivity.class);
                    launches.putExtra("launcherId", launchVehicle.getId());
                    launches.putExtra("launcherName", launchVehicle.getName());
                    context.startActivity(launches);
                });

            } catch (NullPointerException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        // update UI
        model.getLaunch().observe(this, this::setLaunch);
    }

    public static MissionDetailFragment newInstance() {
        return new MissionDetailFragment();
    }



}
