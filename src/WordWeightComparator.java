import java.util.*;
//comparator for wordweights for use with priority queue
class WordWeightComparator implements Comparator<WordWeight> {
    public int compare(WordWeight a, WordWeight b) {
        if(a.weight > b.weight) {
            return -1;
        }else if(a.weight < b.weight) {
            return 1;
        }else {
            return 0;
        }
    }

    public boolean equals(Object a, Object b) {
        WordWeight A = (WordWeight)a;
        WordWeight B = (WordWeight)b;
        if(A.word.equalsIgnoreCase(B.word)) {
            if(A.weight == B.weight) {
                return true;
            }
        }
        return false;
    }
}
