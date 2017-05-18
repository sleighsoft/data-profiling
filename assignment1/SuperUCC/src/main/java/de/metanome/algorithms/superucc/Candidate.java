package de.metanome.algorithms.superucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

import java.util.Arrays;
import java.util.List;

public class Candidate {
  private long score;
  private double boost;
  private ColumnCombinationBitset bitSet;
  private PositionListIndex pli;
  private List<Candidate> compositeCandidates;

  public Candidate(long score, double boost, PositionListIndex pli, int... columns) {
    this.score = score;
    this.boost = boost;
    this.pli = pli;
    this.bitSet = new ColumnCombinationBitset(columns);
  }

  public Candidate(long score, double boost, PositionListIndex pli, ColumnCombinationBitset bitSet) {
    this.score = score;
    this.boost = boost;
    this.pli = pli;
    this.bitSet = bitSet;
  }

  public Candidate(long score, double boost, ColumnCombinationBitset bitSet, Candidate... compositeCandidates) {
    this(score, boost, null, bitSet);
    this.compositeCandidates = Arrays.asList(compositeCandidates);
  }

  public void combineCandidates() {
    if (compositeCandidates != null) {
      PositionListIndex pli = compositeCandidates.get(0).getPli();
      for (Candidate c : compositeCandidates.subList(1, compositeCandidates.size())) {
        pli = pli.intersect(c.getPli());
      }
      this.pli = pli;
    }
  }

  public long getBoostedScore() {
    return (long) (score * boost);
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
  }

  public double getBoost() {
    return boost;
  }

  public void setBoost(double boost) {
    this.boost = boost;
  }

  public ColumnCombinationBitset getBitSet() {
    return bitSet;
  }

  public void setBitSet(ColumnCombinationBitset bitSet) {
    this.bitSet = bitSet;
  }

  public PositionListIndex getPli() {
    return pli;
  }

  public void setPli(PositionListIndex pli) {
    this.pli = pli;
  }
}
