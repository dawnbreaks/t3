package com.taobao.t3.store.util;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.logging.Level;

/**
 * 增加nmap的释放
 *
 * Created by wuxiang on 14-2-7.
 */
public class NMapUtil {

    private static boolean unmapHackSupported = true;
    static{
        try{
            unmapHackSupported =
                    classForName("sun.nio.ch.DirectBuffer")!=null;
        }catch(Exception e){
            unmapHackSupported = false;
        }
    }




    /**
     * Hack to unmap MappedByteBuffer.
     * Unmap is necessary on Windows, otherwise file is locked until JVM exits or BB is GCed.
     * There is no public JVM API to unmap buffer, so this tries to use SUN proprietary API for unmap.
     * Any error is silently ignored (for example SUN API does not exist on Android).
     */
    protected void unmap(MappedByteBuffer b){
        try{
            if(unmapHackSupported){

                // need to dispose old direct buffer, see bug
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
                Method cleanerMethod = b.getClass().getMethod("cleaner", new Class[0]);
                if(cleanerMethod!=null){
                    cleanerMethod.setAccessible(true);
                    Object cleaner = cleanerMethod.invoke(b, new Object[0]);
                    if(cleaner!=null){
                        Method clearMethod = cleaner.getClass().getMethod("clean", new Class[0]);
                        if(cleanerMethod!=null)
                            clearMethod.invoke(cleaner, new Object[0]);
                    }
                }
            }
        }catch(Exception e){
            unmapHackSupported = false;
        }
    }

    public static void main(String[] args) {
        System.out.println(unmapHackSupported);
    }

    protected static Class<?> classForName(String className) {
        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            return Class.forName(className, true,loader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
