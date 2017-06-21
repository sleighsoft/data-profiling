package de.metanome.algorithms.lighthousefd;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class Candidate {
  private ColumnCombinationBitset bitSet;
  private PositionListIndex pli;
  private int lastColumn;

  public Candidate(PositionListIndex pli, int column) {
    this.pli = pli;
    this.bitSet = new ColumnCombinationBitset(column);
    this.lastColumn = column;
  }

  public Candidate(PositionListIndex pli, ColumnCombinationBitset bitSet, int lastColumn) {
    this.pli = pli;
    this.bitSet = bitSet;
  }

  public int getLastColumn(){
    return lastColumn;
  }

  public ColumnCombinationBitset getBitSet() {
    return bitSet;
  }

  public PositionListIndex getPli() {
    return pli;
  }
}
