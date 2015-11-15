(ns seesaw.test.examples.canvas
  (:use seesaw.core
        seesaw.graphics
        seesaw.color))

; A very rudimentary example of (canvas).

; Define some paint handlers. Each takes the canvas and Graphics2D object
; as args. The function is called within a (seesaw.graphics/push) block
; so any changes made to the graphics context will be backed out when
; the function returns.
;

; This first handler uses raw Java2D calls to do painting. See (paint2) below
; for an example of using Seesaw's simple shape support.
(defn paint1 [c g]
  (let [w (.getWidth c)
        h (.getHeight c)]
    (doto g
      (draw (polygon [0 h] [(/ w 4) 0] [(/ w 2) (/ h 2)] [w (/ h 2)] [0 h])
                     (style :foreground java.awt.Color/BLACK
                       :background (color 128 128 128 128)
                       :stroke     (stroke :width 4)))
      (.setColor (color 224 224 0 128))
      (.fillRect 0 0 (/ w 2) (/ h 2))
      (.setColor (color 0 224 224 128))
      (.fillRect 0 (/ h 2) (/ w 2) (/ h 2))
      (.setColor (color 224 0 224 128))
      (.fillRect (/ w 2) 0 (/ w 2) (/ h 2))
      (.setColor (color 224 0 0 128))
      (.fillRect (/ w 2) (/ h 2) (/ w 2) (/ h 2))
      (.setColor (color 0 0 0))
      (.drawString "Hello. This is a canvas example" 20 20))))

(def text-style (style :foreground (color 0 0 0)
                       :font "ARIAL-BOLD-24"))

(def star
  (path []
    (move-to 0 20) (line-to 5 5)
    (line-to 20 0) (line-to 5 -5)
    (line-to 0 -20) (line-to -5 -5)
    (line-to -20 0) (line-to -5 5)
    (line-to 0 20)))

(defn paint2 [c g]
  (let [w (.getWidth c)  w2 (/ w 2)
        h (.getHeight c) h2 (/ h 2)]
    (draw g
      (ellipse 0  0  w2 h2) (style :background (color 224 224 0 128))
      (ellipse 0  h2 w2 h2) (style :background (color 0 224 224 128))
      (ellipse w2 0  w2 h2) (style :background (color 224 0 224 128))
      (ellipse w2 h2 w2 h2) (style :background (color 224 0 0 128)))
    (push g
      (rotate g 20)
      (draw g (string-shape 20 20  "Hello. This is a canvas example") text-style))
    (push g
      (translate g w2 h2)
      (draw g star (style :foreground java.awt.Color/BLACK :background java.awt.Color/YELLOW)))))

; Create an action that swaps the paint handler for the canvas.
; Note that we can use (config!) to set the :paint handler just like
; properties on other widgets.
(defn switch-paint-action [n paint]
  (action :name n
          :handler #(-> (to-frame %)
                      (select [:#canvas])
                      (config! :paint paint))))

(def f
  (frame
    :title "Canvas Example"
    :width 500 :height 300
    :content
    (border-panel :hgap 5 :vgap 5 :border 5
                  ; Create the canvas with initial nil paint function, i.e. just canvas
                  ; will be filled with it's background color and that's it.
                  :center (canvas :id :canvas :background "#BBBBDD" :paint nil)

                  ; Some buttons to swap the paint function
                  :south (horizontal-panel :items ["Switch canvas paint function: "
                                                   (switch-paint-action "None" nil)
                                                   (switch-paint-action "Rectangles" paint1)
                                                   (switch-paint-action "Ovals" paint2)]))))

;(run :dispose)
