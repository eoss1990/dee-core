package com.seeyon.v3x.dee.adapter;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;

import java.io.Serializable;

/**
 * @author zhangfb
 * 2017.2.10增加序列化支持 by yangyu
 */
public interface Adapter extends Serializable{
    Document execute(Document document) throws TransformException;
}
