package abrechnung.privat;

import specs.Contracts;

/** Alle alt, alle neu, splitten. */
//preisanwenden[0] = false;
//preisanwenden[1] = true;
//preisanwenden[2] = false;


   enum PreisanwendenStrategie {
       alleAlt,
       alleNeu,
       splitten;

     public static  PreisanwendenStrategie examine(boolean [] preisanwenden) {
          Contracts.require(preisanwenden.length ==3, "preisanwenden nicht auswertbar");
          if (
              preisanwenden[1]) {
              return alleNeu;
          } else if (
                  preisanwenden[0]) {
              return alleAlt;
          } else {
            return splitten;
        }


       }
   }
