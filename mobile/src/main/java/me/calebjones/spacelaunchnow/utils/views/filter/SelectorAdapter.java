package me.calebjones.spacelaunchnow.utils.views.filter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmList;
import me.calebjones.spacelaunchnow.databinding.FilterItemBinding;

public class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.ViewHolder> {

    private List<FilterItem> filterList;
    private FilterItemBinding binding;

    public SelectorAdapter(Context context) {
        filterList = new RealmList<>();
    }

    public void addItems(List<FilterItem> launchList) {

        if (this.filterList != null) {
            this.filterList.addAll(launchList);
        } else {
            this.filterList = new RealmList<>();
            this.filterList.addAll(launchList);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        filterList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        binding = FilterItemBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                viewGroup,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        FilterItem item = filterList.get(i);

        holder.binding.checkbox.setChecked(Prefs.getBoolean(item.getPreference_key(), true));
        holder.binding.name.setText(item.getName());

        holder.binding.rootView.setOnClickListener(v -> {
            holder.binding.checkbox.performClick();
        });
        holder.binding.checkbox.setOnClickListener(v -> {
            Prefs.putBoolean(item.getPreference_key(), holder.binding.checkbox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FilterItemBinding binding;

        //Add content to the card
        public ViewHolder(FilterItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
