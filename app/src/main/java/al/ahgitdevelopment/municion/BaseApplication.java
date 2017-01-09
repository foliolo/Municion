package al.ahgitdevelopment.municion;

import android.app.Application;

/**
 * Created by ahidalgog on 09/01/2017.
 */

public class BaseApplication extends Application {
    private static BaseApplication singleton;

//    public static BaseApplication getInstance() {
//        return singleton;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
//        singleton = this;
    }
}
