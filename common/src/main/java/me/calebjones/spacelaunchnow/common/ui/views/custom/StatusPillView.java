package me.calebjones.spacelaunchnow.common.ui.views.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.calebjones.spacelaunchnow.common.content.LaunchStatusUtil;
import me.calebjones.spacelaunchnow.common.databinding.StatusPillBinding;
import me.calebjones.spacelaunchnow.data.models.main.Launch;

public class StatusPillView extends FrameLayout {

    private StatusPillBinding binding;
    private Context context;

    public StatusPillView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public StatusPillView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StatusPillView(@NonNull Context context,
                          @Nullable AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = StatusPillBinding.inflate(inflater);
        this.context = context;
    }

    public void setStatus(Launch launch) {
        binding.status.setText(LaunchStatusUtil.getLaunchStatusTitle(
                context,
                launch.getStatus().getId()
        ));
        binding.statusPillLayout.setCardBackgroundColor(LaunchStatusUtil.getLaunchStatusColor(
                context,
                launch.getStatus().getId()
        ));
    }
}
