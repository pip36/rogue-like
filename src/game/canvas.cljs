(ns game.canvas)

(def ID "gameCanvas")
(def WIDTH 500)
(def HEIGHT 500)

(def ctx (atom {}))

(defn clear []
  (. @ctx clearRect 0 0 WIDTH HEIGHT))

(defn init-ctx []
  (reset! ctx
          (. (.getElementById js/document "gameCanvas") getContext "2d")))

(defn draw-rect [x y h w color]
  (set! (. @ctx -fillStyle) color)
  (. @ctx fillRect x y h w))