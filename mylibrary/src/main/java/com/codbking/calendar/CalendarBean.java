package com.codbking.calendar;


public class CalendarBean {

    public static final int PREVIOUS = -1;
    public static final int SELECTED = 0;
    public static final int NEXT = 1;

    public int year;
    public int moth;
    public int day;
    public int week;

    // PREVIOUS,SELECTED ,NEXT
    public int mothFlag;

    //显示
    public String chinaMonth;
    public String chinaDay;

    public CalendarBean(int year, int moth, int day) {
        this.year = year;
        this.moth = moth;
        this.day = day;
    }

    public String getDisplayWeek(){
        String s="";
         switch(week){
             case 1:
                 s="星期日";
          break;
             case 2:
                 s="星期一";
          break;
             case 3:
                 s="星期二";
                 break;
             case 4:
                 s="星期三";
                 break;
             case 5:
                 s="星期四";
                 break;
             case 6:
                 s="星期五";
                 break;
             case 7:
                 s="星期六";
                 break;

         }
        return s ;
    }

    @Override
    public String toString() {

        return year+"/"+moth+"/"+day;
    }
}