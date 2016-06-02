(ns clchess.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [clchess.core-test]))

(enable-console-print!)

(doo-tests 'clchess.core-test)
