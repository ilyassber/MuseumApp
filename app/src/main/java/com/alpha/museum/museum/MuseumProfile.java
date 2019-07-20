package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import mehdi.sakout.fancybuttons.FancyButton;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.models.Monument;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.views.ButtonFlat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MuseumProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = "alpha_tag"; //CompoundButton.OnCheckedChangeListener,

    private int MUSEUM_ID = -1;
    private ManagePreference managePreference;
    private Museum museum;
    private ArrayList<Media> mediaList;
    private RequestQueue requestQueue;

    UltraViewPager ultraViewPager;
    FancyButton showRoomBtn;
    FancyButton gallery360;
    Lifecycle lifecycle;
    TextView name;
    TextView location;
    ImageButton locationBtn;
    TextView description;
    ImageButton fullScreen;

    int fullScreenAccess = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_profile);

        managePreference = new ManagePreference(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);
        MUSEUM_ID = managePreference.getSharedIntData("museum_id");
        if (MUSEUM_ID == -1)
            MUSEUM_ID = managePreference.getSharedIntData("museum_id_n");
        else
            managePreference.shareIntData("museum_id", -1);
        managePreference.shareIntData("museum_id_x", MUSEUM_ID);
        museum = (Museum) getIntent().getSerializableExtra(String.format("museum_%d", MUSEUM_ID));
        if (museum == null) {
            Log.i(TAG, "onCreate: Museum is NULL id = " + MUSEUM_ID);
            try {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://www.kdefaoui-camagru.tk/api/museum/" + MUSEUM_ID,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    Gson gson = new GsonBuilder().create();
                                    museum = gson.fromJson(response, Museum.class);
                                    init();
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
        else {
            init();
        }
    }

    void init() {
        name = (TextView) findViewById(R.id.museum_profile_name);
        location = (TextView) findViewById(R.id.museum_location);
        locationBtn = (ImageButton) findViewById(R.id.museum_location_gps);
        description = (TextView) findViewById(R.id.museum_description);
        fullScreen = (ImageButton) findViewById(R.id.full_screen);
        gallery360 = (FancyButton) findViewById(R.id.gallery_360);
        showRoomBtn = (FancyButton) findViewById(R.id.show_room_button);

        Typeface light = Typeface.createFromAsset(getResources().getAssets(), "Font/Roboto/Roboto-Light.ttf");
        final Typeface medium = Typeface.createFromAsset(getResources().getAssets(), "Font/Roboto/Roboto-Medium.ttf");

        name.setTypeface(light);
        location.setTypeface(medium);

        lifecycle = this.getLifecycle();

        mediaList = initMedia(museum);
        name.setText(museum.getCardTitle());
        location.setText(String.format("%s, %s", museum.getCountry(), museum.getCity()));
        description.setText(museum.getDescription());
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlAddress = "http://maps.google.com/maps?q=" + museum.getGpsLocation() + "(" + museum.getCardTitle() + ")&iwloc=A&hl=es";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                intent.setPackage("com.google.android.apps.maps");
                //if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                //   startActivity(intent);
                //}
                /*
                String uri = "https://goo.gl/maps/HYhG3i7h2SULCpHQ7";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                */
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        innerEx.printStackTrace();
                        //Toast.makeText(this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullScreenAccess == 0) {
                    fullScreen.setBackgroundResource(R.drawable.less_white);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    ultraViewPager.setLayoutParams(params);
                    ultraViewPager.initIndicator();
                    fullScreenAccess = 1;

                } else {
                    fullScreen.setBackgroundResource(R.drawable.full_white);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                    params.weight = 1;
                    ultraViewPager.setLayoutParams(params);
                    ultraViewPager.disableIndicator();
                    fullScreenAccess = 0;
                }
            }
        });

        defaultUltraViewPager(lifecycle);

        showRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumProfile.this, MonumentList.class);
                startActivity(intent);
            }
        });

        init_gallery360();
    }

    void init_gallery360 () {
        gallery360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MuseumProfile.this, Gallery360.class);
                intent.putExtra("media", mediaList);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (ultraViewPager.getIndicator() == null) {
            ultraViewPager.initIndicator();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        ultraViewPager.getIndicator().build();
    }

    private void defaultUltraViewPager(Lifecycle lifecycle) {

        ultraViewPager = (UltraViewPager) findViewById(R.id.ultra_viewpager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);

        //initialize UltraPagerAdapter，and add child view to UltraViewPager
        PagerAdapter adapter = new UltraPagerAdapter(false, this, lifecycle, mediaList);
        ultraViewPager.setAdapter(adapter);

        //set an infinite loop
        ultraViewPager.setInfiniteLoop(true);
        //enable auto-scroll mode
        //ultraViewPager.setAutoScroll(2000);
    }

    public static ArrayList<Media> initMedia(Museum museum) {
        ArrayList<Media> mediaList = new ArrayList<>();
        if (museum.getVideos() != null) {
            for (int i = 0; i < museum.getVideos().size(); i++) {
                Media media = new Media(1, null, museum.getVideos().get(i), null);
                mediaList.add(media);
            }
        }
        for (int i = 0; i < museum.getImages().size(); i++) {
            Image image = museum.getImages().get(i);
            if (image.getIs360() == 0) {
                Media media = new Media(2, museum.getImages().get(i), null, null);
                mediaList.add(media);
            } else {
                Media media = new Media(3, null, null, museum.getImages().get(i));
                mediaList.add(media);
            }
        }
        return mediaList;
    }
}