package com.borleone.paint;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private PaintActivity paintActivity;
    private ImageButton clearBtn, colorBtn, brushBtn, saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        paintActivity = (PaintActivity) findViewById(R.id.paint);

        clearBtn = (ImageButton) findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintActivity.clear();
            }
        });
        colorBtn = (ImageButton) findViewById(R.id.color_btn);
        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColor();
            }
        });
        brushBtn = (ImageButton) findViewById(R.id.brush_btn);
        brushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrushSize();
            }
        });
        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionToWrite();
                paintActivity.saveDrawing();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            // Clear the view
            paintActivity.clear();
            return true;
        }
        if (id == R.id.action_color) {
            // Display dialog of color picker
            pickColor();
            return true;
        }
        if (id == R.id.action_size) {
            // Display dialog to select width of strokes
            changeBrushSize();
            return true;
        }
        if (id == R.id.action_save) {
            // Save the paint view as bmp
            checkPermissionToWrite();
            paintActivity.saveDrawing();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pickColor() {
        final Context context = MainActivity.this;
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(paintActivity.getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        Toast.makeText(getApplicationContext(), "onColorSelected: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        paintActivity.changeColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();

    }

    public void changeBrushSize() {
        final Dialog brushSizeDialog = new Dialog(this);
        brushSizeDialog.setTitle("Select Brush Size:");
        brushSizeDialog.setContentView(R.layout.brush_size);

        final TextView text_brush = (TextView) brushSizeDialog.findViewById(R.id.brush_size);
        final SeekBar seek_brush = (SeekBar) brushSizeDialog.findViewById(R.id.brush_seek);

        seek_brush.setMax(100);
        seek_brush.setProgress((int) paintActivity.getStrokeWidth());
        text_brush.setText(String.valueOf(paintActivity.getStrokeWidth()));

        seek_brush.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //change to progress
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_brush.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button btn_ok = (Button) brushSizeDialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float chosenSize = seek_brush.getProgress();
                paintActivity.changeStrokeWidth(chosenSize);
                brushSizeDialog.dismiss();
            }
        });

        brushSizeDialog.show();

    }

    public void checkPermissionToWrite() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        }
    }

}
