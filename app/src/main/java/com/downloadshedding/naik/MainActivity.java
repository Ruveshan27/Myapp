package com.downloadshedding.naik;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String URL = "url";
    public static final String SETTINGS = "settings";

    private String saved_url;
    private int saved_settings;



    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadData();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Button save_playlist = findViewById(R.id.savePlaylist);
        Button start_app = findViewById(R.id.startApp);


        Intent intent = new Intent(this, MyService.class);
        TextView countVids = findViewById(R.id.playlistCounter);
        TextView settings_TextView = findViewById(R.id.settings);
        TextView download_Settings = findViewById(R.id.download_Settings);
        RadioGroup downSettings = findViewById(R.id.radio_group);

        final int[] count = {2};
        Button save_Settings = findViewById(R.id.saveSettigs);

        save_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();

            }
        });



        settings_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (count[0] %2==0){
                    download_Settings.setVisibility(View.VISIBLE);
                    downSettings.setVisibility(View.VISIBLE);
                    save_Settings.setVisibility(View.VISIBLE);
                    settings_TextView.setText("Close Settings");


                    count[0]++;


                }else {
                    download_Settings.setVisibility(View.GONE);
                    downSettings.setVisibility(View.GONE);
                    save_Settings.setVisibility(View.GONE);
                    settings_TextView.setText("Settings");
                    count[0]++;

                }



            }

        });


        save_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText enter_playlist = findViewById(R.id.input);
                String user_url = enter_playlist.getText().toString().trim();
                if (user_url.isEmpty()==true){
                    Toast.makeText(MainActivity.this,"Enter valid URL",Toast.LENGTH_LONG).show();

                }else {
                    saveData();
                    countVids.setText("Videos currently in playlist: " + countVids());
                    start_app.setVisibility(View.VISIBLE);
                }

            }
        });
        start_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);

                } else {
                    startService(intent);

                }

            }
        });

    }

    public String countVids() {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("myscript");
        PyObject obj = pyobj.callAttr("countVids", "https://youtube.com/playlist?list=PLch6H7qJEpXan7X07XgS_E2DdlTQxxRYE");
        return obj.toString();
    }
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        EditText userLink = findViewById(R.id.input);
        editor.putString(URL,userLink.getText().toString());
        editor.apply();
        Toast.makeText(this,"New link has been saved",Toast.LENGTH_SHORT).show();
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        saved_url = sharedPreferences.getString(URL,"");
        saved_settings = sharedPreferences.getInt(SETTINGS,0);
        EditText userLink = findViewById(R.id.input);
        RadioGroup downSettings = findViewById(R.id.radio_group);
        userLink.setText(saved_url.toString());
        downSettings.check(downSettings.getChildAt(saved_settings).getId());



    }
    public void saveSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        RadioGroup downSettings = findViewById(R.id.radio_group);
        int index = downSettings.indexOfChild(findViewById(downSettings.getCheckedRadioButtonId()));
        editor.putInt(SETTINGS,index);
        editor.apply();
        Toast.makeText(this,"New settings saved",Toast.LENGTH_SHORT).show();

    }
}
