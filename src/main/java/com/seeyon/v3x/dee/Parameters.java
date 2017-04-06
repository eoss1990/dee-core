package com.seeyon.v3x.dee;

import com.seeyon.v3x.dee.adapter.script.groovy.StaticGroovyScript;
import com.seeyon.v3x.dee.script.ScriptRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.ScriptException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parameter容器，包装一系列相关或相互依赖的Parameter。
 *
 * <pre>
 * Parameters params = new Parameters();
 * params.add(&quot;id&quot;, 1).add(&quot;name&quot;, &quot;test&quot;);
 * params.addScript(&quot;plus&quot;, &quot;1+2&quot;); // 结果为3
 * params.addScript(&quot;plus1&quot;, &quot;return 1+2&quot;); //结果还是3
 * params.addScript(&quot;a&quot;, &quot;1&quot;);
 * params.add(&quot;b&quot;, 2);
 * params.addScript(&quot;complex&quot;, &quot;owner.getValue('a')+owner.getValue('b')&quot;);// 结果是3
 * </pre>
 *
 * @author wangwenyou
 */
public class Parameters implements Iterable<Parameter>, Serializable {
    private static final long serialVersionUID = -2758417103448139171L;

    private static Log log = LogFactory.getLog(Parameters.class);

    /**
     * 参数键值对
     */
    private Map<String, Parameter> parameters = new ConcurrentHashMap<String, Parameter>();

    /**
     * 参数缓存
     */
    private Map<String, Object> paramCache;

    /**
     * 添加静态的对象参数。
     *
     * @param name  参数名称
     * @param value 对象。
     * @return 当前的Parameters实例。
     */
    public Parameters add(String name, Object value) {
        Parameter para = new ParameterImpl(name, this);
        para.setValue(value);
        return add(name, para);
    }

    /**
     * 添加脚本类型的参数。
     *
     * @param name   参数名称
     * @param script groovy脚本，可以通过owner取到当前的Parameters对象以进行关联运算。
     * @return 当前的Parameters实例。
     */
    public Parameters addScript(String name, String script) {
        Parameter para = new ParameterImpl(name, this);
        para.setScript(script);
        return add(name, para);
    }

    /**
     * 取得指定的参数对象。
     *
     * @param name 参数名称
     * @return 参数对象，不存在返回<tt>null</tt>。
     */
    public Parameter get(String name) {
        return parameters.get(name);
    }

    /**
     * 获取指定的参数值
     *
     * @param name 参数名称
     * @return 参数对象的值
     */
    public Object getValue(String name) {
        Parameter parameter = get(name);
        if (parameter != null) {
            return parameter.getValue();
        }
        return null;
    }

    /**
     * 删除指定的参数
     *
     * @param name 参数名称
     * @return 当前的Parameters实例。
     */
    public Parameters remove(String name) {
        parameters.remove(name);
        reCalc();
        return this;
    }

    /**
     * 计算Groovy表达式。
     *
     * @param expr 表达式
     * @return groovy脚本返回值
     * @throws TransformException
     */
    public Object eval(String expr) throws TransformException {
        calc();
        try {
            return new ScriptRunner().eval(expr, this.paramCache);
        } catch (ScriptException e) {
            throw new TransformException("无法计算" + expr + " " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * 宏替换。支持在XML中直接配置的文本中的宏替换，更复杂的脚本不能直接配置，需要用expr等tag包装。
     *
     * @param expr 表达式
     * @return groovy脚本返回值
     * @throws TransformException
     */
    public String evalString(String expr) throws TransformException {
        return eval("\"" + expr + "\"").toString();
    }

    private Parameters add(String name, Parameter para) {
        this.parameters.put(name, para);
        // 清除计算结果
        reCalc();
        return this;
    }

    /**
     * 计算并缓存当前容器中的所有参数的值，如果已计算就不再重复计算。如果需要强制计算，请调用reCalc方法。
     */
    private void calc() {
        if (this.paramCache != null) {
            return;
        }

        // 为避免脚本环境中递归调用死锁，不加锁。
        this.paramCache = new ConcurrentHashMap<String, Object>();
        for (Map.Entry<String, Parameter> entry : this.parameters.entrySet()) {
        	if(entry.getKey() == null || entry.getValue() == null || entry.getValue().getValue() == null){
            	continue;
        	}
        	paramCache.put(entry.getKey(), entry.getValue().getValue());
        }
    }

    /**
     * 清除计算结果，重新计算。
     */
    public void reCalc() {
        this.paramCache = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Parameter param : parameters.values()) {
            sb.append(param).append(",");
        }
        int length = sb.length();
        if (length > 1) {
            sb.delete(length - 1, length);
        }
        sb.append("]");
        return sb.toString();
    }

    static class ParameterImpl implements Parameter, Serializable {
        private final String name;

        private Object value = null;

        private String script = null;

        // 宿主，所属参数容器。
        private final Parameters owner;

        public ParameterImpl(String name, Parameters owner) {
            this.name = name;
            this.owner = owner;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Object getValue() {
            if (value != null) {
                return value;
            }
            try {
                if (script != null) {
                    StaticGroovyScript s = new StaticGroovyScript(this.script);
                    // 为解决相互依赖循环计算的问题，将容器注入到上下文。
                    Map<String, Object> params = new ConcurrentHashMap<String, Object>();
                    params.put("params", owner);
                    return s.run(params);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return null;
        }

        @Override
        public void setValue(Object value) {
            if (script != null) {
                throw new UnsupportedOperationException("已经设置了脚本，脚本类型的Parameter不能直接设置具体值。");
            }
            this.value = value;
        }

        @Override
        public void setScript(String script) {
            if (value != null) {
                throw new UnsupportedOperationException("已经设置了具体值，值类型的Parameter不能设置脚本。");
            }
            this.script = script;
        }

        @Override
        public String toString() {
            String s = StringEscapeUtils.unescapeJava(script);
            if (s != null) {
                s = "'" + s + "'";
            }
            return "[name：'" + name + "', value:'" + value + "', script:" + s + "]";
        }
    }

    @Override
    public Iterator<Parameter> iterator() {
        return this.parameters.values().iterator();
    }
}
