package al.ahgitdevelopment.municion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by david.sierra on 25/07/2016.
 */
public class ReceiverBroad extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlarmService.class);
        if (intent.getExtras() != null) {
            service.putExtra("licencia", intent.getExtras().getString("licencia", ""));
        }
        context.startService(service);
    }
}
