package com.taobao.t3.store.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 单个文件
 *
 * Created by wuxiang on 14-2-7.
 */
public class T3DataFile implements IT3File{

    private volatile long currentPos;
    private static final Long max_file_size = 1024*1024*256l;
    private File file;
    private static final String suffix = ".t3";

    public long getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(long currentPos) {
        this.currentPos = currentPos;
    }

    public T3DataFile(T3Config config, String fileName) throws IOException {
        this.config = config;
        this.fileName = fileName;
        try {
            file = new File(config.getFilePath() + fileName + suffix,"rw");
            if (!file.exists()) {
                file.mkdir();
            }
            this.fileChannel = new RandomAccessFile(config.getFilePath() + fileName + suffix,"rw").getChannel();
            this.fileChannel.position(this.fileChannel.size());
            this.currentPos = this.fileChannel.position();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file not exists", e);
        }
    }

    private FileChannel fileChannel;

    private T3Config config;

    private String fileName;


    public boolean isFull() {
        return this.currentPos >= max_file_size;
    }


    public T3Config getConfig() {
        return config;
    }


    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void setConfig(T3Config config) {
        this.config = config;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void read(long position, ByteBuffer byteBuffer) throws IOException {
        int size = 0;
        int l = 0;
        while (byteBuffer.hasRemaining()) {
            l = this.fileChannel.read(byteBuffer, position + size);
            if (l < 0) {
                // 数据还未写入，忙等待
                if (position < this.currentPos) {
                    continue;
                }
                else {
                    break;
                }
            }
            size += l;
        }
    }

    @Override
    public long write( ByteBuffer batchBuffer) throws IOException {
        // 得判断一下文件大小
        while (batchBuffer.hasRemaining()) {
            final int l = this.fileChannel.write(batchBuffer);
            if (l < 0) {
                break;
            }
        }

        return this.fileChannel.position();
    }

    @Override
    public int append(ByteBuffer buffer) throws IOException {
        return 0;
    }


    public void forward(final long offset) {
        this.currentPos += offset;
    }

    @Override
    public File force() throws IOException {
        this.fileChannel.force(true);
        return this.file;
    }
}
