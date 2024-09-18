package me.calebjones.spacelaunchnow.common.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmList;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.databinding.SpacestationListItemBinding;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import timber.log.Timber;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class SpacestationAdapter extends RecyclerView.Adapter<SpacestationAdapter.ViewHolder> {
    public int position;
    private RealmList<Spacestation> spacestations;
    private Context mContext;
    private SpacestationListItemBinding binding;

    public SpacestationAdapter(Context context) {
        spacestations = new RealmList<>();
        mContext = context;
    }

    public void addItems(List<Spacestation> spacestations) {

        if (this.spacestations != null) {
            this.spacestations.addAll(spacestations);
        } else {
            this.spacestations = new RealmList<>();
            this.spacestations.addAll(spacestations);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        spacestations.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Timber.v("onCreate ViewHolder.");
        binding = SpacestationListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        final Spacestation item = spacestations.get(i);

        if (item.getImageUrl() != null) {
            GlideApp.with(mContext)
                    .load(item.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(holder.binding.spacestationIcon);
        } else {
            GlideApp.with(mContext)
                    .load(R.drawable.ic_satellite)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(holder.binding.spacestationIcon);
        }

        holder.binding.spacestationName.setText(item.getName());
        holder.binding.spacestationLocation.setText(item.getOrbit());
        holder.binding.spacestationStatus.setText(item.getStatus().getName());
    }

    @Override
    public int getItemCount() {
        return spacestations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SpacestationListItemBinding binding;

        //Add content to the card
        public ViewHolder(SpacestationListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        //React to click events.
        @Override
        public void onClick(View v) {
            Spacestation spacestation = spacestations.get(getAdapterPosition());
            Intent exploreIntent = null;
            try {
                exploreIntent = new Intent(mContext, Class.forName("me.calebjones.spacelaunchnow.spacestation.detail.SpacestationDetailsActivity"));
                exploreIntent.putExtra("spacestationId", spacestation.getId());
                mContext.startActivity(exploreIntent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}

