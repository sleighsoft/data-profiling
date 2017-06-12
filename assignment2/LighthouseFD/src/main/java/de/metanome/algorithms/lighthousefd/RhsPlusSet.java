package de.metanome.algorithms.lighthousefd;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

public class RhsPlusSet {
    protected ColumnCombinationBitset lhs;
    protected ColumnCombinationBitset candidates;

    RhsPlusSet(ColumnCombinationBitset lhs, ColumnCombinationBitset candidates){
        this.lhs = lhs;
        this.candidates = candidates;
    }

    public ColumnCombinationBitset getLhs() {
        return lhs;
    }

    public ColumnCombinationBitset getCandidates() {
        return candidates;
    }

    public void setCandidates(ColumnCombinationBitset candidates) {
        this.candidates = candidates;
    }
}
