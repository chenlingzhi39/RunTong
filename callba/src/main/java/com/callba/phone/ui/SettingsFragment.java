package com.callba.phone.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.DemoModel;
import com.jenzz.materialpreference.SwitchPreference;

/**
 * Created by PC-20160514 on 2016/7/2.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    SwitchPreference message,voice,shake,log;
    ListPreference lp;
    private DemoModel settingsModel;
    public static SettingsFragment newInstance(){
        SettingsFragment settingsFragment=new SettingsFragment();
        return settingsFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsModel= DemoHelper.getInstance().getModel();
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.preferences);
        message = (SwitchPreference) findPreference("message_key");
        voice=(SwitchPreference)findPreference("voice_key");
        shake=(SwitchPreference)findPreference("shake_key");
        lp=(ListPreference) findPreference("call_mode");
        //log=(SwitchPreference)findPreference("log_key");
        message.setDefaultValue(settingsModel.getSettingMsgNotification());
        voice.setDefaultValue(settingsModel.getSettingMsgSound());
        shake.setDefaultValue(settingsModel.getSettingMsgVibrate());
        voice.setEnabled(settingsModel.getSettingMsgNotification());
        shake.setEnabled(settingsModel.getSettingMsgNotification());
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if(preference instanceof ListPreference){
            //把preference这个Preference强制转化为ListPreference类型
            ListPreference listPreference=(ListPreference)preference;
            //获取ListPreference中的实体内容
            CharSequence[] entries=listPreference.getEntries();
            //获取ListPreference中的实体内容的下标值
            int index=listPreference.findIndexOfValue((String)o);
            //把listPreference中的摘要显示为当前ListPreference的实体内容中选择的那个项目
            listPreference.setSummary(entries[index]);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch(preference.getKey()){
            case "message_key":
                settingsModel.setSettingMsgNotification(!settingsModel.getSettingMsgNotification());
                    voice.setEnabled(settingsModel.getSettingMsgNotification());
                    shake.setEnabled(settingsModel.getSettingMsgNotification());
                break;
            case "voice_key":
                settingsModel.setSettingMsgSound(!settingsModel.getSettingMsgSound());
                break;
            case "shake_key":
                settingsModel.setSettingMsgVibrate(!settingsModel.getSettingMsgVibrate());
                break;
          /*  case "log_key":
                if(log.isChecked())
                    LogcatHelper.getInstance(MyApplication.getInstance()).start();
                else
                    LogcatHelper.getInstance(MyApplication.getInstance()).stop();
                break;*/
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
