(ns yaqp.core
  (:require
   [yaqp.gui :as gui]
   [yaqp.parse :as p]
   [yaqp.watcher :as watcher]
   [clojure.string :as str]
   [simple-time.core :as t]
   [seesaw.mouse :as mouse])
  (:use
   [yaqp.debug]
   [yaqp.speech :only [say]]
   [seesaw.core :only [listen]])
  (:import
   [yaqp.gui Bar]))

(def eq-logs-dir "C:/binski/apps/eq/Logs/")

(def app
  (atom {:kill nil
         :tails []
         :log-paths
         ["/home/mattk/wine/eq/drive_c/eq/Logs/eqlog_Hypermagic_P1999Green.txt"
          "/home/mattk/wine/eq/drive_c/eq/Logs/eqlog_Haddgar_P1999Green.txt"]
         :pk 1
         :timers (sorted-map-by >)}))

(defprotocol Timed
  (expired? [t now])
  (remaining [t now])
  (elapsed [_ now])
  (fraction [_ now]))

(defrecord Timer [id name start-time duration opts]
  Timed
  (expired? [timer now] (t/> (elapsed timer now) duration))
  (elapsed [_ now] (t/- now start-time))
  (remaining [t now] (t/- duration (elapsed t now)))
  (fraction [_ now]
    (let [passed-ms (t/timespan->total-milliseconds
                     (t/- now start-time))
          duration-ms (t/timespan->total-milliseconds duration)]
      (/ (- duration-ms passed-ms) duration-ms))))

