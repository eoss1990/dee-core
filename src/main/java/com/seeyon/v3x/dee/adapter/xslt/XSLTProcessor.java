package com.seeyon.v3x.dee.adapter.xslt;

import com.seeyon.v3x.dee.*;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import com.seeyon.v3x.dee.util.DocumentUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * xslt转换适配器
 */
public class XSLTProcessor  implements Adapter {
	private static Log log = LogFactory.getLog(XSLTProcessor.class);

	// 缓存XSLT Transformer
	private Transformer transformer;

	private String xslFileName;

	public XSLTProcessor() {
	}

	public XSLTProcessor(String xslFileName) throws TransformerConfigurationException, FileNotFoundException {
		super();
		this.xslFileName = xslFileName;
		// init(xslFileName);
	}

	@Override
	public Document execute(Document input) throws TransformException {
		// 判断output是否为空
		com.seeyon.v3x.dee.Document.Attribute attribute = input
				.getRootElement().getAttribute(DocumentUtil.ATTR_NULL_VALUE);
		if (input.getRootElement().getChildren().size() == 0	// 没有表数据
				|| input.getRootElement().getChildren().get(0).getChild("nullrow") != null	//主表为空数据
				|| (attribute != null && "true".equals(attribute.getValue()))) {	//document属性dee_isNull为true
			throw new TransformException("Document数据为空，不能转换，请检查数据来源。");
		}
		try {
			if (transformer == null) {
				init(xslFileName);
			}
			String xml = DocumentUtil.toXML(input);
			log.debug("XSLT:输入：" + xml);
			Source src = new StreamSource(new StringReader(xml));
			StreamResult result = new StreamResult(new StringWriter());
			if (transformer == null) {
				throw new TransformException("transformer为空，不能转换，请检查您的xsl文件路径。");
			}
			transformer.transform(src, result);
			Document doc = new XMLDataSource(result.getWriter().toString()).parse();
			return doc;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new TransformException("XSLT Error", e);
		}
	}

	private void init(String xslFileName)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, FileNotFoundException,
			UnsupportedEncodingException {
		TransformerFactory factory = TransformerFactory.newInstance();
		transformer = factory.newTransformer(new StreamSource(
				new InputStreamReader(new FileInputStream(TransformFactory
						.getInstance().getConfigFilePath(xslFileName)),
						DEEConstants.CHARSET_UTF8)));

		// new
		// FileReader(TransformFactory.getInstance().getConfigFilePath(xslFileName))
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, DEEConstants.CHARSET_UTF8);
	}

	public String getXsl() {
		return xslFileName;
	}

	public void setXsl(String xslFileName) {
		this.xslFileName = xslFileName;
	}
}
