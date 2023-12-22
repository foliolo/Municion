package al.ahgitdevelopment.municion;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Created by ahidalgog on 09/01/2017.
 */

public class BaseApplication extends Application {
    public FirebaseCrashlytics crashlytics;

    @Override
    public void onCreate() {
        super.onCreate();
        crashlytics = FirebaseCrashlytics.getInstance();
    }
}
