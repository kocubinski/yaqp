(ns yaqp.watcher
  (:use [clojure.java.shell :only [sh]]
        [yaqp.debug :only [log]])
  (:require [clojure.string :as str])
  (:import [java.io RandomAccessFile InputStreamReader BufferedReader]
           [java.nio.charset Charset]))

(def log-path "C:/dev/yaqp/log/eqlog_Hadiar_project1999.txt")

(def state (atom {}))

(defn log-out [msg]
  (spit "C:/dev/yaqp/log/out" (str msg "\r\n") :append true))

(defn raf-seq
  [#^RandomAccessFile raf]
  (if-let [line (.readLine raf)]
    (lazy-seq (cons line (raf-seq raf)))
    (do (Thread/sleep 100)
        (recur raf))))

(defn tail-seq [input]
  (let [raf (RandomAccessFile. input "r")]
    (.seek raf (.length raf))
    (raf-seq raf)))

(defn my-sh [cmd]
  (let [proc (.exec (Runtime/getRuntime) cmd)]
    (with-open [stdout (.getInputStream proc)
                ;;stderr (.getErrorStream proc)
                rdr (BufferedReader. (InputStreamReader. stdout (Charset/defaultCharset)))
                ]
      ;;(println (.waitFor proc))
      (while (not (:kill @state))

        (do
          (loop [c (.read rdr)]
            (when (not= -1 c)
              (log-out (str (format "%02X" c) ":" (char c)))
              (recur (.read rdr))))

          ;;(println "reading...")

          ;; This works fine with echo: "test" >> log.txt
          ;; mingw 'echo' writes "<text>\n[0A]"
          ;; eq probably ends with "\r[0D]\n[0A]"
          ;; (when-let [l (.readLine rdr)]
          ;;   (log-out l))

          (Thread/sleep 100))))))

(defn watch-file* [file line-cb]
  (let [cmd (str "/cygwin64/bin/tail.exe -n 0 -f " file)
        proc (.exec (Runtime/getRuntime) cmd)]
    (swap! state assoc :proc proc)
    (with-open [stdout (.getInputStream proc)
                rdr (BufferedReader. (InputStreamReader. stdout (Charset/defaultCharset)))]
      (while (not (:kill @state))
        (do
          (log-out "reading...")

          (loop [c (.read rdr)
                 s ""]
            (do
              (log-out (str c "\n"))
              (if (= -1 c)
                (do
                  (log-out "end of chunk.")
                  (doseq [line (str/split s #"\n")]
                    (line-cb line)))
                (do
                  (log-out (str (format "%02X" c) ":" (char c) " - " s))
                  (recur (.read rdr)
                         (str s (char c))))
                )))

          (Thread/sleep 1000))))))

(defn test-tail []
  (sh "cmd" "/C" "/cygwin64/bin/tail.exe -n 2" log-path))

(defn test-sh []
  (my-sh (str "cmd /C /cygwin64/bin/tail.exe -n 2 -f " log-path)))

(defn stop-test []
  (swap! state assoc :kill true)
  (.destroy (:proc @state)))

(defn start-test []
  (swap! state dissoc :kill)
  (.start (Thread. (fn []
                     (try
                       (watch-file* log-path #(log-out (str "Called back: " %)))
                       (catch Exception ex
                         (log-out ex)))))))
