(ns yaqp.core
  (:require
   [yaqp.gui :as gui])
  (:use
   [yaqp.log]
   [simple-time.core :only [timespan now]])
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

(defn timer-bar [text duration]
  (let [bar (Bar. text duration)]
    (gui/add-bar bar)
    (swap! app assoc-in [:timers]
           {:start-time (now)
            :bar bar})))

(defn handle-line [line]
  ;(log line)
  (log "foo")
  )

(defn watch []
  (let [watcher (proxy [TailerListenerAdapter] []
                  (handle [line]
                    (handle-line line)))]
    (swap! app assoc :tail (Tailer/create (File. (:log-path @app)) watcher 500 true))))

(defn tick []
  (gui/render))

(defn run []
  (when-not (:kill @app)
    (Thread/sleep 1000)
    (tick)
    (recur)))

(defn start []
  (.start (Thread. run)))

(defn stop []
  (swap! app assoc :kill true))
