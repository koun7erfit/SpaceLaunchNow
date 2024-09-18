package me.calebjones.spacelaunchnow.starship.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.starship.RoadClosure;
import me.spacelaunchnow.starship.R;

public class RoadClosureAdapter extends RecyclerView.Adapter<RoadClosureAdapter.ViewHolder> {

    private List<RoadClosure> roadClosureList;
    private Context context;

    public RoadClosureAdapter(Context context) {
        roadClosureList = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<RoadClosure> events) {
        this.roadClosureList = events;
        this.notifyDataSetChanged();
    }

    public void clear() {
        roadClosureList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.road_closure_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RoadClosure roadClosure = roadClosureList.get(position);

        String date = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(roadClosure.getWindowStart());
        String status = "Status: " +  roadClosure.getStatus().getName();
        String window = DateFormat.getTimeInstance(DateFormat.SHORT).format(roadClosure.getWindowStart()) + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(roadClosure.getWindowEnd());

        holder.closureTitle.setText(roadClosure.getTitle());
        holder.closureStatus.setText(status);
        holder.closureDate.setText(date);
        holder.closureWindow.setText(window);
    }

    @Override
    public int getItemCount() {
        return roadClosureList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView closureTitle;
        private TextView closureStatus;
        private TextView closureDate;
        private TextView closureWindow;


        public ViewHolder(View view) {
            super(view);

            closureTitle = view.findViewById(R.id.title);
            closureStatus = view.findViewById(R.id.status);
            closureDate = view.findViewById(R.id.date);
            closureWindow = view.findViewById(R.id.window);

        }
    }
}
