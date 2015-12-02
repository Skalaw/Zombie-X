package com.asda.zombiex.android;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;

import com.asda.zombiex.Game;
import com.asda.zombiex.SystemInterface;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    private final static String FIELD_NICKNAME = "nickname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemInterface systemInterface = new SystemInterface() {
            @Override
            public void setNickname(String nickname) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(FIELD_NICKNAME, nickname);
                if (Build.VERSION.SDK_INT > 8) {
                    editor.apply();
                } else {
                    editor.commit();
                }
            }

            @Override
            public String getNickname() {
                String nickname = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(FIELD_NICKNAME, "");
                return nickname;
            }
        };

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress()); // TODO: support only IPv4
        // TODO: send new IP when we have connect later
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        initialize(new Game(systemInterface, ip), config);
    }
}
