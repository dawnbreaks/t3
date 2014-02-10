package com.taobao.t3.store.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 索引文件实例化，第一次启动时，从硬盘加载至内存
 *
 * Created by wuxiang on 14-2-7.
 */
public class T3IndexFile {

    private static final String suffix = ".i3";
    private File file;
    private FileChannel fileChannel;

    public T3IndexFile(T3Config config, String fileName) {
        try {
            file = new File(config.getFilePath() + fileName + suffix,"rw");
            if (!file.exists()) {
                file.mkdir();
            }
            this.fileChannel = new RandomAccessFile(config.getFilePath() + fileName + suffix,"rw").getChannel();
            this.fileChannel.position(this.fileChannel.size());
        } catch (Exception e) {
            throw new RuntimeException("file not exists", e);
        }
    }



    public void write() {
        try {
            MappedByteBuffer data  =fileChannel.map(FileChannel.MapMode.READ_ONLY,0,24);
            if (!data.isLoaded()) {
               data.load();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
