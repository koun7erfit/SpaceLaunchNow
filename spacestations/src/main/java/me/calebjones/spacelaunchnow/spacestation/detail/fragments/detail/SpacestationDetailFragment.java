package me.calebjones.spacelaunchnow.spacestation.detail.fragments.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.spacestation.R;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationDetailFragmentBinding;
import me.calebjones.spacelaunchnow.spacestation.detail.SpacestationDetailViewModel;

public class SpacestationDetailFragment extends BaseFragment {

    private SpacestationDetailViewModel mViewModel;
    private SpacestationSpecAdapter adapter;
    private SpacestationOwnerAdapter ownerAdapter;
    private Context context;
    private GridLayoutManager layoutManager;
    private SpacestationDetailFragmentBinding binding;

    public static SpacestationDetailFragment newInstance() {
        return new SpacestationDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SpacestationDetailFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        adapter = new SpacestationSpecAdapter();
        ownerAdapter = new SpacestationOwnerAdapter(context);
        layoutManager = new GridLayoutManager(context, 2);
        binding.ownersRecycler.setLayoutManager(new LinearLayoutManager(context));
        binding.ownersRecycler.setAdapter(ownerAdapter);
        binding.ownersRecycler.addItemDecoration(new SimpleDividerItemDecoration(context));
        binding.specs.setLayoutManager(layoutManager);
        binding.specs.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(SpacestationDetailViewModel.class);
        // update UI
        mViewModel.getSpacestation().observe(this, this::setSpacestation);
    }

    private void setSpacestation(Spacestation spacestation) {

        if (spacestation.getDeorbited() != null) {
            binding.deorbited.setVisibility(View.VISIBLE);
            String deorbited = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(spacestation.getDeorbited());
            binding.deorbited.setText(String.format(getString(R.string.deorbited), deorbited));
        } else {
            binding.deorbited.setVisibility(View.GONE);
        }

        if (spacestation.getFounded() != null) {
            binding.founded.setVisibility(View.VISIBLE);
            String founded = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(spacestation.getFounded());
            binding.founded.setText(String.format(getString(R.string.founded), founded));
        } else {
            binding.founded.setVisibility(View.GONE);
        }

        binding.spacestaionDetailSubtitle.setText(String.format(getString(R.string.status), spacestation.getStatus().getName()));
        binding.description.setText(spacestation.getDescription());
        int color = ThemeHelper.getIconColor(getActivity());
        List<SpacestationSpecItem> specs = new ArrayList<>();
        if (spacestation.getHeight() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.height_plain),
                    String.format(getString(R.string.height_unit), spacestation.getHeight().toString()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(GoogleMaterial.Icon.gmd_swap_vert)
                            .sizeDp(24)));
        }

        if (spacestation.getWidth() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.width_plain),
                    String.format(getString(R.string.width_unit), spacestation.getWidth().toString()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(GoogleMaterial.Icon.gmd_swap_horiz)
                            .sizeDp(24)));
        }

        if (spacestation.getMass() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.mass_plain),
                    String.format(getString(R.string.mass_unit), spacestation.getMass().toString()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(FontAwesome.Icon.faw_weight)
                            .sizeDp(24)));
        }

        if (spacestation.getVolume() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.volume_plain),
                    String.format(getString(R.string.volume_unit), spacestation.getVolume().toString()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(FontAwesome.Icon.faw_cubes)
                            .sizeDp(24)));
        }
        if (spacestation.getVolume() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.crew_plain),
                    String.format("%s", spacestation.getOnboardCrew()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(FontAwesome.Icon.faw_user_astronaut)
                            .sizeDp(24)));
        }
        if (spacestation.getVolume() != null) {
            specs.add(new SpacestationSpecItem(getString(R.string.orbit),
                    String.format("%s", spacestation.getOrbit()),
                    new IconicsDrawable(context)
                            .color(color)
                            .icon(FontAwesome.Icon.faw_globe)
                            .sizeDp(24)));
        }
        adapter.addItems(specs);
        ownerAdapter.addItems(spacestation.getOwners());
        String fill = String.valueOf(spacestation.getOwners().size());
        binding.spacestationDetailTitle.setText(String.format(getString(R.string.details_fill), spacestation.getName()));
        binding.spacestaionOwnerSubtitle.setText(String.format(getString(R.string.total_fill), fill));

    }
}
