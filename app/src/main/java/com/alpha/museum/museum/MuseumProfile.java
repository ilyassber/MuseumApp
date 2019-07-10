package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.PrecomputedText;
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

import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.gc.materialdesign.views.ButtonFlat;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.ArrayList;
import java.util.Locale;

public class MuseumProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener { //CompoundButton.OnCheckedChangeListener,

    private int MUSEUM_ID = -1;
    private ManagePreference managePreference;
    private Museum museum;
    private ArrayList<Media> mediaList;

    UltraViewPager ultraViewPager;
    ButtonFlat showRoomBtn;
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
        MUSEUM_ID = managePreference.getSharedIntData("museum_id");
        museum = (Museum) getIntent().getSerializableExtra("museum");
        mediaList = MonumentProfile.initMedia(museum);

        name = (TextView) findViewById(R.id.museum_profile_name);
        location = (TextView) findViewById(R.id.museum_location);
        locationBtn = (ImageButton) findViewById(R.id.museum_location_gps);
        description = (TextView) findViewById(R.id.museum_description);
        fullScreen = (ImageButton) findViewById(R.id.full_screen);

        Typeface light = Typeface.createFromAsset(getResources().getAssets(),"Font/Roboto/Roboto-Light.ttf");
        Typeface medium = Typeface.createFromAsset(getResources().getAssets(),"Font/Roboto/Roboto-Medium.ttf");

        name.setTypeface(light);
        location.setTypeface(medium);

        lifecycle = this.getLifecycle();
        showRoomBtn = (ButtonFlat) findViewById(R.id.show_room_button);

        name.setText(museum.getCardTitle());
        location.setText(String.format("%s, %s", museum.getCountry(), museum.getCity()));
        description.setText(museum.getDescription());
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "https://goo.gl/maps/HYhG3i7h2SULCpHQ7";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try
                {
                    startActivity(intent);
                }
                catch(ActivityNotFoundException ex)
                {
                    try
                    {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    }
                    catch(ActivityNotFoundException innerEx)
                    {
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
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    ultraViewPager.setLayoutParams(params);
                    ultraViewPager.initIndicator();
                    fullScreenAccess = 1;
                } else {
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
}