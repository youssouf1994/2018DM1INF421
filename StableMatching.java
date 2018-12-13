
import java.util.*;

class StableMatching implements StableMatchingInterface {

  class PrefComparator implements Comparator<Integer> {
    int[] prefs;
    PrefComparator(int[] prefs) {
      int n = prefs.length;
      this.prefs = prefs;
    }

    @Override
    public int compare(Integer i, Integer j) {
      if (this.prefs[i] < this.prefs[j]) {
        return 1;
      }
      else {
        if (this.prefs[j] < this.prefs[i]) {
          return -1;
        }
        else {
          return 0;
        }
      }
    }
  }

  public int[][] constructStableMatching (
    int[] menGroupCount,
    int[] womenGroupCount,
    int[][] menPrefs,
    int[][] womenPrefs
  ) {
    // The number of men groups.
    int m = menGroupCount.length;

    // The number of women groups.
    int w = womenGroupCount.length;

    // Inverse the womenPrefs to have the order of each men group.
    int[][] invWomenPrefs = new int[w][m];
    for (int j = 0; j < w; j++) {
      for (int i = 0; i < m; i++) {
        invWomenPrefs[j][womenPrefs[j][i]] = i;
      }
    }


    int[] singleMenGroupCount = new int[m];
    int singleMen = 0;
    for (int i = 0; i < m; i++) {
      singleMenGroupCount[i] = menGroupCount[i];
      singleMen += singleMenGroupCount[i];
    }

    int[] singleWomenGroupCount = new int[w];
    for (int j = 0; j < w; j++) {
      singleWomenGroupCount[j] = womenGroupCount[j];
    }

    int[] lastPropWomenGroup = new int [m];
    for (int i = 0; i < m; i++) {
      lastPropWomenGroup[i] = 0;
    }

    ArrayList<PriorityQueue <Integer>> menGroupEngagedTo = new ArrayList<PriorityQueue <Integer>>(w);
    for (int j = 0; j < w; j++) {
      menGroupEngagedTo.add(new PriorityQueue<Integer> (m, new PrefComparator(invWomenPrefs[j])));
    }

    int[][] M = new int[m][w];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < w; j++) {
        M[i][j] = 0;
      }
    }
    // A boolean to check if a proposal conduct to an engagement or not
    boolean engagement = true;

    boolean done = true;

    int currentMenGroup = 0;

    while (singleMen > 0) {

      if (done) {
        // Search for the group with maximum single men.
        currentMenGroup = 0;
        for (int i = 0; i < m; i++) {
          if (singleMenGroupCount[i] > singleMenGroupCount[currentMenGroup]) {
            currentMenGroup = i;
          }
        }
      }

      done = false;
      int currentWomenGroup = menPrefs[currentMenGroup][lastPropWomenGroup[currentMenGroup]];

      if (singleWomenGroupCount[currentWomenGroup] > 0) {
        int a = singleWomenGroupCount[currentWomenGroup];
        int b = singleMenGroupCount[currentMenGroup];

        int c = (a > b) ? b : a;

        if (M[currentMenGroup][currentWomenGroup] == 0) {
          menGroupEngagedTo.get(currentWomenGroup).offer(currentMenGroup);
        }
        singleMen -= c;
        singleMenGroupCount[currentMenGroup] -= c;
        singleWomenGroupCount[currentWomenGroup] -= c;
        M[currentMenGroup][currentWomenGroup] += c;
      }
      else {
        Integer leastAttractiveMenGroup = menGroupEngagedTo.get(currentWomenGroup).peek();
        assert(leastAttractiveMenGroup != null);
        if (invWomenPrefs[currentWomenGroup][currentMenGroup] < invWomenPrefs[currentWomenGroup][leastAttractiveMenGroup]) {
          int a = M[leastAttractiveMenGroup][currentWomenGroup];
          int b = singleMenGroupCount[currentMenGroup];

          int c = (a > b) ? b : a;

          if (M[currentMenGroup][currentWomenGroup] == 0) {
            menGroupEngagedTo.get(currentWomenGroup).offer(currentMenGroup);
          }
          singleMenGroupCount[currentMenGroup] -= c;
          M[currentMenGroup][currentWomenGroup] += c;

          singleMenGroupCount[leastAttractiveMenGroup] += c;
          M[leastAttractiveMenGroup][currentWomenGroup] -= c;
          if (M[leastAttractiveMenGroup][currentWomenGroup] == 0) {
            menGroupEngagedTo.get(currentWomenGroup).remove();
          }
        }
        else {
          lastPropWomenGroup[currentMenGroup] += 1;
        }
      }
      if (singleMenGroupCount[currentMenGroup] == 0) {
        done = true;
      }
    }

    return M;
  }
}
