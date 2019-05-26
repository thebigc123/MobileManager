package com.itheima.mobilesafe;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class log extends Activity {


    //File externalFilesDir = getExternalFilesDir(null);




//    public String readfromfilr(File file) throws IOException {
//        //File logtxt = new File(path + "/" + "log.txt");
//        if(!file.mkdirs()) {
//            Log.e("Directoryerror", "logFile Directory not created");
//        }
//        String path=file.getPath();
//        System.out.println(path);
//        BufferedReader out = new BufferedReader(new InputStreamReader(new FileInputStream(path), "gbk"));
//        String line = null;
//        while ((line = out.readLine()) != null) {
//            System.out.println(line);
//            response = response + line;
//        }
//        out.close();
//        return response;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView T = (TextView) findViewById(R.id.textView1);
        // T.setText(i.getStringExtra("data"));
//        String logFilepath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + '/' + "LOG";
//        System.out.println(logFilepath);
//        String text="";
//        try {
//            text=readfromfilr(externalFilesDir);
//        }catch (IOException e){
//            Log.e("IOException",e.toString());
//        }
//        T.setText(text);
        File log = getExternalFilesDir("Download/LOG/log.txt");
        String response = "";
        if(!log.exists()){
            throw new RuntimeException("要读取的文件不存在");
        }
        try {
            BufferedReader out = new BufferedReader(new InputStreamReader(new FileInputStream(log), "gbk"));
            String line = null;
            while ((line = out.readLine()) != null) {
            response = response + line+"\r\n";
        }
            out.close();
            T.setText(response);
        }catch (FileNotFoundException e){}
        catch (UnsupportedEncodingException e){}
        catch (java.io.IOException e){}

        File file = new File(Environment.getExternalStorageDirectory(),"log.txt");
        System.out.println("121241342348234123"+file);

    }
}

