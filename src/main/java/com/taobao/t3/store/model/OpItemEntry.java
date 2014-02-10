package com.taobao.t3.store.model;

import java.io.IOException;
import java.nio.MappedByteBuffer;

/**
 * Created by wuxiang on 14-2-8.
 */
public class OpItemEntry {

    public static final int SIZE = 33 + 1;
    private ValueInfo valueInfo;

    public static int getSize() {
        return SIZE;
    }

    public ValueInfo getValueInfo() {
        return valueInfo;
    }

    public void setValueInfo(ValueInfo valueInfo) {
        this.valueInfo = valueInfo;
    }

    public static byte[] getDeltedBuffer() {
        return deltedBuffer;
    }

    private boolean deleted;
    private byte channelIndex;
    // 公用的读取deleted字段的 buffer
    static final byte[] deltedBuffer = new byte[1];


    public boolean isLoaded() {
        return this.valueInfo != null;
    }


    public void unload() {
        this.valueInfo = null; // 消除引用，让GC回收
    }


    public byte getChannelIndex() {
        return channelIndex;
    }


    public void setChannelIndex(final byte channelIndex) {
        this.channelIndex = channelIndex;
    }


    public void load(final MappedByteBuffer mappedByteBuffer, final int offset, final boolean loadItem)
            throws IOException {
        // 已经删除，不用继续读
        if (this.deleted) {
            return;
        }
        mappedByteBuffer.position(offset);
        if (!loadItem) {
            final byte data = mappedByteBuffer.get();
            this.deleted = (data == (byte) 1 ? true : false);
        }
        else {
            final byte[] bytes = new byte[SIZE];
            mappedByteBuffer.get(bytes, 0, SIZE);
            this.deleted = (bytes[0] == (byte) 1 ? true : false);
            this.valueInfo = new ValueInfo();
            this.valueInfo.parse(bytes, 1, bytes.length-1);
        }
    }


    public byte[] encode() {
        if (this.valueInfo != null) {
            final byte[] buffer = new byte[OpItemEntry.SIZE];
            if (this.deleted) {
                buffer[0] = 1;
            }
            else {
                buffer[0] = 0;
            }
            final byte[] data = this.valueInfo.toByte();
            System.arraycopy(data, 0, buffer, 1, data.length);
            return buffer;
        }
        else {
            return null;
        }
    }


    public OpItemEntry(final ValueInfo opItem, final boolean deleted) {
        super();
        this.valueInfo = opItem;
        this.deleted = deleted;
    }





    public boolean isDeleted() {
        return deleted;
    }


    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }
}
