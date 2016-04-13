package com.john990.mvp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import com.john990.mvp.model.Wifi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiHelper {

    public final static String TAG = "WifiHelper";
    /**
     * Anything worse than or equal to this will show 0 bars.
     */
    private static final int MIN_RSSI = -100;

    /**
     * Anything better than or equal to this will show the max bars.
     */
    private static final int MAX_RSSI = -55;
    private static final String[] CARRIER_REDIRECT_WHITE_LIST = new String[]{
            "ChinaNet", "CMCC-WEB", "CMCC-EDU", "ChinaUnicom"
    };
    private static final String WRONG_BSSID = "00:00:00:00:00:00";
    private static final String[] WRONG_SSID_LIST = new String[]{"<unknown ssid>", "wifi"};
    private volatile static WifiHelper sInstance = null;
    private static WifiManager wifiManager;
    private static ConnectivityManager connectivityManager;
    private static Context context;

    public static void init(Context context) {
        WifiHelper.context = context;
        wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static WifiManager getWifiManager() {
        return wifiManager;
    }

    public static int getWifiCapabilities(String capabilities) {
        int level = Wifi.TYPE_NO_PASSWD;
        if (capabilities == null)
            return level;
        if (capabilities.contains("WEP")) {
            level = Wifi.TYPE_WEP;
        } else if (capabilities.contains("PSK")) {
            level = Wifi.TYPE_WPA;
        } else if (capabilities.contains("EAP")) {
            level = Wifi.TYPE_EAP;
        }
        return level;
    }

    public static int getSecurity(WifiConfiguration config) {

        if (config == null) {
            return Wifi.TYPE_UNKNOWN;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return Wifi.TYPE_WPA;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return Wifi.TYPE_EAP;
        }
        return (config.wepKeys[0] != null) ? Wifi.TYPE_WEP : Wifi.TYPE_NO_PASSWD;
    }

    public static boolean isInCarrierRedirectWhiteList(String name) {
        boolean result = false;
        if (name != null) {
            for (String item : CARRIER_REDIRECT_WHITE_LIST) {
                if (name.equals(item)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public static String handleStr(String string) {
        if (string == null)
            return "";
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            float inputRange = (MAX_RSSI - MIN_RSSI);
            float outputRange = (numLevels - 1);
            return (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
        }
    }

    public static boolean isWifiEnable() {
        return wifiManager.isWifiEnabled();
    }

    private static boolean setWifiEnable(boolean state) {
        return wifiManager.setWifiEnabled(state);
    }

    public static boolean closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            return setWifiEnable(false);
        }
        return false;
    }

    public static boolean toggleWifi() {
        return setWifiEnable(!wifiManager.isWifiEnabled());
    }

    public static boolean openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            return setWifiEnable(true);
        }
        return false;
    }

    public static void disconnectionCurWifi() {
        disconnectionCurWifi(-1);
//        disableAllNetwork();
    }

    public static boolean disconnectionCurWifi(int networkId) {
        if (networkId >= 0) {
            return wifiManager.disableNetwork(networkId);
        } else {
            WifiInfo curWifi = getConnectionInfo();
            if (curWifi != null) {
                int nid = curWifi.getNetworkId();
                if (nid > -1) {
                    return wifiManager.disableNetwork(nid);
                }
                // mWifiManager.disconnect();
            }

            return false;
        }
    }

    public static void disableAllNetwork() {
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks != null) {
            for (WifiConfiguration network : networks) {
                if (network.networkId >= 0) {
                    wifiManager.disableNetwork(network.networkId);
                }
            }
        }
    }

    public static WifiConfiguration getWifiConfigurationById(int networkId) {
        if (networkId > -1) {
            List<WifiConfiguration> existingConfigs = wifiManager
                    .getConfiguredNetworks();
            if (existingConfigs != null) {
                for (WifiConfiguration configuration : existingConfigs) {
                    if (configuration.networkId == networkId) {
                        return configuration;
                    }
                }
            }
        }

        return null;
    }


    public static boolean startScan() {
        return wifiManager.startScan();
    }

    private static List<ScanResult> getScanResults() {
        List<ScanResult> results = null;
        try {
            results = wifiManager.getScanResults();
        } catch (Exception e) {
            Hog.e(e);
        }

        return results;
    }

    public static ScanResult getScanResultsByBssid(String bssid) {
        try {
            List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult r : results) {
                if (TextUtils.equals(r.BSSID, bssid)) {
                    return r;
                }
            }
        } catch (Exception e) {
            Hog.e(e);
        }

        return null;
    }


    public static State getWifiConnectiveState() {
        NetworkInfo netWorkInfo = getNetworkInfo();
        if (netWorkInfo != null) {
            return netWorkInfo.getState();
        } else {
            return State.UNKNOWN;
        }
    }

    public static NetworkInfo getNetworkInfo() {
        try {
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Exception e) {

        }
        return null;
    }

    public static boolean isConnected() {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnected();
    }

    public static WifiInfo getConnectionInfo() {
        try {
            return wifiManager.getConnectionInfo();
        } catch (Exception e) {
            Hog.e(e);
        }
        return null;
    }

    public static Wifi getActiveWifi() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null
                && !TextUtils.isEmpty(wifiInfo.getSSID())
                && !TextUtils.isEmpty(wifiInfo.getBSSID())) {
            if ("0x".equals(wifiInfo.getSSID())) {
                return null;
            }
            List<ScanResult> scanResults = getScanResults();
            if (scanResults != null) {
                String bssid = wifiInfo.getBSSID();
                for (ScanResult scan : scanResults) {
                    if (TextUtils.equals(bssid, scan.BSSID)) {
                        return new Wifi(scan);
                    }
                }
            }
        }
        return null;
    }

    // 获取到所有 wifi 列表 只做去重
    private static List<Wifi> getAllWifiListPure() {
        if (!isWifiEnable())
            return null;
        List<Wifi> items = new ArrayList<>();
        Map<String, Wifi> result = new HashMap<>();
        List<Wifi> scanList = getAllScanWifi();
        for (Wifi scan : scanList) {
            if (TextUtils.equals(scan.bssid, WRONG_BSSID)) {
                continue;
            }

            // 过滤掉信号非常差的点
            if (TextUtils.isEmpty(scan.ssid) || scan.level < -95) {
                continue;
            }

            Wifi item = new Wifi().copy(scan);
            String cacheKey = item.ssid + "$" + item.capabilities;
            Wifi w = result.get(cacheKey);
            if (w == null) {
                result.put(cacheKey, item);
            } else {
                if (item.hasPassword() && !w.hasPassword()) {
                    if (w.level > item.level) item.level = w.level;
                    result.put(cacheKey, item);
                } else if (!item.hasPassword() && w.hasPassword()) {
                    if (w.level < item.level) w.level = item.level;
                } else {
                    if (w.level < item.level)
                        result.put(cacheKey, item);
                }
            }
        }
        if (result.size() > 0) {
            items.addAll(result.values());
        }
        return items;
    }

    public static Wifi getWifiByBssid(String bssid) {
        List<Wifi> allWifiList = getAllWifiListPure();
        if (allWifiList == null) {
            return null;
        }
        for (Wifi wifi : allWifiList) {
            if (TextUtils.equals(bssid, wifi.bssid)) {
                return wifi;
            }
        }
        return null;
    }

