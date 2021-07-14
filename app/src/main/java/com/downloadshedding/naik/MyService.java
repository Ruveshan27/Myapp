package com.downloadshedding.naik;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;


public class MyService extends Service{
    Context context;
    private Handler mHandler = new Handler();
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String URL = "url";
    public static final String SETTINGS = "settings";

    private String saved_url;
    private int saved_settings;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(runnable,0);
        createNotificationChannel();

        Intent intent1 = new Intent(this,MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent1,0);

        Notification notification = new NotificationCompat.Builder(this,"ChannelID1")
                .setContentTitle("Downloadshedding")
                .setContentText("Is Running")
                .setContentIntent(pendingIntent).build();


        startForeground(1,notification);

        return START_STICKY;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "ChannelID1","Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {

                if (! Python.isStarted()) {
                    Python.start(new AndroidPlatform(MyService.this));
                }

                Python py = Python.getInstance();
                PyObject pyobj = py.getModule("myscript");
                PyObject obj = pyobj.callAttr("main","https://youtube.com/playlist?list=PLch6H7qJEpXan7X07XgS_E2DdlTQxxRYE");
                PyObject obj2 = pyobj.callAttr("isLoadshedding");
                int count = 0;
                //checking loadshedding
                System.out.println(obj2.toString()+"hhh");

                String[] links = obj.toString().split(",");
                int linkLen = links.length;
                links[0] = links[0].replace("[", "");
                links[linkLen - 1] = links[linkLen - 1].replace("]", "");
                System.out.println("Hello");


                for (int i = 0; i < links.length; i++) {
                    System.out.println(links[i].replaceAll("'|'", "").trim());
                    if (links[i].contains("http"))
                        YTDownload(18,links[i].replaceAll("'|'", "").trim());
                    else {

                    }
                }
            }catch (Exception e){

            }
            mHandler.postDelayed(this,60000);
        }

    };

    public void YTDownload(final int itag,String youTubeURL) {
        String VideoURLDownload = youTubeURL;
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        saved_url = sharedPreferences.getString(URL,"");
        saved_settings = sharedPreferences.getInt(SETTINGS,0);
        @SuppressLint("StaticFieldLeak") YouTubeUriExtractor youTubeUriExtractor = new YouTubeUriExtractor(this) {
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                if ((ytFiles != null)) {
                    String downloadURL = ytFiles.get(itag).getUrl();
                    Log.e("Download URL: ", downloadURL);

                    if (downloadURL != null) {
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadURL));
                        request.setTitle(videoTitle);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoTitle + ".mp4");

                        switch (saved_settings) {
                            case 0:
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                System.out.println("0");
                            case 1:
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
                                System.out.println("1");
                            case 3:
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
                                System.out.println("2");
                        }

                        File applictionFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + videoTitle + ".mp4");

                        if (applictionFile.exists()) {
                        } else
                        if (downloadManager != null) {
                            downloadManager.enqueue(request);
                        }
                    }
                }
            }
        };
        youTubeUriExtractor.execute(VideoURLDownload);
    }
}
