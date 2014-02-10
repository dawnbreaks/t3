package com.taobao.t3.store;

import com.taobao.t3.store.file.T3Config;
import com.taobao.t3.store.file.T3DataFile;
import com.taobao.t3.store.model.BytesKey;
import com.taobao.t3.store.model.ValueInfo;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuxiang on 14-2-7.
 */
public class T3StoreImpl implements T3Store {

    private T3DataFile dataFile;
    private T3DataFile readFile;
    private IIndexMap indexMap;

    /**
     * 文件要控制大小，同时还要能路由文件
     *
     * @param indexMap
     * @throws IOException
     */
    public T3StoreImpl(IIndexMap indexMap) throws IOException {
        mkfile(1);
        this.indexMap = indexMap;
    }

    private void mkfile(int i) throws IOException {
        T3Config config = new T3Config();
        config.setFilePath("/home/wuxiang/data/");
        dataFile = new T3DataFile(config, i+"");
    }


    @Override
    public byte[] get(String key) {
        try {
            ValueInfo valueInfo = indexMap.get(new BytesKey(key));
            if (valueInfo == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(valueInfo.getLength());
            T3Config config = new T3Config();
            config.setFilePath("/home/wuxiang/data/");
            readFile = new T3DataFile(config, valueInfo.getNumber()+"");
            this.readFile.read(valueInfo.getOffset(), buffer);
            buffer.flip();
            byte[] b = new byte[buffer.remaining()];
            buffer.get(b);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * put操作必须要锁，防止并发操作
     *
     * @param key
     * @param value
     */
    @Override
    public void put(String key, byte[] value) {
        Lock lock = new ReentrantLock();
        // 写
        try {
            lock.lock();
            BytesKey bytesKey = new BytesKey(key);
            ByteBuffer buffer = ByteBuffer.allocate(value.length);
            buffer.put(value);
            buffer.flip();
            if (dataFile.isFull()) {
                mkfile(Integer.parseInt(dataFile.getFileName()) + 1);
            }
            ValueInfo valueInfo = new ValueInfo();
            valueInfo.setKey(DigestUtils.md5(key));
            byte op = 1;
            valueInfo.setOp(op);
            long start = dataFile.getCurrentPos();
            valueInfo.setNumber(Integer.parseInt(dataFile.getFileName()));
            valueInfo.setOffset(start);
            valueInfo.setLength(value.length);
            dataFile.write(buffer);
            dataFile.forward(value.length);
            this.indexMap.put(bytesKey, valueInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
