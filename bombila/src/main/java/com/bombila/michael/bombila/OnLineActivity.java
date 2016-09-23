package com.bombila.michael.bombila;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class OnLineActivity extends AppCompatActivity implements OnClickListener {

    TextView tvNET;
    TextView tvGPS;
    TextView tvAddress;
    TextView tvBalance;
    TextView tvPilot;
    TextView tvSettings;
    TextView tvOrdersInfo;
    ListView lv;
    TextView tvAssign;
    Button btnOnPlace;
    Button btnOnRoad;
    Button btnCall;
    Button btnCancel;
    LinearLayout llOnLine;
    LinearLayout llOnPlace;

    int order_id = 0;
    int fmanid = 0;
    String session = "";
    String ftaxi = "";
    String os = "android";
    String android = "4.4.2";
    String number = "";
    int version = 8;
    String model = "";
    String imei = "";
    String mac = "";

    boolean clickBtnCancel = false;
    boolean clickBtnOnPlace = false;
    boolean clickBtnOnRoad = false;

    boolean settings = true;
    boolean pilot = false;
    boolean on_time = false;
    boolean bochka = false;
    boolean big_route = false;

    double latitude = 0.0;
    double longitude = 0.0;
    long locationTime = 0L;
    double currlatitude = 0.0;
    double currlongitude = 0.0;
    long currlocationTime = 0L;
    String scripts_host = "";
    String callNumber = "";
    String login = "";
    String password = "";
    String user = "";
    String referer = "";
    String sdirs = "";
    Double cost = 0.0;
    Double radius = 0.0;
    Double pay = 0.0;
    Double balance = 0.0;
    _IdLtLn dirsLtLn[] = null;
    JSONArray dirCoords = new JSONArray();
    long deltaTime = 10800L;

    ArrayList<Map<String, String>> dataLv = new ArrayList<Map<String, String>>();
    SimpleAdapter sAdapter = null;

    MediaPlayer mp = null;
    SharedPreferences sp;
    private LocationManager locationManager;
    Daemon daemon = new Daemon();

//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//--------------------------------------------------------------------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_line);

        mp = MediaPlayer.create(this, R.raw.order_accepted);

        tvBalance = (TextView) findViewById(R.id.tvBalance);
        tvPilot = (TextView) findViewById(R.id.tvPilot);
        tvNET = (TextView) findViewById(R.id.tvNET);
        tvGPS = (TextView) findViewById(R.id.tvGPS);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvSettings = (TextView) findViewById(R.id.tvSettings);
        tvOrdersInfo = (TextView) findViewById(R.id.tvOrdersInfo);
        lv = (ListView) findViewById(R.id.lv);
        tvAssign = (TextView) findViewById(R.id.tvAssign);
        btnOnPlace = (Button) findViewById(R.id.btnOnPlace);
        btnOnRoad = (Button) findViewById(R.id.btnOnRoad);
        btnCall = (Button) findViewById(R.id.btnCall);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        llOnLine = (LinearLayout) findViewById(R.id.llOnLine);
        llOnPlace = (LinearLayout) findViewById(R.id.llOnPlace);

        tvPilot.setOnClickListener(this);
        tvAddress.setOnClickListener(this);
        tvOrdersInfo.setOnClickListener(this);
        btnOnPlace.setOnClickListener(this);
        btnOnRoad.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sp = getSharedPreferences("bombila_pref",MODE_PRIVATE);
        loadSharaPreferences();

        model = Build.MANUFACTURER + " " + Build.MODEL;
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mac = wifiManager.getConnectionInfo().getMacAddress();

        String settings = "Предварительные: ";
        if(on_time) { settings += "ДА\n"; } else { settings += "НЕТ\n"; }
        settings += "Заказы из бочки: ";
        if(bochka) { settings += "ДА\n"; } else { settings += "НЕТ\n"; }
        settings += "Через адрес: ";
        if(big_route) { settings += "ДА\n"; } else { settings += "НЕТ\n"; }
        settings += "Сумма: " + String.valueOf(cost) + " грн." + "\n";
        settings += "Радиус: " + String.valueOf(radius) + " км" + "\n";
        settings += sdirs.replace(",",", ");
        tvSettings.setText(settings);
        dirsLtLn = _Sector.getDirsLtLn(sdirs);

        try {
            for (int i=0; i<dirsLtLn.length; i++) {
                JSONArray coords = new JSONArray();
                coords.put(0, dirsLtLn[i].lat);
                coords.put(1, dirsLtLn[i].lon);
                dirCoords.put(coords);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] from = { "address","distance","time", "fot", "price", "route"};
        int[] to = { R.id.tvOrderAddress, R.id.tvOrderDistance, R.id.tvOrderTime,
                            R.id.tvOrderBochka, R.id.tvOrderPrice, R.id.tvOrderRoute};

        sAdapter = new SimpleAdapter(this, dataLv, R.layout.item, from, to);
        lv.setAdapter(sAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> m = dataLv.get(position);
                String a = m.get("address");
                order_id = Integer.parseInt(m.get("order_id"));
                Log.d("MyLogs ===>", "address = " + a + ", order_id = " + order_id);
            }
        });




        daemon.execute();

    }
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
//--------------------------------------------------------------------------------------------------
        super.onResume();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000 * 10, 10, locationListener);
        } catch(SecurityException e){}
    }

