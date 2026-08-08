package ir.yaser.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class TaskStore {
    private static final String PREF = "yaser_local_data";
    private static final String TASKS = "tasks";
    private static final String EXPENSES = "expenses";

    public static class Task {
        public long id;
        public String title, category, repeat;
        public long dueAt;
        public boolean done;
        public Task(long id, String title, String category, long dueAt, String repeat, boolean done) {
            this.id=id; this.title=title; this.category=category; this.dueAt=dueAt; this.repeat=repeat; this.done=done;
        }
    }

    public static class Expense {
        public long id, amount, createdAt;
        public String title;
        public Expense(long id, String title, long amount, long createdAt) {
            this.id=id; this.title=title; this.amount=amount; this.createdAt=createdAt;
        }
    }

    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    public static List<Task> tasks(Context c) {
        List<Task> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString(TASKS, "[]"));
            for (int i=0;i<a.length();i++) {
                JSONObject o=a.getJSONObject(i);
                out.add(new Task(o.getLong("id"),o.getString("title"),o.optString("category","شخصی"),
                        o.optLong("dueAt",0),o.optString("repeat","هیچ"),o.optBoolean("done",false)));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static void saveTasks(Context c, List<Task> tasks) {
        JSONArray a=new JSONArray();
        try { for(Task t:tasks) { JSONObject o=new JSONObject(); o.put("id",t.id); o.put("title",t.title);
            o.put("category",t.category); o.put("dueAt",t.dueAt); o.put("repeat",t.repeat); o.put("done",t.done); a.put(o); } }
        catch(Exception ignored) {}
        prefs(c).edit().putString(TASKS,a.toString()).apply();
    }

    public static List<Expense> expenses(Context c) {
        List<Expense> out=new ArrayList<>();
        try { JSONArray a=new JSONArray(prefs(c).getString(EXPENSES,"[]"));
            for(int i=0;i<a.length();i++){ JSONObject o=a.getJSONObject(i); out.add(new Expense(o.getLong("id"),o.getString("title"),o.getLong("amount"),o.getLong("createdAt"))); } }
        catch(Exception ignored) {}
        return out;
    }

    public static void saveExpenses(Context c,List<Expense> expenses){
        JSONArray a=new JSONArray(); try{for(Expense e:expenses){JSONObject o=new JSONObject();o.put("id",e.id);o.put("title",e.title);o.put("amount",e.amount);o.put("createdAt",e.createdAt);a.put(o);}}catch(Exception ignored){}
        prefs(c).edit().putString(EXPENSES,a.toString()).apply();
    }

    public static Task find(Context c,long id){ for(Task t:tasks(c)) if(t.id==id)return t; return null; }

    public static void afterAlarm(Context c,long id){
        List<Task> list=tasks(c);
        for(Task t:list) if(t.id==id){
            if("روزانه".equals(t.repeat)) t.dueAt += 24L*60*60*1000;
            else if("هفتگی".equals(t.repeat)) t.dueAt += 7L*24*60*60*1000;
            else if("ماهانه".equals(t.repeat)) {
                java.util.Calendar cal=java.util.Calendar.getInstance(); cal.setTimeInMillis(t.dueAt); cal.add(java.util.Calendar.MONTH,1); t.dueAt=cal.getTimeInMillis();
            } else t.dueAt=0;
            if(t.dueAt>System.currentTimeMillis()) AlarmScheduler.schedule(c,t);
            break;
        }
        saveTasks(c,list);
    }
}
