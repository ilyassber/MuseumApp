package com.alpha.museum.museum;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.alpha.museum.museum.preference.ManagePreference;
import com.ramotion.expandingcollection.ECCardContentListItemAdapter;
import com.ramotion.expandingcollection.ECCardData;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MuseumCardAdapter extends ECCardContentListItemAdapter<String> {

    private List<ECCardData> dataset;
    private Context context;

    public MuseumCardAdapter(Context context, List<String> objects, List<ECCardData> dataset) {
        super(context, R.layout.museum_list_item, objects);
        this.dataset = dataset;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int i, @Nullable View view, @NonNull ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View rowView = view;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.museum_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemText = (TextView) rowView.findViewById(R.id.list_item_text);
            viewHolder.locationText = (TextView) rowView.findViewById(R.id.location_text);
            viewHolder.desText = (TextView) rowView.findViewById(R.id.description_text);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        String item = getItem(i);
        if (item != null) {
            viewHolder.itemText.setText(dataset.get(i).getCardTitle());
            viewHolder.locationText.setText(dataset.get(i).getCountry() + ", " + dataset.get(i).getCity());
            viewHolder.desText.setText(dataset.get(i).getDescription());
        }
        return rowView;
    }

    static class ViewHolder {
        TextView        itemText;
        TextView        desText;
        TextView        locationText;
    }
}
