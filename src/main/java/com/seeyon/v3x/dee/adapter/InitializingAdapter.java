package com.seeyon.v3x.dee.adapter;

import com.seeyon.v3x.dee.Parameters;

/**
 * Created by yangyu on 16/11/23.
 * 用于adapter处理parameters
 */
public interface InitializingAdapter {

    /**
     * 在adapter的execute之前执行
     * @param parameters
     * @throws Exception
     */
    void evalParaBeforeExe(Parameters parameters) throws Exception;
}
