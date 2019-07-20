package com.alpha.museum.museum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Monument;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.alpha.museum.museum.qrcode.QrCodeActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ramotion.expandingcollection.ECBackgroundSwitcherView;
import com.ramotion.expandingcollection.ECCardData;
import com.ramotion.expandingcollection.ECPagerView;
import com.ramotion.expandingcollection.ECPagerViewAdapter;

import org.altbeacon.beacon.BeaconManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.app.NotificationCompat;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends Activity {

    public static final String CHANNEL_ID = "listeningChannel";

    public static String TAG = "alpha_tag";

    private ECPagerView ecPagerView;
    private LinearLayout progressLayer;
    private ImageButton qrScan;
    private List<ECCardData> dataset;
    Bitmap bitmap = null;
    Bitmap bitmap2 = null;
    public static List<Museum> museumsList;
    private RequestQueue requestQueue;
    private int pass = 0;

    private ManagePreference managePreference;
    private int permissions = 0;

    //beacons

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String BEACON_UUID = "00001111-0000-1111-0000-111100001111";
    private final int BEACON_MAJOR = 0;
    private BeaconManager beaconManager;

    //scanner

    private NotificationCompat.Builder mNotification;
    private Monument monument;
    private int serial = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        //startActivity(new Intent(MainActivity.this, QrCodeActivity.class));

        managePreference = new ManagePreference(getApplicationContext());
        permissions = managePreference.getSharedIntData("permissions");
        progressLayer = (LinearLayout) findViewById(R.id.progress_layer);
        qrScan = (ImageButton) findViewById(R.id.qr_scan);

        mNotification = new NotificationCompat.Builder(this, CHANNEL_ID);

        verifyBluetooth();

        if (permissions != 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android M Permission check
                if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    //builder.setTitle("This app needs location access");
                    //builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                    //builder.setPositiveButton(android.R.string.ok, null);
                    //builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    //@TargetApi(23)
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                    //@Override
                    //public void onDismiss(DialogInterface dialog) {

                    //}
                //}
                    //builder.show();
                }
            }
            managePreference.shareIntData("permissions", 1);
            Log.i(TAG, "onCreate: Permissions = " + permissions);
        }

        //beaconInit();

        // get All Museums from Server
        requestQueue = Volley.newRequestQueue(this);
        getAllMuseumsFromServer();

        qrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        });

        //Intent intent = new Intent(MainActivity.this, VRActivity.class);
        //startActivity(intent);
    }

    void openService() {
        if (!isServiceRunning(ListeningService.class.getName())) {
            Intent intent = new Intent(this, ListeningService.class);
            startService(intent);
        }
    }

    private void getAllMuseumsFromServer() {
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://www.kdefaoui-camagru.tk/api/museums/all",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                museumsList = Arrays.asList(gson.fromJson(response, Museum[].class));
                                if (permissions == 1) {
                                    openService();
                                }
                                loadAllImagesAsBitmap();
                            } catch (Exception e) {
                                Log.i(TAG, "Json PARSE EXception: " + e.getMessage());
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
            Log.i(TAG, "Exception getAllMuseumsFromServer() : " + e.getMessage());
        }
    }

    private void loadAllImagesAsBitmap() {
        for (int i = 0; i < museumsList.size(); i++) {
            final ArrayList<Image> imgList = museumsList.get(i).getImages();
            final Image img = imgList.get(0);
                pass--;
                /*
                ImageRequest ir = new ImageRequest(img.getImgPath(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            img.setImgBitmap(response);
                            pass++;
                        }
                    }, 0, 0, null, null);
                requestQueue.add(ir);
                */
                Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                try {
                                    URL url = new URL(img.getImgPath());
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                    img.setImgBitmap(myBitmap);
                                    pass++;
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
                                if (pass == 0)
                                    afterLoading();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel listeningChannel = new NotificationChannel(CHANNEL_ID, "inListening", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(listeningChannel);
        }
    }

    void afterLoading() {
        progressLayer.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Get pager from layout
        ecPagerView = (ECPagerView) findViewById(R.id.ec_pager_element);

        // Generate example dataset
        dataset = new ArrayList<>();
        dataset.addAll(museumsList);

        // Implement pager adapter and attach it to pager view
        ecPagerView.setPagerViewAdapter(new ECPagerViewAdapter(getApplicationContext(), dataset) {
            @Override
            public void instantiateCard(LayoutInflater inflaterService, ViewGroup head, ListView list, ECCardData data) {
                // Data object for current card
                final Museum museum = (Museum) data;
                MuseumCardAdapter.ViewHolder viewHolder;
                final ManagePreference managePreference = new ManagePreference(getApplicationContext());
                View rowView;

                // Set adapter and items to current card content list
                list.setAdapter(new MuseumCardAdapter(getApplicationContext(), museum.getListItems(), dataset));
                // Also some visual tuning can be done here
                list.setBackgroundColor(Color.WHITE);

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                rowView = inflater.inflate(R.layout.museum_list_item, null);
                viewHolder = new MuseumCardAdapter.ViewHolder();
                viewHolder.itemText = (TextView) rowView.findViewById(R.id.list_item_text);
                viewHolder.locationText = (TextView) rowView.findViewById(R.id.location_text);
                viewHolder.desText = (TextView) rowView.findViewById(R.id.description_text);
                viewHolder.itemText.setText(museum.getCardTitle());
                viewHolder.locationText.setText(museum.getCountry() + ", " + museum.getCity());
                viewHolder.desText.setText(museum.getDescription());
                rowView.setTag(viewHolder);

                // Here we can create elements for head view or inflate layout from xml using inflater service
                //TextView cardTitle = new TextView(getApplicationContext());
                //cardTitle.setText(cardData.getCardTitle());
                //cardTitle.setTextSize(COMPLEX_UNIT_DIP, 20);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);//FrameLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                head.addView(rowView, layoutParams);

                // Card toggling by click on head element
                head.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        //ecPagerView.toggle();
                        managePreference.shareIntData("museum_id", museum.getMuseumId());
                        Log.i(TAG, "museum id = " + Integer.toString(museum.getMuseumId()));
                        Intent intent = new Intent(MainActivity.this, MuseumProfile.class);
                        intent.putExtra(String.format("museum_%d", museum.getMuseumId()), museum);
                        startActivity(intent);
                    }
                });
            }
        });

        // Add background switcher to pager view
        ecPagerView.setBackgroundSwitcherView((ECBackgroundSwitcherView) findViewById(R.id.ec_bg_switcher_element));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    managePreference.shareIntData("permissions", 1);
                    openService();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (scanResult.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + scanResult.getContents(), Toast.LENGTH_LONG).show();
                serial = Integer.parseInt(scanResult.getContents());
                getMonument();
            }
        }
    }

    void getMonument() {
        Log.i(TAG, "onResponse: Get Monument ??");
        try {
            String url = "https://www.kdefaoui-camagru.tk/api/monument/" + serial;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                monument = gson.fromJson(response, Monument.class);
                                Log.i(TAG, "onResponse: Monument Loaded !!");
                                if (monument != null) {
                                    managePreference.shareIntData("monument_id", monument.getMonumentId());
                                    Intent intent = new Intent(MainActivity.this, MonumentProfile.class);
                                    intent.putExtra(String.format("monument_%d", monument.getMonumentId()), monument);
                                    startActivity(intent);
                                    //buildNotification();
                                    //Log.i(TAG, "onResponse: Notification Build !!");
                                }
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Invalid link !!", Toast.LENGTH_SHORT).show();
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
    }

    void buildNotification() {
        managePreference.shareIntData("monument_id", monument.getMonumentId());
        Intent intent = new Intent(this, MonumentProfile.class);
        intent.putExtra(String.format("monument_%d", monument.getMonumentId()), monument);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /*
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(monument.getMonumentTitle())
                .setContentText(monument.getMonumentDescription())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
                */

        mNotification.setSmallIcon(R.drawable.etil) // notification icon
                .setContentTitle(monument.getMonumentTitle()) // title for notification
                .setContentText(monument.getMonumentDescription()) // message for notification
                .setAutoCancel(true) // clear notification after click
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        mNotification.build();
    }
}
