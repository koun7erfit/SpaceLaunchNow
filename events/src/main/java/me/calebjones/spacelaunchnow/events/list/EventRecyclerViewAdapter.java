package me.calebjones.spacelaunchnow.events.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.Event;
import me.calebjones.spacelaunchnow.events.R;
import me.calebjones.spacelaunchnow.events.databinding.EventItemBinding;
import me.calebjones.spacelaunchnow.events.detail.EventDetailsActivity;


public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {


    private List<Event> events;
    private Context context;
    private EventItemBinding binding;

    public EventRecyclerViewAdapter(Context context) {
        events = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<Event> events) {
        this.events = events;
        this.notifyDataSetChanged();
    }

    public void clear() {
        events = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = EventItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = binding.getRoot();
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = events.get(position);
        holder.binding.eventTitle.setText(holder.mItem.getName());
        holder.binding.eventDate.setText(Utils.getSimpleDateFormatForUIWithPrecision(holder.mItem.getDatePrecision()).format(holder.mItem.getDate()));
        holder.binding.eventDescription.setText(holder.mItem.getDescription());
        holder.binding.eventType.setText(holder.mItem.getType().getName());

        if (holder.mItem.getVideoUrl() != null){
            holder.binding.watchButton.setVisibility(View.VISIBLE);
        } else {
            holder.binding.watchButton.setVisibility(View.GONE);
        }

        GlideApp.with(context)
                .load(holder.mItem.getFeatureImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.eventImage);

        holder.binding.watchButton.setOnClickListener(view1 -> onWatchClick(holder.mItem));
        holder.binding.details.setOnClickListener(view2 -> onClick(holder.mItem));
    }

    void onClick(Event event){
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("eventId", event.getId());
        context.startActivity(intent);
    }

    void onWatchClick(Event event){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(event.getVideoUrl()));
        context.startActivity(i);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public Event mItem;
        private EventItemBinding binding;

        public ViewHolder(EventItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
