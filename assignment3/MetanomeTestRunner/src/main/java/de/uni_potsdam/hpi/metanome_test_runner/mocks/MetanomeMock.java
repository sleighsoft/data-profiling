package de.uni_potsdam.hpi.metanome_test_runner.mocks;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.lighthouseind.LighthouseIND;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultCache;
import de.uni_potsdam.hpi.metanome_test_runner.config.Config;
import de.uni_potsdam.hpi.metanome_test_runner.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetanomeMock {

  public static void execute(Config conf) {
    try {
      List<RelationalInputGenerator> inputGenerators = new ArrayList<>();
      for( String datasetName : conf.inputDatasetNames){
        RelationalInputGenerator inputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
                conf.inputFolderPath + datasetName + conf.inputFileEnding, true, conf.inputFileSeparator,
                conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes,
                conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader,
                conf.inputFileSkipDifferingLines, conf.inputFileNullString));

        inputGenerators.add(inputGenerator);
      }

      ResultCache resultReceiver = new ResultCache("MetanomeMock", getAcceptedColumns(inputGenerators));

      LighthouseIND algorithm = new LighthouseIND();
      algorithm.setRelationalInputConfigurationValue(LighthouseIND.Identifier.INPUT_GENERATOR.name(),
              inputGenerators.toArray(new RelationalInputGenerator[inputGenerators.size()]));
      algorithm.setResultReceiver(resultReceiver);

      long runtime = System.currentTimeMillis();
      algorithm.execute();
      runtime = System.currentTimeMillis() - runtime;

      writeResults(conf, resultReceiver, algorithm, runtime);
    } catch (AlgorithmExecutionException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
  public static ResultCache executeWithResult(Config conf) {
    try {
      RelationalInputGenerator inputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
          conf.inputFolderPath + conf.inputDatasetName + conf.inputFileEnding, true, conf.inputFileSeparator,
          conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes,
          conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader,
          conf.inputFileSkipDifferingLines, conf.inputFileNullString));

      ResultCache resultReceiver = new ResultCache("MetanomeMock", getAcceptedColumns(inputGenerator));

      LighthouseIND algorithm = new LighthouseIND();
      algorithm.setRelationalInputConfigurationValue(LighthouseIND.Identifier.INPUT_GENERATOR.name(), inputGenerator);
      algorithm.setResultReceiver(resultReceiver);

      long runtime = System.currentTimeMillis();
      algorithm.execute();
      runtime = System.currentTimeMillis() - runtime;
      System.out.println(algorithm.getClass().getName() + " took " + runtime + " to complete.");
      return resultReceiver;
    } catch (AlgorithmExecutionException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  } */

  private static List<ColumnIdentifier> getAcceptedColumns(List<RelationalInputGenerator> relationalInputGenerators)
      throws InputGenerationException, AlgorithmConfigurationException {
    List<ColumnIdentifier> acceptedColumns = new ArrayList<>();
    for(RelationalInputGenerator generator : relationalInputGenerators) {
      RelationalInput relationalInput = generator.generateNewCopy();
      String tableName = relationalInput.relationName();
      for (String columnName : relationalInput.columnNames())
        acceptedColumns.add(new ColumnIdentifier(tableName, columnName));
    }
    return acceptedColumns;
  }

  private static void writeResults(Config conf, ResultCache resultReceiver, Object algorithm, long runtime)
      throws IOException {
    if (conf.writeResults) {
      String outputPath = conf.measurementsFolderPath + conf.inputDatasetName + "_"
          + algorithm.getClass().getSimpleName() + File.separator;
      List<Result> results = resultReceiver.fetchNewResults();

      FileUtils.writeToFile(algorithm.toString() + "\r\n\r\n" + conf.toString() + "\r\n\r\n" + "Runtime: "
          + runtime + "\r\n\r\n" + "Results: " + results.size(), outputPath + conf.statisticsFileName);
      FileUtils.writeToFile(format(results), outputPath + conf.resultFileName);
    }
  }

  private static String format(List<Result> results) {
    StringBuilder builder = new StringBuilder();
    for (Result result : results) {
      InclusionDependency id = (InclusionDependency) result;
      builder.append(id.toString() + "\r\n");
    }
    return builder.toString();
  }
}
