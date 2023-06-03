package android.bignerdranch.pixivbrowser;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class VpnService extends android.net.VpnService {
    private static final String TAG = "NetGuard.Service";
    private static final String EXTRA_COMMAND = "Command";
    private ParcelFileDescriptor vpn = null;
    public static final int START = 1;
    public static final int RELOAD = 2;
    public static final int STOP = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter ifConnectivity = new IntentFilter();
        ifConnectivity.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        Builder builder = new Builder();
        String[] appPackages = {"android.bignerdranch.pixivbrowser"};
        PackageManager mPackageManager = getPackageManager();

        builder.setSession(getString(R.string.app_name));
        builder.addAddress("222.223.192.3",24);
        builder.addRoute("0.0.0.0",0);
        builder.addDnsServer("208.67.222.222");

        for(String appPackage : appPackages){
            try {
                mPackageManager.getPackageInfo(appPackage,0);
                builder.addAllowedApplication(appPackage);
            }catch (PackageManager.NameNotFoundException e){
                Log.d("vpn","添加允许的vpn应用出错，未找到");
                e.printStackTrace();
            }
        }
        ParcelFileDescriptor parcelFileDescriptor = builder.establish();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = intent.getIntExtra(EXTRA_COMMAND,RELOAD);
        Log.d(TAG,"执行：" + cmd);
        return super.onStartCommand(intent, flags, startId);
    }
}
