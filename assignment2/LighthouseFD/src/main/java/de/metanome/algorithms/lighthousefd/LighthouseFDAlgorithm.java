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
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import javafx.util.Pair;

import java.util.*;

public class LighthouseFDAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected UniqueColumnCombinationResultReceiver resultReceiver = null;

  protected String relationName;
  protected List<String> columnNames;

  protected List<Candidate> primitives = new ArrayList<>();
  // TODO: initialCapacity can be calculated based on input size
  protected List<Candidate> candidates = new ArrayList();
  protected List<Tuple<ColumnCombination, ColumnIdentifier>> minimalFDs = new ArrayList<>();

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
    this.emit(minimalFDs);
  }

  /**
   * Combines to candidates into a new one. Creates a new score for the composite candidate.
   *
   * @param c1 Candidate
   * @param c2 Candidate
   * @param bitSet The new bitset for the two candidates. Normally we can calculate the bitset from,
   *               the two candidates, but we want to use the bitset for a 'already seen test' without
   *               already computing the new PLI.
   * @return A new {@link Candidate}
   */
//  protected Candidate combineCandidates(Candidate c1, Candidate c2, ColumnCombinationBitset bitSet) {
//    return new Candidate(c1.getScore() + c2.getScore(),
//        1,
//        c1.getPli().intersect(c2.getPli()),
//        bitSet);
//  }

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

  }

  protected boolean isFD(Candidate determinant, Candidate dependant){
    PositionListIndex determinantPli = determinant.getPli();
    PositionListIndex dependantPli = dependant.getPli();
    if(determinantPli.equals(determinantPli.intersect(dependantPli))){
      return true;
    }
    else return false;
  }

  /**
   * Build all direct subsets of a {@link Candidate}. We do this to prevent the algorithm from omitting potential column
   * combinations.
   * @param c A {@link Candidate}
   */
//  protected void addDirectSubsets(Candidate c) {
//    List<ColumnCombinationBitset> allSubsets = c.getBitSet().getNSubsetColumnCombinations(c.getBitSet().size() - 1);
//    ListIterator<ColumnCombinationBitset> it = allSubsets.listIterator();
//    while(it.hasNext()) {
//      ColumnCombinationBitset next = it.next();
//      if (alreadySeenColumnCombinations.contains(next)) {
//        it.remove();
//      } else if (hasSupersetInUniques(next)){
//        it.remove();
//      } else {
//        List<Integer> setBits = next.getSetBits();
//        long score = 0;
//        PositionListIndex pli = new PositionListIndex();
//        for (Integer i : setBits) {
//          Candidate prim = originalPrimitives.get(i);
//          if(score == 0) {
//            score += prim.getScore();
//            pli = prim.getPli();
//          } else {
//            score += prim.getScore();
//            pli = pli.intersect(prim.getPli());
//          }
//        }
//        candidates.add(new Candidate(score, 0, pli, next));
//        alreadySeenColumnCombinations.add(next);
//      }
//    }
//  }

  /**
   * Checks if the {@link ColumnCombinationBitset} is already contained in the uniques list.
   * @param bitset
   * @return
   */
//  protected boolean hasSupersetInUniques(ColumnCombinationBitset bitset) {
//    for (Candidate u : uniques) {
//      if (u.getBitSet().isSubsetOf(bitset)) {
//        return true;
//      }
//    }
//    return false;
//  }

  /**
   * Adds a new unique to the uniques list if it is the current minimal unique.
   * Prunes candidates from the candidates and unique list that are no longer needed.
   *
   * @param unique The {@link Candidate} to add to the uniques list.
   */
//  protected void addUnique(Candidate unique) {
//    ArrayList<Candidate> prune = new ArrayList<>();
//    // Prune uniques
//    for (Candidate c : uniques) {
//      // Do not add if we already have a smaller ucc
//      if (unique.getBitSet().containsSubset(c.getBitSet())) {
//        return;
//      }
//      // Remove uniques that are larger than the new one (supersets of the unique)
//      if (c.getBitSet().containsSubset(unique.getBitSet())) {
//        prune.add(c);
//      }
//    }
//    uniques.removeAll(prune);
//    prune.clear();
//    // Prune candidates
//    for (Candidate c : candidates) {
//      // Remove candidates that are smaller than the new unique (subsets of the unique)
//      if (c.getBitSet().containsSubset(unique.getBitSet())) {
//        prune.add(c);
//      }
//    }
//    candidates.removeAll(prune);
//    uniques.add(unique);
//  }

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
      mainLoop(dependant);
    }
    return results;
  }

  protected void emit(List<UniqueColumnCombination> results)
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    for (UniqueColumnCombination ucc : results) {
      this.resultReceiver.receiveResult(ucc);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }
}
