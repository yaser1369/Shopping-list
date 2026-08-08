package ir.yaser.tasks;

import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout listBox;
    private TextView budgetText;
    private final int green=Color.rgb(46,125,107);
    private final String[] cats={"امروز","مغازه","خونه","ققنوس","شخصی"};
    private final String[] repeats={"هیچ","روزانه","هفتگی","ماهانه"};

    @Override public void onCreate(Bundle b){ super.onCreate(b); requestNotifications(); render(); AlarmScheduler.restoreAll(this); }

    private TextView text(String s,int sp){ TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(Color.rgb(30,45,40));v.setPadding(18,14,18,14);return v; }
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setBackgroundColor(green);return b;}

    private void render(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Color.rgb(244,247,245));
        TextView head=text("کارهای یاسر",26);head.setTextColor(Color.WHITE);head.setBackgroundColor(Color.rgb(23,63,55));head.setGravity(Gravity.CENTER);head.setPadding(20,28,20,28);root.addView(head);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);actions.setGravity(Gravity.CENTER);actions.setPadding(8,8,8,8);
        Button add=button("+ کار جدید");Button expense=button("+ خرج خانه");actions.addView(add,new LinearLayout.LayoutParams(0,-2,1));actions.addView(expense,new LinearLayout.LayoutParams(0,-2,1));root.addView(actions);
        budgetText=text("",16);budgetText.setGravity(Gravity.CENTER);root.addView(budgetText);
        ScrollView sc=new ScrollView(this);listBox=new LinearLayout(this);listBox.setOrientation(LinearLayout.VERTICAL);listBox.setPadding(12,6,12,30);sc.addView(listBox);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        add.setOnClickListener(v->addTaskDialog());expense.setOnClickListener(v->expenseDialog());refresh();
    }

    private void refresh(){
        listBox.removeAllViews(); List<TaskStore.Task> tasks=TaskStore.tasks(this);
        tasks.sort((a,b)->Long.compare(a.done?Long.MAX_VALUE:a.dueAt,b.done?Long.MAX_VALUE:b.dueAt));
        if(tasks.isEmpty()){TextView e=text("هنوز کاری ثبت نشده. روی «کار جدید» بزن.",17);e.setGravity(Gravity.CENTER);listBox.addView(e);}
        SimpleDateFormat f=new SimpleDateFormat("yyyy/MM/dd  HH:mm",Locale.getDefault());
        for(TaskStore.Task t:tasks){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(8,10,8,10);row.setBackgroundColor(Color.WHITE);
            CheckBox cb=new CheckBox(this);cb.setChecked(t.done);row.addView(cb);
            String when=t.dueAt>0?f.format(new Date(t.dueAt)):"بدون یادآور";TextView info=text(t.title+"\n"+t.category+" • "+when+("هیچ".equals(t.repeat)?"":" • "+t.repeat),16);if(t.done)info.setAlpha(.45f);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button del=new Button(this);del.setText("×");row.addView(del,new LinearLayout.LayoutParams(70,-2));listBox.addView(row,new LinearLayout.LayoutParams(-1,-2));
            Space space=new Space(this);listBox.addView(space,new LinearLayout.LayoutParams(1,8));
            cb.setOnCheckedChangeListener((v,on)->{List<TaskStore.Task> all=TaskStore.tasks(this);for(TaskStore.Task x:all)if(x.id==t.id){x.done=on;if(on)AlarmScheduler.cancel(this,x.id);else AlarmScheduler.schedule(this,x);}TaskStore.saveTasks(this,all);refresh();});
            del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("این کار حذف شود؟").setPositiveButton("حذف",(d,w)->{List<TaskStore.Task> all=TaskStore.tasks(this);all.removeIf(x->x.id==t.id);TaskStore.saveTasks(this,all);AlarmScheduler.cancel(this,t.id);refresh();}).setNegativeButton("نه",null).show());
        }
        updateBudget();
    }

    private void addTaskDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(36,10,36,0);box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        EditText title=new EditText(this);title.setHint("مثلاً تماس با لوله‌کش");box.addView(title);
        Spinner cat=new Spinner(this);cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats));box.addView(cat);
        Spinner rep=new Spinner(this);rep.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,repeats));box.addView(rep);
        Button when=button("انتخاب تاریخ و ساعت آلارم");box.addView(when); final long[] due={0};
        when.setOnClickListener(v->{Calendar cal=Calendar.getInstance();new DatePickerDialog(this,(dv,y,m,d)->{cal.set(y,m,d);new TimePickerDialog(this,(tv,h,min)->{cal.set(Calendar.HOUR_OF_DAY,h);cal.set(Calendar.MINUTE,min);cal.set(Calendar.SECOND,0);due[0]=cal.getTimeInMillis();when.setText(new SimpleDateFormat("yyyy/MM/dd  HH:mm",Locale.getDefault()).format(cal.getTime()));},cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),true).show();},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();});
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("کار جدید").setView(box).setPositiveButton("ذخیره",null).setNegativeButton("لغو",null).create();
        dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String s=title.getText().toString().trim();if(s.isEmpty()){title.setError("عنوان را بنویس");return;}long id=System.currentTimeMillis();TaskStore.Task t=new TaskStore.Task(id,s,cats[cat.getSelectedItemPosition()],due[0],repeats[rep.getSelectedItemPosition()],false);List<TaskStore.Task> all=TaskStore.tasks(this);all.add(t);TaskStore.saveTasks(this,all);AlarmScheduler.schedule(this,t);dialog.dismiss();refresh();}));dialog.show();
    }

    private void expenseDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(36,8,36,0);box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);EditText title=new EditText(this);title.setHint("عنوان خرج");EditText amount=new EditText(this);amount.setHint("مبلغ به تومان");amount.setInputType(2);box.addView(title);box.addView(amount);
        new AlertDialog.Builder(this).setTitle("ثبت خرج خانه").setView(box).setPositiveButton("ثبت",(d,w)->{try{long a=Long.parseLong(amount.getText().toString());List<TaskStore.Expense> all=TaskStore.expenses(this);all.add(new TaskStore.Expense(System.currentTimeMillis(),title.getText().toString().trim(),a,System.currentTimeMillis()));TaskStore.saveExpenses(this,all);updateBudget();}catch(Exception e){Toast.makeText(this,"مبلغ درست وارد نشده",Toast.LENGTH_SHORT).show();}}).setNegativeButton("لغو",null).show();
    }

    private void updateBudget(){
        Calendar now=Calendar.getInstance();int dow=now.get(Calendar.DAY_OF_WEEK);int diff=(dow-Calendar.SATURDAY+7)%7;Calendar start=(Calendar)now.clone();start.add(Calendar.DAY_OF_MONTH,-diff);start.set(Calendar.HOUR_OF_DAY,0);start.set(Calendar.MINUTE,0);start.set(Calendar.SECOND,0);long spent=0;for(TaskStore.Expense e:TaskStore.expenses(this))if(e.createdAt>=start.getTimeInMillis())spent+=e.amount;long budget=5_000_000L;budgetText.setText("بودجه هفتگی خانه: ۵,۰۰۰,۰۰۰ تومان\nخرج این هفته: "+NumberFormat.getInstance().format(spent)+" • مانده: "+NumberFormat.getInstance().format(budget-spent));
    }

    private void requestNotifications(){ if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},55); }
}
