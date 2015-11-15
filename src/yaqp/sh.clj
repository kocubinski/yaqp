(ns yaqp.shell
  "Run OS commands"
  (:import [java.io BufferedReader InputStreamReader]))

(defn run
  "Executes the given command"
  [cmd]
  (.. Runtime getRuntime (exec (str cmd))))

(defn pipe-to
  "Returns a seq of output lines of a command"
  [process]
  (let [r (-> process .getInputStream InputStreamReader. BufferedReader.)]
    (line-seq r)))

(defmacro thread-start
  "Runs the given body in a new thread"
  [& body]
  `(.start
    (Thread.
     (fn []
       ~@body))))

(defn background
  "Runs the given command in a new thread and runs the given function
   on a seq of its output lines."
  [cmd f]
  (thread-start
   (-> (run cmd) pipe-to f)))

(defn daemon
  "Runs the given command in a new thread and runs the given function on
   a seq of the output lines. Restarts the command on exit and runs an
   on-start function each time the command is started"
  [cmd f on-start]
  (thread-start
    (loop []
      (on-start)
      (-> (run cmd) pipe-to f)
      (recur))))
