package com.time2go.goosedetector;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPcommunication extends AsyncTask<Object, Boolean, String> {
    private static final int UDP_SERVER_PORT = 8888;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static final String TAG = "OCVSample::Activity";
    Activity callerActivity;

    @Override
    protected String doInBackground(Object... params) {
        String udpMsg = (String) params[0];
        callerActivity = (Activity) params[1];
        DatagramSocket ds = null;
        byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];

        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName("crazycats.ddns.net");
            DatagramPacket dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, UDP_SERVER_PORT);
            ds.send(dp);
            DatagramPacket dpr = new DatagramPacket(lMsg, lMsg.length);
            ds.setSoTimeout(4000);
            ds.receive(dpr);
            String lText = new String(lMsg, 0, dpr.getLength());
            Log.i("UDP packet received", lText);
        } catch (SocketException e) {
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
        return "";
    }
    @Override
    protected void onPostExecute(String result) {

    }
}
