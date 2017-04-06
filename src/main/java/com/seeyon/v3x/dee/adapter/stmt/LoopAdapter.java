package com.seeyon.v3x.dee.adapter.stmt;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 循环适配器，根据condition，判断是否进行document分发执行适配器
 *
 * @author zhangfb
 */
public class LoopAdapter implements Adapter {
    private String condition;

    private List<Adapter> adapters = new ArrayList<Adapter>();

    @Override
    public Document execute(Document document) throws TransformException {
        List<Element> elements = document.getRootElement().getChildren();

        return null;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void addAdapter(Adapter adapter) {
        adapters.add(adapter);
    }
}
