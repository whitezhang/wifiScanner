package com.example.wyatt.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wyatt on 1/11/16.
 */
public class TrendView extends View {
    private int XPoint = 60;
    private int YPoint = 260;
    private int XScale = 8;
    private int YScale = 40;
    private int XLength = 380;
    private int YLength = 240;

    private int MaxDataSize = XLength / XScale;
    private List<Integer> data = new ArrayList<Integer>();
    private String[] YLabel = new String[YLength / YScale];

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.INVALIDATE:
                    TrendView.this.invalidate();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public TrendView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        for(int i = 0; i < YLabel.length; i++) {
           YLabel[i] = (i + 1) + "Sig";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 10; i++) {
                    mHandler.sendEmptyMessage(Config.INVALIDATE);
                    try {
                        Thread.sleep(1000);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        data.add(new Random().nextInt(4) + 1);
        data.add(new Random().nextInt(4) + 1);
        data.add(new Random().nextInt(4) + 1);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        canvas.drawLine(XPoint, YPoint - YLength, XPoint, YPoint, paint);
        canvas.drawColor(Color.BLACK);

        canvas.drawLine(XPoint, YPoint - YLength, XPoint - 3, YPoint - YLength + 6, paint);
        canvas.drawLine(XPoint, YPoint-YLength, XPoint+3, YPoint-YLength+6, paint);

        for(int i = 0; i*YScale < YLength; i++) {
            canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + 5, YPoint - i * YScale, paint);
            canvas.drawText(YLabel[i], XPoint - 50, YPoint - i * YScale, paint);
        }
        canvas.drawLine(XPoint, YPoint, XPoint + XLength, YPoint, paint);
        Log.e("Data size", "Data.size = " + data.size());
        if(data.size() > 1) {
            for(int i = 1; i < data.size(); i++) {
                Log.e("draw", "draw");
                canvas.drawLine(XPoint + (i-1) * XScale, YPoint - data.get(i-1) * YScale,
                        XPoint + i * XScale, YPoint - data.get(i) * YScale, paint);
            }
        }
    }
}
