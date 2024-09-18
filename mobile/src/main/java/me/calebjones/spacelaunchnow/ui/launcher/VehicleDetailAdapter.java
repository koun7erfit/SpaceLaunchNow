package me.calebjones.spacelaunchnow.ui.launcher;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.realm.RealmList;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.data.models.main.launcher.LauncherConfig;
import me.calebjones.spacelaunchnow.databinding.VehicleListItemBinding;
import me.calebjones.spacelaunchnow.ui.imageviewer.FullscreenImageActivity;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.launcher.LauncherLaunchActivity;
import me.calebjones.spacelaunchnow.common.GlideApp;

public class VehicleDetailAdapter extends RecyclerView.Adapter<VehicleDetailAdapter.ViewHolder> {

    public int position;
    private Context mContext;
    private Activity activity;
    private List<LauncherConfig> items;
    private RequestOptions requestOptions;
    private int backgroundColor = 0;
    private VehicleListItemBinding binding;

    public VehicleDetailAdapter(Context context, Activity activity) {
        items = new ArrayList<>();
        requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .centerCrop();
        mContext = context;
        this.activity = activity;
    }

    public void addItems(List<LauncherConfig> items) {
        if (this.items != null) {
            this.items.addAll(items);
        } else {
            this.items = new RealmList<>();
            this.items.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        binding = VehicleListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        LauncherConfig launchVehicle = items.get(holder.getAdapterPosition());

        if (launchVehicle != null) {

            holder.binding.launcherLaunches.setText(String.format(mContext.getString(R.string.view_rocket_launches), launchVehicle.getFullName()));
            if (launchVehicle.getDescription() != null && !launchVehicle.getDescription().isEmpty()) {
                holder.binding.launchVehicleDescription.setVisibility(View.VISIBLE);
                holder.binding.launchVehicleDescription.setText(launchVehicle.getDescription());
            } else {
                holder.binding.launchVehicleDescription.setVisibility(View.GONE);
            }

            if (launchVehicle.getLength() != null) {
                holder.binding.launchVehicleSpecsHeight.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.height_value), launchVehicle.getLength()));
            } else {
                holder.binding.launchVehicleSpecsHeight.setText(" - ");
            }

            if (launchVehicle.getDiameter() != null) {
                holder.binding.launchVehicleSpecsDiameter.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.diameter_value), launchVehicle.getDiameter()));
            } else {
                holder.binding.launchVehicleSpecsDiameter.setText(" - ");
            }

            if (launchVehicle.getMaxStage() != null) {
                holder.binding.launchVehicleSpecsStages.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.stage_value), launchVehicle.getMaxStage()));
            } else {
                holder.binding.launchVehicleSpecsStages.setText(" - ");
            }

            if (launchVehicle.getLeoCapacity() != null) {
                holder.binding.launchVehicleSpecsLeo.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.mass_leo_value), launchVehicle.getLeoCapacity()));
            } else {
                holder.binding.launchVehicleSpecsLeo.setText(" - ");
            }

            if (launchVehicle.getGtoCapacity() != null) {
                holder.binding.launchVehicleSpecsGto.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.mass_gto_value), launchVehicle.getGtoCapacity()));
            } else {
                holder.binding.launchVehicleSpecsGto.setText(" - ");
            }

            if (launchVehicle.getLaunchMass() != null) {
                holder.binding.launchVehicleSpecsLaunchMass.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.mass_launch_value), launchVehicle.getLaunchMass()));
            } else {
                holder.binding.launchVehicleSpecsLaunchMass.setText(" - ");
            }

            if (launchVehicle.getToThrust() != null) {
                holder.binding.launchVehicleSpecsThrust.setText(String.format(mContext.getString(me.calebjones.spacelaunchnow.common.R.string.thrust_value), launchVehicle.getToThrust()));
            } else {
                holder.binding.launchVehicleSpecsThrust.setText(" - ");
            }

            if (launchVehicle.getConsecutiveSuccessfulLaunches() != null) {
                holder.binding.consecutiveSuccessValue.setText(String.valueOf(launchVehicle.getConsecutiveSuccessfulLaunches()));
            } else {
                holder.binding.consecutiveSuccessValue.setText(" - ");
            }

            if (launchVehicle.getSuccessfulLaunches() != null) {
                holder.binding.launchSuccessValue.setText(String.valueOf(launchVehicle.getSuccessfulLaunches()));
            } else {
                holder.binding.launchSuccessValue.setText(" - ");
            }

            if (launchVehicle.getTotalLaunchCount() != null) {
                holder.binding.launchTotalValue.setText(String.valueOf(launchVehicle.getTotalLaunchCount()));
            } else {
                holder.binding.launchTotalValue.setText(" - ");
            }

            if (launchVehicle.getFailedLaunches() != null) {
                holder.binding.launchFailureValue.setText(String.valueOf(launchVehicle.getFailedLaunches()));
            } else {
                holder.binding.launchFailureValue.setText(" - ");
            }


            if (backgroundColor != 0) {
                holder.binding.launchVehicleTitle.setBackgroundColor(backgroundColor);
                holder.binding.launchVehicle.setBackgroundColor(backgroundColor);
            }
            if (launchVehicle.getImageUrl() != null && !launchVehicle.getImageUrl().isEmpty()) {
                holder.binding.launchVehicle.setVisibility(View.VISIBLE);
                GlideApp.with(mContext)
                        .load(launchVehicle.getImageUrl())
                        .apply(requestOptions)
                        .into(holder.binding.imageView);
            } else {

                holder.binding.imageView.setVisibility(View.GONE);
            }
            holder.binding.launchVehicleTitle.setText(launchVehicle.getFullName());
            holder.binding.launchVehicle.setText(launchVehicle.getFamily());

            if (launchVehicle.getInfoUrl() != null
                && !launchVehicle.getInfoUrl().isEmpty()
                && !launchVehicle.getInfoUrl().contains("null")) {
                holder.binding.vehicleInfoButton.setVisibility(View.VISIBLE);
            } else {
                holder.binding.vehicleInfoButton.setVisibility(View.INVISIBLE);
            }


            if (launchVehicle.getWikiUrl() != null
                && !launchVehicle.getWikiUrl().isEmpty()
                && !launchVehicle.getWikiUrl().contains("null")) {
                holder.binding.vehicleWikiButton.setVisibility(View.VISIBLE);
            } else {
                holder.binding.vehicleWikiButton.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateColor(int color) {
        backgroundColor = color;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        
        private VehicleListItemBinding binding;

        public ViewHolder(VehicleListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.launcherLaunches.setOnClickListener(v -> {
                Intent launches = new Intent(activity, LauncherLaunchActivity.class);
                launches.putExtra("launcherId", items.get(getAdapterPosition()).getId());
                launches.putExtra("launcherName", items.get(getAdapterPosition()).getName());
                activity.startActivity(launches);
            });
            binding.vehicleInfoButton.setOnClickListener(v -> openCustomTab(activity, mContext,
                    items.get(getAdapterPosition()).getInfoUrl()));
            binding.vehicleWikiButton.setOnClickListener(v -> openCustomTab(activity, mContext,
                    items.get(getAdapterPosition()).getWikiUrl()));
            binding.imageView.setOnClickListener(v -> {
                Intent animateIntent = new Intent(activity, FullscreenImageActivity.class);
                animateIntent.putExtra("imageURL", items.get(getAdapterPosition()).getImageUrl());
                activity.startActivity(animateIntent,
                        ActivityOptions
                                .makeSceneTransitionAnimation(activity, binding.imageView, "imageCover")
                                .toBundle()
                );
            });
        }
    }
}
