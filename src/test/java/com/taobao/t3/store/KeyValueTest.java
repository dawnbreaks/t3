package com.taobao.t3.store;

import com.taobao.t3.store.impl.IndexFileMapImpl;
import com.taobao.t3.store.impl.IndexMapImpl;
import com.taobao.t3.store.model.ValueInfo;
import com.taobao.t3.store.serialization.HashMapSerialization;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Created by wuxiang on 14-2-7.
 */
public class KeyValueTest {

    @Test
        public void test_junit() {
        System.out.println(32<<2);
        System.out.println(System.getProperty("os.name"));
        System.out.println(java.nio.charset.Charset.defaultCharset().toString());
        System.out.println("学java".getBytes().length);
        System.out.println(1024*1024*1024*256l);
    }


    @Test
    public void test_fix_byte() {
        System.out.println("测试一下中文".getBytes().length);
        System.out.println(DigestUtils.md5Hex("测试一下中文").getBytes().length);

        byte[] buffer = new byte[24];
        ByteBuffer bf = ByteBuffer.wrap(buffer);
        bf.putInt(2);
        long tstime = System.currentTimeMillis();
        System.out.println(tstime);
        bf.putLong(tstime);
        bf.flip();
        System.out.println(bf.getInt()); // 取bit位
        System.out.println(bf.getLong()); // 取bit位
        for (byte b: bf.array()) {
            System.out.println(b);
        }
    }


    @Test
    public void test_keyValue() {

        try {
            IIndexMap map = new IndexFileMapImpl(256,"/home/wuxiang/data/key.index", true);
            T3Store store = new T3StoreImpl(map);
            //store.put("wuxiang", "测试一下中文".getBytes());
            byte[] values =  store.get("wuxiang");
            if (values != null) {
                System.out.println(new String(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void test_seri() {

        try {
            IIndexMap map = new IndexMapImpl();
            T3Store store = new T3StoreImpl(map);
            store.put("wuxiang", "测试一下中文".getBytes());
            for (int i = 0;i < 10000;i ++) {
                store.put("abc" + i, (i + "你好测试一下").getBytes());
            }
            // 索引文件会导致内存溢出
            // 多个索引文件
            HashMapSerialization.serializeToDisk(map);
            byte[] values =  store.get("wuxiang");
            if (values != null) {
                System.out.println(new String(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test_deseri() {
        try {
            IIndexMap map = HashMapSerialization.deSerializeToDisk();
            T3Store store = new T3StoreImpl(map);
            long startTime = System.currentTimeMillis();
            byte[] values =  store.get("abc9999");
            if (values != null) {
                long costs = System.currentTimeMillis() - startTime;
                System.out.println(new String(values) +" costs=  + " + costs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test_bit(){
        ValueInfo valueInfo = new ValueInfo();
        valueInfo.setKey(DigestUtils.md5("wuxiang"));
        System.out.println(DigestUtils.md5("wuxiang").length);
        byte op = 1;
        valueInfo.setOp(op);
        long start = 9910;
        valueInfo.setNumber(Integer.parseInt("1"));
        valueInfo.setOffset(start);
        System.out.println(valueInfo.getOffset());
        valueInfo.setLength(16);
        byte[] bytes = valueInfo.toByte();
        ValueInfo valueInfo1 = new ValueInfo();
        valueInfo1.parse(bytes, 0, bytes.length);
        System.out.println(valueInfo1.getOffset());


    }
}

