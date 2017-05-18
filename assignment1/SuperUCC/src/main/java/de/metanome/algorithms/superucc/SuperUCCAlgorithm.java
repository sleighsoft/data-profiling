package de.metanome.algorithms.superucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.*;

public class SuperUCCAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected UniqueColumnCombinationResultReceiver resultReceiver = null;

  protected String relationName;
  protected List<String> columnNames;

  protected List<Candidate> primitives = new ArrayList<>();
  // TODO: initialCapacity can be calculated based on input size
  protected PriorityQueue<Candidate> candidates = new PriorityQueue<>(100, new Comparator<Candidate>() {
    @Override
    public int compare(Candidate o1, Candidate o2) {
      return Long.compare(o2.getBoostedScore(), o1.getBoostedScore());
    }
  });
  protected List<Candidate> uniques = new ArrayList<>();
  protected HashSet<ColumnCombinationBitset> alreadySeenColumnCombinations = new HashSet<>();

  protected long numberOfTuples;

  public void execute() throws AlgorithmExecutionException {
    this.initialize();
    // Build PLI for single columns
    PLIBuilder pliBuilder = this.createPLIBuilder();
    List<PositionListIndex> pliList = pliBuilder.getPLIList();
    primitives = new ArrayList<>();
    numberOfTuples = pliBuilder.getNumberOfTuples();
    int column_index = 0;
    // Build primitives (single column candidates)
    for (PositionListIndex pli : pliList) {
      long sum = 0;
      for (LongArrayList l : pli.getClusters()) {
        sum += l.size();
      }
      // Initial primitive candidate score is #DistinctValuesInColumn
      long distinct = numberOfTuples - sum + pli.getClusters().size();
      // Filter out already unique primitives
      Candidate newCandidate = new Candidate(distinct, 1, pli, column_index);
      if (pli.isUnique()) {
        uniques.add(newCandidate);
      } else {
        primitives.add(newCandidate);
      }
      alreadySeenColumnCombinations.add(newCandidate.getBitSet());
      column_index++;
    }
    // Early out in case of all primitives being unique
    if (primitives.size() > 0) {
      candidates.addAll(primitives);
      mainLoop();
    }
    List<UniqueColumnCombination> results = this.generateResults();
    this.emit(results);
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
  protected Candidate combineCandidates(Candidate c1, Candidate c2, ColumnCombinationBitset bitSet) {
    return new Candidate(c1.getScore() * c2.getScore(),
        1,
        c1.getPli().intersect(c2.getPli()),
        bitSet);
  }

  /**
   * Iterates over the candidates list, finds uniques and adds them to the uniques list.
   */
  protected void mainLoop() {
    while (!candidates.isEmpty()) {
      // Get highest ranked candidate
      Candidate bestCandidate = candidates.remove();
      if (bestCandidate.getPli().isUnique()) {
        addUnique(bestCandidate);
        // TODO: boost subsets
      } else {
        // Build new composite candidates with primitives and bestCandidate
        for (Candidate primitive : primitives) {
          // Combine only with new columns
          if (bestCandidate.getBitSet().containsSubset(primitive.getBitSet())) {
            continue;
          }
          // Add only completely new candidates
          ColumnCombinationBitset newCandidateBitSet = bestCandidate.getBitSet().union(primitive.getBitSet());
          if (alreadySeenColumnCombinations.contains(newCandidateBitSet)) {
            continue;
          }
          Candidate newCandidate = combineCandidates(bestCandidate, primitive, newCandidateBitSet);
          candidates.add(newCandidate);
          alreadySeenColumnCombinations.add(newCandidate.getBitSet());
        }

        ArrayList<Candidate> prune = new ArrayList<>();
        for (Candidate c : candidates) {
          if (bestCandidate.getBitSet().containsSubset(c.getBitSet())) {
            prune.add(c);
          }
        }
        candidates.removeAll(prune);
      }
    }
  }

  protected void addSubsets(Candidate c) {
    List<ColumnCombinationBitset> allSubsets = c.getBitSet().getNSubsetColumnCombinations(c.getBitSet().size() - 1);
    ListIterator<ColumnCombinationBitset> it = allSubsets.listIterator();
    while(it.hasNext()) {
      ColumnCombinationBitset next = it.next();
      if (alreadySeenColumnCombinations.contains(next)) {
        it.remove();
      } else {
        new Candidate(next)
      }
    }

  }

  /**
   * Adds a new unique to the uniques list if it is the current minimal unique.
   * Prunes candidates from the candidates and unique list that are no longer needed.
   *
   * @param unique The {@link Candidate} to add to the uniques list.
   */
  protected void addUnique(Candidate unique) {
    ArrayList<Candidate> prune = new ArrayList<>();
    // Prune uniques
    for (Candidate c : uniques) {
      // Do not add if we already have a smaller ucc
      if (unique.getBitSet().containsSubset(c.getBitSet())) {
        return;
      }
      // Remove uniques that are larger than the new one (supersets of the unique)
      if (c.getBitSet().containsSubset(unique.getBitSet())) {
        prune.add(c);
      }
    }
    uniques.removeAll(prune);
    prune.clear();
    // Prune candidates
    for (Candidate c : candidates) {
      // Remove candidates that are smaller than the new unique (subsets of the unique)
      if (c.getBitSet().containsSubset(unique.getBitSet())) {
        prune.add(c);
      }
    }
    candidates.removeAll(prune);
    uniques.add(unique);
  }

  protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
  }

  protected PLIBuilder createPLIBuilder() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input);
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

  protected List<UniqueColumnCombination> generateResults() {
    List<UniqueColumnCombination> results = new ArrayList<>();
    for (Candidate unique : uniques) {
      ColumnCombinationBitset bitset = unique.getBitSet();
      UniqueColumnCombination ucc = new UniqueColumnCombination(bitset.createColumnCombination(relationName, columnNames));
      results.add(ucc);
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
