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

    String leftValue;
    String rightValue;

    // Assumption: left is dependant, right is referenced

    while(leftIndex < leftValues.size() && rightIndex < rightValues.size()){
      leftValue = leftValues.get(leftIndex);
      rightValue = rightValues.get(rightIndex);

      switch(signum(leftValue.compareTo(rightValue))){
        case -1:
          // left < right
          // left contains a value that is not contained in right
          // => error
          return;
        case 0:
          // left == right
          // everything okay, step both values
          leftIndex++;
          rightIndex++;
          break;
        case 1:
          // left > right
          // right contains a value that is not contained in left
          // skip this value in right, keep left pinned
          rightIndex++;
          break;
      }
    }

    if(rightIndex == rightValues.size()){
      return;
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
      List<List<String>> columns = new ArrayList<>();

      for(int i = 0; i < columnNames.size(); i++){
        columns.add(new ArrayList<String>());
      }

      while (input.hasNext()) {
        List<String> columnValues = input.next();
        int i = 0;
        for(String value : columnValues){
          columns.get(i).add(value);
          i++;
        }
      }

      for(int i = 0; i < columnNames.size(); i++) {
        candidates.add(new Candidate(columns.get(i), relationName, columnNames.get(i)));
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
