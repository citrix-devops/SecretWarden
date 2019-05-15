package com.cyanoth.secretwarden;

public class ScanIncompleteException extends Exception {

   public String toString() {
      return "Attempted to retrieve secret scan results, but a scan has not been started yet!";
   }

}
