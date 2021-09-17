(ns game.builders
  (:require [game.config :as config]))

(defn build-statue [statue-data]
  (merge statue-data config/statue))

(defn build-jelly [jelly-data]
  (merge jelly-data config/jelly))

(defn build-player [player-data]
  (merge player-data config/player))