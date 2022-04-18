package com.example.bloodpressuremonitor.ui.slideshow;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.bloodpressuremonitor.DBHandler;
import com.example.bloodpressuremonitor.R;
import com.example.bloodpressuremonitor.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // define button, text view, and table layouts from the screen

        Button buttonClick = (Button)root.findViewById(R.id.button);
        TextView tv = (TextView)root.findViewById(R.id.textView2);
        TableLayout tl = (TableLayout)root.findViewById(R.id.tableLayout);

        // Define database
        DBHandler dbHandler;
        dbHandler = new DBHandler(root.getContext());
        // code to read in data & place it into the table layout.

        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSevere = false; // variable that is set to true if SBP or DBP exceed recommended values
                tv.setText("");
                String myOutput;
                InputStream myInputStream;

                try {
                    // TODO - Remove the code for reading from a text file once we can confirm the Bluetooth component works.
                    // Assuming the Bluetooth does work, all we should have to worry about is reading data from the database with dbHandler.
                    myInputStream = root.getContext().getAssets().open("sample_data.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(myInputStream));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");

                        dbHandler.addBPData(values[0], values[1], values[2]);
                    }

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(root.getContext());
                    ;
                    String days = sharedPreferences.getString("list_preference_1", "");
                    dbHandler.deleteOld(Integer.parseInt(days));
                    SQLiteDatabase db = dbHandler.getReadableDatabase();

                    Cursor bpCursor = db.rawQuery("SELECT * FROM bloodpressuredata", null);

                    if (bpCursor.moveToFirst()) {
                        do {
                            // logic to determine if a particular reading is severe or not
                            if ((Integer.parseInt(bpCursor.getString(2)) >= 160 || Integer.parseInt(bpCursor.getString(3)) >= 110)) {
                                isSevere = true;
                            } else {
                                isSevere = false;
                            }

                            // create a new table row for the data to be read in
                            TableRow row = new TableRow(tl.getContext());
                            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                            // if the reading is marked as severe, color-code the row in question to indicate this to the user
                            // for the purposes of prototype #1, there should be three severe readings and one normal reading
                            if (isSevere) {
                                row.setBackgroundColor(Color.parseColor("#FA8072"));
                            }

                            // add values into each appropriate column.
                            // since data is formatted as 'datetime,systolic,diastolic' we split each string at the commas and create text views for each
                            TextView t0 = new TextView(row.getContext());
                            t0.setText(bpCursor.getString(1));
                            t0.setTextSize(14);
                            t0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
                            t0.setPadding(10, 10, 10, 10);
                            t0.setTextColor(Color.BLACK);
                            t0.setGravity(Gravity.CENTER_HORIZONTAL);
                            row.addView(t0);

                            TextView t1 = new TextView(row.getContext());
                            t1.setText(bpCursor.getString(2));
                            t1.setTextSize(14);
                            t1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
                            t1.setPadding(10, 10, 10, 10);
                            t1.setTextColor(Color.BLACK);
                            t1.setGravity(Gravity.CENTER_HORIZONTAL);
                            row.addView(t1);

                            TextView t2 = new TextView(row.getContext());
                            t2.setPadding(10, 10, 10, 10);
                            t2.setText(bpCursor.getString(3));
                            t2.setTextSize(14);
                            t2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
                            t2.setTextColor(Color.BLACK);
                            t2.setGravity(Gravity.CENTER_HORIZONTAL);
                            row.addView(t2);

                            tl.addView(row);
                        } while (bpCursor.moveToNext());

                        // mainly here for debugging, will likely not be in the final product
                        // checks to make sure the button even works, or if something went wrong in the file-reading process
                        tv.setText("should be good to go");
                        bpCursor.close();
                        myInputStream.close();
                    }
            } catch (Exception e) {
                    tv.setText("You should not see this.");
                    //e.printStackTrace();
                }
        }


    });
    return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}