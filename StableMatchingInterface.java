public interface StableMatchingInterface {

  // A problem instance consists of the following data, describing groups of men and women.
  
  // menGroupCount:
  // The groups of men are labeled from 0 to m-1, where "m" is shorthand for menGroupCount.length
  // The number of men in the i-th group is given as menGroupCount[i], for 0 <= i < m.
  
  // womenGroupCount:
  // The groups of women are labeled 0 to w-1, where "w" is shorthand for womenGroupCount.length
  // The number of women in the j-th group is given as womenGroupCount[j], for 0 <= j < w.
  
  // Note: The sum of all entries of the array menGroupCount and that of the array womenGroupCount
  // is guaranteed to be the same.

  // menPrefs:
  // Each of the groups of men ranks the groups of women in decreasing order of preference.
  // Thus, for the i-th group of men,  menPrefs[i] is an array of w elements,
  // whose first element menPrefs[i][0] is i's most desirable group of women, and
  // whose last element menPrefs[i][w-1] is i's least desirable group of women.

  // womenPrefs: 
  // Likewise, each of the groups of women ranks the groups of men in decreasing order of preference.
  // Thus, for the j-th group of women,  womenPrefs[j] is an array of m elements,
  // whose first element womenPrefs[j][0] is j's most desirable group of men, and
  // whose last element womenPrefs[j][m-1] is j's least desirable group of men.

  public int[][] constructStableMatching (
    int[] menGroupCount,
    int[] womenGroupCount,
    int[][] menPrefs,
    int[][] womenPrefs
  );

  // The method constructStableMatching must return an int[m][w] array
  // "mar".  The element mar[i][j] represents the number of couples in the matching 
  // with the groom belonging to the i-th group of men and the bride belinging to
  // the j-th group of women. This array must describe a stable matching.
}

