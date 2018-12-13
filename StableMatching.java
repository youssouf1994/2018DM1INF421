
import java.util.*;

class StableMatching implements StableMatchingInterface {

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
      We store every group of men engaged to the women group in PriorityQueue to
      have a direct access of the least attractive men for this group.
      */
    ArrayList<PriorityQueue <Integer>> menGroupEngagedTo = new ArrayList<PriorityQueue <Integer>>(w);
    for (int j = 0; j < w; j++) {
      /*
        The comparator of the PriorityQueue is compatible with preferences of the
        current women group.
        */
      final int jfinal = j;
      menGroupEngagedTo.add(new PriorityQueue<Integer> (m, new Comparator<Integer>() {
        @Override
        public int compare (Integer i, Integer ip) {
          return invWomenPrefs[jfinal][ip] - invWomenPrefs[jfinal][i];
        }
      }));
    }

    // The matching matrix.
    int[][] M = new int[m][w];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < w; j++) {
        M[i][j] = 0;
      }
    }

    /*
      We create a Stack to store the groups of men with number of single larger
      than the total number of single times 1/2m.
      */
    Stack <Integer> singleMenGroupWithBigNumber = new Stack <Integer>();

    while (singleMen > 0) {

      // Adding all the the groups of men with number of single larger
      // than the total number of single times 1/2m.
      if (singleMenGroupWithBigNumber.isEmpty()) {
        for (int i = 0; i < m; i++) {
          if (singleMenGroupCount[i] > (singleMen /(2*m))) {
            singleMenGroupWithBigNumber.push(i);
          }
        }
      }

      while (!singleMenGroupWithBigNumber.isEmpty()) {
        // Get the group of men.
        int currentMenGroup = singleMenGroupWithBigNumber.pop();

        // Get the group of women currentMenGroup want to propose.
        int currentWomenGroup = menPrefs[currentMenGroup][mostUnproposedWomenGroup[currentMenGroup]];

        // Check if there is single woman if the currentWomenGroup.
        if (singleWomenGroupCount[currentWomenGroup] > 0) {
          // Match the group of men with the group of women.
          int a = singleWomenGroupCount[currentWomenGroup];
          int b = singleMenGroupCount[currentMenGroup];

          int c = (a > b) ? b : a;
          // If all men in the group of men were not engaged to any women in the women
          // group add the group of men to the men engaged to the group women.
          if (M[currentMenGroup][currentWomenGroup] == 0) {
            menGroupEngagedTo.get(currentWomenGroup).offer(currentMenGroup);
          }
          singleMen -= c;
          singleMenGroupCount[currentMenGroup] -= c;
          singleWomenGroupCount[currentWomenGroup] -= c;
          M[currentMenGroup][currentWomenGroup] += c;
        }
        else {
          // All women in currentWomenGroup are engaged we look for the least attractive
          // man engaged to this group.
          Integer leastAttractiveMenGroup = menGroupEngagedTo.get(currentWomenGroup).peek();
          // Check if the least attractive man engaged to a women in the current women group
          // is less attractive to this group than the current men group.
          if (invWomenPrefs[currentWomenGroup][currentMenGroup] < invWomenPrefs[currentWomenGroup][leastAttractiveMenGroup]) {
            // Match the current men group with the number of women in the current women group
            // engaged to the least attractive man for this women group.
            int a = M[leastAttractiveMenGroup][currentWomenGroup];
            int b = singleMenGroupCount[currentMenGroup];

            int c = (a > b) ? b : a;

            // If all men in the group of men were not engaged to any women in the women
            // group add the group of men to the men engaged to the group women.
            if (M[currentMenGroup][currentWomenGroup] == 0) {
              menGroupEngagedTo.get(currentWomenGroup).offer(currentMenGroup);
            }
            singleMenGroupCount[currentMenGroup] -= c;
            M[currentMenGroup][currentWomenGroup] += c;

            singleMenGroupCount[leastAttractiveMenGroup] += c;
            M[leastAttractiveMenGroup][currentWomenGroup] -= c;

            // If all men in the least attractive group that were engaged to the women
            // then remove the least attractive group of men from the groups engaged to
            // group women
            if (M[leastAttractiveMenGroup][currentWomenGroup] == 0) {
              menGroupEngagedTo.get(currentWomenGroup).remove();
            }
          }
          else {
            // The proposal was not accepted. We go to the next women group.
            mostUnproposedWomenGroup[currentMenGroup] += 1;
          }
        }
        if (singleMenGroupCount[currentMenGroup] > (singleMen /(2*m))) {
          singleMenGroupWithBigNumber.push(currentMenGroup);
        }
      }
    }

    return M;
  }
}
