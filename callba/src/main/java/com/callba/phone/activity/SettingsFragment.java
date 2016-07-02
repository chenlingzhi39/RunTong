package com.callba.phone.activity;

import android.os.Bundle;
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
public class SettingsFragment extends PreferenceFragment {
    SwitchPreference message,voice,shake;
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
        message.setDefaultValue(settingsModel.getSettingMsgNotification());
        voice.setDefaultValue(settingsModel.getSettingMsgSound());
        shake.setDefaultValue(settingsModel.getSettingMsgVibrate());
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
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
