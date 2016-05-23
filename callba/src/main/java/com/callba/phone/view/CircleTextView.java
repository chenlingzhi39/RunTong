package com.callba.phone.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by PC-20160514 on 2016/5/20.
 */
public class CircleTextView extends TextView{
    private Paint paint1,paint2,paint3;
    public CircleTextView(Context context) {
        super(context);

    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        paint1=new Paint();
        paint2=new Paint();
        paint3=new Paint();
        paint1.setColor(Color.argb(100,255,255,255));
        paint2.setColor(Color.WHITE);
        paint3.setColor(Color.parseColor("#ff7e00"));
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(2);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2, paint1);
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2-12, paint2);
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2-24, paint3);
        canvas.drawLine(36,getHeight()/2,getWidth()-36,getHeight()/2,paint3);
        super.draw(canvas);
    }
}
