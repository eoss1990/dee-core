package com.seeyon.v3x.dee;

import com.seeyon.v3x.dee.util.DocumentUtil;

/**
 * Document抽象实现
 *
 * @author wangwenyou
 */
public abstract class AbstractDocument implements Document {
    private TransformContext context;

    @Override
    public TransformContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(TransformContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return DocumentUtil.toXML(this);
    }
}
