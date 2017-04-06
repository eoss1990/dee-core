package com.seeyon.v3x.dee.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectUtil {
	private static Log log = LogFactory.getLog(ReflectUtil.class);

	private static Map<Class, Class> primitiveWrapperTypeMap = new HashMap<Class, Class>() {
		{
			put(boolean.class, Boolean.class);
			put(byte.class, Byte.class);
			put(char.class, Character.class);
			put(double.class, Double.class);
			put(float.class, Float.class);
			put(int.class, Integer.class);
			put(long.class, Long.class);
			put(short.class, Short.class);
		}
	};

	/**
	 * 通过类全路径名获得类
	 *
	 * @param className 带包名的类 如：com.lang.String
	 * @return 类对象
	 */
	@SuppressWarnings("unchecked")
	public static Object reflectClass(String className) throws ReflectException {
		Class localClass;
		try {
			localClass = Class.forName(className);
			Constructor localConstructor = localClass.getConstructor();
			return localConstructor.newInstance();
		} catch (Exception e) {
			log.error(e.getMessage() + className, e);
		}
		return null;
	}

	/**
	 * 根据反射设置一个类的属性,只针对String值属性，只有一个value的情况
	 *
	 * @param obj        需要设置属性的类
	 * @param methodName 方法名
	 * @param value      方法参数值
	 * @return 设置完成后类
	 */
	public static Object setValue(Object obj, String methodName, String value) {
		String className = obj.getClass().getCanonicalName();
		try {
			Method[] allMethod = obj.getClass().getMethods();
			List<Method> methods = new ArrayList<Method>();
			for (Method mtd : allMethod) {
//                Method mtd=obj.getClass().getMethod(method, new Class[]{String.class});//取得所需类的方法
				if (mtd.getName().equals(methodName)) {
					Class[] parameterTypes = mtd.getParameterTypes();
					// 只有一个参数的方法才能注入
					if (parameterTypes.length != 1) {
						continue;
					}
					// 有一个参数为String的方法，没有必要继续，直接使用此方法注入
					if (parameterTypes[0] == String.class) {
						mtd.invoke(obj, value);//执行相应赋值方法
						return obj;
					}

					methods.add(mtd);
				}
			}
			if (methods.size() == 0) {
				log.warn("对象" + className + "没有指定的方法" + methodName + "，忽略。");
				return null;
			}
			if (methods.size() > 1) {
				log.warn("对象" + className + "有多个同名方法" + methodName + "，无法判断，取第一个方法进行注入。");
			}
			Method mtd = methods.get(0);
			Class<?> requiredType = mtd.getParameterTypes()[0];
			mtd.invoke(obj, convertIfNecessary(methodName, null, value, requiredType)); // 执行相应赋值方法
			return obj;
		} catch (Exception e) {
			log.error(obj + "." + methodName + "(" + value + ")", e);
		}
		return null;
	}
	/**
	 * 根据反射设置一个类的属性,只针对一个value的情况
	 *
	 * @param obj        需要设置属性的类
	 * @param methodName 方法名
	 * @param value      方法参数值
	 * @return 设置完成后类
	 */
	public static Object setObjValue(Object obj, String methodName, Object value) {
		String className = obj.getClass().getCanonicalName();
		try {
			Method[] allMethod = obj.getClass().getMethods();
			List<Method> methods = new ArrayList<Method>();
			for (Method mtd : allMethod) {
				if (mtd.getName().equals(methodName)) {
					Class[] parameterTypes = mtd.getParameterTypes();
					// 只有一个参数的方法才能注入
					if (parameterTypes.length != 1) {
						continue;
					}
					mtd.invoke(obj, value);//执行相应赋值方法
					return obj;
				}
			}
		} catch (Exception e) {
			log.error(obj + "." + methodName + "(" + value + ")", e);
		}
		return null;
	}

	private static Class toPrimitiveWrapperType(Class clazz){
		if(primitiveWrapperTypeMap.containsKey(clazz)){
			return primitiveWrapperTypeMap.get(clazz);
		}
		return clazz;
	}


	/**
	 * Convert the value to the required type (if necessary from a String), for
	 * the specified property.
	 * 
	 * @param propertyName
	 *            name of the property
	 * @param oldValue
	 *            the previous value, if available (may be <code>null</code>)
	 * @param newValue
	 *            the proposed new value
	 * @param requiredType
	 *            the type we must convert to (or <code>null</code> if not
	 *            known, for example in case of a collection element)
	 * @param descriptor
	 *            the JavaBeans descriptor for the property
	 * @param methodParam
	 *            the method parameter that is the target of the conversion (may
	 *            be <code>null</code>)
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException
	 *             if type conversion failed
	 */
	private static Object convertIfNecessary(String propertyName, Object oldValue,
			Object newValue, Class requiredType)
			throws IllegalArgumentException {

		Object convertedValue = newValue;

		if (requiredType != null) {
			// Try to apply some standard type conversion rules if appropriate.

			if (convertedValue != null) {
				if (String.class.equals(requiredType)
						&& ClassUtils.isPrimitiveOrWrapper(convertedValue
								.getClass())) {
					// We can stringify any primitive value...
					return convertedValue.toString();
				}
				// 暂不支持Collection
				/*
				 * else if (requiredType.isArray()) { // Array required -> apply
				 * appropriate conversion of elements. return
				 * convertToTypedArray(convertedValue, propertyName,
				 * requiredType.getComponentType()); } else if (convertedValue
				 * instanceof Collection &&
				 * CollectionFactory.isApproximableCollectionType(requiredType))
				 * { // Convert elements to target type, if determined.
				 * convertedValue = convertToTypedCollection((Collection)
				 * convertedValue, propertyName, methodParam); } else if
				 * (convertedValue instanceof Map &&
				 * CollectionFactory.isApproximableMapType(requiredType)) { //
				 * Convert keys and values to respective target type, if
				 * determined. convertedValue = convertToTypedMap((Map)
				 * convertedValue, propertyName, methodParam); }
				 */
				else if (convertedValue instanceof String
						&& !requiredType.isInstance(convertedValue)) {
					String strValue = ((String) convertedValue).trim();
					
					// 目前支持String和Number
					return NumberUtils.parseNumber(strValue, toPrimitiveWrapperType(requiredType));
					
					// 暂不支持enum
					// Try field lookup as fallback: for JDK 1.5 enum or custom
					// enum
					// with values defined as static fields. Resulting value
					// still needs
					// to be checked, hence we don't return it right away.
/*					try {
						Field enumField = requiredType.getField(strValue);
						convertedValue = enumField.get(null);
					} catch (Throwable ex) {
						if (log.isTraceEnabled()) {
							log.trace("Field [" + convertedValue
									+ "] isn't an enum value", ex);
						}
					}*/
				}
			}

			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				// Definitely doesn't match: throw IllegalArgumentException.
				StringBuffer msg = new StringBuffer();
				msg.append("Cannot convert value of type [").append(
						ClassUtils.getDescriptiveType(newValue));
				msg.append("] to required type [")
						.append(ClassUtils.getQualifiedName(requiredType))
						.append("]");
				if (propertyName != null) {
					msg.append(" for property '" + propertyName + "'");
				}

				throw new IllegalArgumentException(msg.toString());
			}
		}

		return convertedValue;
	}

	/**
	 * 通过属性名称将属性值设置到类中，该方法在多个属性的情况下有效，
	 * 但对于主类的属性用的是接口，但设置的类是实现类的时候会出错
	 * 如 JDBCWriter 有一个属性是接口 dataSource，注入时用的是JDBC,
	 * DataSource，它是DataSource的实现类，此时就会出错
	 *
	 * @param owner      主类
	 * @param methodName 类方法
	 * @param args       参数
	 * @return 设置成功后的类
	 * @throws ReflectException 反射异常
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Object owner, String methodName, Object[] args) throws ReflectException {
		Class ownerClass = owner.getClass();
		Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {

			argsClass[i] = args[i].getClass();
		}

		Method method = null;
		try {
			method = ownerClass.getMethod(methodName, argsClass);
			method.invoke(owner, args);
		} catch (Exception e) {
			log.error("invokeMechod is error" + e);
			throw new ReflectException("invokeMechod is error");
		}

		return owner;
	}

	/**
	 * 通过属性名称将属性值设置到类中,适用于一个属性的设置， 对属性是接口，设置类是实现类的方式有效
	 *
	 * @param owner     主类
	 * @param fieldName 属性名
	 * @param value     属性值
	 * @return 设置属性值后的类
	 * @throws ReflectException 反射异常
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethodByFieldName(Object owner, String fieldName, Object value) throws ReflectException {

		Method method = null;
		try {
			Class ownerClass = owner.getClass();
//			Class typeClass = ownerClass.getDeclaredField(fieldName).getType();
			Field field = findField(ownerClass, fieldName);
			if (field == null) throw new ReflectException("field not found:" + ownerClass.getName() + "." + fieldName);
			Class typeClass = field.getType();
			String methodName = ConfigUtil.getMethodByField(fieldName);
			method = ownerClass.getMethod(methodName, typeClass);
			method.invoke(owner, value);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			throw new ReflectException("invokeMethod is error:" + owner + "." + fieldName + "=" + value);
		}

		return owner;
	}

	private static Field findField(Class ownerClass, String fieldName) {
		// TODO 优化，更高效的查找方法或直接移植spring inject
		for (Field field : ownerClass.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) return field;
		}
		if (ownerClass != Object.class) {
			return findField(ownerClass.getSuperclass(), fieldName);
		}
		return null;
	}
}
