package me.calebjones.spacelaunchnow.data.networking.responses.launchlibrary;

import com.google.gson.annotations.SerializedName;

import me.calebjones.spacelaunchnow.data.models.main.Agency;

public class AgencyResponse extends BaseResponse {
    @SerializedName("results")
    private Agency[] agencies;

    public Agency[] getAgencies() {
        return agencies;
    }
}
