package com.xiaochj.accessibility;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.SwitchCompat;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.kyleduo.switchbutton.SwitchButton;
import com.xiaochj.accessibility.application.LedApplication;
import com.xiaochj.accessibility.util.Utils;
import com.xiaochj.led.R;

public class MainActivity extends Activity {

    SwitchButton b;
    boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){
        setContentView(R.layout.main_layout);
        b = (SwitchButton) findViewById(R.id.main_switch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.isAccessibilitySettingsOn(this, LedApplication.SERVICE)){
            b.setChecked(true);
            isOn = true;
        }else{
            b.setChecked(false);
            isOn = false;
        }
        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && !isOn || !isChecked && isOn){
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent,0);
                }
            }
        });
    }
}
