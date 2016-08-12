package com.itheima.mobilesafe.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itheima.mobilesafe.Constants;
import com.itheima.mobilesafe.MainInterface;
import com.itheima.mobilesafe.R;

/**
 * Created by Catherine on 2016/8/12.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class AntiTheftFragment extends Fragment {

    private static final String TAG = "AntiTheftFragment";
    private SharedPreferences sp;
    private MainInterface mainInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anti_theft, container, false);
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        mainInterface = (MainInterface) getActivity();
        //判断是否做过设置向导
        if (!sp.getBoolean("configed", false)) {
            mainInterface.callFragment(Constants.SETUP1_FRAG);
        } else {
            //已做过,停留此页
        }

        return view;
    }


}