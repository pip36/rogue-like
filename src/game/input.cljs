(ns game.input
  (:require
   [game.player :as p]
   [game.actions :as a]))

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT})

(defn handle-user-update [key]
  (case key
    :UP (a/trigger-actions  [(a/move-player (:x @p/player) (dec (:y @p/player)))])
    :DOWN (a/trigger-actions [(a/move-player (:x @p/player) (inc (:y @p/player)))])
    :LEFT (a/trigger-actions [(a/move-player (dec (:x @p/player)) (:y @p/player))])
    :RIGHT (a/trigger-actions  [(a/move-player (inc (:x @p/player)) (:y @p/player))])
    :default nil))