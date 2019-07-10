package com.alpha.museum.museum;

//import android.support.v7.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ramotion.expandingcollection.ECBackgroundSwitcherView;
import com.ramotion.expandingcollection.ECCardData;
import com.ramotion.expandingcollection.ECPagerView;
import com.ramotion.expandingcollection.ECPagerViewAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "listeningChannel";

    public static String TAG = "#tag#";

    private ECPagerView ecPagerView;
    private List<ECCardData> dataset;
    Bitmap bitmap = null;
    Bitmap bitmap2 = null;
    private List<Museum> museumsList;
    private RequestQueue requestQueue;
    private int pass = 0;

    /*
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (pass != 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            afterLoading();
        }
    });
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //createNotificationChannel();

        //Intent intent = new Intent(this, ListeningService.class);
        //startService(intent);

        // get All Museums from Server
        requestQueue = Volley.newRequestQueue(this);
        getAllMuseumsFromServer();
    }

    private void getAllMuseumsFromServer() {
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://165.22.16.186/api/museums/all",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                museumsList = Arrays.asList(gson.fromJson(response, Museum[].class));
                                loadAllImagesAsBitmap();
                            } catch (Exception e) {
                                Log.i(TAG, "Json PARSE EXception: " + e.getMessage());
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
            Log.i(TAG, "Exception getAllMuseumsFromServer() : " + e.getMessage());
        }
    }

    private void loadAllImagesAsBitmap() {
        for (int i = 0; i < museumsList.size(); i++) {
            final ArrayList<Image> imgList = museumsList.get(i).getImages();
            for (int j = 0; j < imgList.size(); j++) {
                final Image img = imgList.get(j);
                pass--;
                /*
                ImageRequest ir = new ImageRequest(img.getImgPath(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            img.setImgBitmap(response);
                            pass++;
                        }
                    }, 0, 0, null, null);
                requestQueue.add(ir);
                */
                Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                try {
                                    URL url = new URL(img.getImgPath());
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                    img.setImgBitmap(myBitmap);
                                    pass++;
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
                                if (pass == 0)
                                    afterLoading();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel listeningChannel = new NotificationChannel(CHANNEL_ID, "inListening", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(listeningChannel);
        }
    }

    void afterLoading() {
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Get pager from layout
        ecPagerView = (ECPagerView) findViewById(R.id.ec_pager_element);

        // Generate example dataset
        dataset = new ArrayList<>();
        dataset.addAll(museumsList);

        // Implement pager adapter and attach it to pager view
        ecPagerView.setPagerViewAdapter(new ECPagerViewAdapter(getApplicationContext(), dataset) {
            @Override
            public void instantiateCard(LayoutInflater inflaterService, ViewGroup head, ListView list, ECCardData data) {
                // Data object for current card
                final Museum museum = (Museum) data;
                MuseumCardAdapter.ViewHolder viewHolder;
                final ManagePreference managePreference = new ManagePreference(getApplicationContext());
                View rowView;

                // Set adapter and items to current card content list
                list.setAdapter(new MuseumCardAdapter(getApplicationContext(), museum.getListItems(), dataset));
                // Also some visual tuning can be done here
                list.setBackgroundColor(Color.WHITE);

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                rowView = inflater.inflate(R.layout.museum_list_item, null);
                viewHolder = new MuseumCardAdapter.ViewHolder();
                viewHolder.itemText = (TextView) rowView.findViewById(R.id.list_item_text);
                viewHolder.locationText = (TextView) rowView.findViewById(R.id.location_text);
                viewHolder.desText = (TextView) rowView.findViewById(R.id.description_text);
                viewHolder.itemText.setText(museum.getCardTitle());
                viewHolder.locationText.setText(museum.getCountry() + ", " + museum.getCity());
                viewHolder.desText.setText(museum.getDescription());
                rowView.setTag(viewHolder);

                // Here we can create elements for head view or inflate layout from xml using inflater service
                //TextView cardTitle = new TextView(getApplicationContext());
                //cardTitle.setText(cardData.getCardTitle());
                //cardTitle.setTextSize(COMPLEX_UNIT_DIP, 20);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);//FrameLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                head.addView(rowView, layoutParams);

                // Card toggling by click on head element
                head.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        //ecPagerView.toggle();
                        managePreference.shareIntData("museum_id", museum.getMuseumId());
                        Log.i(TAG, "museum id = " + Integer.toString(museum.getMuseumId()));
                        Intent intent = new Intent(MainActivity.this, MuseumProfile.class);
                        intent.putExtra("museum", museum);
                        startActivity(intent);
                    }
                });
            }
        });

        // Add background switcher to pager view
        ecPagerView.setBackgroundSwitcherView((ECBackgroundSwitcherView) findViewById(R.id.ec_bg_switcher_element));
    }
}
