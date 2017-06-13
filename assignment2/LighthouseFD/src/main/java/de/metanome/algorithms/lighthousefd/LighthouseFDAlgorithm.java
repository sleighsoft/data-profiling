package de.metanome.algorithms.lighthousefd;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
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
    this.emit(minFDs);
  }

  protected FunctionalDependency createFD(ColumnCombinationBitset lhs, ColumnCombinationBitset rhs){
    return new FunctionalDependency( lhs.createColumnCombination(relationName, columnNames),
                              rhs.createColumnCombination(relationName, columnNames).getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0]);
  }

  List<FunctionalDependency> minFDs = new ArrayList<>();

  protected void newMainLoop(int columnCount){

    List<RhsPlusSet> primitiveColumns = new ArrayList<>();
    for(int i = 0; i < columnCount; i++){
      ColumnCombinationBitset lhs = new ColumnCombinationBitset(i);
      primitiveColumns.add(new RhsPlusSet(lhs, lhs.invert(columnCount)));
    }
    List<RhsPlusSet> thisLevel = new ArrayList<>();

    thisLevel.addAll(primitiveColumns);

    int level = 1;

    while(!thisLevel.isEmpty()){
      computeDependencies(thisLevel);
      prune(thisLevel);
      thisLevel = generateNextLevel(thisLevel, level);
      level++;
    }
  }

  protected PositionListIndex getPli(ColumnCombinationBitset columnBitset){
    List<Integer> columns = columnBitset.getSetBits();
    PositionListIndex Pli= primitives.get(columns.get(0).intValue()).getPli();
    for(int i : columns.subList(1, columns.size())){
      Pli = Pli.intersect(primitives.get(i).getPli());
    }
    return Pli;
  }

  protected void buildNextLevelPlis(){

  }

  protected List<RhsPlusSet> generateNextLevel(List<RhsPlusSet> currentLevel, int level){
    List<RhsPlusSet> nextLevel = new ArrayList<>();
    ColumnCombinationBitset currentPrefix = new ColumnCombinationBitset(currentLevel.get(0).getLhs().getSetBits().subList(0, level-1));
    List<RhsPlusSet> currentBlock = new ArrayList<>();

    for(RhsPlusSet element : currentLevel){
      // Check whether the elements are in the same prefix set
      ColumnCombinationBitset columns = element.getLhs();
      // TODO: correctly find the prefix (though this seems to work)
      ColumnCombinationBitset difference = columns.minus(currentPrefix);
      if(difference.size() == 1){
        if(level == 1){
            currentBlock.add(element);
        }
        else if(difference.getSetBits().get(0) > currentPrefix.getSetBits().get(level-2)) {
          currentBlock.add(element);
        }
      }
      else{
        processBlock(currentBlock, currentLevel, nextLevel);

        // open the next block
        currentBlock.clear();
        currentBlock.add(element);
        currentPrefix = new ColumnCombinationBitset(columns.getSetBits().subList(0, level-1));
      }
    }

    processBlock(currentBlock, currentLevel, nextLevel);

    buildNextLevelPlis();

    System.out.println("the next level has size: " + nextLevel.size());

    return nextLevel;


    // Find the blocks with elements with only one differing lhs
    // Assume the blocks are ordered
    // Iterate over the array, keeping the current prefix and filling the elements into a list
    // this list is a block
    // for each block:
    //  for each pair of two distinct elements in the block:
    //    union lhs bitsets
    //    check whether there is a completely pruned subset
    //    for that: check whether there are enough direct subsets in the list, refine the rhs with each found subset
    // build PLIs for this level
  }

  protected void processBlock(List<RhsPlusSet> block, List<RhsPlusSet> currentLevel,
                                          List<RhsPlusSet> nextLevel){
      // take care of closing the block
      int i = 0;
      for(RhsPlusSet base : block){
          i++;
          for(RhsPlusSet refine : block.subList(i, block.size())){

              ColumnCombinationBitset lhs = base.getLhs().union(refine.getLhs());
              ColumnCombinationBitset rhs = base.getCandidates().intersect(refine.getCandidates());

              int expected_furtherRefine = lhs.getSetBits().size();
              for(RhsPlusSet further_refine : currentLevel){
                  if(further_refine.getLhs().isProperSubsetOf(lhs)){
                      rhs = rhs.intersect(further_refine.getCandidates().intersect(rhs));
                      expected_furtherRefine--;
                  }
              }
              if(expected_furtherRefine > 0){
                  // This candidate was pruned earlier anyways
                  continue;
              }
              if(expected_furtherRefine == 0) {
                  nextLevel.add(new RhsPlusSet(lhs, rhs));
              }
              else {
                  System.out.println("uh, oh, this should never happen!");
              }
          }
      }

  }

  protected void computeDependencies(List<RhsPlusSet> thisLevel){
    for(RhsPlusSet base : thisLevel){
      ColumnCombinationBitset candidates = base.getCandidates();
      ColumnCombinationBitset toPrune = new ColumnCombinationBitset();
      List<ColumnCombinationBitset> keepList;
      for(ColumnCombinationBitset rhs : candidates.getContainedOneColumnCombinations()){
        // This might be wrong or a big speedup
        /*if(rhs.isSubsetOf(toPrune)){
          continue;
        }*/

        ColumnCombinationBitset lhs = base.getLhs().minus(rhs);


        PositionListIndex lhsPli = getPli(lhs);
        PositionListIndex combinedPli = getPli(lhs.union(rhs));
        if(lhsPli.size() == combinedPli.size()){
          minFDs.add(createFD(lhs, rhs));
          toPrune = toPrune.union(rhs);
          //toPrune = toPrune.union(lhs.invert(columnNames.size()));
        }
      }

      base.setCandidates(base.getCandidates().minus(toPrune));
      // for each right hand column
      // test whether the dependency is valid (How do address get the needed PLIs)?
      //  if valid:
      //    save as minimal FD
      //    remove right hand side from RhsPlusSet (prunelist)
      //    keep only lhs on rhs (doable once)

    }
  }

  protected void prune(List<RhsPlusSet> thisLevel) {
    List<RhsPlusSet> toPrune = new ArrayList<>();
    for(RhsPlusSet set : thisLevel){
      if(set.getCandidates().isEmpty()){
        toPrune.add(set);
      }
      // Superkey pruning (complicated, so later)

    }
    thisLevel.removeAll(toPrune);
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

  protected void generateResults() {

    newMainLoop(columnNames.size());
//    for (Candidate dependant : primitives) {
//      ColumnCombinationBitset bitset = dependant.getBitSet();
//      candidates.clear();
//      candidates.addAll(primitives);
//      candidates.remove(dependant);
//      mainLoop(dependant, primitives);
//    }
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
