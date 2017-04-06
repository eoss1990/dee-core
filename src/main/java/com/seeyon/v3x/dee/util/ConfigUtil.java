package com.seeyon.v3x.dee.util;


public class ConfigUtil {
	

	/**
	 * @description 通过字段属性得到获取该属性的方法
	 * @date 2011-9-1
	 * @author liuls
	 * @param field 属性
	 * @return 属性的set方法
	 */
	public static String getMethodByField(String field){
		return  "set"+field.replaceFirst(field.substring(0, 1),field.substring(0, 1).toUpperCase()) ;
	}
	/**
	 * @description 使用xsd文件验证xml文件是否合法
	 * @date 2011-8-29
	 * @author liuls
	 * @param targetXML 需要校验的文件
	 * @param xsdSchema  校验用的文件
	 * @return true：符合，false：不符合
	 */
/*	
	@SuppressWarnings("unused")
	private boolean isXMLValid(String targetXML,String xsdSchema) {
        boolean flag = true;
        try {
            Source schemaFile = new StreamSource(
            Thread. currentThread ().getContextClassLoader().getResourceAsStream(xsdSchema) );
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(targetXML)));
        } catch (Exception e) {
            flag = false;
        }

        return flag;
    }*/

}
