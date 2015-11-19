(ns yaqp.parse
  (:require [simple-time.core :as t]))

(def spells
  {"A cool breeze slips through your mind." "Clarity"})

(def state (atom
            {:begin-casting []
             :buffs []}))

(defn now [] (new java.util.Date))

(defn spell-start [m]
  (when-let [[_ spell] (re-find #"You begin casting (.*)\." m)]
    (swap! state assoc :spells [spell (now)])
    spell))

(defn spell-cast [m]
  )

(defn parse-line [line triggers]
  (let [timestamp (.substring line 12 20)
        message (.substring line 27)]
    (doseq [[text f] triggers]

      ;(println text message)
      (when (instance? java.util.regex.Pattern text)
        (when-let [match (re-find text message)]
          (eval
           (-> f vec (conj {:target (second match)
                            :timestamp timestamp}) seq))))

      (when (and (instance? String text)
                 (.contains message text))
        (eval
         (-> f vec (conj {:timestamp timestamp}) seq))))))

(def spell-line
  "[Sat Nov 14 22:36:35 2015] A cool breeze slips through your mind."
  ;;"[Sat Nov 14 22:36:35 2015] You begin casting Arch Shielding."
  ;;"[Sat Nov 14 22:36:35 2015] A foobar has been mesmerized."
  )

(def sample-line
  "[Sat Nov 14 22:36:35 2015] A cool breeze slips through your mind.")

(defn a-printer [text & [{:keys [target timestamp]}]]
  (println text target timestamp))

(def test-triggers
  `(
    ("A cool breeze slips through your mind." (a-printer "Hi"))
    (#"(.*) has been mesmerized.") (a-printer "Hi")))



(parse-line spell-line test-triggers)
