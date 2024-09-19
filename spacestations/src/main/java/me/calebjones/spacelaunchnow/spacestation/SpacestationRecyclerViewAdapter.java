package me.calebjones.spacelaunchnow.spacestation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationItemBinding;
import me.calebjones.spacelaunchnow.spacestation.detail.SpacestationDetailsActivity;


public class SpacestationRecyclerViewAdapter extends RecyclerView.Adapter<SpacestationRecyclerViewAdapter.ViewHolder> {


    private List<Spacestation> spacestations;
    private Context context;
    private SpacestationItemBinding binding;

    public SpacestationRecyclerViewAdapter(Context context) {
        spacestations = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<Spacestation> spacestations) {
        this.spacestations = spacestations;
        this.notifyDataSetChanged();
    }

    public void clear() {
        spacestations = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = SpacestationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = spacestations.get(position);
        holder.binding.spacestationTitle.setText(holder.mItem.getName());
        holder.binding.spacestaionSubtitle.setText(holder.mItem.getType().getName());
        holder.binding.spacestationDescription.setText(holder.mItem.getDescription());
        holder.binding.orbitText.setText(holder.mItem.getOrbit());
        holder.binding.founded.setText(Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format((holder.mItem.getFounded())));
        if (holder.mItem.getDeorbited() != null) {
            holder.binding.deorbited.setText(Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format((holder.mItem.getDeorbited())));
        } else {
            holder.binding.deorbited.setText("N/A");
        }

        if (holder.mItem.getStatus() != null){
            holder.binding.statusText.setText(holder.mItem.getStatus().getName());
            switch (holder.mItem.getStatus().getId()){
                case 1:
                    holder.binding.statusPillLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_color_green_500));
                    break;
                case 2:
                    holder.binding.statusPillLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_color_red_500));
                    break;
                case 3:
                    holder.binding.statusPillLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_color_deep_orange_500));
                    break;
                default:
                    holder.binding.statusPillLayout.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    break;
            }
        }

        GlideApp.with(context)
                .load(holder.mItem.getImageUrl())
                .into(holder.binding.spacestationImage);
    }

    @Override
    public int getItemCount() {
        return spacestations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private SpacestationItemBinding binding;
        public Spacestation mItem;

        public ViewHolder(SpacestationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.spacestationButton.setOnClickListener(v -> {
            Spacestation spacestation = spacestations.get(getAdapterPosition());
            Intent exploreIntent = new Intent(context, SpacestationDetailsActivity.class);
            exploreIntent.putExtra("spacestationId", spacestation.getId());
            context.startActivity(exploreIntent);
            });
        }

    }
}
