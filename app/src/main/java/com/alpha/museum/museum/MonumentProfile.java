package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.models.Monument;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.gc.materialdesign.views.ButtonFlat;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.ArrayList;

public class MonumentProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private int MONUMENT_ID = -1;
    private ManagePreference managePreference;
    private Monument monument;
    private ArrayList<Media> mediaList;

    UltraViewPager ultraViewPager;
    Lifecycle lifecycle;
    TextView name;
    TextView description;
    ImageButton fullScreen;

    int fullScreenAccess = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_profile);

        managePreference = new ManagePreference(getApplicationContext());
        MONUMENT_ID = managePreference.getSharedIntData("monument_id");
        monument = (Monument) getIntent().getSerializableExtra(String.format("monument_%d", MONUMENT_ID));
        mediaList = initMedia(monument);

        name = (TextView) findViewById(R.id.monument_profile_name);
        description = (TextView) findViewById(R.id.monument_description);
        fullScreen = (ImageButton) findViewById(R.id.monument_full_screen);

        Typeface light = Typeface.createFromAsset(getResources().getAssets(),"Font/Roboto/Roboto-Light.ttf");

        name.setTypeface(light);

        lifecycle = this.getLifecycle();

        name.setText(monument.getMonumentTitle());
        description.setText(monument.getMonumentDescription());

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

        ultraViewPager = (UltraViewPager) findViewById(R.id.monument_viewpager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);

        //initialize UltraPagerAdapter，and add child view to UltraViewPager
        PagerAdapter adapter = new UltraPagerAdapter(false, this, lifecycle, mediaList);
        ultraViewPager.setAdapter(adapter);

        //set an infinite loop
        ultraViewPager.setInfiniteLoop(true);
        //enable auto-scroll mode
        //ultraViewPager.setAutoScroll(2000);
    }

    public static ArrayList<Media> initMedia(Monument monument) {
        ArrayList<Media> mediaList = new ArrayList<>();
        for (int i = 0; i < monument.getVideos().size(); i++) {
            Media media = new Media(1, null, monument.getVideos().get(i));
            mediaList.add(media);
        }
        for (int i = 0; i < monument.getImages().size(); i++) {
            Media media = new Media(2, monument.getImages().get(i), null);
            mediaList.add(media);
        }
        return mediaList;
    }
}
