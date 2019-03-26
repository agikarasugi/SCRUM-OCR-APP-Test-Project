package com.example.agi.navdrawerandimagepickertestapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.agi.navdrawerandimagepickertestapp.fragment.FragmentAbout;
import com.example.agi.navdrawerandimagepickertestapp.fragment.FragmentHistory;
import com.example.agi.navdrawerandimagepickertestapp.fragment.FragmentHome;
import com.example.agi.navdrawerandimagepickertestapp.fragment.FragmentSettings;
import com.example.agi.navdrawerandimagepickertestapp.fragment.FragmentTextEditor;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawer_layout);

        actionBar.setTitle("SCRUM");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commit();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();

//                        Toast toast;
                        switch (menuItem.getItemId()){
                            case R.id.nav_home:
//                                toast = Toast.makeText(MainActivity.this, "Home selected", Toast.LENGTH_SHORT);
//                                toast.show();
                                actionBar.setTitle("SCRUM");
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commit();
                                return true;
                            case R.id.nav_history:
//                                toast = Toast.makeText(MainActivity.this, "History selected", Toast.LENGTH_SHORT);
//                                toast.show();
                                actionBar.setTitle("HISTORY");
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHistory()).commit();
                                break;
                            case R.id.nav_textEditor:
//                                toast = Toast.makeText(MainActivity.this, "Text Editor selected", Toast.LENGTH_SHORT);
//                                toast.show();
                                actionBar.setTitle("TEXT EDITOR");
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTextEditor()).commit();
                                break;
                            case R.id.nav_settings:
//                                toast = Toast.makeText(MainActivity.this, "Settings selected", Toast.LENGTH_SHORT);
//                                toast.show();
                                actionBar.setTitle("SETTINGS");
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSettings()).commit();
                                break;
                            case R.id.nav_about:
//                                toast = Toast.makeText(MainActivity.this, "About selected", Toast.LENGTH_SHORT);
//                                toast.show();
                                actionBar.setTitle("ABOUT");
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentAbout()).commit();
                                break;
                        }

                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
