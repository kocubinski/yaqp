(ns yaqp.parse)

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
  (let [timestamp (.substring line 0 26)
        message (.substring line 27)]
    (->> triggers
         (filter (fn [[text _]] (.contains message text)))
         first)
    ))

(def spell-line
  ;;"[Sat Nov 14 22:36:35 2015] You begin casting Clarity."
  "[Sat Nov 14 22:36:35 2015] You begin casting Arch Shielding."
  )

(def sample-line
  "[Sat Nov 14 22:36:35 2015] A cool breeze slips through your mind.")

(def test-triggers
  [["A cool breeze slips through your mind."
    (fn [] (println "yay."))]
   ])

(println
 (second (parse-line sample-line test-triggers)))
