(ns game.canvas
  (:require [game.config :as config]))

(def ctx (atom {}))

(defn clear []
  (. @ctx clearRect 0 0 config/CANVAS-WIDTH config/CANVAS-HEIGHT))

(defn init-ctx []
  (reset! ctx
          (. (.getElementById js/document config/CANVAS-ID) getContext "2d")))

(defn draw-rect [x y h w color]
  (set! (. @ctx -fillStyle) color)
  (. @ctx fillRect x y h w))