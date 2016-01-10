package com.example.wyatt.myapplication;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;


/**
 * Created by wyatt on 1/10/16.
 */
public class NetworkScanner {
    private WifiManager manager;
    private List<ScanResult> results;

    private static int resultsNum = 0;

    public NetworkScanner(WifiManager manager) {
        this.manager = manager;
    }

    public static void sInit() {
       resultsNum = 0;
    }

    public void init() {
        if(manager.isWifiEnabled()) {
            scanNetworks();
        }
    }

    public List<ScanResult> scanNetworks() {
        boolean scan = manager.startScan();
        if(scan) {
            results = manager.getScanResults();
        }
        return results;
    }
}
