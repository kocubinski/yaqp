(ns yaqp.core-test
  (:require [clojure.test :refer :all]
            [yaqp.core :refer :all]))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

(def test-lines
  ["[Thu Nov 12 22:03:00 2015] A puma has been mesmerized."
   "[Thu Nov 12 22:03:00 2015] A skeletal guardian has been mesmerized."
   "[Thu Nov 12 22:03:00 2015] A decayed prisoner has been mesmerized."
   "[Thu Nov 12 22:03:00 2015] A dorvlag sentry has been mesmerized."
   "[Thu Nov 12 22:03:00 2015] A cool breeze slips through your mind."
   "[Thu Nov 12 22:03:00 2015] Mycah feels much faster."
   "[Thu Nov 12 22:03:00 2015] Famas feels much faster."
   "[Thu Nov 12 22:03:00 2015] Sokum feels much faster."
   ])

(defn test-render-bars []
  (doseq [line test-lines]
    (handle-line line)))

(test-render-bars)
