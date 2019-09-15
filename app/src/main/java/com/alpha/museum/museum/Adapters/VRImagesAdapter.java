package com.alpha.museum.museum.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpha.museum.museum.R;
import com.alpha.museum.museum.VRActivity;
import com.alpha.museum.museum.models.Media;
import com.alpha.museum.museum.preference.ManagePreference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.alpha.museum.museum.MainActivity.TAG;

public class VRImagesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Media> mediaList;
    private ManagePreference managePreference;

    public VRImagesAdapter(Context context, ArrayList<Media> mediaList) {
        this.mContext = context;
        this.mediaList = mediaList;
        managePreference = new ManagePreference(context);
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Media media = mediaList.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = (LinearLayout) layoutInflater.inflate(R.layout.vr_image_layout, null);

            final ImageView imageView = (ImageView) convertView.findViewById(R.id.vr_image);
            Completable.fromAction(new Action() {
                @Override
                public void run() throws Exception {
                    try {
                        URL url = new URL(media.getVr().getImgPath());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        media.getVr().setImgBitmap(myBitmap);
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
                            imageView.setImageBitmap(media.getVr().getImgBitmap());
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    managePreference.shareStringData("vr_image_path", media.getVr().getImgPath());
                    Intent intent = new Intent(mContext, VRActivity.class);
                    intent.putExtra("vr_image", media);
                    mContext.startActivity(intent);
                }
            });
        }

        return convertView;
    }

}
