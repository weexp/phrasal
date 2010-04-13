package edu.stanford.nlp.mt.discrimreorder;

import java.io.*;
import edu.stanford.nlp.mt.train.AbstractWordAlignment;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.*;
import edu.stanford.nlp.trees.semgraph.*;

/**
 * This class is to represent the alignment in a 2d matrix form
 * (Note that there's similar class in the edu.stanford.nlp.mt.train package or edu.stanford.nlp.mt.train.transtb.
 *  But I think it's good to make a new one in this package so things don't get tangled up.(
 *
 * @author Pi-Chuan Chang
 */

public class AlignmentMatrix {
  String[] f;
  String[] e;
  boolean[][] fe;
  SemanticGraph chGraph = null;
  TwoDimensionalMap<Integer,Integer,String> pathMap = null;

  void getParseInfo(Tree t) {
    try {
      Filter<String> puncWordFilter = Filters.acceptFilter();
      GrammaticalStructure gs = new ChineseGrammaticalStructure(t, puncWordFilter);
      chGraph = SemanticGraphFactory.makeFromTree(gs, "doc1", 0);
    } catch (Exception e) {
      System.err.println("F="+StringUtils.join(f, " "));
      System.err.println("TREE=");
      t.pennPrint(System.err);
      e.printStackTrace();
    }
  }

  void getPathInfo(String path) throws Exception{
    pathMap = new TwoDimensionalMap<Integer,Integer,String>();
    DepUtils.addPathsToMap(path, pathMap);
  }


  public String getSourceWord(int i) {
    if (i < 0 || i >= f.length) return "";
    return f[i];
  }

  public String getTargetWord(int i) {
    if (i < 0 || i >= e.length) return "";
    return e[i];
  }

  private static String[] preproc(String[] words) {
    return AbstractWordAlignment.preproc(words);
  }

  public AlignmentMatrix(String fStr, String eStr, String aStr) 
    throws IOException{
    // for now, always append the boundary symbols
    fStr = new StringBuffer("<s> ").append(fStr).append(" </s>").toString();
    eStr = new StringBuffer("<s> ").append(eStr).append(" </s>").toString();
    f = preproc(fStr.split("\\s+"));
    e = preproc(eStr.split("\\s+"));

    fe = new boolean[f.length][e.length];

    for(String al : aStr.split("\\s+")) {
      String[] els = al.split("-");
      if(els.length == 2) {
        int fpos = Integer.parseInt(els[0]);
        int epos = Integer.parseInt(els[1]);
        // adding one because of the boundary symbol
        ++fpos; ++epos;
        if(0 > fpos || fpos >= f.length)
          throw new IOException("f has index out of bounds (fsize="+f.length+",esize="+e.length+") : "+fpos);
        if(0 > epos || epos >= e.length)
          throw new IOException("e has index out of bounds (esize="+e.length+",fsize="+f.length+") : "+epos);
        fe[fpos][epos] = true;
      } else {
        throw new RuntimeException("Warning: bad alignment token: "+al);
      }
    }
    
    // add the boundary alignments
    int lastf = f.length - 1;
    int laste = e.length - 1;
    fe[0][0] = true;
    fe[lastf][laste] = true;
  }
}