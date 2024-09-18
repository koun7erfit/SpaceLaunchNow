package me.calebjones.spacelaunchnow.starship.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.ui.views.DialogAdapter;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.common.youtube.models.VideoListItem;
import me.calebjones.spacelaunchnow.data.models.main.Event;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;
import me.calebjones.spacelaunchnow.data.models.main.VidURL;
import me.calebjones.spacelaunchnow.data.models.main.dashboards.Starship;
import me.calebjones.spacelaunchnow.starship.StarshipDashboardViewModel;
import me.calebjones.spacelaunchnow.starship.data.StarshipDataRepository;
import me.calebjones.spacelaunchnow.starship.ui.upcoming.CombinedAdapter;
import me.spacelaunchnow.starship.R;
import me.spacelaunchnow.starship.databinding.FragmentStarshipDashboardBinding;
import me.spacelaunchnow.starship.databinding.StarshipDashboardBinding;

/**
 * A fragment representing the Starship Dashboard
 */
public class StarshipDashboardFragment extends BaseFragment {


    private StarshipDataRepository dataRepository;
    private boolean firstLaunch = true;
    private StarshipDashboardViewModel model;
    private YouTubePlayer youTubePlayer;
    private RoadClosureAdapter roadClosureAdapter;
    private NoticesAdapter noticesAdapter;
    private UpdateAdapter updateAdapter;
    private Dialog dialog;
    private String youTubeURL;
    private CombinedAdapter adapter;
    private FragmentStarshipDashboardBinding binding;
    private StarshipDashboardBinding dashboardBinding;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StarshipDashboardFragment() {
    }

    @SuppressWarnings("unused")
    public static StarshipDashboardFragment newInstance() {
        return new StarshipDashboardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new StarshipDataRepository(getContext(), getRealm());
        setScreenName("Starship Dashboard Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStarshipDashboardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        dashboardBinding = binding.starshipDashboard;

        setHasOptionsMenu(true);
        getLifecycle().addObserver(dashboardBinding.starshipDashboardLivestreamCard.youtubeView);
        model = ViewModelProviders.of(getParentFragment()).get(StarshipDashboardViewModel.class);
        model.getStarshipDashboard().observe(this, this::updateViews);


        roadClosureAdapter = new RoadClosureAdapter(getContext());
        dashboardBinding.starshipDashboardRoadclosureCard.roadclosureRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardBinding.starshipDashboardRoadclosureCard.roadclosureRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        dashboardBinding.starshipDashboardRoadclosureCard.roadclosureRecyclerview.setAdapter(roadClosureAdapter);

        noticesAdapter = new NoticesAdapter(getContext());
        dashboardBinding.starshipDashboardNoticesCard.noticesRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardBinding.starshipDashboardNoticesCard.noticesRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        dashboardBinding.starshipDashboardNoticesCard.noticesRecyclerview.setAdapter(noticesAdapter);

        adapter = new CombinedAdapter(getContext(), ThemeHelper.isDarkMode(getActivity()));
        dashboardBinding.starshipDashboardUpnext.upnextRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardBinding.starshipDashboardUpnext.upnextRecyclerview.setAdapter(adapter);

        updateAdapter = new UpdateAdapter(getContext());
        dashboardBinding.starshipDashboardUpnext.updateRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardBinding.starshipDashboardUpnext.updateRecyclerview.setAdapter(updateAdapter);


        if (savedInstanceState != null) {
            youTubeURL = savedInstanceState.getString("youTubeID");
        }

        return view;
    }


    private void loadVideo(YouTubePlayer youTubePlayer, String videoId) {
        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED)
            youTubePlayer.cueVideo(videoId, 0);
        else
            youTubePlayer.cueVideo(videoId, 0);
        youTubePlayer.play();
    }

