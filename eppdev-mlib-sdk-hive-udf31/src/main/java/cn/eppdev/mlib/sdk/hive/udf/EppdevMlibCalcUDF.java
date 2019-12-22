/* FileName: EppdevMlibUdf.java
 * Copyright jinlong.hao(jinlong.hao@eppdev.cn).  All Rights Preserved!
 * Licensed By EPPDEV License
 */
package cn.eppdev.mlib.sdk.hive.udf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author jinlong.hao
 */
public class EppdevMlibCalcUDF extends GenericUDF {

    static Logger logger = LoggerFactory.getLogger(EppdevMlibCalcUDF.class);

    @Override
    public ObjectInspector initialize(ObjectInspector[] objectInspectors) throws UDFArgumentException {
        checkArgsSize(objectInspectors, 3, 4);
        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] deferredObjects) throws HiveException {

        // 获取参数
        String basicUrl = deferredObjects[0].get().toString();
        String modelCode = deferredObjects[1].get().toString();
        String postJson = deferredObjects[2].get().toString();

        if (deferredObjects.length == 3) {
            return eppdevMlibCalc(basicUrl, modelCode, postJson);
        } else {
            String resultField = deferredObjects[3].get().toString();
            return eppdevMlibCalc(basicUrl, modelCode, postJson, resultField);
        }
    }


    public static Object eppdevMlibCalc(String basicUrl, String modelCode, String postJson) {
        Text output = new Text();
        // 调用结果进行模型计算
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(basicUrl + "/calc/" + modelCode);
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setEntity(new StringEntity(postJson, "UTF-8"));
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has("status")) {
                int status = jsonNode.get("status").intValue();
                if (status == 2000) {
                    JsonNode dataNode = jsonNode.get("data");
                    output.set(dataNode.toString());
                }
            }
        } catch (IOException e) {
            logger.error("error: {}", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("error: {}", e);
            }
        }
        return output;
    }

    public Object eppdevMlibCalc(String basicUrl, String modelCode, String postJson, String resultField) {
        Text output = new Text();
        // 调用结果进行模型计算
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(basicUrl + "/calc/" + modelCode);
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setEntity(new StringEntity(postJson, "UTF-8"));
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has("status")) {
                int status = jsonNode.get("status").intValue();
                if (status == 2000) {
                    JsonNode dataNode = jsonNode.get("data");
                    if (dataNode.has(resultField)) {
                        output.set(dataNode.get(resultField).asText());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("error: {}", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("error: {}", e);
            }
        }
        return output;
    }

    @Override
    public String getDisplayString(String[] strings) {
        return getStandardDisplayString("EPPDEV_MLIB_CALC", strings, ",");
    }
}