(defn parse-time [time]
  (let [parts (and (string? time)
                   (str/split time #":"))]
    (when (= (count parts) 2)
      (t/seconds->timespan
       (+ (. Integer parseInt (second parts))
          (* 60 (. Integer parseInt (first parts))))))))

(defn timer [text duration & [{:keys [target on-end] :as opts}]]
  (let [target (apply str (take 20 target))
        text (str text " " target)
        id  (:pk (swap! app update :pk inc))]
    (swap! app assoc-in [:timers id] (Timer. id text (t/now) (parse-time duration) opts))))

(defn pipe [file & [{:keys [timestamp message line]}]]
  (spit (str eq-logs-dir file)
        (str "[" timestamp "] " message "\n")
        :append true))

(defn speak [text & [{:keys [target]}]]
  (future (say (if target
                 (str/replace text #"%t" target)
                 text))))

(def triggers
  `(
    "The cool breeze fades." (speak "Blue meth please?")
    "charm spell has worn off" (speak "Pet is loose, pet is loose.")
    #"(.*) looks very uncomfortable" (speak "%t is malo")
    "You feel yourself starting to appear" (speak "Invis fading")
    #"(.*) yawns" (speak "slow %t")
    #"(.*) slows down" (speak "slow %t")

    "hits you" (speak "hit")

    "You begin casting Dictate" (timer "Dictate" "00:52" {:remaining? true})

;;    #"(.*) looks less aggressive" (timer "Calm" "3:00")
    #"(.*) looks less aggressive" (timer "Paci" "7:00")

    #"(.*) has been mesmerized by the Glamour" (timer "Mez" "00:30")
    #"(.*) has been mesmerized\." (timer "Mez" "00:24")
    #"(.*) has been enthralled." (timer "Mez" "00:48")
    #"(.*) has been entranced." (timer "Mez" "00:80")
    #"(.*) has been fascinated." (timer "Mez" "00:36")

    #"Basher (.*) has been slain by" (timer "Pop" "24:00"
                                            {:remaining? true
                                             :on-end #(speak "%t pop" %)})

    #"(.*) says, 'Curse you" (timer "" "24:00"
                                    {:remaining? true
                                     :on-end #(speak "%t pop" %)})
    

    #"You have slain Bouncer (.*)" (timer "Pop" "24:00"
                                          {:remaining? true
                                           :on-end #(speak "%t pop" %)})

    #"You have slain Basher (.*)" (timer "" "24:00"
                                          {:remaining? true
                                           :on-end #(speak "%t pop" %)})

;;    "slain by" (timer "Pop" "6:40" {:remaining? true})
;;    "You have slain" (timer "Pop" "6:40" {:remaining? true})

    ;; "a spectre has been slain" (timer "spectre" "6:40"
    ;;                                   {:remaining? true})
    ;; "You have slain a spectre" (timer "spectre" "6:40"
    ;;                                   {:remaining? true})

    ;; "You gain experience" (timer "Pop" "27:00"
    ;;                              {:remaining? true})

    "You have slain Cynthia" (timer "Cynthia" "5:50"
                                    {:remaining? true})
    
    ;; "You have slain Cynthia" (timer "Cynthia Warn" "5:35"
    ;;                                 {:remaining? true
    ;;                                  :on-end #(speak (str "Cynthia pop, Cynthia pop" %))})


    ;;"You gain party experience" (timer "Pop" "27:00"
;;                                        {:remaining? true})

     ;; "has been slain by" (timer "Pop" "18:00"
     ;;                            {:remaining? true})

     ;; "You have slain" (timer "Pop" "18:00"
     ;;                         {:remaining? true})

    ;; #"(.*) feels much faster." (timer "Swift" "14:30")

    #"(.*) experiences a quickening." (timer "Haste" "24:00"
                                             {:on-end #(speak "%t needs A.Q." %)})

    #"(.*) begins to move with wonderous rapidity." (timer "WR" "18:20"
                                                           {:on-end #(speak "%t needs haste." %)})

    #"(.*) experiences visions of grandeur." (timer "VoG" "42:00")
    ;#"(.*) experiences a quickening." (timer "AQ" "0:05")

    ;;#"(.*) is surrounded by a thorny barrier." (timer "Thorns" "2:30")

    ;;#"(.*) looks stronger." (timer "Strength" "27:00"
    ;;#"(.*)'s skin turns hard as steel." (timer "Skin" "36:00")
    ;;#"(.*) feet adhere to the ground." (timer "Root" "3:00")
    "You begin to sneak" (timer "Sneak" "0:08")
    "Subgenius drops dead." (timer "Feign" "0:10")

    "A cool breeze slips through your mind." (timer "Crack" "26:00" {:fg "cyan"})
    "A soft breeze slips through your mind." (timer "Crack" "35:00" {:fg "blue"
                                                                     :color "white"})

    "You gather mana from your surroundings." (timer "Harvest" "10:00" {:remaining? true})
    ;;"Your spirit screams with berserker strength." (timer "Zerk" "5:00")
    ;;"the skin breaking and peeling." (timer "Boon" "4:30")

    ;;#"You say, '(.*)-"

    #"(.*)'s body pulses with the spirit of the Shissar" (timer "SoS" "18:00")

    #"(.*) is a test." (timer "Test" "0:05" {:on-end #(speak "%t is a test" %)})
    #"(.*) is a long test." (timer "Test" "5:00" {:on-end #(speak "%t is a test" %)})
    ))

(comment
    "out of character," (pipe "chat.txt" {:fg "green"})
    "shouts," (pipe "chat.txt" {:fg "red"})

    "tells the group," (pipe "chat.txt" {:fg "cyan" :bold true})
    "tell your party," (pipe "chat.txt" {:fg "cyan" :bold true})

    "tells the guild," (pipe "chat.txt" {:fg "green" :bold true})
    "say to your guild," (pipe "chat.txt" {:fg "green" :bold true})

    "you say," (pipe "chat.txt" {:bold true})
    "tells you," (pipe "chat.txt" {:bold true})
    "You told" (pipe "chat.txt" {:bold true})

    "has looted a" (pipe "chat.txt" {:bold true})
  )

(defn handle-line [line]
  ;;(log (str "got line " line))
  (when-not (str/blank? line)
    (p/parse-line line (partition 2 triggers))))

(defn clear-timers []
  (swap! app update-in [:timers]
         (fn [timers]
           (remove #(expired? % (t/now)) timers))))

(defn tick [app]
  ;; iterate through timers, draw fraction according to diff from (now) - start, duration
  (let [timers (-> @app :timers vals)
        now (t/now)
        live-timers (filter #(not (expired? % now)) timers)
        live-ids (set (map :id live-timers))
        dead-timers (filter #(not (live-ids (:id %))) timers)]
    (gui/render-bars
     (->> live-timers
          (map
           (fn [{:keys [id name opts] :as timer}]
             (Bar. (fraction timer now)
                   (if (:remaining? opts)
                     (let [ts (remaining timer now)]
                       (format "%s - %sm%ss" name
                               (t/timespan->minutes ts)
                               (t/timespan->seconds ts)))
                     name)
                   (assoc opts :timer-id id))))))
    (doseq [t dead-timers]
      (when-let [f (-> t :opts :on-end)]
        (f (:opts t))))
    (swap! app update-in [:timers] #(apply dissoc % (map :id dead-timers)))
    ))

(defn on-bar-frame-mouse-click [e]
  (when (= :right (mouse/button e))
    (swap! app update-in [:timers] dissoc (gui/bar-clicked? (mouse/location e)))))

(defn run []
  (when-not (:kill @app)
    (Thread/sleep 100)
    (try
      (tick app)
      (catch Exception e
        (log e)))
    (recur)))

(defn start []
  (swap! app assoc :kill false)
  (swap! app update :events conj (listen (gui/get-canvas) :mouse-clicked #'on-bar-frame-mouse-click))

  ;; file tailer
  ;;(swap! watcher/state dissoc :kill)
  ;;(.start (Thread. (fn []
  ;;                   (watcher/watch-file (:log-path @app) #'handle-line))))

  (swap! app assoc :tails (map (fn [file]
                                 (watcher/tailer file #'handle-line))
                               (:log-paths @app)))

  ;; main loop
  (.start (Thread. run)))

(defn stop []
  (doseq [t (:tails @app)]
    (.stop t))
  (doseq [rm (:events app)] (rm))
  (swap! watcher/state assoc :kill true)
  (swap! app assoc :kill true :events []))
