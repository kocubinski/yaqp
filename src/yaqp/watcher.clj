(ns yaqp.watcher
  (:use [clojure.java.shell :only [sh]])
  (:import [java.io RandomAccessFile InputStreamReader BufferedReader]
           [java.nio.charset Charset]))

(def log-path "C:/binski/apps/eq/Logs/eqlog_Hadiar_project1999.txt")

(def state (atom {}))

(defn log-out [msg]
  (spit "C:/binski/dev/yaqp/log/out" (str msg "\r\n") :append true))

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
                stderr (.getErrorStream proc)
                rdr (BufferedReader. (InputStreamReader. stdout (Charset/defaultCharset)))
                erdr (BufferedReader. (InputStreamReader. stderr (Charset/defaultCharset)))
                ]
      ;;(println (.waitFor proc))
      (while (not (:kill @state))

        (do

          ;; (loop [c (.read rdr)]
          ;;   (when (not= -1 c)
          ;;     (log-out (str (format "%02X" c) ":" (char c)))
          ;;     (recur (.read rdr))))

          ;;(println "reading...")

          ;; This works fine with echo: "test" >> log.txt
          ;; mingw 'echo' writes "<text>\n[0A]"
          ;; eq probably ends with "\r[0D]\n[0A]"
          ;(println "reading...")

          (when-let [l (.readLine rdr)]
            ;(log-out l)
            (println l)
            )

          (Thread/sleep 100))))))

(defn test-tail []
  (sh "cmd" "/C" "/cygwin64/bin/tail.exe -n 2" log-path))

(defn test-sh []
  ;(my-sh (str "cmd /C dir"))
  (my-sh (str "cmd /C C:/binski/apps/cygwin64/bin/tail.exe -n 0 -f " log-path)))

(defn stop-test []
  (swap! state assoc :kill true))

(defn start-test []
  (swap! state assoc :kill false)
  (.start (Thread. test-sh)))

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
