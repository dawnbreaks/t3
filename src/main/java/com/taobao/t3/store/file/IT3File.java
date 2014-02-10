package com.taobao.t3.store.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by wuxiang on 14-2-7.
 */
public interface IT3File {

    /**
     * 读取内容
     *
     * @param position
     * @param byteBuffer
     * @throws IOException
     */
    void read(long position, ByteBuffer byteBuffer) throws IOException;



    long write(ByteBuffer batchBuffer) throws IOException;

    /**
     * 数据文件是否满了
     * @return
     */
    boolean isFull();

    int append(ByteBuffer buffer) throws IOException;

    File force() throws IOException ;
}
