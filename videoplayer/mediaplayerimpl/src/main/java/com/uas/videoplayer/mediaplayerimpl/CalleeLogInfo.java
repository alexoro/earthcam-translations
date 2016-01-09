package com.uas.videoplayer.mediaplayerimpl;

import android.util.Log;

/**
 * Created by a.sorokin@vectordigital.ru on 05.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
class CalleeLogInfo {

    public static class Item {
        public final String className;
        public final String methodName;
        public final int lineNumber;
        public final String additinalMessage;

        public Item(String className, String methodName, int lineNumber, String additinalMessage) {
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.additinalMessage = additinalMessage;
        }

        @Override
        public String toString() {
            return className + "#" + methodName + ":" + lineNumber + " -- " + additinalMessage;
        }
    }

    public static Item detect(String additionalMessage) {
        return detect(additionalMessage, 0);
    }

    private static Item detect(String additionalMessage, int offset) {
        String fullClassName = Thread.currentThread().getStackTrace()[2 + offset].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[2 + offset].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[2 + offset].getLineNumber();
        return new Item(className, methodName, lineNumber, additionalMessage);
    }

    public static void detectAndDumpToLogCat(String logTag, String additionalMessage) {
        Log.d(logTag, detect(additionalMessage, 2).toString());
    }

    public static void detectAndDumpToLogCat(String logTag, int offset, String additionalMessage) {
        Log.d(logTag, detect(additionalMessage, 2 + offset).toString());
    }

}