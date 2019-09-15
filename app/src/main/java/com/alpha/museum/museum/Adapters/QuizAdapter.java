package com.alpha.museum.museum.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.alpha.museum.museum.R;
import com.alpha.museum.museum.preference.ManagePreference;
import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

public class QuizAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> responses;
    private ManagePreference managePreference;

    public QuizAdapter(Context context, ArrayList<String> responses) {
        this.mContext = context;
        this.responses = responses;
        managePreference = new ManagePreference(context);
    }

    @Override
    public int getCount() {
        return responses.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final String response = responses.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = (LinearLayout) layoutInflater.inflate(R.layout.response_ui, null);

            FancyButton button = (FancyButton) convertView.findViewById(R.id.response_btn);
            button.setText(responses.get(position));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                    ViewGroup viewGroup = parent;

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.success_dialog, viewGroup, false);

                    FancyButton okBtn = (FancyButton) dialogView.findViewById(R.id.buttonOk);

                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(parent.getRootView().getContext());

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final AlertDialog alertDialog = builder.create();

                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });

                    alertDialog.show();
                }
            });
        }

        return convertView;
    }
}
