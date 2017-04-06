package com.seeyon.v3x.dee.adapter.stmt;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import com.seeyon.v3x.dee.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件判断适配器。当条件为真时，执行一系列适配器；当条件为false时，执行另外一系列适配器。
 *
 * @author zhangfb
 */
public class JudgementAdapter implements Adapter {
    /**
     * 条件
     */
    private String condition;

    /**
     * 条件为真时，执行的适配器
     */
    private List<Adapter> trueAdapters = new ArrayList<Adapter>();

    /**
     * 条件为假时，执行的适配器
     */
    private List<Adapter> falseAdapters = new ArrayList<Adapter>();

    @Override
    public Document execute(Document document) throws TransformException {
        Object obj = new StaticGroovyScript(condition).run(document);

        if (Utils.obj2Boolean(obj)) {
            return executeAdapters(trueAdapters, document);
        } else {
            return executeAdapters(falseAdapters, document);
        }
    }

    private Document executeAdapters(List<Adapter> adapters, Document document) throws TransformException {
        if (adapters == null) {
            return document;
        }

        Document result = document;
        for (Adapter adapter : adapters) {
            if (adapter == null) {
                continue;
            }
            result = adapter.execute(result);
            result.setContext(document.getContext());
        }

        return result;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void addTrueAdapter(Adapter adapter) {
        trueAdapters.add(adapter);
    }

    public void addFalseAdapter(Adapter adapter) {
        falseAdapters.add(adapter);
    }
}
