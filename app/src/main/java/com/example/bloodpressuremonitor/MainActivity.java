package com.example.bloodpressuremonitor;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.bloodpressuremonitor.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Keep this in mind, not sure if this will work
                View root = binding.getRoot();
                DBHandler bpdb = new DBHandler(root.getContext());

                File file = new File(root.getContext().getFilesDir(), "BP_CSV.csv");
                Uri contentUri = getUriForFile(root.getContext(), "com.example.bloodpressuremonitor.fileprovider", file);
                PrintWriter printWriter = null;
                try {
                    printWriter = new PrintWriter(new FileWriter(file));
                    SQLiteDatabase db = bpdb.getReadableDatabase();

                    Cursor curCSV = db.rawQuery("SELECT * FROM bloodpressuredata", null);
                    printWriter.println("datetime,systolic,diastolic");
                    while(curCSV.moveToNext())
                    {
                        String datetime = curCSV.getString(curCSV.getColumnIndexOrThrow("datetime"));
                        String systolic = curCSV.getString(curCSV.getColumnIndexOrThrow("systolic"));
                        String diastolic = curCSV.getString(curCSV.getColumnIndexOrThrow("diastolic"));

                        String record = datetime + "," + systolic + "," + diastolic;
                        printWriter.println(record);
                    }
                    curCSV.close();
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(printWriter != null) printWriter.close();
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(root.getContext());
                String address = sharedPreferences.getString("email", "");
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {address}); // recipient
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blood Pressure Data");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "This is a CSV file containing the blood pressure data.");
                emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                emailIntent.setPackage("com.google.android.gm");
                startActivity(emailIntent);
                //startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }

        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // User chooses the "Settings" item. Show the app settings screen.
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }// If we got here, the action was not recognized.
        // Invoke the superclass.
        return super.onOptionsItemSelected(item);
    }
}