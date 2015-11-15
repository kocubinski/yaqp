(ns yaqp.log)

;; debug logging hacks
(def ^:private out *out*)
(defn log [msg]
  (. out (append (str msg "\n"))))
