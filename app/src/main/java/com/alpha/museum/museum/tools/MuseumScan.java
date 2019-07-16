package com.alpha.museum.museum.tools;

import android.util.Log;

import com.alpha.museum.museum.models.Museum;

import java.util.List;

import static com.alpha.museum.museum.MainActivity.TAG;

public class MuseumScan {

    public Museum isInRange(List<Museum> museums, double altitude, double longitude) {
        Museum museum = null;
        double def1 = 1;
        double def2 = 1;
        double mAltitude;
        double mLongitude;
        for (int i = 0; i < museums.size(); i++) {
            Museum mMuseum = museums.get(i);
            Log.i(TAG, "isInRange: My Location : " + altitude + "," + longitude);
            mAltitude = Double.parseDouble(mMuseum.getGpsLocation().split(",")[0]);
            mLongitude = Double.parseDouble(mMuseum.getGpsLocation().split(",")[1]);
            Log.i(TAG, "isInRange: Museum Location : " + mAltitude + "," + mLongitude);
            if ((Math.abs(mAltitude - altitude) * 1000) < def1 && (Math.abs(mLongitude - longitude) * 1000) < def2) {
                def1 = Math.abs(mAltitude - altitude) * 1000;
                def2 = Math.abs(mLongitude - longitude) * 1000;
                museum = mMuseum;
            }
        }
        return museum;
    }
}
