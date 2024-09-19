package me.calebjones.spacelaunchnow.starship.ui.vehicles;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.data.models.main.dashboards.Starship;
import me.calebjones.spacelaunchnow.starship.StarshipDashboardViewModel;
import me.calebjones.spacelaunchnow.starship.data.StarshipDataRepository;
import me.spacelaunchnow.starship.R;
import me.spacelaunchnow.starship.databinding.FragmentStarshipVehiclesBinding;
import me.spacelaunchnow.starship.databinding.StarshipDashboardVehiclesBinding;

/**
 * A fragment representing the Starship Dashboard
 */
public class StarshipVehiclesFragment extends BaseFragment {


    private StarshipDataRepository dataRepository;
    private boolean firstLaunch = true;

    private StarshipDashboardViewModel model;
    private LauncherAdapter launcherAdapter;
    private FragmentStarshipVehiclesBinding binding;
    private StarshipDashboardVehiclesBinding starshipDashboardVehiclesBinding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StarshipVehiclesFragment() {
    }

    @SuppressWarnings("unused")
    public static StarshipVehiclesFragment newInstance() {
        return new StarshipVehiclesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRepository = new StarshipDataRepository(getContext(), getRealm());
        setScreenName("Starship Vehicle Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStarshipVehiclesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        starshipDashboardVehiclesBinding =  binding.starshipDashboardVehicles;

        setHasOptionsMenu(true);
        model = ViewModelProviders.of(getParentFragment()).get(StarshipDashboardViewModel.class);
        model.getStarshipDashboard().observe(this, this::updateViews);

        launcherAdapter = new LauncherAdapter(getContext());
        starshipDashboardVehiclesBinding.launcherRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        starshipDashboardVehiclesBinding.launcherRecycler.setAdapter(launcherAdapter);

        return view;
    }

    //Currently only used to debug
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateViews(Starship starship) {
        if (!starship.getVehicles().isEmpty()) {
            launcherAdapter.addItems(starship.getVehicles());
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}
