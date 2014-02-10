package com.taobao.t3.store.serialization;

import com.taobao.t3.store.IIndexMap;
import com.taobao.t3.store.impl.IndexMapImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对hashMap的变成数组，不太会用，先用最简单的实现
 *
 * Created by wuxiang on 14-2-8.
 */
public class HashMapSerialization {

    public static IIndexMap deSerializeToDisk() {
        try {
            FileInputStream fis = new FileInputStream("/home/wuxiang/data/map.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            IndexMapImpl indexMap = (IndexMapImpl) ois.readObject();
            return indexMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static void serializeToDisk(IIndexMap indexMap) {
        try {
            FileOutputStream fos = new FileOutputStream("/home/wuxiang/data/map.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(indexMap);
            oos.close();
        } catch (Exception e) {

        }
    }



}
