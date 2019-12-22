/* FileName: EppdevMlibUdf.java
 * Copyright jinlong.hao(jinlong.hao@eppdev.cn).  All Rights Preserved!
 * Licensed By EPPDEV License
 */
package cn.eppdev.mlib.sdk.hive.udf;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author jinlong.hao
 */
public class EppdevMlibCalcUDF extends UDF {

    static final String CHARSET_UTF_8 = "UTF-8";

    static final String KEY_WORD_STATUS = "status";

    static final String LOG_ERROR_STRING = "Error: {}";


    static Logger logger = LoggerFactory.getLogger(EppdevMlibCalcUDF.class);


    public Text evaluate(String basicUrl, String modelCode, String postJson) {
        return eppdevMlibCalc(basicUrl, modelCode, postJson);
    }

    public static Text eppdevMlibCalc(String basicUrl, String modelCode, String postJson) {
        Text output = new Text();
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(basicUrl + "/calc/" + modelCode);
        method.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        RequestEntity entity = new StringRequestEntity(postJson);
        method.setRequestEntity(entity);
        try {
            client.executeMethod(method);
            String result = method.getResponseBodyAsString();
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has(KEY_WORD_STATUS)) {
                int status = jsonNode.get(KEY_WORD_STATUS).getIntValue();
                if (status == 2000) {
                    output.set(jsonNode.get("data").toString());
                }
            }
        } catch (IOException e) {
            logger.error(LOG_ERROR_STRING, e);
        } finally {
            method.releaseConnection();
        }
        return output;
    }

    public Text evaluate(String basicUrl, String modelCode, String postJson, String resultField) {
        return eppdevMlibCalc(basicUrl, modelCode, postJson, resultField);
    }

    public static Text eppdevMlibCalc(String basicUrl, String modelCode, String postJson, String resultField) {
        Text output = new Text();
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(basicUrl + "/calc/" + modelCode);
        method.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        RequestEntity requestEntity  = new StringRequestEntity(postJson);
        method.setRequestEntity(requestEntity);
        try {
            client.executeMethod(method);
            String result = method.getResponseBodyAsString();
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has(KEY_WORD_STATUS)) {
                int status = jsonNode.get(KEY_WORD_STATUS).getIntValue();
                if (status == 2000) {
                    JsonNode dataNode = jsonNode.get("data");
                    if (dataNode.has(resultField)) {
                        output.set(dataNode.get(resultField).asText());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(LOG_ERROR_STRING, e);
        } finally {
            method.releaseConnection();
        }
        return output;
    }
}
