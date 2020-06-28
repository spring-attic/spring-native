package org.springframework.boot.autoconfigure.condition;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationImportListener;

// Due to package private class
public abstract class ConditionProvider {

	public static AutoConfigurationImportListener getConditionEvaluationReportAutoConfigurationImportListener() {
		return new ConditionEvaluationReportAutoConfigurationImportListener();
	}

	public static AutoConfigurationImportFilter getOnBeanCondition() {
		return new OnBeanCondition();
	}

	public static AutoConfigurationImportFilter getOnClassCondition() {
		return new OnClassCondition();
	}

	public static AutoConfigurationImportFilter getOnWebApplicationCondition() {
		return new OnWebApplicationCondition();
	}

}
