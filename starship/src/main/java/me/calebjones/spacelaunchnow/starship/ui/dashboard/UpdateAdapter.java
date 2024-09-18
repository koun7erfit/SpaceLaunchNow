package me.calebjones.spacelaunchnow.starship.ui.dashboard;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.Update;
import me.spacelaunchnow.starship.R;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    private List<Update> updates;
    private Context context;

    public UpdateAdapter(Context context) {
        updates = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<Update> updates) {
        this.updates = updates;
        this.notifyDataSetChanged();
    }

    public void clear() {
        updates = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.update_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Update update = updates.get(position);

        String date = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(update.getCreatedOn());
        holder.title.setText(update.createdBy + " - " + date);
        holder.comment.setText(update.comment);
        holder.source.setText(update.infoUrl);

        if (update.getProfileImage() != null) {
            GlideApp.with(context)
                    .load(update.getProfileImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(holder.icon);
        }
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView source;
        private TextView comment;
        private TextView title;
        private ImageView icon;
        private View rootview;


        public ViewHolder(View view) {
            super(view);

            source = view.findViewById(R.id.update_source);
            comment = view.findViewById(R.id.update_comment);
            title = view.findViewById(R.id.update_title);
            icon = view.findViewById(R.id.update_icon);
            rootview = view.findViewById(R.id.rootview);

            rootview.setOnClickListener(v -> {
                Update update = updates.get(getAdapterPosition());
                if (update.getInfoUrl() != null) {
                    openCustomTab(context, update.getInfoUrl());
                }
            });

        }
    }
}
