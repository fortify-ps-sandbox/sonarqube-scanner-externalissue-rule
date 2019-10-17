/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ReportParser {
	private Gson gson = new Gson();
	private Path filePath;

	public ReportParser(Path filePath) {
		this.filePath = filePath;
	}

	public Report parse() {
		try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			return validate(gson.fromJson(reader, Report.class));
		} catch (JsonIOException | IOException e) {
			throw new IllegalStateException("Failed to read external rules report '" + filePath + "'", e);
		} catch (JsonSyntaxException e) {
			throw new IllegalStateException(
					"Failed to read external rules report '" + filePath + "': invalid JSON syntax", e);
		}
	}

	private Report validate(Report report) {
		if (report.rules != null) {
			for (Rule rule : report.rules) {
				mandatoryField(rule.engineId, "engineId");
				mandatoryField(rule.ruleId, "ruleId");
				mandatoryField(rule.name, "name");
				mandatoryField(rule.severity, "severity");
				mandatoryField(rule.type, "type");
				mandatoryField(rule.description, "description");
			}
		}
		return report;
	}

	private void mandatoryField(@Nullable String value, String fieldName) {
		if (StringUtils.isBlank(value)) {
			throw new IllegalStateException(
					String.format("Failed to parse report '%s': missing mandatory field '%s'.", filePath, fieldName));
		}
	}

	static class Report {
		Rule[] rules;

		public Report() {
			// http://stackoverflow.com/a/18645370/229031
		}
	}

	static class Rule {
		String engineId;
		String ruleId;
		String name;
		String severity;
		String type;
		String description;

		public Rule() {
			// http://stackoverflow.com/a/18645370/229031
		}
	}
}