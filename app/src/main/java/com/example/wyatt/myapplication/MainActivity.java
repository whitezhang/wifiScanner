package com.example.wyatt.myapplication;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private NetworkScanner wifiScanner;
    private WifiManager wifiManager;
    private ListView wifiListView;
    private TextView infoTextView;

    private Thread wifiThread;
    private volatile boolean isScanning;
    private int wifiResultsCounter = 0;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.STARTSCAN:
                    wifiResultsCounter++;
                    infoTextView = infoTextView == null ? (TextView)findViewById(R.id.tv_counter) : infoTextView;
                    infoTextView.setText("Wifi Counter:" + String.valueOf(wifiResultsCounter));
                    Log.e("Handler", String.valueOf(wifiResultsCounter));
                    if(wifiResultsCounter == Config.WIFINUMBER) {
                        stopThread();
                        Snackbar.make(findViewById(R.id.fab).getRootView(), "Scan finished. Number of wifi:" + String.valueOf(wifiResultsCounter), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    flushWifiList();
                    break;
                default:
                    Log.e("default", "default");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void init() {
        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        wifiScanner = new NetworkScanner(wifiManager);
        isScanning = false;
    }

    public void stopThread() {
        Log.e("stop", "thread");
        isScanning = false;
        if(wifiThread != null) {
            wifiThread.interrupt();
        }
    }

    public void flushWifiList() {
        wifiListView = wifiListView == null ? (ListView)findViewById(R.id.lv_wifi) : wifiListView;
        List<ScanResult> res = wifiScanner.scanNetworks();
        ScanResultsAdapter adapter = new ScanResultsAdapter(MainActivity.this, res);
        wifiListView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton scanButton = (FloatingActionButton) findViewById(R.id.fab);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) {
                    Snackbar.make(view, "Stop scan", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    stopThread();
                } else {
                    Snackbar.make(view, "Start scan", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isScanning = true;
                    wifiThread = new Thread() {
                        public void run() {
                            while(isScanning) {
                                Message msg = new Message();
                                msg.what = Config.STARTSCAN;
                                mHandler.sendMessage(msg);
                                try {
                                    Thread.sleep(1000);
                                } catch(InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    wifiResultsCounter = 0;
                                } catch(Exception e) {

                                }
                            }
                        }
                    };
                    wifiThread.start();
                }
            }
        });

        infoTextView = (TextView) findViewById(R.id.tv_counter);
        infoTextView.setText("Wifi Counter: 0");

        wifiListView = (ListView) findViewById(R.id.lv_wifi);
        List<ScanResult> res = wifiScanner.scanNetworks();
        ScanResultsAdapter adapter = new ScanResultsAdapter(MainActivity.this, res);
        wifiListView.setAdapter(adapter);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
