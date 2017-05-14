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
  protected PriorityQueue<Candidate> candidates = new PriorityQueue<>(100, new Comparator<Candidate>() {
    @Override
    public int compare(Candidate o1, Candidate o2) {
      return Long.compare(o2.getBoostedScore(), o1.getBoostedScore());
    }
  });
  protected List<Candidate> uniques = new ArrayList<>();

  protected long numberOfTuples;

  public void execute() throws AlgorithmExecutionException {

    ////////////////////////////////////////////
    // THE DISCOVERY ALGORITHM LIVES HERE :-) //
    ////////////////////////////////////////////
    // Example: Initialize
    this.initialize();
    // Build PLI
    PLIBuilder pliBuilder = this.createPLIBuilder();
    List<PositionListIndex> pliList = pliBuilder.getPLIList();
    primitives = new ArrayList<>();
    numberOfTuples = pliBuilder.getNumberOfTuples();
    int index = 0;
    for (PositionListIndex pli : pliList) {
      long sum = 0;
      for (LongArrayList l : pli.getClusters()) {
        sum += l.size();
      }
      long distinct = numberOfTuples - sum + pli.getClusters().size();
      primitives.add(new Candidate(distinct, 1, pli, index));
      index++;
    }

    ArrayList<Candidate> uniquePrimitives = new ArrayList<>();
    // Find unique one element candidates
    for (Candidate candidate : primitives) {
      if (candidate.getScore() == numberOfTuples) {
        uniquePrimitives.add(candidate);
      }
    }

    primitives.removeAll(uniquePrimitives);
    uniques.addAll(uniquePrimitives);

    // TODO: Catch special case if only one primitive is in the list
    candidates.addAll(primitives);
    mainLoop();

    // Example: Generate some results (usually, the algorithm should really calculate them on the data)
    List<UniqueColumnCombination> results = this.generateResults();
    // Example: To test if the algorithm outputs results
    this.emit(results);
    /////////////////////////////////////////////

  }

  protected Candidate createCandidate(Candidate c1, Candidate c2) {
    return new Candidate(c1.getScore() * c2.getScore(),
        1,
        c1.getPli().intersect(c2.getPli()),
        c1.getBitSet().union(c2.getBitSet()));
  }

  protected void mainLoop() {
    while (!candidates.isEmpty()) {
      Candidate bestCandidate = candidates.remove();
      // TODO: Refactor tests/candidate PLI building to be lazy
      if (bestCandidate.getPli().isUnique()) {
        addUnique(bestCandidate);
        // TODO: boost subsets
      } else {
        for (Candidate primitive : primitives) {
          if (bestCandidate.getBitSet().containsSubset(primitive.getBitSet())) {
            continue;
          }
          ColumnCombinationBitset newCandidateBitSet = bestCandidate.getBitSet().union(primitive.getBitSet());
          // test whether we already have this candidate
          boolean found = false;
          for (Candidate c : candidates) {
            if (c.getBitSet().equals(newCandidateBitSet)) {
              found = true;
              break;
            }
          }
          if (found) {
            continue;
          }
          candidates.add(createCandidate(bestCandidate, primitive));
        }

        ArrayList<Candidate> prune = new ArrayList<>();
        for (Candidate c : candidates) {
          if (bestCandidate.getBitSet().containsSubset(c.getBitSet())) {
            prune.add(c);
          }
        }
        candidates.removeAll(prune);
      }
      // if ! unique : Update Scores/Discover new candidates, prune candidates
      // else: update uniques and prune uniques, boost subsets
    }
  }

  protected void addUnique(Candidate unique) {
    ArrayList<Candidate> prune = new ArrayList<>();
    // prune unique list
    for (Candidate c : uniques) {
      if (unique.getBitSet().containsSubset(c.getBitSet())) return;
      if (c.getBitSet().containsSubset(unique.getBitSet())) {
        prune.add(c);
      }
    }
    uniques.removeAll(prune);
    prune.clear();
    // prune candidate list
    for (Candidate c : candidates) {
      if (c.getBitSet().containsSubset(unique.getBitSet())) {
        prune.add(c);
      }
    }
    // add
    uniques.add(unique);
  }

  protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
  }

  protected List<List<String>> readInput()
      throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    // TODO: This is probably overly eager, tanking the runtime of the algorithm (If IO and not cache bound)
    List<List<String>> records = new ArrayList<>();
    // TODO: GenerateNewCopy reads data from disk again
    RelationalInput input = this.inputGenerator.generateNewCopy();
    while (input.hasNext())
      records.add(input.next());
    return records;
  }

  protected PLIBuilder createPLIBuilder() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
    RelationalInput input = this.inputGenerator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input);
    return pliBuilder;
  }

  protected void createLattice() {

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
