package me.calebjones.spacelaunchnow.common.ui.launchdetail.fragments;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.app.Activity;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.databinding.DetailLaunchAgencyBinding;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.DetailsViewModel;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.agency.AgencyLaunchActivity;
import timber.log.Timber;

public class AgencyDetailFragment extends BaseFragment {

    private Context context;

    public static Launch detailLaunch;
    private DetailsViewModel model;
    private DetailLaunchAgencyBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Agency Detail Fragment");
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DetailLaunchAgencyBinding.inflate(inflater, container, false);
        binding.lspInfoButtonOne.setOnClickListener(view -> onLspInfoButtonOneClicked());
        binding.lspWikiButtonOne.setOnClickListener(view -> onLspWikiButtonOneClicked());
        binding.lspAgency.setOnClickListener(view -> onViewClicked());
        context = getContext();
        return binding.getRoot();
    }

    public void setLaunch(Launch launch) {
        detailLaunch = launch;
        setUpViews(launch);
    }

    private void setUpViews(Launch launch) {
        try {
            detailLaunch = launch;

            Timber.v("Setting up views...");
            binding.lspCard.setVisibility(View.VISIBLE);

            binding.lspAgency.setText(String.format(this.getString(R.string.view_rocket_launches),
                    launch.getLaunchServiceProvider().getName()));
            if (detailLaunch.getLaunchServiceProvider().getLogoUrl() != null) {
                binding.lspLogo.setVisibility(View.VISIBLE);
                GlideApp.with(context)
                        .load(detailLaunch.getLaunchServiceProvider().getLogoUrl())
                        .centerInside()
                        .into(binding.lspLogo);
            } else {
                binding.lspLogo.setVisibility(View.GONE);
            }
            binding.lspName.setText(detailLaunch.getLaunchServiceProvider().getName());
            binding.lspType.setText(detailLaunch.getLaunchServiceProvider().getType());
            if (detailLaunch.getLaunchServiceProvider().getAdministrator() != null) {
                binding.lspAdministrator.setText(String.format("%s", detailLaunch.getLaunchServiceProvider().getAdministrator()));
            } else {
                binding.lspAdministrator.setText(R.string.unknown_administrator);
            }
            if (detailLaunch.getLaunchServiceProvider().getFoundingYear() != null) {
                binding.lspFoundedYear.setText(String.format(getString(R.string.founded_in), detailLaunch.getLaunchServiceProvider().getFoundingYear()));
            } else {
                binding.lspFoundedYear.setText(R.string.unknown_year);
            }
            binding.lspSummary.setText(detailLaunch.getLaunchServiceProvider().getDescription());
            if (detailLaunch.getLaunchServiceProvider().getInfoUrl() == null) {
                binding.lspInfoButtonOne.setVisibility(View.GONE);
            }

            if (detailLaunch.getLaunchServiceProvider().getWikiUrl() == null) {
                binding.lspWikiButtonOne.setVisibility(View.GONE);
            }
            binding.lspAgency.setVisibility(View.VISIBLE);

            if (detailLaunch.getLaunchServiceProvider() != null) {
                Agency agency = detailLaunch.getLaunchServiceProvider();

                if (agency.getTotalLaunchCount() != null) {
                    binding.launchTotalValue.setText(String.valueOf(agency.getTotalLaunchCount()));
                } else {
                    binding.launchTotalValue.setText(" - ");
                }

                if (agency.getSuccessfulLaunches() != null) {
                    binding.launchSuccessValue.setText(String.valueOf(agency.getSuccessfulLaunches()));
                } else {
                    binding.launchSuccessValue.setText(" - ");
                }

                if (agency.getConsecutiveSuccessfulLaunches() != null) {
                    binding.consecutiveSuccessValue.setText(String.valueOf(agency.getConsecutiveSuccessfulLaunches()));
                } else {
                    binding.consecutiveSuccessValue.setText(" - ");
                }

                if (agency.getFailedLaunches() != null) {
                    binding.launchFailureValue.setText(String.valueOf(agency.getFailedLaunches()));
                } else {
                    binding.launchFailureValue.setText(" - ");
                }

                if (agency.getAttemptedLandings() != null) {
                    binding.landingTotalValue.setText(String.valueOf(agency.getAttemptedLandings()));
                } else {
                    binding.landingTotalValue.setText(" - ");
                }

                if (agency.getSuccessfulLandings() != null) {
                    binding.landingSuccessValue.setText(String.valueOf(agency.getSuccessfulLandings()));
                } else {
                    binding.landingSuccessValue.setText(" - ");
                }

                if (agency.getConsecutiveSuccessfulLandings() != null) {
                    binding.consecutiveLandingSuccessValue.setText(String.valueOf(agency.getConsecutiveSuccessfulLandings()));
                } else {
                    binding.consecutiveLandingSuccessValue.setText(" - ");
                }

                if (agency.getFailedLandings() != null) {
                    binding.landingFailureValue.setText(String.valueOf(agency.getFailedLandings()));
                } else {
                    binding.landingFailureValue.setText(" - ");
                }
            }
        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        model.getLaunch().observe(this, this::setLaunch);
    }

    public static AgencyDetailFragment newInstance() {
        return new AgencyDetailFragment();
    }

    public void onLspInfoButtonOneClicked() {
        Activity activity = (Activity) context;
        openCustomTab(activity, context, detailLaunch.getLaunchServiceProvider().getInfoUrl());
    }

    public void onLspWikiButtonOneClicked() {
        Activity activity = (Activity) context;
        openCustomTab(activity, context, detailLaunch.getLaunchServiceProvider().getWikiUrl());
    }

    public void onViewClicked() {
        Intent intent = new Intent(context, AgencyLaunchActivity.class);
        intent.putExtra("lspName", detailLaunch.getLaunchServiceProvider().getName());
        startActivity(intent);
    }
}
