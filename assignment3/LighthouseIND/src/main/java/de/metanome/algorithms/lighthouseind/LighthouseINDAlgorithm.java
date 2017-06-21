package de.metanome.algorithms.lighthouseind;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;


import java.util.*;

public class LighthouseINDAlgorithm {

  protected RelationalInputGenerator[] inputGenerators = null;
  protected InclusionDependencyResultReceiver resultReceiver = null;

  protected List<InclusionDependency> finalINDs = new ArrayList<>();

  public void execute() throws AlgorithmExecutionException {
    this.initialize();
    // Build PLI for single columns
    PLIBuilder pliBuilder = this.createPLIBuilder();
    List<PositionListIndex> pliList = pliBuilder.getPLIList();

    this.generateResults();
    this.emit(finalINDs);
  }

  protected void generateResults() {
    /*
     * For each generator in inputGenerators do
     *
     * RelationalInput input = generator.generateNewCopy();
     * String relationName = input.relationName();
     * String columnNames = input.columnNames();
     */
  }
  
  protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
  }

  protected PLIBuilder createPLIBuilder(RelationalInput generator) throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    RelationalInput input = generator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input, false);
    return pliBuilder;
  }

  protected void emit(List<InclusionDependency> results)
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    for (InclusionDependency fd : results) {
      this.resultReceiver.receiveResult(fd);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }
}
