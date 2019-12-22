/* FileName: EppdevMlibUdf.java
 * Copyright jinlong.hao(jinlong.hao@eppdev.cn).  All Rights Preserved!
 * Licensed By EPPDEV License
 */
package cn.eppdev.mlib.sdk.hive.udf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hive.ql.exec.UDF;
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
 *
 * @author jinlong.hao
 */
public class EppdevMlibCalcUDF extends UDF {

    static Logger logger = LoggerFactory.getLogger(EppdevMlibCalcUDF.class);

    static final String LOG_ERROR_STRING = "ERROR: {}";
    static final String CHARSET_UTF_8 = "UTF-8";
    static final String KEY_STATUS = "status";

    public Text evaluate(String basicUrl, String modelCode, String postJson) {
        return eppdevMlibCalc(basicUrl, modelCode, postJson);
    }


    public Text evaluate(String basicUrl, String modelCode, String postJson, String resultField) {
        return eppdevMlibCalc(basicUrl, modelCode, postJson, resultField);
    }

    public static Text eppdevMlibCalc(String basicUrl, String modelCode, String postJson) {
        Text text = new Text();
        // 调用结果进行模型计算
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(basicUrl + "/calc/" + modelCode);
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setEntity(new StringEntity(postJson, CHARSET_UTF_8));
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity,CHARSET_UTF_8);
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has(KEY_STATUS)) {
                int status = jsonNode.get(KEY_STATUS).intValue();
                if (status == 2000) {
                    JsonNode dataNode = jsonNode.get("data");
                    text.set(dataNode.toString());
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
        return text;
    }

    public static Text eppdevMlibCalc(String basicUrl, String modelCode, String postJson, String resultField) {
        Text text = new Text();
        // 调用结果进行模型计算
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(basicUrl + "/calc/" + modelCode);
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setEntity(new StringEntity(postJson, CHARSET_UTF_8));
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, CHARSET_UTF_8);
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            if (jsonNode.has(KEY_STATUS)) {
                int status = jsonNode.get(KEY_STATUS).intValue();
                if (status == 2000) {
                    JsonNode dataNode = jsonNode.get("data");
                    if (dataNode.has(resultField)) {
                        text.set(dataNode.get(resultField).asText());
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
        return text;
    }


}
