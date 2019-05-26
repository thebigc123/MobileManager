package com.itheima.mobilesafe.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.itheima.mobilesafe.DolphindetectActivity;
import com.itheima.mobilesafe.HomeActivity;
import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.interfaces.MainInterface;
import com.itheima.mobilesafe.log;
import com.itheima.mobilesafe.utils.CLog;
import com.itheima.mobilesafe.utils.Constants;
import com.itheima.mobilesafe.utils.Settings;
import static com.itheima.mobilesafe.DolphinRecord.GlobalConfig.CHANNEL_CONFIG;
import static com.itheima.mobilesafe.DolphinRecord.GlobalConfig.SAMPLE_RATE_INHZ;
import static com.itheima.mobilesafe.DolphinRecord.GlobalConfig.AUDIO_FORMAT;
import com.itheima.mobilesafe.DolphinRecord.PcmToWavUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DolphindetectFragment extends Fragment implements View.OnClickListener {
    private MainInterface mainInterface;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_dolphin, container, false);
        Log.e(TAG, "onCreate");
        mBtnControl = (Button) view.findViewById(R.id.btn_control);
        mBtnControl.setOnClickListener(this);//implements View.OnClickListener , or it should be (new View.OnClickListener()){ onClick...}
        mBtnConvert = (Button) view.findViewById(R.id.btn_convert);
        mBtnConvert.setOnClickListener(this);
        mBtnSend = (Button) view.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mBtnlog=(Button) view.findViewById(R.id.log);
        mBtnlog.setOnClickListener(this);
        mThreadPool = Executors.newCachedThreadPool();// 初始化线程池
        checkPermissions();
        return view;

    }




    private static final String TAG = "ddx";

    private Button mBtnControl;

    private Button mBtnSend;

    private Button mBtnConvert;
    private Button mBtnlog;

    private ExecutorService mThreadPool;

    String Serverip = "172.20.10.2";
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    public int i=0;
    public int Serverport = 9999;


    /**
     * 需要申请的运行时权限
     */

    private String[] permissions = new String[]{

            Manifest.permission.RECORD_AUDIO,

            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET


    };


    private List<String> mPermissionList = new ArrayList<>();

    private boolean isRecording;

    private AudioRecord audioRecord;

    private AudioTrack audioTrack;

    private byte[] audioData;

    private FileInputStream fileInputStream;

    public void startRecord() {

        Log.e(TAG, "startRecord()");

        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);

        if (audioRecord == null)

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,

                    CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);


        final byte data[] = new byte[minBufferSize];

        final File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");

        if (!file.mkdirs()) {

            Log.e(TAG, "Directory not created");

        }

        if (file.exists()) {

            file.delete();

        }


        audioRecord.startRecording();

        isRecording = true;


        // TODO: 2018/3/10 pcm数据无法直接播放，保存为WAV格式。


        new Thread(new Runnable() {

            @Override

            public void run() {


                FileOutputStream os = null;

                try {

                    os = new FileOutputStream(file);

                } catch (FileNotFoundException e) {

                    e.printStackTrace();

                }


                if (null != os) {

                    while (isRecording) {

                        int read = audioRecord.read(data, 0, minBufferSize);

                        // 如果读取音频数据没有出现错误，就将数据写入到文件

                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                            try {

                                os.write(data);

                            } catch (IOException e) {

                                e.printStackTrace();

                            }

                        }

                    }

                    try {

                        Log.i(TAG, "run: close file output stream !");

                        os.close();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            }

        }).start();

    }




    public void stopRecord() {

        isRecording = false;

        // 释放资源

        if (null != audioRecord) {

            audioRecord.stop();

            audioRecord.release();

            audioRecord = null;

            //recordingThread = null;

        }

    }
    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(getActivity(), permissions, MY_PERMISSIONS_REQUEST);

            }

        }

    }

    public void fileClientTest(String  path) throws IOException {

        SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String   curtime   =   formatter.format(curDate);
        Socket socket = new Socket("172.20.10.2", 9999);
        // 将文件内容从硬盘读入内存中
        FileInputStream fileInputStream = new FileInputStream(path);
        OutputStream outputStream = socket.getOutputStream();
        // 定义每次发送文件的大小
        byte[] buffer = new byte[256];
        int len = 0;
        // 因为文件内容较大，不能一次发送完毕，因此需要通过循环来分次发送
        while ((len = fileInputStream.read(buffer)) != -1){
            outputStream.write(buffer, 0, len);
        }

        outputStream.close();
        fileInputStream.close();
        socket.close();
        //接收服务器传来的消息
        Socket socket1 = new Socket("172.20.10.2", 9999);
        InputStream is = socket1.getInputStream();
        InputStreamReader isr;
        BufferedReader br;
        String response="";
        // 步骤2：创建输入流读取器对象 并传入输入流对象
        // 该对象作用：获取服务器返回的数据
        isr = new InputStreamReader(is,"gbk");
        br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            response=response+line;
        }
        // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
        System.out.println(response);

        File log = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "LOG");
        if (!log.mkdirs()) {
            Log.e(TAG, "logFile Directory not created");
        }
        String logFilepath=getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+'/'+"LOG";
        System.out.println(logFilepath);
        System.out.println(log.getPath());
        File logtxt=new File(logFilepath+"/"+"log.txt");
        if(!logtxt.exists()) {
            logtxt.createNewFile();
        }

        BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logtxt,true), "gbk"));//new FileOutputStream(logtxt,true)表示追加数据
        out.write(curtime);
        out.write(response+"\r\n");
        System.out.println("respone is:"+response);
        out.close();
        socket1.close();
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.e(TAG, "onRequestPermissionsResult");

        if (requestCode == MY_PERMISSIONS_REQUEST) {

            for (int i = 0; i < grantResults.length; i++) {

                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");

                }

            }

            // 运行时权限的申请不是本demo的重点，所以不再做更多的处理，请同意权限申请。

        }

    }



    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_control://will click btn_convert at the same time?

                Log.e(TAG, "onClick, btn_control");

                Button button = (Button) view;

                if (button.getText().toString().equals("start_record")) {

                    button.setText("stop_record");

                    startRecord();
                    i=i+1;

                } else {

                    button.setText("start_record");

                    stopRecord();

                }

            case R.id.btn_convert:
                Log.e(TAG, "onClick, btn_convert");

                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File pcmFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
                File wavFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), i+".wav");
                //Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_MUSIC)"+getActivity().getExternalFilesDir(Environment.getExternalStorageState()));
                Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_MUSIC)"+getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC));
                Log.e(TAG,"waveFile"+wavFile);
                if (!wavFile.mkdirs()) {
                    Log.e(TAG, "wavFile Directory not created");
                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
                break;
            case R.id.btn_send:
                Log.e(TAG, "onClick, btn_send");
                String wavFilepath=getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()+'/'+i+".wav";
                Log.e(TAG,wavFilepath);
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fileClientTest(getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()+'/'+i+".wav");
                            Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_MUSIC)"+getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()+'/'+i+".wav");
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                });
                break;
            case R.id.log:
                Log.e(TAG, "onClick, log");
                Intent intent=new Intent(getActivity(), log.class);
                startActivity(intent );
                break;


            default:

                break;

        }

    }

}

