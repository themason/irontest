package io.irontest.core.assertion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.irontest.models.TestResult;
import io.irontest.models.assertion.Assertion;
import io.irontest.models.assertion.AssertionVerificationResult;
import io.irontest.models.assertion.JSONPathXMLEqualAssertionProperties;
import io.irontest.models.assertion.JSONPathXMLEqualAssertionVerificationResult;
import io.irontest.utils.XMLUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhenw9 on 15/02/2017.
 */
public class JSONPathXMLEqualAssertionVerifier implements AssertionVerifier {
    public AssertionVerificationResult verify(Assertion assertion, Object input) throws Exception {
        JSONPathXMLEqualAssertionProperties otherProperties =
                (JSONPathXMLEqualAssertionProperties) assertion.getOtherProperties();

        //  validate other properties
        if ("".equals(StringUtils.trimToEmpty(otherProperties.getJsonPath()))) {
            throw new IllegalArgumentException("JSONPath not specified");
        } else if ("".equals(StringUtils.trimToEmpty(otherProperties.getExpectedXML()))) {
            throw new IllegalArgumentException("Expected XML not specified");
        } else {
            try {
                XMLUtils.xmlStringToDOM(otherProperties.getExpectedXML());
            } catch (Exception e) {
                throw new IllegalArgumentException("Expected XML is not a valid XML. ", e);
            }
        }

        Object actualValue = JsonPath.read((String) input, otherProperties.getJsonPath());
        if (!(actualValue instanceof String)) {
            ObjectMapper objectMapper = new ObjectMapper();
            throw new Exception("JSONPath does not evaluate to a string. It evaluates to:\n " + objectMapper.writeValueAsString(actualValue));
        } else {
            try {
                XMLUtils.xmlStringToDOM((String) actualValue);
            } catch (Exception e) {
                throw new Exception("JSONPath does not evaluate to an XML. It evaluates to a normal string:\n " + actualValue);
            }
        }

        JSONPathXMLEqualAssertionVerificationResult result = new JSONPathXMLEqualAssertionVerificationResult();
        String actualXML = (String) actualValue;
        result.setActualXML(actualXML);
        StringBuilder differencesSB = XMLUtils.compareXML(otherProperties.getExpectedXML(), actualXML);
        result.setResult(differencesSB.length() > 0 ? TestResult.FAILED : TestResult.PASSED);
        return result;
    }
}
