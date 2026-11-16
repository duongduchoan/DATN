package com.hoan.qlpt.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLogin(String uid, String role, String name) {
        editor.putString(Constants.PREF_USER_UID, uid);
        editor.putString(Constants.PREF_USER_ROLE, role);
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public String getUserUid() {
        return prefs.getString(Constants.PREF_USER_UID, "");
    }

    public String getUserRole() {
        return prefs.getString(Constants.PREF_USER_ROLE, Constants.ROLE_GUEST);
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
