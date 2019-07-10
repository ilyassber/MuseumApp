package com.alpha.museum.museum.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class ManagePreference {


    Context context;

    public ManagePreference(Context context) {
        this.context = context;
    }

    public void shareStringData(String id, String data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, data);
        editor.apply();
    }

    public void shareIntData(String id, int data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(id, data);
        editor.apply();
    }

    public String getSharedStringData (String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        return sharedPreferences.getString(id, null);
    }

    public Integer getSharedIntData (String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(id, 0);
    }
}
