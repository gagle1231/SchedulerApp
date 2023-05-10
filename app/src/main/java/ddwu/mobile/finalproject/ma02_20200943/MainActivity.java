package ddwu.mobile.finalproject.ma02_20200943;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    final int REQ_PERMISSION_CODE = 100;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private ListView lvPlans = null;

    private PlanArrayAdapter adapter;
    private ArrayList<Plan> planList;
    PlanDB planDB;
    PlanDao planDao;
    AlarmManager alarmManager = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        Intent intent = new Intent(this, BrReceiver.class);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        lvPlans= (ListView) findViewById(R.id.lvShovAllPlans);
        planDB = PlanDB.getDatabase(getApplicationContext());
        planDao = planDB.planDao();
        planList = new ArrayList<>();
        adapter = new PlanArrayAdapter(this, R.layout.lv_plan, planList);
        lvPlans.setAdapter(adapter);
        lvPlans.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("일정 삭제")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Plan plan = (Plan)adapter.getItem(position);
                                int code = plan.getAlarmCode();
                                PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), code, intent, PendingIntent.FLAG_MUTABLE);
                                if (sender != null) alarmManager.cancel(sender);
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
        Flowable<List<Plan>> showResult = planDao.getAllPlans();
        mDisposable.add(
                showResult.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(plans -> {
                            planList.clear();
                            planList.addAll(plans);
                            adapter.notifyDataSetChanged();
                        }, throwable -> Log.d("Plan", "error", throwable)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showTodaybtn:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Intent tIntent = new Intent(this, ShowTodayActivity.class);
                startActivity(tIntent);
                break;
            case R.id.addPlanbtn:
                Intent intent = new Intent(this, AddPlanActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*위치 권환 획득 코드*/
    public void checkPermission() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this,"위치 권한  승인 완료", Toast.LENGTH_SHORT).show();
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치권한 획득 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "위치권한 미획득", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //switch (requestCode)
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}