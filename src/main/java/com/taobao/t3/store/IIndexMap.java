package com.taobao.t3.store;

import com.taobao.t3.store.model.BytesKey;
import com.taobao.t3.store.model.ValueInfo;

import java.io.IOException;

/**
 * 内存索引
 *
 * Created by wuxiang on 14-2-7.
 */
public interface IIndexMap {

    boolean put(BytesKey key, ValueInfo valueInfo) throws IOException;

    ValueInfo get(BytesKey key) throws IOException;
}
