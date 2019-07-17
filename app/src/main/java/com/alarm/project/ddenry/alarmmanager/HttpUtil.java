package com.alarm.project.ddenry.alarmmanager;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class HttpUtil {

    byte[] doPost(String serverAddress) {

        Log.i("HttpUtil", "URL is " + serverAddress);

        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(serverAddress);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] data = new byte[bufferSize];
            int count;
            while ((count = inputStream.read(data, 0, bufferSize)) != -1)
                outStream.write(data, 0, count);
            inputStream.close();
            data = null;
            return outStream.toByteArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
