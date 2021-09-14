(ns game.input
  (:require
   [game.player :as p]
   [game.actions :as a]
   [game.tile-map :as m]))

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT})

(defn new-position [x y]
  [(+ (:x @p/player) x) (+ (:y @p/player) y)])

(defn try-move [[x y]]
  (when (= (:type (m/get-tile x y)) :BLANK)
    (a/trigger-actions  [(a/move-player x y)])))

(defn handle-user-update [key]
  (case key
    :UP (try-move (new-position 0 -1))
    :DOWN (try-move (new-position 0 1))
    :LEFT (try-move (new-position -1 0))
    :RIGHT (try-move (new-position 1 0))
    :default nil))

(comment
  (m/get-tile 1 0))