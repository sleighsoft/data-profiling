package de.metanome.algorithms.lighthousefd;

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

public class LighthouseFDAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected FunctionalDependencyResultReceiver resultReceiver = null;

  protected String relationName;
  protected List<String> columnNames;

  protected List<Candidate> primitives = new ArrayList<>();
  // TODO: initialCapacity can be calculated based on input size
  protected List<Candidate> candidates = new ArrayList();
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
   * Iterates over the candidates list, finds uniques and adds them to the uniques list.
   */
  protected void mainLoop(Candidate dependant, List<Candidate> dependantPrimitives) {
    List<Candidate> nonFDThisLevel = new ArrayList<>();
    List<Candidate> candidatesThisLevel = new ArrayList<>();
    List<ColumnCombinationBitset> minimalFDs = new ArrayList<>();

    candidatesThisLevel.addAll(dependantPrimitives);
    while(!candidatesThisLevel.isEmpty()){
      // check one level
      while(!candidatesThisLevel.isEmpty()){
        Candidate currentCandidate = candidatesThisLevel.remove(0);
        if(isFD(currentCandidate, dependant)){
          minimalFDs.add(currentCandidate.getBitSet());
        }
        else{
          nonFDThisLevel.add(currentCandidate);
        }
      }
      // build next level
      for(Candidate base : nonFDThisLevel) {
        for (Candidate primitive : dependantPrimitives){
          // check that we are actually creating something new
          if(base.getBitSet().containsSubset(primitive.getBitSet())){
            continue;
          }
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
                                                 proposed);
          candidatesThisLevel.add(newCandidate);
        }
      }
    }
    for(ColumnCombinationBitset minimalFD : minimalFDs) {
      FunctionalDependency fd = new FunctionalDependency( minimalFD.createColumnCombination(relationName, columnNames),
                                                          dependant.getBitSet().createColumnCombination(relationName, columnNames).getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0]);
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
      ColumnCombinationBitset bitset = dependant.getBitSet();
      candidates.clear();
      candidates.addAll(primitives);
      candidates.remove(dependant);
      mainLoop(dependant, primitives);
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
