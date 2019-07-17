package com.alpha.museum.museum;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.lang.ref.WeakReference;

public class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = "ImageLoaderTask";
    private final WeakReference<VrPanoramaView> viewReference;
    private final VrPanoramaView.Options viewOptions;
    private static WeakReference<Bitmap> lastBitmap = new WeakReference<>(null);
    private static Bitmap lastBit;
    private final Bitmap bitmap;

    @Override
    protected Bitmap doInBackground(Void...voids) {

        if (bitmap == lastBit && lastBitmap.get() != null) {
            return lastBitmap.get();
        }
        try {
            lastBitmap = new WeakReference<>(bitmap);
            lastBit = bitmap;
            return bitmap;
        } catch (OutOfMemoryError ex) {
            Log.i(TAG, "Could not decode default bitmap: " + ex);
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

    public ImageLoaderTask(VrPanoramaView view, VrPanoramaView.Options viewOptions, Bitmap bitmap) {
        viewReference = new WeakReference<>(view);
        this.viewOptions = viewOptions;
        this.bitmap = bitmap;
    }
}
