package com.itheima.mobilesafe.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.NumberAddressDao;
import com.itheima.mobilesafe.utils.CLog;
import com.itheima.mobilesafe.utils.Constants;
import com.itheima.mobilesafe.utils.HttpTools;
import com.itheima.mobilesafe.utils.Settings;
import com.itheima.mobilesafe.utils.ViewKits;
import com.itheima.mobilesafe.utils.XMLPullParserHandler;
import com.itheima.mobilesafe.utils.objects.MobileQuery;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Catherine on 2016/8/25.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class NumberAddressQueryFragment extends Fragment {

    private static final String TAG = "NumberAddressQueryFragment";
    private EditText ed_phone;
    private TextView tv_result;
    private ViewKits viewKits;

    public static NumberAddressQueryFragment newInstance() {
        return new NumberAddressQueryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_number_address_query, container, false);
        viewKits = new ViewKits(getActivity());
        viewKits.showKeyboard();
        ed_phone = (EditText) view.findViewById(R.id.ed_phone);
        ed_phone.requestFocus();
        ed_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    queryAddress();
                }
                else
                    tv_result.setText("查无此号");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tv_result = (TextView) view.findViewById(R.id.tv_result);
        Button bt_numberAddressQuery = (Button) view.findViewById(R.id.bt_numberAddressQuery);
        bt_numberAddressQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(ed_phone.getText().toString())) {
                    queryAddress();
                } else {
                    viewKits.hideKeyboard();
                    ed_phone.clearAnimation();
                    //抖动效果
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    ed_phone.setAnimation(shake);
//                    Toast.makeText(getActivity(), "您还没输入电话号码!", Toast.LENGTH_SHORT).show();

                }
            }
        });
        return view;
    }

    /**
     * 输入手机号码查询归属地
     * 限中国地区号码
     * <p/>
     * 规则如下:
     * 1. 11码
     * 2. 13, 14, 15, 16开头
     */
    private void queryAddress() {
        if (!TextUtils.isEmpty(ed_phone.getText().toString())) {
            String number = ed_phone.getText().toString();
            String address = "查无此号";
            switch (number.length()) {
                case 3:
                    // 110
                    address = "匪警号码";
                    break;
                case 4:
                    // 5554
                    address = "模拟器";
                    break;
                case 5:
                    // 10086
                    address = "客服号码";
                    break;
                case 7:
                    //
                    address = "本地号码";
                    break;

                case 8:
                    address = "本地号码";
                    break;
                case 11:
                    //使用正则表达式过滤错误号码
                    //https://msdn.microsoft.com/zh-cn/library/ae5bf541(v=vs.100).aspx
                    /**
                     * ^ 开头
                     * 1 第一位限定1
                     * [3456] 第二位是3、4、5、6任一都行
                     * [0-9] 效果等同于 \d，适用于之后的九位数字，所以是 \d\d\d\d\d\d\d\d\d 等同于 \d{9}
                     * $ 结尾
                     *
                     * 正则式为 ^1[3456]\d{9}$
                     */
                    //符合规则
                    if (number.matches("^1[3456]\\d{9}$")) {
                        //手机号码
                        NumberAddressDao nad = new NumberAddressDao();
                        address = nad.queryNumber(number);
                        if (!TextUtils.isEmpty(address))
                            tv_result.setText(address);
                        else {
//                        HttpTools.sendDataByGet(Settings.taoBaoGetAddressUrl,new String[]{"tel"},new String[]{number},myHandler);
                            HttpTools.sendDataByGet(Settings.tenpayUrl, new String[]{"chgmobile"}, new String[]{number}, myHandler);
                        }
                    }
                    break;
                default:
                    address = "查无此号";
                    break;
            }
            tv_result.setText(address);
        } else
            Toast.makeText(getActivity(), "您还没输入电话号码!", Toast.LENGTH_SHORT).show();
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Constants.SENT_SUCCESSFULLY) {
                Bundle bundle = msg.getData();
                String message = bundle.getString("MSG");
                CLog.d(TAG, message);
                try {
                    InputStream stream = new ByteArrayInputStream(message.getBytes("GBK"));
                    XMLPullParserHandler xmlParser = new XMLPullParserHandler();
                    MobileQuery mobileQuery = xmlParser.parse(stream);
                    if (mobileQuery.getRetmsg().equals("OK")) {
                        CLog.v(TAG, mobileQuery.getCity());
                        tv_result.setText(mobileQuery.getCity() + mobileQuery.getSupplier());
                    } else
                        tv_result.setText("查无此号");
                } catch (Exception e) {
                    e.printStackTrace();
                    tv_result.setText("查无此号");
                }
            } else if (msg.what == Constants.FAILED_TO_SEND) {
                Bundle bundle = msg.getData();
                String message = bundle.getString("MSG");
                CLog.e(TAG, message);
                tv_result.setText("查无此号");

            }
        }
    };
}