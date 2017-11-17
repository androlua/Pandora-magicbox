package org.nopad.pandorabox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

@SuppressLint({"HandlerLeak"})
public class MainActivity extends Activity {
    public static final int MSG_DOWNLOADED = 0;
    Button exitButton;
    @SuppressLint({"HandlerLeak"})
    private Handler handler = new 1(this);
    Button hideButton;
    String ip = null;
    boolean isRoot = false;
    Server mServer;
    ProgressDialog pd;
    String port = "8089";
    String portt = null;
    ToggleButton startButton;
    boolean waitTimer = false;
    String wwwroot = "/data/data/com.ayansoft.androphp/lighttpd";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(0x7f030000);
        setRequestedOrientation(1);
        Bundle b = getIntent().getExtras();
        Intent i = new Intent(this, DisplayNotification.class);
        i.putExtra("NotifID", 1);
        startActivity(i);
        SharedPreferences pr = PreferenceManager.getDefaultSharedPreferences(this);
        this.port = pr.getString("updates_port", "8080");
        this.wwwroot = pr.getString("updates_path", "/data/data/com.ayansoft.androphp/lighttpd");
        this.isRoot = pr.getBoolean("enable_root", false);
        this.mServer = new Server(this, this.port, this.wwwroot, this.isRoot);
        if (b != null && b.getBoolean("changes")) {
            this.mServer.stopServer();
            SystemClock.sleep(500);
            startServer();
        }
        this.startButton = (ToggleButton) findViewById(0x7f080003);
        this.exitButton = (Button) findViewById(0x7f080008);
        this.exitButton.setOnClickListener(new 2(this));
        this.hideButton = (Button) findViewById(0x7f08000a);
        this.hideButton.setOnClickListener(new 3(this));
        ((Button) findViewById(0x7f080009)).setOnClickListener(new 4(this));
        this.startButton.setOnClickListener(new 5(this));
        this.ip = getLocalIpAddress();
        this.portt = this.port;
        if (this.ip == null) {
            this.ip = "localhost";
        }
        if (this.portt.equals("80")) {
            this.portt = "";
        }
        if (!this.portt.equals("")) {
            this.portt = ":" + this.portt;
        }
        TextView link = (TextView) findViewById(0x7f080004);
        link.setText("http://" + this.ip + this.portt);
        link.setOnClickListener(new 6(this));
        TextView linkmy = (TextView) findViewById(0x7f080005);
        linkmy.setText("http://" + this.ip + this.portt + "/instruction");
        linkmy.setOnClickListener(new 7(this));
        controlServer(true, 0x3b9ac9ff);
    }

    public static void killThisPackageIfRunning(Context context, String packageName) {
        ((ActivityManager) context.getSystemService("activity")).killBackgroundProcesses(packageName);
    }

    private void startServer() {
        this.pd = ProgressDialog.show(this, "Pandora-magicbox", "成功，请代理第一个地址...", true, false);
        new 8(this).start();
    }

    private void stopServer() {
        this.pd = ProgressDialog.show(this, "Pandora-magicbox", "停止服务器...", true, false);
        new 9(this).start();
    }

    private void controlServer(boolean u, int max) {
        int maxi = max;
        if (u) {
            new 10(this, (long) maxi, 1000).start();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(this, QuickPrefsActivity.class);
                intent.setFlags(0x04000000);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    public String getLocalIpAddress() {
        try {
            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            if (nilist.size() > 0) {
                for (NetworkInterface ni : nilist) {
                    List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                    if (ialist.size() > 0) {
                        for (InetAddress address : ialist) {
                            if (!address.isLoopbackAddress()) {
                                String ipv4 = address.getHostAddress();
                                if (InetAddressUtils.isIPv4Address(ipv4)) {
                                    return ipv4;
                                }
                            }
                        }
                        continue;
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }

    public static void CancelNotification(Context ctx, int notifyId) {
        ((NotificationManager) ctx.getSystemService("notification")).cancel(notifyId);
    }

    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
