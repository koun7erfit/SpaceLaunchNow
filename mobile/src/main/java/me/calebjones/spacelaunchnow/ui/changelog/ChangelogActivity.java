package me.calebjones.spacelaunchnow.ui.changelog;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.databinding.ActivityChangelogBinding;
import me.calebjones.spacelaunchnow.utils.Utils;

public class ChangelogActivity extends AppCompatActivity {

    private ActivityChangelogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangelogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.toolbar.setTitle(getResources().getString(R.string.whats_new) + " v" + Utils.getVersionName(this));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        binding.markdownView.loadMarkdownFromAssets("CHANGELOG.md");
        binding.markdownView.setOpenUrlInBrowser(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
