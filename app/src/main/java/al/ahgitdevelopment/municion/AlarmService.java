package al.ahgitdevelopment.municion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by david.sierra on 25/07/2016.
 */
public class AlarmService extends Service {

    private NotificationManager mManager;
    private Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    //    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
        mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);

        Intent intent1 = new Intent(this.getApplicationContext(), FragmentMainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(AlarmService.this);
        builder.setAutoCancel(true);
        builder.setContentTitle("Notificación de Licencias");
        if (intent != null && intent.getExtras() != null && intent.hasExtra("licencia")) {
            builder.setContentText("Tu licencia \"" + intent.getExtras().getString("licencia", "") + "\" caduca hoy");
        } else {
            builder.setContentText("Tu licencia caduca hoy");
        }

//        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        builder.setSmallIcon(R.mipmap.ic_launcher_4_transparent);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
//        builder.setNumber(100);
        // API level 16
//      builder.setSubText("This is subtext...");
//      builder.build();
        notification = builder.getNotification();
        mManager.notify(11, notification);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
