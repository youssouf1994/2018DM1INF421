
class StableMatching implements StableMatchingInterface {

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

    int[] lastPropWomenGroup = int [m];
    for (int i = 0; i < m; i++) {
      lastPropWomenGroup[i] = -1;
    }

    int[] leastAttractiveMenGroup = int[w];
    for (int j = 0; j < w; j++) {
      leastAttractiveMenGroup[j] = m;
    }

    int[][] M = new int[m][w];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < w; j++) {
        M[i][j] = 0;
      }
    }

    while (singleMen > 0) {
      int currentMenGroup = 0;

      // Search for the group with maximum single men.
      for (int i = 1; i < m; i++) {
        if (singleMenGroupCount[i] > singleMenGroupCount[currentMenGroup]) {
          currentMenGroup = i;
        }
      }

      int currentWomenGroup = menPrefs[currentMenGroup][lastPropWomenGroup[currentMenGroup]];

      if (singleWomenGroupCount[currentWomenGroup] > 0) {
        int a = singleWomenGroupCount[currentWomenGroup];
        int b = singleMenGroupCount[currentMenGroup];

        int c = (a > b) ? b : a;

        singleMen -= c;
        singleMenGroupCount[currentMenGroup] -= c;
        singleWomenGroupCount[currentWomenGroup] -= c;
        M[currentMenGroup][currentWomenGroup] += c;

        if ((leastAttractiveMenGroup[currentWomenGroup] == m) || (invWomenPrefs[currentWomenGroup][leastAttractiveMenGroup[currentWomenGroup]] < invWomenPrefs[currentWomenGroup][currentMenGroup])) {
          leastAttractiveMenGroup[currentMenGroup] = currentMenGroup;
        }
      }
      else if (invWomenPrefs[currentWomenGroup][currentMenGroup] < invWomenPrefs[currentWomenGroup][k]) {
        int k = leastAttractiveMenGroup[currentWomenGroup];
        int a = M[k][currentWomenGroup];
        int b = singleMenGroupCount[currentMenGroup];

        int c = (a > b) ? b : a;

        singleMenGroupCount[currentMenGroup] -= c;
        M[currentMenGroup][currentWomenGroup] += c;
        singleMenGroupCount[k] += c;
        M[k][currentWomenGroup] -= c;
      }
      else {
        lastPropWomenGroup[currentMenGroup] += 1;
      }
    }

    return M;
  }
}
