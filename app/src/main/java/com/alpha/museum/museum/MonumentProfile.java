package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.ArrayList;

public class MonumentProfile extends AppCompatActivity {

    private UltraViewPager ultraViewPager;
    private TextView museumName;
    private TextView museumLocation;
    private TextView museumDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_profile);
    }

    public static ArrayList<Media> initMedia(Museum museum) {
        ArrayList<Media> mediaList = new ArrayList<>();
        for (int i = 0; i < museum.getVideos().size(); i++) {
            Media media = new Media(1, null, museum.getVideos().get(i));
            mediaList.add(media);
        }
        for (int i = 0; i < museum.getImages().size(); i++) {
            Media media = new Media(2, museum.getImages().get(i), null);
            mediaList.add(media);
        }
        return mediaList;
    }
}
