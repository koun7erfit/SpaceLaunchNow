package me.calebjones.spacelaunchnow.common.ui.launchdetail.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import io.reactivex.disposables.Disposable;
import io.realm.RealmResults;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.databinding.DetailLaunchSummaryBinding;
import me.calebjones.spacelaunchnow.common.prefs.ListPreferences;
import me.calebjones.spacelaunchnow.common.ui.adapters.NewsListAdapter;
import me.calebjones.spacelaunchnow.common.ui.adapters.UpdateAdapter;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.DetailsViewModel;
import me.calebjones.spacelaunchnow.common.ui.views.CountDownTimer;
import me.calebjones.spacelaunchnow.common.ui.views.DialogAdapter;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.common.youtube.YouTubeAPIHelper;
import me.calebjones.spacelaunchnow.common.youtube.models.Video;
import me.calebjones.spacelaunchnow.common.youtube.models.VideoListItem;
import me.calebjones.spacelaunchnow.common.youtube.models.VideoResponse;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.VidURL;
import me.calebjones.spacelaunchnow.data.models.main.news.NewsItem;

import me.calebjones.spacelaunchnow.data.networking.news.data.Callbacks;
import me.calebjones.spacelaunchnow.data.networking.news.data.NewsDataRepository;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;


public class SummaryDetailFragment extends BaseFragment implements YouTubePlayerListener {


    private SharedPreferences sharedPref;
    private ListPreferences sharedPreference;
    private Context context;
    private CountDownTimer timer;
    public Launch detailLaunch;
    private boolean nightMode;
    private String youTubeURL;
    private Dialog dialog;
    private boolean youTubePlaying = false;
    private int youTubeProgress = 0;
    public Disposable var;
    private boolean future = true;
    private DetailsViewModel model;
    private YouTubePlayer youTubePlayer;
    private NewsDataRepository dataRepository;
    private NewsListAdapter listAdapter;
    boolean isYouTubePlaying = false;
    private UpdateAdapter updateAdapter;
    private DetailLaunchSummaryBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Summary Detail Fragment");
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DetailLaunchSummaryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.context = getContext();

        sharedPreference = ListPreferences.getInstance(this.context);
        if (sharedPreference.isNightModeActive(context)) {
            nightMode = true;
        } else {
            nightMode = false;
        }

        dataRepository = new NewsDataRepository(getContext(), getRealm());

        if (savedInstanceState != null) {
            youTubePlaying = savedInstanceState.getBoolean("youTubePlaying", false);
            youTubeProgress = savedInstanceState.getInt("youTubeProgress", 0);
            youTubeURL = savedInstanceState.getString("youTubeID");
        }

