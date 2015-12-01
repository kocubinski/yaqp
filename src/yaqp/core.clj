(ns yaqp.core
  (:require
   [yaqp.gui :as gui]
   [yaqp.triggers :as tr]
   [yaqp.parse :as p]
   [clj-http.client :as client]
   [clojure.string :as str]
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
         ;;:log-path "/home/makoco/eq-logs/eqlog_Hadiar_project1999.txt"
         :log-path "C:/dev/yaqp/log/eqlog_Hadiar_project1999.txt"
         :timers []}))

(defprotocol Timed
  (expired? [t now])
  (elapsed [_ now])
  (fraction [_ now]))

(defrecord Timer [name start-time duration opts]
  Timed
  (expired? [timer now] (t/> (elapsed timer now) duration))
  (elapsed [_ now] (t/- now start-time))
  (fraction [_ now]
    (let [passed-ms (t/timespan->total-milliseconds
                     (t/- now start-time))
          duration-ms (t/timespan->total-milliseconds duration)]
      (/ (- duration-ms passed-ms) duration-ms))))

(defn timer [text duration & [{:keys [target] :as opts}]]
  (let [target (apply str (take 20 target))
        text (str text " " target)]
    (swap! app update-in [:timers] conj
           (Timer. text (t/now) (tr/parse-time duration) opts))))

(defn clear-timers []
  (swap! app update-in [:timers]
         (fn [timers]
           (remove #(expired? % (t/now)) timers))))

(defn pipe [file & [{:keys [timestamp message line]}]]
  (spit (str eq-logs-dir file)
        (str "[" timestamp "] " message "\n")
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

(def triggers
  `(#"(.*) has been mesmerized." (timer "Mez" "00:24")
    #"(.*) has been enthralled." (timer "Mez" "00:48")
    #"(.*) has been entranced." (timer "Mez" "00:80")

    #"(.*) feels much faster." (timer "Haste" "14:30")
    "A cool breeze slips through your mind." (timer "Crack" "26:00" {:fg "cyan"})
    "the skin breaking and peeling." (timer "Boon" "4:30")

    "out of character," (pipe "chat.txt" {:fg "green"})
    "shouts," (pipe "chat.txt" {:fg "red"})

    "tells the group," (pipe "chat.txt" {:fg "cyan" :bold true})
    "tell your party," (pipe "chat.txt" {:fg "cyan" :bold true})

    "tells the guild," (pipe "chat.txt" {:fg "green" :bold true})
    "tell your guild," (pipe "chat.txt" {:fg "green" :bold true})))

(defn handle-line [line]
  ;(log (str "got line " line))
  (when-not (str/blank? line)
    (p/parse-line line (partition 2 triggers)))
  )

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
           (fn [{:keys [name opts] :as timer} ]
             (Bar. (fraction timer now) name opts)))))
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
