package ddwu.mobile.finalproject.ma02_20200943;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ddwu.mobile.place.placebasic.PlaceBasicManager;
import ddwu.mobile.place.placebasic.pojo.PlaceBasic;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddPlanActivity extends AppCompatActivity {
    static int ALRAM_REQ_CODE = 101;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    EditText etTitle;
    TimePicker timePicker;
    DatePicker datePicker;
    SearchView searchView;
    TextView tvLocation;
    int pyear;
    int pmonth;
    int pday;
    int phourOfDay;
    int pminute;
    String placeName;
    PlanDB planDB;
    PlanDao planDao;
    EditText etDetails;
    private GoogleMap mGoogleMap;       // 지도 객체

    private Marker searchMarker;
    PlacesClient placesClient;
    AlarmManager alarmManager = null;
    PendingIntent pendingIntent = null;
    PlaceBasicManager placeBasicManager;
    double lat; double longi;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);
        planDB = PlanDB.getDatabase(getApplicationContext());
        planDao = planDB.planDao();
        etTitle = findViewById(R.id.planTitle);
        etDetails = findViewById(R.id.planDetails);
        datePicker = findViewById(R.id.planDate);
        timePicker = findViewById(R.id.planTime);

        Date date = new Date();
        pyear = date.getYear()+1900;
        pmonth = date.getMonth()+1;
        pday = date.getDate();

        searchView = (SearchView) findViewById(R.id.planPlace);
        //flpClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocation = findViewById(R.id.planLocation);
        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.planMap);
        mapFragment.getMapAsync(mapReadyCallback);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        placeBasicManager = new PlaceBasicManager(getString(R.string.api_key));
        placeBasicManager.setOnPlaceBasicResult(list -> {
            for (PlaceBasic placeBasic : list) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(placeBasic.getLatitude(), placeBasic.getLongitude()));
                markerOptions.title(placeBasic.getName());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                Marker marker = mGoogleMap.addMarker(markerOptions);
                marker.setTag(placeBasic.getPlaceId());
                marker.showInfoWindow();
            }
        });
        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        placesClient = Places.createClient(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    pyear = year;
                    pmonth = monthOfYear + 1;
                    pday = dayOfMonth;
                }
            });

            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    phourOfDay = hourOfDay;
                    pminute = minute;
                }
            });
        }
    }

    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mGoogleMap = googleMap;
//            지도 초기 위치 이동
            LatLng latLng = new LatLng(37.606320, 127.041808);

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    if(marker.getTag()==null)
                        tvLocation.setText(marker.getTitle());
                    else
                        getPlaceDetail((String) marker.getTag());
                }
            });
        }
    };

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnAddPlan:
                if(etTitle.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }else if(pyear==0 || pday == 0 || pmonth ==0){
                    Toast.makeText(getApplicationContext(), "날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Plan plan = new Plan();
                plan.setTitle(etTitle.getText().toString());
                plan.setYear(pyear);
                plan.setMonth(pmonth);
                plan.setDay(pday);
                plan.setHourOfDay(phourOfDay);
                plan.setMinute(pminute);
                plan.setPlace(placeName);
                plan.setDetails(etDetails.getText().toString());
                plan.setLatitude(lat);
                plan.setLogitude(longi);
                plan.setAlarmCode(ALRAM_REQ_CODE);
                Single<Long> insertResult = planDao.insertPlans(plan);
                mDisposable.add(insertResult.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> Log.d("TAG", "Insertion sucess"+result), throwable -> Log.d("TAG", "error")));
                setAlarm();
                finish();
                break;
            case R.id.btnCanceled:
                finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void searchLocation(String spotName){
        placeName = spotName;
        if(searchMarker!=null)
            searchMarker.remove();
        new GeoTask().execute(spotName);
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
                lat = latLng.latitude;
                longi = latLng.longitude;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                options.position(latLng)
                        .snippet(address.getAddressLine(0))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                searchMarker = mGoogleMap.addMarker(options);
                searchMarker.showInfoWindow();
                //검색한 장소 주변 식당들 추천
                placeBasicManager.searchPlaceBasic(address.getLatitude(), address.getLongitude(), 100, PlaceTypes.RESTAURANT);
            }else{
                Toast.makeText(getApplicationContext(), "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //마커 클릭시 장소 상세 정보 가져오기
    private void getPlaceDetail(String placeId){
        List<Place.Field> placeFields       // 상세정보로 요청할 정보의 유형 지정
                = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                Place place = fetchPlaceResponse.getPlace();
                tvLocation.setText(place.getName()+"/"+place.getAddress());
                placeName = place.getName();
                lat = place.getLatLng().latitude;
                longi = place.getLatLng().longitude;
            }
        });
    }

    public void setAlarm() {
        Intent newAlarmIntent = new Intent(this, BrReceiver.class);
        newAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        newAlarmIntent.putExtra("planTitle", etTitle.getText().toString());


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, phourOfDay);
        calendar.set(Calendar.MINUTE, pminute);
        calendar.set(Calendar.YEAR, pyear);
        calendar.set(Calendar.MONTH, pmonth-1);
        calendar.set(Calendar.DATE, pday);
        calendar.add(Calendar.HOUR_OF_DAY, -1); //시간 전
        //calendar.setTimeInMillis(System.currentTimeMillis());

        pendingIntent =  PendingIntent.getBroadcast(this, ALRAM_REQ_CODE, newAlarmIntent, PendingIntent.FLAG_MUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        ALRAM_REQ_CODE++;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}