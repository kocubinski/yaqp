(ns yaqp.watcher
  (:use [clojure.java.shell :only [sh]])
  (:import [java.io RandomAccessFile InputStreamReader BufferedReader]
           [java.nio.charset Charset]))

(def log-path "C:/binski/apps/eq/Logs/eqlog_Hadiar_project1999.txt")

(def state (atom {}))

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
                rdr (BufferedReader. (InputStreamReader. stdout (Charset/defaultCharset)))
                ]
      (while (not (:kill @state))
        (do
          (loop [c (.read rdr)]
            (when c
              (print c)
              (recur (.read rdr))))
          (Thread/sleep 100))))))

(defn test-tail []
  (sh "cmd" "/C" "/binski/apps/cygwin64/bin/tail.exe -n 10" log-path))

(defn test-sh []
  (my-sh (str "cmd /C /binski/apps/cygwin64/bin/tail.exe -n 2 " log-path)))
