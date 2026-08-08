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
    private static final String SHOPPING = "shopping_v2";
    private static final String BUDGET_PREFIX = "week_budget_";
    public static final long DEFAULT_WEEKLY_BUDGET = 5_000_000L;

    public static class Task {
        public long id;
        public String title, category, repeat;
        public long dueAt;
        public boolean done;
        public Task(long id, String title, String category, long dueAt, String repeat, boolean done) {
            this.id=id; this.title=title; this.category=category; this.dueAt=dueAt; this.repeat=repeat; this.done=done;
        }
    }

    public static class ShoppingItem {
        public long id, price;
        public String title, weekKey;
        public boolean bought;
        public ShoppingItem(long id, String title, long price, String weekKey, boolean bought) {
            this.id=id; this.title=title; this.price=price; this.weekKey=weekKey; this.bought=bought;
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

    public static List<ShoppingItem> shopping(Context c) {
        List<ShoppingItem> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString(SHOPPING, "[]"));
            for (int i=0;i<a.length();i++) {
                JSONObject o=a.getJSONObject(i);
                out.add(new ShoppingItem(o.getLong("id"), o.getString("title"), o.optLong("price",0),
                        o.getString("weekKey"), o.optBoolean("bought",false)));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static List<ShoppingItem> shoppingForWeek(Context c, String weekKey) {
        List<ShoppingItem> out = new ArrayList<>();
        for (ShoppingItem item : shopping(c)) if (weekKey.equals(item.weekKey)) out.add(item);
        return out;
    }

    public static void saveShopping(Context c, List<ShoppingItem> items) {
        JSONArray a = new JSONArray();
        try {
            for (ShoppingItem item : items) {
                JSONObject o = new JSONObject();
                o.put("id", item.id); o.put("title", item.title); o.put("price", item.price);
                o.put("weekKey", item.weekKey); o.put("bought", item.bought); a.put(o);
            }
        } catch (Exception ignored) {}
        prefs(c).edit().putString(SHOPPING, a.toString()).apply();
    }

    public static long weekBudget(Context c, String weekKey) {
        return prefs(c).getLong(BUDGET_PREFIX + weekKey, DEFAULT_WEEKLY_BUDGET);
    }

    public static void setWeekBudget(Context c, String weekKey, long amount) {
        prefs(c).edit().putLong(BUDGET_PREFIX + weekKey, amount).apply();
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
