(ns game.player
  (:require
   [game.tile-map :as m]
   [game.canvas :as canvas]
   [game.actions :as a]))

;; STATE
(def player (atom {:x 2
                   :y 3}))

;; ACTIONS
(defn move-player [x y] {:type :MOVE_PLAYER :payload {:x x :y y}})

(defmethod a/handle :MOVE_PLAYER [action]
  (swap! player conj {:x (-> action :payload :x)
                      :y (-> action :payload :y)}))

;; RENDER
(defn render-player []
  (canvas/draw-rect
   (* m/TILE-SIZE (:x @player))
   (* m/TILE-SIZE (:y @player))
   m/TILE-SIZE m/TILE-SIZE
   "green"))
