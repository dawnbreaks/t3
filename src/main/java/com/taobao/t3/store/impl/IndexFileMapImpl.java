package com.taobao.t3.store.impl;

import com.taobao.t3.store.IIndexMap;
import com.taobao.t3.store.model.BytesKey;
import com.taobao.t3.store.model.OpItemEntry;
import com.taobao.t3.store.model.ValueInfo;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

/**
 */
public class IndexFileMapImpl implements IIndexMap {
    private  OpItemEntry[] table;

    public static final int DEFAULT_CAPACITY = 256;

    private  BitSet bitSet = null;

    private  File file;

    private  FileChannel channel;

    private  MappedByteBuffer mappedByteBuffer;


    public IndexFileMapImpl(final int capacity, final String cacheFilePath, final boolean force) throws IOException {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity<=0");
        }
        this.file = new File(cacheFilePath);
        if (!file.exists()) {
            //file.deleteOnExit();
            this.file.createNewFile();
        }
        this.bitSet = new BitSet(OpItemEntry.SIZE * capacity);
        this.channel = new RandomAccessFile(file, force ? "rws" : "rw").getChannel();
        this.mappedByteBuffer =
                this.channel.map(FileChannel.MapMode.READ_WRITE, OpItemEntry.SIZE * capacity / 2, OpItemEntry.SIZE * capacity);
        this.table = new OpItemEntry[capacity];
    }


    private int hash(final int keyHash, final int i) {
        return this.abs(this.hash1(keyHash) + i * this.hash2(keyHash)) % table.length; // 双重散列
    }


    private int hash1(final int keyHash) {
        return keyHash % table.length;
    }


    private int hashForKey(final BytesKey k) {
        final int hash = k.hashCode();
        return this.abs(hash);
    }


    private int abs(int hash) {
        if (hash == Integer.MIN_VALUE) {
            hash = 0;
        }
        return Math.abs(hash);
    }


    private int hash2(final int keyHash) {
        return keyHash % (table.length - 2);
    }


    public boolean put(final BytesKey key, final ValueInfo value) throws IOException {
        if (this.loadFactor() > 0.75f) {
            return false;
        }
        final int keyHash = this.hashForKey(key);
        int j = this.hash1(keyHash);
        int offset = this.calcOffset(j);
        int i = 0;
        final int m = table.length;
        // 定位
        while (this.table[j] != null && !this.isEntryDeleted(j) && this.bitSet.get(offset) && i < m) {
            j = this.hash(keyHash, i++);
            offset = this.calcOffset(j);
        }
        if (table[j] == null || table[j].isDeleted()) {
            table[j] = new OpItemEntry(value, false);
            final byte[] buffer = table[j].encode();
            if (buffer != null) {
                this.mappedByteBuffer.position(offset);
                this.mappedByteBuffer.put(buffer, 0, buffer.length);
                bitSet.set(offset, true);
            }
            // 从内存释放
            table[j].unload();
            return true;
        }
        else {
            return false;
        }

    }


    private int calcOffset(final int j) {
        return j * OpItemEntry.SIZE;
    }


    private boolean isEntryDeleted(final int j) throws IOException {
        if (!this.table[j].isLoaded()) {
            this.table[j].load(mappedByteBuffer, this.calcOffset(j), false);
        }
        this.table[j].unload(); // 记得释放
        return this.table[j].isDeleted();
    }


    public ValueInfo get(final BytesKey key) throws IOException {
        final int keyHash = this.hashForKey(key);
        int j = this.hash1(keyHash);
        int i = 0;
        final int m = table.length;
        while (this.table[j] != null && i < m) {
            if (!table[j].isLoaded()) {
                table[j].load(this.mappedByteBuffer, this.calcOffset(j), true);
            }
            if (table[j].getValueInfo() != null && Arrays.equals(table[j].getValueInfo().getKey(), DigestUtils.md5(new String(key.getData())))) {
                if (table[j].isDeleted()) {
                    return null;
                }
                else {
                    return table[j].getValueInfo();
                }
            }
            else {
                table[j].unload();// 记住清除
            }
            j = this.hash(keyHash, i++);
        }
        return null;
    }


    public ValueInfo remove(final BytesKey key) throws IOException {

        final int keyHash = this.hashForKey(key);
        int j = this.hash1(keyHash);
        int i = 0;
        final int m = table.length;
        while (this.table[j] != null && i < m) {
            final int offset = this.calcOffset(j);
            if (!table[j].isLoaded()) {
                table[j].load(mappedByteBuffer, offset, true);
            }
            if (table[j].getValueInfo() != null && Arrays.equals(table[j].getValueInfo().getKey(), DigestUtils.md5(new String(key.getData())))) {
                if (table[j].isDeleted()) {
                    return null;
                }
                else {
                    table[j].setDeleted(true);
                    this.bitSet.set(offset, false);
                    // 写入磁盘
                    this.mappedByteBuffer.put(offset, DELETED);
                    return table[j].getValueInfo();
                }
            }
            else {
                table[j].unload();// 切记unload
            }
            j = this.hash(keyHash, i++);
        }
        return null;

    }


    public void close() throws IOException {
        if (this.channel != null) {
            this.channel.close();
            file.delete();
        }
    }

    class DiskIterator implements java.util.Iterator<BytesKey> {
        private int currentIndex = 0;
        private int lastRet = -1;


        @Override
        public boolean hasNext() {

            int i = this.currentIndex;
            if (i >= table.length) {
                return false;
            }
            while (!this.isExists(i)) {
                if (i == table.length - 1) {
                    return false;
                }
                i++;
            }
            return true;

        }


        private boolean isExists(final int i) {
            return table[i] != null && !table[i].isDeleted();
        }


        @Override
        public BytesKey next() {
            try {
                if (currentIndex >= table.length) {
                    return null;
                }
                while (!this.isExists(currentIndex)) {
                    if (currentIndex == table.length - 1) {
                        return null;
                    }
                    currentIndex++;
                }
                if (!table[currentIndex].isLoaded()) {
                    table[currentIndex].load(mappedByteBuffer, IndexFileMapImpl.this.calcOffset(currentIndex), true);
                }
               final BytesKey key = new BytesKey(table[currentIndex].getValueInfo().getKey());
                this.currentIndex++;
                this.lastRet++;
                return key;
            }
            catch (final IOException e) {
                throw new IllegalStateException("Load OpItem fail", e);
            }

        }


        @Override
        public void remove() {
            if (this.lastRet == -1) {
                throw new IllegalStateException("The next method is not been called");
            }
            table[currentIndex - 1].setDeleted(true);
            bitSet.set(IndexFileMapImpl.this.calcOffset(currentIndex - 1), false);
            // 写入磁盘
            mappedByteBuffer.put(IndexFileMapImpl.this.calcOffset(currentIndex - 1), DELETED);
            lastRet = -1;
        }

    }


    public Iterator<BytesKey> iterator() {
        return new DiskIterator();
    }

    static final byte DELETED = (byte) 1;


    public int size() {
        return this.bitSet.cardinality();
    }


    private float loadFactor() {
        return (float) this.bitSet.cardinality() / this.table.length;
    }

}
