package de.metanome.algorithms.lighthouseind;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;

import java.util.List;

/**
 * Created by florian on 6/21/17.
 */
public class Candidate implements Comparable<Candidate>{
    // TODO: datatype

    List<String> values;
    int distinct;
    ColumnPermutation perm;

    Candidate(List<String> values, String relationIdentifier, String columnIdentifier){
        this.values = values;
        this.distinct = values.size();
        perm = new ColumnPermutation(new ColumnIdentifier(relationIdentifier, columnIdentifier));
    }

    public int getDistinct(){return this.distinct;}

    public List<String> getValues(){return this.values;}

    @Override
    public int compareTo(Candidate other) {
        return this.distinct - other.getDistinct();
    }

    public ColumnPermutation getPerm(){return perm;}
}
