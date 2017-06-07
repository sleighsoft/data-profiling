package de.metanome.algorithms.lighthousefd;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class Candidate {
  private ColumnCombinationBitset bitSet;
  private PositionListIndex pli;

  public Candidate(PositionListIndex pli, int... columns) {
    this.pli = pli;
    this.bitSet = new ColumnCombinationBitset(columns);
  }

  public Candidate(PositionListIndex pli, ColumnCombinationBitset bitSet) {
    this.pli = pli;
    this.bitSet = bitSet;
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
