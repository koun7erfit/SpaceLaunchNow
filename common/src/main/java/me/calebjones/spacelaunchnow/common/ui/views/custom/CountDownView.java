package me.calebjones.spacelaunchnow.common.ui.views.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.databinding.CountdownLayoutViewBinding;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.common.ui.views.CountDownTimer;
import timber.log.Timber;

public class CountDownView extends ConstraintLayout {

    private CountDownTimer timer;
    private Disposable var;
    private Launch launch;
    private Context context;
    private CountdownLayoutViewBinding binding;

    public CountDownView(Context context) {
        super(context);
        init(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = CountdownLayoutViewBinding.inflate(inflater);
        this.context = context;
    }

    private void resetCountdown() {
        if (timer != null) {
            Timber.v("Timer is not null, cancelling.");
            timer.cancel();
        }

        if (var != null) {
            var.dispose();
        }
    }

    private void startLaunchCountdown(long timeToFinish) {
        timer = new CountDownTimer(timeToFinish, 1000) {
            StringBuilder time = new StringBuilder();

            @Override
            public void onFinish() {
                Timber.v("Countdown finished.");
                binding.countdownDays.setText("00");
                binding.countdownHours.setText("00");
                binding.countdownMinutes.setText("00");
                binding.countdownSeconds.setText("00");
                binding.countdownStatus.setVisibility(View.VISIBLE);
                binding.countdownStatus.setText("+");
                if (launch.isValid()) {
                    countUpTimer(launch.getNet().getTime());
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                time.setLength(0);
                setCountdownView(millisUntilFinished);
            }
        }.start();
    }

    private void setLaunchCountdownComplete() {
        binding.countdownDays.setText("00");
        binding.countdownHours.setText("00");
        binding.countdownMinutes.setText("00");
        binding.countdownSeconds.setText("00");
    }

    private void launchInFlight() {
        binding.countdownStatus.setVisibility(View.VISIBLE);
        countUpTimer(launch.getNet().getTime());
    }

    private void launchStatusUnknown() {
        binding.countdownDays.setText("- -");
        binding.countdownHours.setText("- -");
        binding.countdownMinutes.setText("- -");
        binding.countdownSeconds.setText("- -");
        binding.countdownViewGroup.setVisibility(GONE);
    }

    private void countUpTimer(long longDate) {
        var = Observable
                .interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(
                        time -> {
                            Calendar currentTime = Calendar.getInstance();
                            long timeSince = currentTime.getTimeInMillis() - longDate;
                            setCountdownView(timeSince);
                        });
    }

    private void setCountdownView(long millisUntilFinished) {

        // Calculate the Days/Hours/Mins/Seconds numerically.
        long longDays = millisUntilFinished / 86400000;
        long longHours = (millisUntilFinished / 3600000) % 24;
        long longMins = (millisUntilFinished / 60000) % 60;
        long longSeconds = (millisUntilFinished / 1000) % 60;

        String days;
        String hours;
        String minutes;
        String seconds;

        if (longDays < 10) {
            days = "0" + String.valueOf(longDays);
        } else {
            days = String.valueOf(longDays);
        }


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


        try {
            // Update the views
            if (Integer.valueOf(days) > 0) {
                binding.countdownDays.setText(days);
            } else {
                binding.countdownDays.setText("00");
            }

            if (Integer.valueOf(hours) > 0) {
                binding.countdownHours.setText(hours);
            } else if (Integer.valueOf(days) > 0) {
                binding.countdownHours.setText("00");
            } else {
                binding.countdownHours.setText("00");
            }

            if (Integer.valueOf(minutes) > 0) {
                binding.countdownMinutes.setText(minutes);
            } else if (Integer.valueOf(hours) > 0 || Integer.valueOf(days) > 0) {
                binding.countdownMinutes.setText("00");
            } else {
                binding.countdownMinutes.setText("00");
            }

            if (Integer.valueOf(seconds) > 0) {
                binding.countdownSeconds.setText(seconds);
            } else if (Integer.valueOf(minutes) > 0 || Integer.valueOf(hours) > 0 || Integer.valueOf(days) > 0) {
                binding.countdownSeconds.setText("00");
            } else {
                binding.countdownSeconds.setText("00");
            }
        } catch (NumberFormatException e) {
            binding.countdownHours.setText("00");
            binding.countdownDays.setText("00");
            binding.countdownMinutes.setText("00");
            binding.countdownSeconds.setText("00");
        }
    }

    public void setLaunch(Launch launch) {
        this.launch = launch;
        checkCountdownTimer(this.launch);
        binding.statusPill.setStatus(launch);
    }

    private void checkCountdownTimer(Launch launch) {
        long longdate = launch.getNet().getTime();
        final Date date = new Date(longdate);

        Calendar launchDate = DateToCalendar(date);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        now.setTimeInMillis(System.currentTimeMillis());

        resetCountdown();

        String hold = launch.getHoldreason();
        String failure = launch.getFailreason();
        binding.commonStatusReason.setVisibility(GONE);
        if (hold != null && hold.length() > 0) {
            binding.commonStatusReason.setText(hold);
            setReasonConstraintToBottom();
            binding.commonStatusReason.setVisibility(VISIBLE);
        }

        if (failure != null && failure.length() > 0) {
            binding.commonStatusReason.setText(failure);
            setReasonConstraintToBottom();
            binding.commonStatusReason.setVisibility(VISIBLE);
        }

        if (launch.getStatus().getId() == 2){
            binding.commonStatusReason.setText(R.string.date_unconfirmed);
            binding.commonStatusReason.setVisibility(VISIBLE);
            setReasonConstraintToStatusPill();
        } else if (launch.getStatus().getId() == 8){
            binding.commonStatusReason.setText(R.string.to_be_confirmed);
            setReasonConstraintToStatusPill();
            binding.commonStatusReason.setVisibility(VISIBLE);
        } else {
            binding.commonStatusReason.setVisibility(GONE);
        }

        long timeToFinish = launchDate.getTimeInMillis() - now.getTimeInMillis();
        binding.countdownViewGroup.setVisibility(VISIBLE);
        if (timeToFinish > 0 && launch.getStatus().getId() == 1) {
            startLaunchCountdown(timeToFinish);
        } else if (launch.getStatus().getId() == 3 || launch.getStatus().getId() == 4 || launch.getStatus().getId() == 7) {
            setLaunchCountdownComplete();
        } else if (launch.getStatus().getId() == 6 || launch.getStatus().getId() == 1) {
            launchInFlight();
        } else {
            launchStatusUnknown();
        }
    }

    private void setReasonConstraintToBottom() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.commonCountdownLayout);
        constraintSet.connect(R.id.common_status_reason, ConstraintSet.TOP, R.id.bottom_divider, ConstraintSet.BOTTOM,60);
        constraintSet.applyTo(binding.commonCountdownLayout);
    }

    private void setReasonConstraintToStatusPill() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.commonCountdownLayout);
        constraintSet.connect(R.id.common_status_reason, ConstraintSet.TOP, R.id.status_pill, ConstraintSet.BOTTOM,20);
        constraintSet.applyTo(binding.commonCountdownLayout);
    }

    public Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return cal;
    }
}
