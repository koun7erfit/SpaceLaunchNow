package me.calebjones.spacelaunchnow.common.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.List;

import io.realm.RealmList;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.content.LaunchStatusUtil;
import me.calebjones.spacelaunchnow.common.databinding.LaunchListItemBinding;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.activity.LaunchDetailActivity;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;


/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int position;
    private RealmList<LaunchList> launchList;
    private Context mContext;
    private final ListPreloader.PreloadSizeProvider sizeProvider = new ViewPreloadSizeProvider();
    private int color;
    private LaunchListItemBinding binding;

    public ListAdapter(Context context, boolean night) {
        launchList = new RealmList<>();
        mContext = context;

        if (night){
            color = ContextCompat.getColor(mContext, R.color.material_color_white);
        } else {
            color = ContextCompat.getColor(mContext, R.color.material_color_black);
        }
    }

    public void addItems(List<LaunchList> launchList) {

        if (this.launchList == null) {
            this.launchList = new RealmList<>();
        }
        this.launchList.addAll(launchList);
        this.notifyDataSetChanged();
    }

    public void clear() {
        launchList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        binding = LaunchListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rHolder, int i) {
        final LaunchList launchItem = launchList.get(i);

        String[] title;
        String launchDate;

        position = i;

        ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) rHolder;

        //Retrieve missionType
        if (launchItem.getImage() != null) {
            GlideApp.with(mContext)
                    .load(launchItem.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(holder.binding.categoryIcon);
        } else {
            if (launchItem.getMission() != null) {

                GlideApp.with(mContext)
                        .load(Utils.getCategoryIcon(launchItem.getMissionType()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .transform(new ColorFilterTransformation(color))
                        .into(holder.binding.categoryIcon);

            } else {
                GlideApp.with(mContext)
                        .load(R.drawable.ic_unknown)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .transform(new ColorFilterTransformation(color))
                        .into(holder.binding.categoryIcon);
            }
        }
//        holder.launch_date.setText(Utils.getStatusBasedDateFormat(launchItem.getNet(), launchItem.getStatus()));
        holder.binding.launchDate.setText(Utils.getSimpleDateFormatForUIWithPrecision(launchItem.getNetPrecision()).format(launchItem.getNet()));

        //If pad and agency exist add it to location, otherwise get whats always available
        if (launchItem.getLocation() != null) {
            holder.binding.location.setText(launchItem.getLocation());
        } else {
            holder.binding.location.setText(mContext.getString(R.string.click_for_info));
        }

        if (launchItem.getName() != null) {
            title = launchItem.getName().split("\\|");
            try {
                if (title.length > 0) {
                    holder.binding.launchRocket.setText(title[1].trim());
                    holder.binding.mission.setText(title[0].trim());
                } else {
                    holder.binding.launchRocket.setText(launchItem.getName());
                    if (launchItem.getMission() != null) {
                        holder.binding.launchRocket.setText(launchItem.getMission());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException exception) {
                holder.binding.launchRocket.setText(launchItem.getName());
                if (launchItem.getMission() != null) {
                    holder.binding.launchRocket.setText(launchItem.getMission());
                }

            }
        }

        if (launchItem.getLanding() != null) {
            holder.binding.landingPillMini.getRoot().setVisibility(View.VISIBLE);
            holder.binding.landingPillMini.landing.setText(launchItem.getLanding());
            holder.binding.landingPillMini.getRoot().setCardBackgroundColor(LaunchStatusUtil.getLandingStatusColor(mContext, launchItem.getLandingSuccess()));
        } else {
            holder.binding.landingPillMini.getRoot().setVisibility(View.GONE);
        }

        if (launchItem.getOrbit() != null) {
            holder.binding.launcherPillMini.getRoot().setVisibility(View.VISIBLE);
            holder.binding.launcherPillMini.launcherName.setText(launchItem.getOrbit());
        } else {
            holder.binding.launcherPillMini.getRoot().setVisibility(View.GONE);
        }

        holder.binding.statusPillMini.status.setText(launchItem.getStatus().getAbbrev());
        holder.binding.statusPillMini.status.setOnClickListener(v -> {
            Toast.makeText(mContext, launchItem.getStatus().getDescription(), Toast.LENGTH_LONG).show();
        });
        holder.binding.statusPillMini.getRoot().setCardBackgroundColor(LaunchStatusUtil.getLaunchStatusColor(mContext, launchItem.getStatus().getId()));
        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(mContext, LaunchDetailActivity.class);
            intent.putExtra("TYPE", "launch");
            intent.putExtra("launchID", launchItem.getId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return launchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LaunchListItemBinding binding;

        //Add content to the card
        public ViewHolder(LaunchListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

