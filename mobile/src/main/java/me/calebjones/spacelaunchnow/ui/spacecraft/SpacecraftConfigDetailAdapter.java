package me.calebjones.spacelaunchnow.ui.spacecraft;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmList;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.databinding.OrbiterListItemBinding;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.spacecraft.SpacecraftConfig;

public class SpacecraftConfigDetailAdapter extends RecyclerView.Adapter<SpacecraftConfigDetailAdapter.ViewHolder> {

    public int position;
    private Context context;
    private Activity activity;
    private List<SpacecraftConfig> items;
    private RequestOptions requestOptions;
    private int backgroundColor = 0;
    private SimpleDateFormat sdf;
    private OrbiterListItemBinding binding;

    public SpacecraftConfigDetailAdapter(Context context, Activity activity) {
        items = new ArrayList<>();
        requestOptions = new RequestOptions().placeholder(R.drawable.placeholder).centerCrop();
        this.context = context;
        this.activity = activity;
        sdf = Utils.getSimpleDateFormatForUI("MMMM yyyy");
        sdf.toLocalizedPattern();
    }

    public void addItems(List<SpacecraftConfig> items) {
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
        binding = OrbiterListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        SpacecraftConfig spacecraftConfig = items.get(holder.getAdapterPosition());

        //Set up vehicle card Information
        holder.binding.orbiterTitle.setText(spacecraftConfig.getName());
        holder.binding.orbiterSubtitle.setText(spacecraftConfig.getCapability());

        holder.binding.orbiterDescription.setText(spacecraftConfig.getDetails());
        holder.binding.orbiterHistoryDescription.setText(spacecraftConfig.getHistory());

        if (backgroundColor != 0) {
            holder.binding.orbiterTitle.setBackgroundColor(backgroundColor);
            holder.binding.orbiterSubtitle.setBackgroundColor(backgroundColor);
        }

        if (spacecraftConfig.getDiameter() != null) {
            holder.binding.diameter.setText(String.format(
                    context.getString(R.string.diameter_full),
                    spacecraftConfig.getDiameter()
            ));
        }
        if (spacecraftConfig.getHeight() != null) {
            holder.binding.height.setText(String.format(
                    context.getString(R.string.height_full),
                    spacecraftConfig.getHeight()
            ));
        }
        if (spacecraftConfig.getPayloadCapacity() != null) {
            holder.binding.payload.setText(String.format(
                    context.getString(R.string.payload),
                    spacecraftConfig.getPayloadCapacity()
            ));
        }

        if (spacecraftConfig.getFlightLife() != null) {
            holder.binding.flightLife.setVisibility(View.VISIBLE);
            holder.binding.flightLife.setText(String.format(
                    context.getString(me.calebjones.spacelaunchnow.R.string.flight_life),
                    spacecraftConfig.getFlightLife()
            ));
        } else {
            holder.binding.flightLife.setVisibility(View.GONE);
        }

        if (spacecraftConfig.getInUse()) {
            GlideApp.with(context).load(R.drawable.ic_checkmark).into(holder.binding.inUseIcon);
        } else {
            GlideApp.with(context).load(R.drawable.ic_failed).into(holder.binding.inUseIcon);
        }

        if (spacecraftConfig.getHumanRated() == null) {
            GlideApp.with(context)
                    .load(R.drawable.ic_question_mark)
                    .into(holder.binding.humanRatedIcon);
            holder.binding.crewCapacity.setVisibility(View.GONE);
        } else if (spacecraftConfig.getHumanRated()) {
            GlideApp.with(context)
                    .load(R.drawable.ic_checkmark)
                    .into(holder.binding.humanRatedIcon);
            holder.binding.crewCapacity.setVisibility(View.VISIBLE);
            holder.binding.crewCapacity.setText(String.format(
                    context.getString(R.string.crew_capacity),
                    spacecraftConfig.getCrewCapacity()
            ));
        } else {
            holder.binding.crewCapacity.setVisibility(View.GONE);
            GlideApp.with(context).load(R.drawable.ic_failed).into(holder.binding.humanRatedIcon);
        }

        if (spacecraftConfig.getMaidenFlight() != null) {
            holder.binding.firstFlightText.setText(sdf.format(spacecraftConfig.getMaidenFlight()));
        } else {
            holder.binding.firstFlightText.setText(R.string.unknown);
        }

        GlideApp.with(context)
                .load(spacecraftConfig.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        holder.binding.orbiterImage.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.binding.orbiterImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(holder.binding.orbiterImage);


        if (spacecraftConfig.getWikiLink() != null && !spacecraftConfig.getWikiLink().isEmpty()) {
            holder.binding.wikiButton.setVisibility(View.VISIBLE);
            holder.binding.wikiButton.setOnClickListener(v -> openCustomTab(
                    activity,
                    context,
                    spacecraftConfig.getWikiLink()
            ));
        } else {
            holder.binding.wikiButton.setVisibility(View.GONE);
        }

        if (spacecraftConfig.getInfoLink() != null && !spacecraftConfig.getInfoLink().isEmpty()) {
            holder.binding.infoButton.setVisibility(View.VISIBLE);
            holder.binding.infoButton.setOnClickListener(v -> openCustomTab(
                    activity,
                    context,
                    spacecraftConfig.getInfoLink()
            ));
        } else {
            holder.binding.infoButton.setVisibility(View.GONE);
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
        private OrbiterListItemBinding binding;

        //Add content to the card
        public ViewHolder(OrbiterListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
