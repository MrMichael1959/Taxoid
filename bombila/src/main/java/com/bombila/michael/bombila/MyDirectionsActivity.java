package com.bombila.michael.bombila;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MyDirectionsActivity extends Activity {

    ListView lv;
    String[] dirs;
    SharedPreferences sp;
    final String DIRS = "dirs";
    final String BOMBILA_PREF = "bombila_pref";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_directions);

        lv = (ListView) findViewById(R.id.lv);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.directions, android.R.layout.simple_list_item_multiple_choice);
        lv.setAdapter(adapter);
        dirs = getResources().getStringArray(R.array.directions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        String s = "";
        SparseBooleanArray sbArray = lv.getCheckedItemPositions();
        for (int i = 0; i < sbArray.size(); i++) {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key)) { s += "," + dirs[key]; }
        }
        if(s.equals("")) {
            s = ",";
        }
        sp = getSharedPreferences(BOMBILA_PREF,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(DIRS, s);
        ed.commit();
    }

}