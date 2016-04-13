package com.john990.mvp.model;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.john990.mvp.utils.WifiHelper;
import com.john990.mvp.utils.WifiUtils;

/**
 * Created by sunsheng1 on 15/7/24.
 */

@Table(name = "WiFi")
public class Wifi extends Model implements Parcelable, Comparable<Wifi> {

    private static final int MIN_SIG_STRENGTH = -999;
    /**
     * Value for safe level
     */
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_NO_PASSWD = 0x02;
    public static final int TYPE_WEP = 0x04;
    public static final int TYPE_WPA = 0x06;
    public static final int TYPE_EAP = 0x08;

    @Column(name = "bssid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String bssid;

    @Column(name = "ssid")
    public String ssid;

    @Column(name = "pwd")
    public String pwd;

    @Column(name = "tmpPwd")
    public String tmpPwd;

    @Column(name = "startConnectTime")
    public long startConnectTime;

    @Column(name = "capabilities")
    public int capabilities;

    @Column(name = "wnysPwd")
    public String wnysPwd;

    @Column(name = "speed")
    public long speed;

    @Column(name = "speed_has_sync")
    public int speedHasSync;

    @Column(name = "isShareCanceled")
    public int isShareCanceled;

    @Column(name = "lat")
    public double lat;

    @Column(name = "lng")
    public double lng;

    public Object arg;

    public String identify;
    public int level = MIN_SIG_STRENGTH;
    public int networkId = -1;
    public boolean isSaved;

    public String getPassword() {
        if (!TextUtils.isEmpty(pwd)) {
            return pwd;
        } else if (!TextUtils.isEmpty(wnysPwd)) {
            return wnysPwd;
        }
        return null;
    }

    public boolean hasPassword() {
        return !TextUtils.isEmpty(pwd) || !TextUtils.isEmpty(wnysPwd);
    }

    public boolean hasCloudPwd() {
        return !TextUtils.isEmpty(wnysPwd);
    }

    public boolean shareAble() {
        return (capabilities == TYPE_WEP || capabilities == TYPE_WPA) && !hasCloudPwd();
    }

    public boolean isFree() {
        return (networkId > -1 || hasPassword())
                && !isPublic();
    }

    public boolean isPublic() {
        return capabilities == TYPE_NO_PASSWD;
    }

    public Wifi() {
    }

    public Wifi(WifiConfiguration config) {
        if (!WifiUtils.isWrongBssid(config.BSSID)) {
            bssid = config.BSSID;
        }
        ssid = WifiHelper.handleStr(config.SSID);
        capabilities = WifiHelper.getSecurity(config);
        networkId = config.networkId;
        setIsSaved(config.networkId > -1);
    }

    public Wifi(ScanResult scanResult) {
        bssid = scanResult.BSSID;
        ssid = WifiHelper.handleStr(scanResult.SSID);
        level = scanResult.level;
        capabilities = WifiHelper.getWifiCapabilities(scanResult.capabilities);
    }


    public Wifi copy(Wifi wifi) {
        if (!WifiUtils.isWrongBssid(wifi.bssid)) {
            bssid = wifi.bssid;
        }
        ssid = wifi.ssid;
        if (!TextUtils.isEmpty(wifi.pwd)) {
            pwd = wifi.pwd;
        }
        if (!TextUtils.isEmpty(wifi.identify)) {
            identify = wifi.identify;
        }
        if (wifi.networkId > -1) {
            networkId = wifi.networkId;
        }
        capabilities = wifi.capabilities;
        if (wifi.level != MIN_SIG_STRENGTH) {
            level = wifi.level;
        }
        isSaved = wifi.isSaved;
        return this;
    }

    public Wifi copy(ScanResult scanResult) {
        bssid = scanResult.BSSID;
        ssid = WifiHelper.handleStr(scanResult.SSID);
        level = scanResult.level;
        capabilities = WifiHelper.getWifiCapabilities(scanResult.capabilities);
        return this;
    }

    public Wifi copy(WifiConfiguration config) {
        ssid = WifiHelper.handleStr(config.SSID);
        bssid = config.BSSID;
        capabilities = WifiHelper.getSecurity(config);
        setIsSaved(config.networkId > -1);
        return this;
    }

    public void setIsSaved(boolean saved) {
        // 开放WiFi不显示已保存
        if (saved && capabilities != Wifi.TYPE_NO_PASSWD) {
            isSaved = true;
        } else {
            isSaved = false;
        }
    }

    public boolean hasLocation() {
        return lat != 0 || lng != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wifi wifi = (Wifi) o;

        if (capabilities != wifi.capabilities) return false;
        return TextUtils.equals(ssid, wifi.ssid);

    }

    @Override
    public int compareTo(Wifi rhs) {
        if (this.isFree()) {
            return -1;
        } else {
            if (rhs.isFree()) {
                return 1;
            } else {
                return rhs.level - this.level;
            }
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ssid.hashCode();
        result = 31 * result + capabilities;
        return result;
    }

    @Override
    public String toString() {
        return "Wifi{" +
                "bssid='" + bssid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", pwd='" + pwd + '\'' +
                ", tmpPwd='" + tmpPwd + '\'' +
                ", startConnectTime=" + startConnectTime +
                ", capabilities=" + capabilities +
                ", wnysPwd='" + wnysPwd + '\'' +
                ", identify='" + identify + '\'' +
                ", level=" + level +
                ", networkId=" + networkId +
                ", isSaved=" + isSaved +
                '}';
    }

    public static final Creator<Wifi> CREATOR =
            new Creator<Wifi>() {
                public Wifi createFromParcel(Parcel in) {
                    Wifi info = new Wifi();
                    info.bssid = in.readString();
                    info.ssid = in.readString();
                    info.pwd = in.readString();
                    info.identify = in.readString();
                    info.networkId = in.readInt();
                    info.capabilities = in.readInt();
                    info.level = in.readInt();
                    return info;
                }


                public Wifi[] newArray(int size) {
                    return new Wifi[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bssid);
        dest.writeString(ssid);
        dest.writeString(pwd);
        dest.writeString(identify);
        dest.writeInt(networkId);
        dest.writeInt(capabilities);
        dest.writeInt(level);
    }
}
