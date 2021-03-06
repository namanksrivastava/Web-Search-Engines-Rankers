package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Collections;

/**
 * Evaluator for HW1.
 *
 * @author fdiaz
 * @author congyu
 */
class Evaluator {
  public static class DocumentRelevances {
    private Map<Integer, Double> relevances = new HashMap<Integer, Double>();
    private Map<Integer, Double> scoredRelevances = new HashMap<Integer, Double>();

    public DocumentRelevances() { }

    public void addDocument(int docid, String grade) {
      relevances.put(docid, convertToBinaryRelevance(grade));
      scoredRelevances.put(docid, convertToScoredRelevance(grade));
    }

    public boolean hasRelevanceForDoc(int docid) {
      return relevances.containsKey(docid);
    }

    public int getTotalRelevantCount() {
      int count = 0;
      for (double score : relevances.values()) {
        if (score == 1.0) {
          count++;
        }
      }
      return count;
    }

    public double getRelevanceForDoc(int docid) {
      return relevances.get(docid);
    }

    public double getScoredRelevanceForDoc(int docid) {
      return scoredRelevances.get(docid);
    }

    private static double convertToBinaryRelevance(String grade) {

      if (grade.equalsIgnoreCase("Perfect") ||
          grade.equalsIgnoreCase("Excellent") ||
          grade.equalsIgnoreCase("Good")) {
        return 1.0;
      }
      return 0.0;
    }

    private static double convertToScoredRelevance(String grade) {
      Map<String, Double> scoredRelevances = new HashMap<String, Double>();
      if(grade.length()>0) {
        scoredRelevances.put(("Perfect").toLowerCase(), 10.0);
        scoredRelevances.put(("Excellent").toLowerCase(), 7.0);
        scoredRelevances.put(("Good").toLowerCase(), 5.0);
        scoredRelevances.put(("Fair").toLowerCase(), 1.0);
        scoredRelevances.put(("Bad").toLowerCase(), 0.0);
        return scoredRelevances.get(grade.toLowerCase());
      } else {
        return 0.0;
      }
    }
  }

  /**
   * Usage: java -cp src edu.nyu.cs.cs2580.Evaluator [labels] [metric_id]
   */
  public static void main(String[] args) throws IOException {
    Map<String, DocumentRelevances> judgments =
        new HashMap<String, DocumentRelevances>();
    SearchEngine.Check(args.length == 2, "Must provide labels and metric_id!");
    readRelevanceJudgments(args[0], judgments);
    evaluateStdin(Integer.parseInt(args[1]), judgments);
  }

  public static void readRelevanceJudgments(
      String judgeFile, Map<String, DocumentRelevances> judgements)
      throws IOException {
    String line = null;
    BufferedReader reader = new BufferedReader(new FileReader(judgeFile));
    while ((line = reader.readLine()) != null) {
      // Line format: query \t docid \t grade
      Scanner s = new Scanner(line).useDelimiter("\t");
      String query = s.next();
      DocumentRelevances relevances = judgements.get(query);
      if (relevances == null) {
        relevances = new DocumentRelevances();
        judgements.put(query, relevances);
      }
      relevances.addDocument(Integer.parseInt(s.next()), s.next());
      s.close();
    }
    reader.close();
  }

  // @CS2580: implement various metrics inside this function
  public static void evaluateStdin(
      int metric, Map<String, DocumentRelevances> judgments)
          throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in));
    List<Integer> results = new ArrayList<Integer>();
    String line = null;
    String currentQuery = "";
    while ((line = reader.readLine()) != null) {
      Scanner s = new Scanner(line).useDelimiter("\t");
      final String query = s.next();
      if (!query.equals(currentQuery)) {
        if (results.size() > 0) {
          evaluateQuery(metric, judgments, results, currentQuery);
          results.clear();
        }
        currentQuery = query;
      }
      results.add(Integer.parseInt(s.next()));
      s.close();
    }
    reader.close();
    if (results.size() > 0) {
      evaluateQuery(metric, judgments, results, currentQuery);
    }
  }

  private static void evaluateQuery(int metric, Map<String, DocumentRelevances> judgments,
                                    List<Integer> results, String currentQuery) {
    switch (metric) {
    case -1:
      evaluateQueryInstructor(currentQuery, results, judgments);
      break;
    case 0: evaluateQueryMetric0(currentQuery, results, judgments);
      break;
    case 1: evaluateQueryMetric1(currentQuery, results, judgments);
      break;
    case 2: evaluateQueryMetric2(currentQuery, results, judgments);
      break;
    case 3: evaluateQueryMetric3(currentQuery, results, judgments);
      break;
    case 4: evaluateQueryMetric4(currentQuery, results, judgments);
      break;
    case 5: evaluateQueryMetric5(currentQuery, results, judgments);
      break;
    case 6: evaluateQueryMetric6(currentQuery, results, judgments);
      break;
    default:
      // @CS2580: add your own metric evaluations above, using function
      // names like evaluateQueryMetric0.
      System.err.println("Requested metric not implemented!");
    }
  }

  public static void evaluateQueryInstructor(
      String query, List<Integer> docids,
      Map<String, DocumentRelevances> judgments) {
    double R = 0.0;
    double N = 0.0;
    for (int docid : docids) {
      DocumentRelevances relevances = judgments.get(query);
      if (relevances == null) {
        System.out.println("Query [" + query + "] not found!");
      } else {
        if (relevances.hasRelevanceForDoc(docid)) {
          R += relevances.getRelevanceForDoc(docid);
        }
        ++N;
      }
    }
    System.out.println(query + "\t" + Double.toString(R / N));
  }

