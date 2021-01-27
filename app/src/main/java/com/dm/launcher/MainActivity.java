package com.dm.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import java.util.List;
import java.util.ArrayList;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.ListAdapter;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends Activity { 

    private RecyclerView appMenu;
    private EditText searchBar;
    private LinearLayout menu;

    AppNameAdapter listAdapter;
    private List<AppInfo> apps;
    SharedPreferences prefs;

    int nb, nf, sb, sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appMenu = findViewById(R.id.app_menu);
        searchBar = findViewById(R.id.search_bar);
        menu = findViewById(R.id.menu);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        LinearLayoutManager AppMenu
            = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        appMenu.setLayoutManager(AppMenu);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH && listAdapter.data.size() > 0) {
                        Intent intent = new Intent();
                        intent.setClassName(listAdapter.data.get(0).packageName,
                                            listAdapter.data.get(0).activity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        searchBar.setText("");
                        return true;
                    }
                    return false;
                }
            });

        searchBar.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    List<AppInfo> find = new ArrayList<>();

                    String search = s.toString().toLowerCase();

                    for (AppInfo info : apps) {
                        if (info.title.toLowerCase().startsWith(search))
                            find.add(info);
                    }

                    listAdapter = new AppNameAdapter(find, searchBar, nb, nf, sb, sf);
                    appMenu.setAdapter(listAdapter);
                }
            });
        
        new Thread(new Runnable() {
                public void run() {
                    apps = getAppsList();
                    listAdapter = new AppNameAdapter(apps, searchBar, nb, nf, sb, sf);

                    appMenu.post(new Runnable() {
                            public void run() {
                                appMenu.setAdapter(listAdapter);
                            }
                        });
                }
            }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrefs();
    }

    public void clickBackground(View view) {
        searchBar.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
    }

    public void loadPrefs() {
        nb = prefs.getInt("nb", 0xFF000000);
        nf = prefs.getInt("nf", 0xFFF3F3F3);
        sb = prefs.getInt("sb", 0xFF005577);
        sf = prefs.getInt("sf", 0xFFFFFFFF);

        switch (prefs.getString("gravity", "0")) {
        case "0":
            ((LinearLayout.LayoutParams) menu.getLayoutParams()).gravity = Gravity.TOP;
            break;
        case "1":
            ((LinearLayout.LayoutParams) menu.getLayoutParams()).gravity = Gravity.BOTTOM;
            break;
        }
        
        searchBar.setTextColor(nf);
        menu.setBackgroundColor(nb);
    }

    public List<AppInfo> getAppsList() {
        PackageManager pm = getApplicationContext().getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

        ArrayList<AppInfo> data = new ArrayList<AppInfo>(allApps.size());

        for (ResolveInfo info : allApps) {
            AppInfo app = new AppInfo();
            app.activity = info.activityInfo.name;
            app.title = (String) info.loadLabel(pm);
            app.packageName = info.activityInfo.applicationInfo.packageName;
            data.add(app);
        }

        return data;
    }

} 