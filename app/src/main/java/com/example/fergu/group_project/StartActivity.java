package com.example.fergu.group_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    protected static final String ACTIVITY_NAME = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button automobileButton = (Button) findViewById(R.id.automobileButton);
        Button nutritionButton = (Button) findViewById(R.id.dietaryButton);
        Button toDoListButton = (Button) findViewById(R.id.toDoList);
        Button calendarButton = (Button) findViewById(R.id.calendarButton);

        automobileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i(ACTIVITY_NAME, "User clicked Automobile");

                Intent startAutolog = new Intent(StartActivity.this ,AutomobileActivity.class);

                startActivityForResult(startAutolog, 10);
            }
        } );


        nutritionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i(ACTIVITY_NAME, "User clicked Nutrition");

                Intent startNutrition = new Intent(StartActivity.this , NutritionActivity.class);

                startActivityForResult(startNutrition, 10);
            }
        } );

        toDoListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i(ACTIVITY_NAME, "User clicked To Do List");

                Intent startToDo = new Intent(StartActivity.this , ToDoList.class);

                startActivityForResult(startToDo, 10);
            }
        } );


        calendarButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i(ACTIVITY_NAME, "User clicked Calendar");

                Intent startCalendar = new Intent(StartActivity.this , CalendarActivity.class);

                startActivityForResult(startCalendar, 10);
            }
        } );

    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.i(ACTIVITY_NAME, "In OnStart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(ACTIVITY_NAME, "In OnResume()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(ACTIVITY_NAME, "In OnPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(ACTIVITY_NAME, "In OnStop()");
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        Log.i(ACTIVITY_NAME, "In OnDestroy()");
    }

}
