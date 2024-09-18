package me.calebjones.spacelaunchnow.news.ui.news;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.news.NewsItem;
import me.calebjones.spacelaunchnow.news.R;
import me.calebjones.spacelaunchnow.news.databinding.ArticleItemBinding;


public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private List<NewsItem> newsList;
    private Context context;
    private ArticleItemBinding binding;

    public NewsRecyclerViewAdapter(Context context) {
        newsList = new ArrayList<>();
        this.context = context;
    }

    public void addItems(List<NewsItem> events) {
        this.newsList = events;
        this.notifyDataSetChanged();
    }

    public void clear() {
        newsList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        binding = ArticleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        holder.binding.articleTitle.setText(news.getTitle());
        holder.binding.articleSite.setText(news.getNewsSite());
        holder.binding.articlePublicationDate.setText(Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(news.getDatePublished()));
        if (news.getFeaturedImage() != null) {
            GlideApp.with(context)
                    .load(news.getFeaturedImage())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.articleImage);
        } else {
            String link = news.getNewsSite();
            if (link.contains("spaceflightnow")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.spaceflightnow_logo))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.articleImage);
            } else if (link.contains("spaceflight101")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.spaceflight_101))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.articleImage);
            } else if (link.contains("spacenews")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.spacenews_logo))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.articleImage);
            } else if (link.contains("nasaspaceflight")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.nasaspaceflight_logo))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.articleImage);
            } else if (link.contains("nasa.gov")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.NASA_logo))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.articleImage);
            } else if (link.contains("spacex.com")) {
                GlideApp.with(context)
                        .load(context.getResources().getString(R.string.spacex_logo))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into( holder.binding.articleImage);
            } else {
                GlideApp.with(context)
                        .load(R.drawable.placeholder)
                        .centerCrop()
                        .into(holder.binding.articleImage);
            }
        }

        holder.binding.rootview.setOnClickListener(v -> {
            openCustomTab(context, news.getUrl());
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ArticleItemBinding binding;

        public ViewHolder(ArticleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
