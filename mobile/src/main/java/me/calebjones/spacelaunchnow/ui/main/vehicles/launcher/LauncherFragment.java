package me.calebjones.spacelaunchnow.ui.main.vehicles.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.base.RetroFitFragment;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.networking.error.ErrorUtil;
import me.calebjones.spacelaunchnow.data.networking.interfaces.SpaceLaunchNowService;
import me.calebjones.spacelaunchnow.data.networking.responses.base.AgencyResponse;
import me.calebjones.spacelaunchnow.databinding.FragmentLaunchVehiclesBinding;
import me.calebjones.spacelaunchnow.ui.launcher.LauncherDetailActivity;
import me.calebjones.spacelaunchnow.common.utils.analytics.Analytics;
import me.calebjones.spacelaunchnow.utils.OnItemClickListener;
import me.calebjones.spacelaunchnow.common.ui.views.SnackbarHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LauncherFragment extends RetroFitFragment implements SwipeRefreshLayout.OnRefreshListener {

    private VehicleAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Agency> items = new ArrayList<>();
    private Context context;
    private View view;
    private FragmentLaunchVehiclesBinding binding;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        adapter = new VehicleAdapter(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentLaunchVehiclesBinding.inflate(inflater, container, false);

        binding.swiperefresh.setOnRefreshListener(this);

        if (getResources().getBoolean(R.bool.landscape) && getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(context, 3);
        } else if (getResources().getBoolean(R.bool.landscape)  || getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(context, 2);
        } else {
            layoutManager = new LinearLayoutManager(context);
        }

        binding.vehicleDetailList.setLayoutManager(layoutManager);
        binding.vehicleDetailList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                binding.swiperefresh.setEnabled(topRowVerticalPosition >= 0);

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        adapter.setOnItemClickListener(recyclerRowClickListener);
        binding.vehicleDetailList.setAdapter(adapter);
        Timber.v("Returning view.");
        binding.statefulView.setOfflineRetryOnClickListener(v -> loadJSON(false));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.v("onResume");
        if (adapter.getItemCount() == 0) {
            binding.statefulView.showProgress();
            new Handler().postDelayed(() -> loadJSON(false), 100);
        }
    }

    private void loadJSON(boolean forceRefresh) {
        Timber.v("Loading vehicles...");
        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            showLoading();
        }

        SpaceLaunchNowService request = getSpaceLaunchNowRetrofit().create(SpaceLaunchNowService.class);
        Call<AgencyResponse> call = request.getAgencies(true, "detailed", 50);
        call.enqueue(new Callback<AgencyResponse>() {
            @Override
            public void onResponse(Call<AgencyResponse> call, Response<AgencyResponse> response) {
                Timber.v("onResponse");
                if (response.raw().cacheResponse() != null) {
                    Timber.v("Response pulled from cache.");
                }

                if (response.raw().networkResponse() != null) {
                    Timber.v("Response pulled from network.");
                }

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    if (response.isSuccessful()) {
                        AgencyResponse jsonResponse = response.body();
                        Timber.v("Success %s", response.message());
                        items = jsonResponse.getAgencies();
                        adapter.addItems(items);
                        binding.statefulView.showContent();
                        Analytics.getInstance().sendNetworkEvent("LAUNCHER_INFORMATION", call.request().url().toString(), true);

                    } else {
                        binding.statefulView.showEmpty();
                        Timber.e(ErrorUtil.parseSpaceLaunchNowError(response).getMessage());
                        SnackbarHandler.showErrorSnackbar(context, binding.vehicleCoordinator,
                                ErrorUtil.parseSpaceLaunchNowError(response).getMessage());
                    }
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<AgencyResponse> call, Throwable t) {
                Timber.e(t);
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    hideLoading();
                    binding.statefulView.showOffline();
                    SnackbarHandler.showErrorSnackbar(context, binding.vehicleCoordinator, t.getLocalizedMessage());
                }
                Analytics.getInstance().sendNetworkEvent("VEHICLE_INFORMATION", call.request().url().toString(), false, t.getLocalizedMessage());
            }
        });
    }

    private void hideLoading() {
        binding.swiperefresh.setRefreshing(false);
    }

    private void showLoading() {
        binding.swiperefresh.setRefreshing(true);
    }

    private OnItemClickListener recyclerRowClickListener = (v, position) -> {
        Analytics.getInstance().sendButtonClicked("Launcher clicked", items.get(position).getName());
        Gson gson = new Gson();
        String jsonItem = gson.toJson(items.get(position));

        Intent intent = new Intent(getActivity(), LauncherDetailActivity.class);
        intent.putExtra("name", items.get(position).getName());
        intent.putExtra("json", jsonItem);
        startActivity(intent);
    };

    @Override
    public void onRefresh() {
        Analytics.getInstance().sendButtonClicked("Launcher Refresh");
        loadJSON(true);
    }
}
