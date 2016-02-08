(ns yaqp.watcher
  (:use [clojure.java.shell :only [sh]])
  (:import [java.io RandomAccessFile InputStreamReader BufferedReader]
           [java.nio.charset Charset]))

(def log-path "C:/binski/apps/eq/Logs/eqlog_Hadiar_project1999.txt")

(def state (atom {}))

(defn log-out [msg]
  (spit "C:/binski/dev/yaqp/log/out" (str msg "\r\n") :append true))

(defn watch-file [file cb]
  (let [cmd (str "cmd /C C:/binski/apps/cygwin64/bin/tail.exe -n 0 -f " file)
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

(defn test-watch []
  (swap! state assoc :kill false)
  (.start (Thread.
           (fn []
             (try
               (watch-file log-path #(log-out %))
               (catch Exception e
                   (log-out e)))))))
