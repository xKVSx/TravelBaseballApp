package android.csulb.edu.travelbaseballapp.adapters;

import android.csulb.edu.travelbaseballapp.R;
import android.csulb.edu.travelbaseballapp.pojos.BaseballEvent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class MonthSectionedAdapter extends SectionedRecyclerViewAdapter{
    private List<MonthSection> mMonthSection = new ArrayList<>();
    final private returnEventId mReturnEventId;
    final private returnEventLocation mReturnLocation;

    public MonthSectionedAdapter(returnEventId listener, returnEventLocation locationListener){
        mReturnEventId = listener;
        mReturnLocation = locationListener;
    }

    public class MonthSection extends StatelessSection{
        String month;
        List<BaseballEvent> eventList;

        public MonthSection(String month, List<BaseballEvent> eventList) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.section_month_event)
                    .headerResourceId(R.layout.section_month_header)
                    .build());

            this.month = month;
            this.eventList = eventList;
            mMonthSection.add(this);
        }

        public void addEvent(BaseballEvent event){
            eventList.add(event);
            notifyDataSetChanged();
        }

        @Override
        public int getContentItemsTotal() {
            return eventList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            final BaseballEvent event = eventList.get(position);

            String abvDayofWeekString = event.getDayOfTheWeek().substring(0, 3).toUpperCase();
            String eventDayString = event.getDay() + "\n" + abvDayofWeekString;

            itemHolder.eventLocation.setText(event.getLocation());
            itemHolder.locationStart.setText(event.getDisplayTimeStart());
            itemHolder.locationName.setText(event.getSummary());
            itemHolder.eventDescription.setText(event.getDescription());
            itemHolder.eventDay.setText(eventDayString);

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int clickedPosition = getPositionInSection(itemHolder.getAdapterPosition());
                    mReturnLocation.returnLocationString(event.getLocation());
                }
            });

            itemHolder.changeEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    itemHolder.changeEvent.setSelection(0);
                    int clickedPosition = getPositionInSection(itemHolder.getAdapterPosition());
                    String eventId = eventList.get(clickedPosition).getDatabaseId();

                    switch (i){
                        case 0:
                            break;//do nothing
                        case 1:
                            eventList.remove(clickedPosition);
                            notifyDataSetChanged();
                            mReturnEventId.returnDeletedEventId(eventId);
                            break;
                        default:
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.monthView.setText(month);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView monthView;

        HeaderViewHolder(View view) {
            super(view);

             monthView = (TextView) view.findViewById(R.id.section_header);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView eventLocation;
        private final TextView locationStart;
        private final TextView locationName;
        private final TextView eventDay;
        private final TextView eventDescription;
        private final Spinner changeEvent;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            eventLocation = view.findViewById(R.id.section_location);
            locationStart = view.findViewById(R.id.event_startTime);
            locationName = view.findViewById(R.id.event_name);
            eventDay = view.findViewById(R.id.event_day);
            eventDescription = view.findViewById(R.id.event_description);
            changeEvent = view.findViewById(R.id.change_event);

            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(view.getContext(), R.array.update,
                    android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            changeEvent.setAdapter(spinnerAdapter);
        }
    }

    public MonthSection getMonthSection(int month) {
        return mMonthSection.get(month);
    }

    public List<MonthSection> getMonthSectionList(){
        return mMonthSection;
    }

    public interface returnEventId{
        void returnDeletedEventId(String deletedEventId);
    }

    public interface returnEventLocation{
        void returnLocationString(String location);
    }
}