//  Precision at 1,5 and 10
  public static void evaluateQueryMetric0(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {

    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getRelevantDocCumulative(relevances, docids);

    System.out.println(query + "\t" + Double.toString(getPrecisionAtIndex(1, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getPrecisionAtIndex(5, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getPrecisionAtIndex(10, relevantDocCumulative)));
  }

//  Recall at 1, 5 and 10
  public static void evaluateQueryMetric1(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getRelevantDocCumulative(relevances, docids);
    int totalRelevantCount = relevances.getTotalRelevantCount();

    System.out.println(query + "\t" + Double.toString(getRecallAtIndex(1, totalRelevantCount, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getRecallAtIndex(5, totalRelevantCount, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getRecallAtIndex(10, totalRelevantCount, relevantDocCumulative)));
  }

//  F measure at 1, 5 and 10
  public static void evaluateQueryMetric2(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getRelevantDocCumulative(relevances, docids);
    int totalRelevantCount = relevances.getTotalRelevantCount();

    double beta = 0.5;
    System.out.println(query + "\t" + Double.toString(getFMeasureAtIndex(1, beta, totalRelevantCount, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getFMeasureAtIndex(5, beta, totalRelevantCount, relevantDocCumulative)));
    System.out.println(query + "\t" + Double.toString(getFMeasureAtIndex(10, beta, totalRelevantCount, relevantDocCumulative)));
  }

//  Precision at recall values of 0.0, 0.1, ..., 1.0
  public static void evaluateQueryMetric3(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getTotalRelevantDocCumulative(relevances, docids);
    List<Double> allRecall = getAllRecall (relevances, relevantDocCumulative);
    List<Double> allMaxPrecision = getAllMaxPrecision (relevantDocCumulative);
    try {
      Double[] Precisions = getPrecisionForRecallRange(allRecall, allMaxPrecision);
      for(int i=0; i<=10;i++){
        System.out.println(query + "\t" + Precisions[i]);
      }
    } catch (IndexOutOfBoundsException e) {
      System.err.println("IndexOutOfBoundsException: " + e.getMessage());
      System.err.println("Not all relevant cases included, please add all docs to result. To do this, in your query add :&num=all.");
      System.err.println("Query Format :\n http://<host>:<port>/search?query=<query>&ranker=<ranker>&format=<format>&num=<count>");
    }
  }

//  Returns the recall at every retrieved document position
  public static List<Double> getAllRecall(DocumentRelevances relevances, List<Double> relevantDocCumulative){
    List<Double> allRecall = new ArrayList<>();
    int totalRelevantCount = relevances.getTotalRelevantCount();
    for(int i=relevantDocCumulative.size(); i>0; i--){
      allRecall.add(getRecallAtIndex(i,totalRelevantCount,relevantDocCumulative));
    }
    return allRecall;
  }

//  Returns a monotonously increasing array of precision values for the retrieved documents
  public static List<Double> getAllMaxPrecision(List<Double> relevantDocCumulative){
    List<Double> allMaxPrecision = new ArrayList<>();
    double maxPrecision = 0.0;
    double precision;
    for(int i=relevantDocCumulative.size(); i>0; i--){
      precision = getPrecisionAtIndex(i,relevantDocCumulative);
      if(precision > maxPrecision){
        maxPrecision = precision;
      }
      allMaxPrecision.add(maxPrecision);
    }
    return allMaxPrecision;
  }

//  Sets the precision at each standard recall value to the max precision observed for a higher recall level
  public static Double[] getPrecisionForRecallRange(List<Double> allRecall, List<Double> allPrecision){
    double previousPrecision = 1.0;
    Double[] Precision = new Double[11];
    for(int i=0; i<11;i++){
      Precision[i] = -1.0;
    }
    for (int i = 0; i < allRecall.size(); i++) {
        if (allRecall.get(i) == 1.0) {
          Precision[10] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.9) {
          Precision[9] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.8) {
          Precision[8] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.7) {
          Precision[7] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.6) {
          Precision[6] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.5) {
          Precision[5] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.4) {
          Precision[4] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.3) {
          Precision[3] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.2) {
          Precision[2] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.1) {
          Precision[1] = allPrecision.get(i);
        } else if (allRecall.get(i) == 0.0) {
          Precision[0] = allPrecision.get(i);
        } else if (allRecall.get(i) < 0.9 && allRecall.get(i) > 0.8 && Precision[9] == -1) {
          Precision[9] = previousPrecision;
        } else if (allRecall.get(i) < 0.8 && allRecall.get(i) > 0.7 && Precision[8] == -1) {
          Precision[8] = previousPrecision;
        } else if (allRecall.get(i) < 0.7 && allRecall.get(i) > 0.6 && Precision[7] == -1) {
          Precision[7] = previousPrecision;
        } else if (allRecall.get(i) < 0.6 && allRecall.get(i) > 0.5 && Precision[6] == -1) {
          Precision[6] = previousPrecision;
        } else if (allRecall.get(i) < 0.5 && allRecall.get(i) > 0.4 && Precision[5] == -1) {
          Precision[6] = previousPrecision;
        } else if (allRecall.get(i) < 0.4 && allRecall.get(i) > 0.3 && Precision[4] == -1) {
          Precision[4] = previousPrecision;
        } else if (allRecall.get(i) < 0.3 && allRecall.get(i) > 0.2 && Precision[3] == -1) {
          Precision[3] = previousPrecision;
        } else if (allRecall.get(i) < 0.2 && allRecall.get(i) > 0.1 && Precision[2] == -1) {
          Precision[2] = previousPrecision;
        } else if (allRecall.get(i) < 0.1 && allRecall.get(i) > 0.0 && Precision[1] == -1) {
          Precision[1] = previousPrecision;
        }
        previousPrecision = allPrecision.get(i);
      }
      if (Precision[0] == -1) {
        Precision[0] = previousPrecision;
      }
      if (Precision[10] == -1) {
        Precision[10] = 0.0;
      }
      for (int i = 9; i >= 0; i--) {
        if (Precision[i] == -1.0) {
          Precision[i] = Precision[i + 1];
        }
      }
      return Precision;

  }

//  Computes the average precision for precision points at each relevant document retrieved
  public static void evaluateQueryMetric4(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getRelevantDocCumulative(relevances, docids);

    double precisionSum = 0;
    double currentCount = 0;
    for (int i = 0; i < relevantDocCumulative.size(); i++) {

      if (relevantDocCumulative.get(i) > currentCount) {
        precisionSum += getPrecisionAtIndex(i+1, relevantDocCumulative);
        currentCount = relevantDocCumulative.get(i);
      }

    }

    double avgPrecision;
    Double numOfRelevantRetrieved = relevantDocCumulative.get(relevantDocCumulative.size() - 1);
    if (numOfRelevantRetrieved == 0) {
      avgPrecision = 0;
    }
    else {
      avgPrecision = precisionSum / numOfRelevantRetrieved;
    }
    System.out.println(query + "\t" + avgPrecision);
  }

//  Computes NDCG value at 1, 5 and 10 retrieved documents
  public static void evaluateQueryMetric5(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances scoredRelevances = judgments.get(query);
    if (scoredRelevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> resultRelevance = getRelevanceAtEachResult(scoredRelevances, docids);
    List<Double> DCG = getDCGForAllResults(resultRelevance);
    Collections.sort(resultRelevance, Collections.reverseOrder());
    List<Double> idealDCG = getDCGForAllResults(resultRelevance);

    System.out.println(query + "\t" + Double.toString(getNDCGAtIndex(1, DCG, idealDCG)));
    System.out.println(query + "\t" + Double.toString(getNDCGAtIndex(5, DCG, idealDCG)));
    System.out.println(query + "\t" + Double.toString(getNDCGAtIndex(10, DCG, idealDCG)));
  }

//  Computes the Reciprocal Rank
  public static void evaluateQueryMetric6(
          String query, List<Integer> docids,
          Map<String, DocumentRelevances> judgments) {
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null) {
      System.out.println("Query [" + query + "] not found!");
      return;
    }

    List<Double> relevantDocCumulative = getRelevantDocCumulative(relevances, docids);

    int i;
    for (i = 0; i < relevantDocCumulative.size(); i++) {
      if (relevantDocCumulative.get(i) == 1) {
        break;
      }
    }

    double rr;
    if (i == relevantDocCumulative.size()) {
      rr = 0;
    }
    else rr = (1.0 / (i+1));

    System.out.println(query + "\t" + rr);
  }

  public static double getFMeasureAtIndex(int index, double beta, int totalRelevantCount, List<Double> relevantDocCumulative) {

    double precision = getPrecisionAtIndex(index, relevantDocCumulative);
    double recall = getRecallAtIndex(index, totalRelevantCount, relevantDocCumulative);

    if (precision == 0 && recall == 0) return 0;
    return (1 + Math.pow(beta, 2))*(precision*recall)/((Math.pow(beta, 2) * precision) + recall);
  }


//  Gets the precision at an index by dividing the cumulative relevant count till that index by the index
  public static double getPrecisionAtIndex(int index, List<Double> relevantDocCumulative) {

    return relevantDocCumulative.get(index-1) / index;

  }

//  Gets the recall at an index by dividing the cumulative relevant count till that index
//  by the total relevant docs in the collection
  public static double getRecallAtIndex(int index, int totalRelevantCount, List<Double> relevantDocCumulative) {
    if (totalRelevantCount == 0) return 0;
    return relevantDocCumulative.get(index-1) / totalRelevantCount;
  }

  public static double getNDCGAtIndex(int index, List<Double> DCG, List<Double> IdealDCG){
    double DCGval = 0.0;
    double IdealDCGval =  1.0;
    if(DCG.size() > index-1 && IdealDCG.size() > index-1 && IdealDCG.get(index-1) != 0){
      DCGval = DCG.get(index-1);
      IdealDCGval = IdealDCG.get(index-1);
    } else if(DCG.size()>0 && IdealDCG.size() > 0 && IdealDCG.get(DCG.size()-1) != 0){
      DCGval = DCG.get(DCG.size()-1);
      IdealDCGval = IdealDCG.get(DCG.size()-1);
    }
    return DCGval/IdealDCGval;
  }

//  Gets the cumulative count of relevant documents at each index of the retrieved docs
  public static List<Double> getRelevantDocCumulative(DocumentRelevances relevances, List<Integer> docids) {

    List<Double> cumulativeCount = new ArrayList<>();
    double count = 0;
    for (int i = 0; i < docids.size(); i++) {
      int docid = docids.get(i);
      if (relevances.hasRelevanceForDoc(docid) && relevances.getRelevanceForDoc(docid) == 1.0) {
        count++;
      }
      cumulativeCount.add(count);
    }
    return cumulativeCount;
  }

  public static List<Double> getTotalRelevantDocCumulative(DocumentRelevances relevances, List<Integer> docids) {

    List<Double> cumulativeCount = new ArrayList<>();
    double count = 0;
    double totalCount = relevances.getTotalRelevantCount();
    for (int i = 0; i < docids.size(); i++) {
      int docid = docids.get(i);
      if (relevances.hasRelevanceForDoc(docid) && relevances.getRelevanceForDoc(docid) == 1.0) {
        count++;
      }
      cumulativeCount.add(count);
      if(count == totalCount){
        break;
      }
    }
    return cumulativeCount;
  }

  public static List<Double> getRelevanceAtEachResult(DocumentRelevances scoredRelevances, List<Integer> docids) {
    List<Double> resultRelevanceList = new ArrayList<>();
    for( int docid : docids) {
      if(scoredRelevances.hasRelevanceForDoc(docid)){
        resultRelevanceList.add(scoredRelevances.getScoredRelevanceForDoc(docid));
      } else {
        resultRelevanceList.add(0.0);
      }
    }
    return resultRelevanceList;
  }

  public static List<Double> getDCGForAllResults(List<Double> resultRelevance){
    List<Double> DCGVal = new ArrayList<>();
    double sum = 0.0;
    int count = 1;
    for(double rel: resultRelevance){
      if(count > 1){
        sum += (rel/((Math.log(count)/Math.log(2))));
      } else {
        sum += rel;
      }
      count++;
      DCGVal.add(sum);
    }
    return DCGVal;
  }
}
