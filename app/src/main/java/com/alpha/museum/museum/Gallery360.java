package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;

import com.alpha.museum.museum.models.Media;

import java.util.ArrayList;

public class Gallery360 extends AppCompatActivity {

    ArrayList<Media> media;
    ArrayList<Media> vrMedia;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery360);

        media = (ArrayList<Media>) getIntent().getExtras().get("media");
        vrMedia = new ArrayList<>();
        for(int i = 0; i < media.size(); i++) {
            if (media.get(i).getType() == 3) {
                vrMedia.add(media.get(i));
            }
        }

        gridView = (GridView) findViewById(R.id.gridview);

        VRImagesAdapter vrImagesAdapter = new VRImagesAdapter(this, vrMedia);
        gridView.setAdapter(vrImagesAdapter);
    }
}
