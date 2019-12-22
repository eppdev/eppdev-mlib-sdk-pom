/* FileName: EppdevMlibToJson.java
 * Copyright jinlong.hao(jinlong.hao@eppdev.cn).  All Rights Preserved!
 * Licensed By EPPDEV License
 */
package cn.eppdev.mlib.sdk.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

/**
 * @author jinlong.hao
 */
public class EppdevMlibToJsonUDF extends GenericUDF {

    static final Text text = new Text();

    @Override
    public ObjectInspector initialize(ObjectInspector[] objectInspectors) throws UDFArgumentException {
        if (objectInspectors == null || objectInspectors.length % 2 != 0) {
            throw new UDFArgumentException("参数数量必须为2的倍数");
        }
        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] deferredObjects) throws HiveException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < deferredObjects.length; i += 2) {
            sb.append("\"")
                    .append(deferredObjects[i].get())
                    .append("\": ");
            if (! (deferredObjects[i].get() instanceof Number)) {
                sb.append("\"");
            }
            sb.append(deferredObjects[i + 1].get());
            if (!(deferredObjects[i].get() instanceof Number)) {
                sb.append("\"");
            }
            if (i < deferredObjects.length - 2) {
                sb.append(",");
            }
        }
        sb.append("}");
        text.set(sb.toString());
        return text;
    }

    @Override
    public String getDisplayString(String[] strings) {
        return "combine json string";
    }
}
