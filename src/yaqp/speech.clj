(ns yaqp.speech
  (:import [com.sun.jna Native]))

(def lib-path "C:/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def lib-path "C:/binski/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def dsound-path "C:/binski/dev/pharmaseq/wand/Apps/DSoundBeeper/x64/Debug/DSoundBeeper.dll")

(defn- get-function [n f]
  (com.sun.jna.Function/getFunction n f))

(defonce _ (System/load lib-path))

(def create-voice (get-function "tts" "create_voice"))
(def say-words (get-function "tts" "say_words"))

(defonce voice (.invoke create-voice com.sun.jna.Pointer (to-array [])))

(defn say [words]
  (.invoke say-words Integer (to-array [voice (com.sun.jna.WString. words) (int 8)])))
