package me.calebjones.spacelaunchnow.spacestation.detail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import me.calebjones.spacelaunchnow.common.GlideApp;
import me.calebjones.spacelaunchnow.common.utils.Utils;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.DockingLocation;
import me.calebjones.spacelaunchnow.data.models.main.spacestation.Expedition;
import me.calebjones.spacelaunchnow.data.networking.DataClient;
import me.calebjones.spacelaunchnow.spacestation.R;
import me.calebjones.spacelaunchnow.common.ui.adapters.CrewAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This adapter takes data from ListPreferences/LoaderService and applies it to RecyclerView
 */
public class SpacestationAdapter extends RecyclerView.Adapter<SpacestationAdapter.ViewHolder> {

    public int position;
    private Context context;
    private List<ListItem> items;


    public SpacestationAdapter(Context context) {
        this.context = context;
        items = new ArrayList<>();
    }

    public void addItems(List<ListItem> items) {

        if (this.items != null) {
            this.items.addAll(items);
        } else {
            this.items = new ArrayList<>();
            this.items.addAll(items);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getListItemType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = null;
        switch (type) {
            case ListItem.TYPE_DOCKED_VEHICLE:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_docked_vehicle, viewGroup, false);
                return new ViewHolderDockedVehicle(view);
            case ListItem.TYPE_DOCKING_EVENT:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_docking_event, viewGroup, false);
                return new ViewHolderDockingEvent(view);
            case ListItem.TYPE_ACTIVE_EXPEDITION:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_active_expedition, viewGroup, false);
                return new ViewHolderActiveExpedition(view);
            case ListItem.TYPE_PAST_EXPEDITION:
                view = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(R.layout.item_past_expedition, viewGroup, false);
                return new ViewHolderPastExpedition(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int pos) {
        ListItem item = items.get(pos);
        viewHolder.bindType(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindType(ListItem item);
    }

    public class ViewHolderDockedVehicle extends ViewHolder {
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private ImageView humanRated;
        private TextView dockedLocation;

        public ViewHolderDockedVehicle(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.orbiter_image);
            title = itemView.findViewById(R.id.spacestation_docked_title);
            subtitle = itemView.findViewById(R.id.spacestation_docked_subtitle);
            humanRated = itemView.findViewById(R.id.human_rated_icon);
            dockedLocation = itemView.findViewById(R.id.docked_text);
        }

        public void bindType(ListItem item) {
            DockingLocationItem dockedVehicleItem = (DockingLocationItem) item;
            DockingLocation dockingLocation = dockedVehicleItem.getDockingLocation();
            dockedLocation.setText(dockingLocation.getName());
            if (dockingLocation.getDocked() != null) {
                GlideApp.with(context)
                        .load(dockingLocation.getDocked().getFlightVehicle().getSpacecraft().getConfiguration().getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(image);

                title.setText(dockingLocation.getDocked().getFlightVehicle().getSpacecraft().getName());
                subtitle.setText(dockingLocation.getDocked().getFlightVehicle().getSpacecraft().getSerialNumber());
                if (dockingLocation.getDocked().getFlightVehicle().getSpacecraft().getConfiguration().getHumanRated() == null) {
                    GlideApp.with(context)
                            .load(R.drawable.ic_question_mark)
                            .into(humanRated);
                } else if (dockingLocation.getDocked().getFlightVehicle().getSpacecraft().getConfiguration().getHumanRated()) {
                    GlideApp.with(context)
                            .load(R.drawable.ic_checkmark)
                            .into(humanRated);
                } else {
                    GlideApp.with(context)
                            .load(R.drawable.ic_failed)
                            .into(humanRated);
                }
            }
        }
    }

    public class ViewHolderDockingEvent extends ViewHolder {
        private final TextView mTextView;

        public ViewHolderDockingEvent(View itemView) {
            super(itemView);

            mTextView = itemView.findViewById(R.id.txtView);
        }

        public void bindType(ListItem item) {
            DockingEventItem dockingEventItem = (DockingEventItem) item;
            mTextView.setText(((DockingEventItem) item).getDockingEvent().getDockingLocation().getName());
        }
    }

    public class ViewHolderActiveExpedition extends ViewHolder {
        private final TextView mTextView;
        private RecyclerView crewRecyler;
        private TextView title;
        private TextView subtitle;
        private TextView start;
        private TextView end;

        public ViewHolderActiveExpedition(View itemView) {
            super(itemView);

            mTextView = itemView.findViewById(R.id.txtView);
            crewRecyler = itemView.findViewById(R.id.crew_recycler_view);
            title = itemView.findViewById(R.id.spacestation_active_title);
            subtitle = itemView.findViewById(R.id.spacestaion_active_subtitle);
            start = itemView.findViewById(R.id.start_date);
            end = itemView.findViewById(R.id.end_date);
        }

        public void bindType(ListItem item) {
            Expedition expedition = ((ActiveExpeditionItem) item).getExpedition();
            title.setText(expedition.getName());
            if (expedition.getStart() != null) {
                start.setVisibility(View.VISIBLE);
                start.setText(String.format(context.getString(R.string.start_fill), Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(expedition.getStart())));
            } else {
                start.setVisibility(View.GONE);
            }

            if (expedition.getEnd() != null) {
                end.setVisibility(View.VISIBLE);
                end.setText(String.format(context.getString(R.string.end_fill), Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(expedition.getEnd())));
            } else {
                end.setVisibility(View.GONE);
            }

            crewRecyler.setLayoutManager(new LinearLayoutManager(context));
            crewRecyler.setAdapter(new CrewAdapter(context, expedition.getCrew()));
        }
    }

    public class ViewHolderPastExpedition extends ViewHolder implements View.OnClickListener {
        private final TextView title;
        private final TextView subtitle;
        private final View rootView;
        private ExpeditionItem expeditionItem;
        private RecyclerView crewRecyler;

        public ViewHolderPastExpedition(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.sub_title);
            rootView = itemView.findViewById(R.id.rootView);
            crewRecyler = itemView.findViewById(R.id.crew_recycler_view);
        }

        public void bindType(ListItem item) {
            expeditionItem = ((ExpeditionItem) item);
            Expedition expedition = ((ExpeditionItem) item).getExpedition();
            title.setText(expedition.getName());
            String startDate = "?";
            String endDate = "?";
            if (expedition.getStart() != null) {
                startDate = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(expedition.getStart());
            }
            if (expedition.getEnd() != null) {
                endDate = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy").format(expedition.getEnd());
            }
            subtitle.setText(String.format("%s - %s", startDate, endDate));
            rootView.setOnClickListener(this);
            crewRecyler.setNestedScrollingEnabled(false);
            ((SimpleItemAnimator) crewRecyler.getItemAnimator()).setSupportsChangeAnimations(false);
            if (expeditionItem.isExpanded()) {
                if (expeditionItem.getExpedition().getCrew() == null) {
                    getCrew(expedition);
                } else {
                    crewRecyler.setVisibility(View.VISIBLE);
                    crewRecyler.setLayoutManager(new LinearLayoutManager(context));
                    crewRecyler.setAdapter(new CrewAdapter(context, expedition.getCrew()));
                }
            } else {
                crewRecyler.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            Expedition expedition = expeditionItem.getExpedition();
            boolean expanded = expeditionItem.isExpanded();
            // Change the state
            expeditionItem.setExpanded(!expanded);
            if (expeditionItem.isExpanded()) {
                if (expeditionItem.getExpedition().getCrew() == null) {
                    getCrew(expedition);
                    notifyItemChanged(position);

                } else {

                    crewRecyler.setVisibility(View.VISIBLE);
                    crewRecyler.setLayoutManager(new LinearLayoutManager(context));
                    crewRecyler.setAdapter(new CrewAdapter(context, expedition.getCrew()));
                    notifyItemChanged(position);
                }
            } else {
                crewRecyler.setVisibility(View.GONE);
                notifyItemChanged(position);
            }
        }

        private void getCrew(Expedition expedition) {
            DataClient.getInstance().getExpeditionById(expedition.getId(), new Callback<Expedition>() {
                @Override
                public void onResponse(Call<Expedition> call, Response<Expedition> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            expeditionItem.getExpedition().setCrew(response.body().getCrew());
                            crewRecyler.setVisibility(View.VISIBLE);
                            crewRecyler.setLayoutManager(new LinearLayoutManager(context));
                            crewRecyler.setAdapter(new CrewAdapter(context, expedition.getCrew()));
                            return;
                        }
                    }
                }

                @Override
                public void onFailure(Call<Expedition> call, Throwable t) {
                    crewRecyler.setVisibility(View.GONE);
                }
            });
        }
    }
}

