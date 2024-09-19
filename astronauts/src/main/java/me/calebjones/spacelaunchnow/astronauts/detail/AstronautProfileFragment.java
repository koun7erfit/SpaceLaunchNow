package me.calebjones.spacelaunchnow.astronauts.detail;

import static me.calebjones.spacelaunchnow.common.utils.LinkHandler.openCustomTab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.prefs.ThemeHelper;
import me.calebjones.spacelaunchnow.common.ui.launchdetail.launches.agency.AgencyLaunchActivity;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.Agency;
import me.calebjones.spacelaunchnow.data.models.main.astronaut.Astronaut;
import me.spacelaunchnow.astronauts.R;
import me.spacelaunchnow.astronauts.databinding.AstronautProfileFragmentBinding;
import timber.log.Timber;

public class AstronautProfileFragment extends BaseFragment {

    private AstronautDetailViewModel mViewModel;
    private Context context;
    private AstronautProfileFragmentBinding binding;

    public static AstronautProfileFragment newInstance() {
        return new AstronautProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AstronautProfileFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(AstronautDetailViewModel.class);
        // update UI
        mViewModel.getAstronaut().observe(this, this::setAstronaut);
        mViewModel.getAgency().observe(this, this::setAgency);
    }

    private void setAstronaut(Astronaut astronaut) {
        binding.astronautBioText.setText(astronaut.getBio());
        binding.astronautStatus.setText(astronaut.getStatus().getName());
        int color = ThemeHelper.getIconColor(getActivity());
        binding.astronautInstagramButton.setImageDrawable(new IconicsDrawable(context).icon(FontAwesome.Icon.faw_instagram).sizeDp(24).color(color));
        binding.astronautWikiButton.setImageDrawable(new IconicsDrawable(context).icon(FontAwesome.Icon.faw_wikipedia_w).sizeDp(24).color(color));
        binding.astronautTwitterButton.setImageDrawable(new IconicsDrawable(context).icon(FontAwesome.Icon.faw_twitter).sizeDp(24).color(color));

        if (astronaut.getInstagram() != null) {
            binding.astronautInstagramButton.setVisibility(View.VISIBLE);
            binding.astronautInstagramButton.setOnClickListener(v -> {
                openCustomTab(context, astronaut.getInstagram());
            });
        } else {
            binding.astronautInstagramButton.setVisibility(View.GONE);
        }

        if (astronaut.getTwitter() != null) {
            binding.astronautTwitterButton.setVisibility(View.VISIBLE);
            binding.astronautTwitterButton.setOnClickListener(v -> {
                openCustomTab(context, astronaut.getTwitter());
            });
        } else {
            binding.astronautTwitterButton.setVisibility(View.GONE);
        }

        if (astronaut.getTwitter() == null
                & astronaut.getInstagram() == null
                & astronaut.getWiki() != null) {
            binding.astronautWikiButtonSolo.setVisibility(View.VISIBLE);
            binding.astronautWikiButton.setVisibility(View.GONE);
            binding.astronautWikiButtonSolo.setOnClickListener(v -> {
                openCustomTab(context, astronaut.getWiki());
            });
        } else if (astronaut.getWiki() != null) {
            binding.astronautWikiButton.setVisibility(View.VISIBLE);
            binding.astronautWikiButton.setOnClickListener(v -> {
                openCustomTab(context, astronaut.getWiki());
            });
        } else {
            binding.astronautWikiButton.setVisibility(View.GONE);
        }



        String bornDate = null;
        String deathDate = null;

        if (astronaut.getDateOfBirth() != null) {
            bornDate = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(astronaut.getDateOfBirth());
        }
        if (astronaut.getDateOfDeath() != null) {
            deathDate = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(astronaut.getDateOfDeath());
        }


        if (bornDate != null && deathDate == null) {
            int bornYear = astronaut.getDateOfBirth().getYear();
            int currentYear = Calendar.getInstance().getTime().getYear();
            binding.astronautBorn.setText(getString(R.string.born, bornDate, currentYear - bornYear));
            binding.astronautDied.setVisibility(View.GONE);
        }
        if (deathDate != null && bornDate != null) {
            int bornYear = astronaut.getDateOfBirth().getYear();
            int diedYear = astronaut.getDateOfDeath().getYear();
            binding.astronautBorn.setText(getString(R.string.born_one_argument, bornDate));
            binding.astronautDied.setText(getString(R.string.died_two_arguments, deathDate, diedYear - bornYear));
            binding.astronautDied.setVisibility(View.VISIBLE);
        }
    }

    private void setAgency(Agency agency) {
        try {
            Timber.v("Setting up views...");
            if (agency != null) {
                binding.lspAgency.setOnClickListener(view -> launchesClicked());
                binding.lspCard.setVisibility(View.VISIBLE);

                binding.lspAgency.setText(String.format(this.getString(me.calebjones.spacelaunchnow.common.R.string.view_rocket_launches), agency.getName()));
                if (agency.getLogoUrl() != null) {
                    binding.lspLogo.setVisibility(View.VISIBLE);
                    GlideApp.with(context)
                            .load(agency.getLogoUrl())
                            .centerInside()
                            .into(binding.lspLogo);
                } else {
                    binding.lspLogo.setVisibility(View.GONE);
                }
                binding.lspName.setText(agency.getName());
                binding.lspType.setText(agency.getType());
                if (agency.getAdministrator() != null) {
                    binding.lspAdministrator.setText(String.format("%s", agency.getAdministrator()));
                } else {
                    binding.lspAdministrator.setText(me.calebjones.spacelaunchnow.common.R.string.unknown_administrator);
                }
                if (agency.getFoundingYear() != null) {
                    binding.lspFoundedYear.setText(String.format(getString(me.calebjones.spacelaunchnow.common.R.string.founded_in), agency.getFoundingYear()));
                } else {
                    binding.lspFoundedYear.setText(me.calebjones.spacelaunchnow.common.R.string.unknown_year);
                }
                binding.lspSummary.setText(agency.getDescription());
                if (agency.getInfoUrl() == null) {
                    binding.lspInfoButtonOne.setVisibility(View.GONE);
                }

                if (agency.getWikiUrl() == null) {
                    binding.lspWikiButtonOne.setVisibility(View.GONE);
                }

                binding.lspAgency.setVisibility(View.VISIBLE);
            } else {
                binding.lspCard.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    void launchesClicked(){
        try {
            Intent intent = new Intent(context, AgencyLaunchActivity.class);
            intent.putExtra("lspName", mViewModel.getAstronaut().getValue().getAgency().getName());
            startActivity(intent);
        } catch (NullPointerException e){
            Timber.e(e);
        }
    }
}
