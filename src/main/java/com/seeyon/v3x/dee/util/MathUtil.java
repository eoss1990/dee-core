package com.seeyon.v3x.dee.util;

import java.math.BigDecimal;


/**
 * 数字相关工具类
 *
 */
public class MathUtil {
	
	/**
	 * 转换为double<br>
	 * 如果给定的值为空，或者转换失败，返回默认值<br>
	 * 转换失败不会报错
	 * 
	 * @param value 被转换的值
	 * @param defaultValue 转换错误时的默认值
	 * @return 结果
	 */
	public static Double toDouble(Object value, Double defaultValue) {
		if (value == null){
			return defaultValue;
		}
		if(value instanceof Double) {
			return (Double)value;
		}
		if(value instanceof Number){
			return ((Number) value).doubleValue();
		}
		final String valueStr =String.valueOf(value);
		if (StrUtil.isBlank(valueStr)){
			return defaultValue;
		}
		try {
			//支持科学计数法
			return new BigDecimal(valueStr.trim()).doubleValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * 转换为double<br>
	 * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
	 * 转换失败不会报错
	 * 
	 * @param value 被转换的值
	 * @return 结果
	 */
	public static Double toDouble(Object value) {
		return toDouble(value, null);
	}
	
	/**
	 * 保留小数位
	 * @param number 被保留小数的数字
	 * @param digit 保留的小数位数
	 * @return 保留小数后的字符串
	 */
	public static String roundStr(double number, int digit) {
		return String.format("%."+digit + 'f', number);
	}
	
	/**
	 * 保留小数位
	 * @param number 被保留小数的数字
	 * @param digit 保留的小数位数
	 * @return 保留小数后的字符串
	 */
	public static double round(double number, int digit) {
		final BigDecimal bg = new BigDecimal(number);
		return bg.setScale(digit, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 数字金额大写转换
	 * 先写个完整的然后将如零拾替换成零
	 * @param n 数字
	 * @return 中文大写数字
	 */
	public static String math_digitUppercase(Object number) {
		Double n = toDouble(number);
		String fraction[] = { "角", "分" };
		String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
		String unit[][] = { { "元", "万", "亿" }, { "", "拾", "佰", "仟" } };

		String head = n < 0 ? "负" : "";
		n = Math.abs(n);

		String s = "";
		for (int i = 0; i < fraction.length; i++) {
			s += (digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
		}
		if (s.length() < 1) {
			s = "整";
		}
		int integerPart = (int) Math.floor(n);

		for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
			String p = "";
			for (int j = 0; j < unit[1].length && n > 0; j++) {
				p = digit[integerPart % 10] + unit[1][j] + p;
				integerPart = integerPart / 10;
			}
			s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
		}
		return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
	}
	
	
	public static double math_add(Object number1, Object number2) {// 加法
		double v1 = toDouble(number1);
		double v2 = toDouble(number2);
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	public static double math_sub(Object number1, Object number2) {// 减法
		double v1 = toDouble(number1);
		double v2 = toDouble(number2);
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	public static double math_mul(Object number1, Object number2) {// 乘法
		double v1 = toDouble(number1);
		double v2 = toDouble(number2);
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	public static double math_div(Object number1, Object number2) {// 除法
		double v1 = toDouble(number1);
		double v2 = toDouble(number2);
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
}
