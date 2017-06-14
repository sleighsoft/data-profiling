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
    mainLoop(columnNames.size());
    this.emit(minFDs);
  }

  protected FunctionalDependency createFD(ColumnCombinationBitset lhs, ColumnCombinationBitset rhs){
    return new FunctionalDependency( lhs.createColumnCombination(relationName, columnNames),
                              rhs.createColumnCombination(relationName, columnNames).getColumnIdentifiers().toArray(new ColumnIdentifier[]{})[0]);
  }

  List<FunctionalDependency> minFDs = new ArrayList<>();

  protected void mainLoop(int columnCount){
    List<RhsPlusSet> L0 = new ArrayList<RhsPlusSet>();
    L0.add(new RhsPlusSet(new ColumnCombinationBitset(0).removeColumn(0),
            new ColumnCombinationBitset(new int[columnCount])));
    List<RhsPlusSet> L1 = buildLevel(L0, columnCount);
    for(RhsPlusSet l : L1) {
      l.setCandidates(new ColumnCombinationBitset(new int[columnNames.size()]));
    }
    List<RhsPlusSet> LPrev = L1;
    List<RhsPlusSet> L = LPrev;
    int level = 2;
    while(!L.isEmpty()){
      L = buildLevel(L1, columnCount);
      computeDependencies(LPrev, L);
      prune(L);
      L = generateNextLevel(L, level);
      level++;
    }
  }

  protected List<RhsPlusSet> buildLevel(List<RhsPlusSet> prev, int columns) {
    List<RhsPlusSet> next = new ArrayList<RhsPlusSet>();
    for(RhsPlusSet prevSet : prev) {
      for(int col = 0; col < columns; col++) {
        if(!prevSet.getLhs().containsColumn(col)){
          next.add(new RhsPlusSet(prevSet.getLhs().addColumn(col), null));
        }
      }
    }
    return next;
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
      ColumnCombinationBitset difference = columns.minus(currentPrefix);
      if(difference.size() == 1){
        if(level == 1){
            currentBlock.add(element);
        }
        else if(difference.getSetBits().get(0) > currentPrefix.getSetBits().get(currentPrefix.size() - 1)) {
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

    return nextLevel;
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
              if(expected_furtherRefine == 0 && !rhs.isEmpty()) {
                  nextLevel.add(new RhsPlusSet(lhs, rhs));
              }
          }
      }

  }

  protected RhsPlusSet findColumnCombination(ColumnCombinationBitset find, List<RhsPlusSet> list) {
    for(RhsPlusSet set : list) {
      if(set.lhs.equals(find)) {
        return set;
      }
    }
    return null;
  }

  protected void computeDependencies(List<RhsPlusSet> previousLevel, List<RhsPlusSet> thisLevel){
    for(RhsPlusSet set : thisLevel) {
      ColumnCombinationBitset intersection = new ColumnCombinationBitset(new int[columnNames.size()]);
      for(ColumnCombinationBitset oneColumn : set.getLhs().getContainedOneColumnCombinations()){
        RhsPlusSet CPlusWithoutOneColumn = findColumnCombination(set.getLhs().minus(oneColumn), previousLevel);
        intersection.intersect(CPlusWithoutOneColumn.getCandidates());
      }
      set.setCandidates(intersection);
      // Second for loop from paper
      ColumnCombinationBitset XintersectCPlus = set.getLhs().intersect(set.getCandidates());
      PositionListIndex Xpli = getPli(set.getLhs());
      for(ColumnCombinationBitset oneColumn : XintersectCPlus.getContainedOneColumnCombinations()) {
        ColumnCombinationBitset lhs = set.getLhs().minus(oneColumn);
        PositionListIndex XwithoutCPlusPli = getPli(lhs);
        if(Xpli.intersect(XwithoutCPlusPli).equals(Xpli)) {
          minFDs.add(createFD(lhs, oneColumn));
        }
      }
    }
    for(RhsPlusSet base : thisLevel){
      ColumnCombinationBitset candidates = base.getCandidates();
      ColumnCombinationBitset toPrune = new ColumnCombinationBitset();
      for(ColumnCombinationBitset rhs : candidates.getContainedOneColumnCombinations()){

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
    }
  }

  protected void prune(List<RhsPlusSet> thisLevel) {
    List<RhsPlusSet> toPrune = new ArrayList<>();
    for(RhsPlusSet set : thisLevel){
      if(set.getCandidates().isEmpty()){
        toPrune.add(set);
      }
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
