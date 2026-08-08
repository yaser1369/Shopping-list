package ir.yaser.tasks;

import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(244,247,245), dark=Color.rgb(24,62,54), green=Color.rgb(46,125,107);
    private final int ink=Color.rgb(28,43,38), muted=Color.rgb(102,119,113), red=Color.rgb(190,65,65);
    private final String[] cats={"امروز","مغازه","خونه","ققنوس","شخصی"};
    private final String[] repeats={"هیچ","روزانه","هفتگی","ماهانه"};
    private String screen="home";
    private int selectedYear;
    private PersianDate.Week selectedWeek;

    @Override public void onCreate(Bundle b){
        super.onCreate(b); requestNotifications();
        selectedYear=PersianDate.today().y; showHome(); AlarmScheduler.restoreAll(this);
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private GradientDrawable shape(int color,int radius){ GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g; }
    private TextView text(String s,int sp){ TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(ink);v.setPadding(dp(16),dp(12),dp(16),dp(12));return v; }
    private TextView title(String s,int sp){TextView v=text(s,sp);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(15);b.setBackground(shape(green,14));b.setAllCaps(false);return b;}
    private LinearLayout column(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return x;}
    private Space space(int h){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return s;}

    private LinearLayout base(String heading, boolean back){
        LinearLayout root=column();root.setBackgroundColor(bg);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);bar.setPadding(dp(10),dp(10),dp(10),dp(10));bar.setBackgroundColor(dark);
        TextView h=title(heading,22);h.setTextColor(Color.WHITE);h.setGravity(Gravity.CENTER);bar.addView(h,new LinearLayout.LayoutParams(0,dp(56),1));
        if(back){Button b=button("‹");b.setTextSize(28);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->{if("week".equals(screen))showWeeks();else showHome();});bar.addView(b,new LinearLayout.LayoutParams(dp(56),dp(56)));}
        root.addView(bar);return root;
    }

    private void showHome(){
        screen="home";selectedWeek=null;
        LinearLayout root=base("برنامه یاسر",false);
        LinearLayout body=column();body.setPadding(dp(18),dp(24),dp(18),dp(18));
        PersianDate.Jalali now=PersianDate.today();
        TextView welcome=title("امروز • "+PersianDate.toFa(now.d)+" "+PersianDate.MONTHS[now.m-1]+" "+PersianDate.toFa(now.y),17);welcome.setTextColor(muted);welcome.setGravity(Gravity.CENTER);body.addView(welcome);
        body.addView(space(22));
        Button shopping=homeCard("🛒  خرید هفتگی","بودجه، قیمت‌ها و آرشیو تمام هفته‌های سال");body.addView(shopping,new LinearLayout.LayoutParams(-1,dp(150)));
        body.addView(space(18));
        Button tasks=homeCard("✓  لیست کارها","کارها، تکرارها و یادآورهای آفلاین");body.addView(tasks,new LinearLayout.LayoutParams(-1,dp(150)));
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        shopping.setOnClickListener(v->{selectedYear=PersianDate.today().y;showWeeks();});tasks.setOnClickListener(v->showTasks());
    }

    private Button homeCard(String main,String sub){
        Button b=new Button(this);b.setText(main+"\n\n"+sub);b.setTextSize(18);b.setTextColor(dark);b.setGravity(Gravity.CENTER);b.setAllCaps(false);b.setBackground(shape(Color.WHITE,22));return b;
    }

    private void showWeeks(){
        screen="weeks";LinearLayout root=base("خرید هفتگی",true);
        LinearLayout yearBar=new LinearLayout(this);yearBar.setGravity(Gravity.CENTER);yearBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);yearBar.setPadding(dp(12),dp(10),dp(12),dp(8));
        Button prev=button("سال قبل");TextView y=title("سال "+PersianDate.toFa(selectedYear),21);y.setGravity(Gravity.CENTER);Button next=button("سال بعد");
        yearBar.addView(prev,new LinearLayout.LayoutParams(dp(90),dp(50)));yearBar.addView(y,new LinearLayout.LayoutParams(0,dp(50),1));yearBar.addView(next,new LinearLayout.LayoutParams(dp(90),dp(50)));root.addView(yearBar);
        ScrollView sc=new ScrollView(this);LinearLayout list=column();list.setPadding(dp(12),dp(4),dp(12),dp(24));sc.addView(list);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        PersianDate.Week current=PersianDate.currentWeek();
        for(PersianDate.Week w:PersianDate.weeksOfYear(selectedYear)){
            long spent=spent(w.key()), budget=TaskStore.weekBudget(this,w.key());
            boolean isCurrent=w.year==current.year&&w.number==current.number;
            TextView card=title((isCurrent?"●  هفته ":"هفته ")+PersianDate.toFa(w.number)+(isCurrent?"  • این هفته":"")+"\n"+PersianDate.range(w)+"\nخرج: "+PersianDate.money(spent)+"   |   مانده: "+PersianDate.money(budget-spent)+" تومان",16);
            card.setBackground(shape(Color.WHITE,16));card.setPadding(dp(18),dp(14),dp(18),dp(14));card.setOnClickListener(v->{selectedWeek=w;showWeekDetail();});list.addView(card,new LinearLayout.LayoutParams(-1,-2));list.addView(space(9));
        }
        prev.setOnClickListener(v->{selectedYear--;showWeeks();});next.setOnClickListener(v->{selectedYear++;showWeeks();});
    }

    private long spent(String key){long s=0;for(TaskStore.ShoppingItem i:TaskStore.shoppingForWeek(this,key))if(i.price>0)s+=i.price;return s;}

    private void showWeekDetail(){
        if(selectedWeek==null){showWeeks();return;} screen="week";String key=selectedWeek.key();
        LinearLayout root=base("هفته "+PersianDate.toFa(selectedWeek.number),true);
        LinearLayout body=column();body.setPadding(dp(12),dp(10),dp(12),dp(22));
        TextView range=title(PersianDate.range(selectedWeek)+" • "+PersianDate.toFa(selectedWeek.year),16);range.setTextColor(muted);range.setGravity(Gravity.CENTER);body.addView(range);
        long budget=TaskStore.weekBudget(this,key), spent=spent(key), remaining=budget-spent;
        LinearLayout summary=new LinearLayout(this);summary.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);summary.setGravity(Gravity.CENTER);summary.setPadding(0,dp(8),0,dp(8));
        summary.addView(stat("بودجه",budget,dark),new LinearLayout.LayoutParams(0,dp(92),1));summary.addView(stat("خرج‌شده",spent,green),new LinearLayout.LayoutParams(0,dp(92),1));summary.addView(stat("مانده",remaining,remaining<0?red:dark),new LinearLayout.LayoutParams(0,dp(92),1));body.addView(summary);
        LinearLayout tools=new LinearLayout(this);tools.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);Button add=button("+ افزودن خرید");Button editBudget=button("✎ تغییر بودجه");tools.addView(add,new LinearLayout.LayoutParams(0,dp(52),1));tools.addView(spaceHorizontal(8));tools.addView(editBudget,new LinearLayout.LayoutParams(0,dp(52),1));body.addView(tools);body.addView(space(12));
        List<TaskStore.ShoppingItem> items=TaskStore.shoppingForWeek(this,key);
        if(items.isEmpty()){TextView empty=text("هنوز چیزی برای این هفته ننوشتی.\nروی «افزودن خرید» بزن.",17);empty.setGravity(Gravity.CENTER);empty.setTextColor(muted);body.addView(empty);}
        for(TaskStore.ShoppingItem item:items){body.addView(shoppingRow(item));body.addView(space(8));}
        ScrollView sc=new ScrollView(this);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        add.setOnClickListener(v->addShoppingDialog());editBudget.setOnClickListener(v->budgetDialog());
    }

    private Space spaceHorizontal(int w){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(dp(w),1));return s;}

    private TextView stat(String label,long amount,int color){TextView t=title(label+"\n"+PersianDate.money(amount)+"\nتومان",14);t.setGravity(Gravity.CENTER);t.setTextColor(color);t.setBackground(shape(Color.WHITE,14));return t;}

    private LinearLayout shoppingRow(TaskStore.ShoppingItem item){
        LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(8),dp(8),dp(8));row.setBackground(shape(Color.WHITE,14));
        CheckBox cb=new CheckBox(this);cb.setChecked(item.bought);row.addView(cb,new LinearLayout.LayoutParams(dp(48),dp(54)));
        TextView name=title(item.title,17);if(item.bought)name.setAlpha(.48f);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        Button price=new Button(this);price.setAllCaps(false);price.setText(item.price>0?PersianDate.money(item.price)+" ت":"ثبت قیمت");price.setTextSize(13);row.addView(price,new LinearLayout.LayoutParams(dp(105),dp(54)));
        Button del=new Button(this);del.setText("×");del.setTextColor(red);del.setTextSize(22);row.addView(del,new LinearLayout.LayoutParams(dp(48),dp(54)));
        cb.setOnCheckedChangeListener((v,on)->{updateShopping(item.id,x->x.bought=on);showWeekDetail();});
        price.setOnClickListener(v->priceDialog(item));
        name.setOnClickListener(v->renameShoppingDialog(item));
        del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("«"+item.title+"» از این هفته حذف شود؟").setPositiveButton("حذف",(d,w)->{List<TaskStore.ShoppingItem> all=TaskStore.shopping(this);all.removeIf(x->x.id==item.id);TaskStore.saveShopping(this,all);showWeekDetail();}).setNegativeButton("نه",null).show());
        return row;
    }

    private interface ShoppingEdit{void apply(TaskStore.ShoppingItem x);}
    private void updateShopping(long id,ShoppingEdit edit){List<TaskStore.ShoppingItem> all=TaskStore.shopping(this);for(TaskStore.ShoppingItem x:all)if(x.id==id){edit.apply(x);break;}TaskStore.saveShopping(this,all);}

    private void addShoppingDialog(){
        EditText input=new EditText(this);input.setHint("مثلاً مرغ");input.setSingleLine(true);input.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);input.setPadding(dp(24),dp(8),dp(24),dp(8));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("افزودن به خرید این هفته").setView(input).setPositiveButton("افزودن",null).setNegativeButton("لغو",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{String s=input.getText().toString().trim();if(s.isEmpty()){input.setError("نام را بنویس");return;}List<TaskStore.ShoppingItem> all=TaskStore.shopping(this);all.add(new TaskStore.ShoppingItem(System.currentTimeMillis(),s,0,selectedWeek.key(),false));TaskStore.saveShopping(this,all);d.dismiss();showWeekDetail();}));d.show();
    }

    private long parseMoney(EditText input) throws Exception {String s=input.getText().toString().replace(",","").replace("٬","").replace(" ","").trim();return Long.parseLong(s);}
    private void priceDialog(TaskStore.ShoppingItem item){
        EditText input=new EditText(this);input.setHint("قیمت به تومان");input.setInputType(InputType.TYPE_CLASS_NUMBER);if(item.price>0)input.setText(String.valueOf(item.price));input.setSelectAllOnFocus(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("قیمت «"+item.title+"»").setView(input).setPositiveButton("ثبت",null).setNeutralButton("پاک کردن قیمت",(x,w)->{updateShopping(item.id,i->i.price=0);showWeekDetail();}).setNegativeButton("لغو",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{long p=parseMoney(input);if(p<0)throw new Exception();updateShopping(item.id,i->{i.price=p;i.bought=true;});d.dismiss();showWeekDetail();}catch(Exception e){input.setError("مبلغ درست وارد کن");}}));d.show();
    }

    private void renameShoppingDialog(TaskStore.ShoppingItem item){
        EditText input=new EditText(this);input.setText(item.title);input.setSelectAllOnFocus(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("ویرایش نام خرید").setView(input).setPositiveButton("ذخیره",null).setNegativeButton("لغو",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{String s=input.getText().toString().trim();if(s.isEmpty()){input.setError("نام را بنویس");return;}updateShopping(item.id,i->i.title=s);d.dismiss();showWeekDetail();}));d.show();
    }

    private void budgetDialog(){
        EditText input=new EditText(this);input.setInputType(InputType.TYPE_CLASS_NUMBER);input.setText(String.valueOf(TaskStore.weekBudget(this,selectedWeek.key())));input.setSelectAllOnFocus(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("بودجه این هفته").setMessage("پیش‌فرض همه هفته‌ها ۵٬۰۰۰٬۰۰۰ تومان است. این تغییر فقط برای همین هفته ذخیره می‌شود.").setView(input).setPositiveButton("ذخیره",null).setNeutralButton("برگشت به ۵ میلیون",(x,w)->{TaskStore.setWeekBudget(this,selectedWeek.key(),TaskStore.DEFAULT_WEEKLY_BUDGET);showWeekDetail();}).setNegativeButton("لغو",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{long a=parseMoney(input);if(a<0)throw new Exception();TaskStore.setWeekBudget(this,selectedWeek.key(),a);d.dismiss();showWeekDetail();}catch(Exception e){input.setError("بودجه درست وارد کن");}}));d.show();
    }

    private void showTasks(){
        screen="tasks";LinearLayout root=base("لیست کارها",true);LinearLayout body=column();body.setPadding(dp(12),dp(10),dp(12),dp(22));Button add=button("+ کار جدید");body.addView(add,new LinearLayout.LayoutParams(-1,dp(54)));body.addView(space(12));
        List<TaskStore.Task> tasks=TaskStore.tasks(this);tasks.sort((a,b)->{if(a.done!=b.done)return a.done?1:-1;if(a.dueAt==0&&b.dueAt!=0)return 1;if(a.dueAt!=0&&b.dueAt==0)return -1;return Long.compare(a.dueAt,b.dueAt);});
        if(tasks.isEmpty()){TextView e=text("هنوز کاری ثبت نشده.\nروی «کار جدید» بزن.",17);e.setTextColor(muted);e.setGravity(Gravity.CENTER);body.addView(e);}
        SimpleDateFormat f=new SimpleDateFormat("yyyy/MM/dd  HH:mm",Locale.getDefault());
        for(TaskStore.Task t:tasks){
            LinearLayout row=new LinearLayout(this);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(8),dp(8),dp(8));row.setBackground(shape(Color.WHITE,14));
            CheckBox cb=new CheckBox(this);cb.setChecked(t.done);row.addView(cb);
            String when=t.dueAt>0?f.format(new Date(t.dueAt)):"بدون یادآور";TextView info=title(t.title+"\n"+t.category+" • "+when+("هیچ".equals(t.repeat)?"":" • "+t.repeat),15);if(t.done)info.setAlpha(.45f);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button del=new Button(this);del.setText("×");del.setTextColor(red);del.setTextSize(22);row.addView(del,new LinearLayout.LayoutParams(dp(48),dp(54)));body.addView(row);body.addView(space(8));
            cb.setOnCheckedChangeListener((v,on)->{List<TaskStore.Task> all=TaskStore.tasks(this);for(TaskStore.Task x:all)if(x.id==t.id){x.done=on;if(on)AlarmScheduler.cancel(this,x.id);else AlarmScheduler.schedule(this,x);}TaskStore.saveTasks(this,all);showTasks();});
            del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("این کار حذف شود؟").setPositiveButton("حذف",(d,w)->{List<TaskStore.Task> all=TaskStore.tasks(this);all.removeIf(x->x.id==t.id);TaskStore.saveTasks(this,all);AlarmScheduler.cancel(this,t.id);showTasks();}).setNegativeButton("نه",null).show());
        }
        ScrollView sc=new ScrollView(this);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);add.setOnClickListener(v->addTaskDialog());
    }

    private void addTaskDialog(){
        LinearLayout box=column();box.setPadding(dp(28),dp(8),dp(28),0);EditText taskTitle=new EditText(this);taskTitle.setHint("مثلاً تماس با لوله‌کش");box.addView(taskTitle);
        Spinner cat=new Spinner(this);cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats));box.addView(cat);Spinner rep=new Spinner(this);rep.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,repeats));box.addView(rep);
        Button when=button("انتخاب تاریخ و ساعت آلارم");box.addView(when,new LinearLayout.LayoutParams(-1,dp(52)));final long[] due={0};
        when.setOnClickListener(v->{Calendar cal=Calendar.getInstance();new DatePickerDialog(this,(dv,y,m,d)->{cal.set(y,m,d);new TimePickerDialog(this,(tv,h,min)->{cal.set(Calendar.HOUR_OF_DAY,h);cal.set(Calendar.MINUTE,min);cal.set(Calendar.SECOND,0);due[0]=cal.getTimeInMillis();when.setText(new SimpleDateFormat("yyyy/MM/dd  HH:mm",Locale.getDefault()).format(cal.getTime()));},cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),true).show();},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();});
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("کار جدید").setView(box).setPositiveButton("ذخیره",null).setNegativeButton("لغو",null).create();
        dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String s=taskTitle.getText().toString().trim();if(s.isEmpty()){taskTitle.setError("عنوان را بنویس");return;}long id=System.currentTimeMillis();TaskStore.Task t=new TaskStore.Task(id,s,cats[cat.getSelectedItemPosition()],due[0],repeats[rep.getSelectedItemPosition()],false);List<TaskStore.Task> all=TaskStore.tasks(this);all.add(t);TaskStore.saveTasks(this,all);AlarmScheduler.schedule(this,t);dialog.dismiss();showTasks();}));dialog.show();
    }

    @Override public void onBackPressed(){
        if("week".equals(screen)){showWeeks();return;} if(!"home".equals(screen)){showHome();return;} super.onBackPressed();
    }

    private void requestNotifications(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},55);}
}
