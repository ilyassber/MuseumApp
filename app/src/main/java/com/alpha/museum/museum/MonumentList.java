package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alpha.museum.museum.models.Category;
import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Monument;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

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
    int clickedItem = -1;
    private int pass = 0;

    List<Monument> monuments;
    ListView theListView;

    private FoldingCellListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_list);

        managePreference = new ManagePreference(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);
        museumId = managePreference.getSharedIntData("museum_id");
        monuments = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.mainListView);
        adapter = new FoldingCellListAdapter(getApplicationContext(), monuments);

        getAllCategories();
    }

    private void getAllMonument() {
        try {
            String url = "https://www.kdefaoui-camagru.tk/api/monuments/museum/" + museumId + "/category/0";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                monuments = Arrays.asList(gson.fromJson(response, Monument[].class));
                                for (int i = 0; i < monuments.size(); i++) {
                                    pass--;
                                    final Image image = monuments.get(i).getImages().get(0);
                                    Completable.fromAction(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            try {
                                                URL url = new URL(image.getImgPath());
                                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                                connection.setDoInput(true);
                                                connection.connect();
                                                InputStream input = connection.getInputStream();
                                                Bitmap bitmap = BitmapFactory.decodeStream(input);
                                                image.setImgBitmap(bitmap);
                                            } catch (IOException e) {
                                                Log.i(TAG, "Error on loading image !!!");
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(Disposable d) {
                                                }

                                                @Override
                                                public void onComplete() {
                                                    pass++;
                                                    Log.i(TAG, "Image Loaded !!!");
                                                    if (pass == 0) {
                                                        setListAdapter();
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                }
                                            });
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

    void setListAdapter () {
        if (monuments != null) {
            final List<Monument> mMonuments = new ArrayList<>();
            mMonuments.addAll(monuments);
            for (int i = 0; i < mMonuments.size(); i++) {
                Monument monument = mMonuments.get(i);
                if (checkCategory(monument.getCategories()) == 0) {
                    mMonuments.remove(i);
                    i = 0;
                }
            }

            adapter.clear();
            adapter.addAll(mMonuments);
            adapter.notifyDataSetChanged();
            theListView.setAdapter(adapter);

            // set on click event listener to list view
            theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    clickedItem = pos;
                    final int position = pos;
                    TextView textView = view.findViewById(R.id.content_request_btn);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            managePreference.shareIntData("monument_id", mMonuments.get(position).getMonumentId());
                            Intent intent = new Intent(MonumentList.this, MonumentProfile.class);
                            intent.putExtra(String.format("monument_%d", mMonuments.get(position).getMonumentId()), mMonuments.get(position));
                            startActivity(intent);
                        }
                    });
                    // toggle clicked cell state
                    ((FoldingCell) view).toggle(false);
                    // register in adapter that state for selected cell is toggled
                    adapter.registerToggle(pos);
                }
            });
        }
    }

    int checkCategory(ArrayList<Category> categories) {
        if (clickedId == 0)
            return (1);
        for (int i = 0; i < categories.size() ; i++) {
            Category mCategory = categories.get(i);
            if (mCategory.getCategoryId() == clickedId) {
                return (1);
            }
        }
        return (0);
    }

    private void getAllCategories() {
        try {
            String url = "https://www.kdefaoui-camagru.tk/api/museum/" + museumId + "/categories/all";
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
                                    getAllMonument();
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
                        clickedId = category.getCategoryId();
                        setListAdapter();
                        //Toast.makeText(getApplicationContext(), category.getCategoryName(), Toast.LENGTH_SHORT).show();
                        //Click able view for casts
                    }
                });
                ((LinearLayout) v).addView(mainLayout);
            }
        }
    }
}
