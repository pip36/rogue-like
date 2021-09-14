(ns game.ai
  (:require
   [game.actions :as a]
   [game.tile-map :as tm]
   [game.monster :as m]))

(defn try-move [[x y]]
  (when (= (:type (tm/get-tile x y)) :BLANK)
    (a/trigger-actions  [(a/move-monster x y)])))

(defn new-position [x y]
  [(+ (:x @m/monster) x) (+ (:y @m/monster) y)])

(defn handle-ai-update []
  (a/trigger-actions [(try-move (new-position (dec (rand-int 3)) (dec (rand-int 3))))]))