//    public static Wifi getActiveWifi() {
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        if (wifiInfo == null) {
//            return null;
//        }
//        return getWifiByBssid(wifiInfo.getBSSID());
////        if (wifi == null) {
////            wifi = new Wifi();
////            wifi.ssid = handleStr(wifiInfo.getSSID());
////            wifi.bssid = wifiInfo.getBSSID();
////            wifi.level = WifiHelper.calculateSignalLevel(wifiInfo.getRssi(), Const.MAX_SIGNAL_LEVEL);
////        }
////        return wifi;
//    }

    public static List<Wifi> getSavedWifi() {
        List<WifiConfiguration> wcs = wifiManager.getConfiguredNetworks();
        List<Wifi> items = new ArrayList<>();
        if (wcs != null && wcs.size() > 0) {
            Wifi item;
            for (WifiConfiguration wc : wcs) {
                item = new Wifi(wc);
                items.add(item);
            }
        }
        return items;
    }

    public static List<WifiConfiguration> getSavedConfig() {
        return wifiManager.getConfiguredNetworks();
    }

    /**
     * 获取所有 Wifi 没有去重
     *
     * @return
     */
    public static List<Wifi> getAllScanWifi() {
        if (!isWifiEnable())
            return null;
        List<Wifi> items = new ArrayList<>();
        List<ScanResult> scanResults = getScanResults();
        if (scanResults != null && scanResults.size() > 0) {
            for (ScanResult r : scanResults) {
                items.add(new Wifi(r));
            }
        }
        return items;
    }

    /**
     * //     * @param item        the wifi to connect
     * //     * @param forceUsePwd Whether force to use the password specified by the item
     * //     * @return
     * //
     */
//    public static boolean connectWifi(Wifi item, boolean forceUsePwd) {
//        if (item == null || !isWifiEnable())
//            return false;
//
//        WifiCache.setActiveWifi(item);
//
//        boolean isConnSuccess = false;
//        int id = getWifiConfigurationId(item, forceUsePwd);
//        Hog.i(TAG, "networkId is : " + id);
//        if (id > -1) {
//            // disconnectionCurWifi();
//            isConnSuccess = wifiManager.enableNetwork(id, true);
//            wifiManager.saveConfiguration();
//            item.setIsSaved(true);
//        }
//        return isConnSuccess;
//    }
    private static int getWifiConfigurationId(Wifi item, boolean forceUsePwd) {
        WifiConfiguration cfg = isExsits(item.ssid, item.capabilities);
        if (cfg != null) {
            Hog.i(TAG, "saved WifiConfiguration is :  " + cfg.toString());
            if (!forceUsePwd) {
                return cfg.networkId;
            }
            if (item.hasPassword()) {
                int type = item.capabilities;
                if (type == Wifi.TYPE_WEP) {
                    setWepPassword(item, cfg);
                } else if (type == Wifi.TYPE_WPA) {
                    setWpaPassword(item, cfg);
                } else if (type == Wifi.TYPE_EAP) {
                    return cfg.networkId;
                }
                return wifiManager.updateNetwork(cfg);
            }
            return cfg.networkId;
        } else {
            cfg = createWifiInfo(item);
            if (cfg != null) {
                Hog.i(TAG, "createWifiInfo is : " + cfg.toString());
                return wifiManager.addNetwork(cfg);
            }
        }
        return -1;
    }

    public static WifiConfiguration createWifiInfo(Wifi item, boolean converSsid) {
        WifiConfiguration config = new WifiConfiguration();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || !converSsid) {
            config.SSID = item.ssid;
        } else {
            config.SSID = convertToQuotedString(item.ssid);
        }
        switch (item.capabilities) {
            case Wifi.TYPE_NO_PASSWD:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case Wifi.TYPE_WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.SHARED);
                setWepPassword(item, config);
                break;
            case Wifi.TYPE_WPA:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                setWpaPassword(item, config);
                break;
            case Wifi.TYPE_EAP:
                return createEAPWifiInfo(item);
            default:
                return null;
        }
        return config;
    }

    public static WifiConfiguration createWifiInfo(Wifi item) {
        return createWifiInfo(item, true);
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private static WifiConfiguration createEAPWifiInfo(Wifi item) {
        try {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + item.ssid + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                config.enterpriseConfig = new WifiEnterpriseConfig();
                config.enterpriseConfig.setPassword(item.pwd);
                config.enterpriseConfig.setIdentity(item.identify);
                config.enterpriseConfig.setEapMethod(0);
                config.enterpriseConfig.setPhase2Method(0);
            } else {
                configEnterpriseField(config, "eap", "PEAP");
                configEnterpriseField(config, "phase2", "");
                configEnterpriseField(config, "identity", (TextUtils.isEmpty(item.identify)) ? "" : item.identify);
                configEnterpriseField(config, "anonymous_identity", "");
                configEnterpriseField(config, "password", (TextUtils.isEmpty(item.pwd)) ? "" : item.pwd);
            }

            return config;
        } catch (Exception e) {
            return null;
        }
    }

    public static void configEnterpriseField(WifiConfiguration wifiConfiguration,
                                             String name, String value) throws Exception {
        Class<?> WifiConfigurationClass = Class
                .forName("android.net.wifi.WifiConfiguration");
        Class<?> EnterpriseFieldClass = Class
                .forName("android.net.wifi.WifiConfiguration$EnterpriseField");

        Field field = WifiConfigurationClass.getField(name);

        Object object = field.get(wifiConfiguration);

        Method setValueMethod = EnterpriseFieldClass.getMethod("setValue",
                String.class);
        setValueMethod.invoke(object, value);

        Method valueMethod = EnterpriseFieldClass.getMethod("value");
        Object invoke = valueMethod.invoke(object);
    }

    public static boolean deleteWifi(String ssid, int securityType) {
        List<WifiConfiguration> wcs = isExsitsAllConfigs(ssid, securityType);
        if (wcs == null)
            return true;
        int count = 0;
        for (WifiConfiguration wc : wcs) {
            if (wifiManager.removeNetwork(wc.networkId)) {
                count++;
            }
        }
        return count > 0 && wifiManager.saveConfiguration();
    }

    public static List<WifiConfiguration> isExsitsAllConfigs(String SSID, int securityType) {
        if (SSID == null)
            return null;
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            List<WifiConfiguration> wcs = new ArrayList<>();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID == null)
                    continue;
                if ((existingConfig.SSID.equals("\"" + SSID + "\"") || existingConfig.SSID
                        .equals(SSID))
                        && getSecurity(existingConfig) == securityType) {
                    wcs.add(existingConfig);
                }
            }
            return wcs;
        }
        return null;
    }

    public static WifiConfiguration isExsits(String SSID, int securityType) {
        if (SSID == null)
            return null;
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID == null)
                    continue;
                if ((existingConfig.SSID.equals("\"" + SSID + "\"") || existingConfig.SSID
                        .equals(SSID))
                        && getSecurity(existingConfig) == securityType) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private static void setWpaPassword(Wifi item, WifiConfiguration config) {
        if (item.hasPassword()) {
            if (item.pwd.matches("[0-9A-Fa-f]{64}")) {
                config.preSharedKey = item.pwd;
            } else {
                config.preSharedKey = '"' + item.pwd + '"';
            }
        }
    }

    private static void setWepPassword(Wifi item, WifiConfiguration config) {
        if (!TextUtils.isEmpty(item.pwd)) {
            int length = item.pwd.length();
            // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
            if ((length == 10 || length == 26 || length == 58)
                    && item.pwd.matches("[0-9A-Fa-f]*")) {
                config.wepKeys[0] = item.pwd;
            } else {
                config.wepKeys[0] = '"' + item.pwd + '"';
            }
        }
    }

    public static List<Wifi> getWifiListExceptActive() {
        if (!isWifiEnable())
            return null;
        List<Wifi> items = new ArrayList<>();
        Map<String, Wifi> result = new HashMap<>();
        List<Wifi> scanList = getAllScanWifi();
        List<Wifi> savedList = getSavedWifi();
        Wifi activeWiFi = getActiveWifi();
        if (scanList == null) {
            return items;
        }
        for (Wifi scan : scanList) {
            // 跳过当前连接WiFi
            if (activeWiFi != null && activeWiFi.equals(scan)) {
                continue;
            }

            if (TextUtils.equals(scan.bssid, WRONG_BSSID)) {
                continue;
            }

            // 过滤掉信号非常差的点
            if (TextUtils.isEmpty(scan.ssid) || scan.level < -95)
                continue;

            Wifi item = new Wifi().copy(scan);
            String cacheKey = item.ssid + "$" + item.capabilities;
            Wifi w = result.get(cacheKey);
            if (w == null) {
                result.put(cacheKey, item);
            } else {
                if (item.hasPassword() && !w.hasPassword()) {
                    if (w.level > item.level) item.level = w.level;
                    result.put(cacheKey, item);
                } else if (!item.hasPassword() && w.hasPassword()) {
                    if (w.level < item.level) w.level = item.level;
                } else {
                    if (w.level < item.level)
                        result.put(cacheKey, item);
                }
            }

            for (Wifi save : savedList) {
                if (save.equals(item)) {
                    item.copy(save);
                }
            }

        }
        if (result.size() > 0) {
            items.addAll(result.values());
        }

        Collections.sort(items);
        return items;
    }

    public static List<Wifi> getAllWifiListIncludeActive() {
        if (!isWifiEnable())
            return null;
        List<Wifi> items = new ArrayList<>();
        Map<String, Wifi> result = new HashMap<>();
        List<Wifi> scanList = getAllScanWifi();
        List<Wifi> savedList = getSavedWifi();
        if (scanList == null) {
            return items;
        }
        Wifi activeWiFi = getActiveWifi();
        for (Wifi scan : scanList) {
            if (TextUtils.equals(scan.bssid, WRONG_BSSID)) {
                continue;
            }

            // 当前wifi的bssid可能不正确
            if (activeWiFi != null && activeWiFi.equals(scan)) {
                if (WifiUtils.isWrongBssid(scan.bssid)) {
                    WifiInfo info = WifiHelper.getConnectionInfo();
                    activeWiFi.bssid = info.getBSSID();
                    scan.bssid = info.getBSSID();
                }
            }

            // 过滤掉信号非常差的点
            if (TextUtils.isEmpty(scan.ssid) || scan.level < -95)
                continue;

            Wifi item = new Wifi().copy(scan);
            String cacheKey = item.ssid + "$" + item.capabilities;
            Wifi w = result.get(cacheKey);
            if (w == null) {
                result.put(cacheKey, item);
            } else {
                if (item.hasPassword() && !w.hasPassword()) {
                    if (w.level > item.level) item.level = w.level;
                    result.put(cacheKey, item);
                } else if (!item.hasPassword() && w.hasPassword()) {
                    if (w.level < item.level) w.level = item.level;
                } else {
                    if (w.level < item.level)
                        result.put(cacheKey, item);
                }
            }

            for (Wifi save : savedList) {
                if (save.equals(item)) {
                    item.copy(save);
                    break;
                }
            }

        }
        if (result.size() > 0) {
            items.addAll(result.values());
        }

        Collections.sort(items);
        return items;
    }

//    public static List<Wifi> getAllWifiListIncludeActive() {
//        List<Wifi> allWifiListPure = getAllWifiListPure();
//        if (allWifiListPure == null) {
//            return null;
//        }
//        Collections.sort(allWifiListPure);
//        return allWifiListPure;
//    }

    public static List<Wifi> getAllWifiListExcept(Wifi wifi) {
        List<Wifi> allWifiListPure = getAllWifiListPure();
        if (allWifiListPure == null) {
            return null;
        }
        Collections.sort(allWifiListPure);
        if (wifi == null) {
            return allWifiListPure;
        }
        Wifi tmp = null;
        for (Wifi w : allWifiListPure) {
            if (w.equals(wifi)) {
                tmp = w;
                break;
            }
        }
        if (tmp != null) {
            allWifiListPure.remove(tmp);
        }

        return allWifiListPure;
    }

    public static boolean isWrongWifi(Wifi wifi) {
        if (wifi == null || wifi.bssid == null || wifi.bssid.equals(WRONG_BSSID) || TextUtils.isEmpty(wifi.ssid)) {
            return true;
        }
        return false;
    }

}
