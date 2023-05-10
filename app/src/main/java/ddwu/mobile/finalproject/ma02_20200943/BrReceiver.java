package ddwu.mobile.finalproject.ma02_20200943;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BrReceiver extends BroadcastReceiver {

    //알람을 받아서 noti를 띄움
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent noti = new Intent(context, ShowTodayActivity.class); //클릭하면 메인 화면으로
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, noti, PendingIntent.FLAG_MUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PlanManager";
            String description = "my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("PlanManager", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);  // 채널 생성
            notificationManager.createNotificationChannel(channel);
        }

        String planTitle = intent.getStringExtra("planTitle");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "PlanManager")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(planTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("곧 일정이 시작됩니다."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "확인", pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        int nid=101;
        notificationManagerCompat.notify(nid, builder.build());
    }

}
