package com.xiaochj.accessibility;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.xiaochj.accessibility.application.LedApplication;
import com.xiaochj.accessibility.util.Utils;

public class MainActivity extends Activity {

    Switch b = null;
    boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
        b = new Switch(this);
        b.setText("辅助功能");
        ll.addView(b);
        setContentView(ll);
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
