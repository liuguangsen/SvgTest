package com.liugs.svgtest;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 自定义View展示SVG
 */
public class MapView extends View {
    public static final String TAG = "MapView";
    //绘制地图的颜色
    private int[] colorArray = new int[]{0xFF00FFFF,0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1};
    // 记录下path的范围
    private RectF totalRect;
    // 记录下所有写item
    private volatile List<MapItem> mapItemList;
    // 默认值缩放比例是 1.0f
    private float scale = 1.0f;
    // 选中的item
    private MapItem selectItem;
    private Paint paint;
    // 记录下宽高
    private int width;
    private int height;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        new Thread(parserPath).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 记录下宽高 适配显示的svg
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        Log.i(TAG,"onMeasure " + width + " " + height);
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }

    /**
     * 计算缩放比例，以小为准
     */
    private void calculationScale() {
        if (totalRect != null){
            double mapWidth = totalRect.width();
            double mapHeight = totalRect.height();
            scale = Math.min((float) (width/mapWidth),(float) (height/mapHeight));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 把所有的path都画出来
        if (mapItemList != null && !mapItemList.isEmpty()){
            canvas.save();
            canvas.scale(scale,scale);
            for (MapItem item : mapItemList){
                item.drawItem(canvas, paint, item.equals(selectItem));
            }
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handlerTouch(event.getX(),event.getY());
        return super.onTouchEvent(event);
    }

    private void handlerTouch(float x, float y) {
       if (mapItemList.isEmpty()){
           return;
       }
       // 先置空下选中的item
       selectItem = null;
       // 记录下选中的item
       for (MapItem item : mapItemList){
           if (item.isTouch(x/scale,y/scale)){
               selectItem = item;
               break;
           }
       }
       // 如果选中今了item，重新绘制
       if (selectItem != null){
           postInvalidate();
       }
    }

    private Runnable parserPath = new Runnable() {
        @Override
        public void run() {
            // 读取SVG文件
            InputStream inputStream = getContext().getResources().openRawResource(R.raw.china);
            List<MapItem> itemList = new ArrayList<>();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);
                Element rootElement = document.getDocumentElement();
                NodeList pathList = rootElement.getElementsByTagName("path");
                //首先 定义四个点，用于记录SVG的范围
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;

                for (int i = 0;i < pathList.getLength();i++){
                    Element item = (Element) pathList.item(i);
                    String pathData = item.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);
                    if (path == null){
                        break;
                    }
                    MapItem mapItem = new MapItem(path);
                    // 取出范围
                    RectF rect = new RectF();
                    path.computeBounds(rect,true);
                    mapItem.setRectF(rect);
                    // 设置颜色
                    int colorIndex = i % 4;
                    mapItem.setDrawColor(colorArray[colorIndex]);
                    itemList.add(mapItem);

                    //遍历取出每个path中的left取所有的最小值
                    left = left == -1 ? rect.left : Math.min(left, rect.left);
                    //遍历取出每个path中的right取所有的最大值
                    right = right == -1 ? rect.right : Math.max(right, rect.right);
                    //遍历取出每个path中的top取所有的最小值
                    top = top == -1 ? rect.top : Math.min(top, rect.top);
                    //遍历取出每个path中的bottom取所有的最大值
                    bottom = bottom == -1 ? rect.bottom : Math.max(bottom, rect.bottom);
                }
                // 根据SVG的范围计算缩放比例
                totalRect = new RectF(left,top,right,bottom);
                calculationScale();
                Log.i(TAG,"parser path over.");
                mapItemList = itemList;
                // 开始绘制所有的path
                postInvalidate();
            } catch (Exception e){
                Log.e(TAG,"parser path error. " + e.getMessage());
            }
        }
    };
}
