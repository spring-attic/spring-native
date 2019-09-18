/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.ResultsWriterFactory;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.util.FileUtils;
import org.openjdk.jmh.util.ScoreFormatter;
import org.openjdk.jmh.util.Statistics;

/**
 * @author Dave Syer
 *
 */
public class CsvResultsWriterFactory implements ResultsWriterFactory {

    @Override
    public ResultsWriter forUri(String uri) {
        if (!uri.startsWith("csv:")) {
            return null;
        }
        return new ResultsWriter() {

            @Override
            public void write(OutputFormat output, Collection<RunResult> results) {
                StringBuilder report = new StringBuilder();
                try {
                    Map<String, Integer> params = new LinkedHashMap<>();
                    int paramPlaces = 0;
                    for (RunResult result : results) {
                        for (String param : result.getParams().getParamsKeys()) {
                            int count = paramPlaces;
                            params.computeIfAbsent(param, key -> count);
                            paramPlaces++;
                        }
                    }
                    Map<String, Integer> auxes = new LinkedHashMap<>();
                    int auxPlaces = 0;
                    for (RunResult result : results) {
                        @SuppressWarnings("rawtypes")
                        Map<String, Result> second = result.getAggregatedResult()
                                .getSecondaryResults();
                        if (second != null) {
                            for (String aux : second.keySet()) {
                                int count = auxPlaces;
                                auxes.computeIfAbsent(aux, key -> count);
                                auxPlaces++;
                            }
                        }
                    }
                    StringBuilder header = new StringBuilder();
                    header.append("class, method, ");
                    params.forEach((key, value) -> header.append(key).append(", "));
                    auxes.forEach((key, value) -> header.append(propertyName(key))
                            .append(", "));
                    header.append("median, mean, range");
                    report.append(header.toString()).append(System.lineSeparator());
                    for (RunResult result : results) {
                        StringBuilder builder = new StringBuilder();
                        String benchmark = result.getParams().getBenchmark();
                        String cls = benchmark.substring(0, benchmark.lastIndexOf("."));
                        String mthd = benchmark.substring(benchmark.lastIndexOf(".") + 1);
                        builder.append(cls).append(", ").append(mthd).append(", ");
                        for (int i = 0; i < params.values().size(); i++) {
                            boolean found = false;
                            for (String param : result.getParams().getParamsKeys()) {
                                if (params.get(param) == i) {
                                    builder.append(result.getParams().getParam(param))
                                            .append(", ");
                                    found = true;
                                }
                            }
                            if (!found) {
                                builder.append(", ");
                            }
                        }
                        @SuppressWarnings("rawtypes")
                        Map<String, Result> second = result.getAggregatedResult()
                                .getSecondaryResults();
                        if (second != null) {
                            for (int i = 0; i < auxes.values().size(); i++) {
                                boolean found = false;
                                for (String param : second.keySet()) {
                                    if (auxes.get(param) == i) {
                                        builder.append(ScoreFormatter
                                                .format(second.get(param).getStatistics()
                                                        .getPercentile(0.5)))
                                                .append(", ");
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    builder.append(", ");
                                }
                            }
                        }
                        Statistics statistics = result.getPrimaryResult().getStatistics();
                        builder.append(
                                ScoreFormatter.format(statistics.getPercentile(0.5)));
                        builder.append(", ");
                        builder.append(ScoreFormatter.format(statistics.getMean()));
                        builder.append(", ");
                        double error = (statistics.getMax() - statistics.getMin()) / 2;
                        builder.append(ScoreFormatter.format(error));
                        report.append(builder.toString()).append(System.lineSeparator());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                output.println(report.toString());
                if (uri != null) {
                    File file = new File(uri.substring("csv:".length()));
                    file.getParentFile().mkdirs();
                    if (file.getParentFile().exists()) {
                        try {
                            FileUtils.writeLines(file,
                                    Collections.singleton(report.toString()));
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            private String propertyName(String key) {
                if (key.matches("get[A-Z].*")) {
                    key = changeFirstCharacterCase(key.substring(3), false);
                }
                return key;
            }
        };
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (str == null || str.length() == 0) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        }
        else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

}
