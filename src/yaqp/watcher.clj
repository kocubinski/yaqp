(ns yaqp.watcher
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.java.io :as io])
  (:import [java.io RandomAccessFile InputStreamReader BufferedReader]
           [org.apache.commons.io.input TailerListenerAdapter Tailer]
           [java.nio.charset Charset]))

;(def tail "cmd /C C:/binski/apps/cygwin64/bin/tail.exe -n 0 -f")
(def tail "tail")
(def log-path "C:/binski/apps/eq/Logs/eqlog_Hadiar_project1999.txt")

(def state (atom {}))
 
(defn log-out [msg]
  (spit "C:/binski/dev/yaqp/log/out" (str msg "\r\n") :append true))

(defn watch-file [file cb]
  (let [;; (str "cmd /C C:/binski/apps/cygwin64/bin/tail.exe -n 0 -f " file)
        cmd (str tail " -n 0 -f " file)
        proc (.exec (Runtime/getRuntime) cmd)]
    (with-open [stdout (.getInputStream proc)
                rdr (BufferedReader. (InputStreamReader. stdout (Charset/defaultCharset)))]
      (while (not (:kill @state))
        (do
          (loop [l (.readLine rdr)]
            (when l
              (cb l)
              (recur (.readLine rdr))))
          (Thread/sleep 100))))))

(defn tailer
  [^String file cb]
  (let [listener (proxy [TailerListenerAdapter] []
                   (^void handle [^String line]
                    (cb line)))
        tailer (Tailer.
                (io/file file)
                listener
                50  ; poll delay
                true ; listen from end
                )
        thread (Thread. tailer)]
    (.setDaemon thread true)
    (.start thread)
    tailer))

(defn test-watch []
  (swap! state assoc :kill false)
  (.start (Thread.
           (fn []
             (try
               (watch-file log-path #(log-out %))
               (catch Exception e
                   (log-out e)))))))
