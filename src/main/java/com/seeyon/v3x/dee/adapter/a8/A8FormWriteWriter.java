package com.seeyon.v3x.dee.adapter.a8;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldComBean;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.Adapter;
import com.seeyon.v3x.dee.adapter.InitializingAdapter;
import com.seeyon.v3x.dee.resource.DbDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * 表单回填Writer
 *
 * @author zhangfb
 */
public class A8FormWriteWriter implements Adapter,InitializingAdapter {
    private final static Log log = LogFactory.getLog(A8FormWriteWriter.class);

    /**
     * 名称
     */
    private String name;

    /**
     * 表单ID
     */
    private String formId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 字段名和字段值
     */
    private Map<String, String> fieldMap;

    private DbDataSource dataSource;

    @Override
    public Document execute(Document output) throws TransformException {
        Object masterId = output.getContext().getParameters().getValue("masterId");

        // 获取masterId
        Long lMasterId = exchangeMasterId(masterId);
        // 获取formId
        Long lFormId = exchangeFormId();

        try {
            // 查找表单数据
            FormDataMasterBean formDataMasterBean = FormService.findDataById(lMasterId, lFormId);
            // 获取A8中DEE的处理方式
            Object a8DeeAttitude = output.getContext().getParameters().getValue("a8DeeAttitude");

            if (a8DeeAttitude != null && "start".equals(a8DeeAttitude)) {
                FormBean formBean = FormService.getForm(lFormId);
                FormAuthViewBean formAuthViewBean = (FormAuthViewBean) formDataMasterBean.getExtraAttr(FormConstant.viewRight);
                Map<String, Object> resultMap = new DataContainer();
                AppContext.putThreadContext("resultMap", resultMap);

                for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                    String key = entry.getKey();
                    String[] array = key.split("\\|");
                    if (array.length >= 2) {
                        String fieldName = array[0];
                        String fieldValue = entry.getValue();
                        formDataMasterBean.addFieldValue(fieldName, fieldValue);
                        String s = FormFieldComBean.FormFieldComEnum.getHTML(formBean,
                                formBean.getFieldBeanByName(fieldName),
                                formAuthViewBean.getFields().get(fieldName),
                                formDataMasterBean);
                        resultMap.put(fieldName, s);
                    }
                }
            } else {
                //  非开发高级的阻塞类型任务，需要将表单数据更新到数据库
                for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                    String key = entry.getKey();
                    String[] array = key.split("\\|");
                    if (array.length >= 2) {
                        String fieldName = array[0];
                        String fieldValue = entry.getValue();
                        formDataMasterBean.addFieldValue(fieldName, fieldValue);
                    }
                }
                FormService.saveOrUpdateFormData(formDataMasterBean, lFormId);
            }
        } catch (BusinessException e) {
            log.error(e.getLocalizedMessage(), e);
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return output;
    }

    /**
     * 获取masterId
     *
     * @param masterId 对象masterId
     * @return Long的masterId
     * @throws com.seeyon.v3x.dee.TransformException
     */
    private Long exchangeMasterId(Object masterId) throws TransformException {
        if (masterId == null || !(masterId instanceof Long)) {
            throw new TransformException("masterId获取异常！");
        }
        return (Long) masterId;
    }

    /**
     * 获取formId
     *
     * @return Long的formId
     * @throws com.seeyon.v3x.dee.TransformException
     */
    private Long exchangeFormId() throws TransformException {
        try {
            return Long.parseLong(formId);
        } catch (NumberFormatException e) {
            throw new TransformException("formId转换异常！");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public void setDataSource(DbDataSource dataSource) {}

    @Override
    public void evalParaBeforeExe(Parameters parameters) throws Exception {
        if (fieldMap!=null && fieldMap.size()>0){
            for (Map.Entry<String,String> entry : fieldMap.entrySet()){
                fieldMap.put(entry.getKey(),parameters.evalString(entry.getValue()));
            }
        }
    }
}
