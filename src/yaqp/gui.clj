(ns yaqp.gui
  (:use [seesaw core graphics color]
        [yaqp.log]))

(native!)

(defrecord Bar [fraction text])

(def ^:private state
  (atom {:bars []
         :layout {:gutter 5
                  :bar-width 180
                  :bar-height 20}}))

(defn add-bar [bar]
  (swap! state update-in [:bars] conj bar))

(defn remove-bar [bar])

(defonce f
  (->
   (frame :title "feed3"
          :height 225 :width 400)
   (config! :content (canvas :id :canvas :background "#444" :paint nil))
   show!))

(def text-style (style :foreground (color 0 0 0)
                       :font "MONOSPACED-20"))

(defn draw-bar [g gutter width height row col fraction text]
  (let [x (+ gutter (* gutter col) (* width col))
        y (+ gutter (* gutter row) (* height row))
        f-x (+ x (* fraction width))
        f-w (- width (* fraction width))]
    (draw g
          (rect x y width height) (style :background (color "green"))
          (rect f-x y f-w height) (style :background (color "olive"))
          (string-shape (+ 20 (* gutter col) (* width col))
                        (+ 22 (* gutter row) (* height row)) text)
          text-style)))

(defn paint [paint-fn]
  (-> f (select [:#canvas])
      (config! :paint paint-fn)))

(defn render-bars [bars]
  (let [window-width (.getWidth f)
        window-height (.getHeight f)
        {:keys [gutter bar-width bar-height]} (:layout @state)
        row-height (+ gutter bar-height)
        row-max (dec (Math/floor (/ window-height (+ gutter bar-height))))
        col-max (Math/floor (/ window-width (+ gutter bar-width)))]
    (paint
     (when (seq bars)
       (fn [c g]
         (doseq [[{:keys [fraction text]} i] (map #(vector %1 %2) bars (range))
                 :let [row (mod i row-max)
                       col (Math/floor (/ i row-max))]]
                                        ;(log i)
           (draw-bar g gutter bar-width bar-height row col fraction text)))))))

(defn test-paint-bars []
  (render-bars
   [(Bar. 0.8 "Mez")
    (Bar. 0.3 "Mez")]))

(defn test-clear []
  (render-bars []))



  (let [now (t/now)


                                   (:timers @app))
