package com.example.touhid.mychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsPagerAdapter mTabsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); //if the user is signed-in or not

        //Tabs for MainActivity
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("myChat");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser(); //gets the current signed-in userID(unique)

        if (currentUser == null) {
            logoutUser();
        }
    }

    private void logoutUser() {
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_button) {
            mAuth.signOut();
            logoutUser();
        }
        if(item.getItemId() == R.id.main_account_setting_button) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_users_button) {
            Intent allUsersIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUsersIntent);
        }
        return true;
    }
}
