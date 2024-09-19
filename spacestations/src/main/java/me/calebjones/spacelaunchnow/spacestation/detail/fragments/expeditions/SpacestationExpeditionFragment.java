package me.calebjones.spacelaunchnow.spacestation.detail.fragments.expeditions;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import me.calebjones.spacelaunchnow.common.base.BaseFragment;
import me.calebjones.spacelaunchnow.common.utils.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Expedition;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Spacestation;
import me.calebjones.spacelaunchnow.data.networking.DataClient;
import me.calebjones.spacelaunchnow.data.networking.responses.base.ExpeditionResponse;
import me.calebjones.spacelaunchnow.spacestation.databinding.SpacestationExpeditionFragmentBinding;
import me.calebjones.spacelaunchnow.spacestation.detail.SpacestationDetailViewModel;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.ActiveExpeditionItem;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.ExpeditionItem;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.ListItem;
import me.calebjones.spacelaunchnow.spacestation.detail.adapter.SpacestationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpacestationExpeditionFragment extends BaseFragment {

    private SpacestationDetailViewModel mViewModel;
    private SpacestationAdapter adapter;
    private SpacestationAdapter pastAdapter;
    private Context context;
    private SpacestationExpeditionFragmentBinding binding;

    public static SpacestationExpeditionFragment newInstance() {
        return new SpacestationExpeditionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SpacestationExpeditionFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        adapter = new SpacestationAdapter(context);
        binding.activeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.activeRecyclerView.setAdapter(adapter);
        pastAdapter = new SpacestationAdapter(context);
        binding.pastExpeditionRecyclerview.setLayoutManager(new LinearLayoutManager(context));
        binding.pastExpeditionRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(context));
        binding.pastExpeditionRecyclerview.setAdapter(pastAdapter);
        binding.statefulView.getProgressView();
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
        if (spacestation != null && spacestation.getActiveExpeditions() != null) {
            List<ListItem> items = new ArrayList<>();
            for (Expedition expedition : spacestation.getActiveExpeditions()) {
                ActiveExpeditionItem item = new ActiveExpeditionItem(expedition);
                items.add(item);
            }
            adapter.clear();
            adapter.addItems(items);
        }
        if (adapter.getItemCount() > 0) {
            binding.statefulView.showContent();
        } else {
            binding.statefulView.showEmpty();
        }
        binding.pastCardView.setVisibility(View.GONE);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        DataClient.getInstance().getExpeditions(100, 0, null, null, spacestation.getId(), formattedDate, new Callback<ExpeditionResponse>() {
            @Override
            public void onResponse(Call<ExpeditionResponse> call, Response<ExpeditionResponse> response) {
                if (response.isSuccessful()){
                    if (response.body() != null) {
                        List<Expedition> pastExpeditions = response.body().getExpeditions();
                        List<ListItem> items = new ArrayList<>();
                        for (Expedition expedition : pastExpeditions) {
                            ExpeditionItem item = new ExpeditionItem(expedition);
                            items.add(item);
                        }
                        int pastCount = pastExpeditions.size();
                        int active = binding.activeRecyclerView.getAdapter().getItemCount();
                        String total = String.valueOf(pastCount + active);
                        binding.spacestaionPastSubtitle.setText(String.format("Total Expeditions: %s", total));
                        if (!items.isEmpty()) {
                            pastAdapter.clear();
                            pastAdapter.addItems(items);
                            binding.pastCardView.setVisibility(View.VISIBLE);
                        } else {
                            if (adapter.getItemCount() == 0) binding.statefulView.showEmpty();
                            binding.pastCardView.setVisibility(View.GONE);
                        }
                        return;
                    }
                }
                if (adapter.getItemCount() == 0 && pastAdapter.getItemCount() == 0) {
                    binding.statefulView.showEmpty();
                    binding.pastCardView.setVisibility(View.GONE);
                } else if (adapter.getItemCount() != 0 || pastAdapter.getItemCount() != 0){
                    binding.statefulView.showContent();
                }
            }

            @Override
            public void onFailure(Call<ExpeditionResponse> call, Throwable t) {
                if (adapter.getItemCount() == 0) binding.statefulView.showEmpty();
                binding.pastCardView.setVisibility(View.GONE);
            }
        });
    }
}
