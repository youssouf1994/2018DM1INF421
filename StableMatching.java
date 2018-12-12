
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

    int[][] M = new int[m][w];

    while (singleMen > 0) {
      int currentMenGroup = 0;
      for (int i = 0; i < m; i++) {
        if(singleMenGroupCount[i] > singleMenGroupCount[currentMenGroup]) {
          currentMenGroup = i;
        }
      }
    }

    return M;
  }
}
