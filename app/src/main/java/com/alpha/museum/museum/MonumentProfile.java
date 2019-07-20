package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmall.ultraviewpager.UltraViewPager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.alpha.museum.museum.MainActivity.TAG;

public class MonumentProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String TAG = "alpha_tag";
    private int MONUMENT_ID = -1;
    private ManagePreference managePreference;
    private Monument monument;
    private ArrayList<Media> mediaList;
    private RequestQueue requestQueue;
    private boolean playPause = false;
    private MediaPlayer mediaPlayer;
    private boolean intialStage = true;

    UltraViewPager ultraViewPager;
    Lifecycle lifecycle;
    TextView name;
    TextView description;
    ImageButton fullScreen;
    ImageButton playPauseBtn;
    ImageButton stopBtn;
    FancyButton gallery360;
    FancyButton shareBtn;
    ShareButton fbShareBtn;

    int fullScreenAccess = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_monument_profile);

        managePreference = new ManagePreference(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        MONUMENT_ID = managePreference.getSharedIntData("monument_id");

        name = (TextView) findViewById(R.id.monument_profile_name);
        description = (TextView) findViewById(R.id.monument_description);
        fullScreen = (ImageButton) findViewById(R.id.monument_full_screen);
        playPauseBtn = (ImageButton) findViewById(R.id.play_pause);
        stopBtn = (ImageButton) findViewById(R.id.stop);
        gallery360 = (FancyButton) findViewById(R.id.gallery_360);
        shareBtn = (FancyButton) findViewById(R.id.share);
        fbShareBtn = (ShareButton) findViewById(R.id.facebook_share);

        Typeface light = Typeface.createFromAsset(getResources().getAssets(),"Font/Roboto/Roboto-Light.ttf");

        name.setTypeface(light);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new Player().execute("https://kozco.com/tech/LRMonoPhase4.wav");

        lifecycle = this.getLifecycle();

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

        monument = (Monument) getIntent().getExtras().get(String.format("monument_%d", MONUMENT_ID));
        if (monument == null) {
            Log.i(TAG, "onCreate: Monument is NULL");
            try {
                String url = "https://www.kdefaoui-camagru.tk/api/monument/" + MONUMENT_ID;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    Gson gson = new GsonBuilder().create();
                                    monument = gson.fromJson(response, Monument.class);
                                    Log.i(TAG, "onResponse: Monument Loaded !!");
                                    if (monument != null) {
                                        mediaList = initMedia(monument);
                                        defaultUltraViewPager(lifecycle);
                                        name.setText(monument.getMonumentTitle());
                                        description.setText(monument.getMonumentDescription());
                                        init_gallery360();
                                    }
                                    Log.i(TAG, "onResponse: Notification Build !!");
                                } catch (Exception e) {
                                    Log.i(TAG, "Exception : " + e);
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
        } else {
            mediaList = initMedia(monument);
            defaultUltraViewPager(lifecycle);
            name.setText(monument.getMonumentTitle());
            description.setText(monument.getMonumentDescription());
            init_gallery360();
        }

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playPause) {
                    if (!intialStage) {
                        playPauseBtn.setBackgroundResource(R.drawable.pause_brown);
                        if (!mediaPlayer.isPlaying())
                            mediaPlayer.start();
                        playPause = true;
                    }
                } else {
                    playPauseBtn.setBackgroundResource(R.drawable.play_brown);
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    playPause = false;
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                playPause = false;
                playPauseBtn.setBackgroundResource(R.drawable.play_brown);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        facebookShare();

        //mediaList = initMedia(monument);
        //defaultUltraViewPager(lifecycle);
    }

    void init_gallery360 () {
        gallery360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonumentProfile.this, Gallery360.class);
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
            Media media = new Media(1, null, monument.getVideos().get(i), null);
            mediaList.add(media);
        }
        for (int i = 0; i < monument.getImages().size(); i++) {
            Image image = monument.getImages().get(i);
            if (image.getIs360() == 0) {
                Media media = new Media(2, monument.getImages().get(i), null, null);
                mediaList.add(media);
            } else {
                Media media = new Media(3, null, null, monument.getImages().get(i));
                mediaList.add(media);
            }
        }
        return mediaList;
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            Boolean prepared;
            try {

                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        intialStage = true;
                        playPause = false;
                        playPauseBtn.setBackgroundResource(R.drawable.play_brown);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegarArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("Prepared", "//" + result);
            intialStage = false;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
//        if (mediaPlayer != null) {
//            mediaPlayer.reset();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
    }

    void share() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                try {
                    URL url = new URL(monument.getImages().get(0).getImgPath());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    monument.getImages().get(0).setImgBitmap(myBitmap);
                    Log.e(TAG, " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Bitmap is Loaded !");
                } catch (IOException e) {
                    Log.e(TAG, "Error on loading image !!! >>>>>>>>>>>>>>>>>>>>");
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
                        Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> On Complete");
                        Intent share = new Intent(Intent.ACTION_SEND);
                        //share.setType("image/jpeg");
                        share.setType("text/plain");
                        //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        //monument.getImages().get(0).getImgBitmap().compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        //File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                        //try {
                        //    f.createNewFile();
                        ///    FileOutputStream fo = new FileOutputStream(f);
                        //    fo.write(bytes.toByteArray());
                        //} catch (IOException e) {
                        //    e.printStackTrace();
                        //}
                        //Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> On Complete");
                        //share.putExtra(Intent.EXTRA_TITLE, monument.getMonumentTitle());
                        share.putExtra(Intent.EXTRA_TEXT, monument.getMonumentDescription());
                        //share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg"));
                        startActivity(Intent.createChooser(share, "Share Monument"));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    void facebookShare() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();
        fbShareBtn.setShareContent(content);
    }
}
