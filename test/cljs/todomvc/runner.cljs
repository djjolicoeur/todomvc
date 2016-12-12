(ns todomvc.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [todomvc.core-test]))

(doo-tests 'todomvc.core-test)
