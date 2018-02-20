package com.layoutxml.sabs.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.layoutxml.sabs.BuildConfig;
import com.layoutxml.sabs.MainActivity;
import com.layoutxml.sabs.R;

import java.util.Objects;

import static com.layoutxml.sabs.Global.BlockPort53;

public class MiscSettingsFragment extends LifecycleFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Objects.requireNonNull(getActivity()).setTitle(R.string.misc_settings);
        View view = inflater.inflate(R.layout.fragment_misc_settings, container, false);

        ((MainActivity)getActivity()).hideBottomBar();

        Switch showDialogSwitch = view.findViewById(R.id.showDialogSwitch);
        Switch blackThemeSwitch = view.findViewById(R.id.blackThemeSwitch);
        Switch blockPortSwitch = view.findViewById(R.id.blockPortSwitch);
        View MiscShowWarning = view.findViewById(R.id.MiscShowWarning);
        View MiscBlackTheme = view.findViewById(R.id.MiscBlackTheme);
        View MiscBlockPort = view.findViewById(R.id.MiscBlockPort);
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Boolean showDialog = sharedPreferences.getBoolean("showDialog", false);
        Boolean blackTheme = sharedPreferences.getBoolean("blackTheme", false);
        Boolean blockPort = sharedPreferences.getBoolean("blockPort53", true);
        showDialogSwitch.setChecked(showDialog);
        blackThemeSwitch.setChecked(blackTheme);
        blockPortSwitch.setChecked(blockPort);


        MiscShowWarning.setOnClickListener(v -> {
            boolean isChecked = !showDialogSwitch.isChecked();
            showDialogSwitch.setChecked(isChecked);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("showDialog", isChecked);
            editor.apply();
        });

        MiscBlackTheme.setOnClickListener(v -> {
            boolean isChecked = !blackThemeSwitch.isChecked();
            blackThemeSwitch.setChecked(isChecked);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("blackTheme", isChecked);
            editor.apply();
            Intent mStartActivity = new Intent(getActivity(), MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
            assert mgr != null;
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 600, mPendingIntent);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    System.exit(0);
                }
            }, 500);
        });

        MiscBlockPort.setOnClickListener(v -> {
            boolean isChecked = !blockPortSwitch.isChecked();
            blockPortSwitch.setChecked(isChecked);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("blockPort53", isChecked);
            editor.apply();
            BlockPort53=isChecked;
        });

        return view;
    }
}