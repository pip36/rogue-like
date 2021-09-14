(ns game.player
  (:require
   [game.tile-map :as m]
   [game.canvas :as canvas]))

(def player (atom {:x 2
                   :y 3}))

(defn render-player []
  (canvas/draw-rect
   (* m/TILE-SIZE (:x @player))
   (* m/TILE-SIZE (:y @player))
   m/TILE-SIZE m/TILE-SIZE
   "green"))
