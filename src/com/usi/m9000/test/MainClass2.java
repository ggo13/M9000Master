package com.usi.m9000.test;

import java.io.IOException;

public class MainClass2 {

  public static void main(String[] args) {

    try {
      while (true) {
        int datum = System.in.read();
        if (datum == -1)
          break;
        System.out.println(datum);
      }
    } catch (IOException ex) {
      System.err.println("Couldn't read from System.in!");
    }
  }
}
