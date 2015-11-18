package org.thoughtcrime.securesms.util;

import android.util.Log;
import com.google.gson.Gson;

import org.thoughtcrime.securesms.BuildConfig;

public class Logger {

    private static final String loggerTag = "Logger";
    private final static String errorLogMsg = loggerTag + " exception: ";


    private StringBuilder stringBuilder;
    private Gson jsonBuilder;

    public enum LOGGER_DEPTH{
        ACTUAL_METHOD(4),
        LOGGER_METHOD(3),
        STACK_TRACE_METHOD(1),
        JVM_METHOD(0);

        private final int value;
        LOGGER_DEPTH(final int newValue){
            value = newValue;
        }
        public int getValue(){
            return value;
        }
    }

    private Logger(){
        if(LoggerLoader.instance != null){
            Log.e(loggerTag, "Error: Logger already instantiated");
            throw new IllegalStateException("Already Instantiated");
        }
        else{
            this.stringBuilder = new StringBuilder(255);
            this.jsonBuilder = new Gson();
        }
    }

    public static Logger instance(){
        return LoggerLoader.instance;
    }

    private String getTag(LOGGER_DEPTH depth){
        try{
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String className = stackTrace[depth.getValue()].getClassName();

            stringBuilder.append(className.substring(className.lastIndexOf(".")+1));
            stringBuilder.append("[");
            stringBuilder.append(stackTrace[depth.getValue()].getMethodName());
            stringBuilder.append("] - ");
            stringBuilder.append(stackTrace[depth.getValue()].getLineNumber());

            return stringBuilder.toString();
        }
        catch (Exception ex){
            ex.printStackTrace();
            Log.d(loggerTag, ex.getMessage());
        }
        finally{
            stringBuilder.setLength(0);
        }
        return null;
    }

    /**
     * Verbose logs
     * */
    public void verbose(String msg){
        if (!BuildConfig.DEBUG) return;
        try {
            Log.v(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        }
        catch (Exception ex) {
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void verbose(Object obj){
        if (!BuildConfig.DEBUG) return;
        try{
            String json = jsonBuilder.toJson(obj);
            Log.v(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void verbose(String msg, LOGGER_DEPTH depth){
        if (!BuildConfig.DEBUG) return;
        try{
            Log.v(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void verbose(String msg, Throwable t, LOGGER_DEPTH depth){
        if (!BuildConfig.DEBUG) return;
        try{
            Log.v(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    /**
     * Debug logs
     * */
    public void debug(String msg){
        if (!BuildConfig.DEBUG) return;
        try {
            Log.d(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void debug(Object obj){
        if (!BuildConfig.DEBUG) return;
        try {
            String json = jsonBuilder.toJson(obj);
            Log.d(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void debug(String msg, LOGGER_DEPTH depth){
        if (!BuildConfig.DEBUG) return;
        try {
            Log.d(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void debug(String msg, Throwable t, LOGGER_DEPTH depth){
        if (!BuildConfig.DEBUG) return;
        try{
            Log.d(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    /**
     * Info logs
     * */
    public void info(String msg){
        try{
            Log.i(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void info(Object obj){
        try{
            String json = jsonBuilder.toJson(obj);
            Log.i(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void info(String msg, LOGGER_DEPTH depth){
        try{
            Log.i(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void info(String msg, Throwable t, LOGGER_DEPTH depth){
        try {
            Log.i(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    /**
     * Warning logs
     * */
    public void warning(String msg){
        try {
            Log.w(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);

        }catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void warning(Object obj){
        try {
            String json = jsonBuilder.toJson(obj);
            Log.w(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);

        }catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void warning(String msg, LOGGER_DEPTH depth){
        try{
            Log.w(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD),  errorLogMsg + ex.getMessage());
        }
    }

    public void warning(String msg, Throwable t, LOGGER_DEPTH depth){
        try{
            Log.w(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    /**
     * Error logs
     * */
    public void error(String msg){
        try{
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void error(Object obj){
        try{
            String json = jsonBuilder.toJson(obj);
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void error(String msg, LOGGER_DEPTH depth){
        try{
            Log.e(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void error(String msg, Throwable t, LOGGER_DEPTH depth){
        try{
            Log.e(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    /**
     * WTF logs
     * */
    public void wtf(String msg){
        try{
            Log.wtf(getTag(LOGGER_DEPTH.ACTUAL_METHOD), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void wtf(Object obj){
        try{
            String json = jsonBuilder.toJson(obj);
            Log.wtf(getTag(LOGGER_DEPTH.ACTUAL_METHOD), json);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void wtf(String msg, LOGGER_DEPTH depth){
        try{
            Log.wtf(getTag(depth), msg);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }

    public void wtf(String msg, Throwable t, LOGGER_DEPTH depth){
        try{
            Log.wtf(getTag(depth), msg, t);
        }
        catch (Exception ex){
            Log.e(getTag(LOGGER_DEPTH.ACTUAL_METHOD), errorLogMsg + ex.getMessage());
        }
    }


    private static class LoggerLoader {
        private static final Logger instance = new Logger();
    }
}