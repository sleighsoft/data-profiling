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
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;


import java.util.*;

public class LighthouseINDAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected FunctionalDependencyResultReceiver resultReceiver = null;

  protected String relationName;
  protected List<String> columnNames;

  protected List<Candidate> primitives = new ArrayList<>();
  // TODO: initialCapacity can be calculated based on input size
  protected List<FunctionalDependency> finalFDs = new ArrayList<>();

  public void execute() throws AlgorithmExecutionException {
    this.initialize();
    // Build PLI for single columns
    PLIBuilder pliBuilder = this.createPLIBuilder();
    List<PositionListIndex> pliList = pliBuilder.getPLIList();
    primitives = new ArrayList<>();
    int column_index = 0;
    // Build primitives (single column candidates)
    for (PositionListIndex pli : pliList) {
      Candidate newCandidate = new Candidate(pli, column_index);
      primitives.add(newCandidate);
      column_index++;
    }
    this.generateResults();
    this.emit(finalFDs);
  }

  /**
   * Iterates over a single dependant and finds the minimal FDs for it.
   */
  protected void mainLoop(List<Candidate> lhsPrimitives, Candidate rhs) {
    List<Candidate> nonFDThisLevel = new ArrayList<>();
    List<Candidate> lhsCandidates = new ArrayList<>();
    List<ColumnCombinationBitset> minimalFDs = new ArrayList<>();

    lhsCandidates.addAll(lhsPrimitives);
    while(!lhsCandidates.isEmpty()){
      // For each determinant (lhs) check if they define the dependent (rhs)
      // lhsCandidates is empty after this loop
      while(!lhsCandidates.isEmpty()){
        Candidate currentLHS = lhsCandidates.remove(0);
        if(isFD(currentLHS, rhs)){
          minimalFDs.add(currentLHS.getBitSet());
        }
        else{
          nonFDThisLevel.add(currentLHS);
        }
      }
      // build next level
      for(Candidate base : nonFDThisLevel) {
        for (Candidate primitive : lhsPrimitives){
          // Allow only one way to create a column combination
          // AB(110) + C(001) -> ABC(111) & AC(101) + B(010) -> ABC(111)
          // We only allow cases where the primitive column index is greater than the base one
          // AB(110) + C(001) -> ABC(111)
          if(base.getLastColumn() > primitive.getLastColumn()){
            continue;
          }
          // TODO Do we really need the ColumnCombinationBitset or is a fixed length boolean array enough
          ColumnCombinationBitset proposed = base.getBitSet().union(primitive.getBitSet());
          
          // check that we have not found a more minimal combination as FD
          boolean okay = true;
          for(ColumnCombinationBitset minimalFD : minimalFDs){
            if(proposed.containsSubset(minimalFD)){
              okay = false;
              break;
            }
          }
          if(!okay) {
            continue;
          }

          Candidate newCandidate = new Candidate(base.getPli().intersect(primitive.getPli()),
                                                 proposed, primitive.getLastColumn());
          lhsCandidates.add(newCandidate);
        }
      }
      nonFDThisLevel.clear();
    }
    for(ColumnCombinationBitset minimalFD : minimalFDs) {
      FunctionalDependency fd = new FunctionalDependency(minimalFD.createColumnCombination(relationName, columnNames),
                                                          rhs.getBitSet().createColumnCombination(relationName, columnNames).getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0]);
      finalFDs.add(fd);
    }


  }

  protected boolean isFD(Candidate determinant, Candidate dependant){
    PositionListIndex determinantPli = determinant.getPli();
    PositionListIndex dependantPli = dependant.getPli();
    if(determinantPli.equals(determinantPli.intersect(dependantPli))){
      return true;
    }
    else return false;
  }

  protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
  }

  protected PLIBuilder createPLIBuilder() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input, false);
    return pliBuilder;
  }

  protected void print(List<List<String>> records) {

    // Print schema
    System.out.print(this.relationName + "( ");
    for (String columnName : this.columnNames)
      System.out.print(columnName + " ");
    System.out.println(")");

    // Print records
    for (List<String> record : records) {
      System.out.print("| ");
      for (String value : record)
        System.out.print(value + " | ");
      System.out.println();
    }
  }

  protected void generateResults() {

    for (Candidate dependant : primitives) {
      List<Candidate> runPrimitives = new ArrayList<>(primitives);
      runPrimitives.remove(dependant);
      mainLoop(runPrimitives, dependant);
    }
    return;
  }

  protected void emit(List<FunctionalDependency> results)
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    for (FunctionalDependency fd : results) {
      this.resultReceiver.receiveResult(fd);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }
}
