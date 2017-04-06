package com.seeyon.v3x.dee.adapter;

import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.adapter.xslt.XSLTProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 包装Adapter，并且使用克隆对象
 * Created by yangyu on 2017/2/9.
 */
public class AdapterWrapper implements Adapter {

    private static Log log = LogFactory.getLog(AdapterWrapper.class);

    private static final Set<Class> noSerializeSet;

    private Adapter src;

    private Adapter target;

    static {
        noSerializeSet = new HashSet<Class>();
        noSerializeSet.add(XSLTProcessor.class);
    }

    private AdapterWrapper() {
    }

    public AdapterWrapper(Adapter adapter) throws Exception {
        this.src = adapter;
        this.target = cloneAdapter(adapter);
    }

    @Override
    public Document execute(Document document) throws TransformException {
        /**
         * 不需要序列化的Adapter，则不需要拷贝副本，比如：XSLTProcessor无参数并且有缓存
         */
        if (noSerializeSet.contains(src.getClass()))
            return src.execute(document);
        return target.execute(document);
    }

    /**
     * 通过序列化与反序列化深度clone Adapter
     *
     * @param adapter 原Adapter对象
     * @return 克隆Adapter对象
     * @throws Exception
     */
    private Adapter cloneAdapter(Adapter adapter) throws Exception {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(byteOut));
            out.writeObject(adapter);
            out.flush();
            in = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(byteOut.toByteArray())));
            return (Adapter) in.readObject();
        } catch (Exception e) {
            log.error(e.getStackTrace());
            throw new Exception(e);
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log.error(e.getStackTrace());
                throw new IOException(e);
            }
        }
    }

    public Adapter getAdapter() {
        return target;
    }
}