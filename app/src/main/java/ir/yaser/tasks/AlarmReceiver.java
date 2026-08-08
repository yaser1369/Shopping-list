package ir.yaser.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        long id=i.getLongExtra("taskId",0);
        TaskStore.Task t=TaskStore.find(c,id);
        if(t==null || t.done) return;
        Intent service=new Intent(c,AlarmService.class).putExtra("taskId",id).putExtra("title",t.title);
        if(Build.VERSION.SDK_INT>=26)c.startForegroundService(service); else c.startService(service);
        TaskStore.afterAlarm(c,id);
    }
}
