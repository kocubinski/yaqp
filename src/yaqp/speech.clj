(ns yaqp.speech
  (:import [com.sun.jna Native]))

(def lib-path "C:/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def lib-path "C:/binski/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def dsound-path "C:/binski/dev/pharmaseq/wand/Apps/DSoundBeeper/x64/Debug/DSoundBeeper.dll")

(defn- get-function [n f]
  (com.sun.jna.Function/getFunction n f))

(defonce _ (System/load lib-path))

(def create-voice* (get-function "tts" "create_voice"))
(def say-words* (get-function "tts" "say_words"))

(defn create-voice []
  (.invoke create-voice* com.sun.jna.Pointer (to-array [])))

(def state (atom {:voice (create-voice)}))

(defn say-words [words]
  (.invoke say-words* Integer (to-array [(:voice @state) (com.sun.jna.WString. words) (int 8)])))

(defn say [words]
  (when (= (say-words words) -2147467259)
    (swap! state assoc :voice (create-voice))
    (say words)))
