package com.loftysites.connor.percentpaleo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.hardware.camera2.TotalCaptureResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.sql.Types.REAL;

public class MainActivity extends AppCompatActivity {

    //variables for main layout
    TextView percentTextView, iAmTextView, percentPaleoTextView, wantTextView;
    TextView mealWasTextView, andTextView;
    TextView snackWasTextView, andTextView2;
    TextView drinkWasTextView, andTextView3;
    double percentDouble;
    double totalGiven;
    double totalPossible;
    double scoreGiven;
    double scorePossible;

    //Database
    SQLiteDatabase entryDatabase;

    //Sharedpref
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //layout variables
    RelativeLayout mainLayout;
    RelativeLayout mealLayout;
    RelativeLayout drinkLayout;
    RelativeLayout snackLayout;

    //variables for selection layouts
    RadioGroup mealSizeRG, mealPaleoRG;
    RadioGroup snackSizeRG, snackPaleoRG;
    RadioGroup drinkSizeRG, drinkPaleoRG;
    int scoreFlag = 0;
    int calcPeriod = 2; //0 for week, 1 for month, 2 for all time

    //Time period variables
    RadioGroup timePeriodRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        //Create or open database
        try{

            entryDatabase = this.openOrCreateDatabase("Entries", MODE_PRIVATE, null);

            entryDatabase.execSQL("CREATE TABLE IF NOT EXISTS entries (date VARCHAR, given REAL, possible REAL)");

            //entryDatabase.execSQL("INSERT INTO entries (date, given, possible) VALUES ('2017-05-10', '50', '50')");


        }catch(Exception e){
            e.printStackTrace();
        }




        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        mealLayout = (RelativeLayout)findViewById(R.id.mealLayout);
        snackLayout = (RelativeLayout)findViewById(R.id.snackLayout);
        drinkLayout = (RelativeLayout)findViewById(R.id.drinkLayout);

        //initialize textviews and set custom font
        percentTextView = (TextView)findViewById(R.id.percentTextView);
        iAmTextView = (TextView)findViewById(R.id.iAmTextView);
        percentPaleoTextView = (TextView)findViewById(R.id.percentPaleoTextView);
        wantTextView = (TextView)findViewById(R.id.wantTextView);
        mealWasTextView = (TextView)findViewById(R.id.mealWasTextView);
        andTextView = (TextView)findViewById(R.id.andTextView);
        snackWasTextView = (TextView)findViewById(R.id.snackWasTextView);
        andTextView2 = (TextView)findViewById(R.id.andTextView2);
        drinkWasTextView = (TextView)findViewById(R.id.drinkWasTextView);
        andTextView3 = (TextView)findViewById(R.id.andTextView3);


        //custom font
        Typeface font = Typeface.createFromAsset(getAssets(), "KBPlanetEarth.ttf");
        percentTextView.setTypeface(font);
        iAmTextView.setTypeface(font);
        percentPaleoTextView.setTypeface(font);
        wantTextView.setTypeface(font);
        mealWasTextView.setTypeface(font);
        andTextView.setTypeface(font);
        snackWasTextView.setTypeface(font);
        andTextView2.setTypeface(font);
        drinkWasTextView.setTypeface(font);
        andTextView3.setTypeface(font);

        //Radiogroup for time period to display
        timePeriodRG = (RadioGroup) findViewById(R.id.timeRG);

        //Check by default
        calcPeriod = pref.getInt("calcPeriod", 2);
        switch(calcPeriod) {
            case 0:
                timePeriodRG.check(R.id.weekRB);
                break;
            case 1:
                timePeriodRG.check(R.id.monthRB);
                break;
            case 2:
                timePeriodRG.check(R.id.allRB);
                break;
            default:
                timePeriodRG.check(R.id.allRB);
                break;
        }

        timePeriodRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking the time period radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.weekRB:
                        calcPeriod = 0;
                        updatePercent();
                        editor.putInt("calcPeriod", calcPeriod);
                        editor.commit();
                        break;
                    case R.id.monthRB:
                        calcPeriod = 1;
                        updatePercent();
                        editor.putInt("calcPeriod", calcPeriod);
                        editor.commit();
                        break;
                    case R.id.allRB:
                        calcPeriod = 2;
                        updatePercent();
                        editor.putInt("calcPeriod", calcPeriod);
                        editor.commit();
                        break;
                }
            }
        });

        updatePercent();


        //setting up radiogroups for meal layout
        //size radio group
        mealSizeRG = (RadioGroup) findViewById(R.id.mealSizeRadioGroup);
        mealSizeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking size radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.smallMealRadioButton:
                        scorePossible = 20;
                        break;
                    case R.id.averageMealRadioButton:
                        scorePossible = 36;
                        break;
                    case R.id.largeMealRadioButton:
                        scorePossible = 50;
                        break;
                }
            }
        });

        //amount paleo radio group
        mealPaleoRG = (RadioGroup) findViewById(R.id.mealPaleoRadioGroup);
        mealPaleoRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking amount paleo radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.mealCompRadioButton:
                        scoreFlag = 1;
                        break;
                    case R.id.mostlyMealRadioButton2:
                        scoreFlag = 2;
                        break;
                    case R.id.notMealRadioButton:
                        scoreFlag = 3;
                        break;
                }
            }
        });



        //setting up radiogroups for snack layout
        //size radio group
        snackSizeRG = (RadioGroup) findViewById(R.id.snackSizeRadioGroup);
        snackSizeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking size radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.smallSnackRadioButton:
                        scorePossible = 5;
                        break;
                    case R.id.averageSnackRadioButton:
                        scorePossible = 10;
                        break;
                    case R.id.largeSnackRadioButton:
                        scorePossible = 20;
                        break;
                }
            }
        });

        //amount paleo radio group
        snackPaleoRG = (RadioGroup) findViewById(R.id.snackPaleoRadioGroup);
        snackPaleoRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking amount paleo radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.snackCompRadioButton:
                        scoreFlag = 1;
                        break;
                    case R.id.notSnackRadioButton:
                        scoreFlag = 2;
                        break;
                }
            }
        });



        //setting up radiogroups for drink layout
        //size radio group
        drinkSizeRG = (RadioGroup) findViewById(R.id.drinkSizeRadioGroup);
        drinkSizeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking size radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.smallDrinkRadioButton:
                        scorePossible = 4;
                        break;
                    case R.id.averageDrinkRadioButton:
                        scorePossible = 10;
                        break;
                    case R.id.largeDrinkRadioButton:
                        scorePossible = 20;
                        break;
                }
            }
        });

        //amount paleo radio group
        drinkPaleoRG = (RadioGroup) findViewById(R.id.drinkPaleoRadioGroup);
        drinkPaleoRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //method for checking amount paleo radio group selection
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //switch for each radiogroup selection
                switch (checkedId) {
                    case R.id.drinkCompRadioButton:
                        scoreFlag = 1;

                        break;
                    case R.id.notDrinkRadioButton:
                        scoreFlag = 2;
                        break;
                }
            }
        });

    }

    //OnClick method for meal
    public void mealOnClick(View view){

        scorePossible = 0;
        scoreFlag = 0;

        mainLayout.setVisibility(View.INVISIBLE);
        mealLayout.setVisibility(View.VISIBLE);

    }

    //OnClick method for add meal
    public void addMealOnClick(View view){

        //calculate scoreGiven and scorePossible
        if (scoreFlag == 1){
            scoreGiven = scorePossible;
        }else if(scoreFlag == 2){
            scoreGiven = scorePossible/2;
        }else if(scoreFlag == 3){
            scoreGiven = 0;
        }

        //check if radiogroups not selected, if pass start next activity
        if(scorePossible == 0){
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else if(scoreFlag == 0) {
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getBaseContext(), "You just added a meal!", Toast.LENGTH_SHORT).show();

            createDbEntry(scoreGiven, scorePossible);

            updatePercent();

            //Change layout
            mainLayout.setVisibility(View.VISIBLE);
            mealLayout.setVisibility(View.INVISIBLE);

            //clear radiogroups
            mealSizeRG.clearCheck();
            mealPaleoRG.clearCheck();
        }

    }



    //cancel button to go back activity
    public void cancelOnClick(View view){

        mainLayout.setVisibility(View.VISIBLE);
        mealLayout.setVisibility(View.INVISIBLE);
        snackLayout.setVisibility(View.INVISIBLE);
        drinkLayout.setVisibility(View.INVISIBLE);

        //clear radiogroups
        mealSizeRG.clearCheck();
        mealPaleoRG.clearCheck();
        snackSizeRG.clearCheck();
        snackPaleoRG.clearCheck();
        drinkSizeRG.clearCheck();
        drinkPaleoRG.clearCheck();

    }

    //OnClick method for snack
    public void snackOnClick(View view){

        scorePossible = 0;
        scoreFlag = 0;

        mainLayout.setVisibility(View.INVISIBLE);
        snackLayout.setVisibility(View.VISIBLE);

    }

    //OnClick method for add snack
    public void addSnackOnClick(View view){

        //calculate scoreGiven and scorePossible
        if (scoreFlag == 1){
            scoreGiven = scorePossible;
        }else if(scoreFlag == 2){
            scoreGiven = 0;
        }

        //check if radiogroups not selected, if pass start next activity
        if(scorePossible == 0){
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else if(scoreFlag == 0) {
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getBaseContext(), "You just added a snack!", Toast.LENGTH_SHORT).show();

            createDbEntry(scoreGiven, scorePossible);

            updatePercent();

            //Change layouts
            mainLayout.setVisibility(View.VISIBLE);
            snackLayout.setVisibility(View.INVISIBLE);

            //clear radiogroups
            snackSizeRG.clearCheck();
            snackPaleoRG.clearCheck();
        }
    }

    //OnClick method for drink
    public void drinkOnClick(View view){

        scorePossible = 0;
        scoreFlag = 0;

        mainLayout.setVisibility(View.INVISIBLE);
        drinkLayout.setVisibility(View.VISIBLE);

}

    //OnClick method for add drink
    public void addDrinkOnClick(View view){

        //calculate scoreGiven and scorePossible
        if (scoreFlag == 1){
            scoreGiven = scorePossible/2;
            scorePossible = scoreGiven;
        }else if(scoreFlag == 2){
            scoreGiven = 0;
        }

        //check if radiogroups not selected, if pass start next activity
        if(scorePossible == 0){
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else if(scoreFlag == 0) {
            Toast.makeText(getApplicationContext(), "Oops! You forgot to select an option!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getBaseContext(), "You just added a drink!", Toast.LENGTH_SHORT).show();

            createDbEntry(scoreGiven, scorePossible);

            updatePercent();

            //Change layouts
            mainLayout.setVisibility(View.VISIBLE);
            drinkLayout.setVisibility(View.INVISIBLE);

            //clear radiogroups
            drinkSizeRG.clearCheck();
            drinkPaleoRG.clearCheck();
        }
    }

    public String getCurrentDateAsString(){

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

    public void createDbEntry(Double given, Double possible){

        String currentDate = getCurrentDateAsString();
        String givenString = Double.toString(given);
        String possibleString = Double.toString(possible);
        entryDatabase.execSQL("INSERT INTO entries (date, given, possible) VALUES (?, ?, ?)", new String[] {currentDate, givenString, possibleString});

    }

    public void updatePercent(){

        //Reset totals
        totalGiven = 0.0;
        totalPossible = 0.0;

        //Test Printing DB Entries
        Cursor c = entryDatabase.rawQuery("SELECT * FROM entries", null);
        int dateIndex = c.getColumnIndex("date");
        int givenIndex = c.getColumnIndex("given");
        int possibleIndex = c.getColumnIndex("possible");

        if(calcPeriod == 0){                //Week

            if(c.moveToFirst()) {

                do {

                    if(isInTimePeriod(c.getString(dateIndex), 7)){

                        //Add to totals
                        totalGiven += c.getDouble(givenIndex);
                        totalPossible += c.getDouble(possibleIndex);

                    }

                } while (c.moveToNext());

            }

            calculatePercent();

        }else if(calcPeriod == 1){          //Month

            if(c.moveToFirst()) {

                do {

                    if(isInTimePeriod(c.getString(dateIndex), 30)){

                        //Add to totals
                        totalGiven += c.getDouble(givenIndex);
                        totalPossible += c.getDouble(possibleIndex);

                    }

                } while (c.moveToNext());

            }

            calculatePercent();

        }else if(calcPeriod == 2){          //All time

            if(c.moveToFirst()) {

                do {

                    //Add to totals
                    totalGiven += c.getDouble(givenIndex);
                    totalPossible += c.getDouble(possibleIndex);

                } while (c.moveToNext());

            }

            calculatePercent();

        }else{

            Toast.makeText(getApplicationContext(), "Please select a period of time!", Toast.LENGTH_SHORT).show();

        }

    }

    public void calculatePercent(){

        if(totalPossible != 0.0){

            //calculate percent
            percentDouble = (totalGiven/totalPossible) * 100;
            int percentInt = (int) percentDouble;

            percentTextView.setText(Integer.toString(percentInt) + "%");

        }else{

            percentTextView.setText("100%");

        }

    }

    public boolean isInTimePeriod(String date, int numDays){

        int differenceInDays = Integer.MAX_VALUE;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        try {
            Date dateToCheck = sdf.parse(date);
            Date currentDate = sdf.parse(getCurrentDateAsString());

            long diffInMillis = currentDate.getTime() - dateToCheck.getTime();
            differenceInDays = (int)(diffInMillis / (1000*60*60*24));

        } catch (ParseException e) {
            Log.i("Error isInTimePeriod", e.getMessage());
        }

        Log.i("differenceInDays - ", Integer.toString(differenceInDays));

        if(differenceInDays < numDays){
            return true;
        }else{
            return false;
        }

    }

}
