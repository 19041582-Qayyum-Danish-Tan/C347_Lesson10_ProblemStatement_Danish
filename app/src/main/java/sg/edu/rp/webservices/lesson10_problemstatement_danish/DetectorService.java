package sg.edu.rp.webservices.lesson10_problemstatement_danish;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DetectorService extends Service {
    boolean started;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d("DetectorService", "Service created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false){
            started = true;
            Log.d("DetectorService", "Service started");
        } else {
            Log.d("DetectorService", "Service is still running");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("DetectorService", "Service exited");
        super.onDestroy();
    }

}
