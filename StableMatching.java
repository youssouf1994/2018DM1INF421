
import java.util.*;

class StableMatching implements StableMatchingInterface {

  class PrefComparator implements Comparator<Integer> {
    int[] prefs;
    PrefComparator(int[] prefs) {
      int n = prefs.length;
      this.prefs = new int[n];

      for (int i = 0; i < n; i++) {
        this.prefs[i] = prefs[i];
      }
    }

    @Override
    public int compare(Integer i, Integer j) {
      assert (i < this.prefs.length && j < this.prefs.length);
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
    int m = menGroupCount.length;
    int w = womenGroupCount.length;

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
    for (int j = 0; j < m; j++) {
      singleWomenGroupCount[j] = womenGroupCount[j];
    }

    int[] lastPropWomenGroup = new int [m];
    for (int i = 0; i < m; i++) {
      lastPropWomenGroup[i] = -1;
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

    boolean engagement = true;
    int currentMenGroup = 0;

    while (singleMen > 0) {

      // Search for the group with maximum single men.
      if (engagement) {
        for (int i = 1; i < m; i++) {
          if (singleMenGroupCount[i] > singleMenGroupCount[currentMenGroup]) {
            currentMenGroup = i;
          }
        }
      }

      engagement = true;
      int currentWomenGroup = menPrefs[currentMenGroup][lastPropWomenGroup[currentMenGroup]];

      if (singleWomenGroupCount[currentWomenGroup] > 0) {
        int a = singleWomenGroupCount[currentWomenGroup];
        int b = singleMenGroupCount[currentMenGroup];

        int c = (a > b) ? b : a;

        singleMen -= c;
        singleMenGroupCount[currentMenGroup] -= c;
        singleWomenGroupCount[currentWomenGroup] -= c;
        M[currentMenGroup][currentWomenGroup] += c;
        if (!(menGroupEngagedTo.get(currentWomenGroup).contains(currentMenGroup))) {
          menGroupEngagedTo.get(currentWomenGroup).add(currentMenGroup);
        }
      }
      else {
        int leastAttractiveMenGroup = menGroupEngagedTo.get(currentWomenGroup).remove();
        if (invWomenPrefs[currentWomenGroup][currentMenGroup] < invWomenPrefs[currentWomenGroup][leastAttractiveMenGroup]) {
          int a = M[leastAttractiveMenGroup][currentWomenGroup];
          int b = singleMenGroupCount[currentMenGroup];

          int c = (a > b) ? b : a;

          singleMenGroupCount[currentMenGroup] -= c;
          M[currentMenGroup][currentWomenGroup] += c;
          if (!(menGroupEngagedTo.get(currentWomenGroup).contains(currentMenGroup))) {
            menGroupEngagedTo.get(currentWomenGroup).add(currentMenGroup);
          }

          singleMenGroupCount[leastAttractiveMenGroup] += c;
          M[leastAttractiveMenGroup][currentWomenGroup] -= c;
          if (c < a) {
            menGroupEngagedTo.get(currentWomenGroup).add(leastAttractiveMenGroup);
          }
        }
        else {
          lastPropWomenGroup[currentMenGroup] += 1;
          engagement = false;
        }
      }
    }

    return M;
  }
}
