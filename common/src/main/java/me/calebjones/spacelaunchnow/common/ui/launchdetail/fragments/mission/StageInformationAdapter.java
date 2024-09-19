package me.calebjones.spacelaunchnow.common.ui.launchdetail.fragments.mission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.databinding.CoreInformationBinding;
import me.calebjones.spacelaunchnow.data.models.main.Landing;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.launcher.Launcher;
import me.calebjones.spacelaunchnow.data.models.main.launcher.LauncherStage;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.launcher.LauncherLaunchActivity;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class StageInformationAdapter extends RecyclerView.Adapter<StageInformationAdapter.ViewHolder> {
    public int position;
    private List<LauncherStage> launcherList;
    private Context context;
    private Launch launch;
    private CoreInformationBinding binding;

    public StageInformationAdapter(Launch launch, Context context) {
        this.launch = launch;
        launcherList = launch.getRocket().getLauncherStage();
        this.context = context;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        binding = CoreInformationBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup,
                false
        );
        return new ViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        LauncherStage stage = launcherList.get(position);
        if (stage.getLauncher() != null) {
            Launcher launcher = stage.getLauncher();
            holder.binding.viewCoreLaunches.setVisibility(View.VISIBLE);
            holder.binding.viewCoreLaunches.setText(String.format(
                    context.getString(R.string.view_x_launches),
                    stage.getLauncher().getSerialNumber()
            ));
            holder.binding.viewCoreLaunches.setOnClickListener(v -> {
                Intent launches = new Intent(context, LauncherLaunchActivity.class);
                launches.putExtra("serialNumber", launcher.getSerialNumber());
                context.startActivity(launches);
            });

            if (launcher.getImageUrl() != null) {
                GlideApp.with(context)
                        .load(launcher.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.coreImage);
            }
            holder.binding.coreInformation.setText(String.format(
                    context.getString(R.string.booster_information),
                    launcher.getSerialNumber()
            ));
            if (stage.getType() != null) {
                holder.binding.coreInformationSubtitle.setText(String.format(
                        context.getString(R.string.first_stage_x),
                        stage.getType()
                ));
            }
            holder.binding.details.setText(launcher.getDetails());
            holder.binding.serialNumberText.setText(stage.getLauncher().getSerialNumber());
            String cap = launcher.getStatus().substring(0, 1).toUpperCase() +
                         stage.getLauncher().getStatus().substring(1);
            holder.binding.statusText.setText(cap);
            holder.binding.previousText.setText("");

            if (launcher.getFlightProven() == null) {
                holder.binding.flightProven.setImageResource(R.drawable.ic_question_mark);
            } else if (launcher.getFlightProven()) {
                holder.binding.flightProven.setImageResource(R.drawable.ic_checkmark);
            } else if (!launcher.getFlightProven()) {
                holder.binding.flightProven.setImageResource(R.drawable.ic_failed);
            }

            holder.binding.previousText.setText(String.format(
                    "%d",
                    stage.getLauncher().getPreviousFlights()
            ));

            if (launcher.getAttemptedLandings() != null) {
                holder.binding.landingTotalValue.setText(String.valueOf(launcher.getAttemptedLandings()));
            } else {
                holder.binding.landingTotalValue.setText(" - ");
            }

            if (launcher.getSuccessfulLandings() != null) {
                holder.binding.landingSuccessCountValue.setText(String.valueOf(launcher.getSuccessfulLandings()));
            } else {
                holder.binding.landingSuccessCountValue.setText(" - ");
            }

        } else {
            holder.binding.viewCoreLaunches.setVisibility(View.GONE);
        }

        if (launch.getMission() != null && launch.getMission().getName() != null) {
            if (stage.getLanding() != null) {
                holder.binding.landingInformationTitle.setText(String.format(
                        context.getString(R.string.x_landing_information),
                        launch.getMission().getName()
                ));
            } else {
                holder.binding.landingInformationTitle.setText("Landing Information Unavailable");
            }
        }

        holder.binding.landingGroup.setVisibility(View.GONE);
        holder.binding.landingGroupLandingtype.setVisibility(View.GONE);
        holder.binding.landingGroupLandinglocation.setVisibility(View.GONE);
        holder.binding.landingMore.setVisibility(View.GONE);

        if (stage.getLanding() != null) {
            holder.binding.landingGroup.setVisibility(View.VISIBLE);
            Landing landing = stage.getLanding();

            if (landing.getAttempt() == null) {
                holder.binding.attemptIcon.setImageResource(R.drawable.ic_question_mark);
            } else if (landing.getAttempt()) {
                holder.binding.attemptIcon.setImageResource(R.drawable.ic_checkmark);
            } else if (!landing.getAttempt()) {
                holder.binding.attemptIcon.setImageResource(R.drawable.ic_failed);
            }

            if (landing.getSuccess() == null) {
                holder.binding.successIcon.setImageResource(R.drawable.ic_question_mark);
            } else if (landing.getSuccess()) {
                holder.binding.successIcon.setImageResource(R.drawable.ic_checkmark);
            } else if (!landing.getSuccess()) {
                holder.binding.successIcon.setImageResource(R.drawable.ic_failed);
            }

            if (!landing.getDescription().isEmpty()) {
                holder.binding.landingDescription.setText(landing.getDescription());
                holder.binding.landingDescription.setOnClickListener(v -> {
                    if (!holder.binding.landingDescription.isExpanded()) {
                        holder.binding.landingDescription.expand();
                    }
                });
            } else {
                holder.binding.landingDescription.setVisibility(View.GONE);
            }

            if (landing.getLandingType() != null && landing.getLandingType().getName() != null) {
                holder.binding.landingType.setText(landing.getLandingType().getName());
                holder.binding.landingGroupLandingtype.setVisibility(View.VISIBLE);
            }

            if (landing.getLandingLocation() != null &&
                landing.getLandingLocation().getName() != null) {
                holder.binding.landingLocation.setText(landing.getLandingLocation().getName());
                holder.binding.landingGroupLandinglocation.setVisibility(View.VISIBLE);
            }

            if (landing.getLandingLocation() != null
                && landing.getLandingLocation().getName() != null
                && landing.getLandingLocation().getDescription() != null
                && landing.getLandingType() != null
                && landing.getLandingType().getName() != null
                && landing.getLandingType().getDescription() != null) {
                holder.binding.landingMore.setVisibility(View.VISIBLE);
                holder.binding.landingMore.setOnClickListener((View v) -> {
                    MaterialDialog dialog = new MaterialDialog.Builder(context)
                            .title(context.getString(R.string.additional_landing_information))
                            .customView(R.layout.landing_information, true)
                            .positiveText("Close")
                            .show();
                    View view = dialog.getCustomView();

                    TextView landingType = view.findViewById(R.id.landing_type);
                    TextView landingTypeDescription =
                            view.findViewById(R.id.landing_type_description);
                    TextView landingLocation = view.findViewById(R.id.landing_location);
                    TextView landingLocationDescription =
                            view.findViewById(R.id.landing_location_description);

                    landingType.setText(landing.getLandingType().getName());
                    landingTypeDescription.setText(landing.getLandingType().getDescription());
                    landingLocation.setText(landing.getLandingLocation().getName());
                    landingLocationDescription.setText(landing.getLandingLocation()
                                                              .getDescription());
                });
            } else {
                holder.binding.landingMore.setVisibility(View.GONE);
            }

        } else {
            holder.binding.landingGroup.setVisibility(View.GONE);
            holder.binding.landingGroupLandingtype.setVisibility(View.GONE);
            holder.binding.landingGroupLandinglocation.setVisibility(View.GONE);
            holder.binding.landingMore.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return launcherList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CoreInformationBinding binding;

        //Add content to the card
        public ViewHolder(CoreInformationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
