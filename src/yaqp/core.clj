(ns yaqp.core
  (:require
   [yaqp.gui :as gui]
   [simple-time.core :as t])
  (:use
   [yaqp.log])
  (:import
   [yaqp.gui Bar]
   [java.io File]
   [org.apache.commons.io.input TailerListenerAdapter Tailer])
  )

;; (defn watch-test []
;;   (w/watcher ["C:/binski/dev/apps/eq/notes.txt"]
;;              (w/rate 50) ;; poll every 50ms
;;              (w/file-filter ignore-dotfiles) ;; add a filter for the files we care about
;;              (w/file-filter (extensions :clj :cljs)) ;; filter by extensions
;;              (w/on-change #(println "files changed: " %))
;;              ))

(defn trigger
  ([on-start] (trigger on-start nil nil nil))
  ([on-start on-update on-end duration]
   {:on-start on-start
    :on-update on-update
    :on-end on-end
    :duration duration}))

(def triggers
  {"A cool breeze slips through your mind."
   {:on-start nil
    :on-update nil
    :on-end nil}})

(def app
  (atom {:kill nil
         :tail nil
         :log-path "/home/makoco/eq-logs/eqlog_Hadiar_project1999.txt"
         :timers []}))

(defprotocol Trigger
  (on-trigger []))

(defprotocol Timed
  (expired? [now])
  (time-passed [now])
  )

(defrecord Timer [name start-time duration]
  Timed
  (expired? [now] (t/< (time-passed now) duration))
  (time-passed [now] (t/- now start-time)))

(defn add-timer! [text duration]
  (swap! app update-in [:timers] conj
         (Timer. text (time/now) duration-ms)))

(defn handle-line [line]
  ;;(log "foo")
  )

(defn watch []
  (let [watcher (proxy [TailerListenerAdapter] []
                  (handle [line]
                    (handle-line line)))]
    (swap! app assoc :tail (Tailer/create (File. (:log-path @app)) watcher 500 true))))

(defn tick [app]
  ;; iterate through timers, draw fraction according to diff from (now) - start, duration
  (log "tick")
  (let [now (time/now)
        [live-timers dead-timers] (split-with (fn [{:keys [start-time duration]}]
                                                )
                                              (:timers app))]
    (gui/render-bars
     (map
      (fn [{:keys [start-time duration text]}]
        (let [passed-ms (time/timespan->total-milliseconds
                         (time/- now start-time))
              duration-ms (time/timespan->total-milliseconds duration)]
          (Bar. (/ (- duration-ms passed-ms) duration-ms) text)))
      live-timers))))

(defn run []
  (when-not (:kill @app)
    (Thread/sleep 100)
    (try
      (tick)
      (catch Exception e
        (log e)))
    (recur)))

(defn start []
  (swap! app assoc :kill false)
  (.start (Thread. run)))

(defn stop []
  (swap! app assoc :kill true))
