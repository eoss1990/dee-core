package com.seeyon.v3x.dee;

import java.io.Serializable;
import java.util.Set;


/**
 * 转换上下文，在Reader、Processor和Writer以及脚本之间传递信息。
 * 
 * @author wangwenyou
 * 
 */
public interface TransformContext extends Serializable {
	/**
	 * 取得上下文的唯一标识。
	 * @return  Context Id
	 */
	String getId();
	/**
	 * Binds an object to a given attribute name in this transform context. If
	 * the name specified is already used for an attribute, this method will
	 * replace the attribute with the new to the new attribute. If a null value
	 * is passed, the effect is the same as calling
	 * <code>removeAttribute()</code>.
	 * 
	 * @param name
	 *            a <code>String</code> specifying the name of the attribute
	 * @param object
	 *            an <code>Object</code> representing the attribute to be bound
	 */
	void setAttribute(String name, Object object);

	/**
	 * Returns the transform container attribute with the given name, or
	 * <code>null</code> if there is no attribute by that name. An attribute
	 * allows a transform container to give the transform additional information
	 * not already provided by this interface. A list of supported attributes
	 * can be retrieved using <code>getAttributeNames</code>.
	 * <p>
	 * The attribute is returned as a <code>java.lang.Object</code> or some
	 * subclass.
	 * 
	 * @param name
	 *            a <code>String</code> specifying the name of the attribute
	 * @return an <code>Object</code> containing the value of the attribute, or
	 *         <code>null</code> if no attribute exists matching the given name
	 * @see #getAttributeNames
	 */
	Object getAttribute(String name);

	/**
	 * Returns an <code>Set</code> containing the attribute names available
	 * within this transform context. Use the {@link #getAttribute} method with
	 * an attribute name to get the value of an attribute.
	 * 
	 * @return an <code>Set</code> of attribute names
	 * @see #getAttribute
	 */
	Set<String> getAttributeNames();

	/**
	 * Removes the attribute with the given name from the transform context.
	 * After removal, subsequent calls to {@link #getAttribute} to retrieve the
	 * attribute's value will return <code>null</code>.
	 * 
	 * 
	 * @param name
	 *            a <code>String</code> specifying the name of the attribute to
	 *            be removed
	 */
	void removeAttribute(String name);
	
	/**
	 * 按名称查找当前任务的对象。
	 * @param name 配置文件中配置的名称（DataSource、Reader、Processor、Writer或Script）。
	 * @return 对象实例，没有找到返回<code>null</code>。
	 */
	Object lookup(String name);
	/**
	 * 取得外部传入当前上下文的参数。
	 * <p>在配置文件中配置了${param}的值域，可以使用<code>document.getContext().getParameters().evalString(value)</code>得到具体的值。例如配置了
	 * <pre>
	 *     name="sql" value="select * from table where id=${id}"
	 * </pre>
	 * 作如下调用
	 * <pre>
	 *     executeFlow("flowName",new Parameters().add("id", "101"))
	 * </pre>。
	 * 实现代码中可以通过	 
	 ** <pre>
	 *     document.getContext().getParameters().evalString(sql)
	 * </pre>
	 * 得到"select * from table where id=101"
	 * </p>
	 */
	Parameters getParameters();
}
