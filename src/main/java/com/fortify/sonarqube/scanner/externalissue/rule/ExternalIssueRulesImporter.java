/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.sonarqube.scanner.externalissue.rule;

import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.sonarqube.scanner.externalissue.rule.ReportParser.Report;
import com.fortify.sonarqube.scanner.externalissue.rule.ReportParser.Rule;

public class ExternalIssueRulesImporter {
	private static final Logger LOG = Loggers.get(ExternalIssueRulesImporter.class);

	private final SensorContext context;
	private final Report report;

	public ExternalIssueRulesImporter(SensorContext context, Report report) {
		this.context = context;
		this.report = report;
	}

	public void execute() {
		int ruleCount = 0;

		for (Rule rule : report.rules) {
			importRule(rule);
			ruleCount++;
		}

		LOG.info("Imported {} {}", ruleCount, pluralize("rule", ruleCount));
	}

	private void importRule(Rule rule) {
		context.newAdHocRule()
			.engineId(rule.engineId)
			.ruleId(rule.ruleId)
			.name(rule.name)
			.description(rule.description)
			.type(RuleType.valueOf(rule.type))
			.severity(Severity.valueOf(rule.severity))
			.save();
	}

	private static String pluralize(String msg, int count) {
		if (count == 1) {
			return msg;
		}
		return msg + "s";
	}

}
