package com.taobao.t3.store.model;

import java.io.Serializable;

/**
 * Created by wuxiang on 14-2-7.
 */
public class BytesKey implements Serializable{

    public BytesKey(String key){
        this.data = key.getBytes();
    }

    public BytesKey(byte[] data){
        this.data =data;
    }



    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (null != this.data) {
            for (int i = 0; i < this.data.length; i++) {
                h = 31 * h + data[i++];
            }
        }
        return h;
    }


    @Override
    public boolean equals(final Object o) {
        if (null == o || !(o instanceof BytesKey)) {
            return false;
        }
        final BytesKey k = (BytesKey) o;
        if (null == k.getData() && null == this.getData()) {
            return true;
        }
        if (null == k.getData() || null == this.getData()) {
            return false;
        }
        if (k.getData().length != this.getData().length) {
            return false;
        }
        for (int i = 0; i < this.data.length; ++i) {
            if (this.data[i] != k.getData()[i]) {
                return false;
            }
        }
        return true;
    }
}
