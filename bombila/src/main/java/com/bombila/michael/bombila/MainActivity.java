package com.bombila.michael.bombila;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    TextView tvOnTime;
    TextView tvCost;
    TextView tvRadius;
    TextView tvDirs;
    TextView tvBochka;
    TextView tvBigRoute;
    Button btnChange;
    Button btnOnLine;
    Button btnPayment;

    SharedPreferences sp;
    String lastModified;
    String modifiedFile = "http://185.25.119.3/taxoid/bombila.apk";
    String scripts_host = "http://185.25.119.3/taxoid/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOnTime = (TextView) findViewById(R.id.tvOnTime);
        tvBochka = (TextView) findViewById(R.id.tvBochka);
        tvBigRoute = (TextView) findViewById(R.id.tvBigRoute);
        tvCost = (TextView) findViewById(R.id.tvCost);
        tvRadius = (TextView) findViewById(R.id.tvRadius);
        tvDirs = (TextView) findViewById(R.id.tvDirs);

        btnChange = (Button)findViewById(R.id.btnChange);
        btnOnLine = (Button)findViewById(R.id.btnOnLine);
        btnPayment = (Button)findViewById(R.id.btnPayment);
        btnChange.setOnClickListener(this);
        btnOnLine.setOnClickListener(this);
        btnPayment.setOnClickListener(this);

        sp = getSharedPreferences("bombila_pref",MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("scripts_host", scripts_host);
        ed.commit();

        if(checkUpdate()) {
            _UpdateApp app = new _UpdateApp();
            app.setContext(getApplicationContext());
            app.execute(modifiedFile);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChange:
                startActivity(new Intent(this, BombilaPreferencesActivity.class));
                break;
            case R.id.btnOnLine:
                startActivity(new Intent(this, OnLineActivity.class));
                break;
            case R.id.btnPayment:
                startActivity(new Intent(this, PaymentActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSharaPreferences();
    }

    void loadSharaPreferences() {
        tvCost.setText("Сумма: " + sp.getString("cost", ""));
        tvRadius.setText("Радиус: " + sp.getString("radius", ""));
        tvDirs.setText(sp.getString("dirs", " ").substring(1).replace(",",", "));

        if (sp.getBoolean("on_time", false)) { tvOnTime.setText("Предварительные: Да"); }
        else { tvOnTime.setText("Предварительные: Нет"); }

        if (sp.getBoolean("bochka", false)) { tvBochka.setText("Заказы из бочки: Да"); }
        else { tvBochka.setText("Заказы из бочки: Нет"); }

        if (sp.getBoolean("big_route", false)) { tvBigRoute.setText("Через адрес: Да"); }
        else { tvBigRoute.setText("Через адрес: Нет"); }
    }

    boolean checkUpdate() {
        lastModified = sp.getString("lastModified", "");
        try {
            String s = (new _LastModified()).execute(modifiedFile).get();
            if(lastModified.equals(s)) {
                return false;
            } else {
                lastModified = s;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("lastModified", lastModified);
        ed.commit();

        return true;
    }

}
