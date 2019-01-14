package me.spacelaunchnow.astronauts.data;


import android.content.Context;
import android.net.Uri;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.astronaut.Astronaut;
import me.calebjones.spacelaunchnow.data.networking.DataClient;
import me.calebjones.spacelaunchnow.data.networking.error.ErrorUtil;
import me.calebjones.spacelaunchnow.data.networking.error.SpaceLaunchNowError;
import me.calebjones.spacelaunchnow.data.networking.responses.base.AstronautResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AstronautDataLoader {

    private Context context;

    public AstronautDataLoader(Context context) {
        this.context = context;
    }

    public void getAstronautList(int limit, int offset, String search, List<Integer> statusIDs,
                                 final Callbacks.AstronautListNetworkCallback networkCallback) {
        Timber.i("Running getUpcomingLaunchesList");
        String stringStatusIDs = null;
        if (statusIDs != null) {
            stringStatusIDs = "";
            for (int i = 0; i < statusIDs.size(); i++) {
                stringStatusIDs += String.valueOf(statusIDs.get(i));
                if (i != statusIDs.size() - 1) {
                    stringStatusIDs += ",";
                }
            }
        }

        DataClient.getInstance().getAstronauts(limit, offset, search, null, stringStatusIDs, new Callback<AstronautResponse>() {
            @Override
            public void onResponse(Call<AstronautResponse> call, Response<AstronautResponse> response) {
                if (response.isSuccessful()) {
                    AstronautResponse astronautResponse = response.body();

                    Timber.v("Astronauts returned Count: %s", astronautResponse.getCount());

                    if (astronautResponse.getNext() != null) {
                        Uri uri = Uri.parse(astronautResponse.getNext());
                        String limit = uri.getQueryParameter("limit");
                        String nextOffset = uri.getQueryParameter("offset");
                        String total = uri.getQueryParameter("offset");
                        int next = Integer.valueOf(nextOffset);
                        networkCallback.onSuccess(astronautResponse.getAstronauts(), next, astronautResponse.getCount(), true);
                    } else {
                        networkCallback.onSuccess(astronautResponse.getAstronauts(), 0, astronautResponse.getCount(), false);
                    }
                } else {
                    SpaceLaunchNowError error = ErrorUtil.parseSpaceLaunchNowError(response);
                    Timber.e(error.getMessage());
                    networkCallback.onNetworkFailure(response.code());

                }
            }

            @Override
            public void onFailure(Call<AstronautResponse> call, Throwable t) {
                networkCallback.onFailure(t);
            }
        });
    }

    public void getAstronaut(int id, final Callbacks.AstronautNetworkCallback networkCallback) {
        Timber.i("Running getUpcomingLaunchesList");
        DataClient.getInstance().getAstronautsById(id, new Callback<Astronaut>() {
            @Override
            public void onResponse(Call<Astronaut> call, Response<Astronaut> response) {
                if (response.isSuccessful()) {
                    Astronaut astronaut = response.body();
                    networkCallback.onSuccess(astronaut);

                } else {
                    networkCallback.onNetworkFailure(response.code());

                }
            }

            @Override
            public void onFailure(Call<Astronaut> call, Throwable t) {
                networkCallback.onFailure(t);
            }
        });
    }

}
