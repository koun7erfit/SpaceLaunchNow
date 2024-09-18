package me.calebjones.spacelaunchnow.starship.ui.vehicles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;


import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.databinding.StatusPillMiniBinding;
import me.calebjones.spacelaunchnow.data.models.main.launcher.Launcher;
import me.spacelaunchnow.starship.R;
import me.spacelaunchnow.starship.databinding.LauncherItemBinding;

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {

    private List<Launcher> roadClosureList;
    private Context context;
    private LauncherItemBinding binding;
    private StatusPillMiniBinding statusPillMiniBinding;

    public LauncherAdapter(Context context) {
        roadClosureList = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<Launcher> events) {
        this.roadClosureList = events;
        this.notifyDataSetChanged();
    }

    public void clear() {
        roadClosureList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = LauncherItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Launcher launcher = roadClosureList.get(position);

        if (launcher.getImageUrl() != null) {
            GlideApp.with(context)
                    .load(launcher.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.imageView);
        }
        String status = "";
        if (launcher.getStatus() != null && !launcher.getStatus().isEmpty()) {
            status = launcher.getStatus().substring(0, 1).toUpperCase() + launcher.getStatus().substring(1).toLowerCase();
        }


        holder.binding.title.setText(launcher.getLauncherConfig().getName() + " - " + launcher.getSerialNumber());
        if (status.toLowerCase().contains("active")) {
            holder.binding.statusPillMini.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.material_color_green_600));
        } else if (status.toLowerCase().contains("destroyed")) {
            holder.binding.statusPillMini.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.material_color_red_600));
        } else {
            holder.binding.statusPillMini.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.material_color_blue_grey_500));
        }
        holder.binding.statusPillMini.status.setText(status);
        holder.binding.flights.setText("Flights: " + launcher.getPreviousFlights().toString());
        holder.binding.description.setText(launcher.getDetails());
    }

    @Override
    public int getItemCount() {
        return roadClosureList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LauncherItemBinding binding;

        public ViewHolder(LauncherItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
