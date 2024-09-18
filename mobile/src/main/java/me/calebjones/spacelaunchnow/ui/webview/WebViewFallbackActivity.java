package me.calebjones.spacelaunchnow.ui.webview;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.base.BaseActivityOld;
import me.calebjones.spacelaunchnow.common.utils.analytics.Analytics;
import me.calebjones.spacelaunchnow.databinding.ActivityWebViewFallbackBinding;
import me.spacelaunchnow.astronauts.databinding.ActivityAstronautDetailsBinding;

public class WebViewFallbackActivity extends BaseActivityOld {
    public static final String EXTRA_URL = "extra.url";

    private ActivityWebViewFallbackBinding binding;

//    @BindView(R.id.progressView)
//    ProgressBar bar;

    public WebViewFallbackActivity() {
        super("Web View Activity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewFallbackBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);

        String url = getIntent().getStringExtra(EXTRA_URL);
        binding.webview.setWebViewClient(new WebViewClient());
        WebSettings webSettings = binding.webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        setTitle("Space Launch Now - " + url);

        binding.webview.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                if(progress < 100 && binding.progressView.getVisibility() == ProgressBar.GONE){
                    binding.progressView.setVisibility(ProgressBar.VISIBLE);
                }
                binding.progressView.setProgress(progress);
                if(progress == 100) {
                    binding.progressView.setVisibility(ProgressBar.GONE);
                }
            }
        });
        Analytics.getInstance().sendScreenView("Web View Activity", "Loaded URL: " + url);

        if (url != null) {
            binding.webview.loadUrl(url);
        }
    }

}
