(ns yaqp.triggers
  (:require
   [simple-time.core :as t]
   [clojure.string :as str]))

(defn timer [time & actions])

(defn bar [name & {:keys [fg bg] :or {fg "green"
                                      bg "olive"}}]
  {:name name :fg fg :bg bg})

(defn parse-time [time]
  (let [parts (and (string? time)
                   (str/split time #":"))]
    (when (= (count parts) 2)
      (t/seconds->timespan
       (+ (. Integer parseInt (second parts))
          (* 60 (. Integer parseInt (first parts))))))))

(defn parse-action [sexp]
  (let [time (when (-> sexp first string?) (first sexp))
        action (if time (second sexp) sexp)]
    {:time time
     :action (when (ns-resolve 'yaqp.triggers (first action))
               (eval action))}))

;; (defrecord Timed. [start-time finish actions opts]
;;   (let [duration (parse-time finish)]
;;     {:start-time (t/now)
;;      :duration duration
;;      :finish-trigger (when (not duration) finish)
;;      }))

(defn parse-trigger [trigger]
  (let [text (first trigger)]
    (doseq [sexp (rest trigger)]
      (println sexp))))

(defn test-timed []
  (parse-trigger
   '("[target] has been mesmerized."
     (timed {:repeat :new} "00:24"
            (bar "Mez [target]")))))

(test-timed)

(defmacro deftriggers [& body])

(deftriggers

  ("[target] has been enthralled."
   (timed {:repeat :new} "00:48"
          (bar "Mez [target]")
          ))

  ("A cool breeze slips through your mind."
   (timed {:repeat :none} "27:00"
          (bar "Clarity")
          ("26:00" (say "Blue meth please." 3))
          ))

  ("[target] feels much faster."
   (timed {:repeat :new
           :track "Haste"} "14:00"
           (bar "Haste [target]")))


  (("You begin casting Arch Shielding..."
    "You feel armored.")
   (timed {:track "Arch Shielding"} "You feel less armored."))

  ("You feel yourself starting to appear."
   (say "Invis failing." 3))

  ("out of character,"
   (pipe "chat.txt")))
