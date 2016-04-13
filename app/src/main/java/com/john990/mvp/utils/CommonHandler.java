package com.john990.mvp.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by yuhengbing on 2015/3/2.
 */
public class CommonHandler extends Handler {
    public interface MessageHandler {
        void handleMessage(Message msg);
    }

    private WeakReference<MessageHandler> mMessageHandler;

    public CommonHandler(MessageHandler msgHandler) {
        mMessageHandler = new WeakReference<MessageHandler>(msgHandler);
    }

    public CommonHandler(Looper looper, MessageHandler msgHandler) {
        super(looper);
        mMessageHandler = new WeakReference<MessageHandler>(msgHandler);
    }

    @Override
    public void handleMessage(Message msg) {
        MessageHandler realHandler = mMessageHandler.get();
        if (realHandler != null) {
            realHandler.handleMessage(msg);
        }
    }
}
