(ns game.monster
  (:require
   [game.tile-map :as m]
   [game.canvas :as canvas]))

(def monster (atom {:x 13
                    :y 13}))

(defn render-monster []
  (canvas/draw-rect
   (* m/TILE-SIZE (:x @monster))
   (* m/TILE-SIZE (:y @monster))
   m/TILE-SIZE m/TILE-SIZE
   "red"))
