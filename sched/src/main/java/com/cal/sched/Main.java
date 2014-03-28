package com.cal.sched;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tim on 3/23/2014.
 */
public class Main extends ActionBarActivity
{

    private String sched = "";
    private boolean schedAdd = false;
    private myAdapter adapt;

    //set arrays too big fix later

    //regular
    private String[] classes = new String[10];
    private String[] teachers = new String[10];
    private String[] rooms = new String[10];

    //times
    private String[] day100 = new String[]{"7:22-8:05", "8:10-8:52", "8:57-9:39", "9:44-10:26",
        "10:31-11:17", "11:22-12:08", "12:13-12:53", " 12:58-1:40", "12:07-12:53", "1:45-2:27",
            "2:32-3:14"};
    private String[] cycleDay = new String[]{"7:22-8:05", "8:10-9:07", "9:12-9:24", "9:29-10:26",
            "10:31-11:28", "11:37-12:34", "12:39-1:10", " 1:15-2:12", "2:17-3:14"};

    //lunch
    private Boolean[] bLunchBool = new Boolean[5];
    private String bLunches = "";

    //cycle
    private String[] cycleClass = new String[9];
    private String[] cycleTeacher = new String[9];
    private String[] cycleRoom = new String[9];


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mainAct();
    }

    /**
     * used as main process that handles all of the processes on the main page
     */
    private void mainAct()
    {
        /********** sets date and cycle **********/
        getDate();
        getCycle();

        try
        {
            day100[5] = getIntent().getStringExtra("day1005");
            day100[6] = getIntent().getStringExtra("day1006");
            cycleDay[4] = getIntent().getStringExtra("cycleDay4");
            cycleDay[5] = getIntent().getStringExtra("cycleDay5");
            bLunches = getIntent().getStringExtra("bLunch");
            sched = getIntent().getStringExtra("userSched");
            if(sched != null)
                schedAdd = true;
            else
            {
                try
                {
                    sched = readSched("schedule");
                    bLunches = readSched("bLunch");

                    String[] full = bLunches.split(",");
                    for(int i = 0; i < 5; i++)
                    {
                        if (full[i].equals("true"))
                            bLunchBool[i] = true;
                        else
                            bLunchBool[i] = false;
                    }
                    if(sched != null)
                    {
                        schedAdd = true;
                        for(int i = 0; i < 5; i++)
                        {
                            if(i == 0 && bLunchBool[0])
                            {
                                day100[5] = "11:22-12:02";
                                day100[6] = "12:07-12:53";
                            }
                            else if(bLunchBool[i])
                            {
                                cycleDay[4] = "11:33-12:08";
                                cycleDay[5] = "12:13-1:10";
                            }

                            /********** preset values of D lunch **********/
                            else
                            {
                                day100[5] = "12:07-12:53";
                                day100[6] = "11:22-12:02";
                                cycleDay[4] = "12:13-1:10";
                                cycleDay[5] = "11:33-12:08";
                            }
                        }
                    }

                } catch(Exception e)
                {
                    sched = "";
                }
            }
        } catch(NullPointerException e)
        {
            sched = "";

        }

        if(schedAdd)
        {
            /********** makes the schedule **********/
            splitSched(sched);

            ListView lists = (ListView) findViewById(R.id.listView);
            if(getDate().equals("Monday\n\n"))
                adapt = new myAdapter(this, classes, teachers, rooms, day100);
            else
            {
                if(getDate().equals("Tuesday\n\n"))
                    setCycleArray(78);
                else if(getDate().equals("Wednesday\n\n"))
                    setCycleArray(56);
                else if(getDate().equals("Thursday\n\n"))
                    setCycleArray(34);
                else if(getDate().equals("Friday\n\n"))
                    setCycleArray(12);
                adapt = new myAdapter(this, Get("classes"), Get("teachers"), Get("rooms"), cycleDay);
            }
            lists.setAdapter(adapt);
        }
        else
        {
            if(sched.equals(""))
            {
                Button add = (Button) findViewById(R.id.finish);
                try
                {
                    /********** makes button visible **********/
                    add.requestFocus();
                    add.setFocusable(true);
                    if(add.getVisibility() == View.INVISIBLE || add.getVisibility() == View.GONE)
                        add.setVisibility(View.VISIBLE);
                    /********** makes toast for no schedule **********/
                    Toast.makeText(this, "No Current Schedule", Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    Log.e("SCHEDULE", e.getMessage() + " error!");
                }
            }
            else
                splitSched(sched);
        }
    }

    /**
     * in the button click, it starts the new activity for the enterance of all of the classes
     * @param v for the view of the View
     */
    public void onClick(View v)
    {
        Intent myIntent = new Intent(Main.this, AddSched.class);
        Main.this.startActivity(myIntent);
    }

    /**
     * splits the string into ArrayLists of classes, teachers, and rooms
     * @param s string to be split
     */
    public void splitSched(String s)
    {
        saveSched(s);

        String[] full = s.split(",");
        int indivT = 0;
        int indivR = 0;

        for(int i = 0; i < full.length; i++)
        {
            if (i < 10)
                classes[i] = full[i];
            else if (i < 20)
            {
                teachers[indivT] = full[i];
                indivT++;
            }
            else
            {
                rooms[indivR] = full[i];
                indivR++;
            }
        }
    }

    /**
     * returns today's date
     * @return string of day
     */
    public String getDate()
    {
        TextView day = (TextView) findViewById(R.id.day);
        TextView date = (TextView) findViewById(R.id.date);
        SimpleDateFormat dayForm = new SimpleDateFormat("EEEE");
        SimpleDateFormat dateForm = new SimpleDateFormat("MMM dd, yyyy");
        Date today = new Date();

        /********** check to see if day is accessible **********/
        try {
            day.setText(Html.fromHtml("<b><h1>" + dayForm.format(today) + "</h1></b>"));
            date.setText(Html.fromHtml("<fontsize=\"10\">" + dateForm.format(today) + "</font>"));
        } catch (Exception e) {
            Log.e("DATE", e.getMessage() + " Error!");
        }

        return day.getText().toString();
    }

    /**
     * gets the cycle based on the day
     */
    public void getCycle()
    {
        TextView cycle = (TextView) findViewById(R.id.cycle);
        String dc = getDate();
        /********** sets cycle **********/
        if(dc.equals("Monday\n\n"))
            cycle.setText(Html.fromHtml("h3> 100 Day </h3>"));
        else if(dc.equals("Tuesday\n\n"))
            cycle.setText(Html.fromHtml("<h3> 78 Day </h3>"));
        else if(dc.equals("Wednesday\n\n"))
            cycle.setText(Html.fromHtml("<h3> 56 Day </h3>"));
        else if(dc.equals("Thursday\n\n"))
            cycle.setText(Html.fromHtml("<h3> 34 Day </h3>"));
        else if(dc.equals("Friday\n\n"))
            cycle.setText(Html.fromHtml("<h3> 12 Day </h3>"));
        else
            cycle.setText(Html.fromHtml("<h3> 100 Day </h3>"));
    }

    /**
     *
     * @param s takes in String of what wants to be gotten
     * @return array of what ever the programmer wants
     */
    public String[] Get(String s)
    {
        if(s.equals("classes"))
            return cycleClass;
        else if(s.equals("teachers"))
            return cycleTeacher;
        else
            return cycleRoom;
    }

    public void setCycleArray(int a)
    {
        if(a == 78)
        {
            cycleClass[0] = classes[0];
            cycleTeacher[0] = teachers[0];
            cycleRoom[0] = rooms[0];
            cycleClass[1] = classes[9];
            cycleTeacher[1] = teachers[9];
            cycleRoom[1] = rooms[9];
            cycleClass[2] = classes[1];
            cycleTeacher[2] = teachers[1];
            cycleRoom[2] = rooms[1];
            cycleClass[3] = classes[2];
            cycleTeacher[3] = teachers[2];
            cycleRoom[3] = rooms[2];
            cycleClass[4] = classes[3];
            cycleTeacher[4] = teachers[3];
            cycleRoom[4] = rooms[3];
            if(cycleDay[5].toString().equals("11:37-12:34"))
            {
                cycleClass[5] = classes[4];
                cycleTeacher[5] = teachers[4];
                cycleRoom[5] = rooms[4];
                cycleClass[6] = "Lunch";
                cycleTeacher[6] = "-----";
                cycleRoom[6] = "-----";
            }
            else
            {
                cycleClass[5] = "Lunch";
                cycleTeacher[5] = "-----";
                cycleRoom[5] = "-----";
                cycleClass[6] = classes[4];
                cycleTeacher[6] = teachers[4];
                cycleRoom[6] = rooms[4];
            }
            cycleClass[7] = classes[5];
            cycleTeacher[7] = teachers[5];
            cycleRoom[7] = rooms[5];
            cycleClass[8] = classes[6];
            cycleTeacher[8] = teachers[6];
            cycleRoom[8] = rooms[6];
        }
        else if(a == 56)
        {
            cycleClass[0] = classes[0];
            cycleTeacher[0] = teachers[0];
            cycleRoom[0] = rooms[0];
            cycleClass[1] = classes[9];
            cycleTeacher[1] = teachers[9];
            cycleRoom[1] = rooms[9];
            cycleClass[2] = classes[1];
            cycleTeacher[2] = teachers[1];
            cycleRoom[2] = rooms[1];
            cycleClass[3] = classes[2];
            cycleTeacher[3] = teachers[2];
            cycleRoom[3] = rooms[2];
            cycleClass[4] = classes[3];
            cycleTeacher[4] = teachers[3];
            cycleRoom[4] = rooms[3];
            if(cycleDay[5].toString().equals("11:37-12:34"))
            {
                cycleClass[5] = classes[4];
                cycleTeacher[5] = teachers[4];
                cycleRoom[5] = rooms[4];
                cycleClass[6] = "Lunch";
                cycleTeacher[6] = "-----";
                cycleRoom[6] = "-----";
            }
            else
            {
                cycleClass[5] = "Lunch";
                cycleTeacher[5] = "-----";
                cycleRoom[5] = "-----";
                cycleClass[6] = classes[4];
                cycleTeacher[6] = teachers[4];
                cycleRoom[6] = rooms[4];
            }
            cycleClass[7] = classes[7];
            cycleTeacher[7] = teachers[7];
            cycleRoom[7] = rooms[7];
            cycleClass[8] = classes[8];
            cycleTeacher[8] = teachers[8];
            cycleRoom[8] = rooms[8];
        }
        else if(a == 34)
        {
            cycleClass[0] = classes[0];
            cycleTeacher[0] = teachers[0];
            cycleRoom[0] = rooms[0];
            cycleClass[1] = classes[9];
            cycleTeacher[1] = teachers[9];
            cycleRoom[1] = rooms[9];
            cycleClass[2] = classes[1];
            cycleTeacher[2] = teachers[1];
            cycleRoom[2] = rooms[1];
            cycleClass[3] = classes[2];
            cycleTeacher[3] = teachers[2];
            cycleRoom[3] = rooms[2];
            cycleClass[4] = classes[5];
            cycleTeacher[4] = teachers[5];
            cycleRoom[4] = rooms[5];
            if(cycleDay[5].toString().equals("11:37-12:34"))
            {
                cycleClass[5] = classes[6];
                cycleTeacher[5] = teachers[6];
                cycleRoom[5] = rooms[6];
                cycleClass[6] = "Lunch";
                cycleTeacher[6] = "-----";
                cycleRoom[6] = "-----";
            }
            else
            {
                cycleClass[5] = "Lunch";
                cycleTeacher[5] = "-----";
                cycleRoom[5] = "-----";
                cycleClass[6] = classes[6];
                cycleTeacher[6] = teachers[6];
                cycleRoom[6] = rooms[6];
            }
            cycleClass[7] = classes[7];
            cycleTeacher[7] = teachers[7];
            cycleRoom[7] = rooms[7];
            cycleClass[8] = classes[8];
            cycleTeacher[8] = teachers[8];
            cycleRoom[8] = rooms[8];
        }
        else if(a == 12)
        {
            cycleClass[0] = classes[0];
            cycleTeacher[0] = teachers[0];
            cycleRoom[0] = rooms[0];
            cycleClass[1] = classes[9];
            cycleTeacher[1] = teachers[9];
            cycleRoom[1] = rooms[9];
            cycleClass[2] = classes[3];
            cycleTeacher[2] = teachers[3];
            cycleRoom[2] = rooms[3];
            cycleClass[3] = classes[4];
            cycleTeacher[3] = teachers[4];
            cycleRoom[3] = rooms[4];
            cycleClass[4] = rooms[5];
            cycleTeacher[4] = teachers[5];
            cycleRoom[4] = rooms[5];
            if(cycleDay[5].toString().equals("11:37-12:34"))
            {
                cycleClass[5] = classes[6];
                cycleTeacher[5] = teachers[6];
                cycleRoom[5] = rooms[6];
                cycleClass[6] = "Lunch";
                cycleTeacher[6] = "-----";
                cycleRoom[6] = "-----";
            }
            else
            {
                cycleClass[5] = "Lunch";
                cycleTeacher[5] = "-----";
                cycleRoom[5] = "-----";
                cycleClass[6] = classes[6];
                cycleTeacher[6] = teachers[6];
                cycleRoom[6] = rooms[6];
            }
            cycleClass[7] = classes[7];
            cycleTeacher[7] = teachers[7];
            cycleRoom[7] = rooms[7];
            cycleClass[8] = classes[8];
            cycleTeacher[8] = teachers[8];
            cycleRoom[8] = rooms[8];
        }
    }

    public void saveSched(String s)
    {
        SharedPreferences sharedPref = getSharedPreferences("StudentSched", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("schedule", s);
        editor.putString("bLunch", bLunches);
        editor.commit();
    }
    public String readSched(String s)
    {
        SharedPreferences sharedPref = getSharedPreferences("StudentSched", Context.MODE_PRIVATE);
        if(s.equals("schedule"))
            return sharedPref.getString("schedule", "");
        else
            return sharedPref.getString("bLunch", "");
    }
}