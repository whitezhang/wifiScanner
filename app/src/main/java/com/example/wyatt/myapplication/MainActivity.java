package com.example.wyatt.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private NetworkScanner wifiScanner;
    private WifiManager wifiManager;
    private ListView wifiListView;
    private TextView infoTextView;

    private ScanResultsAdapter scanResultsAdapter;
    private List<ScanResult> scanResult;

    private Thread wifiThread;
    private volatile boolean isScanning;
    private int wifiResultsCounter = 0;

    private ArrayList<List<ScanResult>> wifiList;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.STARTSCAN:
                    wifiList.add(scanResult);

                    wifiResultsCounter++;
                    infoTextView = infoTextView == null ? (TextView) findViewById(R.id.tv_counter) : infoTextView;
                    infoTextView.setText("Wifi Counter:" + String.valueOf(wifiResultsCounter) + " (Scanning)");
                    if (wifiResultsCounter == Config.WIFINUMBER) {
                        stopThread();
                        infoTextView.setText("Wifi Counter:" + String.valueOf(wifiResultsCounter) + " (Finished)");
//                        Snackbar.make(findViewById(R.id.fab_ss).getRootView(), "Scan finished. Number of wifi:" + String.valueOf(wifiResultsCounter), Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
                    }
                    scanResult = wifiScanner.scanNetworks();
                    scanResultsAdapter = new ScanResultsAdapter(MainActivity.this, scanResult);
                    wifiListView.setAdapter(scanResultsAdapter);
                    break;
                case Config.STOPSCAN:
                    stopThread();
                    infoTextView = infoTextView == null ? (TextView) findViewById(R.id.tv_counter) : infoTextView;
                    infoTextView.setText("Wifi Counter:" + String.valueOf(wifiResultsCounter) + " (Stopped)");
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void init() {
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiScanner = new NetworkScanner(wifiManager);

        wifiList = new ArrayList<>();
    }

    public void stopThread() {
        isScanning = false;
        if (wifiThread != null) {
            wifiThread.interrupt();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.fab_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("Save Records")
                        .setView(et)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String apName = et.getText().toString();

                                try {
                                    Calendar calendar = Calendar.getInstance();
                                    String year = String.valueOf(calendar.get(Calendar.YEAR));
                                    String month = String.valueOf(calendar.get(Calendar.MONTH));
                                    String day = String.valueOf(calendar.get(Calendar.DATE));
                                    String foutName = String.format("%s%s%s_%s", year, month, day, apName);

                                    FileOutputStream fout = openFileOutput(foutName, MODE_APPEND);

                                    for(List<ScanResult> results: wifiList) {
                                        for(ScanResult r: results) {
                                            String info = String.format("%s;%s;%s|", r.BSSID, r.SSID, r.level);
                                            fout.write(info.getBytes());
                                        }
                                        fout.write("".getBytes());
                                    }
                                    fout.flush();
                                    fout.close();

                                    Snackbar.make(findViewById(R.id.fab_ss).getRootView(), "The data has been saved in " + foutName, Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                } catch (IOException e) {
                                    Log.e("IOException", "IOException");
                                }
                            }
                        })
                        .setNegativeButton("Cancle", null).show();
            }
        });

        FloatingActionButton seeButton = (FloatingActionButton) findViewById(R.id.fab_see);
        seeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoTextView = infoTextView == null ? (TextView) findViewById(R.id.tv_counter) : infoTextView;
                scanResult = wifiScanner.scanNetworks();
                scanResultsAdapter = new ScanResultsAdapter(MainActivity.this, scanResult);
                wifiListView.setAdapter(scanResultsAdapter);
            }
        });

        FloatingActionButton scanButton = (FloatingActionButton) findViewById(R.id.fab_ss);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) {
//                    Snackbar.make(view, "Stop scan", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    Message msg = new Message();
                    msg.what = Config.STOPSCAN;
                    mHandler.sendMessage(msg);
                } else {
//                    Snackbar.make(view, "Start scan", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                    wifiResultsCounter = 0;
                    isScanning = true;
                    wifiThread = new Thread() {
                        public void run() {
                            while (isScanning) {
                                Message msg = new Message();
                                msg.what = Config.STARTSCAN;
                                mHandler.sendMessage(msg);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } catch (Exception e) {

                                }
                            }
                        }
                    };
                    wifiThread.start();
                }
            }
        });

        infoTextView = (TextView) findViewById(R.id.tv_counter);
        infoTextView.setText("Wifi Counter: 0 (Stopped)");

        wifiListView = (ListView) findViewById(R.id.lv_wifi);
        scanResult = wifiScanner.scanNetworks();
        scanResultsAdapter = new ScanResultsAdapter(MainActivity.this, scanResult);
        wifiListView.setAdapter(scanResultsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final EditText myinput = new EditText(this);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            new AlertDialog.Builder(this).setTitle("Wifi Number: "+Config.WIFINUMBER).setIcon(
                    android.R.drawable.ic_dialog_info).setView(myinput)
                    .setPositiveButton("Save",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Config.WIFINUMBER = Integer.parseInt(myinput.getText().toString());
                                    } catch (Exception e) {
                                        Snackbar.make(findViewById(R.id.fab_ss).getRootView(), "Failed to save", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                        return;
                                    }
                                    Snackbar.make(findViewById(R.id.fab_ss).getRootView(), "Save successfully", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            })
                    .setNegativeButton("Cancle", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
