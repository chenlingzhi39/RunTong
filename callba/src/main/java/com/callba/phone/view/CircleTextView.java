package com.callba.phone.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by PC-20160514 on 2016/5/20.
 */
public class CircleTextView extends TextView{
    int density,size,size1,size2,size3,width1,width2;
    private Paint paint1,paint2,paint3,paint4;
    private int constant;
    private boolean is_sign=false;
    public CircleTextView(Context context) {
        super(context);

    }

    public boolean is_sign() {
        return is_sign;
    }

    public void setIs_sign(boolean is_sign) {
        invalidate();
        this.is_sign = is_sign;
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getDensity() {

        return density;
    }

    public void setConstant(int constant) {
        this.constant = constant;
        invalidate();
    }

    public void setDensity(int density) {
        this.density = density;
        size=20*density/160;
        size1=4*density/160;
        size2=8*density/160;
        size3=12*density/160;
        width1=1*density/160;
        width2=1*density/160;
        invalidate();
      /*  switch (density){
            case 120:
                size=15;
                size1=3;
                size2=6;
                size3=9;
                width1=1;
                width2=1;
                invalidate();
                break;
            case 160:
                size=20;
                size1=4;
                size2=8;
                size3=12;
                width1=1;
                width2=1;
                invalidate();
                break;
            case 240:
                size=30;
                size1=6;
                size2=12;
                size3=18;
                width1=1;
                width2=2;
                invalidate();
                break;
            case 320:
                size=40;
                size1=8;
                size2=16;
                size3=24;
                width1=1;
                width2=2;
                invalidate();
                break;
            case 480:
                size=60;
                size1=12;
                size2=24;
                size3=36;
                width1=2;
                width2=3;
                invalidate();
                break;
            case 640:
                size=80;
                size1=16;
                size2=32;
                size3=48;
                width1=2;
                width2=4;
                invalidate();
                break;
        }*/
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        paint1=new Paint();
        paint2=new Paint();
        paint3=new Paint();
        paint4=new Paint();
        paint1.setColor(Color.argb(100,255,255,255));
        paint2.setColor(Color.WHITE);
        paint3.setColor(Color.parseColor("#ff7e00"));
        paint4.setColor(Color.parseColor("#ff7e00"));
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(width1);
        paint4.setStrokeWidth(width2);
        paint4.setTextSize(size);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2, paint1);
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2-size1, paint2);
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2-size2, paint3);
        canvas.drawLine(size3,getHeight()/2,getWidth()-size3,getHeight()/2,paint3);
        Rect targetRect = new Rect((int)(getMeasuredWidth()*(2-(Math.sqrt(2)))/4), (int)(getMeasuredHeight()*(2-(Math.sqrt(2)))/4),(int)(getMeasuredWidth()*(2+(Math.sqrt(2)))/4), (getMeasuredHeight()/2));
        Rect targetRect1 = new Rect((int)(getMeasuredWidth()*(2-(Math.sqrt(2)))/4), (getMeasuredHeight()/2),(int)(getMeasuredWidth()*(2+(Math.sqrt(2)))/4), (int)(getMeasuredHeight()*(2+(Math.sqrt(2)))/4));
        Paint.FontMetricsInt fontMetrics = paint4.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        int baseline1=(targetRect1.bottom + targetRect1.top - fontMetrics.bottom - fontMetrics.top) / 2;
        paint4.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(is_sign?"已签到":"签到", targetRect.centerX(), baseline, paint4);
        paint4.setTextSize(3*size/4);
        canvas.drawText("连续"+constant+"天",targetRect1.centerX(), baseline1, paint4);
        super.draw(canvas);
    }
}
