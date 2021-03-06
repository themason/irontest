package io.irontest.models.assertion;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.irontest.models.TestResult;

/**
 * Output of assertion verifier.
 * Created by Zheng on 5/08/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "minClassName")
public class AssertionVerificationResult {
    private TestResult result;
    private String error;            //  message of error occurred during verification

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
