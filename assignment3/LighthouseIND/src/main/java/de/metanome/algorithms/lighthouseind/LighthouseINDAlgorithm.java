package de.metanome.algorithms.lighthouseind;

import de.metanome.algorithm_integration.*;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;



import java.util.*;

import static java.lang.Integer.signum;

public class LighthouseINDAlgorithm {

  protected RelationalInputGenerator[] inputGenerators = null;
  protected InclusionDependencyResultReceiver resultReceiver = null;

  protected List<InclusionDependency> finalINDs = new ArrayList<>();
  protected List<Candidate> candidates = new ArrayList<>();

  public void execute() throws AlgorithmExecutionException {
    this.initialize();

    this.generateResults();
    this.emit(finalINDs);
  }

  protected void generateResults() {
    ListIterator<Candidate> lhsIt = candidates.listIterator();
    while(lhsIt.hasNext()){
      Candidate lhs = lhsIt.next();

      ListIterator<Candidate> rhsIt = candidates.listIterator(lhsIt.nextIndex());

      while(rhsIt.hasNext()){
        Candidate rhs = rhsIt.next();

        checkCombination(lhs, rhs);
      }
    }
  }

  protected void checkCombination(Candidate lhs, Candidate rhs) {
    int leftIndex = 0;
    int rightIndex = 0;

    List<String> leftValues = lhs.getValues();
    List<String> rightValues = rhs.getValues();

    String leftValue = leftValues.get(leftIndex);
    String rightValue = rightValues.get(rightIndex);

    while(leftIndex < leftValues.size()){
      int comparison = leftValue.compareTo(rightValue);

      if(comparison < 1)


      switch(signum(leftValue.compareTo(rightValue))){
        case -1:
          rightIndex++;
          break;
        case 0:
          rightIndex++;
          leftIndex++;
          break;
        case 1:
          return;
      }
    }

    finalINDs.add(new InclusionDependency(lhs.getPerm(), rhs.getPerm()));
  }
  
  protected void initialize() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    /*
     * For each generator in inputGenerators do
     *
     * RelationalInput input = generator.generateNewCopy();
     * String relationName = input.relationName();
     * String columnNames = input.columnNames();
     */

    for(RelationalInputGenerator inputGenerator : inputGenerators) {
      RelationalInput input = inputGenerator.generateNewCopy();

      String relationName = input.relationName();
      List<String> columnNames = input.columnNames();

      int i = 0;
      while (input.hasNext()) {
        candidates.add(createCandidate(input.next(), relationName, columnNames.get(i)));
        i++;
      }
    }

    java.util.Collections.sort (candidates, new Comparator<Candidate>() {
      public int compare(Candidate o1, Candidate o2) {
          // Intentional: Reverse order for this demo
          return o2.compareTo(o1);
      }
    });
  }

  protected Candidate createCandidate(List<String> column, String relationName, String columnName) {
    Set<String> uniques = new HashSet<>(column);
    column = new ArrayList<>(uniques);
    java.util.Collections.sort(column);
    return new Candidate(column, relationName, columnName);
  }

  protected void emit(List<InclusionDependency> results)
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    for (InclusionDependency id : results) {
      this.resultReceiver.receiveResult(id);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }
}
