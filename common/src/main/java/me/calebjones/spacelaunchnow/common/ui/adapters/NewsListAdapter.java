package me.calebjones.spacelaunchnow.common.ui.adapters;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.realm.RealmList;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.databinding.NewsListItemBinding;
import me.calebjones.spacelaunchnow.data.models.main.news.NewsItem;
import timber.log.Timber;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> {
    public int position;
    private RealmList<NewsItem> newsList;
    private Context mContext;
    private NewsListItemBinding binding;

    public NewsListAdapter(Context context) {
        newsList = new RealmList<>();
        mContext = context;

    }

    public void addItems(List<NewsItem> newList) {

        if (this.newsList != null) {
            this.newsList.addAll(newList);
        } else {
            this.newsList = new RealmList<>();
            this.newsList.addAll(newList);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        newsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Timber.v("onCreate ViewHolder.");
        binding = NewsListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        final NewsItem item = newsList.get(i);
        if (item != null) {
            GlideApp.with(mContext)
                    .load(item.getFeaturedImage())
                    .placeholder(R.drawable.placeholder)
                    .thumbnail(0.5f)
                    .circleCrop()
                    .into(holder.binding.newsIcon);
        }
        holder.binding.newsTitle.setText(item.getTitle());
        holder.binding.newsSubtitle.setText(item.getNewsSite());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private NewsListItemBinding binding;


        //Add content to the card
        public ViewHolder(NewsListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        //React to click events.
        @Override
        public void onClick(View v) {
            final NewsItem item = newsList.get(getAdapterPosition());
            if (item != null) {
                openCustomTab(mContext, item.getUrl());
            }
        }
    }

}

