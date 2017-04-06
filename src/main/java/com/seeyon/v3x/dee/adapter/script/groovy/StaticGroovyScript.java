package com.seeyon.v3x.dee.adapter.script.groovy;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.context.GroovyScriptClosure;
import com.seeyon.v3x.dee.script.ScriptRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 执行groovy脚本
 *
 * @author wuyz
 */
public class StaticGroovyScript implements Adapter {
    private static Log log = LogFactory.getLog(StaticGroovyScript.class);

    /**
     * groovy脚本内容，如下：<br/>
     * <code>
     * --------------------------<br/>
     * println "hello world";<br/>
     * // add code here <br/>
     * return document;<br/>
     * --------------------------<br/>
     * </code>
     */
    private String codeText;

    public StaticGroovyScript() {
    }

    public StaticGroovyScript(String codeText) {
        this.codeText = codeText;
    }

    @Override
    public Document execute(Document document) throws TransformException {
        Object obj = run(document);

        if (obj != null && obj instanceof Document) {
            return (Document) obj;
        }

        return document;
    }

    public Object run(Document document) throws TransformException {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("document", document);
        params.put("flow", document.getContext().getParameters().getValue("flow"));

        return run(params);
    }

    public Object run(Map<String, Object> params) throws TransformException {
        return run(GroovyScriptClosure.getClosure(), params);
    }

    private Object run(String prefix, Map<String, Object> params) throws TransformException {
        try {
            return new ScriptRunner().eval(prefix + "\n" + codeText, params);
        } catch (Exception e) {
            log.error("脚本执行异常：" + e.getLocalizedMessage(), e);
            throw new TransformException(e);
        }
    }

    public String getCodeText() {
        return codeText;
    }

    public void setCodeText(String codeText) {
        this.codeText = codeText;
    }
}