//--------------------------------------------------------------------------------------------------
    @Override
    protected void onPause() {
//--------------------------------------------------------------------------------------------------
        super.onPause();
        try {
            locationManager.removeUpdates(locationListener);
        } catch(SecurityException e){}
    }

//--------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
//--------------------------------------------------------------------------------------------------
        super.onBackPressed();
        daemon.cancel(false);
    }

//--------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
//--------------------------------------------------------------------------------------------------
        switch (v.getId()) {

            case R.id.tvPilot:
                if (pilot) {
                    pilot = false;
                    tvPilot.setTextColor(0xffcc0000);
                } else {
                    pilot = true;
                    tvPilot.setTextColor(0xFF99CC00);
                }
                break;

            case R.id.tvAddress:
                if (settings) {
                    settings = false;
                    tvSettings.setVisibility(View.GONE);
                } else  {
                    settings = true;
                    tvSettings.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.tvOrdersInfo:
                if (settings) {
                    settings = false;
                    tvSettings.setVisibility(View.GONE);
                } else  {
                    settings = true;
                    tvSettings.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btnCancel:
                btnCancel.setVisibility(View.GONE);
                clickBtnCancel = true;
                break;

            case R.id.btnOnPlace:
                btnOnPlace.setVisibility(View.GONE);
                btnOnRoad.setVisibility(View.VISIBLE);
                clickBtnOnPlace = true;
                break;

            case R.id.btnOnRoad:
                btnOnPlace.setVisibility(View.GONE);
                btnOnRoad.setVisibility(View.GONE);
                clickBtnOnRoad = true;
                break;

            case R.id.btnCall:
                if (callNumber != null) {
                    String number = String.format("tel:%s", callNumber);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(number)));
                }
                break;

            default:
                break;
        }
    }

//--------------------------------------------------------------------------------------------------
    void loadSharaPreferences() {
//--------------------------------------------------------------------------------------------------
        scripts_host = sp.getString("scripts_host", "");
        login = sp.getString("login", "");
        password = sp.getString("password", "");
        cost = Double.valueOf(sp.getString("cost", "0.0"));
        radius = Double.valueOf(sp.getString("radius", "1.5"));
        sdirs = sp.getString("dirs", ",").substring(1);
        on_time = sp.getBoolean("on_time", false);
        bochka = sp.getBoolean("bochka", false);
        big_route = sp.getBoolean("big_route", false);
    }

