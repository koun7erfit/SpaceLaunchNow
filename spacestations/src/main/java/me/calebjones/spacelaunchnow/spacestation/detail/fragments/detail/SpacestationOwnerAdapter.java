package me.calebjones.spacelaunchnow.spacestation.detail.fragments.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.spacestation.R;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationOwnerItemBinding;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class SpacestationOwnerAdapter extends RecyclerView.Adapter<SpacestationOwnerAdapter.ViewHolder> {

        public int position;
        private Context context;
        private List<Agency> agencies;
        private SpacestationOwnerItemBinding binding;

        public SpacestationOwnerAdapter(Context context) {
            this.context = context;
            agencies = new ArrayList<>();
        }

        public void addItems(List<Agency> items) {
            agencies = items;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            binding = SpacestationOwnerItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int i) {
            Agency item = agencies.get(i);
            holder.binding.ownerTitle.setText(item.getName());
            holder.binding.ownerSubtitle.setText(String.format("%s - %s", item.getType(), item.getAbbrev()));
            GlideApp.with(context).load(item.getImageUrl()).circleCrop().placeholder(R.drawable.placeholder).into(holder.binding.ownerIcon);
        }

        @Override
        public int getItemCount() {
            return agencies.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private SpacestationOwnerItemBinding binding;

            //Add content to the card
            public ViewHolder(SpacestationOwnerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

