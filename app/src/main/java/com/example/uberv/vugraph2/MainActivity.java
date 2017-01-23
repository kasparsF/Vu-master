package com.example.uberv.vugraph2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberv.vugraph2.api.ApiBAAS;
import com.example.uberv.vugraph2.utils.PreferencesUtils;

import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;

public class MainActivity extends AppCompatActivity {

    TextView usernameTv;
    TextView emailTv;
    TextView accessTokenTv;
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView  nvDrawer;

    private VuGraphUser currentUser;
    private ActionBarDrawerToggle drawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        usernameTv = (TextView)findViewById(R.id.textView);
//        emailTv = (TextView)findViewById(R.id.textView2);
//        accessTokenTv = (TextView)findViewById(R.id.textView3);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView)findViewById(R.id.nvView);

        currentUser = PreferencesUtils.getCurrentUser();
        populateDrawerHeader(currentUser);

//        usernameTv.setText(currentUser.getUsername());
//        emailTv.setText(currentUser.getEmail());
//        accessTokenTv.setText(currentUser.getAccessToken());

        setSupportActionBar(toolbar);

        setupDrawerContent(nvDrawer);
        drawerToggle=new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
        drawer.addDrawerListener(drawerToggle);
    }

    private void populateDrawerHeader(VuGraphUser currentUser){
        View header = nvDrawer.getHeaderView(0);
        TextView drawerHeaderUsernameTv = (TextView) header.findViewById(R.id.drawer_header_username_tv);
        drawerHeaderUsernameTv.setText(currentUser.getUsername());
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            case R.id.nav_first_fragment:
                                startPreview();
                                break;
                            case R.id.nav_second_fragment:
                                Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_third_fragment:
                                Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_logout:
                                // 1 delete from preferences
                                PreferencesUtils.remove(PreferencesUtils.CURRENT_USER);
                                // 2 revoke tokens from ApiGee
                                ApiBAAS.getInstance(MainActivity.this).getApigeeDataClient().logOutAppUserAsync(currentUser.getUsername(),null);
                                // 3 open RegisterActivity
                                Intent startRegisterActivityIntent = new Intent(MainActivity.this,RegisterActivity.class);
                                startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(startRegisterActivityIntent);
                                break;
                            default:
                        }
                        return true;
                    }
                });
    }


    public void startPreview() {
        boolean opened = openApp(this, "com.fisers.graphs");
        if (!opened) {
            // player app was not found =>
            // alert the user to download player app
            new AlertDialog.Builder(MainActivity.this)
                    //.setIcon(R.drawable.logout)
                    .setTitle("Something went wrong")
                    .setMessage("Player App Needed")
                    .setPositiveButton("Download Player", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO open link to download player apk
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    /**
     * Open another app.
     *
     * @param context     current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        // do not add this activity to recent apps
        intent.addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        // start it's LAUNCHER acitivity
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(intent);
        return true;
    }




    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
