package ddwu.mobile.finalproject.ma02_20200943;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ddwu.mobile.finalproject.ma02_20200943.Weather.TranstoGrid;
import ddwu.mobile.finalproject.ma02_20200943.Weather.WeatherInfo;
import ddwu.mobile.finalproject.ma02_20200943.Weather.WeatherXMLParser;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ShowTodayActivity extends AppCompatActivity {
    private ListView lvPlans = null;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private TodayPlanArrayAdater adapter;
    private ArrayList<Plan> planList;
    PlanDB planDB;
    PlanDao planDao;
    FusedLocationProviderClient flpClient;
    Location mLastLocation;
    String weatherAPIAddress;
    String key;
    TextView tvLocation;
    Button getLocationBtn;
    TextView tvTDate;
    TextView tvTime;
    TextView tvTemp;
    TextView tvSKY;
    TextView tvPOP;
    TextView tvReh;
    LocalDate date;
    String nowDate;
    LocalTime localTime;
    int hour;
    //List<WeatherInfo> weatherInfos;
    String baseTime;
    double x, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_today);
        flpClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocation = findViewById(R.id.tvLocation);
        getLocationBtn = findViewById(R.id.getLocation);
        tvTDate = findViewById(R.id.tvTDate);
        tvTime = findViewById(R.id.tvTimeEx);
        tvTemp = findViewById(R.id.tvTmp);
        tvPOP = findViewById(R.id.tvPop);
        tvSKY = findViewById(R.id.tvSky);
        tvReh = findViewById(R.id.tvReh);
        weatherAPIAddress = getResources().getString(R.string.weatherAPIAddress);
        key = getResources().getString(R.string.wkey);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDate.now();
            localTime = LocalTime.now();
            hour = localTime.getHour();
            baseTime = mappingTime(hour);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            nowDate = date.format(formatter);

            tvTDate.setText(date + "의 날씨");
            tvTime.setText("*" + baseTime + "시 기준");
        }

        lvPlans = (ListView) findViewById(R.id.lvShowTodayPlans);
        planDB = PlanDB.getDatabase(getApplicationContext());
        planDao = planDB.planDao();

        planList = new ArrayList<>();
        adapter = new TodayPlanArrayAdater(this, R.layout.lv_plan, planList);
        lvPlans.setAdapter(adapter);
        lvPlans.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowTodayActivity.this);
                builder.setTitle("일정 삭제")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Plan plan = (Plan) adapter.getItem(position);
                                Completable deleteResult = planDao.deletePlan(plan);
                                mDisposable.add(
                                        deleteResult.subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()
                                );
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        });
        lvPlans.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), ShowPlanActivity.class);
                Plan plan = (Plan) lvPlans.getAdapter().getItem(position);
                i.putExtra("plan", plan);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mLastLocation!=null)
            executeGeocoding(mLastLocation);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            Flowable<List<Plan>> showResult = planDao.getTodayPlans(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            mDisposable.add(
                    showResult.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(plans -> {
                                planList.clear();
                                planList.addAll(plans);
                                adapter.notifyDataSetChanged();
                            }, throwable -> Log.d("Plan", "error", throwable)));
        }
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getLocation:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                flpClient.requestLocationUpdates(
                        getLocationRequest(),
                        mLocCallback,
                        Looper.getMainLooper()
                );
                if (baseTime.equals("2300")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        date = date.minusDays(1);
                    }
                    DateTimeFormatter formatter = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        nowDate = date.format(formatter);
                    }
                }
                getPresentLocation();
        }

    }



    //현재 위치 request 함수
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest()
                .setInterval(5000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    //현재 위치 callback 함수
    LocationCallback mLocCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location loc : locationResult.getLocations()) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                mLastLocation = loc;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        flpClient.removeLocationUpdates(mLocCallback);
    }

    /*현재 위치 구하는 함수*/
    public void getPresentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } //위치권한 있는지 체크
        flpClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            mLastLocation = location;
                        } else {
                            Toast.makeText(getApplicationContext(), "위치 정보 알 수 없음", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        flpClient.getLastLocation().addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }
        );
        executeGeocoding(mLastLocation);
    }
    //지오코딩 AsyncTask 부분
    class GeoTask extends AsyncTask<Location, Void, List<Address>> {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        @Override
        protected List<Address> doInBackground(Location... locations) {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(locations[0].getLatitude(),
                        locations[0].getLongitude(), 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            Address address = null;
            if(addresses!=null)
                address = addresses.get(0);
            if(address!=null){
                tvLocation.setText(address.getAddressLine(0));
                TranstoGrid transtoGrid = new TranstoGrid(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                TranstoGrid.transfer(transtoGrid, 0);
                x=transtoGrid.getxLat();
                y=transtoGrid.getyLon();

                new WeatherAsyncTask().execute(weatherAPIAddress);
            }
            else
                tvLocation.setText("현재 위치 알 수 없음");
        }
    }
    private void executeGeocoding(Location location) {
        if (Geocoder.isPresent()) {
            if (location != null)  new GeoTask().execute(location);
            else
                Toast.makeText(this, "위치 오류", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "위치 오류", Toast.LENGTH_SHORT).show();
        }
    }

    //날씨 API를 위한 시간 매핑
    public String mappingTime(int hour){
        /*0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)*/

        if(hour>2&&hour<=5) return "0200";
        else if(hour>5&&hour<=8) return "0500";
        else if(hour>8&&hour<=11) return "0800";
        else if(hour>11&&hour<=14) return "1100";
        else if(hour>14&&hour<=17) return "1400";
        else if(hour>17&&hour<=20) return "1700";
        else if(hour>20&&hour<=23) return "2000";
        else if(hour>-1&&hour<=2) return "2300";
        return null;
    }


    /*날씨 AsyncTask 부분*/
    class WeatherAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String address = strings[0] + "?serviceKey=" + key + "&base_date=" + nowDate + "&numOfRows=12&pageNo=1&" +
                    "base_time=" + baseTime + "&nx=" + Math.round(x) + "&ny=" + Math.round(y);
            String result = downloadContents(address);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            WeatherXMLParser parser = new WeatherXMLParser();
            List<WeatherInfo> weatherInfos = parser.parse(s);

            for(WeatherInfo w: weatherInfos){
                if(w.getCategory().equals("TMP"))
                    tvTemp.setText(w.getFcstValue()+"도씨");
                else if(w.getCategory().equals("SKY")){
                    switch (w.getFcstValue()){
                        case "1": tvSKY.setText("맑음"); break;
                        case "2": tvSKY.setText("구름많음"); break;
                        case "3": tvSKY.setText("흐림"); break;
                    }
                }else if(w.getCategory().equals("POP"))
                    tvPOP.setText(w.getFcstValue()+"%");
                else if(w.getCategory().equals("REH"))
                    tvReh.setText(w.getFcstValue()+"%");
            }

        }

        private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {

            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            return conn.getInputStream();
        }

        protected String downloadContents(String address) {
            HttpURLConnection conn = null;
            InputStream stream = null;
            String result = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();
                stream = getNetworkConnection(conn);
                result = readStreamToString(stream);
                if (stream != null) stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }

        /* InputStream을 전달받아 문자열로 변환 후 반환 */
        protected String readStreamToString(InputStream stream) {
            StringBuilder result = new StringBuilder();

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String readLine = bufferedReader.readLine();

                while (readLine != null) {
                    result.append(readLine + "\n");
                    readLine = bufferedReader.readLine();
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }
    }
}