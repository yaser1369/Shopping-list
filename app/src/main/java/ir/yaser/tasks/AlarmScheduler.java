package ir.yaser.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class AlarmScheduler {
    private static PendingIntent pi(Context c,long id){
        Intent i=new Intent(c,AlarmReceiver.class).putExtra("taskId",id);
        return PendingIntent.getBroadcast(c,(int)(id & 0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void schedule(Context c, TaskStore.Task t){
        if(t.dueAt<=System.currentTimeMillis() || t.done) return;
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,t.dueAt,pi(c,t.id));
    }
    public static void cancel(Context c,long id){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); am.cancel(pi(c,id));
    }
    public static void restoreAll(Context c){ for(TaskStore.Task t:TaskStore.tasks(c)) schedule(c,t); }
}
