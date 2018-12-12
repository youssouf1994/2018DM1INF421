
class StableMatching implements StableMatchingInterface {

  public int[][] constructStableMatching (
    int[] menGroupCount,
    int[] womenGroupCount,
    int[][] menPrefs,
    int[][] womenPrefs
  ) {
    int m = menGroupCount.length;
    int w = womenGroupCount.length;

    int[] singleMenGroupCount = new int[m];
    int singleMen = 0;
    for (int i = 0; i < m; i++) {
      singleMenGroupCount[i] = menGroupCount[i];
      singleMen += singleMenGroupCount[i];
    }

    int[] lastPropWomenGroup = int [m];
    for (int i = 0; i < m; i++) {
      lastPropWomenGroup[i] = -1;
    }

    int[] leastAttractiveMenGroup = int[w];
    for (int j = 0; j < w; j++) {
      leastAttractiveMenGroup[j] = -1;
    }

    int[][] M = new int[m][w];

    while (singleMen > 0) {
      int currentMenGroup = 0;

      // Search for the group with maximum single men.
      for (int i = 1; i < m; i++) {
        if (singleMenGroupCount[i] > singleMenGroupCount[currentMenGroup]) {
          currentMenGroup = i;
        }
      }

    }

    return M;
  }
}