        return view;
    }


    @Override
    public void onResume() {

        if (sharedPreference.isNightModeActive(context)) {
            nightMode = true;
        } else {
            nightMode = false;
        }
        super.onResume();
    }

    public void setLaunch(Launch launch) {
        Timber.v("Launch update received: %s", launch.getName());
        detailLaunch = launch;
        setUpViews(launch);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
//        if (youTubePlayer != null) {
//            try {
//                outState.putBoolean("youTubePlaying", youTubePlayer.);
//                outState.putInt("youTubeProgress", );
//                outState.putString("youTubeID", youTubeURL);
//            } catch (IllegalStateException e) {
//                Timber.e(e);
//            }
//        }
        super.onSaveInstanceState(outState);
    }

    private void setUpViews(Launch launch) {
        try {
            getLifecycle().addObserver(binding.youtubeView);
            binding.videosEmpty.setVisibility(View.GONE);
            detailLaunch = launch;

            fetchRelatedNews(launch.getId());

            setupCountdownTimer(launch);

            Date mDate;
            String dateText;

            if (detailLaunch.getVidURLs() != null && !detailLaunch.getVidURLs().isEmpty()) {

                for (VidURL url : detailLaunch.getVidURLs()) {
                    youTubeURL = Utils.getYouTubeID(url.getUrl());
                    if (youTubeURL != null) break;
                }

                if (youTubeURL != null) {
                    Timber.v("Loading %s", youTubeURL);
                    binding.youtubeView.setVisibility(View.VISIBLE);
                    binding.errorMessage.setVisibility(View.GONE);
//                    youTubePlayerView.getPlayerUIController().enableLiveVideoUI(true);
                    binding.youtubeView.getPlayerUIController().showFullscreenButton(false);
                    binding.youtubeView.initialize(youTubePlayer -> {
                        youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                            @Override
                            public void onReady() {
                                loadVideo(youTubePlayer, youTubeURL);
                            }
                        });
                    }, true);
                }

                binding.watchButton.setVisibility(View.VISIBLE);
                binding.watchButton.setOnClickListener(v -> {
                    Timber.d("Watch: %s", detailLaunch.getVidURLs().size());
                    if (!detailLaunch.getVidURLs().isEmpty()) {
                        final DialogAdapter adapter = new DialogAdapter((index, item, longClick) -> {
                            if (longClick) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, item.getVideoURL().toString()); // Simple text and URL to share
                                sendIntent.setType("text/plain");
                                context.startActivity(sendIntent);
                            } else {
                                String url = item.getVideoURL().toString();
                                String youTubeID = Utils.getYouTubeID(url);
                                if (youTubePlayer != null && youTubeID != null) {
                                    youTubeURL = youTubeID;
                                    if (dialog != null && dialog.isShowing())
                                        dialog.dismiss();
                                    youTubePlayer.cueVideo(youTubeURL, 0);
                                    youTubePlayer.play();
                                } else {
                                    Uri watchUri = Uri.parse(url);
                                    Intent i = new Intent(Intent.ACTION_VIEW, watchUri);
                                    context.startActivity(i);
                                }
                            }
                        });

                        for (VidURL s : detailLaunch.getVidURLs()) {
                            //Do your stuff here
                            adapter.add(new VideoListItem.Builder(context)
                                    .content(s.getName())
                                    .videoURL(s.getUrl())
                                    .build());
                        }

                        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                                .title("Select a Source")
                                .content("Long press an item to share.")
                                .adapter(adapter, null)
                                .negativeText("Cancel");
                        dialog = builder.show();
                    }
                });

            } else {
                if (future) {
                    binding.videosEmpty.setVisibility(View.VISIBLE);
                }
                binding.watchButton.setVisibility(View.GONE);
                binding.errorMessage.setText(getString(R.string.video_source_unavailable));
                binding.errorMessage.setVisibility(View.VISIBLE);
            }

            if (detailLaunch.getUpdates() != null && detailLaunch.getUpdates().size() > 0){
                updateAdapter = new UpdateAdapter(context);
                binding.updateRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.updateRecyclerView.setAdapter(updateAdapter);
                updateAdapter.addItems(detailLaunch.getUpdates());
                binding.updateCard.setVisibility(View.VISIBLE);
            } else {
                binding.updateCard.setVisibility(View.GONE);
            }

            Date date = detailLaunch.getNet();

            //Get launch date
