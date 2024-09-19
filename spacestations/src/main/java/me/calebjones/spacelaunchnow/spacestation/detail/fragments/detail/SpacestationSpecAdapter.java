package me.calebjones.spacelaunchnow.spacestation.detail.fragments.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.calebjones.spacelaunchnow.spacestation.R;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationItemBinding;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationSpecItemBinding;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class SpacestationSpecAdapter extends RecyclerView.Adapter<SpacestationSpecAdapter.ViewHolder> {

        public int position;
        private List<SpacestationSpecItem> specs;
        private SpacestationSpecItemBinding binding;

        public SpacestationSpecAdapter() {
            specs = new ArrayList<>();
        }

        public void addItems(List<SpacestationSpecItem> items) {
            specs = items;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            binding = SpacestationSpecItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int i) {
            SpacestationSpecItem item = specs.get(i);
            holder.binding.specTitle.setText(item.getTitle());
            holder.binding.specValue.setText(item.getValue());
            holder.binding.specIcon.setImageDrawable(item.getDrawable());
        }

        @Override
        public int getItemCount() {
            return specs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private SpacestationSpecItemBinding binding;

            //Add content to the card
            public ViewHolder(SpacestationSpecItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

