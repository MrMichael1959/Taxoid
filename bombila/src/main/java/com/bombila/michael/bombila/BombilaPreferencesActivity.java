package com.bombila.michael.bombila;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class BombilaPreferencesActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etLogin;
    EditText etPassword;
    CheckBox cbOnTime;
    CheckBox cbBochka;
    CheckBox cbBigRoute;
    EditText etRadius;
    EditText etCost;
    TextView tvDirs;
    Button btnDirs;

    SharedPreferences sp;

    final String LOGIN = "login";
    final String PASSWORD = "password";
    final String ON_TIME = "on_time";
    final String BOCHKA = "bochka";
    final String BIG_ROUTE = "big_route";
    final String COST = "cost";
    final String RADIUS = "radius";
    final String DIRS = "dirs";
    final String BOMBILA_PREF = "bombila_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bombila_preferences);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        cbOnTime = (CheckBox) findViewById(R.id.cbOnTime);
        cbBochka = (CheckBox) findViewById(R.id.cbBochka);
        cbBigRoute = (CheckBox) findViewById(R.id.cbBigRoute);
        etCost = (EditText) findViewById(R.id.etCost);
        etRadius = (EditText) findViewById(R.id.etRadius);
        tvDirs = (TextView) findViewById(R.id.tvDirs);
        btnDirs = (Button) findViewById(R.id.btnDirs);

        btnDirs.setOnClickListener(this);
        sp = getSharedPreferences(BOMBILA_PREF,MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, MyDirectionsActivity.class));
    }

    @Override
    protected void onPause() {
        saveSharaPreferences();
        super.onPause();
    }

    @Override
    protected void onResume() {
        loadSharaPreferences();
        super.onResume();
    }

    void saveSharaPreferences() {
        Editor ed = sp.edit();
        ed.putString(LOGIN, etLogin.getText().toString());
        ed.putString(PASSWORD, etPassword.getText().toString());
        ed.putBoolean(ON_TIME, cbOnTime.isChecked());
        ed.putBoolean(BOCHKA, cbBochka.isChecked());
        ed.putBoolean(BIG_ROUTE, cbBigRoute.isChecked());

        String s = etCost.getText().toString();
        if(s.equals("") || s==null) s = "0.0";
        ed.putString(COST, s);

        s = etRadius.getText().toString();
        if(s.equals("") || s==null) s = "0.0";
        ed.putString(RADIUS, s);

        ed.commit();
    }

    void loadSharaPreferences() {
        etLogin.setText(sp.getString(LOGIN, ""));
        etPassword.setText(sp.getString(PASSWORD, ""));
        cbOnTime.setChecked(sp.getBoolean(ON_TIME, false));
        cbBochka.setChecked(sp.getBoolean(BOCHKA, false));
        cbBigRoute.setChecked(sp.getBoolean(BIG_ROUTE, false));
        etCost.setText(sp.getString(COST, ""));
        etRadius.setText(sp.getString(RADIUS, ""));
        tvDirs.setText(sp.getString(DIRS, " ").substring(1).replace(",",", "));
    }

}
