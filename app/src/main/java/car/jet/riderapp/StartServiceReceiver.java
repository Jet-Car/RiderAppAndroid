package car.jet.riderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import car.general.files.MyBackGroundService;
import car.general.files.StartActProcess;

/**
 * Created by Admin on 27-01-2016.
 */
public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        new StartActProcess(context).startService(MyBackGroundService.class);

    }
}