package ir.yaser.tasks;

import android.app.*;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

public class AlarmService extends Service {
    private static final String CHANNEL="yaser_alarm";
    private Ringtone ringtone;

    @Override public void onCreate(){ super.onCreate(); createChannel(); }

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if("STOP".equals(intent.getAction())){ stopAlarm(); return START_NOT_STICKY; }
        String title=intent.getStringExtra("title"); if(title==null)title="یادآور";
        Intent stop=new Intent(this,AlarmService.class).setAction("STOP");
        PendingIntent stopPi=PendingIntent.getService(this,91,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent openPi=PendingIntent.getActivity(this,92,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("⏰ کارهای یاسر").setContentText(title)
                .setContentIntent(openPi).setOngoing(true).setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX)
                .addAction(new Notification.Action.Builder(null,"قطع آلارم",stopPi).build());
        startForeground(1001,b.build());
        if(ringtone==null){ Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if(uri==null)uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); ringtone=RingtoneManager.getRingtone(this,uri); if(ringtone!=null){ if(Build.VERSION.SDK_INT>=28) ringtone.setLooping(true); ringtone.play(); } }
        return START_NOT_STICKY;
    }

    private void createChannel(){ if(Build.VERSION.SDK_INT>=26){
        Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build();
        NotificationChannel ch=new NotificationChannel(CHANNEL,"آلارم یادآورها",NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("آلارم‌های آفلاین کارهای یاسر"); ch.enableVibration(true); ch.setSound(uri,attrs);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }}
    private void stopAlarm(){ if(ringtone!=null && ringtone.isPlaying())ringtone.stop(); stopForeground(true); stopSelf(); }
    @Override public void onDestroy(){ if(ringtone!=null && ringtone.isPlaying())ringtone.stop(); super.onDestroy(); }
    @Override public IBinder onBind(Intent i){ return null; }
}
