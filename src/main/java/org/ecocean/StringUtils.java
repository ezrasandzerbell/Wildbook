package org.ecocean;

import java.util.*;

public final class StringUtils {
  private StringUtils() {}

  /**
   * Parses a comma-separated string into a list of strings.
   * @param s string containing comma-separated items
   * @return list of strings
   */
  public static List<String> parseCommaSeparatedList(final String s) {
    if (s == null)
      return null;
    List<String> x = new ArrayList<>();
    for (StringTokenizer st = new StringTokenizer(s, ","); st.hasMoreTokens();)
      x.add(st.nextToken().trim());
    return x;
  }

  /**
   * Parses a comma-separated string into a set of strings.
   * @param s string containing comma-separated items
   * @return set of strings
   */
  public static Set<String> parseCommaSeparatedSet(final String s) {
    if (s == null)
      return null;
    Set<String> x = new HashSet<>();
    for (StringTokenizer st = new StringTokenizer(s, ","); st.hasMoreTokens();)
      x.add(st.nextToken().trim());
    return x;
  }

  /**
   * Collates the specified strings into a single string using the specified extra string data.
   * This method supports lookup of source strings in a ResourceBundle, to allow for convenient internationalization.
   * @param c collection of strings to collate
   * @param res ResourceBundle to use for string lookups
   * @param resKey format string to use as resource key for lookups (e.g. &quot;keyPrefix.%s&quot;)
   * @param prefix prefix for each string item
   * @param suffix suffix for each string item
   * @param delimiter delimiter between string items
   * @return string representing collated for of string collection
   */
  public static String collateStrings(final Collection<String> c, ResourceBundle res, String resKey, String prefix, String suffix, String delimiter) {
    Objects.requireNonNull(c);
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> it = c.iterator(); it.hasNext();) {
      if (prefix != null)
        sb.append(prefix);
      if (res != null && resKey != null && resKey.contains("%s"))
        sb.append(res.getString(String.format(resKey, it.next())));
      else
        sb.append(it.next());
      if (suffix != null)
        sb.append(suffix);
      if (delimiter != null && it.hasNext())
        sb.append(delimiter);
    }
    return sb.toString();
  }

  /**
   * Collates the specified strings into a single string using the specified extra string data.
   * This method supports lookup of source strings in a ResourceBundle, to allow for convenient internationalization.
   * @param c collection of strings to collate
   * @param res ResourceBundle to use for string lookups
   * @param resKey format string to use as resource key for lookups (e.g. &quot;keyPrefix.%s&quot;)
   * @param prefix prefix for each string item
   * @param suffix suffix for each string item
   * @param delimiter delimiter between string items
   * @return string representing collated for of string collection
   */
  public static String collateStrings(String[] c, ResourceBundle res, String resKey, String prefix, String suffix, String delimiter) {
    return collateStrings(Arrays.asList(c), res, resKey, prefix, suffix, delimiter);
  }

  /**
   * Collates the specified strings into a single string using the specified extra string data.
   * @param prefix prefix for each string item
   * @param suffix suffix for each string item
   * @param delimiter delimiter between string items
   * @return string representing collated for of string collection
   */
  public static String collateStrings(Collection<String> c, String prefix, String suffix, String delimiter) {
    return collateStrings(c, null, null, prefix, suffix, delimiter);
  }

  /**
   * Collates the specified strings into a single string using the specified extra string data.
   * @param prefix prefix for each string item
   * @param suffix suffix for each string item
   * @param delimiter delimiter between string items
   * @return string representing collated for of string collection
   */
  public static String collateStrings(String[] c, String prefix, String suffix, String delimiter) {
    return collateStrings(Arrays.asList(c), null, null, prefix, suffix, delimiter);
  }
}