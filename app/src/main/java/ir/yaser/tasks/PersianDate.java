package ir.yaser.tasks;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PersianDate {
    public static final String[] MONTHS={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};

    public static class Jalali { public final int y,m,d; Jalali(int y,int m,int d){this.y=y;this.m=m;this.d=d;} }
    public static class Week {
        public final int year, number; public final LocalDate start,end;
        Week(int year,int number,LocalDate start,LocalDate end){this.year=year;this.number=number;this.start=start;this.end=end;}
        // The Saturday itself is the stable identity of a week. This keeps a week that
        // crosses Nowruz as one single archive entry even when visible from both years.
        public String key(){ return start.toString(); }
    }

    public static Jalali today(){ return toJalali(LocalDate.now()); }

    public static List<Week> weeksOfYear(int jy){
        List<Week> out=new ArrayList<>();
        LocalDate yearStart=toGregorian(jy,1,1), yearEnd=toGregorian(jy,12,isLeap(jy)?30:29);
        LocalDate first=yearStart;
        while(first.getDayOfWeek()!=DayOfWeek.SATURDAY) first=first.minusDays(1);
        int n=1;
        for(LocalDate s=first; !s.isAfter(yearEnd); s=s.plusDays(7)) out.add(new Week(jy,n++,s,s.plusDays(6)));
        return out;
    }

    public static Week currentWeek(){
        Jalali j=today(); LocalDate now=LocalDate.now();
        for(Week w:weeksOfYear(j.y)) if(!now.isBefore(w.start)&&!now.isAfter(w.end)) return w;
        return weeksOfYear(j.y).get(0);
    }

    public static String shortDate(LocalDate date){
        Jalali j=toJalali(date); return toFa(j.d)+" "+MONTHS[j.m-1];
    }

    public static String range(Week w){ return "شنبه "+shortDate(w.start)+" تا جمعه "+shortDate(w.end); }
    public static String toFa(long value){
        String s=String.valueOf(value); String[] fa={"۰","۱","۲","۳","۴","۵","۶","۷","۸","۹"};
        for(int i=0;i<10;i++) s=s.replace(String.valueOf(i),fa[i]); return s;
    }
    public static String money(long value){ return String.format(Locale.US,"%,d",value).replace(',', '٬'); }

    public static boolean isLeap(int jy){ return toGregorian(jy+1,1,1).toEpochDay()-toGregorian(jy,1,1).toEpochDay()==366; }

    public static Jalali toJalali(LocalDate date){
        int gy=date.getYear(), gm=date.getMonthValue(), gd=date.getDayOfMonth();
        int[] gdm={0,31,59,90,120,151,181,212,243,273,304,334};
        int gy2=gm>2?gy+1:gy;
        long days=355666L+(365L*gy)+(gy2+3)/4-(gy2+99)/100+(gy2+399)/400+gd+gdm[gm-1];
        int jy=(int)(-1595+33*(days/12053)); days%=12053;
        jy+=4*(days/1461); days%=1461;
        if(days>365){ jy+=(days-1)/365; days=(days-1)%365; }
        int jm,jd;
        if(days<186){ jm=1+(int)(days/31); jd=1+(int)(days%31); }
        else { jm=7+(int)((days-186)/30); jd=1+(int)((days-186)%30); }
        return new Jalali(jy,jm,jd);
    }

    public static LocalDate toGregorian(int jy,int jm,int jd){
        int gy;
        jy+=1595; long days=-355668L+(365L*jy)+(jy/33)*8+((jy%33)+3)/4+jd;
        if(jm<7) days+=(jm-1)*31L; else days+=(jm-7)*30L+186;
        gy=(int)(400*(days/146097)); days%=146097;
        if(days>36524){ gy+=100*(--days/36524); days%=36524; if(days>=365)days++; }
        gy+=4*(days/1461); days%=1461;
        if(days>365){ gy+=(days-1)/365; days=(days-1)%365; }
        int gd=(int)days+1; int[] sal={0,31,((gy%4==0&&gy%100!=0)||gy%400==0)?29:28,31,30,31,30,31,31,30,31,30,31};
        int gm=1; while(gm<=12&&gd>sal[gm]) gd-=sal[gm++];
        return LocalDate.of(gy,gm,gd);
    }
}
