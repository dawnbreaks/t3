package com.taobao.t3.store.impl;

import com.taobao.t3.store.IIndexMap;
import com.taobao.t3.store.model.BytesKey;
import com.taobao.t3.store.model.ValueInfo;
import sun.print.resources.serviceui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuxiang on 14-2-7.
 */
public class IndexMapImpl implements IIndexMap, Serializable{


    private Map<BytesKey, ValueInfo> maps = new HashMap<BytesKey, ValueInfo>();


    @Override
    public boolean put(BytesKey key, ValueInfo valueInfo) {
        maps.put(key, valueInfo);
        return true;
    }

    @Override
    public ValueInfo get(BytesKey key) {
        return maps.get(key);
    }
}
