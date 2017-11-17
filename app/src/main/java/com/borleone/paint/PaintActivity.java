package com.borleone.paint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Borle on 6/3/2017.
 */
public class PaintActivity extends View {

    String TAG = "PAINT";
    String packageName;
    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint, mPaint;
    String mImageName;
    File mediaFile, mediaStorageDir;

    public PaintActivity(Context context, AttributeSet attrs) {
        super(context, attrs);
        packageName = context.getPackageName();

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);
        setDrawingCacheEnabled(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        setDrawingCacheEnabled(false);
        onSizeChanged(width, height, width, height);
        invalidate();

        setDrawingCacheEnabled(true);
    }

    public void changeColor(int color) {
        mPaint.setColor(color);
    }

    public int getColor() {
        return mPaint.getColor();
    }

    public void changeStrokeWidth(float size) {
        mPaint.setStrokeWidth(size);
    }

    public float getStrokeWidth() {
        return mPaint.getStrokeWidth();
    }

    public void saveDrawing() {

        File pictureFile = getOutputMediaFile();
        //Log.d(TAG, pictureFile.toString());

    }

    private File getOutputMediaFile() {

        mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Paint");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            //mediaStorageDir.mkdirs();
            Log.e(TAG, mediaStorageDir.mkdirs() + " " + mediaStorageDir.toString());
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        mImageName = "MI_" + timeStamp;

        //Save dialog box
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.save_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Save image with title");
        // set save_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);
        final EditText userInput = (EditText) dialogView.findViewById(R.id.et_input);
        userInput.setText(mImageName);
        userInput.setSelectAllOnFocus(true);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mImageName = userInput.getText().toString();
                        mImageName += ".png";
                        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
                        Toast.makeText(getContext(), mImageName + " saved successfully", Toast.LENGTH_LONG).show();
                        Log.e(TAG, mediaFile.toString());
                        Bitmap image = getDrawingCache();

                        if (mediaFile == null) {
                            Log.e(TAG, "Error creating media file, check storage permissions: ");
                            return;
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(mediaFile);
                            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d(TAG, "Error accessing file: " + e.getMessage());
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.getMessage());
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // show it
        alertDialog.show();

        return mediaFile;
    }


}


