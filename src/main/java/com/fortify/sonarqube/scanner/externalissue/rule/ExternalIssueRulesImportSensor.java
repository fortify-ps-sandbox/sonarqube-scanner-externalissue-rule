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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fortify.sonarqube.scanner.externalissue.rule.ReportParser.Report;

public class ExternalIssueRulesImportSensor implements Sensor {
	private static final Logger LOG = Loggers.get(ExternalIssueRulesImportSensor.class);
	static final String REPORT_PATHS_PROPERTY_KEY = "sonar.externalIssuesReportPaths";

	private final Configuration config;

	public ExternalIssueRulesImportSensor(Configuration config) {
		this.config = config;
	}

	public static List<PropertyDefinition> properties() {
		return Collections.singletonList(PropertyDefinition.builder(REPORT_PATHS_PROPERTY_KEY)
				.name("Issues report paths")
				.description(
						"List of comma-separated paths (absolute or relative) containing report with issues created by external rule engines.")
				.category(CoreProperties.CATEGORY_EXTERNAL_ISSUES).onQualifiers(Qualifiers.PROJECT).build());
	}

	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Import external issues report")
				.onlyWhenConfiguration(c -> c.hasKey(REPORT_PATHS_PROPERTY_KEY));
	}

	@Override
	public void execute(SensorContext context) {
		Set<String> reportPaths = loadReportPaths();
		for (String reportPath : reportPaths) {
			LOG.debug("Importing issues from '{}'", reportPath);
			Path reportFilePath = context.fileSystem().resolvePath(reportPath).toPath();
			ReportParser parser = new ReportParser(reportFilePath);
			Report report = parser.parse();
			ExternalIssueRulesImporter issueImporter = new ExternalIssueRulesImporter(context, report);
			issueImporter.execute();
		}
	}

	private Set<String> loadReportPaths() {
		return Arrays.stream(config.getStringArray(REPORT_PATHS_PROPERTY_KEY)).collect(Collectors.toSet());
	}

}