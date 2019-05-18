package com.cyanoth.secretwarden;

public class ScanIncompleteException extends Exception {

   public ScanIncompleteException() {
      super("Attempted to retrieve secret scan results, but a scan has not been started yet!");
   }

}
