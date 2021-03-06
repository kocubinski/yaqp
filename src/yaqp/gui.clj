(ns yaqp.gui
  (:use [seesaw core graphics color]
        [yaqp.debug])
  (:require [seesaw.mouse :as mouse])
  (:import [java.awt GraphicsEnvironment Color]
           [javax.swing JTextPane JScrollPane JViewport JPanel]
           [javax.swing.text SimpleAttributeSet StyleConstants]))

(native!)

(defrecord Bar [fraction text opts])

(def ^:private state
  (atom {:bars []
         :window {:bars {:opacity 255}
                  :chat {:opacity 60}
                  :scroll-pane nil}
         :layout {:gutter 5
                  :bar-width 240
                  :bar-height 20}}))

(defn bar-clicked? [[x y :as pos]]
  ;; try to guess border width and title bar height...
  (->
   (filter (fn [{:keys [x1 x2 y1 y2]}]
             (and
              (and (> x x1) (< x x2))
              (and (> y y1) (< y y2))))
           (:bars @state))
   first :timer-id))

(defn gray-trans [g a]
  (color g g g a))

(defn set-opacity [f op]
  (println "opacity" op)
  (let [bg (Color. (int 44) (int 44) (int 44) (int op))]
    (.setUndecorated f true)
    (.setBackground f bg)
    (when (= op 255)
      (.setUndecorated f false))
    f))

(def chat-pane
  (config! (make-widget (JTextPane.))
           :background (gray-trans 30 60)
           :opaque? false
           :editable? false))

(defn chat-line [pane line & [{:keys [fg size font bold]
                               :or {fg Color/white size 14 bold false
                                    font "Segoe UI"}}]]
  (println fg)
  (let [style (doto (SimpleAttributeSet.)
                (StyleConstants/setForeground fg)
                (StyleConstants/setFontFamily font)
                (StyleConstants/setFontSize size)
                (StyleConstants/setBold bold))
        doc (. pane getDocument)
        sb (.getVerticalScrollBar (-> @state :window :scroll-pane))]
    (.insertString doc (.. doc getEndPosition getOffset) (str line "\n") style)
    (.setValue sb (.getMaximum sb))))

(defn create-bar-frame []
  (->
   (frame :title "yaqp" :height 225 :width 400)
   (config! :content (canvas :id :canvas :background "#444" :paint nil))
   show!))

;; swing: because fuck you, that's why.
(defn scroll-pane [text-pane]
  (let [vp (JViewport.)
        sp (JScrollPane. text-pane)]
    (. vp setView text-pane)
    (. vp setOpaque false)
    (. sp setViewport vp)
    (.setOpaque (.getViewport sp) false)
    (. sp setOpaque false)
    (swap! state assoc-in [:window :scroll-pane] sp)
    sp))

(defn create-chat-frame []
  (->
   (doto (frame :title "yaqp chat" :height 255 :width 400)
     (.setAlwaysOnTop true))
   ;; (config! :content chat-pane)
   ;; (config! :content
   ;;          (config! (make-widget
   ;;                    (doto (JScrollPane. chat-pane)
   ;;                      #(.setOpaque (.getViewport %) false)))
   ;;                   ;:background (gray-trans 30 30)
   ;;                   :opaque? false
   ;;                   ))
   (config! :content (scroll-pane chat-pane))
   (set-opacity (-> @state :window :chat :opacity int))
   show!))

(defonce bar-frame
  (create-bar-frame))

(defn get-canvas []
  (select bar-frame [:#canvas]))

(defn draw-bar [g gutter width height row col fraction text
                & [{:keys [bg fg color font timer-id]
                    :or {fg (color "green")
                         bg (color "olive")
                         color (color "black")
                         font "DejaVu Sans-BOLD-14"}}]]
  (let [x (+ gutter (* gutter col) (* width col))
        y (+ gutter (* gutter row) (* height row))
        f-x (+ x (* fraction width))
        f-w (- width (* fraction width))]
    (swap! state update-in [:bars] conj {:timer-id timer-id
                                         :x1 x :x2 (+ x width)
                                         :y1 y :y2 (+ y height)})
    (draw g
          (rect x y width height) (style :background fg)
          (rect f-x y f-w height) (style :background bg)
          (string-shape (+ 15 (* gutter col) (* width col))
                        (+ 20 (* gutter row) (* height row)) text)
          (style :foreground color :font font))))

(defn paint [paint-fn]
  (-> bar-frame (select [:#canvas])
      (config! :paint paint-fn)))

(defn render-bars [bars]
  (let [window-width (.getWidth bar-frame)
        window-height (.getHeight bar-frame)
        {:keys [gutter bar-width bar-height]} (:layout @state)
        row-height (+ gutter bar-height)
        row-max (dec (dec (Math/floor (/ window-height (+ gutter bar-height)))))
        col-max (Math/floor (/ window-width (+ gutter bar-width)))]
    (swap! state assoc :bars [])
    (paint
     (when (seq bars)
       (fn [c g]
         (doseq [[{:keys [fraction text opts]} i] (map #(vector %1 %2) bars (range))
                 :let [row (mod i row-max)
                       col (Math/floor (/ i row-max))]]
           (draw-bar g gutter bar-width bar-height row col fraction text opts)))))))

(defn test-paint-bars []
  (render-bars
   [(Bar. 0.8 "Crack" {:fg "Blue" :color "cyan"})
    (Bar. 0.3 "Mez" {})]))

(defn test-fonts []
  (doseq [f (. (GraphicsEnvironment/getLocalGraphicsEnvironment) getAvailableFontFamilyNames)]
    (println f)))

;; need mouse events and position?:
;; http://docs.oracle.com/javase/7/docs/api/javax/swing/text/JTextComponent.html#getToolTipText%28java.awt.event.MouseEvent%29
;; http://docs.oracle.com/javase/7/docs/api/javax/swing/text/JTextComponent.html#viewToModel%28java.awt.Point%29

;; anti-alias? https://wiki.archlinux.org/index.php/Java_Runtime_Environment_fonts
