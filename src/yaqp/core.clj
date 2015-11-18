(ns yaqp.core
  (:require
   [yaqp.gui :as gui]
   [yaqp.triggers :as tr]
   [clj-http.client :as client]
   [simple-time.core :as t])
  (:use
   [yaqp.debug])
  (:import
   [yaqp.gui Bar]
   [java.io File]
   [org.apache.commons.io.input TailerListenerAdapter Tailer]
   [java.io File FileOutputStream ByteArrayInputStream]
   [javazoom.jl.player Player]))

(def eq-logs-dir "/home/makoco/eq-logs/")

(def app
  (atom {:kill nil
         :tail nil
         :log-path "/home/makoco/eq-logs/eqlog_Hadiar_project1999.txt"
         :timers []}))

(defprotocol Timed
  (expired? [t now])
  (elapsed [_ now])
  (fraction [_ now]))

(defrecord Timer [name start-time duration]
  Timed
  (expired? [timer now] (t/> (elapsed timer now) duration))
  (elapsed [_ now] (t/- now start-time))
  (fraction [_ now]
    (let [passed-ms (t/timespan->total-milliseconds
                     (t/- now start-time))
          duration-ms (t/timespan->total-milliseconds duration)]
      (/ (- duration-ms passed-ms) duration-ms))))

(defn add-timer! [text duration]
  (swap! app update-in [:timers] conj
         (Timer. text (t/now) (tr/parse-time duration))))

(defn clear-timers []
  (swap! app update-in [:timers]
         (fn [timers]
           (remove #(expired? % (t/now)) timers))))

(defn pipe [line file]
  (spit (str eq-logs-dir file) (str line "\n")
        :append true))

(defn say [text times]
  (doseq [_ (range times)]
    (let [mp3 (:body (client/get "http://translate.google.com/translate_tts"
                                 {:query-params {"ie" "UTF-8"
                                                 "tl" "en"
                                                 "q" text}
                                  :as :byte-array})) ]
      (with-open [player (new Player (ByteArrayInputStream. mp3))]
        (.play player)))))

(defn handle-line [line]
  ;(log (str "got line " line))
  (condp (fn [needle hay] (.contains hay needle)) line
      "has been mesmerized." (add-timer! "Mez" "00:24")
      "has been entranced." (add-timer! "Mez" "00:80")
      "has been enthralled." (add-timer! "Mez" "00:48")

      "feels much faster." (add-timer! "Haste" "14:30")
      "A cool breeze slips through your mind." (add-timer! "Crack" "26:00")
      "the skin breaking and peeling." (add-timer! "Boon" "4:30")
      ;;"The cool breeze fades." (say "Blue meth please" 3)

      "out of character," (pipe line "chat.txt")
      "shouts," (pipe line "chat.txt")

      "tells the group," (pipe line "chat.txt")
      "tell your party," (pipe line "chat.txt")

      "tells the guild," (pipe line "chat.txt")
      "tell your guild," (pipe line "chat.txt")
      nil))

(defn watch []
  (let [watcher (proxy [TailerListenerAdapter] []
                  (handle [line]
                    (handle-line line)))]
    (swap! app assoc :tail (Tailer/create (File. (:log-path @app)) watcher 100 true))))

(defn tick [app]
  ;; iterate through timers, draw fraction according to diff from (now) - start, duration
  (let [now (t/now)
        [dead-timers live-timers] (split-with #(expired? % now) (:timers @app))]
    (gui/render-bars
     (->> (:timers @app)
          (filter #(not (expired? % now)))
          (map
           (fn [{:keys [name] :as timer} ]
             (Bar. (fraction timer now) name)))))
    (clear-timers)
    ;(swap! app update-in [:timers] #(remove #{dead-timers} %))
    ))

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
  (watch)
  (.start (Thread. run)))

(defn stop []
  (when-let [tail (:tail @app)]
   (.stop tail))
  (swap! app assoc :kill true))
