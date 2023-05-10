package ddwu.mobile.finalproject.ma02_20200943;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.PlaceTypes;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdatePlanActivity extends AppCompatActivity {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    final static String TAG = "UpdatePlanActivity";
    PlanDB planDB;
    PlanDao planDao;
    AlarmManager alarmManager = null;
    PendingIntent pendingIntent = null;
    TextView planTitle;
    TimePicker planTimePicker;
    DatePicker planDatePicker;
    TextView planDetail;
    TextView planLocation;
    SearchView planPlace;
    Button button;
    Plan plan;
    private GoogleMap mGoogleMap;       // 지도 객체
    private Marker mMarker;         // 위치 표시 Marker
    int pyear;
    int pmonth;
    int pday;
    int phourOfDay;
    int pminute;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_plan);
        planTitle = findViewById(R.id.updatePlanTitle);
        planDatePicker = findViewById(R.id.updatePlanDate);
        planTimePicker = findViewById(R.id.updatePlanTime);
        planDetail = findViewById(R.id.updatePlanDetails);
        planLocation = findViewById(R.id.updateplanLocation);
        planPlace = findViewById(R.id.updatetPlanPlace);
        planPlace.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new GeoTask().execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        button = findViewById(R.id.btnUpdatePlan);
        Intent intent = getIntent();
        planDB = PlanDB.getDatabase(getApplicationContext());
        planDao = planDB.planDao();
        plan = (Plan) intent.getSerializableExtra("plan");
        pyear = plan.getYear();
        pmonth = plan.getMonth();
        pday = plan.getDay();
        phourOfDay = plan.getHourOfDay();
        pminute = plan.getMinute();
        planTitle.setText(plan.getTitle());
        planDatePicker.init(plan.getYear(), plan.getMonth() - 1, plan.getDay(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                pyear = year;
                pmonth = monthOfYear+1;
                pday = dayOfMonth;
            }
        });

        planTimePicker.setHour(plan.getHourOfDay());
        planTimePicker.setMinute(plan.getMinute());
        planTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                phourOfDay = hourOfDay;
                pminute = minute;
            }
        });
        planDetail.setText(plan.getDetails());
        planLocation.setText(plan.getPlace());

        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.planMap3);
        mapFragment.getMapAsync(mapReadyCallback);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mGoogleMap = googleMap;
//            지도 초기 위치 이동

            LatLng latLng = new LatLng(plan.getLatitude(), plan.getLogitude());

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(plan.getPlace())
                    .position(latLng);
            mMarker = mGoogleMap.addMarker(markerOptions);
            mMarker.showInfoWindow();
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    planLocation.setText(marker.getTitle());
                }
            });
        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUpdatePlan:
                if (planTitle.getText().toString() == "") {
                    Toast.makeText(getApplicationContext(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pyear == 0 || pday == 0 || pmonth == 0) {
                    Toast.makeText(getApplicationContext(), "날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                plan.setTitle(planTitle.getText().toString());
                plan.setYear(pyear);
                plan.setMonth(pmonth);
                plan.setDay(pday);
                plan.setHourOfDay(phourOfDay);
                plan.setMinute(pminute);
                plan.setPlace(planLocation.getText().toString());
                plan.setDetails(planDetail.getText().toString());
                plan.setLatitude(latitude);
                plan.setLogitude(longitude);
                Completable deletePlan = planDao.updatePlan(plan);
                mDisposable.add(deletePlan.subscribeOn(Schedulers.io())
                        .subscribe(() -> Log.d(TAG, "deletion success"),
                                throwable -> Log.d(TAG, "error"))
                );
                setAlarm();
                finish();

            case R.id.btnUpdateCanceled:
                finish();
        }

    }
    public void setAlarm () {
        Intent newAlarmIntent = new Intent(this, BrReceiver.class);
        newAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        newAlarmIntent.putExtra("planTitle", planTitle.getText().toString());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, phourOfDay);
        calendar.set(Calendar.MINUTE, pminute);
        calendar.set(Calendar.YEAR, pyear);
        calendar.set(Calendar.MONTH, pmonth-1);
        calendar.set(Calendar.DATE, pday);
        calendar.add(Calendar.HOUR_OF_DAY, -1); //5분 전 알람 울리기

        int code = plan.getAlarmCode();
        pendingIntent = PendingIntent.getBroadcast(this, code, newAlarmIntent, PendingIntent.FLAG_MUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    class GeoTask extends AsyncTask<String , Void, List<Address>> {
        //        Geocoder 객체 생성
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        MarkerOptions options;
        @Override
        protected List<Address> doInBackground(String... spotName) {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(spotName[0], 3);
                options = new MarkerOptions();
                options.title(spotName[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            LatLng latLng;
            if(addresses !=null && addresses.size()>0) {
                Address address = addresses.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                options.position(latLng)
                        .snippet(address.getAddressLine(0))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mGoogleMap.addMarker(options).showInfoWindow();

            }else{
                Toast.makeText(getApplicationContext(), "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
            }
        }

    }

}