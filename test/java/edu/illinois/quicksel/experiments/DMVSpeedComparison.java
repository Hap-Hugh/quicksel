package edu.illinois.quicksel.experiments;

import edu.illinois.quicksel.basic.AssertionReader;
import edu.illinois.quicksel.Assertion;
import edu.illinois.quicksel.Hyperrectangle;
import edu.illinois.quicksel.quicksel.QuickSel;
import edu.illinois.quicksel.isomer.Isomer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;

public class DMVSpeedComparison {

  public static void main(String[] args) throws IOException {
    Pair<Vector<Assertion>, Vector<Assertion>> assertionPair = AssertionReader.readAssertion("power-2d-10001.txt", "permanent_assertion_2d.txt");
    Vector<Assertion> assertions = assertionPair.getLeft();
    Vector<Assertion> permanent_assertions = assertionPair.getRight();

    Vector<Assertion> queryAssertion = new Vector<>(assertions.subList(2000, 2100));

    System.out.println("dataset and query set generations done.\n");


    System.out.println("QuickSel test");
    quickSelTest(permanent_assertions, assertions, queryAssertion);
    System.out.println("");

    System.out.println("Isomer test");
    isomerTest(permanent_assertions, assertions, queryAssertion);
    System.out.println("");
// data_sensitive/forest-data-2100-2d
  }

  private static void quickSelTest(
      Vector<Assertion> permanent_assertions,
      Vector<Assertion> assertions,
      List<Assertion> queryset) {

    // build Crumbs
    List<Integer> list = Arrays.asList(30,
        50,
        200,
        500,
        1000,
        2000);
    List<Integer> pointers = Arrays.asList(50,
        95,
        99,
        100);
    for (int assertionNum : list) {
      Pair<Hyperrectangle, Double> range_freq = computeMinMaxRange();
      QuickSel quickSel = new QuickSel(range_freq.getLeft(), range_freq.getRight());

      for (Assertion a : permanent_assertions) {
        quickSel.addPermanentAssertion(a);
      }

      long time1 = System.nanoTime();
      for (Assertion a : assertions.subList(0, assertionNum)) {
        quickSel.addAssertion(a);
      }
      quickSel.prepareOptimization();
      long time2 = System.nanoTime();

      boolean debug_output = false;
      quickSel.assignOptimalWeights(debug_output);
      long time3 = System.nanoTime();


      for (int i = 0; i < quickSel.weights.size(); i++) {
          if (quickSel.weights.get(i) < -1.5 || quickSel.weights.get(i) > 1.5) {
              System.out.println(i + " " + quickSel.weights.get(i));
          }
      }

      for (Assertion q : queryset) {
        quickSel.answer(q.query);
      }
      long time4 = System.nanoTime();

      //write time
      System.out.println(String.format("Train time: %.3f, Estimation time: %.3f", (time3 - time1) / 1e9, (time4 - time3) / 1e9));

      //write sel
      double squared_err_sum = 0.0;
      ArrayList<Double> q_list = new ArrayList<Double>();
      for (Assertion q : queryset) {
        Double sel = Math.max(0, quickSel.answer(q.query));
        squared_err_sum += Math.pow(sel - q.freq, 2);
        double q_error = 100000.0;
        if (sel == 0 || q.freq == 0){
          q_error = 1.0;
        } else {
          q_error = Math.max(sel, q.freq) / Math.min(sel, q.freq);
        }
        q_list.add(q_error);
      }
      Collections.sort(q_list);
      for (int pointer : pointers){
        System.out.printf(" %.3f ", q_list.get(pointer - 1));
      }
      double rms_err = Math.sqrt(squared_err_sum / queryset.size());

      System.out.println(String.format("Learning %d assertions, RMS error: %.5f\n", assertionNum, rms_err));
    }
  }

  private static void isomerTest(
      Vector<Assertion> permanent_assertions,
      Vector<Assertion> assertions,
      List<Assertion> queryset) {
    List<Integer> list = Arrays.asList(50,
        100,
        200);
    List<Integer> pointers = Arrays.asList(50,
        95,
        99,
        100);
    for (int assertionNum : list) {
      Pair<Hyperrectangle, Double> range_freq = computeMinMaxRange();
      Isomer isomer = new Isomer(range_freq.getLeft(), range_freq.getRight());

      long time1 = System.nanoTime();
      for (Assertion a : assertions.subList(0, assertionNum)) {
        isomer.addAssertion(a);
      }
      long time2 = System.nanoTime();

      boolean debug_output = false;
      isomer.assignOptimalWeights(debug_output);
      long time3 = System.nanoTime();

      for (Assertion q : queryset) {
        isomer.answer(q.query);
      }
      long time4 = System.nanoTime();

      //write time
      System.out.println(String.format("Insertion time: %.3f, Estimation time: %.3f", (time3 - time1) / 1e9, (time4 - time3) / 1e9));

      //write sel
      double squared_err_sum = 0.0;
      ArrayList<Double> q_list = new ArrayList<Double>();
      for (Assertion q : queryset) {
        Double sel = Math.max(0, isomer.answer(q.query  ));
        squared_err_sum += Math.pow(sel - q.freq, 2);
        double q_error = 100000.0;
        if (sel == 0 || q.freq == 0){
          q_error = 1.0;
        } else {
          q_error = Math.max(sel, q.freq) / Math.min(sel, q.freq);
        }
        q_list.add(q_error);
      }
      Collections.sort(q_list);
      for (int pointer : pointers){
        System.out.printf(" %.3f ", q_list.get(pointer - 1));
      }
      double rms_err = Math.sqrt(squared_err_sum / queryset.size());
      System.out.println(String.format("Learning %d assertions, RMS error: %.5f\n", assertionNum, rms_err));
    }
  }


  private static Pair<Hyperrectangle, Double> computeMinMaxRange() {
    Vector<Pair<Double, Double>> min_max = new Vector<Pair<Double, Double>>();
    min_max.add(Pair.of(0.0, 1.0));
    min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
//     min_max.add(Pair.of(0.0, 1.0));
    Hyperrectangle min_max_rec = new Hyperrectangle(min_max);
    double total_freq = 1.0;
    return Pair.of(min_max_rec, total_freq);
  }

}