    //Currently only used to debug
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateViews(Starship starship) {
        assert binding.starshipDashboard != null;

        if (!starship.getLiveStreams().isEmpty()) {
            VidURL liveStream = starship.liveStreams.get(0);
            assert liveStream != null;
            youTubeURL = Utils.getYouTubeID(liveStream.getUrl());
            binding.starshipDashboard.starshipDashboardLivestreamCard.title.setText(liveStream.getName());
            binding.starshipDashboard.starshipDashboardLivestreamCard.description.setText(liveStream.getDescription());
            binding.starshipDashboard.starshipDashboardLivestreamCard.youtubeView.initialize(youTubePlayer -> {
                youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        loadVideo(youTubePlayer, youTubeURL);
                        binding.starshipDashboard.starshipDashboardLivestreamCard.youtubeView.getPlayerUIController().enableLiveVideoUI(true);
                        binding.starshipDashboard.starshipDashboardLivestreamCard.youtubeView.getPlayerUIController().showFullscreenButton(false);
                    }
                });
            }, true);
        }
        ArrayList<Object> upcomingCombinedObjects = new ArrayList<>();
        if (!starship.getUpcomingObjects().getEvents().isEmpty()) {
            upcomingCombinedObjects.addAll(starship.getUpcomingObjects().getEvents());
        }

        if (!starship.getUpcomingObjects().getLaunches().isEmpty()) {
            upcomingCombinedObjects.addAll(starship.getUpcomingObjects().getLaunches());
        }

        if (!upcomingCombinedObjects.isEmpty()) {
            binding.starshipDashboard.starshipDashboardUpnext.upnextStatefulLayout.showContent();
            upcomingCombinedObjects = sortMultiClassList(upcomingCombinedObjects);
            Object object = upcomingCombinedObjects.get(0);
            upcomingCombinedObjects = new ArrayList<>();
            upcomingCombinedObjects.add(object);
            adapter.addItems(upcomingCombinedObjects);
        } else {
            binding.starshipDashboard.starshipDashboardUpnext.upnextStatefulLayout.showEmpty();
        }

        if (!starship.getRoadClosures().isEmpty()) {
            binding.starshipDashboard.starshipDashboardRoadclosureCard.roadclosureStatefulLayout.showContent();
            roadClosureAdapter.addItems(starship.getRoadClosures());
        } else {
            binding.starshipDashboard.starshipDashboardRoadclosureCard.roadclosureStatefulLayout.showEmpty();
        }

        if (!starship.getNotices().isEmpty()) {
            binding.starshipDashboard.starshipDashboardNoticesCard.noticesStatefulLayout.showContent();
            noticesAdapter.addItems(starship.getNotices());
        } else {
            binding.starshipDashboard.starshipDashboardNoticesCard.noticesStatefulLayout.showEmpty();
        }

        if (!starship.getUpdates().isEmpty()){
            binding.starshipDashboard.starshipDashboardUpnext.updateStatefulLayout.showContent();
            updateAdapter = new UpdateAdapter(getContext());
            binding.starshipDashboard.starshipDashboardUpnext.updateRecyclerview.setAdapter(updateAdapter);
            updateAdapter.addItems(starship.getUpdates());
        } else {
            binding.starshipDashboard.starshipDashboardUpnext.updateStatefulLayout.showEmpty();
        }

        binding.starshipDashboard.starshipDashboardLivestreamCard.liveStreamsButton.setOnClickListener(v -> {
            if (!starship.getLiveStreams().isEmpty()) {
                final DialogAdapter adapter = new DialogAdapter((index, item, longClick) -> {
                    if (longClick) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, item.getVideoURL().toString()); // Simple text and URL to share
                        sendIntent.setType("text/plain");
                        getContext().startActivity(sendIntent);
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
                            getContext().startActivity(i);
                        }
                    }
                });

                for (VidURL s : starship.getLiveStreams()) {
                    //Do your stuff here
                    adapter.add(new VideoListItem.Builder(getContext())
                            .content(s.getName())
                            .videoURL(s.getUrl())
                            .build());
                }

                MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                        .title("Select a Source")
                        .content("Long press an item to share.")
                        .adapter(adapter, null)
                        .negativeText("Cancel");
                dialog = builder.show();
            }
        });

    }

    public ArrayList<Object> sortMultiClassList(ArrayList<Object> yourList) {
        Collections.sort(yourList, (Comparator<Object>) (o1, o2) -> {
            if (o1 instanceof LaunchList && o2 instanceof LaunchList) {
                return ((LaunchList) o1).getNet().compareTo(((LaunchList) o2).getNet());
            } else if (o1 instanceof LaunchList && o2 instanceof Event) {
                return ((LaunchList) o1).getNet().compareTo(((Event) o2).getDate());
            } else if (o1 instanceof Event && o2 instanceof LaunchList) {
                return ((Event) o1).getDate().compareTo(((LaunchList) o2).getNet());
            } else if (o1 instanceof Event && o2 instanceof Event) {
                return ((Event) o1).getDate().compareTo(((Event) o2).getDate());
            } else {
                throw new IllegalArgumentException("Don't know how to compare");
            }
        });
        return yourList;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }



}
