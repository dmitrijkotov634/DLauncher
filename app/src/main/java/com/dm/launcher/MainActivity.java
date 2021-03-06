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

    private AppNameAdapter listAdapter;
    private List < AppInfo > apps;
    private SharedPreferences prefs;

    private int nb, nf, sb, sf, textSize;

    private boolean search, icons;

    private PackageReceiver PackageReceiver;
    private Thread appsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appMenu = findViewById(R.id.app_menu);
        searchBar = findViewById(R.id.search_bar);
        menu = findViewById(R.id.menu);

        apps = new ArrayList<AppInfo>();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        LinearLayoutManager AppMenu
            = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        appMenu.setLayoutManager(AppMenu);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        listAdapter.startApp(getApplicationContext(), 0);
                        return true;
                    }
                    return false;
                }
            });

        searchBar.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {}

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    List<AppInfo> find = new ArrayList<>();

                    String searchText = s.toString().toLowerCase();

                    for (AppInfo info: apps) {
                        if (info.title.toLowerCase().startsWith(searchText))
                            find.add(info);
                    }

                    if (find.size() == 0 && search) {
                        AppInfo searchAction = new AppInfo();
                        searchAction.title = getString(R.string.search_item);

                        find.add(searchAction);
                    }

                    listAdapter = new AppNameAdapter(find, searchBar, nb, nf, sb, sf,
                                                     textSize, icons);

                    appMenu.setAdapter(listAdapter);
                }
            });

        appsLoader = new AppsLoader();
        appsLoader.run();

        PackageReceiver = new PackageReceiver(getApplicationContext(), appsLoader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrefs();
    }
    
    @Override
    public void onBackPressed() {
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

        search = prefs.getBoolean("search", false);
        icons = prefs.getBoolean("icons", false);

        textSize = Integer.valueOf(prefs.getString("size", "15"));

        if (textSize < 2) textSize = 15;

        switch (prefs.getString("gravity", "0")) {
            case "0":
                ((LinearLayout.LayoutParams) menu.getLayoutParams()).gravity = Gravity.TOP;
                break;
            case "1":
                ((LinearLayout.LayoutParams) menu.getLayoutParams()).gravity = Gravity.BOTTOM;
                break;
        }

        searchBar.setTextSize(textSize);
        searchBar.setTextColor(nf);

        menu.setBackgroundColor(nb);
    }

    class AppsLoader extends Thread {
        public void run() {
            PackageManager pm = getApplicationContext().getPackageManager();

            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

            apps = new ArrayList<AppInfo>(allApps.size());

            for (ResolveInfo info: allApps) {
                AppInfo app = new AppInfo();
                app.activity = info.activityInfo.name;
                app.title = (String) info.loadLabel(pm);
                app.packageName = info.activityInfo.applicationInfo.packageName;
                app.icon = info.activityInfo.loadIcon(pm);
                apps.add(app);
            }

            AppInfo prefs = new AppInfo();
            prefs.activity = getPackageName() + ".PrefActivity";
            prefs.title = "Launcher settings";
            prefs.packageName = getPackageName();
            prefs.icon = getResources().getDrawable(R.drawable.ic_launcher);
            apps.add(prefs);

            appMenu.post(new Runnable() {
                    public void run() {
                        listAdapter = new AppNameAdapter(apps, searchBar, nb, nf, sb, sf,
                                                         textSize, icons);
                        appMenu.setAdapter(listAdapter);
                    }
                });
        }
    }
}