//--------------------------------------------------------------------------------------------------
    String getAddress(){
//--------------------------------------------------------------------------------------------------
        if(currlatitude==0.0 || currlongitude==0.0) { return null; }
        Geocoder coder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String address = null;
        try {
            addresses = coder.getFromLocation(currlatitude, currlongitude, 1);
            if (addresses==null || addresses.size()==0) { return null; }
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

//**************************************************************************************************
    class Daemon extends AsyncTask<Void, String, Void> {
//**************************************************************************************************
        Socket socket = null;
        protected static final String server_IP = "94.153.161.234";
        private static final int server_Port = 10000;

        String count = "";
        String response = "null";
        String status = "null";
        String type = "null";
        String action_response = "";
        JSONArray orders = new JSONArray();
        JSONArray self_orders = new JSONArray();
        JSONArray deleted_orders = new JSONArray();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... values) {

            init();
            initSocket();

            publishProgress("balance");
            if (balance < 0) {
                publishProgress("badBalance", "У Вас отрицательный баланс: " +
                        String.valueOf(balance) + "\nПополните счет.");
                finish();
                return null;
            }

            auth();
            response = "get_orders";

            while (true) {
                if(isCancelled()) {
                    Log.d("Canceled ====>>>","true");
                    break;
                }
                double distance = _Sector.getDistance(currlatitude, currlongitude, latitude, longitude);
                if (distance > 1.0) {
                    latitude = currlatitude;
                    longitude = currlongitude;
                    locationTime = currlocationTime;
                }
                if (latitude==0.0 || longitude==0.0){ continue; }

//===> get_orders
                if (response.equals("get_orders")) {
                    getOrders();
                    //bombilaScript();
                    continue;
                }

//===> assign
                if (response.equals("assign")) {
                    type = "assign";
                    if (status.equals("null")) {
                        revise();
                        continue;
                    }
                    if (status.equals("success")) {
                        action("state");
                        continue;
                    }
                    response = "get_orders";
                    continue;
                }

//===> revise
                if (response.equals("revise")) {
                    if (status.equals("null")) {
                        revise();
                        continue;
                    }
                    if (status.equals("success")) {
                        if (type.equals("assign")) action("state");
                        continue;
                    }
                    response = "get_orders";
                    continue;
                }

//===> state
                if (response.equals("state")) {
                    action("state");
                    continue;
                }

//===> cancel
                if (response.equals("cancel")) {
                    continue;
                }

//===> point_a
                if (response.equals("point_a")) {
                    //if (status.equals("null")) revise();
                    //continue;
                }

                if (clickBtnCancel) {
                    publishProgress("cancel");
//                    action("cancel");
                    clickBtnCancel = false;
                }
                if (clickBtnOnPlace) {
                    clickBtnOnPlace = false;
                }
                if (clickBtnOnRoad) {
                    clickBtnOnRoad = false;
                }

                try { TimeUnit.MILLISECONDS.sleep(100); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }

            finish();

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values[0].equals("balance")) {
                double b = new BigDecimal(balance).setScale(1, RoundingMode.UP).doubleValue();
                tvBalance.setText(tvBalance.getText() + String.valueOf(b));
            }
            if(values[0].equals("badBalance")) {
                Toast toast = Toast.makeText(OnLineActivity.this,
                        values[1], Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if(values[0].equals("badLogin")) {
                Toast toast = Toast.makeText(OnLineActivity.this,
                        "Неверный Логин или Пароль", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if(values[0].equals("show_orders")) {
                showOrders();
                deleteOrdersFromBochka();
            }
            if(values[0].equals("assign")) {
                if (pilot) mp.start();
                pilot = false;
                tvPilot.setTextColor(0xffcc0000);
                llOnLine.setVisibility(View.GONE);
                llOnPlace.setVisibility(View.VISIBLE);
                tvAssign.setText(String.valueOf(order_id));
            }
            if(values[0].equals("cancel")) {
                if (pilot) mp.start();
                pilot = false;
                tvPilot.setTextColor(0xffcc0000);
                llOnLine.setVisibility(View.GONE);
                llOnPlace.setVisibility(View.VISIBLE);
                tvAssign.setText(String.valueOf(order_id));
            }
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
        void initSocket() {
            try {
                socket = new Socket(server_IP, server_Port);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        void action(String action) {
            publishProgress(action);
/*
            JSONObject obj = new JSONObject();
            try {
                obj.put("action", action);
                obj.put("os", os);
                obj.put("ftaxi", ftaxi);
                obj.put("order_id", order_id);
                obj.put("session", session);
                obj.put("fmanid", fmanid);
                obj.put("version", version);

                sendToSocket(obj.toString());
                action_response = getFromSocket();

                JSONObject jresp = new JSONObject(action_response);
                response = jresp.getString("response");
                if (jresp.has("status")) status = jresp.getString("status");
                if (jresp.has("type")) type = jresp.getString("type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
*/
        }
        void revise() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("action", "revise");
                obj.put("order_id", order_id);
                obj.put("type", type);
                obj.put("session", session);
                obj.put("fmanid", fmanid);
                obj.put("version", version);
                sendToSocket(obj.toString());
                String resp = getFromSocket();
                JSONObject o = new JSONObject(resp);
                response = o.getString("response");
                status = o.getString("status");
                type = o.getString("type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void  init() {
            String script = scripts_host + "init.php";
            String _user = toScript(script, login, password);
            if (_user.equals("error")) {
                publishProgress("badLogin");
                daemon.cancel(false);
                finish();
                return;
            }
            JSONObject juser;
            String sBal = "";
            String sPay = "";

            try {
                juser = new JSONObject(_user);
                sBal = juser.getString("balance");
                sPay = juser.getString("pay");
                user = juser.getString("user");
                referer = juser.getString("referer");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            balance = Double.parseDouble(sBal);
            pay = Double.parseDouble(sPay);
        }
        String toScript(String... args) {
            String resultString = "";
            String pars = "";
            for (int i=1; i<args.length; i++) {
                if (i == args.length-1) {
                    pars += "par" + String.valueOf(i) + "=" + args[i];
                } else {
                    pars += "par" + String.valueOf(i) + "=" + args[i] + "&";
                }
            }
            try {
                URL url = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                byte[] data = pars.getBytes("UTF-8");
                os.write(data); os.flush(); os.close();

                data = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) { baos.write(buffer, 0, bytesRead); }
                data = baos.toByteArray();
                baos.flush(); baos.close(); is.close();
                resultString = new String(data, "UTF-8");
                conn.disconnect();
            } catch (MalformedURLException e) { resultString = "MalformedURLException:" + e.getMessage();
            } catch (IOException e) { resultString = "IOException:" + e.getMessage();
            } catch (Exception e) { resultString = "Exception:" + e.getMessage();
            }
            return resultString;
        }
        void auth() {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            JSONObject data = new JSONObject();

            try {
                data.put("login", login);
                data.put("password", password);
                arr.put(0, data);

                obj.put("os", os);
                obj.put("model", model);
                obj.put("android", android);
                obj.put("imei", imei);
                obj.put("data", arr);
                obj.put("action", "get_session");
                obj.put("mac", mac);
                obj.put("number", number);
                obj.put("version", version);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendToSocket(obj.toString());
            String s = getFromSocket();

            try {
                JSONObject auth = new JSONObject(s);
                fmanid = auth.getJSONArray("data").getJSONObject(0).getInt("fmanid");
                session = auth.getJSONArray("data").getJSONObject(0).getString("session");
                ftaxi = auth.getJSONArray("money").getJSONObject(0).getString("ftaxi");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void sendToSocket(String request) {
            try {
                socket.getOutputStream().write(request.getBytes());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String getFromSocket() {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            int prevChar = 0;
            try {
                prevChar = socket.getInputStream().read();
                int curChar = socket.getInputStream().read();
                while (true) {
                    if (prevChar == 13 && curChar == 10) {
                        break;
                    }
                    os.write(prevChar);
                    prevChar = curChar;
                    curChar = socket.getInputStream().read();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return os.toString();
        }
        void getOrders() {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            JSONObject data = new JSONObject();

            try {
                data.put("self_orders", self_orders);
                data.put("session", session);
                data.put("fmanid", fmanid);
                arr.put(0, data);

                obj.put("data", arr);
                obj.put("action", "get_orders");
                obj.put("version", version);
                sendToSocket(obj.toString());
                String resp = getFromSocket();
                JSONObject jresp = new JSONObject(resp);
                response = jresp.getString("response");
                if (response.equals("get_orders")) {
                    updateOrders(resp);
                    publishProgress("show_orders");
                }
                if (pilot) {
                    String o = jresp.getJSONArray("data").getJSONObject(0).getString("order");
                    if (o.equals("null")) {
                        int i;
                        for (int j = 0; j < orders.length(); j++) {
                            JSONObject order = orders.getJSONObject(j);
                            order_id = order.getInt("FID");
                            int n = order.getJSONObject("FCOST_DATA").getJSONArray("Coord").length();
                            if (!bochka && !order.getString("FOT").equals(ftaxi)) continue;
                            if (!on_time && !order.getString("FPDATE").equals("null")) continue;
                            if (!big_route && n > 2) continue;
                            if (order.getDouble("distance") <= radius) {
                                JSONObject fcost_data = order.getJSONObject("FCOST_DATA");
                                double order_cost = 0.0;
                                if (fcost_data.has("cost_s")) {
                                    String scost = fcost_data.getString("cost_s");
                                    order_cost = Double.parseDouble(scost.split("грн")[0]);
                                }
                                if (order_cost >= cost) {
                                    int count = dirsLtLn.length;
                                    if (count > 0 && n > 1) {
                                        for (i = 0; i < count; i++) {
                                            JSONArray Coord = fcost_data.getJSONArray("Coord");
                                            JSONArray order_dir_coords = Coord.getJSONArray(n - 1);
                                            double distance = _Sector.getDistance(
                                                    dirsLtLn[i].lat,
                                                    dirsLtLn[i].lon,
                                                    order_dir_coords.getDouble(0),
                                                    order_dir_coords.getDouble(1));
                                            if (distance <= radius) {
                                                action("assign");
                                                break;
                                            }
                                        }
                                    } else {
                                        action("assign");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void bombilaScript() {
            JSONObject data = new JSONObject();
            JSONArray myCoods = new JSONArray();
            try {
                myCoods.put(0, latitude);
                myCoods.put(1, longitude);

                data.put("os", os);
                data.put("ftaxi", ftaxi);
                data.put("session", session);
                data.put("fmanid", fmanid);
                data.put("version", version);
                data.put("self_orders", self_orders);
                data.put("my_coords", myCoods);
                data.put("pilot", pilot);
                data.put("bochka", bochka);
                data.put("on_time", on_time);
                data.put("big_route", big_route);
                data.put("cost", cost);
                data.put("radius", radius);
                data.put("dir_coords", dirCoords);

                String script = scripts_host + "bombila.php";
                String server = "tcp://" + server_IP + ":" + server_Port;
                String resp = toScript( script, server, data.toString());
                JSONObject obj = new JSONObject(resp);
                response = obj.getString("response");
                if (response.equals("get_orders")) {
                    if (user.equals("букрей")) count = " [" + obj.getInt("i") + "]";
                    updateOrders(resp);
                    publishProgress("show_orders");
                }
                if (response.equals("assign")) {
                    order_id = obj.getInt("order_id");
                    status = obj.getString("status");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void updateOrders(String resp) {
            try {
                JSONObject obj = new JSONObject(resp);
                deleted_orders = obj.
                        getJSONArray("data").getJSONObject(0).getJSONArray("deleted_orders");
                JSONArray new_orders;
                new_orders = obj.getJSONArray("data").getJSONObject(0).getJSONArray("orders");
                for (int i=0; i<new_orders.length(); i++) {
                    JSONObject new_order = new_orders.getJSONObject(i);
                    orders.put(new_order);
/*
                    boolean b = true;
                    for (int j=0; j<orders.length(); j++) {
                        int order_fid = orders.getJSONObject(j).getInt("FID");
                        int new_order_fid = new_order.getInt("FID");
                        if (order_fid == new_order_fid) b = false;
                    }
                    if(b) orders.put(new_order);
*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            deleteOrders();
            addDistanceToOrders();
            getSelfOrders();
        }
        void getSelfOrders() {
            try {
                JSONArray self = new JSONArray();
                for (int i=0; i<orders.length(); i++) {
                    JSONObject obj = orders.getJSONObject(i);
                    String fot = obj.getString("FOT");
                    if (ftaxi.equals(fot)) {
                        Integer fid = obj.getInt("FID");
                        String uhash = obj.getString("UHASH");
                        self.put(String.valueOf(fid) + ":" + uhash);
                    }
                }
                self_orders = self;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void deleteOrdersFromBochka() {
            JSONArray arr = new JSONArray();
            try {
                for (int i=0; i<orders.length(); i++) {
                    JSONObject obj = orders.getJSONObject(i);
                    if (!obj.getString("FOT").equals(ftaxi)) continue;
                    arr.put(obj);
                }
                orders = arr;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void deleteOrders() {
            try {
                for (int i=0; i<deleted_orders.length(); i++) {
                    JSONArray arr = new JSONArray();
                    for (int j=0; j<orders.length(); j++) {
                        JSONObject order = orders.getJSONObject(j);
                        String fid = String.valueOf(order.getString("FID"));
                        if (deleted_orders.getString(i).equals(fid)) continue;
                        arr.put(order);
                    }
                    orders = arr;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void addDistanceToOrders() {
            try {
                for (int i=0; i<orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);
                    JSONArray arr = order.getJSONObject("FCOST_DATA").getJSONArray("Coord");
                    double lat = arr.getJSONArray(0).getDouble(0);
                    double lon = arr.getJSONArray(0).getDouble(1);
                    double d = _Sector.getDistance(latitude, longitude, lat, lon);
                    orders.getJSONObject(i).put("distance", d);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        void showOrders() {
            dataLv.clear();
            int length = orders.length();
            tvOrdersInfo.setText("Заказов: " +  String.valueOf(length) + count);

            Map<String, String> m;
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject obj = orders.getJSONObject(i);
                    String order_id = obj.getString("FID");
                    String address = obj.getString("FAD_STR");
                    if (!obj.getString("FAD_H").equals("null")) address += " " + obj.getString("FAD_H");
                    if (!obj.getString("FAD_PO").equals("null")) address += " " + obj.getString("FAD_PO");
                    String fpdate = obj.getString("FPDATE");
                    String time = "";
                    if (fpdate != "null") {
                        long ltime = (Long.parseLong(fpdate) - deltaTime) * 1000;
                        SimpleDateFormat spf = new SimpleDateFormat("HH:mm");
                        time = "[" + spf.format(new Date(ltime)) + "]";
                    }
                    String price = "";
                    if (obj.getJSONObject("FCOST_DATA").has("cost_s")) {
                        price = obj.getJSONObject("FCOST_DATA").getString("cost_s");
                    }
                    double d = new BigDecimal(obj.getDouble("distance")).
                                    setScale(2, RoundingMode.UP).doubleValue();
                    String dist = String.valueOf(d);
                    String distance = "(" + dist + " )";
                    if (d < 10.00) dist = "0" + dist;
                    String fot = "";
                    if (!obj.getString("FOT").equals(ftaxi)) fot = "[Б]";
                    String route = "";
                    JSONArray fad_route = obj.getJSONArray("FAD_ROUTE");
                    if (fad_route != null) {
                        for (int j = 0; j < fad_route.length(); j++) {
                            route += "=>" + fad_route.getString(j) + " ";
                        }
                    }
                    m = new HashMap<String, String>();
                    m.put("order_id", order_id);
                    m.put("address", address);
                    m.put("dist", dist);
                    m.put("distance", distance);
                    m.put("time", time);
                    m.put("fot", fot);
                    m.put("price", price);
                    m.put("route", route);
                    dataLv.add(m);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(dataLv, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> m1, Map<String, String> m2) {
                    String d1 = m1.get("dist");
                    String d2 = m2.get("dist");
                    return d1.compareTo(d2);
                }
            });
            sAdapter.notifyDataSetChanged();
        }

    }
//**************************************************************************************************
    private LocationListener locationListener = new LocationListener() {
//**************************************************************************************************
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;
            currlatitude = location.getLatitude();
            currlongitude = location.getLongitude();
            currlocationTime = location.getTime();
            String addr = getAddress();
            if(addr != null) {
                tvAddress.setText(addr);
            }
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                tvGPS.setTextColor(0xFF99CC00);
                if(addr == null) {
                    String gps = "GPS: " + String.valueOf(currlatitude) + ", "
                            + String.valueOf(currlongitude);
                    tvAddress.setText(gps);
                }
            }
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                tvNET.setTextColor(0xFF99CC00);
                if (addr == null) {
                    String net = "NETWORK: " + String.valueOf(currlatitude) + ", "
                            + String.valueOf(currlongitude);
                    tvAddress.setText(net);
                }
            }
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
}