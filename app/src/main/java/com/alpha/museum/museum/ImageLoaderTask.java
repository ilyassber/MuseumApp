package com.alpha.museum.museum;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = "ImageLoaderTask";
    private final String url;
    private final WeakReference<VrPanoramaView> viewReference;
    private final VrPanoramaView.Options viewOptions;
    private static WeakReference<Bitmap> lastBitmap = new WeakReference<>(null);
    private static String lastUrl;

    @Override
    protected Bitmap doInBackground(Void...voids) {

        Bitmap bm = null;

        if (url.equals(lastUrl) && lastBitmap.get() != null) {
            return lastBitmap.get();
        }

        try {
            URL link = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) link.openConnection();
            if (conn.getResponseCode() != 200) {
                return bm;
            }
            conn.connect();
            InputStream is = conn.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is);
            try {
                bm = BitmapFactory.decodeStream(bis);
                lastBitmap = new WeakReference<>(bm);
                lastUrl = url;
                bis.close();
                is.close();
                return bm;
            } catch (OutOfMemoryError ex) {
                Log.i(TAG, "Could not decode default bitmap: " + ex);
                bis.close();
                is.close();
                return null;
            }
        } catch (Exception e) {
            Log.i(TAG, "doInBackground: Exception : " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        final VrPanoramaView vw = viewReference.get();
        if (vw != null && bitmap != null) {
            vw.loadImageFromBitmap(bitmap, viewOptions);
            vw.resumeRendering();
        }
    }

    public ImageLoaderTask(VrPanoramaView view, VrPanoramaView.Options viewOptions, String url) {
        viewReference = new WeakReference<>(view);
        this.viewOptions = viewOptions;
        this.url = url;
    }
}
