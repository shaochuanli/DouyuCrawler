package me.brucezz.crawler.handler;

import me.brucezz.crawler.model.Message;
import me.brucezz.crawler.util.HexUtil;
import me.brucezz.crawler.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zero on 2016/01/04.
 * Douyu
 */
public class MessageHandler {

    /**
     * 发送消息
     */
    public static void send(Socket socket, String content) throws IOException {
        if (socket == null || !socket.isConnected()) return;

        Message message = new Message(content);
        OutputStream out = socket.getOutputStream();
        out.write(message.getBytes());

        LogUtil.d("Send Message", message.toString());
    }

    /**
     * 接收消息并处理
     */
    public static void receive(Socket socket, MessageHandler.OnReceiveListener listener)
            throws IOException {
        if (socket == null || !socket.isConnected()) return;

        int len;
        byte[] buffer = new byte[8 * 1024];
        InputStream in = socket.getInputStream();

        while (socket.isConnected() //链接结束
                && (len = in.read(buffer)) != -1 //输入流结束
                ) {
            if (listener != null) {
                listener.onReceive(splitResponse(Arrays.copyOf(buffer, len)));
                if (listener.isFinished()) return;
            }
        }
    }

    /**
     * 分离同时返回的多组数据
     */
    public static List<String> splitResponse(byte[] buffer) {
        if (buffer == null || buffer.length <= 0) return null;

        List<String> resList = new ArrayList<>();
        String byteArray = HexUtil.Bytes2HexStringWithOutSpace(buffer).toLowerCase();

        String[] strings = byteArray.split("b2020000");
        int end;
        for (int i = 1; i < strings.length; i++) {
            if (!strings[i].contains("00")) continue;
            end = strings[i].indexOf("00");
            byte[] bytes = HexUtil.hexString2Bytes(strings[i].substring(0, end));
            if (bytes != null) resList.add(new String(bytes));
        }

        return resList;
    }

    public interface OnReceiveListener {

        /**
         * 返回数据
         */
        void onReceive(List<String> responses);

        /**
         * 确认是否结束接收数据
         */
        boolean isFinished();
    }


}
