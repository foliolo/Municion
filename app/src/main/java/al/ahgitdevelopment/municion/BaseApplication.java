package al.ahgitdevelopment.municion;

import androidx.multidex.MultiDexApplication;

/**
 * Created by ahidalgog on 09/01/2017.
 */

public class BaseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        singleton = this;
    }
}
