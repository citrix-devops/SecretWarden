package com.cyanoth.secretwarden;

class ScanIncompleteException extends Exception {

   ScanIncompleteException() {
      super("Attempted to retrieve secret scan results, but a scan has not been started yet!");
   }

}
