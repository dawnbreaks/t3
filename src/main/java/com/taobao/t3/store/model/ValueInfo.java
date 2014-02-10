package com.taobao.t3.store.model;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 物理存放位置信息
 *
 * Created by wuxiang on 14-2-7.
 */
public class ValueInfo implements Serializable{

   public static final byte OP_ADD = 1;
   public static final byte OP_DEL = 2;

   public static final int KEY_LENGTH = 16;
   public static final int LENGTH = KEY_LENGTH + 1 + 4 + 8 + 4;


   private byte op;// 操作
   private byte[] key;// key
   private int number;// 数据文件编号
   private volatile long offset;//位置开始点
   private int length; // 长度


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.key);
        result = prime * result + this.length;
        result = prime * result + this.number;
        result = prime * result + (int) (this.offset ^ this.offset >>> 32);
        result = prime * result + this.op;
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ValueInfo other = (ValueInfo) obj;
        if (!Arrays.equals(this.key, other.key)) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        if (this.number != other.number) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if (this.op != other.op) {
            return false;
        }
        return true;
    }


    /**
     * 将一个操作转换成字节数组
     *
     * @return 字节数组
     */
    public byte[] toByte() {
        final byte[] data = new byte[LENGTH];
        final ByteBuffer bf = ByteBuffer.wrap(data);
        bf.put(this.key);//16
        bf.put(this.op);//1
        bf.putInt(this.number);//4
        bf.putLong(this.offset);//8
        bf.putInt(this.length);//4
        bf.flip();
        return bf.array();
    }


    /**
     * 通过字节数组构造成一个操作日志
     *
     * @param data
     */
    public void parse(final byte[] data) {
        this.parse(data, 0, data.length);
    }


    public void parse(final byte[] data, final int offset, final int length) {
        final ByteBuffer bf = ByteBuffer.wrap(data, offset, length);
        this.key = new byte[16];
        bf.get(this.key);
        this.op = bf.get();
        this.number = bf.getInt();
        this.offset = bf.getLong();
        this.length = bf.getInt();
    }


    public void parse(final ByteBuffer bf) {
        this.key = new byte[16];
        bf.get(this.key);
        this.op = bf.get();
        this.number = bf.getInt();
        this.offset = bf.getLong();
        this.length = bf.getInt();
    }


    @Override
    public String toString() {
        return "valueInfo number:" + this.number + ", op:" + (int) this.op + ", offset:" + this.offset + ", length:"
                + this.length;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
