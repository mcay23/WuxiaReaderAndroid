package com.yuwono.wuxiareader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class PopupSettingsActivity extends Activity {

    static TextView size;
    static TextView spacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_settings);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // scaling
        getWindow().setLayout((int) (width*0.8), (int)(height*0.7));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        ImageButton btn = findViewById(R.id.close_settings_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        size = findViewById(R.id.font_size);

        final int font_sp = (int) pixelsToSp(TextActivity.context, TextActivity.bodyText.getTextSize());
        size.setText(font_sp + " sp");
        SeekBar font_size = findViewById(R.id.seekBarFontSize);
        font_size.setProgress(font_sp);
        font_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size.setText(progress + " sp");
                TextActivity.setFontSettings(progress, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = getSharedPreferences(TextActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("fontSize", seekBar.getProgress());
                editor.apply();
            }
        });
        spacing = findViewById(R.id.spacing_size);
        final int spacing_sp = (int) pixelsToSp(TextActivity.context,
                TextActivity.bodyText.getPaint().getFontSpacing());
        spacing.setText(spacing_sp + " sp");
        SeekBar line_spacing = findViewById(R.id.seekBarSpacingSize);
        line_spacing.setProgress(spacing_sp);
        line_spacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                spacing.setText(progress + " sp");
                TextActivity.setFontSettings(-1, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = getSharedPreferences(TextActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("fontSpacing", seekBar.getProgress());
                editor.apply();
            }
        });
        Spinner selectionSpinner = findViewById(R.id.font_spinner);
        selectionSpinner.setSelection(TextActivity.getFontFamily());
        selectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextActivity.setFontFamily(position);
                SharedPreferences.Editor editor = getSharedPreferences(TextActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("font", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner themeSpinner = findViewById(R.id.theme_spinner);
        themeSpinner.setSelection(TextActivity.getTextTheme());
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextActivity.setTextTheme(position);
                SharedPreferences.Editor editor = getSharedPreferences(TextActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("theme", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px/scaledDensity;
    }
}