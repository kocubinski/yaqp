(ns yaqp.speech
  (:import [com.sun.jna Native]
           [com.sun.speech.freetts VoiceManager]
           [java.io PrintStream]
           [org.apache.commons.io.output WriterOutputStream]))

(def lib-path "C:/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")
;(def lib-path "C:/binski/dev/yaqp/tts/yaqp.tts/x64/Debug/tts.dll")

(defn- get-function [n f]
  (com.sun.jna.Function/getFunction n f))

;;(defonce _ (System/load lib-path))

;;(def create-voice* (get-function "tts" "create_voice"))
;;(def say-words* (get-function "tts" "say_words"))

;;n(defn create-voice []
 ;; (.invoke create-voice* com.sun.jna.Pointer (to-array [])))

;;(def state (atom {:voice (create-voice)}))

;;(defn say-words [words]
 ;; (.invoke say-words* Integer (to-array [(:voice @state) (com.sun.jna.WString. words) (int 8)])))

(comment (defn say [words]
           (when (= (say-words words) -2147467259)
             (swap! state assoc :voice (create-voice))
             (say words))))

;; java impl

(def java-voice
  (do
    (System/setProperty "FreeTTSSynthEngineCentral" "com.sun.speech.freetts.jsapi.FreeTTSEngineCentral")
    (System/setProperty "com.sun.speech.freetts.voice.defaultAudioPlayer" "yaqp.PatchAudioPlayer")
    (System/setProperty "freetts.voices" "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")
    ;;(System/setProperty "freetts.voices" "com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory")
    (let [voice (.getVoice (VoiceManager/getInstance) "kevin16")]
      (.allocate voice)
      voice)))

(defn say [words]
  (.speak java-voice words))
