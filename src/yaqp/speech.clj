(ns yaqp.speech
  (:import [com.sun.jna Native]))

(def lib-path "C:/binski/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def dsound-path "C:/binski/dev/pharmaseq/wand/Apps/DSoundBeeper/x64/Debug/DSoundBeeper.dll")

(defn- get-function [n f]
  (com.sun.jna.Function/getFunction n f))

(defonce _
  (do (System/load lib-path)
      ;(System/load dsound-path)
      ))

(def create-voice (get-function "tts" "create_voice"))
(def say-words (get-function "tts" "say_words"))
;(def create-voice (get-function "DSoundBeeper" "beeper_create"))

(defn hello-world []
  (let [v (.invoke create-voice com.sun.jna.Pointer (to-array []))]
    (println v)
    (.invoke say-words Integer (to-array [v (com.sun.jna.WString. "Justin is my homie")]))))
