package com.globalreviewcenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;

import com.globalreviewcenter.R;
import com.globalreviewcenter.controller.ExceptionHandler;
import com.globalreviewcenter.view.fragments.AboutFragment;
import com.globalreviewcenter.view.fragments.MainFragment;
import com.globalreviewcenter.view.fragments.NavigationMenuFragment;
import com.globalreviewcenter.view.fragments.TermsOfUseFragment;

public class HomeActivity extends AppCompatActivity {

    private String GLOBAL_REVIEW_CENTER = "Global Review Center";

    public static DrawerLayout mDrawerLayout;
    private NavigationMenuFragment navigationMenuFragment;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;

    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///set exception handler
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        initVariables();
        initUI();

    }
    private void initVariables() {
        mContext = this;
    }
    private void initUI() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btnMenu = (Button)toolbar.findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        //////////////navigation view
        fragmentManager = getSupportFragmentManager();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
        navigationMenuFragment = new NavigationMenuFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.main_menu_container, navigationMenuFragment)
                .commit();
        MainFragment mainFragment = new MainFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mainFragment, GLOBAL_REVIEW_CENTER)
                .commit();
    }


    public void navigationTo(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment(), GLOBAL_REVIEW_CENTER)
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new AboutFragment(), GLOBAL_REVIEW_CENTER)
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new TermsOfUseFragment(), GLOBAL_REVIEW_CENTER)
                        .commit();
                break;
        }
        mDrawerLayout.closeDrawers();
    }

}
