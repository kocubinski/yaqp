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
         :tail nil
         ;;:log-path "/home/kocubinski/wine/eq-live/drive_c/eq/Logs/eqlog_Hadiar_agnarr.txt"
         :log-path "/home/kocubinski/wine/eq/drive_c/eq/Logs/eqlog_Coldeeze_project1999.txt"
         ;;:log-path "C:/dev/yaqp/log/eqlog_Hadiar_project1999.txt"
         ;:log-path "C:/binski/apps/eq/Logs/eqlog_Subgenius_project1999.txt"
         ;;:log-path "C:/binski/apps/eq/Logs/eqlog_Hadiar_project1999.txt"
         :pk 1
         :timers (sorted-map-by >)}))

(defprotocol Timed
  (expired? [t now])
  (elapsed [_ now])
  (fraction [_ now]))

(defrecord Timer [id name start-time duration opts]
  Timed
  (expired? [timer now] (t/> (elapsed timer now) duration))
  (elapsed [_ now] (t/- now start-time))
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
    "The soft breeze fades." (speak "Blue meth please?")
    "Cajoling Whispers spell has worn off" (speak "Pet is loose, pet is loose.")
    "Allure spell has worn off" (speak "Pet is loose, pet is loose.")
    "Boltran's Agacerie spell has worn off" (speak "Pet is loose, pet is loose.")
    
    #"(.*) looks less aggressive" (timer "Lull" "0:42")

    #"(.*) has been mesmerized by the Glamour" (timer "Mez" "00:54")
    #"(.*) has been mesmerized\." (timer "Mez" "00:24")
    #"(.*) has been enthralled." (timer "Mez" "00:48")
    #"(.*) has been entranced." (timer "Mez" "00:80")
    #"(.*) has been fascinated." (timer "Mez" "00:36")


    #"(.*) feels much faster" (timer "Haste" "15:30")
    #"(.*)'s body pulses with energy" (timer "Aug" "26:00")
    #"(.*) experiences a quickening." (timer "Haste" "24:00"
                                             {:on-end #(speak "%t needs A.Q." %)})

    #"(.*) experiences visions of grandeur." (timer "VoG" "42:00")
    ;#"(.*) experiences a quickening." (timer "AQ" "0:05")

    ;;#"(.*) is surrounded by a thorny barrier." (timer "Thorns" "2:30")

    ;;#"(.*) looks stronger." (timer "Strength" "27:00"
    ;;#"(.*)'s skin turns hard as steel." (timer "Skin" "36:00")
    ;;#"(.*) feet adhere to the ground." (timer "Root" "3:00")
    "You begin to sneak" (timer "Sneak" "0:08")
    "Subgenius drops dead." (timer "Feign" "0:10")

    "A cool breeze slips through your mind." (timer "Crack" "26:00" {:fg "cyan"
                                                                     :on-end #(speak "Blue meth please.")})
    "A soft breeze slips through your mind." (timer "Crack" "35:00" {:fg "blue" :color "white"})
    ;;"Your spirit screams with berserker strength." (timer "Zerk" "5:00")
    ;;"the skin breaking and peeling." (timer "Boon" "4:30")

    #"(.*)'s body pulses with the spirit of the Shissar" (timer "SoS" "18:00"
                                                                {:on-end #(speak "%t needs sos" %)})

    #"(.*) is a test." (timer "Test" "0:05" {:on-end #(speak "%t is a test" %)})
    #"(.*) is a long test." (timer "Test" "5:00" {:on-end #(speak "%t is a test" %)})

    ;;#"(.*) glances nervously about" (speak "%t tashed")
    ;;#"(.*) looks very uncomfortable" (speak "%t malodw")

    "You feel yourself starting to appear" (speak "Invis fading, invis fading.")
    
    ;; shaman triggers
    ;;#"(.*) feels much faster." (timer "Alacrity" "10:00")
    ;;#"(.*) looks stronger." (timer "Strength" "54:00")
    ;;#"(.*) looks robust." (timer "Stamina" "63:00")
    ;;#"(.*) looks agile." (timer "Agility" "63:00")
    ;;#"(.*) begins to regenerate." (timer "Chloro" "14:20" {:fg "cyan"})

    ;; EC
    ;;#"(.*) tells you," (speak "tell from %t")
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
           (fn [{:keys [id name opts] :as timer} ]
             (Bar. (fraction timer now) name (assoc opts :timer-id id))))))
    (doseq [t dead-timers]
      (when-let [f (-> t :opts :on-end)]
        (f (:opts t))))
    (swap! app update-in [:timers] #(apply dissoc % (map :id dead-timers)))
    ))

(defn on-bar-frame-mouse-click [e]
  (when (= :right (mouse/button e))
    (when-let [timer-id (gui/bar-clicked? (mouse/location e))]
      (swap! app update-in [:timers] dissoc timer-id))))

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
 ;; (swap! app update :events conj (listen (gui/get-canvas) :mouse-clicked #'on-bar-frame-mouse-click))

  ;; file tailer
  (swap! watcher/state dissoc :kill)
  (.start (Thread. (fn []
                     (watcher/watch-file (:log-path @app) #'handle-line))))

  ;; main loop
  (.start (Thread. run)))

(defn stop []
  (when-let [tail (:tail @app)]
    (.stop tail))
  ;;(doseq [rm (:events app)] (rm))
  (swap! watcher/state assoc :kill true)
  (swap! app assoc :kill true))

;; events
(defonce init
  (do
    (gui/add-event :mouse-clicked #'on-bar-frame-mouse-click)
    (gui/reset-bar-frame!)))
