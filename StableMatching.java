
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

    // The number of single men in every group.
    int[] singleMenGroupCount = new int[m];

    // The number of all single men.
    int singleMen = 0;
    for (int i = 0; i < m; i++) {
      singleMenGroupCount[i] = menGroupCount[i];
      singleMen += singleMenGroupCount[i];
    }

    // The number of single women in every group.
    int[] singleWomenGroupCount = new int[w];
    for (int j = 0; j < w; j++) {
      singleWomenGroupCount[j] = womenGroupCount[j];
    }

    /*
      For every group of men we store the most attractive women group
      that the group of men does not propose to.
      */
    int[] mostUnproposedWomenGroup = new int [m];
    for (int i = 0; i < m; i++) {
      mostUnproposedWomenGroup[i] = 0;
    }
    /*
      For every group of women we store all groups of men engaded to the women group.
      We store every group in PriorityQueue to have a direct access of the least att-
      ractive men for this group.
      */
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

    LinkedList <Integer> singleMenGroupWithBigNumber = new LinkedList <Integer>();

    while (singleMen > 0) {

      if (singleMenGroupWithBigNumber.isEmpty()) {
        for (int i = 0; i < m; i++) {
          if (singleMenGroupCount[i] > (singleMen /(2*m))) {
            singleMenGroupWithBigNumber.add(i);
          }
        }
      }

      while (!singleMenGroupWithBigNumber.isEmpty()) {

        int currentMenGroup = singleMenGroupWithBigNumber.poll();
        int currentWomenGroup = menPrefs[currentMenGroup][mostUnproposedWomenGroup[currentMenGroup]];

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
            mostUnproposedWomenGroup[currentMenGroup] += 1;
          }
        }
        if (singleMenGroupCount[currentMenGroup] > (singleMen /(2*m))) {
          singleMenGroupWithBigNumber.add(currentMenGroup);
        }
      }
    }

    return M;
  }
}
