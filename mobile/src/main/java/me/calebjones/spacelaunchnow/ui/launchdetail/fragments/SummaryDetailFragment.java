package me.calebjones.spacelaunchnow.ui.launchdetail.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.zetterstrom.com.forecast.ForecastClient;
import android.zetterstrom.com.forecast.models.DataPoint;
import android.zetterstrom.com.forecast.models.Forecast;
import android.zetterstrom.com.forecast.models.Unit;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.bumptech.glide.Glide;
import com.github.pwittchen.weathericonview.WeatherIconView;
import com.mypopsy.maps.StaticMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.calendar.CalendarSyncManager;
import me.calebjones.spacelaunchnow.calendar.model.CalendarItem;
import me.calebjones.spacelaunchnow.common.BaseFragment;
import me.calebjones.spacelaunchnow.content.database.ListPreferences;
import me.calebjones.spacelaunchnow.content.database.SwitchPreferences;
import me.calebjones.spacelaunchnow.content.events.LaunchEvent;
import me.calebjones.spacelaunchnow.content.services.LibraryDataManager;
import me.calebjones.spacelaunchnow.content.util.DialogAdapter;
import me.calebjones.spacelaunchnow.data.models.Launch;
import me.calebjones.spacelaunchnow.data.models.Pad;
import me.calebjones.spacelaunchnow.data.models.RocketDetails;
import me.calebjones.spacelaunchnow.data.models.realm.RealmStr;
import me.calebjones.spacelaunchnow.utils.Utils;
import me.calebjones.spacelaunchnow.utils.analytics.Analytics;
import me.calebjones.spacelaunchnow.utils.views.CountDownTimer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class SummaryDetailFragment extends BaseFragment {

    private SharedPreferences sharedPref;
    private static ListPreferences sharedPreference;
    private Context context;
    private CountDownTimer timer;
    public Launch detailLaunch;
    private RocketDetails launchVehicle;

    private boolean nightMode;

    @BindView(R.id.content_TMinus_status)
    TextView contentTMinusStatus;
    @BindView(R.id.countdown_days)
    TextView countdownDays;
    @BindView(R.id.countdown_hours)
    TextView countdownHours;
    @BindView(R.id.countdown_minutes)
    TextView countdownMinutes;
    @BindView(R.id.countdown_seconds)
    TextView countdownSeconds;
    @BindView(R.id.countdown_layout)
    LinearLayout countdownLayout;
    @BindView(R.id.day_two)
    LinearLayout dayTwo;
    @BindView(R.id.day_three)
    LinearLayout dayThree;
    @BindView(R.id.day_four)
    LinearLayout dayFour;
    @BindView(R.id.launch_summary)
    NestedScrollView launchSummary;
    @BindView(R.id.map_view_summary)
    ImageView staticMap;
    @BindView(R.id.launch_date_title)
    TextView launch_date_title;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.launch_window_start)
    TextView launch_window_start;
    @BindView(R.id.launch_window_end)
    TextView launch_window_end;
    @BindView(R.id.launch_status)
    TextView launch_status;
    @BindView(R.id.watchButton)
    AppCompatButton watchButton;
    @BindView(R.id.notification_switch)
    SwitchCompat notificationSwitch;
    @BindView(R.id.weather_card)
    CardView weatherCard;
    @BindView(R.id.weather_icon)
    WeatherIconView weatherIconView;
    @BindView(R.id.weather_current_temp)
    TextView weatherCurrentTemp;
    @BindView(R.id.weather_feels_like)
    TextView weatherFeelsLike;
    @BindView(R.id.weather_low_high)
    TextView weatherLowHigh;
    @BindView(R.id.weather_location)
    TextView weatherLocation;
    @BindView(R.id.weather_percip_chance)
    TextView weatherPrecip;
    @BindView(R.id.weather_wind_speed)
    TextView weatherWindSpeed;
    @BindView(R.id.weather_summary_day)
    TextView weatherSummaryDay;
    @BindView(R.id.day_two_weather_icon)
    WeatherIconView dayTwoWeatherIconView;
    @BindView(R.id.day_two_low_high)
    TextView dayTwoWeatherLowHigh;
    @BindView(R.id.day_two_precip_prob)
    TextView dayTwoWeatherPrecip;
    @BindView(R.id.day_two_weather_wind_speed)
    TextView dayTwoWeatherWindSpeed;
    @BindView(R.id.day_two_day)
    TextView dayTwoDay;
    @BindView(R.id.day_three_weather_icon)
    WeatherIconView dayThreeWeatherIconView;
    @BindView(R.id.day_three_low_high)
    TextView dayThreeWeatherLowHigh;
    @BindView(R.id.day_three_precip_prob)
    TextView dayThreeWeatherPrecip;
    @BindView(R.id.day_three_weather_wind_speed)
    TextView dayThreeWeatherWindSpeed;
    @BindView(R.id.day_three_day)
    TextView dayThreeDay;
    @BindView(R.id.day_four_weather_icon)
    WeatherIconView dayFourWeatherIconView;
    @BindView(R.id.day_four_low_high)
    TextView dayFourWeatherLowHigh;
    @BindView(R.id.day_four_precip_prob)
    TextView dayFourWeatherPrecip;
    @BindView(R.id.day_four_weather_wind_speed)
    TextView dayFourWeatherWindSpeed;
    @BindView(R.id.day_four_day)
    TextView dayFourDay;
    @BindView(R.id.three_day_forecast)
    LinearLayout threeDayForecast;
    @BindView(R.id.day_two_weather_wind_speed_icon)
    WeatherIconView dayTwoWeatherWindIcon;
    @BindView(R.id.day_three_weather_wind_speed_icon)
    WeatherIconView dayThreeWeatherWindIcon;
    @BindView(R.id.day_four_weather_wind_speed_icon)
    WeatherIconView dayFourWeatherWindIcon;
    @BindView(R.id.day_two_precip_prob_icon)
    WeatherIconView dayTwoWeatherPrecipIcon;
    @BindView(R.id.day_three_precip_prob_icon)
    WeatherIconView dayThreeWeatherPrecipIcon;
    @BindView(R.id.day_four_precip_prob_icon)
    WeatherIconView dayFourWeatherPrecipIcon;
    @BindView(R.id.weather_percip_chance_icon)
    WeatherIconView weatherPrecipIcon;
    @BindView(R.id.weather_wind_speed_icon)
    WeatherIconView weatherSpeedIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenName("Summary Detail Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.context = getContext();

        sharedPreference = ListPreferences.getInstance(this.context);

        if (sharedPreference.isNightModeActive(context)) {
            nightMode = true;
        } else {
            nightMode = false;
        }

        View view = inflater.inflate(R.layout.detail_launch_summary, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {

        if (sharedPreference.isNightModeActive(context)) {
            nightMode = true;
        } else {
            nightMode = false;
        }
        if (detailLaunch != null) {
            setUpViews(detailLaunch);
        }
        super.onResume();
    }

    public void setLaunch(Launch launch) {
        detailLaunch = launch;
        setUpViews(launch);
    }

    private void fetchPastWeather() {
        if (detailLaunch.getLocation().getPads().size() > 0) {

            Pad pad = detailLaunch.getLocation().getPads().get(0);

            double latitude = pad.getLatitude();
            double longitude = pad.getLongitude();

            Unit unit;

            if (sharedPref.getBoolean("weather_US_SI", true)) {
                unit = Unit.US;
            } else {
                unit = Unit.SI;
            }

            ForecastClient.getInstance()
                    .getForecast(latitude, longitude, detailLaunch.getNetstamp(), null, unit, null, false, new Callback<Forecast>() {
                        @Override
                        public void onResponse(Call<Forecast> forecastCall, Response<Forecast> response) {
                            if (response.isSuccessful()) {
                                Forecast forecast = response.body();
                                if (SummaryDetailFragment.this.isVisible()) {
                                    Analytics.from(getActivity()).sendWeatherEvent(detailLaunch.getName(), true, "Success");
                                    updateWeatherView(forecast);
                                }
                            } else {
                                Analytics.from(getActivity()).sendWeatherEvent(detailLaunch.getName(), false, response.errorBody().toString());
                                Timber.e("Error: %s", response.errorBody());
                            }
                        }

                        @Override
                        public void onFailure(Call<Forecast> forecastCall, Throwable t) {
                            Analytics.from(getActivity()).sendWeatherEvent(detailLaunch.getName(), false, t.getLocalizedMessage());
                            Timber.e("ERROR: %s", t.getLocalizedMessage());
                        }
                    });
        }
    }

    private void fetchCurrentWeather() {
        // Sample WeatherLib client init
        if (detailLaunch.getLocation().getPads().size() > 0) {

            Pad pad = detailLaunch.getLocation().getPads().get(0);

            double latitude = pad.getLatitude();
            double longitude = pad.getLongitude();

            Unit unit;

            if (sharedPref.getBoolean("weather_US_SI", true)) {
                unit = Unit.US;
            } else {
                unit = Unit.SI;
            }

            ForecastClient.getInstance()
                    .getForecast(latitude, longitude, null, null, unit, null, false, new Callback<Forecast>() {
                        @Override
                        public void onResponse(Call<Forecast> forecastCall, Response<Forecast> response) {
                            if (response.isSuccessful()) {
                                Forecast forecast = response.body();
                                if (SummaryDetailFragment.this.isVisible()) {
                                    updateWeatherView(forecast);
                                }
                            } else {
                                Timber.e("Error: %s", response.errorBody());
                            }
                        }

                        @Override
                        public void onFailure(Call<Forecast> forecastCall, Throwable t) {
                            Timber.e("ERROR: %s", t.getLocalizedMessage());
                        }
                    });
        }
    }

    private void updateWeatherView(Forecast forecast) {
        final String temp;
        final String speed;
        String precip;
        String pressure;
        String visibility;

        if (sharedPref.getBoolean("weather_US_SI", true)) {
            temp = "F";
            speed = "Mph";
            precip = "in.";
            pressure = "mb";
            visibility = "mile";
        } else {
            temp = "C";
            speed = "m/s";
            precip = "cm";
            pressure = "hPa";
            visibility = "km";
        }
        if (forecast.getCurrently() != null) {
            if (forecast.getCurrently().getTemperature() != null) {
                String currentTemp = String.valueOf(Math.round(forecast.getCurrently().getTemperature())) + (char) 0x00B0 + " " + temp;
                weatherCurrentTemp.setText(currentTemp);
            }
            if (forecast.getCurrently().getApparentTemperature() != null) {
                String feelsLikeTemp;
                if (detailLaunch.getNet().after(Calendar.getInstance().getTime())) {
                    feelsLikeTemp = "Feels like ";
                } else {
                    feelsLikeTemp = "Felt like ";
                }
                feelsLikeTemp = feelsLikeTemp + String.valueOf(Math.round(forecast.getCurrently().getApparentTemperature())) + (char) 0x00B0;
                weatherFeelsLike.setText(feelsLikeTemp);
            }

            if (forecast.getCurrently().getWindSpeed() != null) {
                String windSpeed = String.valueOf(Math.round(forecast.getCurrently().getWindSpeed())) + " " + speed;
                weatherWindSpeed.setText(windSpeed);
            }
        }
        if (forecast.getDaily() != null && forecast.getDaily().getDataPoints() != null && forecast.getDaily().getDataPoints().size() > 0) {
            String highTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(0).getTemperatureMax()));
            String lowTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(0).getTemperatureMin()));
            String lowHigh = lowTemp + (char) 0x00B0 + " " + temp + " | " + highTemp + (char) 0x00B0 + " " + temp;
            weatherLowHigh.setText(lowHigh);

            if (forecast.getDaily().getDataPoints().get(0).getPrecipProbability() != null) {
                double testPrecip = forecast.getDaily().getDataPoints().get(0).getPrecipProbability();
                String precipProb = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(0).getPrecipProbability() * 100) + "%");
                weatherPrecip.setText(precipProb);
            }

            if (forecast.getDaily().getDataPoints().size() >= 3) {

                DataPoint dayOne = forecast.getDaily().getDataPoints().get(1);

                if (dayOne.getIcon() != null && dayOne.getIcon().getText() != null) {
                    //Day One!
                    setIconView(dayTwoWeatherIconView, dayOne.getIcon().getText());
                }

                String dayTwoLowHigh = "";
                if (dayOne.getTemperatureMax() != null && dayOne.getTemperatureMin() != null) {
                    //Get Low - High temp
                    String dayTwoHighTemp = String.valueOf(Math.round(dayOne.getTemperatureMax()));
                    String dayTwoLowTemp = String.valueOf(Math.round(dayOne.getTemperatureMin()));
                    dayTwoLowHigh = dayTwoLowTemp + (char) 0x00B0 + " " + temp + " | " + dayTwoHighTemp + (char) 0x00B0 + " " + temp;
                }

                //Get rain prop
                String dayTwoPrecipProb = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(1).getPrecipProbability() * 100) + "%");

                //Get Wind speed
                String dayTwoWindSpeed = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(1).getWindSpeed())) + " " + speed;

                //Get day date
                String dayTwoDate = new SimpleDateFormat("EE ").format(forecast.getDaily().getDataPoints().get(1).getTime());

                dayTwoWeatherLowHigh.setText(dayTwoLowHigh);
                dayTwoWeatherPrecip.setText(dayTwoPrecipProb);
                dayTwoWeatherWindSpeed.setText(dayTwoWindSpeed);
                dayTwoDay.setText(dayTwoDate);

                //Day Two!
                setIconView(dayThreeWeatherIconView, forecast.getDaily().getDataPoints().get(2).getIcon().getText());

                //Get Low - High temp
                String dayThreeHighTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(2).getTemperatureMax()));
                String dayThreeLowTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(2).getTemperatureMin()));
                String dayThreeLowHigh = lowTemp + (char) 0x00B0 + " " + temp + " | " + highTemp + (char) 0x00B0 + " " + temp;

                //Get rain prop
                String dayThreePrecipProb = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(2).getPrecipProbability() * 100) + "%");

                //Get Wind speed
                String dayThreeWindSpeed = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(2).getWindSpeed())) + " " + speed;

                //Get day date
                String dayThreeDate = new SimpleDateFormat("EE").format(forecast.getDaily().getDataPoints().get(2).getTime());

                dayThreeWeatherLowHigh.setText(dayThreeLowHigh);
                dayThreeWeatherPrecip.setText(dayThreePrecipProb);
                dayThreeWeatherWindSpeed.setText(dayThreeWindSpeed);
                dayThreeDay.setText(dayThreeDate);

                //Day Three!
                setIconView(dayFourWeatherIconView, forecast.getDaily().getDataPoints().get(3).getIcon().getText());

                //Get Low - High temp
                String dayFourHighTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(3).getTemperatureMax()));
                String dayFourLowTemp = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(3).getTemperatureMin()));
                String dayFourLowHigh = lowTemp + (char) 0x00B0 + " " + temp + " | " + highTemp + (char) 0x00B0 + " " + temp;

                String dayFourPrecipProb = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(3).getPrecipProbability() * 100) + "%");

                //Get Wind speed
                String dayFourWindSpeed = String.valueOf(Math.round(forecast.getDaily().getDataPoints().get(3).getWindSpeed())) + " " + speed;

                //Get day date
                String dayFourDate = new SimpleDateFormat("EE").format(forecast.getDaily().getDataPoints().get(3).getTime());

                dayFourWeatherLowHigh.setText(dayFourLowHigh);
                dayFourWeatherPrecip.setText(dayFourPrecipProb);
                dayFourWeatherWindSpeed.setText(dayFourWindSpeed);
                dayFourDay.setText(dayFourDate);
            } else {
                threeDayForecast.setVisibility(View.GONE);
            }
        }

        if (forecast.getCurrently().getIcon() != null && forecast.getCurrently().getIcon().getText() != null) {
            setIconView(weatherIconView, forecast.getCurrently().getIcon().getText());
        }

        if (forecast.getDaily() != null && forecast.getDaily().getSummary() != null) {
            weatherSummaryDay.setText(forecast.getDaily().getSummary());
        } else if (forecast.getCurrently() != null && forecast.getCurrently().getSummary() != null) {
            weatherSummaryDay.setText(forecast.getCurrently().getSummary());
        } else {
            weatherSummaryDay.setVisibility(View.GONE);
        }

        weatherLocation.setText(detailLaunch.getLocation().getName());
        weatherCard.setVisibility(View.VISIBLE);

        if (nightMode) {
            dayTwoWeatherWindIcon.setIconColor(Color.WHITE);
            dayTwoWeatherPrecipIcon.setIconColor(Color.WHITE);
            dayThreeWeatherWindIcon.setIconColor(Color.WHITE);
            dayThreeWeatherPrecipIcon.setIconColor(Color.WHITE);
            dayFourWeatherWindIcon.setIconColor(Color.WHITE);
            dayFourWeatherPrecipIcon.setIconColor(Color.WHITE);
            weatherPrecipIcon.setIconColor(Color.WHITE);
            weatherSpeedIcon.setIconColor(Color.WHITE);
        }
    }

    private void setIconView(WeatherIconView view, String icon) {
        if (icon.contains("partly-cloudy-day")) {
            view.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_day));
        } else if (icon.contains("partly-cloudy-night")) {
            view.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_night));
        } else if (icon.contains("clear-day")) {
            view.setIconResource(getString(R.string.wi_forecast_io_clear_day));
        } else if (icon.contains("clear-night")) {
            view.setIconResource(getString(R.string.wi_forecast_io_clear_night));
        } else if (icon.contains("rain")) {
            view.setIconResource(getString(R.string.wi_forecast_io_rain));
        } else if (icon.contains("snow")) {
            view.setIconResource(getString(R.string.wi_forecast_io_snow));
        } else if (icon.contains("sleet")) {
            view.setIconResource(getString(R.string.wi_forecast_io_sleet));
        } else if (icon.contains("wind")) {
            view.setIconResource(getString(R.string.wi_forecast_io_wind));
        } else if (icon.contains("fog")) {
            view.setIconResource(getString(R.string.wi_forecast_io_fog));
        } else if (icon.contains("cloudy")) {
            view.setIconResource(getString(R.string.wi_forecast_io_cloudy));
        } else if (icon.contains("hail")) {
            view.setIconResource(getString(R.string.wi_forecast_io_hail));
        } else if (icon.contains("thunderstorm")) {
            view.setIconResource(getString(R.string.wi_forecast_io_thunderstorm));
        } else if (icon.contains("tornado")) {
            view.setIconResource(getString(R.string.wi_forecast_io_tornado));
        }
        if (nightMode) {
            view.setIconColor(Color.WHITE);
        } else {
            view.setIconColor(Color.BLACK);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setUpViews(Launch launch) {
        try {
            weatherCard.setVisibility(View.GONE);

            detailLaunch = launch;

            // Check if Weather card is enabled, defaults to false if null.
            if (sharedPref.getBoolean("weather", false)) {
                if (detailLaunch.getNet().after(Calendar.getInstance().getTime())) {
                    fetchCurrentWeather();
                } else {
                    fetchPastWeather();
                }
            }

            if (detailLaunch.getRocket() != null) {
                getLaunchVehicle(detailLaunch);
            }

            setupCountdownTimer(launch);

            double dlat = 0;
            double dlon = 0;
            if (detailLaunch.getLocation() != null && detailLaunch.getLocation().getPads() != null) {
                dlat = detailLaunch.getLocation().getPads().get(0).getLatitude();
                dlon = detailLaunch.getLocation().getPads().get(0).getLongitude();
            }

            // Getting status
            if (dlat == 0 && dlon == 0 || Double.isNaN(dlat) || Double.isNaN(dlon) || dlat == Double.NaN || dlon == Double.NaN) {
                if (staticMap != null) {
                    staticMap.setVisibility(View.GONE);
                }
            } else {
                staticMap.setVisibility(View.VISIBLE);
                final Resources res = context.getResources();
                final StaticMap map = new StaticMap()
                        .center(dlat, dlon)
                        .scale(4)
                        .type(StaticMap.Type.ROADMAP)
                        .zoom(7)
                        .marker(dlat, dlon)
                        .key(res.getString(R.string.GoogleMapsKey));

                //Strange but necessary to calculate the height/width
                staticMap.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        map.size(
                                staticMap.getWidth() / 2,
                                staticMap.getHeight() / 2
                        );

                        Timber.v("onPreDraw: %s", map.toString());
                        Glide.with(context).load(map.toString())
                                .error(R.drawable.placeholder)
                                .centerCrop()
                                .into(staticMap);
                        staticMap.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }

            //Setup SimpleDateFormat to parse out getNet date.
            SimpleDateFormat input = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss zzz");
            SimpleDateFormat output = new SimpleDateFormat("MMMM dd, yyyy");
            input.toLocalizedPattern();

            Date mDate;
            String dateText = null;

            switch (detailLaunch.getStatus()) {
                case 1:
                    String go = context.getResources().getString(R.string.status_go);
                    if (detailLaunch.getProbability() != null && detailLaunch.getProbability() > 0) {
                        go = String.format("%s | Forecast - %s%%", go, detailLaunch.getProbability());
                    }
                    //GO for launch
                    launch_status.setText(go);
                    launch_status.setTextColor(ContextCompat.getColor(context, R.color.colorGo));
                    break;
                case 2:
                    //NO GO for launch
                    launch_status.setText(R.string.status_nogo);
                    launch_status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                    break;
                case 3:
                    //Success for launch
                    launch_status.setText(R.string.status_success);
                    launch_status.setTextColor(ContextCompat.getColor(context, R.color.colorGo));
                    break;
                case 4:
                    //Failure to launch
                    launch_status.setText(R.string.status_failure);
                    launch_status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                    break;
            }

            notificationSwitch.setChecked(this.detailLaunch.isNotifiable());
            notificationSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRealm().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (!detailLaunch.isUserToggledNotifiable()) {
                                detailLaunch.setUserToggledNotifiable(true);
                            }
                            detailLaunch.setNotifiable(!detailLaunch.isNotifiable());
                            notificationSwitch.setChecked(detailLaunch.isNotifiable());
                            realm.copyToRealmOrUpdate(detailLaunch);
                            Timber.v("Launch %s notifiable updated to %s", detailLaunch.getName(), detailLaunch.isNotifiable());
                        }
                    });
                    LibraryDataManager libraryDataManager = new LibraryDataManager(context);
                    libraryDataManager.syncNotifiers();
                }
            });

            if (detailLaunch.getVidURLs() != null && detailLaunch.getVidURLs().size() > 0) {
                watchButton.setVisibility(View.VISIBLE);
                watchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Timber.d("Watch: %s", detailLaunch.getVidURLs().size());
                        if (detailLaunch.getVidURLs().size() > 0) {
                            final DialogAdapter adapter = new DialogAdapter(new DialogAdapter.Callback() {

                                @Override
                                public void onListItemSelected(int index, MaterialSimpleListItem item, boolean longClick) {
                                    if (longClick) {
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, detailLaunch.getVidURLs().get(index).getVal()); // Simple text and URL to share
                                        sendIntent.setType("text/plain");
                                        context.startActivity(sendIntent);
                                    } else {
                                        Uri watchUri = Uri.parse(detailLaunch.getVidURLs().get(index).getVal());
                                        Intent i = new Intent(Intent.ACTION_VIEW, watchUri);
                                        context.startActivity(i);
                                    }
                                }
                            });
                            for (RealmStr s : detailLaunch.getVidURLs()) {
                                //Do your stuff here
                                adapter.add(new MaterialSimpleListItem.Builder(context)
                                        .content(s.getVal())
                                        .build());
                            }

                            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                                    .title("Select a Source")
                                    .content("Long press for additional options.")
                                    .adapter(adapter, null)
                                    .negativeText("Cancel");
                            builder.show();
                        }
                    }
                });
            } else {
                watchButton.setVisibility(View.GONE);
            }

            //Try to convert to Month day, Year.
            mDate = detailLaunch.getNet();
            dateText = output.format(mDate);
            if (mDate.before(Calendar.getInstance().getTime())) {
                launch_date_title.setText("Launch Date");
            }

            date.setText(dateText);

            if (detailLaunch.getWsstamp() > 0 && detailLaunch.getWestamp() > 0) {
                setWindowStamp();
            } else {
                launch_window_start.setVisibility(View.GONE);
                launch_window_end.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    private void setupCountdownTimer(Launch launch) {
        //If timestamp is available calculate TMinus and date.
        if (launch.getNetstamp() > 0) {
            long longdate = launch.getNetstamp();
            longdate = longdate * 1000;
            final Date date = new Date(longdate);

            Calendar future = Utils.DateToCalendar(date);
            Calendar now = Calendar.getInstance();

            now.setTimeInMillis(System.currentTimeMillis());
            if (timer != null) {
                Timber.v("Timer is not null, cancelling.");
                timer.cancel();
            }

            final int status = launch.getStatus();
            final String hold = launch.getHoldreason();

            final int nightColor = ContextCompat.getColor(context, R.color.dark_theme_secondary_text_color);
            final int color = ContextCompat.getColor(context, R.color.colorTextSecondary);
            final int accentColor = ContextCompat.getColor(context, R.color.colorAccent);

            contentTMinusStatus.setTypeface(Typeface.SANS_SERIF);
            contentTMinusStatus.setTextColor(accentColor);

            countdownLayout.setVisibility(View.VISIBLE);
            long timeToFinish = future.getTimeInMillis() - now.getTimeInMillis();
            timer = new CountDownTimer(timeToFinish, 1000) {
                StringBuilder time = new StringBuilder();

                @Override
                public void onFinish() {
                    Timber.v("Countdown finished.");
                    contentTMinusStatus.setTypeface(Typeface.DEFAULT);
                    if (sharedPreference.isNightModeActive(context)) {
                        contentTMinusStatus.setTextColor(nightColor);
                    } else {
                        contentTMinusStatus.setTextColor(color);
                    }
                    if (status == 1) {
                        contentTMinusStatus.setText("Watch Live webcast for up to date status.");

                    } else {
                        if (hold != null && hold.length() > 1) {
                            contentTMinusStatus.setText(hold);
                        } else {
                            contentTMinusStatus.setText("Watch Live webcast for up to date status.");
                        }
                    }
                    contentTMinusStatus.setVisibility(View.VISIBLE);
                    countdownDays.setText("- -");
                    countdownHours.setText("- -");
                    countdownMinutes.setText("- -");
                    countdownSeconds.setText("- -");
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    time.setLength(0);

                    // Calculate the Days/Hours/Mins/Seconds numerically.
                    long longDays = millisUntilFinished / 86400000;
                    long longHours = (millisUntilFinished / 3600000) % 24;
                    long longMins = (millisUntilFinished / 60000) % 60;
                    long longSeconds = (millisUntilFinished / 1000) % 60;

                    String days = String.valueOf(longDays);
                    String hours;
                    String minutes;
                    String seconds;

                    // Translate those numerical values to string values.
                    if (longHours < 10) {
                        hours = "0" + String.valueOf(longHours);
                    } else {
                        hours = String.valueOf(longHours);
                    }

                    if (longMins < 10) {
                        minutes = "0" + String.valueOf(longMins);
                    } else {
                        minutes = String.valueOf(longMins);
                    }

                    if (longSeconds < 10) {
                        seconds = "0" + String.valueOf(longSeconds);
                    } else {
                        seconds = String.valueOf(longSeconds);
                    }


                    // Update the views
                    if (Integer.valueOf(days) > 0) {
                        countdownDays.setText(days);
                    } else {
                        countdownDays.setText("- -");
                    }

                    if (Integer.valueOf(hours) > 0) {
                        countdownHours.setText(hours);
                    } else if (Integer.valueOf(days) > 0) {
                        countdownHours.setText("00");
                    } else {
                        countdownHours.setText("- -");
                    }

                    if (Integer.valueOf(minutes) > 0) {
                        countdownMinutes.setText(minutes);
                    } else if (Integer.valueOf(hours) > 0 || Integer.valueOf(days) > 0) {
                        countdownMinutes.setText("00");
                    } else {
                        countdownMinutes.setText("- -");
                    }

                    if (Integer.valueOf(seconds) > 0) {
                        countdownSeconds.setText(seconds);
                    } else if (Integer.valueOf(minutes) > 0 || Integer.valueOf(hours) > 0 || Integer.valueOf(days) > 0) {
                        countdownSeconds.setText("00");
                    } else {
                        countdownSeconds.setText("- -");
                    }

                    // Hide status if countdown is active.
                    contentTMinusStatus.setVisibility(View.GONE);
                }
            }.start();

        } else {
            countdownLayout.setVisibility(View.GONE);
            contentTMinusStatus.setVisibility(View.VISIBLE);
            contentTMinusStatus.setTypeface(Typeface.DEFAULT);
            if (sharedPreference.isNightModeActive(context)) {
                contentTMinusStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_theme_secondary_text_color));
            } else {
                contentTMinusStatus.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            }
            if (timer != null) {
                timer.cancel();
            }
            if (launch.getStatus() != 1) {
                if (launch.getRocket().getAgencies().size() > 0) {
                    contentTMinusStatus.setText(String.format("Pending confirmed GO from %s", launch.getRocket().getAgencies().get(0).getName()));
                } else {
                    contentTMinusStatus.setText("Pending confirmed Go for Launch from launch agency");
                }
            } else {
                if (launch.getRocket().getAgencies().size() > 0) {
                    contentTMinusStatus.setText(String.format("Waiting on exact launch time from %s", launch.getRocket().getAgencies().first().getName()));
                } else {
                    contentTMinusStatus.setText("Waiting on exact launch time from launch provider");
                }
            }
        }
    }

    private void setDefaultCalendar() {
        SwitchPreferences switchPreferences = SwitchPreferences.getInstance(context);
        final List<me.calebjones.spacelaunchnow.calendar.model.Calendar> calendarList = me.calebjones.spacelaunchnow.calendar.model.Calendar.getWritableCalendars(context.getContentResolver());

        if (calendarList.size() > 0) {

            final CalendarItem calendarItem = new CalendarItem();
            calendarItem.setAccountName(calendarList.get(0).accountName);
            calendarItem.setId(calendarList.get(0).id);

            getRealm().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(CalendarItem.class).findAll().deleteAllFromRealm();
                    realm.copyToRealm(calendarItem);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    CalendarSyncManager calendarSyncManager = new CalendarSyncManager(context);
                    calendarSyncManager.syncAllEevnts();
                }
            });
        } else {
            Toast.makeText(context, "No Calendars available to sync detailLaunch events with.", Toast.LENGTH_LONG).show();
            switchPreferences.setCalendarStatus(false);
        }
    }

    private void getLaunchVehicle(Launch vehicle) {
        String query;
        if (vehicle.getRocket().getName().contains("Space Shuttle")) {
            query = "Space Shuttle";
        } else {
            query = vehicle.getRocket().getName();
        }

        launchVehicle = getRealm().where(RocketDetails.class).contains("name", query).findFirst();
    }

    private void setWindowStamp() {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter;
        if (sharedPref.getBoolean("24_hour_mode", false)) {
            formatter = new SimpleDateFormat("HH:mm zzz");
        } else {
            formatter = new SimpleDateFormat("hh:mm a zzz");
        }

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendarStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long startDate = detailLaunch.getWsstamp();
        startDate = startDate * 1000;
        calendarStart.setTimeInMillis(startDate);
        String start = formatter.format(calendarStart.getTime());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendarEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long endDate = detailLaunch.getWestamp();
        endDate = endDate * 1000;
        calendarEnd.setTimeInMillis(endDate);
        String end = formatter.format(calendarEnd.getTime());

        if (!start.equals(end)) {
            if (start.length() == 0
                    || end.length() == 0) {
                launch_window_start.setText(String.format("Launch Time: %s",
                        start));
                launch_window_end.setVisibility(View.GONE);
            } else {
                launch_window_start.setText(String.format("Window Start: %s",
                        start));
                launch_window_end.setVisibility(View.VISIBLE);
                launch_window_end.setText(String.format("Window End: %s",
                        end));
            }
        } else {
            launch_window_start.setText(String.format("Launch Time: %s",
                    start));
            launch_window_end.setVisibility(View.GONE);
        }
    }

    private void setWindowStartEnd() {
        if (!detailLaunch.getWindowstart().equals(detailLaunch.getWindowend())) {
            if (detailLaunch.getWindowstart().toString().length() > 0
                    || detailLaunch.getWindowend().toString().length() > 0) {
                launch_window_start.setText("Launch Window unavailable.");
                launch_window_end.setVisibility(View.INVISIBLE);
            } else {
                launch_window_start.setText(String.format("Window Start: %s",
                        detailLaunch.getWindowstart()));
                launch_window_end.setVisibility(View.VISIBLE);
                launch_window_end.setText(String.format("Window End: %s",
                        detailLaunch.getWindowend()));
            }
        } else {
            launch_window_start.setText(String.format("Launch Time: %s",
                    detailLaunch.getWindowstart()));
            launch_window_end.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static SummaryDetailFragment newInstance() {
        return new SummaryDetailFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LaunchEvent event) {
        setLaunch(event.launch);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
