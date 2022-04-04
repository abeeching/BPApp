package com.example.bloodpressuremonitor;

import static android.widget.Toast.LENGTH_LONG;

import static java.net.Proxy.Type.HTTP;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodpressuremonitor.databinding.ActivityMainBinding;

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

        // To do: test this FAB on a physical device. Email may not work correctly on Android Studio emulator.
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Keep this in mind, not sure if this will work
                View root = binding.getRoot();
                DBHandler bpdb = new DBHandler(root.getContext());

                File file;
                PrintWriter printWriter = null;
                try {
                    file = new File(root.getContext().getFilesDir(), "BP_CSV.csv");
                    file.createNewFile();
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
                    Snackbar errSnackbar = Snackbar.make(view, "Something Went Wrong", BaseTransientBottomBar.LENGTH_LONG);
                } finally {
                    if(printWriter != null) printWriter.close();
                }

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/html");
                // TODO - This is a test. We will need to allow the user to set the email. Maybe as a setting or something.
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "aidanbeeching@gmail.com");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blood Pressure Data");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "This is a CSV file containing the blood pressure data.");
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.valueOf(root.getContext().getFilesDir())));
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