//            dateText = Utils.getStatusBasedDateFormat(launch.getNet(), launch.getStatus());
            dateText = Utils.getSimpleDateFormatForUIWithPrecision(launch.getNetPrecision()).format(launch.getNet());
            binding.date.setText(Html.fromHtml(String.format(getString(R.string.launch_date), dateText)));


            if (detailLaunch.getWindowStart() != null && launch.getStatus().getId() != 2) {
                setWindowStamp();
            } else {
                binding.launchWindowText.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    private void fetchRelatedNews(String id) {
        dataRepository.getNewsByLaunch(id, new Callbacks.NewsListCallback() {
            @Override
            public void onNewsLoaded(RealmResults<NewsItem> news) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
                    Timber.v(news.toString());
                    if (!news.isEmpty()) {
                        listAdapter = new NewsListAdapter(context);
                        binding.newsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                        binding.newsRecyclerView.setAdapter(listAdapter);
                        listAdapter.addItems(news);
                        binding.relatedCard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {

                }
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {

                    if (throwable != null) {
                        Timber.e(throwable);
                    } else {
                        Timber.e(message);
                    }
                }
            }
        });
    }


    private void loadVideo(YouTubePlayer youTubePlayer, String videoId) {
        youTubePlayer.addListener(this);
        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED)
            youTubePlayer.cueVideo(videoId, 0);
        else
            youTubePlayer.cueVideo(videoId, 0);
    }

    private void setupCountdownTimer(Launch launch) {
        //If timestamp is available calculate TMinus and launchDate.
        if (launch.getNet() != null) {
            binding.countdownLayout.setLaunch(launch);
        }
    }


    private void setWindowStamp() {
        // Create a DateFormatter object for displaying launchDate in specified format.

        Date windowStart = detailLaunch.getWindowStart();
        Date windowEnd = detailLaunch.getWindowEnd();
        SimpleDateFormat dateFormat;

        if (sharedPref.getString("time_format", "Default").contains("12-Hour")) {
            dateFormat = Utils.getSimpleDateFormatForUI("h:mm a zzz");
        } else if (sharedPref.getString("time_format", "Default").contains("24-Hour")) {
            dateFormat = Utils.getSimpleDateFormatForUI("HH:mm zzz");
        } else if (android.text.format.DateFormat.is24HourFormat(context)) {
            dateFormat = Utils.getSimpleDateFormatForUI("HH:mm zzz");
        } else {
            dateFormat = Utils.getSimpleDateFormatForUI("h:mm a zzz");
        }

        dateFormat.toLocalizedPattern();

        if (sharedPref.getBoolean("local_time", true)){
            dateFormat.setTimeZone(TimeZone.getDefault());
        } else {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }


        if (windowStart.equals(windowEnd)) {
            // Window Start and Window End match - meaning instantaneous.

            TimeZone timeZone = dateFormat.getTimeZone();

            binding.launchWindowText.setText(Html.fromHtml(String.format(getString(R.string.instantaneous_launch_window),
                    dateFormat.format(windowStart))));
        } else if (windowStart.after(windowEnd)) {
            // Launch data is not trustworthy - start is after end.

            TimeZone timeZone = dateFormat.getTimeZone();

            binding.launchWindowText.setText(dateFormat.format(windowStart));
        } else if (windowStart.before(windowEnd)) {
            // Launch Window is properly configured

            String difference = Utils.printDifference(windowStart, windowEnd);
            binding.launchWindowText.setText(Html.fromHtml(String.format(getString(R.string.launch_window_extras),
                    dateFormat.format(windowStart),
                    dateFormat.format(windowEnd),
                    difference)));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        // update UI
        model.getLaunch().observe(this, this::setLaunch);
    }

    public static SummaryDetailFragment newInstance() {
        return new SummaryDetailFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
        }
    }
    @Override
    public void onReady() {

    }

    @Override
    public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
        if (state == PlayerConstants.PlayerState.PLAYING) {
            isYouTubePlaying = true;
        } else {
            isYouTubePlaying = false;
        }
    }

    @Override
    public void onPlaybackQualityChange(@NonNull PlayerConstants.PlaybackQuality playbackQuality) {

    }

    @Override
    public void onPlaybackRateChange(@NonNull PlayerConstants.PlaybackRate playbackRate) {

    }

    @Override
    public void onError(@NonNull PlayerConstants.PlayerError error) {

    }

    @Override
    public void onApiChange() {

    }

    @Override
    public void onCurrentSecond(float second) {

    }

    @Override
    public void onVideoDuration(float duration) {

    }

    @Override
    public void onVideoLoadedFraction(float loadedFraction) {

    }

    @Override
    public void onVideoId(@NonNull String videoId) {
        YouTubeAPIHelper youTubeAPIHelper = new YouTubeAPIHelper(context,
                context.getResources().getString(R.string.GoogleMapsKey));
        binding.youtubeView.getPlayerUIController().enableLiveVideoUI(false);
        youTubeAPIHelper.getVideoById(videoId, new retrofit2.Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getVideos();
                    if (videos.size() > 0) {
                        try {
                            if (videos.get(0).getSnippet().getLiveBroadcastContent().contains("live")){
                                binding.youtubeView.getPlayerUIController().enableLiveVideoUI(true);
                            }
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
            }
        });
    }
}