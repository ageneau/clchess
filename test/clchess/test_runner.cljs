(ns clchess.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [clchess.chess-test]))

(enable-console-print!)

(doo-tests 'clchess.chess-test)
