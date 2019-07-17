package com.alpha.museum.museum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.tools.StringFormat;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.alpha.museum.museum.MainActivity.TAG;

/**
 * Created by mikeafc on 15/11/26.
 */
public class UltraPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    private Context context;
    private Lifecycle lifecycle;
    private ArrayList<Media> mediaList;
    final StringFormat stringFormat = new StringFormat();

    private ImageLoaderTask backgroundImageLoaderTask;

    public UltraPagerAdapter(boolean isMultiScr, Context context, Lifecycle lifecycle, ArrayList<Media> mediaList) {
        this.isMultiScr = isMultiScr;
        this.context = context;
        this.lifecycle = lifecycle;
        this.mediaList = mediaList;
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout linearLayout;
        final Media media = mediaList.get(position);
        if (media.getType() == 2) {
            linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null);
            final ImageView imageView = (ImageView) linearLayout.findViewById(R.id.viewer_image);
            Completable.fromAction(new Action() {
                @Override
                public void run() throws Exception {
                    try {
                        URL url = new URL(media.getImage().getImgPath());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        media.getImage().setImgBitmap(myBitmap);
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
                            imageView.setImageBitmap(media.getImage().getImgBitmap());
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
            imageView.setImageBitmap(media.getImage().getImgBitmap());
            linearLayout.setId(R.id.item_id);
        }
        else if (media.getType() == 1) {
            linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.video_layout, null);
            YouTubePlayerView youTubePlayerView = linearLayout.findViewById(R.id.youtube_player_view);
            lifecycle.addObserver(youTubePlayerView);

            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    String videoId = stringFormat.getYoutubeId(media.getVideo().getVideoUrl());
                    Log.i(TAG, "onReady: " + videoId);
                    youTubePlayer.loadVideo(videoId, 0);
                    youTubePlayer.pause();
                    youTubePlayer.setVolume(100);
                }
            });
        }
        /*
        else if (media.getType() == 2 && media.getImage().getIs360() == 1) {
            linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.activity_vr, null);
            VrPanoramaView panoWidgetView = linearLayout.findViewById(R.id.vr_view);
            ImageLoaderTask task = backgroundImageLoaderTask;
            if (task != null && !task.isCancelled()) {
                // Cancel any task from a previous loading.
                task.cancel(true);
            }

            // pass in the name of the image to load from assets.
            VrPanoramaView.Options viewOptions = new VrPanoramaView.Options();
            viewOptions.inputType = VrPanoramaView.Options.TYPE_MONO;

            // create the task passing the widget view and call execute to start.
            task = new ImageLoaderTask(panoWidgetView, viewOptions, media.getImage().getImgPath());
            task.execute();
            backgroundImageLoaderTask = task;
        }
        */
        else {
            linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null);
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.viewer_image);
            imageView.setImageDrawable(context.getDrawable(R.drawable.dark_layer));
        }

        container.addView(linearLayout);
//        linearLayout.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, container.getContext().getResources().getDisplayMetrics());
//        linearLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, container.getContext().getResources().getDisplayMetrics());
        return linearLayout;
    }

    @Nullable
    @Override
    public Parcelable saveState() {
        return super.saveState();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        LinearLayout view = (LinearLayout) object;
        container.removeView(view);
    }
}
