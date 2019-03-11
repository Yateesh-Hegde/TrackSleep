package com.y.sleeptracker;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.y.sleeptracker.database.DatabaseHelper;
import com.y.sleeptracker.database.Event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShowResult.CallBack {

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_BOOT_COMPLETED =0 ;
    private static final int MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 0;
    private static final int READ_REQUEST_CODE = 1 ;
    private static final int WRITE_REQUEST_CODE = 1;
    public List<Event> eventList;
    private Intent mServiceIntent;
    ListView listView;
    ListAdapter listAdapter;
    HashMap<String,Float> sleepCount = null;
    List <String> list= new ArrayList<>();

/*@Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.more_options, menu);

    // return true so that the menu pop up is opened
    return true;
}*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        starSleepTracking();

        Button calculateSleep = findViewById(R.id.fetchDta);
        calculateSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sleepCount = new HashMap<>();
                ShowResult showResult = new ShowResult(MainActivity.this);
                showResult.setCallBack(new ShowResult.CallBack() {
                    @Override
                    public void callBack() {
                        if (list.size() >3)
                        {
                            long yourmilliseconds = System.currentTimeMillis();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:mm:dd");
                            String today = sdf.format(new Date(yourmilliseconds));
                            final Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date(yourmilliseconds));
                            cal.add(Calendar.DATE, -1);
                            String today_1=sdf.format(cal.getTime());
                            cal.add(Calendar.DATE,-1);
                            String today_2 = sdf.format(cal.getTime());
                            List<String> tempList = new ArrayList<>();
                            for (String s:list)
                            {
                                if (s.contains(today.split(":")[2]) ||
                                        s.contains(today_1.split(":")[2]) ||s.contains(today_2.split(":")[2]))
                                {
                                    tempList.add(s);

                                }
                            }
                            list = tempList;
                        }
                        listView.setAdapter(new ListAdapter(MainActivity.this,R.layout.list_item,list));
                    }
                });
                showResult.execute(MainActivity.this.getApplicationContext());
                listView = findViewById(R.id.listView);

            }
        });
        

        Button fab = findViewById(R.id.startService);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceHelp.getValueFromPref("autoStart",MainActivity.this) == null) {
                    PreferenceHelp.putValueToPref("autoStart","asked",MainActivity.this);
                    addAutoStartup();
                }
                requestDisableBatteryOptimization(MainActivity.this.getApplicationContext());
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        MY_PERMISSIONS_REQUEST_RECEIVE_BOOT_COMPLETED);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE);
                Snackbar.make(view, "Tracking Started", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mServiceIntent = new Intent(MainActivity.this, SleepTracker.class);
                if (!isMyServiceRunning(SleepTracker.class)) {
                    sendBroadcast(new Intent(MainActivity.this,Restarter.class));
                    startService(mServiceIntent);
                }
            }
        });
    }

    private void requestDisableBatteryOptimization(final Context context) {
        try {
            final Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!pm.isIgnoringBatteryOptimizations(packageName)){
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Battery Optimization");
                    builder.setMessage("Please provide permission to ignore battery optimization for the app to function properly ")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }catch (Exception e)
                                    {
                                        //ignore
                                    }

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        }catch (Exception e)
        {
            //Ignore
        }
    }

    private void addAutoStartup() {

        try {
            final Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("AutoStart Permission");
                builder.setMessage("Please provide autoStart permission in next screen for the app to function Properly")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (Exception e) {
            Log.e("exc" , String.valueOf(e));
        }
    }

    private void starSleepTracking() {
    String[] permissions = {Manifest.permission.RECEIVE_BOOT_COMPLETED,Manifest.permission.FOREGROUND_SERVICE};
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions,
                MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE);
        mServiceIntent = new Intent(MainActivity.this, SleepTracker.class);
        if (!isMyServiceRunning(SleepTracker.class)) {
            sendBroadcast(new Intent(MainActivity.this, Restarter.class));
            startService(mServiceIntent);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.fetchFromFile:
                fetChFromFile();
                break;
            case R.id.writeToFile:
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(permissions, WRITE_REQUEST_CODE);
                }
                writeToFile();
                break;
        }
        return true;
    }*/

    private void writeToFile() {

    new WritetoFile(this).execute(this.getApplicationContext());

    }

    private void fetChFromFile() {

            FileInputStream is = null;
            BufferedReader reader;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MainActivity.this.requestPermissions(permissions, READ_REQUEST_CODE);
            }
            final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                    "/download/"+"myEvents.txt");

            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                while(line != null){
                    String[] strings = line.split(" ::: ");
                    databaseHelper.insertEvent(strings[0],strings[1]);
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        super.onDestroy();
    }

    @Override
    public void callBack() {
        listView.setAdapter(new ListAdapter(this,R.layout.list_item,list));
    }

    public static class PreferenceHelp{
    static String MY_PREFS_NAME ="pref";

    public static void putValueToPref(String key,String value,Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
    public static String getValueFromPref(String key,Context context){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(key, null);
        return restoredText;
    }


    }

    public class ListAdapter extends ArrayAdapter<String> {

        private int resourceLayout;
        private Context mContext;

        public ListAdapter(Context context, int resource, List<String> items) {
            super(context,resource,items);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            String day = getItem(position);

            Float value = sleepCount.get(day);


            if (day != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.day);
                TextView tt2 = (TextView) v.findViewById(R.id.sleepTime);

                if (tt1 != null) {
                    tt1.setText("Date "+day);

                }

                if (tt2 != null) {
                    tt2.setText(String.valueOf(value - 0.3333333));
                }


            }

            return v;
        }

    }

}
