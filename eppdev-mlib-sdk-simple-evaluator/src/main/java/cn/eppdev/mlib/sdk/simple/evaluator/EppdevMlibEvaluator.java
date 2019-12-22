/* FileName: EppdevMlibEvaluator.java
 * Copyright jinlong.hao(jinlong.hao@eppdev.cn).  All Rights Preserved!
 * Licensed By EPPDEV License
 */
package cn.eppdev.mlib.sdk.simple.evaluator;

import cn.eppdev.mlib.sdk.simple.commons.EppdevMlibException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.visitors.DefaultVisitorBattery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jinlong.hao
 */
public class EppdevMlibEvaluator {

    static Logger logger = LoggerFactory.getLogger(EppdevMlibEvaluator.class);

    public static EppdevMlibEvaluator getInstance(String pmmlContent) throws EppdevMlibException {
        return new EppdevMlibEvaluator(pmmlContent);
    }

    private EppdevMlibEvaluator(String pmmlContent) throws EppdevMlibException {
        try {
            Evaluator evaluator = new LoadingModelEvaluatorBuilder().setLocatable(false)
                    .setVisitors(new DefaultVisitorBattery())
                    // .setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                    .load(new ByteArrayInputStream(pmmlContent.getBytes())).build();
            this.evaluator = evaluator.verify();
        } catch (SAXException | JAXBException e) {
            logger.error("Error when parse pmml: {}", e);
        }
        throw new EppdevMlibException("解析pmml失败");

    }

    private Evaluator evaluator = null;

    private Map<String, ?> evaluate(String json) throws EppdevMlibException {
        List<? extends InputField> inputFields = evaluator.getInputFields();
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            for (InputField inputField : inputFields) {
                if (jsonNode.has(inputField.getFieldName().getValue())) {
                    JsonNode valueNode = jsonNode.get(inputField.getFieldName().getValue());
                    arguments.put(inputField.getFieldName(),
                            FieldValue.create(inputField.getDataType(),
                                    inputField.getOpType(), valueNode.textValue()));
                }
            }
        } catch (IOException e) {
            logger.error("json解析错误： {}", e);
            throw new EppdevMlibException("计算错误：" + e.getMessage());
        }
        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        return EvaluatorUtil.decodeAll(results);
    }

    public String calc(String json) throws EppdevMlibException {
        try {
            return new ObjectMapper().writeValueAsString(evaluate(json));
        } catch (JsonProcessingException e) {
            logger.error("Error: {}", e);
            throw new EppdevMlibException("结果解析错误：" + e.getMessage());
        }
    }


    public String calc(String json, String fieldName) throws EppdevMlibException {
        Map<String, ?> map = evaluate(json);
        if (map.containsKey(fieldName)) {
            return map.get(fieldName).toString();
        } else {
            throw new EppdevMlibException("结果不存在");
        }
    }
}
