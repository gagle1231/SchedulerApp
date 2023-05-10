package ddwu.mobile.finalproject.ma02_20200943;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ShowPlanActivity extends AppCompatActivity {
    PlanDB planDB;
    PlanDao planDao;
    TextView planTitle;
    TextView planDate;
    TextView planTime;
    TextView planDetail;
    TextView planLocation;
    Button button;
    Plan plan;
    private GoogleMap mGoogleMap;       // 지도 객체
    private Marker mMarker;         // 위치 표시 Marker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_plan);
        planDB = PlanDB.getDatabase(getApplicationContext());
        planDao = planDB.planDao();
        planTitle = findViewById(R.id.planTitle1);
        planDate = findViewById(R.id.planDate1);
        planTime = findViewById(R.id.planTime1);
        planLocation = findViewById(R.id.planLocationName1);
        button = findViewById(R.id.updatePlanbtn);
        Intent intent = getIntent();
        plan = (Plan) intent.getSerializableExtra("plan");
        planDetail = findViewById(R.id.tvPlanDetails1);
        planTitle.setText(plan.getTitle());
        planDate.setText(String.format("%4d년\t%2d월\t%2d일", plan.getYear(), plan.getMonth(), plan.getDay()));
        int hour = plan.getHourOfDay();
        if(hour == 0) hour = 12;
        planTime.setText(String.format("%d시 %d분", hour, plan.getMinute()));
        if(plan.getDetails()!=null)
            planDetail.setText(plan.getDetails());
        if(plan.getPlace()!=null)
            planLocation.setText(plan.getPlace());

        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.planMap2);
        mapFragment.getMapAsync(mapReadyCallback);
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


        }
    };
    public void onClick(View view){
        switch (view.getId()){
            case R.id.updatePlanbtn:
                Intent intent = new Intent(this, UpdatePlanActivity.class);
                intent.putExtra("plan", plan);
                startActivity(intent);
                finish();
                break;
            case R.id.btnShare:
                shareTwitter();
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void shareTwitter() {

        try {
            String planInfos = "약속: " + plan.getTitle()+"\n날짜:"+String.format("%d/%d/%d", plan.getYear(), plan.getMonth(), plan.getMinute())
                    +"\n상세: " + plan.getDetails();
            String sharedText = String.format("http://twitter.com/intent/tweet?text=%s",
                    URLEncoder.encode(planInfos, "utf-8"));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedText));
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}