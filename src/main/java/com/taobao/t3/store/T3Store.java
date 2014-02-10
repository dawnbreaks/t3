package com.taobao.t3.store;

import java.util.Objects;

/**
 * Created by wuxiang on 14-2-7.
 */
public interface T3Store {

    byte[] get(String key);

    void put(String key, byte[] value);

}
