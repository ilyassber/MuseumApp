package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alpha.museum.museum.models.Category;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MonumentList extends AppCompatActivity {

    private List<Category> categories;
    private RequestQueue requestQueue;
    public static String TAG = "#tag#";
    ManagePreference managePreference;
    private int museumId;
    Category categoryAll;

    // Managing category list variables

    View lastClickedView = null;
    int clickedId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_list);

        managePreference = new ManagePreference(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);
        museumId = managePreference.getSharedIntData("museum_id");

        getAllCategories();

        // get our list view
        ListView theListView = (ListView) findViewById(R.id.mainListView);

        // prepare elements to display
        final ArrayList<Monument> items = Monument.getTestingList();

        // add custom btn handler to first list item
        items.get(0).setRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "CUSTOM HANDLER FOR FIRST BUTTON", Toast.LENGTH_SHORT).show();
            }
        });

        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)
        final FoldingCellListAdapter adapter = new FoldingCellListAdapter(this, items);

        // add default btn handler for each request btn on each item if custom handler not found
        adapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "DEFAULT HANDLER FOR ALL BUTTONS", Toast.LENGTH_SHORT).show();
            }
        });

        // set elements to adapter
        theListView.setAdapter(adapter);

        // set on click event listener to list view
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked cell state
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                adapter.registerToggle(pos);
            }
        });

    }

    private void getAllCategories() {
        Log.i(TAG, "1");
        try {
            Log.i(TAG, "2");
            String url = "http://165.22.16.186/api/museum/" + museumId + "/categories/all";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                List<Category> tmpCategories = Arrays.asList(gson.fromJson(response, Category[].class));
                                categories = new ArrayList<>();
                                categories.add(new Category(0, "All"));
                                categories.addAll(tmpCategories);
                                if (categories != null) {
                                    initCategories();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "Error : " + error);
                }
            });
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    private void initCategories() {
        //create LayoutInflator class
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = (LinearLayout) findViewById(R.id.category_container);
        int size = categories.size();
        for (int i = 0; i < size; i++) {
            final Category category = categories.get(i);
            if (category != null) {
                LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.category, null);
                LinearLayout categoryLayout = (LinearLayout) mainLayout.findViewById(R.id.category_layout);
                TextView categoryText = (TextView) mainLayout.findViewById(R.id.category_text);
                categoryText.setText(category.getCategoryName());
                if (i == 0) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(15, 3, 3, 3);
                    categoryLayout.setLayoutParams(params);
                    lastClickedView = categoryLayout;
                }
                else if (i == categories.size() - 1) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(3, 3, 15, 3);
                    categoryLayout.setLayoutParams(params);
                    LinearLayout clicked = v.findViewById(R.id.category_layout);
                    TextView clickedText = (TextView) lastClickedView.findViewById(R.id.category_text);
                    clicked.setBackground(getDrawable(R.drawable.box_shadow_dark));
                    clickedText.setTextColor(getResources().getColor(R.color.white));
                }
                mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lastClickedView != null) {
                            LinearLayout lastClickedLayout = (LinearLayout) lastClickedView.findViewById(R.id.category_layout);
                            TextView clickedText = (TextView) lastClickedView.findViewById(R.id.category_text);
                            lastClickedLayout.setBackground(getDrawable(R.drawable.box_shadow));
                            clickedText.setTextColor(getResources().getColor(R.color.darkGray));
                        }
                        lastClickedView = v;
                        Category cast = (Category) v.getTag();
                        LinearLayout clicked = v.findViewById(R.id.category_layout);
                        TextView clickedText = (TextView) lastClickedView.findViewById(R.id.category_text);
                        clicked.setBackground(getDrawable(R.drawable.box_shadow_dark));
                        clickedText.setTextColor(getResources().getColor(R.color.white));
                        Toast.makeText(getApplicationContext(), category.getCategoryName(), Toast.LENGTH_SHORT).show();
                        //Click able view for casts
                    }
                });
                ((LinearLayout) v).addView(mainLayout);
            }
        }
    }
}
