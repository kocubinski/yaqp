(ns yaqp.test
  (:require  [clojure.test :refer :all]
             [yaqp.core :refer :all]))

;; use the log path inside this project
(swap! app assoc :log-path "C:/dev/yaqp/log/eqlog_Hadiar_project1999.txt")

(defn test-line [line]
  (handle-line
   (str "[Sun Nov 15 01:39:20 2015] " line)))

(defn twenty-quick-lines []
  (dotimes [i 20]
    (test-line (str "your patience" i " is a test. "))
    (Thread/sleep 500)))

(defn some-long-lines []
  (test-line "This is a long test."))
