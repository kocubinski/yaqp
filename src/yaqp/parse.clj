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
        message (.substring line 27)
        append-args
        (fn [sx args]
          (if (map? (-> sx vec last))
            (update-in sx [(-> sx count dec)] merge args)
            (conj sx args)))]
    (doseq [[text f :as g] triggers]
      (when (instance? java.util.regex.Pattern text)
        (when-let [match (re-find text message)]
          (eval
           (-> f vec (append-args {:target (second match)
                                   :timestamp timestamp}) seq))))

      (when (and (instance? String text)
                 (.contains message text))
        (eval
         (-> f vec (append-args {:timestamp timestamp}) seq))))))
                          :message message
                          :line line}) seq))))))

(def spell-line
  ;;"[Sat Nov 14 22:36:35 2015] A cool breeze slips through your mind."
  ;;"[Sat Nov 14 22:36:35 2015] You begin casting Arch Shielding."
  "[Sat Nov 14 22:36:35 2015] A foobar has been mesmerized."
  )

(def sample-line
  "[Sat Nov 14 22:36:35 2015] A cool breeze slips through your mind.")

(defn a-printer [text & [{:keys [target timestamp fg]}]]
  (println text target timestamp fg))

(def test-triggers
  `(
    ("A cool breeze slips through your mind." (a-printer "Hi" {:fg "blue"}))
    (#"(.*) has been mesmerized." (a-printer "Hi" {:fg "blue"}))))

(parse-line spell-line test-triggers)
