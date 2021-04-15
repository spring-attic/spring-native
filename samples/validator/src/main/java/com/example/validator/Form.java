package com.example.validator;

import javax.validation.constraints.Min;

public class Form {

    @Min(0)
    Integer testIntMin;

    public Form() {}

    public Integer getTestIntMin() {
        return testIntMin;
    }

    public void setTestIntMin(Integer testIntMin) {
        this.testIntMin = testIntMin;
    }
}
