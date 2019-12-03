package com.liugs.svgtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import static com.liugs.svgtest.MapView.TAG;


public class MapItem {
    // item对应的path
    private Path path;
    // path对应的范围
    private RectF rectF;
    // 绘制颜色
    private int drawColor;

    public MapItem(Path path) {
        this.path = path;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }

    public void drawItem(Canvas canvas, Paint paint, boolean isSelect){

        if(isSelect){
            //选中时，绘制描边效果
            paint.clearShadowLayer();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawPath(path, paint);
        }else{
            //这是不选中的情况下   设置边界
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(8,0,0,0xffffff);
            canvas.drawPath(path,paint);
            //后面是填充
            paint.clearShadowLayer();
            paint.setColor(drawColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 根据点击的 x y 位置，使用区域Api Region.contains，是否检测触摸到item
     */
    public boolean isTouch(float x, float y) {
        Region region = new Region();
        Region clip = new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        region.setPath(path, clip);
        return region.contains((int)x,(int)y);
    }
}
