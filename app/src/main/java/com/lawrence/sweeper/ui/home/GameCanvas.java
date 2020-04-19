package com.lawrence.sweeper.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.lawrence.sweeper.GameLogic;
import com.lawrence.sweeper.MainActivity;
import com.lawrence.sweeper.R;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class GameCanvas extends View {

    private Paint mPaint;
    TextPaint textPaint;
    GameLogic gameLogic;

    private boolean ready = false;
    private Point displaySize;
    private int xPlacing = 0;
    private int yPlacing = 0;
    private int defaultBlockSize = 140;
    private int blockSize = defaultBlockSize;
    private int blockSpacing = 5;

    private int textOffset;

    long longPressStart = 0;
    int longPressDelay = 400;//milliseconds

    int moveX = -1;
    int moveY = -1;
    boolean moved = false;
    boolean scaling = false;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    VectorDrawableCompat flagVector;
    VectorDrawableCompat bombVector;
    Bitmap flagMap;
    Bitmap bombMap;
    Bitmap flag;
    Bitmap bomb;

    public GameCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);

        MainActivity appState = ((MainActivity)c);
        gameLogic = appState.getGameLogic();

    }

    private void setup(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(blockSize);

        float textHeight = textPaint.descent() - textPaint.ascent();
        textOffset = (int)((textHeight / 2) - textPaint.descent());

        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        flagVector = VectorDrawableCompat.create(getContext().getResources(), R.drawable.flag, null);
        if (flagVector != null) {
            flagMap = Bitmap.createBitmap(flagVector.getIntrinsicWidth(), flagVector.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        }

        bombVector = VectorDrawableCompat.create(getContext().getResources(), R.drawable.bomb, null);
        if (bombVector != null) {
            bombMap = Bitmap.createBitmap(bombVector.getIntrinsicWidth(), bombVector.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        }

        scaleBitmaps();

        xPlacing = (displaySize.x-getTotalWidth(blockSize))/2;
        yPlacing = (displaySize.y-getTotalHeight(blockSize))/2;
        invalidate();
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //if(!ready){
        //    return;
        //}
        //canvas.scale(mScaleFactor, mScaleFactor);
        canvas.drawColor(Color.BLACK);
        int[][] state = gameLogic.getGridArray();
        int gridWidth = gameLogic.getGridWidth();

        if(state !=null && ready) {
            for (int x = 0; x < state.length; x++) {
                Rect rect = getRect(x, gridWidth);

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.LTGRAY);

                if (state[x][0] == 1) {
                    //if revealed
                    mPaint.setColor(Color.GRAY);

                    if (state[x][1] == 9) {
                        int xLoc = rect.centerX() - (bomb.getWidth() / 2);
                        int yLoc = rect.centerY() - (bomb.getHeight() / 2);
                        canvas.drawRect(rect, mPaint);
                        canvas.drawBitmap(bomb, xLoc, yLoc, mPaint);
                    } else {
                        String text = Integer.toString(state[x][1]);
                        int xLoc = rect.centerX();
                        int yLoc = rect.centerY() + textOffset;
                        canvas.drawRect(rect, mPaint);
                        textPaint.setColor(getColor(state[x][1]));
                        canvas.drawText(text, xLoc, yLoc, textPaint);
                    }
                    mPaint.setColor(Color.LTGRAY);
                    mPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(rect, mPaint);


                } else if (state[x][0] == 2) {
                    //Flagged
                    int xLoc = rect.centerX() - (flag.getWidth() / 2);
                    int yLoc = rect.centerY() - (flag.getHeight() / 2);
                    canvas.drawRect(rect, mPaint);
                    canvas.drawBitmap(flag, xLoc, yLoc, mPaint);

                } else {
                    canvas.drawRect(rect, mPaint);
                }
            }
        }

        //mPaint.setColor(Color.GREEN);
        //canvas.drawLine(0, displaySize.y-300, 200, 200, mPaint);
        //canvas.drawLine(0, displaySize.y-100, displaySize.x+100, displaySize.y, mPaint);
    }

    private Rect getRect(int x, int total_width ){
        int offset = blockSize + blockSpacing;
        int loc = x/total_width;
        int xLoc = (x - (loc*total_width))*offset + xPlacing;
        int yLoc = loc*offset + yPlacing;

        int width = xLoc + blockSize;
        int height = yLoc + blockSize;
        return new Rect(xLoc, yLoc, width, height);
    }
    private int getMaxX(){
        int total_width = gameLogic.getGridWidth();
        int x = total_width - 1;
        return ((x - ((x/total_width)*total_width))*(blockSize + blockSpacing) + xPlacing) + blockSize;
    }
    private int getTotalWidth(int currentBlockSize){
        return (gameLogic.getGridWidth())*(currentBlockSize + blockSpacing);
    }

    private int getMaxY(){
        return (((gameLogic.getGridArray().length-1)/gameLogic.getGridWidth())*(blockSize + blockSpacing) + yPlacing) + blockSize;
    }
    private int getTotalHeight(int currentBlockSize){
        return gameLogic.getGridHeight()*(currentBlockSize + blockSpacing);
    }

    private int getColor(int number){
        switch (number){
            case 1:
                return Color.rgb(40, 40, 255);
            case 2:
                return Color.rgb(12, 112, 1);
            case 3:
                return Color.RED;
            case 4:
                return Color.rgb(0, 50, 200);
            case 5:
                return Color.rgb(143, 0, 0);
            case 6:
                return Color.rgb(0, 130, 181);
            case 7:
                return Color.BLACK;
            case 8:
                return Color.GRAY;
            case 9:
                return Color.MAGENTA;
            default:
                return 0;
        }
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
       /* mPath.moveTo(x, y);
        mX = x;
        mY = y;*/
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        boolean updated = false;
        //Set boundaries
        int maxX = getMaxX();
        int maxY = getMaxY();


        if(Math.abs(x)>1) {
            if ((xPlacing) >= 0 && (maxX) <= displaySize.x) {
                if(xPlacing + x < 0){
                    xPlacing = 0;
                } else if(xPlacing + getTotalWidth(blockSize)+x > displaySize.x){
                    xPlacing = displaySize.x - getTotalWidth(blockSize);
                } else {
                    xPlacing += x;
                }

            } else if(maxX >= displaySize.x && x <0){
                if(maxX + x < displaySize.x){
                    xPlacing = displaySize.x - getTotalWidth(blockSize);
                } else {
                    xPlacing += x;
                }
            } else if(xPlacing < 0 && x > 0){
                if(xPlacing + x > 0){
                    xPlacing = 0;
                } else {
                    xPlacing += x;
                }
            }
            updated = true;
        }

        if(Math.abs(y)>1){
            if(yPlacing >= 0 && maxY < displaySize.y){
                if(yPlacing + y < 0){
                    yPlacing = 0;
                } else if(yPlacing + getTotalHeight(blockSize) + y > displaySize.y){
                    yPlacing = displaySize.y - getTotalHeight(blockSize);
                } else {
                    yPlacing += y;
                }
            } else if(maxY >= displaySize.y && y <0){
                if(maxY + y < displaySize.y){
                    yPlacing = displaySize.y - getTotalHeight(blockSize);
                } else {
                    yPlacing += y;
                }
            } else if(yPlacing < 0 && y > 0){
                if(yPlacing + y > 0){
                    yPlacing = 0;
                } else {
                    yPlacing += y;
                }
            }
            updated = true;
        }


        /*if(Math.abs(x) > 1) {
            if (xPlacing <= 100 && (xPlacing + x) <= 100) {
                xPlacing += x;
                updated = true;
            }
        }

        if(yPlacing <= 100 && (yPlacing+y)<=100 && Math.abs(y) > 1){
            yPlacing += y;
            updated = true;
        }*/


        /*if(xPlacing >100){
            xPlacing = 100;
        }
        if(yPlacing > 100) {
            yPlacing = 100;
        }*/

        if(updated){
            moved = true;
            invalidate();
        }

        /*float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }*/
    }

    // when ACTION_UP stop touch
    private void upTouch(int x, int y, boolean longPress) {
        int[][] state = gameLogic.getGridArray();
        int gridWidth = gameLogic.getGridWidth();

        for(int loc = 0; loc < state.length; loc++){
            Rect rect = getRect(loc, gridWidth);
            if (x > rect.left && x < rect.right && y > rect.top && y < rect.bottom) {
                //Log.i("PRESS",loc+", "+rect.left +", "+rect.right+", "+rect.top+", "+rect.bottom);
                gameLogic.updateBlock(loc, longPress);
                break;
            }
        }
    }

    //override the onTouchEvent
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                longPressStart = System.currentTimeMillis();
                moveX = x;
                moveY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!mScaleDetector.isInProgress() && !scaling) {
                    if((Math.abs(x - moveX) > 20 || Math.abs(y - moveY) > 20) || moved) {
                        moveTouch(x - moveX, y - moveY);
                        moveX = x;
                        moveY = y;
                    }
                } else {
                    scaling = true;
                    moved = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!moved) {
                    if (System.currentTimeMillis() - longPressStart > longPressDelay) {
                        upTouch(x, y, true);
                    } else {
                        upTouch(x, y, false);
                    }
                }
                scaling = false;
                moved = false;
                invalidate();
                moveX = -1;
                moveY = -1;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //if(scaling){
                //    scaling = false;
                    //moveX = x;
                    //moveY = y;
                //}
                break;
        }
        return true;
    }

    public void clearCanvas() {
        //mPath.reset();
        invalidate();
    }

    private void handleScaling(int newBlockSize){


        scaleBitmaps();

        textPaint.setTextSize(blockSize);
        float textHeight = textPaint.descent() - textPaint.ascent();
        textOffset = (int)((textHeight / 2) - textPaint.descent());

        //invalidate();
    }
    private void scaleBitmaps(){
        int downSize = (int) Math.round(blockSize * .75);

        Canvas canvas = new Canvas(flagMap);
        flagVector.setBounds(0,0, canvas.getWidth(), canvas.getHeight());
        flagVector.draw(canvas);
        flag = Bitmap.createScaledBitmap(flagMap, downSize, downSize, true);

        canvas = new Canvas(bombMap);
        bombVector.setBounds(0,0, canvas.getWidth(), canvas.getHeight());
        bombVector.draw(canvas);
        bomb = Bitmap.createScaledBitmap(bombMap, downSize, downSize, true);

    }

    public void resetView(){
        mScaleFactor = 1.f;
        blockSize = (int)(defaultBlockSize * mScaleFactor);
        xPlacing = (displaySize.x-getTotalWidth(blockSize))/2;
        yPlacing = (displaySize.y-getTotalHeight(blockSize))/2;
        handleScaling(blockSize);

        invalidate();
    }

    public void setDisplaySize(int x, int y){
        displaySize = new Point(x, y);
        setup();
        ready = true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(.5f, Math.min(mScaleFactor, 5.0f));

            int newBlockSize = (int)(defaultBlockSize * mScaleFactor);

            xPlacing = xPlacing + (getTotalWidth(blockSize)-getTotalWidth(newBlockSize))/2;
            yPlacing = yPlacing + (getTotalHeight(blockSize)-getTotalHeight(newBlockSize))/2;

            blockSize = newBlockSize;
            handleScaling(newBlockSize);



            invalidate();
            return true;
        }
    }


}